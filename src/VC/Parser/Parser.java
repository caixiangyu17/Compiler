/*
 * Parser.java            
 *
 * This parser for a subset of the VC language is intended to 
 *  demonstrate how to create the AST nodes, including (among others): 
 *  [1] a list (of statements)
 *  [2] a function
 *  [3] a statement (which is an expression statement), 
 *  [4] a unary expression
 *  [5] a binary expression
 *  [6] terminals (identifiers, integer literals and operators)
 *
 * In addition, it also demonstrates how to use the two methods start 
 * and finish to determine the position information for the start and 
 * end of a construct (known as a phrase) corresponding an AST node.
 *
 * NOTE THAT THE POSITION INFORMATION WILL NOT BE MARKED. HOWEVER, IT CAN BE
 * USEFUL TO DEBUG YOUR IMPLEMENTATION.
 *
 * (10-*-April-*-2015)


program       -> func-decl
func-decl     -> type identifier "(" ")" compound-stmt
type          -> void
identifier    -> ID
// statements
compound-stmt -> "{" stmt* "}" 
stmt          -> expr-stmt
expr-stmt     -> expr? ";"
// expressions 
expr                -> additive-expr
additive-expr       -> multiplicative-expr
                    |  additive-expr "+" multiplicative-expr
                    |  additive-expr "-" multiplicative-expr
multiplicative-expr -> unary-expr
	            |  multiplicative-expr "*" unary-expr
	            |  multiplicative-expr "/" unary-expr
unary-expr          -> "-" unary-expr
		    |  primary-expr

primary-expr        -> identifier
 		    |  INTLITERAL
		    | "(" expr ")"
 */

package VC.Parser;

import VC.ErrorReporter;
import VC.ASTs.Arg;
import VC.ASTs.ArgList;
import VC.ASTs.ArrayExpr;
import VC.ASTs.ArrayType;
import VC.ASTs.AssignExpr;
import VC.ASTs.BinaryExpr;
import VC.ASTs.BooleanExpr;
import VC.ASTs.BooleanLiteral;
import VC.ASTs.BooleanType;
import VC.ASTs.BreakStmt;
import VC.ASTs.CallExpr;
import VC.ASTs.CompoundStmt;
import VC.ASTs.ContinueStmt;
import VC.ASTs.Decl;
import VC.ASTs.DeclList;
import VC.ASTs.EmptyArgList;
import VC.ASTs.EmptyCompStmt;
import VC.ASTs.EmptyDeclList;
import VC.ASTs.EmptyExpr;
import VC.ASTs.EmptyExprList;
import VC.ASTs.EmptyParaList;
import VC.ASTs.EmptyStmtList;
import VC.ASTs.Expr;
import VC.ASTs.ExprList;
import VC.ASTs.ExprStmt;
import VC.ASTs.FloatExpr;
import VC.ASTs.FloatLiteral;
import VC.ASTs.FloatType;
import VC.ASTs.ForStmt;
import VC.ASTs.FuncDecl;
import VC.ASTs.GlobalVarDecl;
import VC.ASTs.Ident;
import VC.ASTs.IfStmt;
import VC.ASTs.InitExpr;
import VC.ASTs.IntExpr;
import VC.ASTs.IntLiteral;
import VC.ASTs.IntType;
import VC.ASTs.List;
import VC.ASTs.LocalVarDecl;
import VC.ASTs.Operator;
import VC.ASTs.ParaDecl;
import VC.ASTs.ParaList;
import VC.ASTs.Program;
import VC.ASTs.ReturnStmt;
import VC.ASTs.SimpleVar;
import VC.ASTs.Stmt;
import VC.ASTs.StmtList;
import VC.ASTs.StringExpr;
import VC.ASTs.StringLiteral;
import VC.ASTs.Type;
import VC.ASTs.UnaryExpr;
import VC.ASTs.Var;
import VC.ASTs.VarExpr;
import VC.ASTs.VoidType;
import VC.ASTs.WhileStmt;
import VC.Scanner.Scanner;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;

public class Parser {

	private Scanner scanner;
	private ErrorReporter errorReporter;
	private Token currentToken;
	private SourcePosition previousTokenPosition;
	private SourcePosition dummyPos = new SourcePosition();

	public Parser(Scanner lexer, ErrorReporter reporter) {
		scanner = lexer;
		errorReporter = reporter;

		previousTokenPosition = new SourcePosition();

		currentToken = scanner.getToken();
	}

	// match checks to see f the current token matches tokenExpected.
	// If so, fetches the next token.
	// If not, reports a syntactic error.

	void match(int tokenExpected) throws SyntaxError {
		if (currentToken.kind == tokenExpected) {
			previousTokenPosition = currentToken.position;
			currentToken = scanner.getToken();
		} else {
			syntacticError("\"%\" expected here", Token.spell(tokenExpected));
		}
	}

	void accept() {
		previousTokenPosition = currentToken.position;
		currentToken = scanner.getToken();
	}

	void syntacticError(String messageTemplate, String tokenQuoted)
			throws SyntaxError {
		SourcePosition pos = currentToken.position;
		errorReporter.reportError(messageTemplate, tokenQuoted, pos);
		throw (new SyntaxError());
	}

	// start records the position of the start of a phrase.
	// This is defined to be the position of the first
	// character of the first token of the phrase.

	void start(SourcePosition position) {
		position.lineStart = currentToken.position.lineStart;
		position.charStart = currentToken.position.charStart;
	}

	// finish records the position of the end of a phrase.
	// This is defined to be the position of the last
	// character of the last token of the phrase.

	
	void finish(SourcePosition position) {
		position.lineFinish = previousTokenPosition.lineFinish;
		position.charFinish = previousTokenPosition.charFinish;
	}

	void copyStart(SourcePosition from, SourcePosition to) {
		to.lineStart = from.lineStart;
		to.charStart = from.charStart;
	}

	// ========================== PROGRAMS ========================

	public Program parseProgram() {

		Program programAST = null;

		SourcePosition programPos = new SourcePosition();
		start(programPos);

		try {
			List dlAST = parseDeclList();
			finish(programPos);
			programAST = new Program(dlAST, programPos);
			if (currentToken.kind != Token.EOF) {
				syntacticError("\"%\" unknown type", currentToken.spelling);
			}
		} catch (SyntaxError s) {
			return null;
		}
		return programAST;
	}

	// ========================== DECLARATIONS ========================

	Type tAST = null;

	List parseDeclList() throws SyntaxError {
		List dlAST = null;
		Decl dAST = null;
		SourcePosition delListPos = new SourcePosition();
		start(delListPos);
		Ident iAST = null;
		if (currentToken.kind == Token.EOF) {
			return new EmptyDeclList(dummyPos);
		}

		if (currentToken.kind == Token.VOID || currentToken.kind == Token.INT
				|| currentToken.kind == Token.FLOAT
				|| currentToken.kind == Token.BOOLEAN) {
			tAST = parseType();
			iAST = parseIdent();
		} else {
			iAST = parseIdent();
		}

		if (currentToken.kind == Token.LPAREN) {
			dAST = parseFuncDecl(tAST, iAST);
			dlAST = parseDeclList();
		} else {
			dAST = parseGlobalVarDecl(tAST, iAST);
			if (currentToken.kind == Token.SEMICOLON
					|| currentToken.kind == Token.COMMA) {
				acceptOperator();
				dlAST = parseDeclList();
			}
		}
		if (dlAST == null) {
			dlAST = new EmptyDeclList(dummyPos);
		} else {
			finish(delListPos);
			dlAST = new DeclList(dAST, dlAST, delListPos);
		}

		return dlAST;
	}

	GlobalVarDecl parseGlobalVarDecl(Type tAST, Ident iAST) throws SyntaxError {
		GlobalVarDecl globalVarAST = null;

		SourcePosition globalVarPos = new SourcePosition();
		start(globalVarPos);

		if (currentToken.kind == Token.LBRACKET) {
			acceptOperator();
			Expr dAST = new EmptyExpr(dummyPos);
			if (currentToken.kind != Token.RBRACKET) {
				dAST = parseExpr();
			}
			match(Token.RBRACKET);
			finish(globalVarPos);
			tAST = new ArrayType(tAST, dAST, globalVarPos);
		}

		Expr eAST = new EmptyExpr(dummyPos);
		if (currentToken.kind == Token.EQ) {
			acceptOperator();
			eAST = parseInitialiser();

		}
		finish(globalVarPos);
		globalVarAST = new GlobalVarDecl(tAST, iAST, eAST, globalVarPos);
		return globalVarAST;
	}

	// List parseFuncDeclList() throws SyntaxError {
	// List dlAST = null;
	// Decl dAST = null;
	//
	// SourcePosition funcPos = new SourcePosition();
	// start(funcPos);
	//
	// dAST = parseFuncDecl();
	//
	// if (currentToken.kind == Token.VOID || currentToken.kind == Token.INT
	// || currentToken.kind == Token.BOOLEAN
	// || currentToken.kind == Token.FLOAT) {
	// dlAST = parseFuncDeclList();
	// finish(funcPos);
	// dlAST = new DeclList(dAST, dlAST, funcPos);
	// } else if (dAST != null) {
	// finish(funcPos);
	// dlAST = new DeclList(dAST, new EmptyDeclList(dummyPos), funcPos);
	// }
	// if (dlAST == null)
	// dlAST = new EmptyDeclList(dummyPos);
	//
	// return dlAST;
	// }

	Decl parseFuncDecl(Type tAST, Ident iAST) throws SyntaxError {
		Decl fAST = null;
		SourcePosition funcPos = new SourcePosition();
		start(funcPos);
		List fplAST = parseParaList();
		Stmt cAST = parseCompoundStmt();
		finish(funcPos);
		fAST = new FuncDecl(tAST, iAST, fplAST, cAST, funcPos);
		return fAST;
	}

	void parseDeclarator() throws SyntaxError {
		if (currentToken.kind == Token.LBRACKET) {
			acceptOperator();
			if (currentToken.kind != Token.RBRACKET) {
				parseIntLiteral();
				match(Token.RBRACKET);
			} else {
				match(Token.RBRACKET);
			}
		}

	}

	Expr parseInitialiser() throws SyntaxError {
		Expr initialiserAST = null;
		SourcePosition initialiserPos = new SourcePosition();
		start(initialiserPos);
		if (currentToken.kind == Token.LCURLY) {
			acceptOperator();
			List lAST = parseArrayExprList();
			match(Token.RCURLY);
			finish(initialiserPos);
			initialiserAST = new InitExpr(lAST, initialiserPos);
		} else {
			initialiserAST = parseExpr();
		}
		return initialiserAST;
	}

	List parseArrayExprList() throws SyntaxError {
		List arrayExprList = null;
		SourcePosition arrayExprPos = new SourcePosition();
		start(arrayExprPos);
		Expr eAST = parseExpr();
		if (currentToken.kind == Token.COMMA) {
			acceptOperator();
			List elAST = parseArrayExprList();
			finish(arrayExprPos);
			arrayExprList = new ExprList(eAST, elAST, arrayExprPos);
		} else {
			finish(arrayExprPos);
			arrayExprList = new ExprList(eAST, new EmptyExprList(arrayExprPos),
					arrayExprPos);
		}
		return arrayExprList;
	}

	List parseLocalVarDeclList() throws SyntaxError {
		List localVarDeclListAst = null;
		SourcePosition localVarListPos = new SourcePosition();
		start(localVarListPos);
		Type tAST = parseType();
		LocalVarDecl dAST = parseLocalVarDecl(tAST);
		if (currentToken.kind == Token.SEMICOLON) {
			acceptOperator();
			if (currentToken.kind == Token.VOID
					|| currentToken.kind == Token.INT
					|| currentToken.kind == Token.FLOAT
					|| currentToken.kind == Token.BOOLEAN) {
				List dlAST = parseLocalVarDeclList();
				finish(localVarListPos);
				localVarDeclListAst = new DeclList(dAST, dlAST, localVarListPos);
			} else {
				finish(localVarListPos);
				localVarDeclListAst = new DeclList(dAST, new EmptyDeclList(
						dummyPos), localVarListPos);
			}
		}
		if (currentToken.kind == Token.COMMA) {
			acceptOperator();
			List dlAST = parseLocalVarDeclList(tAST);
			finish(localVarListPos);
			localVarDeclListAst = new DeclList(dAST, dlAST, localVarListPos);
		}

		return localVarDeclListAst;
	}

	List parseLocalVarDeclList(Type tAST) throws SyntaxError {
		List localVarDeclListAst = null;
		SourcePosition localVarListPos = new SourcePosition();
		start(localVarListPos);
		LocalVarDecl dAST = parseLocalVarDecl(tAST);
		if (currentToken.kind == Token.SEMICOLON) {
			acceptOperator();
			if (currentToken.kind == Token.VOID
					|| currentToken.kind == Token.INT
					|| currentToken.kind == Token.FLOAT
					|| currentToken.kind == Token.BOOLEAN) {
				List dlAST = parseLocalVarDeclList();
				finish(localVarListPos);
				localVarDeclListAst = new DeclList(dAST, dlAST, localVarListPos);
			} else {
				finish(localVarListPos);
				localVarDeclListAst = new DeclList(dAST, new EmptyDeclList(
						dummyPos), localVarListPos);
			}
		}
		if (currentToken.kind == Token.COMMA) {
			acceptOperator();
			List dlAST = parseLocalVarDeclList(tAST);
			finish(localVarListPos);
			localVarDeclListAst = new DeclList(dAST, dlAST, localVarListPos);
		}

		return localVarDeclListAst;
	}

	LocalVarDecl parseLocalVarDecl(Type tAST) throws SyntaxError {
		LocalVarDecl localVarAST = null;

		SourcePosition localVarPos = new SourcePosition();
		start(localVarPos);

		Ident iAST = parseIdent();

		if (currentToken.kind == Token.LBRACKET) {
			acceptOperator();
			Expr dAST = new EmptyExpr(dummyPos);
			if (currentToken.kind != Token.RBRACKET) {
				dAST = parseExpr();
			}
			match(Token.RBRACKET);
			finish(localVarPos);
			tAST = new ArrayType(tAST, dAST, localVarPos);
		}

		Expr eAST = new EmptyExpr(dummyPos);
		if (currentToken.kind == Token.EQ) {
			acceptOperator();
			eAST = parseInitialiser();
		}
		finish(localVarPos);
		localVarAST = new LocalVarDecl(tAST, iAST, eAST, localVarPos);
		return localVarAST;
	}

	// ======================== TYPES ==========================

	Type parseType() throws SyntaxError {
		Type typeAST = null;

		SourcePosition typePos = new SourcePosition();
		start(typePos);

		if (currentToken.kind == Token.VOID) {
			typeAST = new VoidType(typePos);
		} else if (currentToken.kind == Token.INT) {
			typeAST = new IntType(typePos);
		} else if (currentToken.kind == Token.BOOLEAN) {
			typeAST = new BooleanType(typePos);
		} else if (currentToken.kind == Token.FLOAT) {
			typeAST = new FloatType(typePos);
		}
		accept();
		finish(typePos);

		return typeAST;
	}

	// ======================= STATEMENTS ==============================

	Stmt parseCompoundStmt() throws SyntaxError {
		Stmt cAST = null;

		SourcePosition stmtPos = new SourcePosition();
		start(stmtPos);

		match(Token.LCURLY);
		// Insert code here to build a DeclList node for variable declarations
		List dlAST = new EmptyDeclList(dummyPos);
		if (currentToken.kind == Token.VOID || currentToken.kind == Token.INT
				|| currentToken.kind == Token.FLOAT
				|| currentToken.kind == Token.BOOLEAN) {
			dlAST = parseLocalVarDeclList();
		}
		List slAST = parseStmtList();
		match(Token.RCURLY);
		finish(stmtPos);

		/*
		 * In the subset of the VC grammar, no variable declarations are
		 * allowed. Therefore, a block is empty iff it has no statements.
		 */
		if (slAST instanceof EmptyStmtList && dlAST instanceof EmptyDeclList)
			cAST = new EmptyCompStmt(stmtPos);
		else
			cAST = new CompoundStmt(dlAST, slAST, stmtPos);
		return cAST;
	}

	List parseStmtList() throws SyntaxError {
		List slAST = null;

		SourcePosition stmtPos = new SourcePosition();
		start(stmtPos);

		if (currentToken.kind != Token.RCURLY) {
			Stmt sAST = parseStmt();
			{
				if (currentToken.kind != Token.RCURLY) {
					slAST = parseStmtList();
					finish(stmtPos);
					slAST = new StmtList(sAST, slAST, stmtPos);
				} else {
					finish(stmtPos);
					slAST = new StmtList(sAST, new EmptyStmtList(dummyPos),
							stmtPos);
				}
			}
		} else
			slAST = new EmptyStmtList(dummyPos);

		return slAST;
	}

	Stmt parseStmt() throws SyntaxError {
		Stmt sAST = null;

		switch (currentToken.kind) {
		case Token.LCURLY:
			sAST = parseCompoundStmt();
			break;
		case Token.IF:
			sAST = parseIfStmt();
			break;
		case Token.FOR:
			sAST = parseForStmt();
			break;
		case Token.WHILE:
			sAST = parseWhileStmt();
			break;
		case Token.BREAK:
			sAST = parseBreakStmt();
			break;
		case Token.CONTINUE:
			sAST = parseContinueStmt();
			break;
		case Token.RETURN:
			sAST = parseReturnStmt();
			break;
		default:
			sAST = parseExprStmt();
			break;

		}
		return sAST;
	}

	IfStmt parseIfStmt() throws SyntaxError {

		IfStmt ifStmtAST = null;

		SourcePosition ifStmtPos = new SourcePosition();
		start(ifStmtPos);

		match(Token.IF);
		match(Token.LPAREN);
		Expr eAST = parseExpr();
		match(Token.RPAREN);
		Stmt s1AST = parseStmt();
		if (currentToken.kind == Token.ELSE) {
			acceptOperator();
			Stmt s2AST = parseStmt();
			finish(ifStmtPos);
			ifStmtAST = new IfStmt(eAST, s1AST, s2AST, ifStmtPos);
		} else {
			finish(ifStmtPos);
			ifStmtAST = new IfStmt(eAST, s1AST, ifStmtPos);
		}

		return ifStmtAST;
	}

	ForStmt parseForStmt() throws SyntaxError {

		ForStmt forStmtAST = null;
		SourcePosition forStmtPos = new SourcePosition();
		start(forStmtPos);
		Expr e1AST = null;
		Expr e2AST = null;
		Expr e3AST = null;

		match(Token.FOR);
		match(Token.LPAREN);
		if (currentToken.kind != Token.SEMICOLON) {
			e1AST = parseExpr();
			match(Token.SEMICOLON);
		} else {
			acceptOperator();
		}
		if (currentToken.kind != Token.SEMICOLON) {
			e2AST = parseExpr();
			match(Token.SEMICOLON);
		} else {
			acceptOperator();
		}
		if (currentToken.kind != Token.RPAREN) {
			e3AST = parseExpr();
			match(Token.RPAREN);
		} else {
			acceptOperator();
		}
		Stmt sAST = parseStmt();
		finish(forStmtPos);
		forStmtAST = new ForStmt(e1AST == null ? new EmptyExpr(forStmtPos)
				: e1AST, e2AST == null ? new EmptyExpr(forStmtPos) : e2AST,
				e3AST == null ? new EmptyExpr(forStmtPos) : e3AST, sAST,
				forStmtPos);
		return forStmtAST;
	}

	WhileStmt parseWhileStmt() throws SyntaxError {
		WhileStmt whileStmtAST = null;
		SourcePosition whileStmtPos = new SourcePosition();
		start(whileStmtPos);

		match(Token.WHILE);
		match(Token.LPAREN);
		Expr eAST = parseExpr();
		match(Token.RPAREN);
		Stmt sAST = parseStmt();

		finish(whileStmtPos);
		whileStmtAST = new WhileStmt(eAST, sAST, whileStmtPos);
		return whileStmtAST;
	}

	BreakStmt parseBreakStmt() throws SyntaxError {

		BreakStmt breakStmtAST = null;
		SourcePosition breakStmtPos = new SourcePosition();
		start(breakStmtPos);
		match(Token.BREAK);
		match(Token.SEMICOLON);
		finish(breakStmtPos);
		breakStmtAST = new BreakStmt(breakStmtPos);
		return breakStmtAST;
	}

	ContinueStmt parseContinueStmt() throws SyntaxError {

		ContinueStmt continueStmtAST = null;
		SourcePosition continueStmtPos = new SourcePosition();
		start(continueStmtPos);
		match(Token.CONTINUE);
		match(Token.SEMICOLON);
		finish(continueStmtPos);
		continueStmtAST = new ContinueStmt(continueStmtPos);
		return continueStmtAST;
	}

	ReturnStmt parseReturnStmt() throws SyntaxError {
		ReturnStmt returnStmtAST = null;
		SourcePosition returnStmtPos = new SourcePosition();
		start(returnStmtPos);
		match(Token.RETURN);
		Expr eAST = null;
		if (currentToken.kind != Token.SEMICOLON) {
			eAST = parseExpr();
			match(Token.SEMICOLON);
		} else {
			acceptOperator();
		}
		finish(returnStmtPos);
		returnStmtAST = new ReturnStmt(eAST, returnStmtPos);
		return returnStmtAST;
	}

	Stmt parseExprStmt() throws SyntaxError {
		Stmt sAST = null;

		SourcePosition stmtPos = new SourcePosition();
		start(stmtPos);

		if (currentToken.kind == Token.ID
				|| currentToken.kind == Token.INTLITERAL
				|| currentToken.kind == Token.FLOATLITERAL
				|| currentToken.kind == Token.BOOLEANLITERAL
				|| currentToken.kind == Token.STRINGLITERAL
				|| currentToken.kind == Token.PLUS
				|| currentToken.kind == Token.MINUS
				|| currentToken.kind == Token.NOT
				|| currentToken.kind == Token.LPAREN) {
			Expr eAST = parseExpr();
			match(Token.SEMICOLON);
			finish(stmtPos);
			sAST = new ExprStmt(eAST, stmtPos);
		} else {
			match(Token.SEMICOLON);
			finish(stmtPos);
			sAST = new ExprStmt(new EmptyExpr(dummyPos), stmtPos);
		}
		return sAST;
	}

	// ======================= PARAMETERS =======================

	List parseParaList() throws SyntaxError {
		List formalsAST = null;

		SourcePosition formalsPos = new SourcePosition();
		start(formalsPos);

		match(Token.LPAREN);
		if (currentToken.kind != Token.RPAREN) {
			ParaDecl pAST = parseParaDecl();
			List plAST = null;
			if (currentToken.kind == Token.COMMA) {
				acceptOperator();
				plAST = parseProperParaList();
			}
			match(Token.RPAREN);
			finish(formalsPos);
			formalsAST = new ParaList(pAST, plAST == null ? new EmptyParaList(
					formalsPos) : plAST, formalsPos);
		} else {
			match(Token.RPAREN);
			finish(formalsPos);
			formalsAST = new EmptyParaList(formalsPos);
		}

		return formalsAST;
	}

	List parseProperParaList() throws SyntaxError {
		List paraListAST = null;
		SourcePosition paraListPos = new SourcePosition();
		start(paraListPos);
		ParaDecl pAST = parseParaDecl();
		List plAST;
		if (currentToken.kind == Token.COMMA) {
			acceptOperator();
			plAST = parseProperParaList();
			finish(paraListPos);
			paraListAST = new ParaList(pAST, plAST, paraListPos);
		} else {
			finish(paraListPos);
			paraListAST = new ParaList(pAST, new EmptyParaList(paraListPos),
					paraListPos);
		}
		return paraListAST;
	}

	List parseArgList() throws SyntaxError {
		List argListAST = null;
		SourcePosition argListPos = new SourcePosition();
		start(argListPos);
		Expr eAST = parseExpr();
		if (currentToken.kind == Token.COMMA) {
			acceptOperator();
			List alAST = parseArgList();
			finish(argListPos);
			Arg argAST = new Arg(eAST, argListPos);
			argListAST = new ArgList(argAST, alAST, argListPos);
		} else {
			finish(argListPos);
			Arg argAST = new Arg(eAST, argListPos);
			argListAST = new ArgList(argAST, new EmptyArgList(dummyPos),
					argListPos);
		}
		return argListAST;
	}

	// ======================= EXPRESSIONS ======================

	ParaDecl parseParaDecl() throws SyntaxError {
		// TODO Auto-generated method stub
		ParaDecl paraDecl = null;
		SourcePosition paraPos = new SourcePosition();
		start(paraPos);
		Type tAST = parseType();
		Ident idAST = parseIdent();
		if (currentToken.kind == Token.LBRACKET) {
			acceptOperator();
			Expr dAST = new EmptyExpr(dummyPos);
			IntLiteral intAST = null;
			if (currentToken.kind != Token.RBRACKET) {
				intAST = parseIntLiteral();

				match(Token.RBRACKET);
			} else {
				match(Token.RBRACKET);
			}
			finish(paraPos);
			if (intAST != null) {
				dAST = new IntExpr(intAST, paraPos);
			}
			tAST = new ArrayType(tAST, dAST, paraPos);
			paraDecl = new ParaDecl(tAST, idAST, paraPos);
		} else {
			finish(paraPos);
			paraDecl = new ParaDecl(tAST, idAST, paraPos);
		}
		return paraDecl;
	}

	Expr parseExpr() throws SyntaxError {
		Expr exprAST = null;
		exprAST = parseAssignExpr();
		return exprAST;
	}

	Expr parseAssignExpr() throws SyntaxError {

		Expr exprAST = null;

		SourcePosition assignPos = new SourcePosition();
		start(assignPos);

		Expr e1AST = parseCondOrExpr();
		if (currentToken.kind == Token.EQ) {
			Operator opAST = acceptOperator();
			Expr e2AST = parseAssignExpr();

			finish(assignPos);
			exprAST = new AssignExpr(e1AST, e2AST, assignPos);
		} else {
			exprAST = e1AST;
		}
		return exprAST;
	}

	Expr parseCondOrExpr() throws SyntaxError {
		Expr exprAST = null;

		SourcePosition condOrExprPos = new SourcePosition();
		start(condOrExprPos);

		exprAST = parseCondAndExpr();
		while (currentToken.kind == Token.OROR) {
			Operator opAST = acceptOperator();
			Expr e2AST = parseCondAndExpr();

			finish(condOrExprPos);
			exprAST = new BinaryExpr(exprAST, opAST, e2AST, condOrExprPos);
		}
		return exprAST;
	}

	Expr parseCondAndExpr() throws SyntaxError {
		Expr exprAST = null;

		SourcePosition condAndExprPos = new SourcePosition();
		start(condAndExprPos);

		exprAST = parseEqualityExpr();
		while (currentToken.kind == Token.ANDAND) {
			Operator opAST = acceptOperator();
			Expr e2AST = parseEqualityExpr();

			finish(condAndExprPos);
			exprAST = new BinaryExpr(exprAST, opAST, e2AST, condAndExprPos);
		}
		return exprAST;
	}

	Expr parseEqualityExpr() throws SyntaxError {
		Expr exprAST = null;

		SourcePosition equalityExprPos = new SourcePosition();
		start(equalityExprPos);

		exprAST = parseRelExpr();
		while (currentToken.kind == Token.EQEQ
				|| currentToken.kind == Token.NOTEQ) {
			Operator opAST = acceptOperator();
			Expr e2AST = parseRelExpr();

			finish(equalityExprPos);
			exprAST = new BinaryExpr(exprAST, opAST, e2AST, equalityExprPos);
		}
		return exprAST;
	}

	Expr parseRelExpr() throws SyntaxError {
		Expr exprAST = null;

		SourcePosition relExprPos = new SourcePosition();
		start(relExprPos);

		exprAST = parseAdditiveExpr();
		while (currentToken.kind == Token.LT || currentToken.kind == Token.GT
				|| currentToken.kind == Token.LTEQ
				|| currentToken.kind == Token.GTEQ) {
			Operator opAST = acceptOperator();
			Expr e2AST = parseAdditiveExpr();

			finish(relExprPos);
			exprAST = new BinaryExpr(exprAST, opAST, e2AST, relExprPos);
		}
		return exprAST;
	}

	Expr parseAdditiveExpr() throws SyntaxError {
		Expr exprAST = null;

		SourcePosition addStartPos = new SourcePosition();
		start(addStartPos);

		exprAST = parseMultiplicativeExpr();
		while (currentToken.kind == Token.PLUS
				|| currentToken.kind == Token.MINUS) {
			Operator opAST = acceptOperator();
			Expr e2AST = parseMultiplicativeExpr();

			SourcePosition addPos = new SourcePosition();
			copyStart(addStartPos, addPos);
			finish(addPos);
			exprAST = new BinaryExpr(exprAST, opAST, e2AST, addPos);
		}
		return exprAST;
	}

	Expr parseMultiplicativeExpr() throws SyntaxError {

		Expr exprAST = null;

		SourcePosition multStartPos = new SourcePosition();
		start(multStartPos);

		exprAST = parseUnaryExpr();
		while (currentToken.kind == Token.MULT
				|| currentToken.kind == Token.DIV) {
			Operator opAST = acceptOperator();
			Expr e2AST = parseUnaryExpr();
			SourcePosition multPos = new SourcePosition();
			copyStart(multStartPos, multPos);
			finish(multPos);
			exprAST = new BinaryExpr(exprAST, opAST, e2AST, multPos);
		}
		return exprAST;
	}

	Expr parseUnaryExpr() throws SyntaxError {

		Expr exprAST = null;

		SourcePosition unaryPos = new SourcePosition();
		start(unaryPos);

		switch (currentToken.kind) {
		case Token.MINUS:
		case Token.PLUS:
		case Token.NOT:
			Operator opAST = acceptOperator();
			Expr e2AST = parseUnaryExpr();
			finish(unaryPos);
			exprAST = new UnaryExpr(opAST, e2AST, unaryPos);
			break;
		default:
			exprAST = parsePrimaryExpr();
			break;

		}
		return exprAST;
	}

	Expr parsePrimaryExpr() throws SyntaxError {

		Expr exprAST = null;

		SourcePosition primPos = new SourcePosition();
		start(primPos);

		switch (currentToken.kind) {

		case Token.ID:
			Ident iAST = parseIdent();
			if (currentToken.kind == Token.LBRACKET) {
				acceptOperator();
				Expr indexAST = parseExpr();
				match(Token.RBRACKET);
				finish(primPos);
				Var simVAST = new SimpleVar(iAST, primPos);
				exprAST = new ArrayExpr(simVAST, indexAST, primPos);
			} else if (currentToken.kind == Token.LPAREN) {
				acceptOperator();
				List aplAST = new EmptyArgList(dummyPos);
				if (currentToken.kind != Token.RPAREN) {
					aplAST = parseArgList();
				}
				match(Token.RPAREN);
				finish(primPos);
				exprAST = new CallExpr(iAST, aplAST, primPos);
			} else {
				finish(primPos);
				Var simVAST = new SimpleVar(iAST, primPos);
				exprAST = new VarExpr(simVAST, primPos);
			}

			break;

		case Token.LPAREN: {
			accept();
			exprAST = parseExpr();
			match(Token.RPAREN);
		}
			break;
		case Token.INTLITERAL:
			IntLiteral ilAST = parseIntLiteral();
			finish(primPos);
			exprAST = new IntExpr(ilAST, primPos);
			break;
		case Token.FLOATLITERAL:
			FloatLiteral flAST = parseFloatLiteral();
			finish(primPos);
			exprAST = new FloatExpr(flAST, primPos);
			break;
		case Token.BOOLEANLITERAL:
			BooleanLiteral blAST = parseBooleanLiteral();
			finish(primPos);
			exprAST = new BooleanExpr(blAST, primPos);
			break;
		case Token.STRINGLITERAL:
			StringLiteral slAST = parseStringLiteral();
			finish(primPos);
			exprAST = new StringExpr(slAST, primPos);
			break;
		default:
			syntacticError("illegal primary expression", currentToken.spelling);

		}
		return exprAST;
	}

	// ========================== ID, OPERATOR and LITERALS
	// ========================

	Ident parseIdent() throws SyntaxError {

		Ident I = null;

		if (currentToken.kind == Token.ID) {
			previousTokenPosition = currentToken.position;
			String spelling = currentToken.spelling;
			I = new Ident(spelling, previousTokenPosition);
			currentToken = scanner.getToken();
		} else
			syntacticError("identifier expected here", "");
		return I;
	}

	// acceptOperator parses an operator, and constructs a leaf AST for it

	Operator acceptOperator() throws SyntaxError {
		Operator O = null;

		previousTokenPosition = currentToken.position;
		String spelling = currentToken.spelling;
		O = new Operator(spelling, previousTokenPosition);
		currentToken = scanner.getToken();
		return O;
	}

	IntLiteral parseIntLiteral() throws SyntaxError {
		IntLiteral IL = null;

		if (currentToken.kind == Token.INTLITERAL) {
			String spelling = currentToken.spelling;
			accept();
			IL = new IntLiteral(spelling, previousTokenPosition);
		} else
			syntacticError("integer literal expected here", "");
		return IL;
	}

	FloatLiteral parseFloatLiteral() throws SyntaxError {
		FloatLiteral FL = null;

		if (currentToken.kind == Token.FLOATLITERAL) {
			String spelling = currentToken.spelling;
			accept();
			FL = new FloatLiteral(spelling, previousTokenPosition);
		} else
			syntacticError("float literal expected here", "");
		return FL;
	}

	BooleanLiteral parseBooleanLiteral() throws SyntaxError {
		BooleanLiteral BL = null;

		if (currentToken.kind == Token.BOOLEANLITERAL) {
			String spelling = currentToken.spelling;
			accept();
			BL = new BooleanLiteral(spelling, previousTokenPosition);
		} else
			syntacticError("boolean literal expected here", "");
		return BL;
	}

	StringLiteral parseStringLiteral() throws SyntaxError {
		StringLiteral SL = null;

		if (currentToken.kind == Token.STRINGLITERAL) {
			String spelling = currentToken.spelling;
			accept();
			SL = new StringLiteral(spelling, previousTokenPosition);
		} else
			syntacticError("boolean literal expected here", "");
		return SL;
	}

}
