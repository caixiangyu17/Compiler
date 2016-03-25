.class public test/test15
.super java/lang/Object
	
	
	; standard class static initializer 
.method static <clinit>()V
	
	
	; set limits used by this method
.limit locals 0
.limit stack 0
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
.method init([II)V
L0:
.var 0 is this Ltest/test15; from L0 to L1
.var 1 is a [I from L0 to L1
.var 2 is n I from L0 to L1
.var 3 is i I from L0 to L1
	iconst_0
	istore_3
	iconst_0
	istore_3
L2:
	iload_3
	iload_2
	if_icmplt L5
	iconst_0
	goto L6
L5:
	iconst_1
L6:
	ifeq L4
	aload_1
	iload_3
	iload_3
	iastore
L3:
	iload_3
	iconst_1
	iadd
	istore_3
	goto L2
L4:
L1:
	
	; return may not be present in a VC function returning void
	; The following return inserted by the VC compiler
	return
	nop
	
	; set limits used by this method
.limit locals 4
.limit stack 3
.end method
.method print([II)V
L0:
.var 0 is this Ltest/test15; from L0 to L1
.var 1 is a [I from L0 to L1
.var 2 is n I from L0 to L1
.var 3 is i I from L0 to L1
	iconst_0
	istore_3
L2:
	iload_3
	iload_2
	if_icmplt L4
	iconst_0
	goto L5
L4:
	iconst_1
L5:
	ifeq L3
	aload_1
	iload_3
	iaload
	invokestatic VC/lang/System/putIntLn(I)V
	iload_3
	iconst_1
	iadd
	istore_3
	goto L2
L3:
L1:
	
	; return may not be present in a VC function returning void
	; The following return inserted by the VC compiler
	return
	nop
	
	; set limits used by this method
.limit locals 4
.limit stack 2
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest/test15; from L0 to L1
	new test/test15
	dup
	invokenonvirtual test/test15/<init>()V
	astore_1
.var 2 is a [I from L0 to L1
	iconst_3
	newarray int
	astore_2
	aload_1
	aload_2
	iconst_3
	invokevirtual test/test15/init([II)V
	aload_1
	aload_2
	iconst_3
	invokevirtual test/test15/print([II)V
	return
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 3
.limit stack 3
.end method
