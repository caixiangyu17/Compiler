.class public test/test3
.super java/lang/Object
	
.field static a I
.field static a1 I
	
	; standard class static initializer 
.method static <clinit>()V
	
	iconst_0
	putstatic test/test3/a I
	iconst_1
	putstatic test/test3/a1 I
	
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
.var 1 is vc$ Ltest/test3; from L0 to L1
	new test/test3
	dup
	invokenonvirtual test/test3/<init>()V
	astore_1
.var 2 is a2 I from L0 to L1
	iconst_1
	istore_2
.var 3 is a3 I from L0 to L1
	iconst_2
	istore_3
	getstatic test/test3/a1 I
	dup
	putstatic test/test3/a I
	istore_2
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 4
.limit stack 2
.end method
