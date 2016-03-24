.class public test/test12
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
	putstatic test/test12/b [Z
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
	putstatic test/test12/f [F
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
	putstatic test/test12/j [I
	
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
.var 0 is this Ltest/test12; from L0 to L1
.var 1 is c Z from L0 to L1
.var 2 is i I from L0 to L1
.var 3 is a I from L0 to L1
	iconst_0
	istore_3
.var 4 is d F from L0 to L1
	bipush 10
	i2f
	fstore 4
.var 5 is temp F from L0 to L1
	iconst_0
	i2f
	fstore 5
.var 6 is temp1 F from L0 to L1
	iconst_0
	i2f
	fstore 6
.var 7 is temp2 F from L0 to L1
	iconst_0
	i2f
	fstore 7
L2:
	iload_1
	ifeq L4
	iconst_0
	goto L5
L4:
	iconst_1
L5:
	ifeq L3
	getstatic test/test12/b [Z
	iload_2
	baload
	ifeq L9
	goto L3
	goto L10
L9:
L10:
	iload_2
	iconst_1
	iadd
	istore_2
	goto L2
	ldc "this should not be seen"
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
	goto L2
L3:
L15:
	iload_1
	ifeq L16
	ldc "this should not be seen"
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
	getstatic test/test12/b [Z
	iload_2
	baload
	ifeq L20
	goto L16
	goto L21
L20:
L21:
	iload_2
	iconst_1
	iadd
	istore_2
	goto L15
	goto L15
L16:
	iconst_0
	istore_3
L26:
	iload_3
	i2f
	fload 4
	fcmpg
	iflt L29
	iconst_0
	goto L30
L29:
	iconst_1
L30:
	ifeq L28
	getstatic test/test12/f [F
	iload_3
	faload
	fneg
	invokestatic VC/lang/System/putFloat(F)V
	ldc " "
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
L27:
	iload_3
	iconst_1
	iadd
	istore_3
	goto L26
L28:
	ldc " "
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
	iconst_0
	istore_3
L35:
	iload_3
	i2f
	fload 4
	fcmpg
	iflt L38
	iconst_0
	goto L39
L38:
	iconst_1
L39:
	ifeq L37
	getstatic test/test12/j [I
	iload_3
	iaload
	ineg
	invokestatic VC/lang/System.putInt(I)V
	ldc " "
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
L36:
	iload_3
	iconst_1
	iadd
	istore_3
	goto L35
L37:
	ldc " "
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
	ldc "Binary test"
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
	iconst_0
	istore_3
L44:
	iload_3
	i2f
	fload 4
	fcmpg
	iflt L47
	iconst_0
	goto L48
L47:
	iconst_1
L48:
	ifeq L46
	getstatic test/test12/f [F
	iload_3
	faload
	getstatic test/test12/j [I
	iload_3
	iaload
	ineg
	i2f
	getstatic test/test12/f [F
	iload_3
	faload
	fmul
	fadd
	getstatic test/test12/f [F
	iload_3
	faload
	fmul
	getstatic test/test12/j [I
	iload_3
	iaload
	ineg
	i2f
	getstatic test/test12/f [F
	iload_3
	faload
	fdiv
	fadd
	fstore 5
	fload 5
	invokestatic VC/lang/System/putFloat(F)V
	ldc " "
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
L45:
	iload_3
	iconst_1
	iadd
	istore_3
	goto L44
L46:
	ldc " "
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
	ldc "Multi assign test"
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
	iconst_0
	istore_3
L63:
	iload_3
	i2f
	fload 4
	fcmpg
	iflt L66
	iconst_0
	goto L67
L66:
	iconst_1
L67:
	ifeq L65
	getstatic test/test12/f [F
	iload_3
	getstatic test/test12/f [F
	iload_3
	faload
	getstatic test/test12/j [I
	iload_3
	iaload
	ineg
	i2f
	getstatic test/test12/f [F
	iload_3
	faload
	fmul
	fadd
	dup_x2
	fastore
	dup
	fstore 7
	dup
	fstore 6
	fstore 5
	fload 5
	invokestatic VC/lang/System/putFloat(F)V
	ldc " "
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
L64:
	iload_3
	iconst_1
	iadd
	istore_3
	goto L63
L65:
	ldc " "
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
	iload_2
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 8
.limit stack 10
.end method
.method breaker()I
L0:
.var 0 is this Ltest/test12; from L0 to L1
.var 1 is c Z from L0 to L1
	iconst_0
	istore_1
.var 2 is i I from L0 to L1
	iconst_0
	istore_2
	aload_0
	iload_1
	iload_2
	invokevirtual test/test12/loop(ZI)I
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
.var 1 is vc$ Ltest/test12; from L0 to L1
	new test/test12
	dup
	invokenonvirtual test/test12/<init>()V
	astore_1
	aload_1
	invokevirtual test/test12/breaker()I
	invokestatic VC/lang/System/putIntLn(I)V
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
