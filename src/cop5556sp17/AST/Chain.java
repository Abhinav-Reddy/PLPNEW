package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {

	private TypeName type;
	private Dec dec;

	public Chain(Token firstToken) {
		super(firstToken);
		type = null;
	}

	public void setType(TypeName t){
		type = t;
	}

	public TypeName getType(){
		return type;
	}

	public void setDec(Dec d) {
		dec = d;
	}

	public Dec getDec() {
		return dec;
	}

}
