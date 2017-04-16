package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	
	private TypeName type;
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

}
