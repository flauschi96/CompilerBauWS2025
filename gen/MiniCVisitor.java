// Generated from U:/Compilerbau/SemantischeAnalyse/Semantische Analyse/src/MiniC.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MiniCParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MiniCVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MiniCParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(MiniCParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(MiniCParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#vardecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVardecl(MiniCParser.VardeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#assign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssign(MiniCParser.AssignContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#fndecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFndecl(MiniCParser.FndeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#params}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParams(MiniCParser.ParamsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#returnStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStmt(MiniCParser.ReturnStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#fncall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFncall(MiniCParser.FncallContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgs(MiniCParser.ArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(MiniCParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#whileStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStmt(MiniCParser.WhileStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#cond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCond(MiniCParser.CondContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(MiniCParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(MiniCParser.TypeContext ctx);
}