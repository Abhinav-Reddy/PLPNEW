package cop5556sp17;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.AST.*;
import cop5556sp17.AST.Type.TypeName;

import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// Implemented this
		Token first = binaryChain.getFirstToken();
		Chain ch = binaryChain.getE0();
		ch.visit(this, arg);
		ChainElem ce = binaryChain.getE1();
		ce.visit(this, arg);
		Token t = binaryChain.getArrow();
		if (t.isKind(ARROW)){
			if (ch.getType().equals(URL) && ce.getType().equals(IMAGE)){
				binaryChain.setType(IMAGE);
			}
			else if (ch.getType().equals(FILE) && ce.getType().equals(IMAGE)){
				binaryChain.setType(IMAGE);
			}
			else if (ch.getType().equals(FRAME) &&
					ce.getFirstToken().isAmongKind(KW_XLOC, KW_YLOC)){
				binaryChain.setType(INTEGER);
			}
			else if (ch.getType().equals(FRAME) &&
					ce.getFirstToken().isAmongKind(KW_SHOW, KW_HIDE, KW_MOVE)){
				binaryChain.setType(FRAME);
			}
			else if (ch.getType().equals(IMAGE) &&
					ce.getFirstToken().isAmongKind(OP_WIDTH, OP_HEIGHT)){
						binaryChain.setType(INTEGER);
					}
			else if (ch.getType().equals(IMAGE) &&
					ce.getType().equals(FRAME)){
						binaryChain.setType(FRAME);
					}
			else if (ch.getType().equals(IMAGE) &&
					ce.getType().equals(FILE)){
						binaryChain.setType(NONE);
					}
			else if (ch.getType().equals(TypeName.IMAGE) &&
					ce.getFirstToken().isAmongKind(OP_GRAY, OP_BLUR, OP_CONVOLVE)){
						binaryChain.setType(IMAGE);
					}
			else if (ch.getType().equals(TypeName.IMAGE) &&
					ce.getFirstToken().isAmongKind(KW_SCALE)){
						binaryChain.setType(IMAGE);
					}
			else if (ch.getType().equals(TypeName.IMAGE) &&
					ce.getFirstToken().isKind(IDENT)){
						binaryChain.setType(IMAGE);
					}
			else{
				throw new TypeCheckException("Error");
			}
		}
		else if(t.isKind(BARARROW)){
			if (ch.getType().equals(TypeName.IMAGE) &&
				ce.getFirstToken().isAmongKind(OP_GRAY, OP_BLUR, OP_CONVOLVE)){
				binaryChain.setType(IMAGE);
			}
			else{
				throw new TypeCheckException("Error");
			}
		}
		else{
			throw new TypeCheckException("Error");
		}
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// Implemented this
		Expression e1, e2;
		e1 = binaryExpression.getE0();
		e2 = binaryExpression.getE1();
		e1.visit(this, arg);
		e2.visit(this, arg);
		Token op = binaryExpression.getOp();
		switch (op.kind) {
		case PLUS:
		case MINUS:
			if (e1.getType().equals(INTEGER) && e2.getType().equals(INTEGER))
				binaryExpression.setType(INTEGER);
			else if (e1.getType().equals(IMAGE) && e2.getType().equals(IMAGE))
				binaryExpression.setType(IMAGE);
			else {
				throw new TypeCheckException("Error");
			}
		break;

		case TIMES:
			if (e1.getType().equals(INTEGER) && e2.getType().equals(INTEGER))
				binaryExpression.setType(INTEGER);
			else if (e1.getType().equals(INTEGER) && e2.getType().equals(IMAGE))
				binaryExpression.setType(IMAGE);
			else if (e1.getType().equals(IMAGE) && e2.getType().equals(INTEGER))
				binaryExpression.setType(IMAGE);
			else
				throw new TypeCheckException("Error");
		break;

		case DIV:
			if (e1.getType().equals(INTEGER) && e2.getType().equals(INTEGER))
				binaryExpression.setType(INTEGER);
			else
				throw new TypeCheckException("Error");
		break;

		case LT:
		case GT:
		case LE:
		case GE:
			if (e1.getType().equals(INTEGER) && e2.getType().equals(INTEGER))
				binaryExpression.setType(BOOLEAN);
			else if (e1.getType().equals(BOOLEAN) && e2.getType().equals(BOOLEAN))
				binaryExpression.setType(BOOLEAN);
			else
				throw new TypeCheckException("Error");
		break;

		case EQUAL:
		case NOTEQUAL:
			if (e1.getType().equals(e2.getType()))
				binaryExpression.setType(BOOLEAN);
			else
				throw new TypeCheckException("Error");
		break;
		default:
			throw new TypeCheckException("Error");
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// Implemented this
		symtab.enterScope();
		int i,j;
		ArrayList<Dec> dec = block.getDecs();
		ArrayList<Statement>st = block.getStatements();
		for (i=0, j=0; i<dec.size() && j<st.size();){

			if (dec.get(i).firstToken.pos < st.get(j).firstToken.pos){
				dec.get(i).visit(this, arg);
				i++;
			}
			else{
				st.get(j).visit(this, arg);
				j++;
			}
		}

		for (; i<dec.size(); i++){
			dec.get(i).visit(this, arg);
		}

		for (; j<st.size(); j++){
			st.get(j).visit(this, arg);
		}
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// Implemented this
		booleanLitExpression.setType(BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// Implemented this
		if (filterOpChain.getArg().getExprList().size() != 0){
			throw new TypeCheckException("Error");
		}
		filterOpChain.setType(TypeName.IMAGE);
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// Implemented this
		Token t = frameOpChain.firstToken;
		Tuple tp = frameOpChain.getArg();
		if (t.isAmongKind(KW_SHOW, KW_HIDE)){
			if (tp.getExprList().size() != 0){
				throw new TypeCheckException("Error");
			}
			frameOpChain.setType(NONE);
		}
		else if (t.isAmongKind(KW_XLOC, KW_YLOC)){
			if (tp.getExprList().size() != 0){
				throw new TypeCheckException("Error");
			}
			frameOpChain.setType(TypeName.INTEGER);
		}
		else if(t.isKind(KW_MOVE)){
			if (tp.getExprList().size() != 2){
				throw new TypeCheckException("Error");
			}
			frameOpChain.setType(NONE);
		}
		else{
			throw new TypeCheckException("Error");
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// Implemented this
		//identChain.setType();
		Token first = identChain.getFirstToken();
		Dec d = symtab.lookup(first.getText());
		if (d == null){
			throw new TypeCheckException("Error");
		}
		identChain.setType(d.getType());
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// Implemented this
		Dec d = symtab.lookup(identExpression.getFirstToken().getText());
		if (d == null){
			throw new TypeCheckException("Error");
		}
		//System.out.println(d.getType());
		identExpression.setType( d.getType() );
		identExpression.setDec(d);
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// Implemented this
		Expression e = ifStatement.getE();
		e.visit(this, arg);
		if (!e.getType().equals( TypeName.BOOLEAN ) ){
			throw new TypeCheckException("Error");
		}
		Block b = ifStatement.getB();
		b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// Implemented this
		intLitExpression.setType(INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// Implemented this
		Expression e = sleepStatement.getE();
		e.visit(this, arg);
		if (!( e.getType().equals( TypeName.INTEGER) ) ){
			throw new TypeCheckException("Error");
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// Implemented this
		Expression e = whileStatement.getE();
		e.visit(this, arg);
		if (! (e.getType().equals( TypeName.BOOLEAN ) ) ){
			throw new TypeCheckException("Error");
		}
		Block b = whileStatement.getB();
		b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// Implemented this
		if (symtab.IsAlreadyDeclared(declaration.getIdent().getText())){
			throw new TypeCheckException("Error");
		}
		symtab.insert(declaration.getIdent().getText(), declaration);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// Implemented this
		ArrayList<ParamDec> arr = program.getParams();
		for (int i=0; i<arr.size(); i++){
			arr.get(i).visit(this, arg);
		}
		Block b = program.getB();
		b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// Implemented this
		IdentLValue id = assignStatement.getVar();
		id.visit(this, arg);
		Expression e = assignStatement.getE();
		e.visit(this, arg);
		if ( !(e.getType().equals( id.getType())) ){
			throw new TypeCheckException("Error");
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// Implemented this
		Dec d = symtab.lookup(identX.getText());
		if (d == null)
			throw new TypeCheckException("Error");
		else {
			identX.setDec(d);

		}
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// Implemented this
		if (symtab.IsAlreadyDeclared(paramDec.getIdent().getText())){
			throw new TypeCheckException("Error");
		}
		symtab.insert(paramDec.getIdent().getText(), paramDec);
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// Implemented this
		constantExpression.setType(INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// Implemented this
		Token t = imageOpChain.firstToken;
		Tuple tp = imageOpChain.getArg();
		if (t.isAmongKind(OP_WIDTH, OP_HEIGHT)){
			if (tp.getExprList().size() != 0){
				throw new TypeCheckException("Error");
			}
			imageOpChain.setType(TypeName.INTEGER);
		}
		else if (t.isKind(KW_SCALE)){
			if (tp.getExprList().size() != 1){
				throw new TypeCheckException("Error");
			}
			imageOpChain.setType(TypeName.IMAGE);
		}
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// Implemented this
		List<Expression> e = tuple.getExprList();
		for (int i=0;i<e.size();i++){
			e.get(i).visit(this, arg);
			if (!(e.get(i).getType().equals(INTEGER)))
				throw new TypeCheckException("Error");
		}
		return null;
	}
}