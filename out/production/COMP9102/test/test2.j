.class public test/test2
.super java/lang/Object
	
.field static a I
.field static b F
.field static c Z
.field static a1 I
.field static b1 F
.field static c1 Z
	
	; standard class static initializer 
.method static <clinit>()V
	
	iconst_0
	putstatic test/test2/a I
	fconst_0
	putstatic test/test2/b F
	iconst_0
	putstatic test/test2/c Z
	iconst_1
	putstatic test/test2/a1 I
	fconst_2
	putstatic test/test2/b1 F
	iconst_1
	putstatic test/test2/c1 Z
	
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
.var 1 is vc$ Ltest/test2; from L0 to L1
	new test/test2
	dup
	invokenonvirtual test/test2/<init>()V
	astore_1
	getstatic test/test2/a1 I
	putstatic test/test2/a I
	getstatic test/test2/b1 F
	putstatic test/test2/b F
	getstatic test/test2/c1 Z
	putstatic test/test2/c Z
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
