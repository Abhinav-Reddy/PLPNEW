package cop5556sp17;

import java.util.*;
import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	class hashValue{
		public
			int scope;
			Dec dec;
			public hashValue() {
				scope = 0;
				dec = null;
			}
			
			public hashValue(int s, Dec d){
				scope = s;
				dec = d;
			}
			
	}
	
	//TODO  add fields
	ArrayList<Integer> stack;
	int next_scope, current_scope;
	HashMap< String, ArrayList<hashValue> >table;
	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		stack.add(new Integer(next_scope));
		current_scope = next_scope;
		next_scope++;
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		stack.remove(stack.size()-1);
		current_scope = stack.get(stack.size()-1);
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		ArrayList<hashValue> arr = table.get(ident);
		if (arr == null){
			arr = new ArrayList<hashValue>();
			table.put(ident, arr);
		}
		arr.add(new hashValue(current_scope, dec));
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		ArrayList<hashValue> arr = table.get(ident);
		if (arr == null){
			return null;
		}
		for(int j=stack.size() - 1; j >=0; j--){
			for(int i=0; i<arr.size(); i++){
				if (stack.get(j) == arr.get(i).scope){
					return arr.get(i).dec;
				}
			}
		}
		return null;
	}
	
	public boolean IsAlreadyDeclared(String ident){
		ArrayList<hashValue> arr = table.get(ident);
		if (arr == null){
			return false;
		}
		int j=stack.size() - 1;
		for(int i=0; i<arr.size(); i++){
			if (stack.get(j) == arr.get(i).scope){
				return true;
			}
		}
		return false;
	}
	
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		stack = new ArrayList<Integer>();
		table = new HashMap<>();
		stack.add(0);
		next_scope = 1;
		current_scope = 0;
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}
	
	


}