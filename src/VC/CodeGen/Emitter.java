/*
 * Emitter.java    15 -*- MAY -*- 2015
 * Jingling Xue, School of Computer Science, UNSW, Australia
 */

// A new frame object is created for every function just before the
// function is being translated in visitFuncDecl.
//
// All the information about the translation of a function should be
// placed in this Frame object and passed across the AST nodes as the
// 2nd argument of every visitor method in Emitter.java.

package VC.CodeGen;

import VC.ASTs.*;
import VC.ErrorReporter;
import VC.StdEnvironment;

import java.util.ArrayList;

public final class Emitter implements Visitor {

    private ErrorReporter errorReporter;
    private String inputFilename;
    private String classname;
    private String outputFilename;

    /**
     * ******created by Leo*************
     */
    private ArrayList<String> loopEndLabels = new ArrayList<String>();
    private ArrayList<String> loopStartLabels = new ArrayList<String>();
    private ArrayList<String> loopContinueLabels = new ArrayList<String>();

    public Emitter(String inputFilename, ErrorReporter reporter) {
        this.inputFilename = inputFilename;
        errorReporter = reporter;

        int i = inputFilename.lastIndexOf('.');
        if (i > 0)
            classname = inputFilename.substring(0, i);
        else
            classname = inputFilename;

    }

    // PRE: ast must be a Program node

    public final void gen(AST ast) {
        ast.visit(this, null);
        JVM.dump(classname + ".j");
    }

    // Programs
    public Object visitProgram(Program ast, Object o) {
        /** This method works for scalar variables only. You need to modify
         it to handle all array-related declarations and initialisations.
         **/

        // Generates the default constructor initialiser
        emit(JVM.CLASS, "public", classname);
        emit(JVM.SUPER, "java/lang/Object");

        emit("");

        // Three subpasses:

        // (1) Generate .field definition statements since
        //     these are required to appear before method definitions
        List list = ast.FL;
        while (!list.isEmpty()) {
            DeclList dlAST = (DeclList) list;
            if (dlAST.D instanceof GlobalVarDecl) {
                GlobalVarDecl vAST = (GlobalVarDecl) dlAST.D;
                emit(JVM.STATIC_FIELD, vAST.I.spelling, VCtoJavaType(vAST.T));
            }
            list = dlAST.DL;
        }

        emit("");

        // (2) Generate <clinit> for global variables (assumed to be static)

        emit("; standard class static initializer ");
        emit(JVM.METHOD_START, "static <clinit>()V");
        emit("");

        // create a Frame for <clinit>

        Frame frame = new Frame(false);

        list = ast.FL;
        while (!list.isEmpty()) {
            DeclList dlAST = (DeclList) list;
            if (dlAST.D instanceof GlobalVarDecl) {
                GlobalVarDecl vAST = (GlobalVarDecl) dlAST.D;
                if (!vAST.E.isEmptyExpr()) {
                    if (vAST.T.isArrayType()) {
                        vAST.T.visit(this, frame);
                        int positionIndex = 0;
                        if (true) {
                            ExprList exprList = (ExprList) ((InitExpr) vAST.E).IL;
                            while (!exprList.isEmpty()) {
                                emit(JVM.DUP);
                                frame.push();
                                emitICONST(positionIndex);
                                frame.push();
                                exprList.E.visit(this, frame);
                                if (exprList.E.type.isBooleanType()) {
                                    emit(JVM.BASTORE);
                                } else if (exprList.E.type.isIntType()) {
                                    emit(JVM.IASTORE);
                                } else if (exprList.E.type.isFloatType()) {
                                    emit(JVM.FASTORE);
                                }
                                frame.pop(3);
                                if (exprList.EL instanceof EmptyExprList) {
                                    break;
                                } else {
                                    exprList = (ExprList) exprList.EL;
                                    positionIndex++;
                                }
                            }
                        }

                    } else if (vAST.T.isFloatType()) {
                        vAST.E.visit(this, frame);
                    } else {
                        vAST.E.visit(this, frame);
                    }
                } else {
                    if (vAST.T.isArrayType()) {
                        vAST.T.visit(this, frame);
                    } else if (vAST.T.isFloatType()) {
                        emit(JVM.FCONST_0);
                        frame.push();
                    } else {
                        emit(JVM.ICONST_0);
                        frame.push();
                    }

                }
                emitPUTSTATIC(VCtoJavaType(vAST.T), vAST.I.spelling);
                frame.pop();
            }
            list = dlAST.DL;
        }

        emit("");
        emit("; set limits used by this method");
        emit(JVM.LIMIT, "locals", frame.getNewIndex());

        emit(JVM.LIMIT, "stack", frame.getMaximumStackSize());
        emit(JVM.RETURN);
        emit(JVM.METHOD_END, "method");

        emit("");

        // (3) Generate Java bytecode for the VC program

        emit("; standard constructor initializer ");
        emit(JVM.METHOD_START, "public <init>()V");
        emit(JVM.LIMIT, "stack 1");
        emit(JVM.LIMIT, "locals 1");
        emit(JVM.ALOAD_0);
        emit(JVM.INVOKESPECIAL, "java/lang/Object/<init>()V");
        emit(JVM.RETURN);
        emit(JVM.METHOD_END, "method");

        return ast.FL.visit(this, o);
    }

    // Statements

    public Object visitStmtList(StmtList ast, Object o) {
        Frame frame = (Frame) o;
        ast.S.visit(this, o);
        ast.SL.visit(this, o);
        return null;
    }

    @Override
    public Object visitIfStmt(IfStmt ast, Object o) {
        Frame frame = (Frame) o;
        if (ast.E instanceof ArrayExpr) {
            if (((SimpleVar) ((ArrayExpr) ast.E).V).I.decl instanceof GlobalVarDecl) {
                GlobalVarDecl globalVarDecl = (GlobalVarDecl) ((SimpleVar) ((ArrayExpr) ast.E).V).I.decl;
                emitGETSTATIC(VCtoJavaType(globalVarDecl.T), ((SimpleVar) ((ArrayExpr) ast.E).V).I.spelling);
                frame.push();
                ((ArrayExpr) ast.E).E.visit(this, o);
                if (ast.E.type.isFloatType()) {
                    emit(JVM.FALOAD);
                    frame.pop(2);
                    frame.push();
                } else if (ast.E.type.isIntType()) {
                    emit(JVM.IALOAD);
                    frame.pop(2);
                    frame.push();
                } else if (ast.E.type.isBooleanType()) {
                    emit(JVM.BALOAD);
                    frame.pop(2);
                    frame.push();
                }
            } else if (((SimpleVar) ((ArrayExpr) ast.E).V).I.decl instanceof LocalVarDecl) {
                LocalVarDecl localVarDecl = (LocalVarDecl) ((SimpleVar) ((ArrayExpr) ast.E).V).I.decl;
                emitALOAD(localVarDecl.index);
                frame.push();
                ((ArrayExpr) ast.E).E.visit(this, o);
                if (ast.E.type.isFloatType()) {
                    emit(JVM.FALOAD);
                    frame.pop(2);
                    frame.push();
                } else if (ast.E.type.isIntType()) {
                    emit(JVM.IALOAD);
                    frame.pop(2);
                    frame.push();
                } else if (ast.E.type.isBooleanType()) {
                    emit(JVM.BALOAD);
                    frame.pop(2);
                    frame.push();
                }
            } else if (((SimpleVar) ((ArrayExpr) ast.E).V).I.decl instanceof ParaDecl) {
                ParaDecl paraDecl = (ParaDecl) ((SimpleVar) ((ArrayExpr) ast.E).V).I.decl;
                emitALOAD(paraDecl.index);
                frame.push();
                ((ArrayExpr) ast.E).E.visit(this, o);
                if (ast.E.type.isFloatType()) {
                    emit(JVM.FALOAD);
                    frame.pop(2);
                    frame.push();
                } else if (ast.E.type.isIntType()) {
                    emit(JVM.IALOAD);
                    frame.pop(2);
                    frame.push();
                } else if (ast.E.type.isBooleanType()) {
                    emit(JVM.BALOAD);
                    frame.pop(2);
                    frame.push();
                }
            }
        } else if (ast.E instanceof VarExpr) {
            if (((SimpleVar) ((VarExpr) ast.E).V).I.decl instanceof GlobalVarDecl) {
                GlobalVarDecl globalVarDecl = (GlobalVarDecl) ((SimpleVar) ((VarExpr) ast.E).V).I.decl;
                emitGETSTATIC(VCtoJavaType(globalVarDecl.T), ((SimpleVar) ((VarExpr) ast.E).V).I.spelling);
                frame.push();
            } else if (((SimpleVar) ((VarExpr) ast.E).V).I.decl instanceof LocalVarDecl) {
                LocalVarDecl localVarDecl = (LocalVarDecl) ((SimpleVar) ((VarExpr) ast.E).V).I.decl;
                if (localVarDecl.T.isIntType()) {
                    emitILOAD(localVarDecl.index);
                } else if (localVarDecl.T.isBooleanType()) {
                    emitILOAD(localVarDecl.index);
                } else if (localVarDecl.T.isFloatType()) {
                    emitFLOAD(localVarDecl.index);
                }
                frame.push();
            } else if (((SimpleVar) ((VarExpr) ast.E).V).I.decl instanceof ParaDecl) {
                ParaDecl paraDecl = (ParaDecl) ((SimpleVar) ((VarExpr) ast.E).V).I.decl;
                if (paraDecl.T.isIntType()) {
                    emitILOAD(paraDecl.index);
                } else if (paraDecl.T.isBooleanType()) {
                    emitILOAD(paraDecl.index);
                } else if (paraDecl.T.isFloatType()) {
                    emitFLOAD(paraDecl.index);
                }
                frame.push();
            }
        } else {
            ast.E.visit(this, o);
        }

        String trueLabel = frame.getNewLabel();
        String falseLabel = frame.getNewLabel();
        String endLabel = frame.getNewLabel();
        emit(JVM.IFEQ + " " + falseLabel);
        frame.pop();
        ast.S1.visit(this, o);
        emit(JVM.GOTO + " " + endLabel);
        emit(falseLabel + ":");
        ast.S2.visit(this, o);
        emit(endLabel + ":");
        return null;
    }

    @Override
    public Object visitWhileStmt(WhileStmt ast, Object o) {
        Frame frame = (Frame) o;
        String whileStmt = frame.getNewLabel();
        String whileEnd = frame.getNewLabel();
        loopEndLabels.add(whileEnd);
        loopStartLabels.add(whileStmt);
        loopContinueLabels.add(whileStmt);
        emit(whileStmt + ":");
        ast.E.visit(this, o);
        emit(JVM.IFEQ, whileEnd);
        frame.pop();
        ast.S.visit(this, o);
        emit(JVM.GOTO, whileStmt);
        emit(whileEnd + ":");
        loopStartLabels.remove(loopStartLabels.size() - 1);
        loopEndLabels.remove(loopEndLabels.size() - 1);
        return null;
    }

    @Override
    public Object visitForStmt(ForStmt ast, Object o) {
        Frame frame = (Frame) o;
        String forStmt = frame.getNewLabel();
        String forContinue = frame.getNewLabel();
        String forEnd = frame.getNewLabel();
        loopEndLabels.add(forEnd);
        loopStartLabels.add(forStmt);
        loopContinueLabels.add(forContinue);
        ast.E1.visit(this, o);
        emit(forStmt + ":");
        ast.E2.visit(this, o);
        emit(JVM.IFEQ, forEnd);
        frame.pop();
        ast.S.visit(this, o);
        emit(forContinue + ":");
        ast.E3.visit(this, o);
        emit(JVM.GOTO, forStmt);
        emit(forEnd + ":");
        loopStartLabels.remove(loopStartLabels.size() - 1);
        loopEndLabels.remove(loopEndLabels.size() - 1);
        loopContinueLabels.remove(loopContinueLabels.size() - 1);
        return null;
    }

    @Override
    public Object visitBreakStmt(BreakStmt ast, Object o) {
        emit(JVM.GOTO, loopEndLabels.get(loopEndLabels.size() - 1));
        return null;
    }

    @Override
    public Object visitContinueStmt(ContinueStmt ast, Object o) {
        emit(JVM.GOTO, loopContinueLabels.get(loopContinueLabels.size() - 1));
        return null;
    }

    public Object visitCompoundStmt(CompoundStmt ast, Object o) {
        Frame frame = (Frame) o;

        String scopeStart = frame.getNewLabel();
        String scopeEnd = frame.getNewLabel();
        frame.scopeStart.push(scopeStart);
        frame.scopeEnd.push(scopeEnd);
        if (ast.parent instanceof WhileStmt || ast.parent instanceof ForStmt || ast.parent instanceof IfStmt) {
            //we should create the label in its parent so that the goto command could find the label.
        } else {
            emit(scopeStart + ":");
        }
        if (ast.parent instanceof FuncDecl) {
            if (((FuncDecl) ast.parent).I.spelling.equals("main")) {
                emit(JVM.VAR, "0 is argv [Ljava/lang/String; from " + (String) frame.scopeStart.peek() + " to " + (String) frame.scopeEnd.peek());
                emit(JVM.VAR, "1 is vc$ L" + classname + "; from " + (String) frame.scopeStart.peek() + " to " + (String) frame.scopeEnd.peek());
                // Generate code for the initialiser vc$ = new classname();
                emit(JVM.NEW, classname);
                emit(JVM.DUP);
                frame.push(2);
                emit("invokenonvirtual", classname + "/<init>()V");
                frame.pop();
                emit(JVM.ASTORE_1);
                frame.pop();
            } else {
                emit(JVM.VAR, "0 is this L" + classname + "; from " + (String) frame.scopeStart.peek() + " to " + (String) frame.scopeEnd.peek());
                ((FuncDecl) ast.parent).PL.visit(this, o);
            }
        }
        ast.DL.visit(this, o);
        ast.SL.visit(this, o);
        if (ast.parent instanceof WhileStmt || ast.parent instanceof ForStmt || ast.parent instanceof IfStmt) {
            //we should create the label in its parent so that the goto command could find the label.
        } else {
            emit(scopeEnd + ":");
        }

        frame.scopeStart.pop();
        frame.scopeEnd.pop();
        return null;
    }

    @Override
    public Object visitExprStmt(ExprStmt ast, Object o) {
        Frame frame = (Frame) o;
        loadExpr(ast.E, frame);
        ast.E.visit(this, o);
        if (ast.E instanceof BinaryExpr || ast.E instanceof UnaryExpr || ast.E instanceof IntExpr
                || ast.E instanceof FloatExpr || ast.E instanceof BooleanExpr || ast.E instanceof ArrayExpr
                || ast.E instanceof VarExpr) {
            emit(JVM.POP);
        }
        return null;
    }

    public Object visitReturnStmt(ReturnStmt ast, Object o) {
        Frame frame = (Frame) o;

/*
  int main() { return 0; } must be interpretted as
  public static void main(String[] args) { return ; }
  Therefore, "return expr", if present in the main of a VC program
  must be translated into a RETURN rather than IRETURN instruction.
*/

        if (frame.isMain()) {
            emit(JVM.RETURN);
            return null;
        }

// Your other code goes here
        ast.E.visit(this, o);
        if (ast.E instanceof ArrayExpr) {
            if (ast.E.type.isIntType()) {
                emit(JVM.IALOAD);
                frame.pop(2);
                frame.push();
            } else if (ast.E.type.isBooleanType()) {
                emit(JVM.BALOAD);
                frame.pop(2);
                frame.push();
            } else if (ast.E.type.isFloatType()) {
                emit(JVM.FALOAD);
                frame.pop(2);
                frame.push();
            }
        } else if (ast.E.type.isFloatType()) {
            if (ast.E instanceof VarExpr) {
                Ident id = ((SimpleVar) ((VarExpr) ast.E).V).I;
                emitFLOAD(id);
                frame.push();
            }
        } else {
            if (ast.E instanceof VarExpr) {
                Ident id = ((SimpleVar) ((VarExpr) ast.E).V).I;
                emitILOAD(id);
                frame.push();
            }
        }
        if (ast.E.type.isFloatType()) {
            emit(JVM.FRETURN);
        } else if (ast.E.type.isVoidType()) {
            emit(JVM.RETURN);
        } else {
            emit(JVM.IRETURN);
        }
        return null;
    }

    public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
        return null;
    }

    @Override
    public Object visitEmptyExprList(EmptyExprList ast, Object o) {
        return null;
    }

    public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
        return null;
    }

    public Object visitEmptyStmt(EmptyStmt ast, Object o) {
        return null;
    }

    // Expressions

    public Object visitCallExpr(CallExpr ast, Object o) {
        Frame frame = (Frame) o;
        String fname = ast.I.spelling;

        if (fname.equals("getInt")) {
            ast.AL.visit(this, o); // push args (if any) into the op stack
            emit("invokestatic VC/lang/System.getInt()I");
            frame.push();
        } else if (fname.equals("putInt")) {
            ast.AL.visit(this, o); // push args (if any) into the op stack
            emit("invokestatic VC/lang/System.putInt(I)V");
            frame.pop();
        } else if (fname.equals("putIntLn")) {
            ast.AL.visit(this, o); // push args (if any) into the op stack
            emit("invokestatic VC/lang/System/putIntLn(I)V");
            frame.pop();
        } else if (fname.equals("getFloat")) {
            ast.AL.visit(this, o); // push args (if any) into the op stack
            emit("invokestatic VC/lang/System/getFloat()F");
            frame.push();
        } else if (fname.equals("putFloat")) {
            ast.AL.visit(this, o); // push args (if any) into the op stack
            emit("invokestatic VC/lang/System/putFloat(F)V");
            frame.pop();
        } else if (fname.equals("putFloatLn")) {
            ast.AL.visit(this, o); // push args (if any) into the op stack
            emit("invokestatic VC/lang/System/putFloatLn(F)V");
            frame.pop();
        } else if (fname.equals("putBool")) {
            ast.AL.visit(this, o); // push args (if any) into the op stack
            emit("invokestatic VC/lang/System/putBool(Z)V");
            frame.pop();
        } else if (fname.equals("putBoolLn")) {
            ast.AL.visit(this, o); // push args (if any) into the op stack
            emit("invokestatic VC/lang/System/putBoolLn(Z)V");
            frame.pop();
        } else if (fname.equals("putString")) {
            ast.AL.visit(this, o);
            emit(JVM.INVOKESTATIC, "VC/lang/System/putString(Ljava/lang/String;)V");
            frame.pop();
        } else if (fname.equals("putStringLn")) {
            ast.AL.visit(this, o);
            emit(JVM.INVOKESTATIC, "VC/lang/System/putStringLn(Ljava/lang/String;)V");
            frame.pop();
        } else if (fname.equals("putLn")) {
            ast.AL.visit(this, o); // push args (if any) into the op stack
            emit("invokestatic VC/lang/System/putLn()V");
        } else { // programmer-defined functions

            FuncDecl fAST = (FuncDecl) ast.I.decl;

            // all functions except main are assumed to be instance methods
            if (frame.isMain())
                emit("aload_1"); // vc.funcname(...)
            else
                emit("aload_0"); // this.funcname(...)
            frame.push();

            ast.AL.visit(this, o);

            String retType = VCtoJavaType(fAST.T);

            // The types of the parameters of the called function are not
            // directly available in the FuncDecl node but can be gathered
            // by traversing its field PL.

            StringBuffer argsTypes = new StringBuffer("");
            List fpl = fAST.PL;
            int argsCount = 0;
            while (!fpl.isEmpty()) {
                if (((ParaList) fpl).P.T.equals(StdEnvironment.booleanType)) {
                    argsTypes.append("Z");
                    argsCount++;
                } else if (((ParaList) fpl).P.T.equals(StdEnvironment.floatType)) {
                    argsTypes.append("F");
                    argsCount++;
                } else if (((ParaList) fpl).P.T.equals(StdEnvironment.intType)) {
                    argsTypes.append("I");
                    argsCount++;
                } else if (((ParaList) fpl).P.T instanceof ArrayType) {
                    if (((ArrayType) ((ParaList) fpl).P.T).T.isFloatType()) {
                        argsTypes.append("[F");
                        argsCount++;
                    } else if (((ArrayType) ((ParaList) fpl).P.T).T.isIntType()) {
                        argsTypes.append("[I");
                        argsCount++;
                    } else if (((ArrayType) ((ParaList) fpl).P.T).T.isBooleanType()) {
                        argsTypes.append("[Z");
                        argsCount++;
                    }
                }
                fpl = ((ParaList) fpl).PL;
            }
            emit("invokevirtual", classname + "/" + fname + "(" + argsTypes + ")" + retType);
            frame.pop(argsCount + 1);

            if (!retType.equals("V"))
                frame.push();
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(AssignExpr ast, Object o) {
        Frame frame = (Frame) o;
        ast.E1.visit(this, o);
        ast.E2.visit(this, o);

        if (ast.E2 instanceof VarExpr) {
            AST decl = (((SimpleVar) ((VarExpr) ast.E2).V).I).decl;
            if (decl instanceof GlobalVarDecl) {

            } else {
                if (ast.E2.type.isFloatType()) {
                    if (ast.E2 instanceof VarExpr) {
                        Ident id = ((SimpleVar) ((VarExpr) ast.E2).V).I;
                        emitFLOAD(id);
                        frame.push();
                    }
                } else {
                    if (ast.E2 instanceof VarExpr) {
                        Ident id = ((SimpleVar) ((VarExpr) ast.E2).V).I;
                        emitILOAD(id);
                        frame.push();
                    }
                }
            }
        } else if (ast.E2 instanceof ArrayExpr) {
            if (ast.E2.type.isFloatType()) {
                emit(JVM.FALOAD);
                frame.pop(2);
                frame.push();
            } else if (ast.E2.type.isIntType()) {
                emit(JVM.IALOAD);
                frame.pop(2);
                frame.push();
            } else if (ast.E2.type.isBooleanType()) {
                emit(JVM.BALOAD);
                frame.pop(2);
                frame.push();
            }
        }

        if (ast.parent instanceof AssignExpr || ast.parent instanceof BinaryExpr || ast.parent instanceof ReturnStmt) {
            if (ast.E1 instanceof ArrayExpr) {
                emit(JVM.DUP + "_x2");
                frame.push(1);
            } else {
                emit(JVM.DUP);
                frame.push();
            }
        }
//        if (!(ast.E1 instanceof ArrayExpr) && ast.E2 instanceof AssignExpr) {
//            emit(JVM.DUP);
//            frame.push();
//        }
        if (ast.E1 instanceof ArrayExpr) {
            AST decl = (((SimpleVar) ((ArrayExpr) ast.E1).V).I).decl;
            if (decl instanceof GlobalVarDecl) {
                GlobalVarDecl globalVarDecl = (GlobalVarDecl) decl;
                if (!globalVarDecl.T.isArrayType()) {
                    emitPUTSTATIC(VCtoJavaType(globalVarDecl.T), globalVarDecl.I.spelling);
                    frame.pop();
                    return null;
                }
            }
        } else if (ast.E1 instanceof VarExpr) {
            AST decl = (((SimpleVar) ((VarExpr) ast.E1).V).I).decl;
            if (decl instanceof GlobalVarDecl) {
                GlobalVarDecl globalVarDecl = (GlobalVarDecl) decl;
                if (!globalVarDecl.T.isArrayType()) {
                    emitPUTSTATIC(VCtoJavaType(globalVarDecl.T), globalVarDecl.I.spelling);
                    frame.pop();
                    return null;
                }
            }
        }
        if (ast.E1 instanceof ArrayExpr) {
            if (ast.E1.type.isFloatType()) {
                emit(JVM.FASTORE);
                frame.pop(3);
            } else if (ast.E1.type.isIntType()) {
                emit(JVM.IASTORE);
                frame.pop(3);
            } else if (ast.E1.type.isBooleanType()) {
                emit(JVM.BASTORE);
                frame.pop(3);
            }
        } else if (ast.E1.type.isFloatType()) {
            if (ast.E1 instanceof VarExpr) {
                Ident id = ((SimpleVar) ((VarExpr) ast.E1).V).I;
                emitFSTORE(id);
                frame.pop();
            }
        } else {
            if (ast.E1 instanceof VarExpr) {
                Ident id = ((SimpleVar) ((VarExpr) ast.E1).V).I;
                emitISTORE(id);
                frame.pop();
            }
        }
        return null;
    }

    public Object visitEmptyExpr(EmptyExpr ast, Object o) {
        return null;
    }

    public Object visitIntExpr(IntExpr ast, Object o) {
        ast.IL.visit(this, o);
        return null;
    }

    public Object visitFloatExpr(FloatExpr ast, Object o) {
        ast.FL.visit(this, o);
        return null;
    }

    public Object visitBooleanExpr(BooleanExpr ast, Object o) {
        ast.BL.visit(this, o);
        return null;
    }

    public Object visitStringExpr(StringExpr ast, Object o) {
        ast.SL.visit(this, o);
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr ast, Object o) {
        Frame frame = (Frame) o;
        String op = ast.O.spelling;
        if (op.equals("i+")) {
            loadExpr(ast.E, frame);
            ast.E.visit(this, o);
        } else if (op.equals("i-")) {
            ast.E.visit(this, o);
            loadExpr(ast.E, frame);
            emit(JVM.INEG);
        } else if (op.equals("f+")) {
            ast.E.visit(this, o);
            loadExpr(ast.E, frame);
        } else if (op.equals("f-")) {
            ast.E.visit(this, o);
            loadExpr(ast.E, frame);
            emit(JVM.FNEG);
        } else if (op.equals("i!")) {
            ast.E.visit(this, o);
            String falseLable = frame.getNewLabel();
            String finishLable = frame.getNewLabel();
            emit(JVM.IFEQ + " " + falseLable);
            frame.pop();
            emitICONST(0);
            emit(JVM.GOTO + " " + finishLable);
            emit(falseLable + ":");
            emitICONST(1);
            emit(finishLable + ":");
            frame.push();
        } else if (op.equals("i2f")) {
            if (ast.E instanceof ArrayExpr) {
                if (((SimpleVar) ((ArrayExpr) ast.E).V).I.decl instanceof GlobalVarDecl) {
                    GlobalVarDecl globalVarDecl = (GlobalVarDecl) ((SimpleVar) ((ArrayExpr) ast.E).V).I.decl;
                    emitGETSTATIC(VCtoJavaType(globalVarDecl.T), ((SimpleVar) ((ArrayExpr) ast.E).V).I.spelling);
                    frame.push();
                    ((ArrayExpr) ast.E).E.visit(this, o);
                    if (ast.E.type.isFloatType()) {
                        emit(JVM.FALOAD);
                        frame.pop(2);
                        frame.push();
                    } else if (ast.E.type.isIntType()) {
                        emit(JVM.IALOAD);
                        frame.pop(2);
                        frame.push();
                    } else if (ast.E.type.isBooleanType()) {
                        emit(JVM.BALOAD);
                        frame.pop(2);
                        frame.push();
                    }
                } else if (((SimpleVar) ((ArrayExpr) ast.E).V).I.decl instanceof LocalVarDecl) {
                    LocalVarDecl localVarDecl = (LocalVarDecl) ((SimpleVar) ((ArrayExpr) ast.E).V).I.decl;
                    emitALOAD(localVarDecl.index);
                    frame.push();
                    ((ArrayExpr) ast.E).E.visit(this, o);
                    if (ast.E.type.isFloatType()) {
                        emit(JVM.FALOAD);
                        frame.pop(2);
                        frame.push();
                    } else if (ast.E.type.isIntType()) {
                        emit(JVM.IALOAD);
                        frame.pop(2);
                        frame.push();
                    } else if (ast.E.type.isBooleanType()) {
                        emit(JVM.BALOAD);
                        frame.pop(2);
                        frame.push();
                    }
                } else if (((SimpleVar) ((ArrayExpr) ast.E).V).I.decl instanceof ParaDecl) {
                    ParaDecl paraDecl = (ParaDecl) ((SimpleVar) ((ArrayExpr) ast.E).V).I.decl;
                    emitALOAD(paraDecl.index);
                    frame.push();
                    ((ArrayExpr) ast.E).E.visit(this, o);
                    if (ast.E.type.isFloatType()) {
                        emit(JVM.FALOAD);
                        frame.pop(2);
                        frame.push();
                    } else if (ast.E.type.isIntType()) {
                        emit(JVM.IALOAD);
                        frame.pop(2);
                        frame.push();
                    } else if (ast.E.type.isBooleanType()) {
                        emit(JVM.BALOAD);
                        frame.pop(2);
                        frame.push();
                    }
                }
            } else if (ast.E instanceof VarExpr) {
                if (((SimpleVar) ((VarExpr) ast.E).V).I.decl instanceof GlobalVarDecl) {
                    GlobalVarDecl globalVarDecl = (GlobalVarDecl) ((SimpleVar) ((VarExpr) ast.E).V).I.decl;
                    emitGETSTATIC(VCtoJavaType(globalVarDecl.T), ((SimpleVar) ((VarExpr) ast.E).V).I.spelling);
                    frame.push();
                } else if (((SimpleVar) ((VarExpr) ast.E).V).I.decl instanceof LocalVarDecl) {
                    LocalVarDecl localVarDecl = (LocalVarDecl) ((SimpleVar) ((VarExpr) ast.E).V).I.decl;
                    if (localVarDecl.T.isIntType()) {
                        emitILOAD(localVarDecl.index);
                    } else if (localVarDecl.T.isBooleanType()) {
                        emitILOAD(localVarDecl.index);
                    } else if (localVarDecl.T.isFloatType()) {
                        emitFLOAD(localVarDecl.index);
                    }
                    frame.push();
                } else if (((SimpleVar) ((VarExpr) ast.E).V).I.decl instanceof ParaDecl) {
                    ParaDecl paraDecl = (ParaDecl) ((SimpleVar) ((VarExpr) ast.E).V).I.decl;
                    if (paraDecl.T.isIntType()) {
                        emitILOAD(paraDecl.index);
                    } else if (paraDecl.T.isBooleanType()) {
                        emitILOAD(paraDecl.index);
                    } else if (paraDecl.T.isFloatType()) {
                        emitFLOAD(paraDecl.index);
                    }
                    frame.push();
                }
            } else {
                ast.E.visit(this, o);
            }
            emit(JVM.I2F);
        }
        return null;
    }

    private void loadExpr(Expr e, Frame frame) {
        if (e instanceof ArrayExpr) {
            if (e.type.isFloatType()) {
                emit(JVM.FALOAD);
                frame.pop(2);
                frame.push();
            } else if (e.type.isIntType()) {
                emit(JVM.IALOAD);
                frame.pop(2);
                frame.push();
            } else if (e.type.isBooleanType()) {
                emit(JVM.BALOAD);
                frame.pop(2);
                frame.push();
            }

        } else if (e.type.isFloatType()) {
            if (e instanceof VarExpr) {
                Ident id = ((SimpleVar) ((VarExpr) e).V).I;
                emitFLOAD(id);
                frame.push();
            }
        } else {
            if (e instanceof VarExpr) {
                Ident id = ((SimpleVar) ((VarExpr) e).V).I;
                emitILOAD(id);
                frame.push();
            }
        }
    }


    private static final int EXPR_RESULT_INT = 0;
    private static final int EXPR_RESULT_BOOLEAN = 1;
    private static final int EXPR_RESULT_FLOAT = 2;

    @Override
    public Object visitBinaryExpr(BinaryExpr ast, Object o) {
        Frame frame = (Frame) o;
        String op = ast.O.spelling;
        String trueLabel = frame.getNewLabel();
        String falseLabel = trueLabel;
        String endLabel = frame.getNewLabel();

        ast.E1.visit(this, o);
        if (ast.E1 instanceof ArrayExpr) {
            if (ast.E1.type.isFloatType()) {
                emit(JVM.FALOAD);
                frame.pop(2);
                frame.push();
            } else if (ast.E1.type.isIntType()) {
                emit(JVM.IALOAD);
                frame.pop(2);
                frame.push();
            } else if (ast.E1.type.isBooleanType()) {
                emit(JVM.BALOAD);
                frame.pop(2);
                frame.push();
            }
        } else if (ast.E1.type.isFloatType()) {
            if (ast.E1 instanceof VarExpr) {
                Ident id = ((SimpleVar) ((VarExpr) ast.E1).V).I;
                if (!(id.decl instanceof GlobalVarDecl)) {
                    emitFLOAD(id);
                    frame.push();
                }
            }
        } else {
            if (ast.E1 instanceof VarExpr) {
                Ident id = ((SimpleVar) ((VarExpr) ast.E1).V).I;
                if (!(id.decl instanceof GlobalVarDecl)) {
                    emitILOAD(id);
                    frame.push();
                }
            }
        }

        if (op.equals("i&&")) {
            emit(JVM.IFEQ, falseLabel);
            frame.pop();
        } else if (op.equals("i||")) {
            emit(JVM.IFLT, trueLabel);
            frame.pop();
        }

        ast.E2.visit(this, o);
        if (ast.E2 instanceof ArrayExpr) {
            if (ast.E2.type.isFloatType()) {
                emit(JVM.FALOAD);
                frame.pop(2);
                frame.push();
            } else if (ast.E2.type.isIntType()) {
                emit(JVM.IALOAD);
                frame.pop(2);
                frame.push();
            } else if (ast.E2.type.isBooleanType()) {
                emit(JVM.BALOAD);
                frame.pop(2);
                frame.push();
            }
        } else if (ast.E2.type.isFloatType()) {
            if (ast.E2 instanceof VarExpr) {
                Ident id = ((SimpleVar) ((VarExpr) ast.E2).V).I;
                emitFLOAD(id);
                frame.push();
            }
        } else {
            if (ast.E2 instanceof VarExpr) {
                Ident id = ((SimpleVar) ((VarExpr) ast.E2).V).I;
                emitILOAD(id);
                frame.push();
            }
        }
        if (op.equals("i+")) {
            emit(JVM.IADD);
            frame.pop(2);
            frame.push();
        } else if (op.equals("i-")) {
            emit(JVM.ISUB);
            frame.pop(2);
            frame.push();
        } else if (op.equals("i*")) {
            emit(JVM.IMUL);
            frame.pop(2);
            frame.push();
        } else if (op.equals("i/")) {
            emit(JVM.IDIV);
            frame.pop(2);
            frame.push();
        } else if (op.equals("f+")) {
            emit(JVM.FADD);
            frame.pop(2);
            frame.push();
        } else if (op.equals("f-")) {
            emit(JVM.FSUB);
            frame.pop(2);
            frame.push();
        } else if (op.equals("f*")) {
            emit(JVM.FMUL);
            frame.pop(2);
            frame.push();
        } else if (op.equals("f/")) {
            emit(JVM.FDIV);
            frame.pop(2);
            frame.push();
        } else if (op.equals("i&&")) {
            emit(JVM.IFEQ, falseLabel);
            frame.pop();
            emitICONST(1);
            emit(JVM.GOTO, endLabel);
            emit(falseLabel + ":");
            emitICONST(0);
            frame.push();
            emit(endLabel + ":");
        } else if (op.equals("i||")) {
            emit(JVM.IFLT, trueLabel);
            frame.pop();
            emitICONST(0);
            emit(JVM.GOTO, endLabel);
            emit(falseLabel + ":");
            emitICONST(1);
            emit(endLabel + ":");
        } else {
            if (op.contains("i")) {
                if (op.equals("i<")) {
                    emit(JVM.IF_ICMPLT, trueLabel);
                    frame.pop(2);
                } else if (op.equals("i<=")) {
                    emit(JVM.IF_ICMPLE, trueLabel);
                    frame.pop(2);
                } else if (op.equals("i>")) {
                    emit(JVM.IF_ICMPGT, trueLabel);
                    frame.pop(2);
                } else if (op.equals("i>=")) {
                    emit(JVM.IF_ICMPGE, trueLabel);
                    frame.pop(2);
                } else if (op.equals("i==")) {
                    emit(JVM.IF_ICMPEQ, trueLabel);
                    frame.pop(2);
                } else if (op.equals("i!=")) {
                    emit(JVM.IF_ICMPNE, trueLabel);
                    frame.pop(2);
                }
                emitICONST(0);
                frame.push();
                emit(JVM.GOTO, endLabel);
                emit(trueLabel + ":");
                emitICONST(1);
                emit(endLabel + ":");
            } else if (op.contains("f")) {
                if (op.equals("f<")) {
                    emit(JVM.FCMPG);
                    emit(JVM.IFLT, trueLabel);
                    frame.pop(2);
                    frame.push();
                } else if (op.equals("f<=")) {
                    emit(JVM.FCMPG);
                    emit(JVM.IFLE, trueLabel);
                    frame.pop(2);
                    frame.push();
                } else if (op.equals("f>")) {
                    emit(JVM.FCMPG);
                    emit(JVM.IFGT, trueLabel);
                    frame.pop(2);
                    frame.push();
                } else if (op.equals("f>=")) {
                    emit(JVM.FCMPG);
                    emit(JVM.IFGE, trueLabel);
                    frame.pop(2);
                    frame.push();
                } else if (op.equals("f==")) {
                    emit(JVM.FCMPG);
                    emit(JVM.IFEQ, trueLabel);
                    frame.pop(2);
                    frame.push();
                } else if (op.equals("f!=")) {
                    emit(JVM.FCMPG);
                    emit(JVM.IFNE, trueLabel);
                    frame.pop(2);
                    frame.push();
                }
                emitICONST(0);
                frame.push();
                emit(JVM.GOTO, endLabel);
                emit(trueLabel + ":");
                emitICONST(1);
                emit(endLabel + ":");
            }
        }


        return null;
    }

    @Override
    public Object visitInitExpr(InitExpr ast, Object o) {
        ast.IL.visit(this, o);
        return null;
    }

    @Override
    public Object visitExprList(ExprList ast, Object o) {
        Frame frame = (Frame) o;
        emit(JVM.DUP);
        frame.push();
        emitICONST(0);
        frame.push();
        ast.E.visit(this, o);
        if (ast.E.type.isIntType()) {
            emit(JVM.IASTORE);
        } else if (ast.E.type.isFloatType()) {
            emit(JVM.FASTORE);
        } else if (ast.E.type.isBooleanType()) {
            emit(JVM.BASTORE);
        }
        frame.pop(3);
        ast.EL.visit(this, o);
        return null;
    }

    @Override
    public Object visitArrayExpr(ArrayExpr ast, Object o) {
        Frame frame = (Frame) o;
        ast.V.visit(this, o);
        ast.E.visit(this, o);
        if (ast.parent instanceof Arg ||ast.parent instanceof ArrayExpr) {
            if (ast.type.isFloatType()) {
                emit(JVM.FALOAD);
                frame.pop(2);
                frame.push();
            } else if (ast.type.isIntType()) {
                emit(JVM.IALOAD);
                frame.pop(2);
                frame.push();
            } else if (ast.type.isBooleanType()) {
                emit(JVM.BALOAD);
                frame.pop(2);
                frame.push();
            }
        }
        return null;
    }

    @Override
    public Object visitVarExpr(VarExpr ast, Object o) {
        ast.V.visit(this, o);
        return null;
    }

    // Declarations

    public Object visitDeclList(DeclList ast, Object o) {
        ast.D.visit(this, o);
        ast.DL.visit(this, o);
        return null;
    }

    public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
        return null;
    }

    public Object visitFuncDecl(FuncDecl ast, Object o) {

        Frame frame;

        if (ast.I.spelling.equals("main")) {

            frame = new Frame(true);

            // Assume that main has one String parameter and reserve 0 for it
            frame.getNewIndex();

            emit(JVM.METHOD_START, "public static main([Ljava/lang/String;)V");
            // Assume implicitly that
            //      classname vc$;
            // appears before all local variable declarations.
            // (1) Reserve 1 for this object reference.

            frame.getNewIndex();

        } else {

            frame = new Frame(false);

            // all other programmer-defined functions are treated as if
            // they were instance methods
            frame.getNewIndex(); // reserve 0 for "this"

            String retType = VCtoJavaType(ast.T);

            // The types of the parameters of the called function are not
            // directly available in the FuncDecl node but can be gathered
            // by traversing its field PL.

            StringBuffer argsTypes = new StringBuffer("");
            List fpl = ast.PL;
            while (!fpl.isEmpty()) {
                if (((ParaList) fpl).P.T.equals(StdEnvironment.booleanType)) {
                    argsTypes.append("Z");
                } else if (((ParaList) fpl).P.T.equals(StdEnvironment.floatType)) {
                    argsTypes.append("F");
                } else if (((ParaList) fpl).P.T.equals(StdEnvironment.intType)) {
                    argsTypes.append("I");
                } else if (((ParaList) fpl).P.T instanceof ArrayType) {
                    if (((ArrayType) ((ParaList) fpl).P.T).T.isFloatType()) {
                        argsTypes.append("[F");
                    } else if (((ArrayType) ((ParaList) fpl).P.T).T.isIntType()) {
                        argsTypes.append("[I");
                    } else if (((ArrayType) ((ParaList) fpl).P.T).T.isBooleanType()) {
                        argsTypes.append("[Z");
                    }
                }
                fpl = ((ParaList) fpl).PL;
            }
            emit(JVM.METHOD_START, ast.I.spelling + "(" + argsTypes + ")" + retType);
        }

        ast.S.visit(this, frame);

        // JVM requires an explicit return in every method.
        // In VC, a function returning void may not contain a return, and
        // a function returning int or float is not guaranteed to contain
        // a return. Therefore, we add one at the end just to be sure.

        if (ast.T.equals(StdEnvironment.voidType)) {
            emit("");
            emit("; return may not be present in a VC function returning void");
            emit("; The following return inserted by the VC compiler");
            emit(JVM.RETURN);
        } else if (ast.I.spelling.equals("main")) {
            // In case VC's main does not have a return itself
            emit(JVM.RETURN);
        }
        emit(JVM.NOP);

        emit("");
        emit("; set limits used by this method");
        emit(JVM.LIMIT, "locals", frame.getNewIndex());

        emit(JVM.LIMIT, "stack", frame.getMaximumStackSize());
        emit(".end method");

        return null;
    }

    public Object visitGlobalVarDecl(GlobalVarDecl ast, Object o) {
        // nothing to be done
        return null;
    }

    public Object visitLocalVarDecl(LocalVarDecl ast, Object o) {
        Frame frame = (Frame) o;
        ast.index = frame.getNewIndex();
        String T = VCtoJavaType(ast.T);

        emit(JVM.VAR + " " + ast.index + " is " + ast.I.spelling + " " + T + " from " + (String) frame.scopeStart.peek() + " to " + (String) frame.scopeEnd.peek());

        if (!ast.E.isEmptyExpr()) {
            if (!(ast.E instanceof InitExpr)) {
                ast.E.visit(this, frame);
            }
            if (ast.E instanceof ArrayExpr) {
                if (ast.E.type.isFloatType()) {
                    emit(JVM.FALOAD);
                    frame.pop(2);
                    frame.push();
                } else if (ast.E.type.isIntType()) {
                    emit(JVM.IALOAD);
                    frame.pop(2);
                    frame.push();
                } else if (ast.E.type.isBooleanType()) {
                    emit(JVM.BALOAD);
                    frame.pop(2);
                    frame.push();
                }
            } else if (ast.T.isFloatType()) {
                if (ast.E instanceof VarExpr) {
                    Ident id = ((SimpleVar) ((VarExpr) ast.E).V).I;
                    emitFLOAD(id);
                    frame.push();
                }
            } else {
                if (ast.E instanceof VarExpr) {
                    Ident id = ((SimpleVar) ((VarExpr) ast.E).V).I;
                    emitILOAD(id);
                    frame.push();
                }
            }
            if (ast.T.isArrayType()) {
                ast.T.visit(this, frame);
                int positionIndex = 0;
                if (true) {
                    ExprList exprList = (ExprList) ((InitExpr) ast.E).IL;
                    while (!exprList.isEmpty()) {
                        emit(JVM.DUP);
                        frame.push();
                        emitICONST(positionIndex);
                        frame.push();
                        exprList.E.visit(this, frame);
                        if (exprList.E.type.isBooleanType()) {
                            emit(JVM.BASTORE);
                        } else if (exprList.E.type.isIntType()) {
                            emit(JVM.IASTORE);
                        } else if (exprList.E.type.isFloatType()) {
                            emit(JVM.FASTORE);
                        }
                        frame.pop(3);
                        if (exprList.EL instanceof EmptyExprList) {
                            break;
                        } else {
                            exprList = (ExprList) exprList.EL;
                            positionIndex++;
                        }
                    }
                }
                emitASTORE(ast.index);
                frame.pop();
            } else if (ast.T.isFloatType()) {
                emitFSTORE(ast.index);
                frame.pop();
            } else {
                emitISTORE(ast.index);
                frame.pop();
            }
        } else {
            if (ast.T.isArrayType()) {
                ast.T.visit(this, frame);
                emitASTORE(ast.index);
                frame.pop();
            }
        }
        return null;
    }

    // Parameters

    public Object visitParaList(ParaList ast, Object o) {
        ast.P.visit(this, o);
        ast.PL.visit(this, o);
        return null;
    }

    public Object visitParaDecl(ParaDecl ast, Object o) {
        Frame frame = (Frame) o;
        ast.index = frame.getNewIndex();
        String T = VCtoJavaType(ast.T);

        emit(JVM.VAR + " " + ast.index + " is " + ast.I.spelling + " " + T + " from " + (String) frame.scopeStart.peek() + " to " + (String) frame.scopeEnd.peek());
        return null;
    }

    public Object visitEmptyParaList(EmptyParaList ast, Object o) {
        return null;
    }

    // Arguments

    public Object visitArgList(ArgList ast, Object o) {
        ast.A.visit(this, o);
        ast.AL.visit(this, o);
        return null;
    }

    public Object visitArg(Arg ast, Object o) {
        ast.E.visit(this, o);
        return null;
    }

    public Object visitEmptyArgList(EmptyArgList ast, Object o) {
        return null;
    }

    // Types

    public Object visitIntType(IntType ast, Object o) {
        return null;
    }

    public Object visitFloatType(FloatType ast, Object o) {
        return null;
    }

    @Override
    public Object visitStringType(StringType ast, Object o) {
        return null;
    }

    @Override
    public Object visitArrayType(ArrayType ast, Object o) {
        Frame frame = (Frame) o;
        ast.E.visit(this, o);
        if (ast.T.isFloatType()) {
            emit(JVM.NEW_ARRAY_FLOAT);
        } else if (ast.T.isIntType()) {
            emit(JVM.NEW_ARRAY_INT);
        } else {
            emit(JVM.NEW_ARRAY_BOOLEAN);
        }
        return null;
    }

    public Object visitBooleanType(BooleanType ast, Object o) {
        return null;
    }

    public Object visitVoidType(VoidType ast, Object o) {
        return null;
    }

    public Object visitErrorType(ErrorType ast, Object o) {
        return null;
    }

    // Literals, Identifiers and Operators

    public Object visitIdent(Ident ast, Object o) {
        Frame frame = (Frame) o;
        if (ast.decl instanceof GlobalVarDecl) {
            if (ast.parent.parent.parent instanceof AssignExpr) {
                AssignExpr assignExpr = (AssignExpr) ast.parent.parent.parent;
                if (assignExpr.E1 == ast.parent.parent) {
                    GlobalVarDecl decl = (GlobalVarDecl) ast.decl;
                    if (decl.T.isArrayType()) {
                        emitGETSTATIC(VCtoJavaType(decl.T), decl.I.spelling);
                        frame.push();
                    }
                } else {
                    GlobalVarDecl decl = (GlobalVarDecl) ast.decl;
                    emitGETSTATIC(VCtoJavaType(decl.T), decl.I.spelling);
                    frame.push();
                }
            } else {
                GlobalVarDecl decl = (GlobalVarDecl) ast.decl;
                if (ast.parent.parent.parent instanceof Arg) {
                    emitGETSTATIC(VCtoJavaType(decl.T), decl.I.spelling);
                    frame.push();
                } else {
                    emitGETSTATIC(VCtoJavaType(decl.T), decl.I.spelling);
                    frame.push();
//                    if (decl.T.isArrayType()) {
//
//                    } else if (decl.T.isIntType() || decl.T.isFloatType() || decl.T.isBooleanType() || decl.T.isStringType()) {
//                        emitGETSTATIC(VCtoJavaType(decl.T), decl.I.spelling);
//                        frame.push();
//                    }
                }
            }

        } else if (ast.decl instanceof LocalVarDecl) {
            LocalVarDecl decl = (LocalVarDecl) ast.decl;
            if (decl.T.isArrayType()) {
                int index = decl.index;
                emitALOAD(index);
                frame.push();
            } else if (decl.T.isFloatType()) {
                if (ast.parent.parent.parent instanceof Arg || ast.parent.parent.parent instanceof ArrayExpr
                        || ast.parent.parent.parent instanceof CallExpr || ast.parent.parent.parent instanceof WhileStmt || ast.parent.parent.parent instanceof UnaryExpr) {
                    int index = decl.index;
                    emitFLOAD(index);
                    frame.push();
                }
            } else {
                if (ast.parent.parent.parent instanceof Arg || ast.parent.parent.parent instanceof ArrayExpr
                        || ast.parent.parent.parent instanceof CallExpr || ast.parent.parent.parent instanceof WhileStmt || ast.parent.parent.parent instanceof UnaryExpr) {
                    int index = decl.index;
                    emitILOAD(index);
                    frame.push();
                }
            }
        } else if (ast.decl instanceof ParaDecl) {
            ParaDecl decl = (ParaDecl) ast.decl;
            if (decl.T.isArrayType()) {
                int index = decl.index;
                emitALOAD(index);
                frame.push();
            } else if (decl.T.isFloatType()) {
                if (ast.parent.parent.parent instanceof Arg || ast.parent.parent.parent instanceof ArrayExpr
                        || ast.parent.parent.parent instanceof CallExpr || ast.parent.parent.parent instanceof WhileStmt || ast.parent.parent.parent instanceof UnaryExpr) {
                    int index = decl.index;
                    emitFLOAD(index);
                    frame.push();
                }
            } else {
                if (ast.parent.parent.parent instanceof Arg || ast.parent.parent.parent instanceof ArrayExpr
                        || ast.parent.parent.parent instanceof CallExpr || ast.parent.parent.parent instanceof WhileStmt || ast.parent.parent.parent instanceof UnaryExpr) {
                    int index = decl.index;
                    emitILOAD(index);
                    frame.push();
                }
            }
        }
        return null;
    }


    public Object visitIntLiteral(IntLiteral ast, Object o) {
        Frame frame = (Frame) o;
        emitICONST(Integer.parseInt(ast.spelling));
        frame.push();
        return null;
    }

    public Object visitFloatLiteral(FloatLiteral ast, Object o) {
        Frame frame = (Frame) o;
        emitFCONST(Float.parseFloat(ast.spelling));
        frame.push();
        return null;
    }

    public Object visitBooleanLiteral(BooleanLiteral ast, Object o) {
        Frame frame = (Frame) o;
        emitBCONST(ast.spelling.equals("true"));
        frame.push();
        return null;
    }

    public Object visitStringLiteral(StringLiteral ast, Object o) {
        Frame frame = (Frame) o;
        emit(JVM.LDC, "\"" + ast.spelling + "\"");
        frame.push();
        return null;
    }

    public Object visitOperator(Operator ast, Object o) {
        emitIF_ICMPCOND(ast.spelling, (Frame) o);
        return null;
    }

    // Variables

    public Object visitSimpleVar(SimpleVar ast, Object o) {
        ast.I.visit(this, o);
        return null;
    }

    // Auxiliary methods for byte code generation

    // The following method appends an instruction directly into the JVM
    // Code Store. It is called by all other overloaded emit methods.

    private void emit(String s) {
        JVM.append(new Instruction(s));
    }

    private void emit(String s1, String s2) {
        emit(s1 + " " + s2);
    }

    private void emit(String s1, int i) {
        emit(s1 + " " + i);
    }

    private void emit(String s1, float f) {
        emit(s1 + " " + f);
    }

    private void emit(String s1, String s2, int i) {
        emit(s1 + " " + s2 + " " + i);
    }

    private void emit(String s1, String s2, String s3) {
        emit(s1 + " " + s2 + " " + s3);
    }

    private void emitIF_ICMPCOND(String op, Frame frame) {
        String opcode;

        if (op.equals("i!="))
            opcode = JVM.IF_ICMPNE;
        else if (op.equals("i=="))
            opcode = JVM.IF_ICMPEQ;
        else if (op.equals("i<"))
            opcode = JVM.IF_ICMPLT;
        else if (op.equals("i<="))
            opcode = JVM.IF_ICMPLE;
        else if (op.equals("i>"))
            opcode = JVM.IF_ICMPGT;
        else // if (op.equals("i>="))
            opcode = JVM.IF_ICMPGE;

        String falseLabel = frame.getNewLabel();
        String nextLabel = frame.getNewLabel();

        emit(opcode, falseLabel);
        frame.pop(2);
        emit("iconst_0");
        emit("goto", nextLabel);
        emit(falseLabel + ":");
        emit(JVM.ICONST_1);
        frame.push();
        emit(nextLabel + ":");
    }

    private void emitFCMP(String op, Frame frame) {
        String opcode;

        if (op.equals("f!="))
            opcode = JVM.IFNE;
        else if (op.equals("f=="))
            opcode = JVM.IFEQ;
        else if (op.equals("f<"))
            opcode = JVM.IFLT;
        else if (op.equals("f<="))
            opcode = JVM.IFLE;
        else if (op.equals("f>"))
            opcode = JVM.IFGT;
        else // if (op.equals("f>="))
            opcode = JVM.IFGE;

        String falseLabel = frame.getNewLabel();
        String nextLabel = frame.getNewLabel();

        emit(JVM.FCMPG);
        frame.pop(2);
        emit(opcode, falseLabel);
        emit(JVM.ICONST_0);
        emit("goto", nextLabel);
        emit(falseLabel + ":");
        emit(JVM.ICONST_1);
        frame.push();
        emit(nextLabel + ":");

    }

    private void emitILOAD(int index) {
        if (index >= 0 && index <= 3)
            emit(JVM.ILOAD + "_" + index);
        else
            emit(JVM.ILOAD, index);
    }

    private void emitILOAD(Ident ast) {
        int index = -1;
        if (ast.decl instanceof ParaDecl)
            index = ((ParaDecl) ast.decl).index;
        else if (ast.decl instanceof LocalVarDecl)
            index = ((LocalVarDecl) ast.decl).index;
        else if (ast.decl instanceof GlobalVarDecl)
            index = ((GlobalVarDecl) ast.decl).index;

        if (index >= 0 && index <= 3)
            emit(JVM.ILOAD + "_" + index);
        else
            emit(JVM.ILOAD, index);
    }

    private void emitFLOAD(int index) {
        if (index >= 0 && index <= 3)
            emit(JVM.FLOAD + "_" + index);
        else
            emit(JVM.FLOAD, index);
    }

    private void emitFLOAD(Ident ast) {
        int index = -1;
        if (ast.decl instanceof ParaDecl)
            index = ((ParaDecl) ast.decl).index;
        else if (ast.decl instanceof LocalVarDecl)
            index = ((LocalVarDecl) ast.decl).index;
        else if (ast.decl instanceof GlobalVarDecl)
            index = ((GlobalVarDecl) ast.decl).index;
        if (index >= 0 && index <= 3)
            emit(JVM.FLOAD + "_" + index);
        else
            emit(JVM.FLOAD, index);
    }

    private void emitGETSTATIC(String T, String I) {
        emit(JVM.GETSTATIC, classname + "/" + I, T);
    }

    private void emitISTORE(Ident ast) {
        int index = -1;
        if (ast.decl instanceof ParaDecl)
            index = ((ParaDecl) ast.decl).index;
        else if (ast.decl instanceof LocalVarDecl)
            index = ((LocalVarDecl) ast.decl).index;
        else if (ast.decl instanceof GlobalVarDecl)
            index = ((GlobalVarDecl) ast.decl).index;

        if (index >= 0 && index <= 3)
            emit(JVM.ISTORE + "_" + index);
        else
            emit(JVM.ISTORE, index);
    }

    private void emitISTORE(int index) {
        if (index >= 0 && index <= 3)
            emit(JVM.ISTORE + "_" + index);
        else
            emit(JVM.ISTORE, index);
    }

    private void emitFSTORE(Ident ast) {
        int index = -1;
        if (ast.decl instanceof ParaDecl)
            index = ((ParaDecl) ast.decl).index;
        else if (ast.decl instanceof LocalVarDecl)
            index = ((LocalVarDecl) ast.decl).index;
        else if (ast.decl instanceof GlobalVarDecl)
            index = ((GlobalVarDecl) ast.decl).index;
        if (index >= 0 && index <= 3)
            emit(JVM.FSTORE + "_" + index);
        else
            emit(JVM.FSTORE, index);
    }

    private void emitFSTORE(int index) {
        if (index >= 0 && index <= 3)
            emit(JVM.FSTORE + "_" + index);
        else
            emit(JVM.FSTORE, index);
    }

    private void emitASTORE(Ident ast) {
        int index = -1;
        if (ast.decl instanceof ParaDecl)
            index = ((ParaDecl) ast.decl).index;
        else if (ast.decl instanceof LocalVarDecl)
            index = ((LocalVarDecl) ast.decl).index;
        else if (ast.decl instanceof GlobalVarDecl)
            index = ((GlobalVarDecl) ast.decl).index;
        if (index >= 0 && index <= 3)
            emit(JVM.ASTORE + "_" + index);
        else
            emit(JVM.ASTORE, index);
    }

    private void emitALOAD(Ident ast) {
        int index = -1;
        if (ast.decl instanceof ParaDecl)
            index = ((ParaDecl) ast.decl).index;
        else if (ast.decl instanceof LocalVarDecl)
            index = ((LocalVarDecl) ast.decl).index;
        else if (ast.decl instanceof GlobalVarDecl)
            index = ((GlobalVarDecl) ast.decl).index;
        if (index >= 0 && index <= 3)
            emit(JVM.ALOAD + "_" + index);
        else
            emit(JVM.ALOAD, index);
    }

    private void emitALOAD(int index) {
        if (index >= 0 && index <= 3)
            emit(JVM.ALOAD + "_" + index);
        else
            emit(JVM.ALOAD, index);
    }

    private void emitASTORE(int index) {
        if (index >= 0 && index <= 3)
            emit(JVM.ASTORE + "_" + index);
        else
            emit(JVM.ASTORE, index);
    }

    private void emitPUTSTATIC(String T, String I) {
        emit(JVM.PUTSTATIC, classname + "/" + I, T);
    }

    private void emitICONST(int value) {
        if (value == -1)
            emit(JVM.ICONST_M1);
        else if (value >= 0 && value <= 5)
            emit(JVM.ICONST + "_" + value);
        else if (value >= -128 && value <= 127)
            emit(JVM.BIPUSH, value);
        else if (value >= -32768 && value <= 32767)
            emit(JVM.SIPUSH, value);
        else
            emit(JVM.LDC, value);
    }

    private void emitFCONST(float value) {
        if (value == 0.0)
            emit(JVM.FCONST_0);
        else if (value == 1.0)
            emit(JVM.FCONST_1);
        else if (value == 2.0)
            emit(JVM.FCONST_2);
        else
            emit(JVM.LDC, value);
    }

    private void emitBCONST(boolean value) {
        if (value)
            emit(JVM.ICONST_1);
        else
            emit(JVM.ICONST_0);
    }

    private String VCtoJavaType(Type t) {
//        if (t.equals(StdEnvironment.booleanType))
//            return "Z";
//        else if (t.equals(StdEnvironment.intType))
//            return "I";
//        else if (t.equals(StdEnvironment.floatType))
//            return "F";
//        else // if (t.equals(StdEnvironment.voidType))
//            return "V";
        String result = null;
        if (t.isArrayType()) {
            ArrayType array = (ArrayType) t;
            if (array.T.isArrayType()) {
                //TODO
            } else if (array.T.isIntType()) {
                result = "[I";
            } else if (array.T.isFloatType()) {
                result = "[F";
            } else if (array.T.isStringType()) {
                //TODO
                result = "[Ljava/lang/String";
            } else if (array.T.isBooleanType()) {
                result = "[Z";
            }
        } else if (t.isBooleanType()) {
            result = "Z";
        } else if (t.isIntType()) {
            result = "I";
        } else if (t.isFloatType()) {
            result = "F";
        } else if (t.isVoidType()) {
            result = "V";
        } else if (t.isStringType()) {
            //TODO
            result = "Ljava/lang/String";
        }
        return result;
    }

}
