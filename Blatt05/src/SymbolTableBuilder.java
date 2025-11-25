import java.util.*;

public class SymbolTableBuilder {

    public static class Symbol {
        String name;
        MiniCCompiler.PrimType type;
        boolean isFunction;
        List<MiniCCompiler.Param> params; // nur für Funktionen
        MiniCCompiler.Node declNode;

        public Symbol(String name, MiniCCompiler.PrimType type, boolean isFunction, List<MiniCCompiler.Param> params, MiniCCompiler.Node declNode) {
            this.name = name;
            this.type = type;
            this.isFunction = isFunction;
            this.params = params;
            this.declNode = declNode;
        }
    }

    public static class Scope {
        Map<String, Symbol> symbols = new HashMap<>();
        Scope parent;

        public Scope(Scope parent) { this.parent = parent; }

        public boolean containsInCurrent(String name) { return symbols.containsKey(name); }

        public void addSymbol(Symbol sym) { symbols.put(sym.name, sym); }

        public Symbol resolve(String name) {
            Symbol sym = symbols.get(name);
            if (sym != null) return sym;
            if (parent != null) return parent.resolve(name);
            return null;
        }
    }

    private Scope currentScope;
    private List<String> errors = new ArrayList<>();

    public SymbolTableBuilder() {
        currentScope = new Scope(null); // globaler Scope
    }

    public List<String> getErrors() { return errors; }


    public void build(List<MiniCCompiler.Stmt> program) {
        for (MiniCCompiler.Stmt s : program) visitStmt(s);
    }

    private void visitStmt(MiniCCompiler.Stmt stmt) {
        if (stmt instanceof MiniCCompiler.VarDecl vd) {
            if (currentScope.containsInCurrent(vd.name)) {
                errors.add("Fehler: Variable '" + vd.name + "' bereits im aktuellen Scope definiert (" + vd.pos() + ")");
            } else {
                currentScope.addSymbol(new Symbol(vd.name, vd.type, false, null, vd));
            }
            if (vd.initializer != null) visitExpr(vd.initializer);

        } else if (stmt instanceof MiniCCompiler.Assign as) {
            Symbol sym = currentScope.resolve(as.name);
            if (sym == null) errors.add("Fehler: Variable '" + as.name + "' nicht definiert (" + as.pos() + ")");
            else if (sym.isFunction) errors.add("Fehler: '" + as.name + "' ist eine Funktion, keine Variable (" + as.pos() + ")");
            visitExpr(as.value);

        } else if (stmt instanceof MiniCCompiler.FnDecl fd) {
            if (currentScope.containsInCurrent(fd.name)) {
                errors.add("Fehler: Funktion '" + fd.name + "' bereits im aktuellen Scope definiert (" + fd.pos() + ")");
            } else {
                currentScope.addSymbol(new Symbol(fd.name, fd.returnType, true, fd.params, fd));
            }
            // Scope für Funktionskörper
            enterScope();
            for (MiniCCompiler.Param p : fd.params) {
                if (currentScope.containsInCurrent(p.name)) {
                    errors.add("Fehler: Parameter '" + p.name + "' mehrfach definiert (" + fd.pos() + ")");
                } else {
                    currentScope.addSymbol(new Symbol(p.name, p.type, false, null, fd));
                }
            }
            visitStmt(fd.body);
            exitScope();

        } else if (stmt instanceof MiniCCompiler.Block bl) {
            enterScope();
            for (MiniCCompiler.Stmt s : bl.statements) visitStmt(s);
            exitScope();

        } else if (stmt instanceof MiniCCompiler.WhileStmt ws) {
            visitExpr(ws.condition);
            visitStmt(ws.body);

        } else if (stmt instanceof MiniCCompiler.IfStmt ifs) {
            visitExpr(ifs.condition);
            visitStmt(ifs.thenBranch);
            if (ifs.elseBranch != null) visitStmt(ifs.elseBranch);

        } else if (stmt instanceof MiniCCompiler.ReturnStmt rs) {
            if (rs.value != null) visitExpr(rs.value);

        } else if (stmt instanceof MiniCCompiler.ExprStmt es) {
            visitExpr(es.expr);

        } else {
            errors.add("Unbekannter Statement-Typ: " + stmt.getClass().getSimpleName());
        }
    }

    private void visitExpr(MiniCCompiler.Expr expr) {
        if (expr instanceof MiniCCompiler.Binary b) {
            visitExpr(b.left);
            visitExpr(b.right);

        } else if (expr instanceof MiniCCompiler.Call c) {
            Symbol sym = currentScope.resolve(c.name);
            if (sym == null) errors.add("Fehler: Funktion '" + c.name + "' nicht definiert (" + c.pos() + ")");
            else if (!sym.isFunction) errors.add("Fehler: '" + c.name + "' ist keine Funktion (" + c.pos() + ")");
            for (MiniCCompiler.Expr e : c.args) visitExpr(e);

        } else if (expr instanceof MiniCCompiler.Variable v) {
            Symbol sym = currentScope.resolve(v.name);
            if (sym == null) errors.add("Fehler: Variable '" + v.name + "' nicht definiert (" + v.pos() + ")");

        } else if (expr instanceof MiniCCompiler.IntLiteral || expr instanceof MiniCCompiler.StringLiteral || expr instanceof MiniCCompiler.BoolLiteral) {

        } else {
            errors.add("Unbekannter Expression-Typ: " + expr.getClass().getSimpleName());
        }
    }

    private void enterScope() { currentScope = new Scope(currentScope); }

    private void exitScope() { if (currentScope.parent != null) currentScope = currentScope.parent; }
}
