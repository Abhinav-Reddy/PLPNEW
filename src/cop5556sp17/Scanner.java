package cop5556sp17;

import java.util.ArrayList;
import java.util.Collections;


public class Scanner {
	/**
	 * Kind enum
	 */

	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"),
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"),
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"),
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"),
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"),
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="),
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"),
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"),
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"),
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"),
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"),
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}


	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}




	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;

		//returns the text of this Token
		public String getText() {
			//IMPLEMENTED THIS
			return chars.substring(pos, pos+length);
		}

		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			//IMPLEMENTED THIS
			int idx;
			idx = Collections.binarySearch(lineStartPos, pos);
			if(idx < 0){
				idx=-1*(idx+1)-1;
			}
			// pos = Collections.binarySearch();
			return new LinePos(idx, pos-lineStartPos.get(idx));
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/**
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 *
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			//IMPLEMENTED THIS
			return Integer.parseInt(chars.substring(pos, pos+length));
		}

		public boolean isKind(Kind k){
			return kind.equals(k);
		}

		public boolean isAmongKind(Kind... kinds) {
			// Added this method
			for (Kind arg:kinds){
				if (this.isKind(arg)){
					return true;
				}
			}
			return false;
		}

		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }


		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
	}



ArrayList<Integer> lineStartPos;

	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		lineStartPos = new ArrayList<Integer>();
	}

	public static enum State{
		START,
		IN_DIGIT,
		IN_IDENT,
		IN_COMMENT;
	}


	public int skipWhiteSpace(int pos){
		while(pos< chars.length() && chars.charAt(pos) != '\n' && Character.isWhitespace(chars.charAt(pos))){
			pos++;
		}
		return pos;
	}

	public Boolean checkForIntOverflow(int startPos, int pos){

		try {
			Integer.parseInt(chars.substring(startPos, pos));
			return false;
		} catch (Exception NumberFormatException) {
			// TODO: handle exception
			return true;
		}


	}

	public Boolean IsIdentifierStart(char ch){
		return ((ch >= 'A' && ch<= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '$' || ch == '_');
	}

	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 *
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNum,berException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0;
		//TODO IMPLEMENT THIS!!!!
		int length = chars.length();
		State state = State.START;
		int startPos = 0;
		lineStartPos.add(0);
		char ch;
		while (pos < length) {
			ch = chars.charAt(pos);

			switch (state) {
				case START: {
					pos = skipWhiteSpace(pos);
					if (pos >= length)
						break;
					else
						ch = chars.charAt(pos);
					startPos = pos;
					switch (ch) {
					case '&': {
						tokens.add(new Token(Kind.AND, startPos, 1));
						pos++;
						state = State.START;
					} break;
					case '\n': {

						//System.out.println(pos);
						//System.out.println("Inside new line");
						pos++;
						lineStartPos.add(pos);

					} break;
					case '/': {
						pos++;
						if (pos >= length || chars.charAt(pos) != '*'){
							tokens.add(new Token(Kind.DIV, startPos, 1));
							state = State.START;
						}
						else{
							state = State.IN_COMMENT;
							pos++;
						}
					} break;
					case '%': {
						tokens.add(new Token(Kind.MOD, startPos, 1));
						pos++;
						state = State.START;
					} break;
					case '+': {
						tokens.add(new Token(Kind.PLUS, startPos, 1));
						pos++;
						state = State.START;
					} break;
					case '*': {
						tokens.add(new Token(Kind.TIMES, startPos, 1));
						pos++;
						state = State.START;
					} break;
					case '!': {
						pos++;
						if (pos >= length || chars.charAt(pos) != '='){
							tokens.add(new Token(Kind.NOT, startPos, 1));
						}
						else{
							tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));
							pos++;
						}
						state = State.START;
					} break;
					case '|': {
						pos++;
						if (pos >= length - 1 || chars.charAt(pos) != '-' || chars.charAt(pos+1) != '>'){
							tokens.add(new Token(Kind.OR, startPos, 1));
						}
						else{
							tokens.add(new Token(Kind.BARARROW, startPos, 3));
							pos+=2;
						}
						state = State.START;
					} break;
					case '-': {
						pos++;
						if (pos >= length || chars.charAt(pos) != '>'){
							tokens.add(new Token(Kind.MINUS, startPos, 1));
						}
						else{
							tokens.add(new Token(Kind.ARROW, startPos, 2));
							pos++;
						}
						state = State.START;
					} break;
					case '<': {
						pos++;
						if (pos >= length || (chars.charAt(pos) != '=' && chars.charAt(pos) != '-')){
							tokens.add(new Token(Kind.LT, startPos, 1));
						}
						else{
							if (chars.charAt(pos) == '=')
								tokens.add(new Token(Kind.LE, startPos, 2));
							else
								tokens.add(new Token(Kind.ASSIGN, startPos, 2));
							pos++;
						}
						state = State.START;
					} break;
					case '>': {
						pos++;
						if (pos >= length || chars.charAt(pos) != '='){
							tokens.add(new Token(Kind.GT, startPos, 1));
						}
						else{
							tokens.add(new Token(Kind.GE, startPos, 2));
							pos++;
						}
						state = State.START;
					} break;
					case '=': {
						pos++;
						if (pos >= length){
							pos--;
							throw new IllegalCharException( "illegal char " +ch+" at pos "+pos);
						}
						else if (chars.charAt(pos) != '='){
							throw new IllegalCharException( "illegal char " +ch+" at pos "+pos);
						}
						else{
							tokens.add(new Token(Kind.EQUAL, startPos, 2));
							pos++;
						}
						state = State.START;
					} break;
					case ';':{
						pos++;
						state = State.START;
						tokens.add(new Token(Kind.SEMI, startPos, 1));
					} break;
					case ',':{
						pos++;
						state = State.START;
						tokens.add(new Token(Kind.COMMA, startPos, 1));
					} break;
					case '(':{
						pos++;
						state = State.START;
						tokens.add(new Token(Kind.LPAREN, startPos, 1));
					} break;
					case ')':{
						pos++;
						state = State.START;
						tokens.add(new Token(Kind.RPAREN, startPos, 1));
					} break;
					case '{':{
						pos++;
						state = State.START;
						tokens.add(new Token(Kind.LBRACE, startPos, 1));
					} break;
					case '}':{
						pos++;
						state = State.START;
						tokens.add(new Token(Kind.RBRACE, startPos, 1));
					} break;
					case '0': {
						tokens.add(new Token(Kind.INT_LIT,startPos, 1));
						pos++;
						state = State.START;
					} break;

					default: {
						if (Character.isDigit(ch)){
							state = State.IN_DIGIT;pos++;
						}else if (IsIdentifierStart(ch)) {
							state = State.IN_IDENT;pos++;
						}else {
							throw new IllegalCharException( "illegal char " +ch+" at pos "+pos);
							}
						}
					}
				}break;
				case IN_DIGIT:{
					if (Character.isDigit(ch)){
						state = State.IN_DIGIT;pos++;
					}else if (checkForIntOverflow(startPos, pos)) {
						throw new IllegalNumberException("Number out of range for int type "+chars.substring(startPos, pos));
					}
					else {
						tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos));
						state = State.START;
					}
				}break;
				case IN_IDENT:{
					if (IsIdentifierStart(ch) || Character.isDigit(ch)) {
							pos++;
						} else {
							int isReserved = 0;
							String str = chars.substring(startPos, pos);
							for (Kind tk : Kind.values()){
								//System.out.print(tk.getText()+" "+str+"  ");
								if (tk.getText().equals(str)){
									tokens.add(new Token(tk, startPos, pos - startPos));
									isReserved = 1;
									break;
								}
							}
							//System.out.print("\n");
							if (isReserved == 0)
								tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
							state = State.START;
						}
				}break;
				case IN_COMMENT:{
					pos++;
					if (pos < length && ch == '*' && chars.charAt(pos) == '/'){
						state = State.START;
						pos++;
					}
					else if (ch == '\n'){
						lineStartPos.add(pos);
					}
				}break;
				default:  assert false;
			}// switch(state)
		} // while

		switch (state) {
			case IN_DIGIT:{
				if (checkForIntOverflow(startPos, pos)){
					throw new IllegalNumberException("Number out of range for int type "+chars.substring(startPos, pos));
				}
				else{
					tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos));
				}
			}break;
			case IN_IDENT:{
				int isReserved = 0;
				String str = chars.substring(startPos, pos);
				for (Kind tk : Kind.values()){
					if (tk.getText().equals(str)){
						tokens.add(new Token(tk, startPos, pos - startPos));
						isReserved = 1;
					}
				}
				if (isReserved == 0)
					tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
			}break;
			default:
				break;
		}
		tokens.add(new Token(Kind.EOF, pos, 0));
		return this;
	}



	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}

	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);
	}



	/**
	 * Returns a LinePos object containing the line and position in line of the
	 * given token.
	 *
	 * Line numbers start counting at 0
	 *
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		// IMPLEMENTED THIS
		return t.getLinePos();
	}


}
