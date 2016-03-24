.class public test/test11
.super java/lang/Object
	
.field static b [Z
.field static f [F
.field static j [I
	
	; standard class static initializer 
.method static <clinit>()V
	
	bipush 8
	newarray boolean
	dup
	iconst_0
	iconst_0
	bastore
	dup
	iconst_1
	iconst_0
	bastore
	dup
	iconst_2
	iconst_0
	bastore
	dup
	iconst_3
	iconst_0
	bastore
	dup
	iconst_4
	iconst_0
	bastore
	dup
	iconst_5
	iconst_0
	bastore
	dup
	bipush 6
	iconst_0
	bastore
	dup
	bipush 7
	iconst_1
	bastore
	putstatic test/test11/b [Z
	bipush 10
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
	dup
	iconst_3
	iconst_4
	i2f
	fastore
	dup
	iconst_4
	iconst_5
	i2f
	fastore
	dup
	iconst_5
	bipush 6
	i2f
	fastore
	dup
	bipush 6
	bipush 7
	i2f
	fastore
	dup
	bipush 7
	bipush 8
	i2f
	fastore
	dup
	bipush 8
	bipush 9
	i2f
	fastore
	dup
	bipush 9
	bipush 10
	i2f
	fastore
	putstatic test/test11/f [F
	bipush 10
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
	dup
	iconst_4
	iconst_5
	iastore
	dup
	iconst_5
	bipush 6
	iastore
	dup
	bipush 6
	bipush 7
	iastore
	dup
	bipush 7
	bipush 8
	iastore
	dup
	bipush 8
	bipush 9
	iastore
	dup
	bipush 9
	bipush 10
	iastore
	putstatic test/test11/j [I
	
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
.method loop(ZI)I
L0:
.var 0 is this Ltest/test11; from L0 to L1
.var 1 is c Z from L0 to L1
.var 2 is i I from L0 to L1
.var 3 is a I from L0 to L1
	iconst_0
	istore_3
.var 4 is d F from L0 to L1
	bipush 10
	i2f
	fstore 4
L2:
	iload_1
	ifeq L3
	getstatic test/test11/b [Z
	iload_2
	baload
	ifeq L7
	goto L3
	goto L8
L7:
L8:
	iload_2
	iconst_1
	iadd
	istore_2
	goto L2
	ldc "this should not be seen"
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
	goto L2
L3:
	iconst_0
	istore_3
L13:
	iload_3
	i2f
	fload 4
	fcmpg
	iflt L16
	iconst_0
	goto L17
L16:
	iconst_1
L17:
	ifeq L15
	getstatic test/test11/f [F
	iload_3
	faload
	invokestatic VC/lang/System/putFloat(F)V
	ldc " "
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
L14:
	iload_3
	iconst_1
	iadd
	istore_3
	goto L13
L15:
	ldc " "
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
	iconst_0
	istore_3
L22:
	iload_3
	i2f
	fload 4
	fcmpg
	iflt L25
	iconst_0
	goto L26
L25:
	iconst_1
L26:
	ifeq L24
	getstatic test/test11/j [I
	iload_3
	iaload
	invokestatic VC/lang/System.putInt(I)V
	ldc " "
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
L23:
	iload_3
	iconst_1
	iadd
	istore_3
	goto L22
L24:
	ldc " "
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
	iload_2
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 5
.limit stack 4
.end method
.method breaker()I
L0:
.var 0 is this Ltest/test11; from L0 to L1
.var 1 is c Z from L0 to L1
	iconst_1
	istore_1
.var 2 is i I from L0 to L1
	iconst_0
	istore_2
	aload_0
	iload_1
	iload_2
	invokevirtual test/test11/loop(ZI)I
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 3
.limit stack 3
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest/test11; from L0 to L1
	new test/test11
	dup
	invokenonvirtual test/test11/<init>()V
	astore_1
	aload_1
	invokevirtual test/test11/breaker()I
	invokestatic VC/lang/System/putIntLn(I)V
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
