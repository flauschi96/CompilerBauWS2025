import java.util.*;

public class ParseTreeToAst extends MiniCBaseVisitor<Object> {

    @Override
    public List<MiniCCompiler.Stmt> visitProgram(MiniCParser.ProgramContext ctx) {
        List<MiniCCompiler.Stmt> stmts = new ArrayList<>();
        for (MiniCParser.StmtContext sctx : ctx.stmt()) {
            MiniCCompiler.Stmt s = (MiniCCompiler.Stmt) visit(sctx);
            if (s != null) stmts.add(s);
        }
        return stmts;
    }

    @Override
    public MiniCCompiler.Stmt visitVardecl(MiniCParser.VardeclContext ctx) {
        MiniCCompiler.PrimType type = parseType(ctx.type().getText());
        String name = ctx.ID().getText();
        MiniCCompiler.Expr init = ctx.expr() != null ? visitExpr(ctx.expr()) : null;
        return new MiniCCompiler.VarDecl(type, name, init, ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public MiniCCompiler.Stmt visitAssign(MiniCParser.AssignContext ctx) {
        String name = ctx.ID().getText();
        MiniCCompiler.Expr value = visitExpr(ctx.expr());
        return new MiniCCompiler.Assign(name, value, ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public MiniCCompiler.Stmt visitFndecl(MiniCParser.FndeclContext ctx) {
        MiniCCompiler.PrimType retType = parseType(ctx.type().getText());
        String name = ctx.ID().getText();
        List<MiniCCompiler.Param> params = new ArrayList<>();

        if (ctx.params() != null) {
            MiniCParser.ParamsContext pctx = ctx.params();
            for (int i = 0; i < pctx.type().size(); i++) {
                MiniCCompiler.PrimType pType = parseType(pctx.type(i).getText());
                String pName = pctx.ID(i).getText();
                params.add(new MiniCCompiler.Param(pType, pName));
            }
        }

        MiniCCompiler.Block body = (MiniCCompiler.Block) visit(ctx.block());
        return new MiniCCompiler.FnDecl(retType, name, params, body, ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public MiniCCompiler.Stmt visitBlock(MiniCParser.BlockContext ctx) {
        List<MiniCCompiler.Stmt> stmts = new ArrayList<>();
        for (MiniCParser.StmtContext sctx : ctx.stmt()) {
            MiniCCompiler.Stmt s = (MiniCCompiler.Stmt) visit(sctx);
            if (s != null) stmts.add(s);
        }
        return new MiniCCompiler.Block(stmts, ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public MiniCCompiler.Stmt visitWhileStmt(MiniCParser.WhileStmtContext ctx) {
        MiniCCompiler.Expr cond = visitExpr(ctx.expr());
        MiniCCompiler.Block body = (MiniCCompiler.Block) visit(ctx.block());
        return new MiniCCompiler.WhileStmt(cond, body, ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public MiniCCompiler.Stmt visitCond(MiniCParser.CondContext ctx) {
        MiniCCompiler.Expr cond = visitExpr(ctx.expr());
        MiniCCompiler.Block thenBlock = (MiniCCompiler.Block) visit(ctx.block(0));
        MiniCCompiler.Block elseBlock = ctx.block().size() > 1 ? (MiniCCompiler.Block) visit(ctx.block(1)) : null;
        return new MiniCCompiler.IfStmt(cond, thenBlock, elseBlock, ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public MiniCCompiler.Stmt visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        MiniCCompiler.Expr value = ctx.expr() != null ? visitExpr(ctx.expr()) : null;
        return new MiniCCompiler.ReturnStmt(value, ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public MiniCCompiler.Stmt visitStmt(MiniCParser.StmtContext ctx) {
        if (ctx.vardecl() != null) return visitVardecl(ctx.vardecl());
        if (ctx.assign() != null) return visitAssign(ctx.assign());
        if (ctx.fndecl() != null) return visitFndecl(ctx.fndecl());
        if (ctx.block() != null) return visitBlock(ctx.block());
        if (ctx.whileStmt() != null) return visitWhileStmt(ctx.whileStmt());
        if (ctx.cond() != null) return visitCond(ctx.cond());
        if (ctx.returnStmt() != null) return visitReturnStmt(ctx.returnStmt());
        if (ctx.expr() != null) return new MiniCCompiler.ExprStmt(visitExpr(ctx.expr()), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        throw new RuntimeException("Unknown statement: " + ctx.getText());
    }

    public MiniCCompiler.Expr visitExpr(MiniCParser.ExprContext ctx) {
        if (ctx.fncall() != null) {
            MiniCParser.FncallContext fctx = ctx.fncall();
            String name = fctx.ID().getText();
            List<MiniCCompiler.Expr> args = new ArrayList<>();
            if (fctx.args() != null) {
                for (MiniCParser.ExprContext ectx : fctx.args().expr()) {
                    args.add(visitExpr(ectx));
                }
            }
            return new MiniCCompiler.Call(name, args, ctx.start.getLine(), ctx.start.getCharPositionInLine());
        } else if (ctx.NUMBER() != null) {
            return new MiniCCompiler.IntLiteral(Integer.parseInt(ctx.NUMBER().getText()), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        } else if (ctx.STRING() != null) {
            String s = ctx.STRING().getText();
            s = s.substring(1, s.length() - 1); // Quotes entfernen
            return new MiniCCompiler.StringLiteral(s, ctx.start.getLine(), ctx.start.getCharPositionInLine());
        } else if (ctx.ID() != null) {
            return new MiniCCompiler.Variable(ctx.ID().getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        } else if (ctx.getChildCount() == 3) { // Binäroperator oder Klammern
            MiniCCompiler.Expr left = visitExpr(ctx.expr(0));
            MiniCCompiler.Expr right = visitExpr(ctx.expr(1));
            String opStr = ctx.getChild(1).getText();
            MiniCCompiler.Operator op;
            switch (opStr) {
                case "+": op = MiniCCompiler.Operator.PLUS; break;
                case "-": op = MiniCCompiler.Operator.MINUS; break;
                case "*": op = MiniCCompiler.Operator.MUL; break;
                case "/": op = MiniCCompiler.Operator.DIV; break;
                case ">": op = MiniCCompiler.Operator.GT; break;
                case "<": op = MiniCCompiler.Operator.LT; break;
                case "==": op = MiniCCompiler.Operator.EQ; break;
                case "!=": op = MiniCCompiler.Operator.NEQ; break;
                default: throw new RuntimeException("Unsupported operator: " + opStr);
            }
            return new MiniCCompiler.Binary(left, op, right, ctx.start.getLine(), ctx.start.getCharPositionInLine());
        } else if (ctx.getChildCount() == 1) { // Klammer oder BoolLiteral
            String t = ctx.getText();
            if (t.equals("T")) return new MiniCCompiler.BoolLiteral(true, ctx.start.getLine(), ctx.start.getCharPositionInLine());
            if (t.equals("F")) return new MiniCCompiler.BoolLiteral(false, ctx.start.getLine(), ctx.start.getCharPositionInLine());
            if (ctx.expr().size() == 1) return visitExpr(ctx.expr(0));
        }
        throw new RuntimeException("Unsupported expression: " + ctx.getText());
    }

    private MiniCCompiler.PrimType parseType(String t) {
        return switch(t) {
            case "int" -> MiniCCompiler.PrimType.INT;
            case "string" -> MiniCCompiler.PrimType.STRING;
            case "bool" -> MiniCCompiler.PrimType.BOOL;
            default -> throw new RuntimeException("Unknown type: " + t);
        };
    }
}
