package cop5556sp17;

import java.util.ArrayList;


import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		int idx=0;
		for (ParamDec dec : params){
			dec.visit(this, idx);
			idx++;
		}
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		//System.out.println("Test");
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, 1);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);

//TODO  visit the local variables
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method


		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {

		binaryChain.getE0().visit(this, 0);

		if (binaryChain.getE0().getType().equals(URL)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
		}
		else if (binaryChain.getE0().getType().equals(TypeName.FILE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);;
		}

		if (binaryChain.getE1().firstToken.isAmongKind(OP_CONVOLVE, OP_BLUR, OP_GRAY)){
			if (binaryChain.getArrow().isKind(BARARROW))
				mv.visitInsn(DUP);
			else
				mv.visitInsn(ACONST_NULL);
		}

		binaryChain.getE1().visit(this, 1);


		if (binaryChain.getE1() instanceof IdentChain){

			if (binaryChain.getE1().getDec() instanceof ParamDec){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, binaryChain.getE1().getDec().getIdent().getText(),
						binaryChain.getE1().getDec().getType().getJVMTypeDesc());

			}
			else{
				if (binaryChain.getE1().getDec().getType()==TypeName.INTEGER) {
					mv.visitVarInsn(ILOAD, binaryChain.getE1().getDec().getSlot());
				} else {
					mv.visitVarInsn(ALOAD, binaryChain.getE1().getDec().getSlot());
				}
			}
		}

		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();
		Token token = binaryExpression.getOp();
		e0.visit(this, arg);
		e1.visit(this, arg);
		Label l1 = new Label();
		Label l3 = new Label();
		switch(token.kind){
		case LT:
			mv.visitJumpInsn(IF_ICMPLT, l1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, l3);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l3);
			//mv.visitInsn();
			break;
		case LE:
			mv.visitJumpInsn(IF_ICMPLE, l1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, l3);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l3);
			//mv.visitInsn();
			break;
		case GT:
			//mv.visitInsn();
			mv.visitJumpInsn(IF_ICMPGT, l1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, l3);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l3);
			break;
		case GE:
			mv.visitJumpInsn(IF_ICMPGE, l1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, l3);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l3);
			//mv.visitInsn();
			break;
		case EQUAL:
			//mv.visitInsn();
			mv.visitJumpInsn(IF_ICMPEQ, l1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, l3);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l3);
			break;
		case NOTEQUAL:
			mv.visitJumpInsn(IF_ICMPEQ, l1);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, l3);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l3);
			//mv.visitInsn();
			break;
		case PLUS:
			if (binaryExpression.getType() == TypeName.INTEGER) {
				mv.visitInsn(IADD);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
			}

			break;
		case MINUS:
			if (binaryExpression.getType() == TypeName.INTEGER) {
				mv.visitInsn(ISUB);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
			}
			break;
		case OR:
			mv.visitInsn(IOR);
			break;
		case TIMES:
			if (binaryExpression.getType() == TypeName.INTEGER) {
				mv.visitInsn(IMUL);
			}else if ((binaryExpression.getE0().getType() == TypeName.INTEGER) && (binaryExpression.getE1().getType() == TypeName.IMAGE)) {
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			}
			break;
		case DIV:
			if (binaryExpression.getType() == TypeName.INTEGER) {
				mv.visitInsn(IDIV);
			} else {
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
			}
			break;
		case AND:
			mv.visitInsn(IAND);
			break;
		case MOD:
			if (binaryExpression.getType() == TypeName.INTEGER) {
				mv.visitInsn(IREM);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
			}
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		int slotNo = (int)arg;
		int i,j;
		ArrayList<Dec> dec = block.getDecs();
		ArrayList<Statement>st = block.getStatements();
		Label startLabel = new Label();
		mv.visitLabel(startLabel);
		for (i=0, j=0; i<dec.size() && j<st.size();){

			if (dec.get(i).firstToken.pos < st.get(j).firstToken.pos){
				dec.get(i).visit(this, slotNo);
				i++;
				slotNo++;
			}
			else{
				if (st.get(j) instanceof BinaryChain){
					st.get(j).visit(this, 0);
					mv.visitInsn(POP);
				}
				else{
					st.get(j).visit(this, slotNo);
				}
				j++;
			}
		}

		for (; i<dec.size(); i++){
			dec.get(i).visit(this, slotNo);
			slotNo++;
		}

		for (; j<st.size(); j++){
			if (st.get(j) instanceof BinaryChain){
				st.get(j).visit(this, 0);
				mv.visitInsn(POP);
			}
			else{
				st.get(j).visit(this, slotNo);
			}
		}
		Label endLabel = new Label();
		mv.visitLabel(endLabel);

		for(i=0;i<dec.size();i++){
			mv.visitLocalVariable(dec.get(i).getIdent().getText(), dec.get(i).getType().getJVMTypeDesc(), null, startLabel, endLabel, dec.get(i).getSlot());
		}

		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		if (booleanLitExpression.getValue() == true)
			mv.visitInsn(ICONST_1);
		else
			mv.visitInsn(ICONST_0);
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		if (constantExpression.firstToken.isKind(Kind.KW_SCREENHEIGHT)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight",
					PLPRuntimeFrame.getScreenHeightSig, false);
		} else if (constantExpression.firstToken.isKind(Kind.KW_SCREENWIDTH)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth",
					PLPRuntimeFrame.getScreenWidthSig, false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this

		declaration.setSlot((int)arg);
		if (declaration.getType() == TypeName.IMAGE || declaration.getType() == TypeName.FRAME){
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getSlot());
		}
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {


		if (filterOpChain.getFirstToken().kind == Kind.OP_BLUR){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
		}
		else if (filterOpChain.getFirstToken().kind == OP_CONVOLVE){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
		}
		else if (filterOpChain.getFirstToken().kind == Kind.OP_GRAY){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);

		switch (frameOpChain.firstToken.kind){
		case KW_HIDE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
		break;
		case KW_SHOW:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
		break;
		case KW_MOVE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
		break;
		case KW_XLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
		break;
		case KW_YLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
		break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {

		//arg is 0 for left side
		if ((int)arg == 0){
			if (identChain.getDec() instanceof ParamDec){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getFirstToken().getText(), identChain.getType().getJVMTypeDesc());
			}
			else{
				if (identChain.getDec().getType() == TypeName.INTEGER){
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlot());
				}
				else if (identChain.getDec().getType() == TypeName.IMAGE){
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
				}
				else if (identChain.getDec().getType() == TypeName.FRAME){
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
				}

			}
		}
		else{
			if (identChain.getDec() instanceof ParamDec){
				if (identChain.getDec().getType() == TypeName.INTEGER){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getFirstToken().getText(), identChain.getType().getJVMTypeDesc());
				}
				else if (identChain.getDec().getType() == TypeName.FILE){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getType().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
				}
			}
			else{
				if (identChain.getDec().getType() == TypeName.INTEGER){
					mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
				}
				else if (identChain.getDec().getType() == TypeName.IMAGE){
					mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
				}
				else if (identChain.getDec().getType() == FRAME){
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
					mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
				}
			}
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
		if (identExpression.getDec() instanceof ParamDec){
			mv.visitIntInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, identExpression.getFirstToken().getText(), identExpression.getType().getJVMTypeDesc());
		}
		else{
			if (identExpression.getType() == TypeName.INTEGER
					|| identExpression.getType() == TypeName.BOOLEAN) {
				mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
			} else {
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
			}
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		if (identX.getDec() instanceof ParamDec){
			mv.visitIntInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className, identX.getFirstToken().getText(), identX.getType().getJVMTypeDesc());
		}
		else{
			if (identX.getDec().getType() == TypeName.INTEGER || identX.getDec().getType() == TypeName.BOOLEAN) {
				mv.visitVarInsn(ISTORE, identX.getDec().getSlot());
			}
			else if (identX.getDec().getType() == IMAGE) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",
						PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
			}else {
				mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
			}
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
		Label l1 = new Label();
		ifStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFEQ, l1);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(l1);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);

		switch (imageOpChain.getFirstToken().kind) {
		case OP_WIDTH:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
		break;
		case OP_HEIGHT:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
		break;

		case KW_SCALE:
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
		break;

		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans

		if (paramDec.getType().getJVMTypeDesc().equals("I")){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "I", null, new Integer(0));
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH, (int)arg);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}
		else if(paramDec.getType().getJVMTypeDesc().equals("Z")){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Z", null, new Boolean(false)) ;
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(BIPUSH, (int)arg);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}
		else if (paramDec.getType().equals(TypeName.FILE)){
			cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), paramDec.getType().getJVMTypeDesc(), null, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(BIPUSH, (int)arg);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
		}

		else if (paramDec.getType().equals(TypeName.URL)){
			cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), paramDec.getType().getJVMTypeDesc(), null, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(BIPUSH, (int)arg);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
		}
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for (Expression expression : tuple.getExprList()){
			expression.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l1);
		mv.visitLabel(l2);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(l1);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, l2);
		return null;
	}

}
