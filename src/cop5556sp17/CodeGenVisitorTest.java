package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Program;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class CodeGenVisitorTest {

    static final boolean doPrint = true;
    boolean devel = false;
    boolean grade = true;
    private PrintStream stdout;
    private ByteArrayOutputStream outputStream;

    static void show(Object s) {
        if (doPrint) {
            System.out.println(s);
        }
    }

    private void assertProgramValidity(String inputCode, String[] args, String expectedOutput) throws Exception {
        Scanner scanner = new Scanner(inputCode);
        scanner.scan();
        Parser parser = new Parser(scanner);
        //System.out.println("Enter");
        Program program = (Program) parser.parse();
        TypeCheckVisitor v = new TypeCheckVisitor();
        program.visit(v, null);

        CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
        //System.out.println("Before visitor");
        byte[] bytecode = (byte[]) program.visit(cv, null);

        String programName = program.getName();

//        String classFileName = "bin/" + programName + ".class";
//        OutputStream output = new FileOutputStream(classFileName);
//        output.write(bytecode);
//        output.close();
        //System.out.println(programName+"\n");
        Runnable instance = CodeGenUtils.getInstance(programName, bytecode, args);
        instance.run();

        String actualOutput = outputStream.toString().trim();

        assertEquals("Invalid Output\n -------------------- \n" + inputCode + "\n --------------------", expectedOutput, actualOutput);
    }

    @Before
    public void setUp() throws Exception {
        stdout = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(stdout);
    }

    @Test
    public void emptyProg() throws Exception {
        //scan, parse, and type check the program
        String progname = "emptyProg";
        String input = progname + "  {}";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode program = parser.parse();
        TypeCheckVisitor v = new TypeCheckVisitor();
        program.visit(v, null);
        show(program);

        //generate code
        CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
        byte[] bytecode = (byte[]) program.visit(cv, null);

        //output the generated bytecode
        CodeGenUtils.dumpBytecode(bytecode);

        //write byte code to file
        String name = ((Program) program).getName();
        String classFileName = "bin/" + name + ".class";
        OutputStream output = new FileOutputStream(classFileName);
        output.write(bytecode);
        output.close();
        System.out.println("wrote classfile to " + classFileName);

        // directly execute bytecode
        String[] args = new String[0]; //create command line argument array to initialize params, none in this case
        Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
        instance.run();
    }

    @Test
    public void testEmptyProgWithAssertions() throws Exception {
        String inputCode = "emptyProg {}";
        String[] args = new String[0];

        assertProgramValidity(inputCode, args, "");
    }

    @Test
    public void testParamDecProg0() throws Exception {
        String[] input = new String[]{
                "paramDecProg0 integer b, boolean c {",
                "c <- c;",
                "b <- b;",
                "b <- 2;",
                "c <- true;",
                "c <- false;",
                "b <- 3;",
                "}"
        };
        String inputCode = String.join("\n", input);
        String[] args = new String[]{"1", "false"};

        assertProgramValidity(inputCode, args, "false12truefalse3");
    }

    @Test
    public void testdecProg0() throws Exception {
        String[] input = new String[]{
                "decProg0 integer a {",
                "integer b boolean c boolean d",
                "a <- 3;",
                "b <- 4;",
                "c <- false;",
                "b <- 5;",
                "d <- true;",
                "a <- 6;",
                "}"
        };
        String inputCode = String.join("\n", input);
        String[] args = new String[]{"1"};

        assertProgramValidity(inputCode, args, "34false5true6");
    }

    @Test
    public void testIfStatementProg0() throws Exception {
        String[] input = new String[]{
                "ifStatementProg0 integer a {",
                "boolean b",
                "b <- true;",
                "if(b) { a <- 4; }",
                "b <- false;",
                "if(b) { a <- 5; }",
                "b <- true;",
                "if(b) { a <- 6; }",
                "}"
        };
        String inputCode = String.join("\n", input);
        String[] args = new String[]{"0"};

        assertProgramValidity(inputCode, args, "true4falsetrue6");
    }

    @Test
    public void testIfStatementProg1() throws Exception {
        String[] input = new String[]{
                "ifStatementProg1 integer a {",
                "if(2 != 3) { a <- 1; }",
                "if(2 == 3) { a <- 2; }",
                "if(5 != 5) { a <- 3; }",
                "if(5 == 5) { a <- 4; }",
                "if(false == false) { a <- 5; }",
                "if(false != false) { a <- 6; }",
                "if(true == false) { a <- 7; }",
                "if(true != false) { a <- 8; }",
                "if(true != true) { a <- 9; }",
                "if(true == true) { a <- 10; }",
                "}"
        };
        String inputCode = String.join("\n", input);
        String[] args = new String[]{"0"};

        assertProgramValidity(inputCode, args, "145810");
    }

    @Test
    public void testIfStatementProg2() throws Exception {
        String[] input = new String[]{
                "ifStatementProg2 {",
                "integer a",
                "if(5 == 5) { a <- 4; }",
                "if(false < false) { a <- 5; }",
                "if(false <= false) { a <- 6; }",
                "if(true <= false) { a <- 7; }",
                "if(true >= false) { a <- 8; }",
                "if(true >= true) { a <- 9; }",
                "if(true > false) { a <- 10; }",
                "}"
        };
        String inputCode = String.join("\n", input);
        String[] args = new String[0];

        assertProgramValidity(inputCode, args, "468910");
    }

    @Test
    public void testWhileStatementProg0() throws Exception {
        String[] input = new String[]{
                "whileStatementProg0 {",
                "integer a boolean b",
                "a <- 1;",
                "while(a <= 3) {a <- a + 1;}",
                "while(a > 1) {a <- a - 1;}",
                "while(a < 4) {a <- a * 2;}",
                "while(a >= 2) {a <- a / 2;}",
                "b <- false;",
                "while(b) { b <- true; }",
                "b <- true;",
                "while(b) { b <- false; }",
                "}"
        };
        String inputCode = String.join("\n", input);
        String[] args = new String[0];

        assertProgramValidity(inputCode, args, "12343212421falsetruefalse");
    }

    @Test
    public void testWhileStatementProg1() throws Exception {
        String[] input = new String[]{
                "whileStatementProg1 {",
                "integer a boolean b integer c boolean d",
                "a <- 1;",
                "b <- true;",
                "while(a < 3){ if(b){ c <- 7; while(c <= 8){ boolean a a <-false; c <- c +1; } } a <- a+1; if(2+3 == 1+4) {d <- false;} }",
                "}"
        };
        String inputCode = String.join("\n", input);
        String[] args = new String[0];

        assertProgramValidity(inputCode, args, "1true7false8false92false7false8false93false");
    }
}
