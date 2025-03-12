package main.zenit.zencodearea;

import generated.JavaParser;
import generated.JavaParserBaseListener;

import java.util.*;

/**
 * This class analyzes Java source code to collect semantic information during the parsing process.
 * It extends {@link JavaParserBaseListener} and overrides various methods to capture the names of
 * classes, methods, and variables, as well as to track the type of Java class (e.g., Runnable, Interface, Enum).
 * <p>
 * The class provides methods to access these semantic details after the parsing process is complete.
 * </p>
 * @author Philip Boyde
 */
public class SemanticAnalyzer extends JavaParserBaseListener {
    private final Set<String> classNames = new HashSet<>();
    private final Set<String> methodNames = new HashSet<>();
    private final Set<String> variables = new HashSet<>();

    private boolean hasClass = false;  // Tracks if a class has a main method
    private JavaClassType JclassType;

    /**
     * Called when a class declaration is encountered during the parse process.
     * <p>
     * This method extracts the class name and checks if it contains a main method.
     * If a class has a main method, it is marked as a runnable class ({@link JavaClassType#RUNNABLE}).
     * </p>
     *
     * @author Philip Boyde
     * @param ctx The context object containing the details of the class declaration.
     */
    @Override
    public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        String currentClassName = ctx.identifier().getText();
        hasClass = false;

        if (consistOfMainMethod(ctx)){
            hasClass = true;
            JclassType = JavaClassType.RUNNABLE;
        }
        classNames.add(currentClassName);
    }

    /**
     * Determines if the class contains a main method.
     *
     * @author Philip Boyde
     * @param ctx The context object containing the class declaration.
     * @return {@code true} if the class contains the main method, {@code false} otherwise.
     */
    private boolean consistOfMainMethod(JavaParser.ClassDeclarationContext ctx) {
        return ctx.getText().contains("publicstaticvoidmain(String[]args)"); //No space required
    }

    /**
     * Called when a method declaration is encountered during the parse process.
     *
     * @author Philip Boyde
     * @param ctx The context object containing the details of the method declaration.
     */
    @Override
    public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        String methodName = ctx.identifier().getText();
        if(Objects.equals(methodName, "int")){
            System.out.println("enterMethodCall = int");
        }
        methodNames.add(methodName);
    }

    /**
     * Called when a variable declaration is encountered during the parse process.
     *
     * @author Philip Boyde
     * @param ctx The context object containing the details of the variable declaration.
     */
    @Override
    public void enterVariableDeclarator(JavaParser.VariableDeclaratorContext ctx) {
        String varName = ctx.variableDeclaratorId().getText();
        variables.add(varName);
    }

    /**
     * Called when an interface declaration is encountered during the parse process.
     * If no class has been marked yet, it marks the current type as an interface.
     *
     * @author Philip Boyde
     * @param ctx The context object containing the details of the interface declaration.
     */
    @Override
    public void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
        String interfaceName = ctx.identifier().getText();

        if (!hasClass) {hasClass = true; JclassType = JavaClassType.INTERFACE;}
        classNames.add(interfaceName);
    }

    /**
     * Called when an enum declaration is encountered during the parse process.
     * If no class has been marked yet, it marks the current type as an enum.
     *
     * @author Philip Boyde
     * @param ctx The context object containing the details of the enum declaration.
     */
    @Override
    public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
        String enumName = ctx.identifier().getText();

        if (!hasClass) {hasClass = true; JclassType = JavaClassType.ENUM;}
        classNames.add(enumName);

    }

    /**
     * Called when a catch clause is encountered during the parse process.
     *
     * @author Philip Boyde
     * @param ctx The context object containing the details of the catch clause.
     */
    @Override
    public void enterCatchClause(JavaParser.CatchClauseContext ctx) {
        String exceptionVar = ctx.identifier().getText();
        variables.add(exceptionVar);
    }

    public void enterLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
        String className = ctx.getChild(0).getChild(0).getChild(0).getText();
        System.out.println("enterTypeIdentifier: " + className);
        classNames.add(className);
    }

    public void enterObjectCreationExpression(JavaParser.ObjectCreationExpressionContext ctx) {
        String objectCreation = ctx.getChild(1).getText();
        System.out.println("enterObjectCreationExpression: " + objectCreation);
        methodNames.add(objectCreation);
    }

    @Override
    public void enterMethodCall(JavaParser.MethodCallContext ctx) {
        String methodName = ctx.identifier().getText();
        methodNames.add(methodName);
    }
    @Override
    public void enterPrimaryExpression(JavaParser.PrimaryExpressionContext ctx) {

        if (ctx.getText() != null && ctx.getText().equals("System")) {
            String className = ctx.getText();
            classNames.add(className);
        }
    }
    @Override
    public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        String fieldName = ctx.variableDeclarators().getText();
        if (fieldName.contains("out")) {
            System.out.println("Declared field 'out' at line " + ctx.getStart().getLine());
        }
    }
    @Override
    public void enterMemberReferenceExpression(JavaParser.MemberReferenceExpressionContext ctx) {
        if (ctx.identifier() != null && ctx.identifier().getText() != null) {
            String fieldName = ctx.identifier().getText();
            variables.add(fieldName);
        }
    }

    /**
     * Retrieves the set of class names found during the parsing process.
     *
     * @author Philip Boyde
     * @return A {@link Set} of class names.
     */
    public Set<String> getClassNames() { return classNames; }

    /**
     * Retrieves the set of method names found during the parsing process.
     *
     * @author Philip Boyde
     * @return A {@link Set} of method names.
     */
    public Set<String> getMethodNames() {
        return methodNames;
    }

    /**
     * Retrieves the set of variable names found during the parsing process.
     *
     * @author Philip Boyde
     * @return A {@link Set} of variable names.
     */
    public Set<String> getVariables() { return variables; }


    /**
     * Retrieves the type of the Java class being parsed (e.g., Runnable, Interface, Enum).
     *
     * @author Philip Boyde
     * @return The {@link JavaClassType} of the parsed class.
     */
    public JavaClassType getClassType() {return JclassType;}
}
