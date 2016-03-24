.class public test/test1
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
	putstatic test/test1/a I
	fconst_0
	putstatic test/test1/b F
	iconst_0
	putstatic test/test1/c Z
	iconst_1
	putstatic test/test1/a1 I
	fconst_2
	putstatic test/test1/b1 F
	iconst_1
	putstatic test/test1/c1 Z
	
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
.method test1(I)I
L0:
.var 0 is this Ltest/test1; from L0 to L1
.var 1 is a I from L0 to L1
	iconst_1
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 1
.end method
.method test2(F)F
L0:
.var 0 is this Ltest/test1; from L0 to L1
.var 1 is a F from L0 to L1
	iconst_1
	i2f
	fstore_1
	fload_1
	freturn
L1:
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 1
.end method
.method test3(Z)Z
L0:
.var 0 is this Ltest/test1; from L0 to L1
.var 1 is a Z from L0 to L1
	iconst_1
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 1
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest/test1; from L0 to L1
	new test/test1
	dup
	invokenonvirtual test/test1/<init>()V
	astore_1
	aload_1
	getstatic test/test1/a1 I
	invokevirtual test/test1/test1(I)I
	aload_1
	getstatic test/test1/b1 F
	invokevirtual test/test1/test2(F)F
	aload_1
	getstatic test/test1/c1 Z
	invokevirtual test/test1/test3(Z)Z
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 4
.end method
