package cop5556sp17;

import java.util.ArrayList;


import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {
		ASTNode root = program();
		matchEOF();
		return root;
	}

	Expression expression() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("expression");
		Expression e0, e1;
		
		Token first = t;
		Token op;
		e0 = term();
		while(t.isAmongKind(LT, LE, GT, GE, EQUAL, NOTEQUAL)){
			op = t;
			match(LT, LE, GT, GE, EQUAL, NOTEQUAL);
			e1 = term();
			e0 = new BinaryExpression(first, e0, op, e1);
		}
		return e0;
	}

	Expression term() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("term");
		Expression e0, e1;
		Token first = t;
		Token op;
		e0 = elem();
		while(t.isAmongKind(PLUS, MINUS, OR)){
			op = t;
			match(PLUS, MINUS, OR);
			e1 = elem();
			e0 = new BinaryExpression(first, e0, op, e1);
		}
		return e0;
	}

	 Expression elem() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("elem");
		Expression e0, e1;
		Token first = t;
		Token op;
		e0 = factor();
		while(t.isAmongKind(TIMES, DIV, AND, MOD)){
			op = t;
			match(TIMES, DIV, AND, MOD);
			e1 = factor();
			e0 = new BinaryExpression(first, e0, op, e1);
		}
		return e0;
	}

	Expression factor() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("factor");
		Kind kind = t.kind;
		Token first = t;
		switch (kind) {
		case IDENT: {
			consume();
			return new IdentExpression(first);
		}
		case INT_LIT: {
			consume();
			return new IntLitExpression(first);
		}
		case KW_TRUE:
		case KW_FALSE: {
			consume();
			return new BooleanLitExpression(first);
		}
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			consume();
			return new ConstantExpression(first);
		}
		case LPAREN: {
			consume();
			Expression e0;
			e0 = expression();
			match(RPAREN);
			return e0;
		}
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor"+kind);
		}
	}

	Block block() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("block");
		Token first = t;
		match(LBRACE);
		ArrayList<Dec>decs = new ArrayList<Dec>();
		ArrayList<Statement>statements = new ArrayList<Statement>();
		
		while(t.isKind(RBRACE) == false){
			if (t.isAmongKind(KW_INTEGER, KW_BOOLEAN, KW_IMAGE, KW_FRAME)){
				decs.add(dec());
			}
			else{
				statements.add(statement());
			}
		}
		match(RBRACE);
		return new Block(first, decs, statements);
	}

	Program program() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("program");
		ArrayList<ParamDec>array = new ArrayList<>();
		Block b;
		//ParamDec par;
		Token first = t;
		match(IDENT);
		if (t.isKind(LBRACE)){
			b = block();
		}
		else{
			array.add(paramDec());
			while(t.isKind(COMMA)){
				consume();
				array.add(paramDec());
			}
			b = block();
		}
		
		return new Program(first, array, b);
	}

	ParamDec paramDec() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("paramDec");
		Token t1 = t; 
		match(KW_URL, KW_FILE, KW_INTEGER, KW_BOOLEAN);
		Token t2 = t;
		match(IDENT);
		return new ParamDec(t1, t2);
	}

	Dec dec() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("dec");
		Token first = t;
		match(KW_INTEGER, KW_BOOLEAN, KW_IMAGE, KW_FRAME);
		Token ident = t;
		match(IDENT);
		return new Dec(first, ident);
	}

	Statement statement() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("statement");
		Token first = t;
		Statement st;
		switch (t.kind) {
		case OP_SLEEP:
			consume();
			st =  new SleepStatement(first, expression());
			match(SEMI);	
			break;
		case KW_WHILE:
			st = whileStatement();
			break;
		case KW_IF:
			st = ifStatement();
			break;
		case IDENT:
			if (scanner.peek().isKind(ASSIGN)){
				st = assign();
			}
			else{
				st = chain();
			}
			match(SEMI);
			break;
		default:
			if (t.isAmongKind(OP_BLUR, OP_GRAY, OP_CONVOLVE, KW_SHOW, KW_HIDE, KW_MOVE, KW_XLOC, KW_YLOC, OP_WIDTH, OP_HEIGHT, KW_SCALE)){
				st = chain();
				match(SEMI);
			}
			else{
				throw new SyntaxException("illegal statement"+t.kind);
			}
		}
		return st;
	}

	
	WhileStatement whileStatement() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("whileStatement");
		Token first = t;
		match(KW_WHILE);
		match(LPAREN);
		Expression e0 = expression();
		match(RPAREN);
		Block b0 = block();
		return new WhileStatement(first, e0, b0);
	}

	IfStatement ifStatement() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("ifStatement");
		Token first = t;
		match(KW_IF);
		match(LPAREN);
		Expression e0 = expression();
		match(RPAREN);
		Block b0 = block();
		return new IfStatement(first, e0, b0);
	}

	AssignmentStatement assign() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("assign");
		Token first = t;
		match(IDENT);
		match(ASSIGN);
		Expression e0 = expression();
		return new AssignmentStatement(first, new IdentLValue(first), e0);
	}
	
	Chain chain() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("chain");
		Token first = t;
		Chain c1 = chainElem();
		Token op = t;
		match(ARROW, BARARROW);
		ChainElem c2 = chainElem();
		c1 = new BinaryChain(first, c1, op, c2);
		while(t.isAmongKind(ARROW, BARARROW)){
			op = t;
			match(ARROW, BARARROW);
			c2 = chainElem();
			c1 = new BinaryChain(first, c1, op, c2);
		}
		return c1;
	}

	ChainElem chainElem() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("chainElem");
		Token first = t;
		switch (t.kind) {
		case IDENT:
			consume();
			return new IdentChain(first);
		case OP_BLUR:
		case OP_GRAY:
		case OP_CONVOLVE:
			consume();
			return new FilterOpChain(first, arg());
		case KW_SHOW:
		case KW_HIDE:
		case KW_MOVE:
		case KW_XLOC:
		case KW_YLOC:
			consume();
			return new FrameOpChain(first, arg());
		case OP_WIDTH:
		case OP_HEIGHT:
		case KW_SCALE:
			consume();
			return new ImageOpChain(first, arg());
		default:
			throw new SyntaxException("illegal statement"+t.kind);
		}
		
	}

	Tuple arg() throws SyntaxException {
		//IMPLEMENTED THIS
		//System.out.println("arg");
		ArrayList< Expression>array = new ArrayList< Expression>();
		Token first = t;
		if (t.isKind(LPAREN)){
			consume();
			array.add(expression());
			while(t.isKind(COMMA)){
				consume();
				array.add(expression());
			}
			match(RPAREN);
		}
		
		return new Tuple(first, array);
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + " at "+t.getLinePos().toString()+" expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// IMPLEMENTED THIS
		for (Kind arg:kinds){
			if (t.isKind(arg)){
				return consume();
			}
		}
		throw new SyntaxException("saw " + t.kind + " at " + t.getLinePos().toString() +" expected " + kinds);
		
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
