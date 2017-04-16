
/**
 * Important to test the error cases in case the
 * AST is not being completely traversed.
 * <p>
 * Only need to test syntactically correct programs, or
 * program fragments.
 */

package cop5556sp17;

import cop5556sp17.AST.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TypeCheckVisitorTest {


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // Program
    
    @Test
    public void Abhinavtest() throws Exception {
        String input = "p file p {boolean y \nx <- true;}";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode ast = parser.parse();
        assertEquals(Program.class, ast.getClass());
        Program program = (Program) ast;

        TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor();
        thrown.expect(TypeCheckVisitor.TypeCheckException.class);
        program.visit(typeCheckVisitor, null);
    }
    
    // FrameOpChain
    @Test
    public void testFrameOpChain0() throws Exception {
        String input =   "p url y {boolean i  y->i;}";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode ast = parser.parse();
        assertEquals(Program.class, ast.getClass());
        Program program = (Program) ast;
        
        TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor();
        program.visit(typeCheckVisitor, null);

    }
  
}