.class public test/test5
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
	putstatic test/test5/a [F
	
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
.method func([F)F
L0:
.var 0 is this Ltest/test5; from L0 to L1
.var 1 is f [F from L0 to L1
	aload_1
	iconst_1
	faload
	freturn
L1:
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest/test5; from L0 to L1
	new test/test5
	dup
	invokenonvirtual test/test5/<init>()V
	astore_1
.var 2 is b F from L0 to L1
	iconst_1
	i2f
	fstore_2
	aload_1
	getstatic test/test5/a [F
	invokevirtual test/test5/func([F)F
	aload_1
	getstatic test/test5/a [F
	invokevirtual test/test5/func([F)F
	fstore_2
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 3
.limit stack 3
.end method
