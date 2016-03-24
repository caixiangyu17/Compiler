.class public test/test6
.super java/lang/Object
	
.field static a [F
	
	; standard class static initializer 
.method static <clinit>()V
	
	iconst_3
	newarray float
	dup
	iconst_0
	iconst_1
	i2f
	fastore
	dup
	iconst_1
	iconst_2
	i2f
	fastore
	dup
	iconst_2
	iconst_3
	i2f
	fastore
	putstatic test/test6/a [F
	
	; set limits used by this method
.limit locals 0
.limit stack 4
	return
.end method
	
	; standard constructor initializer 
.method public <init>()V
.limit stack 1
.limit locals 1
	aload_0
	invokespecial java/lang/Object/<init>()V
	return
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest/test6; from L0 to L1
	new test/test6
	dup
	invokenonvirtual test/test6/<init>()V
	astore_1
.var 2 is b I from L0 to L1
	iconst_0
	istore_2
.var 3 is c F from L0 to L1
	iconst_3
	i2f
	fstore_3
.var 4 is d Z from L0 to L1
	iconst_1
	istore 4
	iload_2
	invokestatic VC/lang/System/putIntLn(I)V
	iconst_0
	istore_2
L2:
	iload_2
	i2f
	fload_3
	fcmpg
	iflt L5
	iconst_0
	goto L6
L5:
	iconst_1
L6:
	ifeq L4
	getstatic test/test6/a [F
	iload_2
	faload
	invokestatic VC/lang/System/putFloatLn(F)V
L3:
	iload_2
	iconst_1
	iadd
	istore_2
	goto L2
L4:
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 5
.limit stack 3
.end method
