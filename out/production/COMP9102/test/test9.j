.class public test/test9
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
	putstatic test/test9/b [Z
	
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
.method breaker()I
L0:
.var 0 is this Ltest/test9; from L0 to L1
.var 1 is i I from L0 to L1
	iconst_0
	istore_1
	iconst_0
	istore_1
L2:
	iload_1
	bipush 100
	if_icmplt L5
	iconst_0
	goto L6
L5:
	iconst_1
L6:
	ifeq L4
	getstatic test/test9/b [Z
	iload_1
	baload
	ifeq L10
	goto L4
	goto L11
L10:
	goto L3
L11:
L3:
	iload_1
	iconst_1
	iadd
	istore_1
	goto L2
L4:
	iload_1
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest/test9; from L0 to L1
	new test/test9
	dup
	invokenonvirtual test/test9/<init>()V
	astore_1
	aload_1
	invokevirtual test/test9/breaker()I
	invokestatic VC/lang/System/putIntLn(I)V
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
