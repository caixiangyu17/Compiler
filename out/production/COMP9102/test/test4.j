.class public test/test4
.super java/lang/Object
	
.field static a [F
.field static c [I
	
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
	putstatic test/test4/a [F
	iconst_4
	newarray int
	dup
	iconst_0
	iconst_1
	iastore
	dup
	iconst_1
	iconst_2
	iastore
	dup
	iconst_2
	iconst_3
	iastore
	dup
	iconst_3
	iconst_4
	iastore
	putstatic test/test4/c [I
	
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
.var 1 is vc$ Ltest/test4; from L0 to L1
	new test/test4
	dup
	invokenonvirtual test/test4/<init>()V
	astore_1
.var 2 is b I from L0 to L1
	iconst_4
	istore_2
.var 3 is d [Z from L0 to L1
	iconst_4
	newarray boolean
	dup
	iconst_0
	iconst_1
	bastore
	dup
	iconst_1
	iconst_0
	bastore
	dup
	iconst_2
	iconst_1
	bastore
	dup
	iconst_3
	iconst_1
	bastore
	astore_3
	getstatic test/test4/a [F
	iconst_1
	iload_2
	i2f
	fastore
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 4
.limit stack 4
.end method
