.class public test/test10
.super java/lang/Object
	
.field static b [Z
	
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
	putstatic test/test10/b [Z
	
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
.var 0 is this Ltest/test10; from L0 to L1
.var 1 is c Z from L0 to L1
.var 2 is i I from L0 to L1
L2:
	iload_1
	ifeq L3
	getstatic test/test10/b [Z
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
	iload_2
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 3
.limit stack 2
.end method
.method breaker()I
L0:
.var 0 is this Ltest/test10; from L0 to L1
.var 1 is c Z from L0 to L1
	iconst_1
	istore_1
.var 2 is i I from L0 to L1
	iconst_0
	istore_2
	aload_0
	iload_1
	iload_2
	invokevirtual test/test10/loop(ZI)I
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
.var 1 is vc$ Ltest/test10; from L0 to L1
	new test/test10
	dup
	invokenonvirtual test/test10/<init>()V
	astore_1
	aload_1
	invokevirtual test/test10/breaker()I
	invokestatic VC/lang/System/putIntLn(I)V
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
