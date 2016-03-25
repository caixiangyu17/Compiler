.class public test/gcd
.super java/lang/Object
	
.field static i I
.field static j I
	
	; standard class static initializer 
.method static <clinit>()V
	
	iconst_0
	putstatic test/gcd/i I
	iconst_0
	putstatic test/gcd/j I
	
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
.method gcd(II)I
L0:
.var 0 is this Ltest/gcd; from L0 to L1
.var 1 is a I from L0 to L1
.var 2 is b I from L0 to L1
	iload_2
	iconst_0
	if_icmpeq L2
	iconst_0
	goto L3
L2:
	iconst_1
L3:
	ifeq L5
	iload_1
	ireturn
	goto L6
L5:
	aload_0
	iload_2
	iload_1
	iload_1
	iload_2
	idiv
	iload_2
	imul
	isub
	invokevirtual test/gcd/gcd(II)I
	ireturn
L6:
L1:
	nop
	
	; set limits used by this method
.limit locals 3
.limit stack 6
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest/gcd; from L0 to L1
	new test/gcd
	dup
	invokenonvirtual test/gcd/<init>()V
	astore_1
	aload_1
	getstatic test/gcd/i I
	getstatic test/gcd/j I
	invokevirtual test/gcd/gcd(II)I
	invokestatic VC/lang/System/putIntLn(I)V
	invokestatic VC/lang/System.getInt()I
	putstatic test/gcd/i I
	invokestatic VC/lang/System.getInt()I
	putstatic test/gcd/j I
	aload_1
	getstatic test/gcd/i I
	getstatic test/gcd/j I
	invokevirtual test/gcd/gcd(II)I
	invokestatic VC/lang/System/putIntLn(I)V
L1:
	return
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 3
.end method
