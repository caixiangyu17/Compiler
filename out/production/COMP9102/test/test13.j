.class public test/test13
.super java/lang/Object
	
.field static ia [I
.field static fa [F
	
	; standard class static initializer 
.method static <clinit>()V
	
	iconst_3
	newarray int
	dup
	iconst_0
	iconst_0
	iastore
	dup
	iconst_1
	iconst_1
	iastore
	dup
	iconst_2
	iconst_2
	iastore
	putstatic test/test13/ia [I
	iconst_3
	newarray float
	dup
	iconst_0
	iconst_0
	i2f
	fastore
	dup
	iconst_1
	iconst_1
	i2f
	fastore
	dup
	iconst_2
	iconst_2
	i2f
	fastore
	putstatic test/test13/fa [F
	
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
.method array([I[F)V
L0:
.var 0 is this Ltest/test13; from L0 to L1
.var 1 is ib [I from L0 to L1
.var 2 is fb [F from L0 to L1
.var 3 is ic [I from L0 to L1
	iconst_3
	newarray int
	dup
	iconst_0
	iconst_2
	iastore
	dup
	iconst_1
	iconst_0
	iastore
	dup
	iconst_2
	iconst_1
	iastore
	astore_3
.var 4 is fc [F from L0 to L1
	iconst_3
	newarray float
	dup
	iconst_0
	fconst_2
	fastore
	dup
	iconst_1
	fconst_0
	fastore
	dup
	iconst_2
	fconst_1
	fastore
	astore 4
	getstatic test/test13/ia [I
	iconst_1
	iaload
	invokestatic VC/lang/System.putInt(I)V
	aload_1
	iconst_1
	iaload
	invokestatic VC/lang/System.putInt(I)V
	aload_3
	iconst_1
	iaload
	invokestatic VC/lang/System.putInt(I)V
	getstatic test/test13/ia [I
	getstatic test/test13/ia [I
	iconst_1
	iaload
	iaload
	invokestatic VC/lang/System.putInt(I)V
	getstatic test/test13/ia [I
	aload_1
	iconst_1
	iaload
	iaload
	invokestatic VC/lang/System.putInt(I)V
	getstatic test/test13/ia [I
	aload_3
	iconst_1
	iaload
	iaload
	invokestatic VC/lang/System.putInt(I)V
	aload_1
	getstatic test/test13/ia [I
	iconst_1
	iaload
	iaload
	invokestatic VC/lang/System.putInt(I)V
	aload_1
	aload_1
	iconst_1
	iaload
	iaload
	invokestatic VC/lang/System.putInt(I)V
	aload_1
	aload_3
	iconst_1
	iaload
	iaload
	invokestatic VC/lang/System.putInt(I)V
	aload_3
	getstatic test/test13/ia [I
	iconst_1
	iaload
	iaload
	invokestatic VC/lang/System.putInt(I)V
	aload_3
	aload_1
	iconst_1
	iaload
	iaload
	invokestatic VC/lang/System.putInt(I)V
	aload_3
	aload_3
	iconst_1
	iaload
	iaload
	invokestatic VC/lang/System.putInt(I)V
	getstatic test/test13/fa [F
	iconst_1
	faload
	invokestatic VC/lang/System/putFloat(F)V
	aload_2
	iconst_1
	faload
	invokestatic VC/lang/System/putFloat(F)V
	aload 4
	iconst_1
	faload
	invokestatic VC/lang/System/putFloat(F)V
	getstatic test/test13/fa [F
	getstatic test/test13/ia [I
	iconst_1
	iaload
	faload
	invokestatic VC/lang/System/putFloat(F)V
	getstatic test/test13/fa [F
	aload_1
	iconst_1
	iaload
	faload
	invokestatic VC/lang/System/putFloat(F)V
	getstatic test/test13/fa [F
	aload_3
	iconst_1
	iaload
	faload
	invokestatic VC/lang/System/putFloat(F)V
	aload_2
	getstatic test/test13/ia [I
	iconst_1
	iaload
	faload
	invokestatic VC/lang/System/putFloat(F)V
	aload_2
	aload_1
	iconst_1
	iaload
	faload
	invokestatic VC/lang/System/putFloat(F)V
	aload_2
	aload_3
	iconst_1
	iaload
	faload
	invokestatic VC/lang/System/putFloat(F)V
	aload 4
	getstatic test/test13/ia [I
	iconst_1
	iaload
	faload
	invokestatic VC/lang/System/putFloat(F)V
	aload 4
	aload_1
	iconst_1
	iaload
	faload
	invokestatic VC/lang/System/putFloat(F)V
	aload 4
	aload_3
	iconst_1
	iaload
	faload
	invokestatic VC/lang/System/putFloat(F)V
L1:
	
	; return may not be present in a VC function returning void
	; The following return inserted by the VC compiler
	return
	nop
	
	; set limits used by this method
.limit locals 5
.limit stack 4
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest/test13; from L0 to L1
	new test/test13
	dup
	invokenonvirtual test/test13/<init>()V
	astore_1
.var 2 is ic [I from L0 to L1
	iconst_3
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
	iconst_0
	iastore
	astore_2
.var 3 is fc [F from L0 to L1
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
	iconst_0
	i2f
	fastore
	astore_3
	aload_1
	aload_2
	aload_3
	invokevirtual test/test13/array([I[F)V
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 4
.limit stack 4
.end method
