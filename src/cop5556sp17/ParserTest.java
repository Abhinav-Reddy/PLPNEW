package cop5556sp17;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;


public class ParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		System.out.println("\n\n\n");
		parser.factor();
	}

	@Test
	public void testArg() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,5) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		//System.out.println(scanner);
		Parser parser = new Parser(scanner);
		System.out.println("\n\n\n");
		parser.arg();
	}

	@Test
	public void testArgerror() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		/*
		for(int i=0;i<scanner.tokens.size();i++){
			System.out.println(scanner.tokens.get(i).getText());
			System.out.println(scanner.tokens.get(i).kind);
			System.out.println(scanner.tokens.get(i).getLinePos().toString());
		}
		*/
		Parser parser = new Parser(scanner);
		System.out.println("\n\n\n");
		thrown.expect(Parser.SyntaxException.class);
		parser.arg();
	}


	@Test
	public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "prog0 {}";
		Parser parser = new Parser(new Scanner(input).scan());
		System.out.println("\n\n\n");
		parser.parse();
	}
		
	@Test
	public void testAbhinav() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "sta integer a, url b, file f {while(true){x<-x*y*x*20;\nwhile(false){y<-x*y\n*xbc; sleep a > 10 + b * ((10*k)*(ac*false)*screenheight);}}integer x boolean b x -> gray |-> move (a * b, a); }";
		Parser parser = new Parser(new Scanner(input).scan());
		//thrown.expect(SyntaxException.class);
		parser.program();
	}
}
