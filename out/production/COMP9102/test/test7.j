.class public test/test7
.super java/lang/Object
	
.field static a I
	
	; standard class static initializer 
.method static <clinit>()V
	
	iconst_0
	putstatic test/test7/a I
	
	; set limits used by this method
.limit locals 0
.limit stack 1
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
.var 1 is vc$ Ltest/test7; from L0 to L1
	new test/test7
	dup
	invokenonvirtual test/test7/<init>()V
	astore_1
.var 2 is b I from L0 to L1
	bipush 100
	istore_2
L2:
	getstatic test/test7/a I
	iload_2
	if_icmple L4
	iconst_0
	goto L5
L4:
	iconst_1
L5:
	ifeq L3
	getstatic test/test7/a I
	invokestatic VC/lang/System.putInt(I)V
	ldc " "
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
	getstatic test/test7/a I
	iconst_1
	iadd
	putstatic test/test7/a I
	goto L2
L3:
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 3
.limit stack 2
.end method
