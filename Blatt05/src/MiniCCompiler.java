import java.util.*;

public class MiniCCompiler {

    // ---------------- AST ----------------
    static abstract class Node {
        public final int line, col;
        Node(int l,int c){line=l;col=c;}
        public String pos(){return "line "+line+":"+col;}
        public abstract String toString(String indent);
        @Override public String toString() { return toString(""); }
    }

    static abstract class Stmt extends Node { Stmt(int l,int c){super(l,c);} }

    static class VarDecl extends Stmt {
        PrimType type; String name; Expr initializer;
        VarDecl(PrimType t,String n,Expr i,int l,int c){super(l,c);type=t;name=n;initializer=i;}
        @Override
        public String toString(String indent) {
            return indent+"VarDecl("+type+" "+name+(initializer!=null?" = "+initializer:"")+")";
        }
    }

    static class Assign extends Stmt {
        String name; Expr value;
        Assign(String n,Expr v,int l,int c){super(l,c);name=n;value=v;}
        @Override
        public String toString(String indent) {
            return indent+"Assign("+name+" = "+value+")";
        }
    }

    static class FnDecl extends Stmt {
        PrimType returnType; String name; List<Param> params; Block body;
        FnDecl(PrimType r,String n,List<Param> p,Block b,int l,int c){super(l,c);returnType=r;name=n;params=p;body=b;}
        @Override
        public String toString(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent+"FnDecl("+returnType+" "+name+"(");
            for(int i=0;i<params.size();i++){
                sb.append(params.get(i));
                if(i<params.size()-1) sb.append(", ");
            }
            sb.append("))\n");
            sb.append(body.toString(indent+"  "));
            return sb.toString();
        }
    }

    static class ReturnStmt extends Stmt {
        Expr value;
        ReturnStmt(Expr v,int l,int c){super(l,c);value=v;}
        @Override
        public String toString(String indent) { return indent+"Return("+value+")"; }
    }

    static class ExprStmt extends Stmt {
        Expr expr;
        ExprStmt(Expr e,int l,int c){super(l,c);expr=e;}
        @Override
        public String toString(String indent) { return indent+"ExprStmt("+expr+")"; }
    }

    static class Block extends Stmt {
        List<Stmt> statements;
        Block(List<Stmt> s,int l,int c){super(l,c);statements=s;}
        @Override
        public String toString(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent+"Block:\n");
            for(Stmt s: statements) sb.append(s.toString(indent+"  ")+"\n");
            return sb.toString();
        }
    }

    static class WhileStmt extends Stmt {
        Expr condition; Block body;
        WhileStmt(Expr cond,Block b,int l,int c){super(l,c);condition=cond;body=b;}
        @Override
        public String toString(String indent) {
            return indent+"While("+condition+")\n"+body.toString(indent+"  ");
        }
    }

    static class IfStmt extends Stmt {
        Expr condition; Block thenBranch; Block elseBranch;
        IfStmt(Expr cond,Block t,Block e,int l,int c){super(l,c);condition=cond;thenBranch=t;elseBranch=e;}
        @Override
        public String toString(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent+"If("+condition+")\n");
            sb.append(thenBranch.toString(indent+"  "));
            if(elseBranch!=null){
                sb.append(indent+"Else\n");
                sb.append(elseBranch.toString(indent+"  "));
            }
            return sb.toString();
        }
    }

    // ---------------- Expr ----------------
    static abstract class Expr extends Node { Expr(int l,int c){super(l,c);} }
    static class IntLiteral extends Expr {
        int value; IntLiteral(int v,int l,int c){super(l,c);value=v;}
        @Override public String toString(String indent){ return indent+"Int("+value+")"; }
    }
    static class StringLiteral extends Expr {
        String value; StringLiteral(String v,int l,int c){super(l,c);value=v;}
        @Override public String toString(String indent){ return indent+"String(\""+value+"\")"; }
    }
    static class BoolLiteral extends Expr {
        boolean value; BoolLiteral(boolean v,int l,int c){super(l,c);value=v;}
        @Override public String toString(String indent){ return indent+"Bool("+value+")"; }
    }
    static class Variable extends Expr {
        String name; Variable(String n,int l,int c){super(l,c);name=n;}
        @Override public String toString(String indent){ return indent+"Var("+name+")"; }
    }
    static class Binary extends Expr {
        Expr left; Operator op; Expr right;
        Binary(Expr l,Operator o,Expr r,int ln,int cn){super(ln,cn);left=l;op=o;right=r;}
        @Override
        public String toString(String indent){
            return indent+"Binary("+left+" "+op+" "+right+")";
        }
    }
    static class Call extends Expr {
        String name; List<Expr> args;
        Call(String n,List<Expr> a,int l,int c){super(l,c);name=n;args=a;}
        @Override
        public String toString(String indent){
            StringBuilder sb = new StringBuilder();
            sb.append(indent+"Call("+name+"(");
            for(int i=0;i<args.size();i++){
                sb.append(args.get(i));
                if(i<args.size()-1) sb.append(", ");
            }
            sb.append("))");
            return sb.toString();
        }
    }

    // ---------------- Param ----------------
    static class Param {
        PrimType type; String name;
        Param(PrimType t,String n){type=t;name=n;}
        @Override
        public String toString(){ return type+" "+name; }
    }

    enum PrimType { INT, STRING, BOOL }
    enum Operator { EQ, NEQ, PLUS, MINUS, MUL, DIV, LT, GT }

}
