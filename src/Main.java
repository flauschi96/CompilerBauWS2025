import java.util.*;

public class Main {

    public static void main(String[] args) {

        List<List<MiniCCompiler.Stmt>> testPrograms = new ArrayList<>();

        // === Test 1: einfache Variablen ===
        testPrograms.add(Arrays.asList(
                new MiniCCompiler.VarDecl(MiniCCompiler.PrimType.INT, "x", new MiniCCompiler.IntLiteral(5,1,0),1,0),
                new MiniCCompiler.VarDecl(MiniCCompiler.PrimType.INT, "y", new MiniCCompiler.Binary(
                        new MiniCCompiler.Variable("x",2,0),
                        MiniCCompiler.Operator.PLUS,
                        new MiniCCompiler.IntLiteral(3,2,4),
                        2,2),2,0)
        ));

        // === Test 2: Variable als Funktion (Fehler) ===
        testPrograms.add(Arrays.asList(
                new MiniCCompiler.VarDecl(MiniCCompiler.PrimType.INT,"a",null,1,0),
                new MiniCCompiler.ExprStmt(
                        new MiniCCompiler.Call("a", new ArrayList<>(),2,0),
                        2,0)
        ));

        // === Test 3: return type mismatch ===
        List<MiniCCompiler.Param> fParams = Arrays.asList(new MiniCCompiler.Param(MiniCCompiler.PrimType.INT,"a"),
                new MiniCCompiler.Param(MiniCCompiler.PrimType.STRING,"b"));
        MiniCCompiler.Block fBody = new MiniCCompiler.Block(
                Arrays.asList(new MiniCCompiler.ReturnStmt(new MiniCCompiler.Variable("a",2,0),2,0)),
                2,0);
        testPrograms.add(Arrays.asList(
                new MiniCCompiler.FnDecl(MiniCCompiler.PrimType.STRING,"f",fParams,fBody,1,0),
                new MiniCCompiler.VarDecl(MiniCCompiler.PrimType.INT,"x",new MiniCCompiler.Call(
                        "f",Arrays.asList(new MiniCCompiler.IntLiteral(1,3,0), new MiniCCompiler.StringLiteral("s",3,3)),3,0),3,0)
        ));

        // === Test 4: Redeclaration in inner scope ===
        MiniCCompiler.Block innerBlock = new MiniCCompiler.Block(
                Arrays.asList(
                        new MiniCCompiler.VarDecl(MiniCCompiler.PrimType.INT,"x",new MiniCCompiler.IntLiteral(2,2,0),2,0),
                        new MiniCCompiler.VarDecl(MiniCCompiler.PrimType.INT,"x",new MiniCCompiler.IntLiteral(3,3,0),3,0)
                ),1,0);
        testPrograms.add(Arrays.asList(
                new MiniCCompiler.VarDecl(MiniCCompiler.PrimType.INT,"x",new MiniCCompiler.IntLiteral(1,0,0),0,0),
                innerBlock
        ));

        // === Test 5: korrektes Funktionsaufruf ===
        List<MiniCCompiler.Param> addParams = Arrays.asList(
                new MiniCCompiler.Param(MiniCCompiler.PrimType.INT,"a"),
                new MiniCCompiler.Param(MiniCCompiler.PrimType.INT,"b"));
        MiniCCompiler.Block addBody = new MiniCCompiler.Block(
                Arrays.asList(
                        new MiniCCompiler.VarDecl(MiniCCompiler.PrimType.INT,"c",new MiniCCompiler.Binary(
                                new MiniCCompiler.Variable("a",2,0),
                                MiniCCompiler.Operator.PLUS,
                                new MiniCCompiler.Variable("b",2,2),2,4),2,0),
                        new MiniCCompiler.ReturnStmt(new MiniCCompiler.Variable("c",3,0),3,0)
                ),1,0);
        testPrograms.add(Arrays.asList(
                new MiniCCompiler.FnDecl(MiniCCompiler.PrimType.INT,"add",addParams,addBody,0,0),
                new MiniCCompiler.VarDecl(MiniCCompiler.PrimType.INT,"r",new MiniCCompiler.Call(
                        "add",Arrays.asList(new MiniCCompiler.IntLiteral(1,5,0),new MiniCCompiler.IntLiteral(2,5,2)),5,0),5,0)
        ));

        // === Test 6: absichtlich fehlerhaft ===
        testPrograms.add(Arrays.asList(
                new MiniCCompiler.VarDecl(MiniCCompiler.PrimType.INT,"b",null,1,0),
                new MiniCCompiler.ExprStmt(
                        new MiniCCompiler.Call("undefinedFunc", new ArrayList<>(),2,0),
                        2,0)
        ));

        // === Main Loop ===
        int t=1;
        for(List<MiniCCompiler.Stmt> program : testPrograms){
            System.out.println("=== Test "+(t++)+" ===");

            // Alte Ausgabe (Objekt + Speicheradresse)
            System.out.println("=== AST (Raw Objects) ===");
            for(MiniCCompiler.Stmt s: program) System.out.println(s);

            // Lesbare Ausgabe
            System.out.println("=== AST (Pretty Print) ===");
            for(MiniCCompiler.Stmt s: program) System.out.println(s.toString(""));

            // Semantische Analyse
            System.out.println("=== Semantische Analyse ===");
            SemanticAnalyzer sa = new SemanticAnalyzer(program);
            sa.analyze();
            if(sa.errors.isEmpty()) System.out.println("Keine Fehler gefunden. Semantische Analyse erfolgreich!");
            else {
                System.out.println("Semantische Analyse fehlgeschlagen!");
                sa.errors.forEach(System.out::println);
            }

            System.out.println();
        }
    }

    // ---------------- Simple Semantic Analyzer ----------------
    static class SemanticAnalyzer {
        List<String> errors = new ArrayList<>();
        List<MiniCCompiler.Stmt> program;

        SemanticAnalyzer(List<MiniCCompiler.Stmt> program){this.program=program;}

        void analyze(){
            Map<String,String> vars = new HashMap<>();
            Map<String,String> fns = new HashMap<>();

            for(MiniCCompiler.Stmt s: program){
                if(s instanceof MiniCCompiler.VarDecl vd){
                    if(vars.containsKey(vd.name)) errors.add(vd.pos()+": redeclaration of variable '"+vd.name+"'");
                    vars.put(vd.name, "var");
                } else if(s instanceof MiniCCompiler.FnDecl fd){
                    if(fns.containsKey(fd.name)) errors.add(fd.pos()+": redeclaration of function '"+fd.name+"'");
                    fns.put(fd.name,"fn");
                    for(MiniCCompiler.Param p: fd.params){
                        if(vars.containsKey(p.name)) errors.add(fd.pos()+": duplicate parameter '"+p.name+"'");
                        vars.put(p.name,"param");
                    }
                } else if(s instanceof MiniCCompiler.ExprStmt es && es.expr instanceof MiniCCompiler.Call c){
                    if(!vars.containsKey(c.name) && !fns.containsKey(c.name))
                        errors.add(c.pos()+": call to undefined function or variable '"+c.name+"'");
                } else if(s instanceof MiniCCompiler.Block b){
                    analyzeBlock(b, vars, fns);
                }
            }
        }

        private void analyzeBlock(MiniCCompiler.Block block, Map<String,String> vars, Map<String,String> fns){
            Map<String,String> localVars = new HashMap<>(vars);
            for(MiniCCompiler.Stmt s: block.statements){
                if(s instanceof MiniCCompiler.VarDecl vd){
                    if(localVars.containsKey(vd.name)) errors.add(vd.pos()+": redeclaration of variable '"+vd.name+"'");
                    localVars.put(vd.name, "var");
                } else if(s instanceof MiniCCompiler.ExprStmt es && es.expr instanceof MiniCCompiler.Call c){
                    if(!localVars.containsKey(c.name) && !fns.containsKey(c.name))
                        errors.add(c.pos()+": call to undefined function or variable '"+c.name+"'");
                } else if(s instanceof MiniCCompiler.Block innerBlock){
                    analyzeBlock(innerBlock, localVars, fns);
                }
            }
        }
    }
}
