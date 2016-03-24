.class public test/test14
.super java/lang/Object
	
.field static a [I
	
	; standard class static initializer 
.method static <clinit>()V
	
	iconst_3
	newarray int
	putstatic test/test14/a [I
	
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
.method f()Z
L0:
.var 0 is this Ltest/test14; from L0 to L1
	iconst_0
	invokestatic VC/lang/System/putBool(Z)V
	iconst_0
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 1
.limit stack 1
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest/test14; from L0 to L1
	new test/test14
	dup
	invokenonvirtual test/test14/<init>()V
	astore_1
.var 2 is b [I from L0 to L1
	iconst_3
	newarray int
	astore_2
.var 3 is i I from L0 to L1
	iconst_1
	istore_3
.var 4 is j I from L0 to L1
	iconst_1
	istore 4
	aload_2
	iload_3
	iload 4
	iadd
	getstatic test/test14/a [I
	iconst_0
	iaload
	iload 4
	iadd
	iastore
	aload_2
	iconst_2
	iaload
	iconst_1
	if_icmpeq L6
	iconst_0
	goto L7
L6:
	iconst_1
L7:
	invokestatic VC/lang/System/putBoolLn(Z)V
	getstatic test/test14/a [I
	iconst_0
	getstatic test/test14/a [I
	iconst_1
	getstatic test/test14/a [I
	iconst_2
	aload_2
	iconst_2
	iaload
	dup_x2
	iastore
	dup_x2
	iastore
	iastore
	getstatic test/test14/a [I
	iconst_0
	iaload
	getstatic test/test14/a [I
	iconst_1
	iaload
	if_icmpeq L12
	iconst_0
	goto L13
L12:
	iconst_1
L13:
	ifeq L10
	getstatic test/test14/a [I
	iconst_1
	iaload
	getstatic test/test14/a [I
	iconst_2
	iaload
	if_icmpeq L14
	iconst_0
	goto L15
L14:
	iconst_1
L15:
	ifeq L10
	iconst_1
	goto L11
L10:
	iconst_0
L11:
	ifeq L8
	getstatic test/test14/a [I
	iconst_2
	iaload
	aload_2
	iconst_2
	iaload
	if_icmpeq L16
	iconst_0
	goto L17
L16:
	iconst_1
L17:
	ifeq L8
	iconst_1
	goto L9
L8:
	iconst_0
L9:
	invokestatic VC/lang/System/putBoolLn(Z)V
	iconst_0
	ifeq L18
	aload_1
	invokevirtual test/test14/f()Z
	ifeq L18
	iconst_1
	goto L19
L18:
	iconst_0
L19:
	pop
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 5
.limit stack 8
.end method
