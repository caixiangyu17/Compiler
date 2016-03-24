/**
 * Checker.java
 * Sun Apr 26 13:41:38 AEST 2015
 **/

package VC.Checker;

import VC.ASTs.*;
import VC.Scanner.SourcePosition;
import VC.ErrorReporter;
import VC.StdEnvironment;

public final class Checker implements Visitor {

	private String errMesg[] = {
			"*0: main function is missing",
			"*1: return type of main is not int",

			// defined occurrences of identifiers
			// for global, local and parameters
			"*2: identifier redeclared",
			"*3: identifier declared void",
			"*4: identifier declared void[]",

			// applied occurrences of identifiers
			"*5: identifier undeclared",

			// assignments
			"*6: incompatible type for =",
			"*7: invalid lvalue in assignment",

			// types for expressions
			"*8: incompatible type for return",
			"*9: incompatible type for this binary operator",
			"*10: incompatible type for this unary operator",

			// scalars
			"*11: attempt to use an array/fuction as a scalar",

			// arrays
			"*12: attempt to use a scalar/function as an array",
			"*13: wrong type for element in array initialiser",
			"*14: invalid initialiser: array initialiser for scalar",
			"*15: invalid initialiser: scalar initialiser for array",
			"*16: excess elements in array initialiser",
			"*17: array subscript is not an integer",
			"*18: array size missing",

			// functions
			"*19: attempt to reference a scalar/array as a function",

			// conditional expressions in if, for and while
			"*20: if conditional is not boolean",
			"*21: for conditional is not boolean",
			"*22: while conditional is not boolean",

			// break and continue
			"*23: break must be in a while/for",
			"*24: continue must be in a while/for",

			// parameters
			"*25: too many actual parameters",
			"*26: too few actual parameters",
			"*27: wrong type for actual parameter",

			// reserved for errors that I may have missed (J. Xue)
			"*28: misc 1",
			"*29: misc 2",

			// the following two checks are optional
			"*30: statement(s) not reached",
			"*31: missing return statement",
	};


	private SymbolTable idTable;
	private static SourcePosition dummyPos = new SourcePosition();
	private ErrorReporter reporter;
	private boolean hasMain = false;
	private boolean hasReturn = false;
	private int eListSize = 0;
	private int loops = 0;
	// Checks whether the source program, represented by its AST,
	// satisfies the language's scope rules and type rules.
	// Also decorates the AST as follows:
	//  (1) Each applied occurrence of an identifier is linked to
	//      the corresponding declaration of that identifier.
	//  (2) Each expression and variable is decorated by its type.

	public Checker (ErrorReporter reporter) {
		this.reporter = reporter;
		this.idTable = new SymbolTable ();
		establishStdEnvironment();
	}

	public void check(AST ast) {
		ast.visit(this, null);
	}


	// auxiliary methods

	private void declareVariable(Ident ident, Decl decl) {
		IdEntry entry = idTable.retrieveOneLevel(ident.spelling);

		if (entry == null) {
			; // no problem
		} else
			reporter.reportError(errMesg[2] + ": %", ident.spelling, ident.position);
		idTable.insert(ident.spelling, decl);
	}

	private void declareFunction(Ident ident, Decl decl) {
		declareVariable(ident, decl);
	}

	private boolean isLogicOperator(Operator o) {
		if (o.spelling.equals("!")) return true;
		if (o.spelling.equals("&&")) return true;
		if (o.spelling.equals("||")) return true;
		return false;
	}
	private boolean isCompOperator(Operator o) {
		if (o.spelling.equals("<")) return true;
		if (o.spelling.equals("<=")) return true;
		if (o.spelling.equals(">")) return true;
		if (o.spelling.equals(">=")) return true;
		if (o.spelling.equals("==")) return true;
		if (o.spelling.equals("!=")) return true;
		return false;
	}
	// Expand given expr
	private Expr typeCoercion(Expr e) {
		if (e.type.equals(StdEnvironment.floatType))
			return e;
		Operator op = new Operator ("i2f", dummyPos);
		AST parent = e.parent;
		UnaryExpr eAST = new UnaryExpr(op, e, e.position);
		eAST.parent = parent;
		eAST.type = StdEnvironment.floatType;
		return eAST;
	}
	// Used in var/para decl
	private void checkVoidType(Decl ast) {
		if (ast.T.isVoidType()) {
			reporter.reportError(errMesg[3] + ": %", ast.I.spelling, ast.I.position);
		} else if (ast.T.isArrayType()) {
			if (((ArrayType) ast.T).T.isVoidType())
				reporter.reportError(errMesg[4] + ": %", ast.I.spelling, ast.I.position);
		}
	}
	private boolean isLValue(Expr e) {
		if (e instanceof VarExpr) {
			VarExpr v = (VarExpr)e;
			Decl dl = (Decl)((SimpleVar)v.V).I.decl;
			if(dl instanceof GlobalVarDecl || dl instanceof LocalVarDecl || dl instanceof ParaDecl)
				return true;
			else
				return false;
		} else if (e instanceof ArrayExpr) {
			ArrayExpr av = (ArrayExpr)e;
			if (((SimpleVar)av.V).type.isArrayType())
				return true;
			else
				return false;
		} else
			return false;
	}
	// Programs
	public Object visitProgram(Program ast, Object o) {
		ast.FL.visit(this, null);
		if (!hasMain) {
			reporter.reportError(errMesg[0], "", ast.position);
		}
		return null;
	}
	// Lists for denoting the null reference
	public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
		return null;
	}
	public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
		return null;
	}
	public Object visitEmptyExprList(EmptyExprList ast, Object o) {
		return null;
	}

	// Declarations

	public Object visitDeclList(DeclList ast, Object o) {
		ast.D.visit(this, null);
		ast.DL.visit(this, null);
		return null;
	}

	public Object visitFuncDecl(FuncDecl ast, Object o) {
		hasReturn = false;
		declareFunction(ast.I, ast);

		// HINT
		// Pass ast as the 2nd argument (as done below) so that the
		// formal parameters of the function can be extracted from ast when the
		// function body is later visited
		// And return type of function can be checked when meeting return statement
		ast.S.visit(this, ast);

		// Check return
		if (!hasReturn && !ast.T.isVoidType()) {
			reporter.reportError(errMesg[31] , "", ast.position);
		}
		// Check main function
		if (ast.I.spelling.equals("main")) {
			hasMain = true;
			if (!ast.T.isIntType()) {
				reporter.reportError(errMesg[1] , "", ast.position);
			}
		}
		return null;
	}

	// Handle some erros related to array decl, include
	// "*15: invalid initialiser: scalar initialiser for array",
	// "*16: excess elements in array initialiser",
	// "*17: array subscript is not an integer",
	// "*18: array size missing"
	private Object visitArrayDecl(ArrayType ast, Expr e, Object o) {
		Expr subscript = ast.E;
		// e should be either InitExpr or EmptyExpr
		if (!(e instanceof InitExpr || e instanceof EmptyExpr)) {
			reporter.reportError(errMesg[15], "", ast.position);
		}

		// Type checking on initialiser needs type of the array
		// Update eListSize
		e.visit(this, ast.T);
		//case 1
		if (subscript instanceof IntExpr) {
			int sz = Integer.parseInt(((IntExpr)subscript).IL.spelling);
			if (e instanceof InitExpr) {
				if (sz < eListSize) {
					reporter.reportError(errMesg[16], "", ast.position);
				}
			}
			// case 2
		} else if (subscript instanceof EmptyExpr){
			if (e instanceof InitExpr) {
				ast.E = ((ArrayType)e.type).E;
			} else {
				reporter.reportError(errMesg[18], "", ast.position);
			}
			// case 3
		} else {
			reporter.reportError(errMesg[17], "", ast.position);
		}
		return null;
	}
	private Object visitVarDecl(Decl ast, Expr e, Object o) {
		declareVariable(ast.I, ast);
		checkVoidType(ast);

		// assignable compatible
		if (ast.T.isArrayType()) {
			visitArrayDecl((ArrayType)ast.T, e, o);
		} else { // this is not an array
			if (e instanceof InitExpr) {
				reporter.reportError(errMesg[14], "", ast.position);
			} else { // normal var decl
				e.visit(this, o);
				if (!ast.T.assignable(e.type))
					reporter.reportError(errMesg[6], "", ast.position);
			}
		}
		return null;
	}
	public Object visitGlobalVarDecl(GlobalVarDecl ast, Object o) {
		visitVarDecl(ast, ast.E, o);
		 	if (ast.T.isFloatType() && ast.E.type.isIntType())
				ast.E = typeCoercion(ast.E);
		return null;
	}

	public Object visitLocalVarDecl(LocalVarDecl ast, Object o) {

		visitVarDecl(ast, ast.E, o);
		if (ast.T.isFloatType() && ast.E.type.isIntType())
			ast.E = typeCoercion(ast.E);
		return null;

	}

	// Statements
	// o is null or funcdecl
	public Object visitCompoundStmt(CompoundStmt ast, Object o) {
		idTable.openScope();
		if (o instanceof FuncDecl) {
			// load parameters
			FuncDecl fdl = (FuncDecl)o;
			fdl.PL.visit(this, null);
			ast.DL.visit(this, fdl.T);
			ast.SL.visit(this, fdl.T);
		} else {
			ast.DL.visit(this, o);
			ast.SL.visit(this, o);
		}

		idTable.closeScope();
		return null;
	}

	public Object visitStmtList(StmtList ast, Object o) {
		ast.S.visit(this, o);
		if (ast.S instanceof ReturnStmt && ast.SL instanceof StmtList)
			reporter.reportError(errMesg[30], "", ast.SL.position);
		ast.SL.visit(this, o);
		return null;
	}

	public Object visitIfStmt(IfStmt ast, Object o) {
		ast.E.visit(this, o);
		if (!ast.E.type.isBooleanType())
			reporter.reportError(errMesg[20], "", ast.E.position);
		ast.S1.visit(this, o);
		ast.S2.visit(this, o);
		return null;
	}
	public Object visitForStmt(ForStmt ast, Object o) {
		ast.E1.visit(this, o);
		ast.E2.visit(this, o);
		if (!ast.E2.type.equals(StdEnvironment.booleanType))
			reporter.reportError(errMesg[21], "", ast.E2.position);
		ast.E3.visit(this, o);
		loops++;
		ast.S.visit(this, o);
		loops--;

		return null;
	}
	public Object visitWhileStmt(WhileStmt ast, Object o) {
		ast.E.visit(this, o);
		if (!ast.E.type.isBooleanType())
			reporter.reportError(errMesg[22], "", ast.E.position);
		loops++;
		ast.S.visit(this, o);
		loops--;
		return null;
	}

	public Object visitBreakStmt(BreakStmt ast, Object o) {
		if (loops == 0) {
			reporter.reportError(errMesg[23], "", ast.position);
		}
		return null;
	}
	public Object visitContinueStmt(ContinueStmt ast, Object o) {
		if (loops == 0) {
			reporter.reportError(errMesg[24], "", ast.position);
		}
		return null;
	}
	// void -> return ;
	// others -> return exp;
	public Object visitReturnStmt(ReturnStmt ast, Object o) {
		hasReturn = true;
		ast.E.visit(this, o);
		if (!((Type)o).assignable(ast.E.type)) {
			reporter.reportError(errMesg[8], "", ast.position);
		}
		return null;
	}

	public Object visitExprStmt(ExprStmt ast, Object o) {
		ast.E.visit(this, o);
		return null;
	}
	public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
		return null;
	}
	public Object visitEmptyStmt(EmptyStmt ast, Object o) {
		return null;
	}

	// Expressions

	// Returns the Type denoting the type of the expression. Does
	// not use the given object.


	public Object visitEmptyExpr(EmptyExpr ast, Object o) {
		ast.type = StdEnvironment.errorType;
		return ast.type;
	}

	public Object visitBooleanExpr(BooleanExpr ast, Object o) {
		ast.type = StdEnvironment.booleanType;
		return ast.type;
	}

	public Object visitIntExpr(IntExpr ast, Object o) {
		ast.type = StdEnvironment.intType;
		return ast.type;
	}

	public Object visitFloatExpr(FloatExpr ast, Object o) {
		ast.type = StdEnvironment.floatType;
		return ast.type;
	}

	public Object visitStringExpr(StringExpr ast, Object o) {
		ast.type = StdEnvironment.stringType;
		return ast.type;
	}

	// legal unary expr - !boolean, (+|-)(int|float)
	public Object visitUnaryExpr(UnaryExpr ast, Object o) {
		ast.E.visit(this, o);
		Type t = ast.E.type;

		if (isLogicOperator(ast.O)) {
			if (t.equals(StdEnvironment.booleanType)) { // Catch errorType
				ast.O.spelling = "i" + ast.O.spelling;
				return ast.type = StdEnvironment.booleanType;
			} else {
				reporter.reportError(errMesg[10] + ": %", ast.O.spelling, ast.position);
				return ast.type = StdEnvironment.errorType;
			}
		} else {
			if (t.isFloatType()) {
				ast.O.spelling = "f" + ast.O.spelling;
				return ast.type = t;
			} else if (t.equals(StdEnvironment.intType)) { // Catch errorType
				ast.O.spelling = "i" + ast.O.spelling;
				return ast.type = t;
			} else {
				reporter.reportError(errMesg[10] + ": %", ast.O.spelling, ast.position);
				return ast.type = StdEnvironment.errorType;
			}
		}
	}
	// legal binary expr
	// (int|float) +|-|*|/ (int|float)
	// boolean &&||| boolean
	public Object visitBinaryExpr(BinaryExpr ast, Object o) {
		ast.E1.visit(this, o);
		ast.E2.visit(this, o);
		Type t1 = ast.E1.type, t2 = ast.E2.type;
		if (isLogicOperator(ast.O)) {
			if (t1.equals(StdEnvironment.booleanType) && t2.equals(StdEnvironment.booleanType)) { // Catch errorType
				ast.O.spelling = "i" + ast.O.spelling;
				return ast.type = t1;
			} else {
				reporter.reportError(errMesg[9] + ": %", ast.O.spelling, ast.position);
				return ast.type = StdEnvironment.errorType;
			}
		} else {
			if (t1.isFloatType() && t2.isFloatType()) {
				if (isCompOperator(ast.O)) {
					ast.O.spelling = "f" + ast.O.spelling;
					return ast.type = StdEnvironment.booleanType;
				} else {
					ast.O.spelling = "f" + ast.O.spelling;
					return ast.type = StdEnvironment.floatType;
				}
			} else if (t1.isFloatType() && t2.isIntType()) {
				ast.E2 = typeCoercion(ast.E2);
				if (isCompOperator(ast.O)) {
					ast.O.spelling = "f" + ast.O.spelling;
					return ast.type = StdEnvironment.booleanType;
				} else {
					ast.O.spelling = "f" + ast.O.spelling;
					return ast.type = StdEnvironment.floatType;
				}
			} else if (t2.isFloatType() && t1.isIntType()) {
				ast.E1 = typeCoercion(ast.E1);
				if (isCompOperator(ast.O)) {
					ast.O.spelling = "f" + ast.O.spelling;
					return ast.type = StdEnvironment.booleanType;
				} else {
					ast.O.spelling = "f" + ast.O.spelling;
					return ast.type = StdEnvironment.floatType;
				}
			} else if (t1.isIntType() && t2.isIntType()) {
				if (isCompOperator(ast.O)) {
					ast.O.spelling = "i" + ast.O.spelling;
					return ast.type = StdEnvironment.booleanType;
				} else {
					ast.O.spelling = "i" + ast.O.spelling;
					return ast.type = StdEnvironment.intType;
				}
			} else if (t1.isBooleanType() && t2.isBooleanType() &&
					(ast.O.spelling.equals("==") || ast.O.spelling.equals("!="))) {
				ast.O.spelling = "i" + ast.O.spelling;
				return ast.type = StdEnvironment.booleanType;
			} else {
				reporter.reportError(errMesg[9] + ": %", ast.O.spelling, ast.position);
				return ast.type = StdEnvironment.errorType;
			}
		}
	}
	public Object visitInitExpr(InitExpr ast, Object o) {
		Type T = (Type)o;
		eListSize = 0;
		ast.IL.visit(this, T);
		return ast.type = new ArrayType(T, new IntExpr(new IntLiteral(Integer.toString(eListSize),dummyPos),dummyPos),dummyPos);
	}
	// Check every expr in exprlist with function type T
	// And increase eListSize
	public Object visitExprList(ExprList ast, Object o) {
		eListSize++;
		Type T = (Type)o;
		Type t = (Type)ast.E.visit(this, o);
		if (T.isFloatType() && t.isIntType()) {
			ast.E = typeCoercion(ast.E);
		} else if (!T.assignable(t)) {
			reporter.reportError(errMesg[13], "", ast.E.position);
		}
		ast.EL.visit(this, o);
		return null;
	}
	// subscript must be int type
	public Object visitArrayExpr(ArrayExpr ast, Object o) {
		ast.V.visit(this, ast);
		Type subscript = (Type) ast.E.visit(this, o);
		if (!subscript.isIntType())
			reporter.reportError(errMesg[17], "", ast.position);
		Type t = ast.V.type;
		if (t.isArrayType()) { // accept error in subscript
			return ast.type = ((ArrayType)t).T;
		} else {
			reporter.reportError(errMesg[12], ((SimpleVar)ast.V).I.spelling, ast.position);
			return ast.type = StdEnvironment.errorType;
		}
	}

	public Object visitVarExpr(VarExpr ast, Object o) {
		ast.type = (Type) ast.V.visit(this, o);
		return ast.type;
	}
	public Object visitCallExpr(CallExpr ast, Object o) {
		ast.I.visit(this, ast);
		if (ast.I.decl instanceof FuncDecl) {
			FuncDecl fdl = (FuncDecl)ast.I.decl;
			ast.AL.visit(this, fdl.PL); // match args and paras
			return ast.type = fdl.T; // error recovery
		} else {
			if (ast.I.decl != null)
				reporter.reportError(errMesg[19] + ": %", ast.I.spelling, ast.position);
			return ast.type = StdEnvironment.errorType;
		}
	}
	public Object visitAssignExpr(AssignExpr ast, Object o) {
		Type t1 = (Type)ast.E1.visit(this, o);
		Type t2 = (Type)ast.E2.visit(this, o);
		if (t1.isFloatType() && t2.isIntType()) {
			ast.E2 = typeCoercion(ast.E2);
		}
		if (isLValue(ast.E1)) {
			if (!t1.assignable(t2))
				reporter.reportError(errMesg[6], "", ast.position);
			return ast.type = t1;
		} else {
			reporter.reportError(errMesg[7], "", ast.position);
			return ast.type = StdEnvironment.errorType;
		}
	}

	// Parameters

	// Always returns null. Does not use the given object.

	public Object visitParaList(ParaList ast, Object o) {
		ast.P.visit(this, null);
		ast.PL.visit(this, null);
		return null;
	}

	public Object visitParaDecl(ParaDecl ast, Object o) {
		declareVariable(ast.I, ast);
		checkVoidType(ast);
		return null;
	}

	public Object visitEmptyParaList(EmptyParaList ast, Object o) {
		return null;
	}

	// Arguments

	// Your visitor methods for arguments go here
	// o is paraList
	public Object visitArgList(ArgList ast, Object o) {
		if (o instanceof EmptyParaList) {
			reporter.reportError(errMesg[25], "", ast.position);
			return null;
		}
		ParaList pl = (ParaList) o;
		ast.A.visit(this, pl.P);
		ast.AL.visit(this, pl.PL);
		return null;
	}

	public Object visitArg(Arg ast, Object o) {
		ast.E.visit(this, ast);
		ParaDecl pd = (ParaDecl) o;
		if (pd.T.isArrayType() && ast.E.type.isArrayType()) {
			ArrayType at1 = (ArrayType)(pd.T);
			ArrayType at2 = (ArrayType)(ast.E.type);
			if (!at1.T.assignable(at2.T))
				reporter.reportError(errMesg[27] + ": %", pd.I.spelling, ast.position);
		} else if (!pd.T.assignable(ast.E.type)) {
			reporter.reportError(errMesg[27] + ": %", pd.I.spelling, ast.position);
		}
		return ast.E.type;
	}

	public Object visitEmptyArgList(EmptyArgList ast, Object o) {
		if (o instanceof ParaList)
			reporter.reportError(errMesg[26], "", ast.position);
		return null;
	}
	// Types

	// Returns the type predefined in the standard environment.

	public Object visitErrorType(ErrorType ast, Object o) {
		return StdEnvironment.errorType;
	}

	public Object visitBooleanType(BooleanType ast, Object o) {
		return StdEnvironment.booleanType;
	}

	public Object visitIntType(IntType ast, Object o) {
		return StdEnvironment.intType;
	}

	public Object visitFloatType(FloatType ast, Object o) {
		return StdEnvironment.floatType;
	}

	public Object visitStringType(StringType ast, Object o) {
		return StdEnvironment.stringType;
	}

	public Object visitVoidType(VoidType ast, Object o) {
		return StdEnvironment.voidType;
	}
	// This function will never be called in Checker
	public Object visitArrayType(ArrayType ast, Object o) {
		return ast;
	}

	// Literals, Identifiers and Operators

	public Object visitIdent(Ident I, Object o) {
		Decl binding = idTable.retrieve(I.spelling);
		if (binding != null) {
			I.decl = binding;
			if (binding instanceof FuncDecl && !(o instanceof CallExpr)) {
				reporter.reportError(errMesg[11] + ": %", I.spelling, I.position);
				return I.decl = null;
			} else if (binding.T.isArrayType() && !(o instanceof ArrayExpr || o instanceof Arg)) {
				reporter.reportError(errMesg[11] + ": %", I.spelling, I.position);
				return I.decl = null;
			}
		} else {
			reporter.reportError(errMesg[5] + ": %", I.spelling, I.position);
		}
		return I.decl = binding;
	}

	public Object visitBooleanLiteral(BooleanLiteral SL, Object o) {
		return StdEnvironment.booleanType;
	}

	public Object visitIntLiteral(IntLiteral IL, Object o) {
		return StdEnvironment.intType;
	}

	public Object visitFloatLiteral(FloatLiteral IL, Object o) {
		return StdEnvironment.floatType;
	}

	public Object visitStringLiteral(StringLiteral IL, Object o) {
		return StdEnvironment.stringType;
	}

	public Object visitOperator(Operator O, Object o) {
		return null;
	}

	public Object visitSimpleVar(SimpleVar ast, Object o) {
		ast.I.visit(this, o);
		if (ast.I.decl == null) {
			return ast.type = StdEnvironment.errorType;
		} else {
			return ast.type = ((Decl)ast.I.decl).T;
		}
	}
	// Creates a small AST to represent the "declaration" of each built-in
	// function, and enters it in the symbol table.

	private FuncDecl declareStdFunc (Type resultType, String id, List pl) {

		FuncDecl binding;

		binding = new FuncDecl(resultType, new Ident(id, dummyPos), pl,
				new EmptyStmt(dummyPos), dummyPos);
		idTable.insert (id, binding);

		return binding;
	}

	// Creates small ASTs to represent "declarations" of all
	// build-in functions.
	// Inserts these "declarations" into the symbol table.

	private final static Ident dummyI = new Ident("x", dummyPos);

	private void establishStdEnvironment () {

		// Define four primitive types
		// errorType is assigned to ill-typed expressions

		StdEnvironment.booleanType = new BooleanType(dummyPos);
		StdEnvironment.intType = new IntType(dummyPos);
		StdEnvironment.floatType = new FloatType(dummyPos);
		StdEnvironment.stringType = new StringType(dummyPos);
		StdEnvironment.voidType = new VoidType(dummyPos);
		StdEnvironment.errorType = new ErrorType(dummyPos);

		// enter into the declarations for built-in functions into the table

		StdEnvironment.getIntDecl = declareStdFunc( StdEnvironment.intType,
				"getInt", new EmptyParaList(dummyPos));
		StdEnvironment.putIntDecl = declareStdFunc( StdEnvironment.voidType,
				"putInt", new ParaList(
						new ParaDecl(StdEnvironment.intType, dummyI, dummyPos),
						new EmptyParaList(dummyPos), dummyPos));
		StdEnvironment.putIntLnDecl = declareStdFunc( StdEnvironment.voidType,
				"putIntLn", new ParaList(
						new ParaDecl(StdEnvironment.intType, dummyI, dummyPos),
						new EmptyParaList(dummyPos), dummyPos));
		StdEnvironment.getFloatDecl = declareStdFunc( StdEnvironment.floatType,
				"getFloat", new EmptyParaList(dummyPos));
		StdEnvironment.putFloatDecl = declareStdFunc( StdEnvironment.voidType,
				"putFloat", new ParaList(
						new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos),
						new EmptyParaList(dummyPos), dummyPos));
		StdEnvironment.putFloatLnDecl = declareStdFunc( StdEnvironment.voidType,
				"putFloatLn", new ParaList(
						new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos),
						new EmptyParaList(dummyPos), dummyPos));
		StdEnvironment.putBoolDecl = declareStdFunc( StdEnvironment.voidType,
				"putBool", new ParaList(
						new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos),
						new EmptyParaList(dummyPos), dummyPos));
		StdEnvironment.putBoolLnDecl = declareStdFunc( StdEnvironment.voidType,
				"putBoolLn", new ParaList(
						new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos),
						new EmptyParaList(dummyPos), dummyPos));

		StdEnvironment.putStringLnDecl = declareStdFunc( StdEnvironment.voidType,
				"putStringLn", new ParaList(
						new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos),
						new EmptyParaList(dummyPos), dummyPos));

		StdEnvironment.putStringDecl = declareStdFunc( StdEnvironment.voidType,
				"putString", new ParaList(
						new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos),
						new EmptyParaList(dummyPos), dummyPos));

		StdEnvironment.putLnDecl = declareStdFunc( StdEnvironment.voidType,
				"putLn", new EmptyParaList(dummyPos));

	}
}
