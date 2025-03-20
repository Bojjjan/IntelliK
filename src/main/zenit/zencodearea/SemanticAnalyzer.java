package main.zenit.zencodearea;

import generated.JavaLexer;
import generated.JavaParser;
import generated.JavaParserBaseListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import main.zenit.zencodearea.Symbol;
import main.zenit.zencodearea.Context;

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

    private final CommonTokenStream tokenStream;

    /**
     * Called when a class declaration is encountered during the parse process.
     * <p>
     * This method extracts the class name and checks if it contains a main method.
     * If a class has a main method, it is marked as a runnable class ({@link ClassContext.JavaClassType#RUNNABLE}).
     * </p>
     *
     * @author Philip Boyde
     */

    public SemanticAnalyzer(CommonTokenStream tokenStream) {
        this.tokenStream = tokenStream;
    }

    /**
     * Determines if the class contains a main method.
     *
     * @author Philip Boyde
     * @param ctx The context object containing the class declaration.
     * @return {@code true} if the class contains the main method, {@code false} otherwise.
     */

    private boolean consistOfMainMethod(JavaParser.ClassDeclarationContext ctx) {
        if (ctx.classBody() == null) {
            return false;
        }
        for (JavaParser.ClassBodyDeclarationContext member : ctx.classBody().classBodyDeclaration()) {
            if (member.memberDeclaration() != null && member.memberDeclaration().methodDeclaration() != null) {
                JavaParser.MethodDeclarationContext methodCtx = member.memberDeclaration().methodDeclaration();

                if (!"main".equals(methodCtx.identifier().getText())) {
                    continue;
                }

                if (methodCtx.typeTypeOrVoid() == null || !"void".equals(methodCtx.typeTypeOrVoid().getText())) {
                    continue;
                }

                if (methodCtx.formalParameters() == null) {
                    continue;
                }

                JavaParser.FormalParameterListContext paramListCtx = methodCtx.formalParameters().formalParameterList();
                if (paramListCtx == null) {
                    continue;
                }

                String paramText = paramListCtx.getText();
                if (!paramText.contains("String") || !paramText.contains("[")) {
                    continue;
                }

                boolean isPublic = false;
                boolean isStatic = false;
                if (member.modifier() != null) {
                    for (JavaParser.ModifierContext modCtx : member.modifier()) {
                        String modText = modCtx.getText();
                        if ("public".equals(modText)) {
                            isPublic = true;
                        }
                        if ("static".equals(modText)) {
                            isStatic = true;
                        }
                    }
                }

                if (isPublic && isStatic) {
                    return true;
                }
            }
        }
        return false;
    }

    private String extractModifiers(ParserRuleContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode tn) {
                int tokenType = tn.getSymbol().getType();
                if (tokenType == JavaLexer.PUBLIC ||
                        tokenType == JavaLexer.PROTECTED ||
                        tokenType == JavaLexer.PRIVATE) {
                    return tn.getText();
                }
            } else if (child instanceof ParserRuleContext) {
                String mod = extractModifiers((ParserRuleContext) child);
                if (!mod.equals("default")) {
                    return mod;
                }
            }
        }
        return "default";
    }

    private String extractModifiersFromContext(ParserRuleContext ctx, CommonTokenStream tokenStream) {
        int startIndex = ctx.getStart().getTokenIndex();
        int stopIndex = ctx.getStop().getTokenIndex();
        List<Token> tokens = tokenStream.getTokens(startIndex, stopIndex);
        for (Token token : tokens) {
            String text = token.getText();
            if ("public".equals(text) || "protected".equals(text) || "private".equals(text)) {
                return text;
            }
        }
        if (ctx.getParent() != null) {
            return extractModifiersFromContext(ctx.getParent(), tokenStream);
        }
        return "default";
    }

    @Override
    public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        if(ProjectController.getInstance().getClassContext(ctx.identifier().getText()) != null){
            String modifier = extractModifiers(ctx);
            String packageName = ProjectController.getInstance().getCurrentPackage();
            Symbol symbol = new Symbol(ctx.identifier().getText(), Symbol.Type.CLASS_REFERENCE, modifier, null
            , packageName, ProjectController.getInstance().getCurrentClass());
            ProjectController.getInstance().getCurrentClass().addSymbol(symbol);
        }else{
            ClassContext ct = createClassContext(ctx);
            if (consistOfMainMethod(ctx)){
                ct.setType(ClassContext.JavaClassType.RUNNABLE);
            }else{
                ct.setType(ClassContext.JavaClassType.BASIC);
            }
            ct.setStyle(computeStyle(ct));
            ProjectController.getInstance().addClass(ct);
        }
    }

    private ClassContext createClassContext(ParserRuleContext ctx) {
        if (ctx == null) {
            return null;
        }
        String currentClassName = switch (ctx) {
            case JavaParser.ClassDeclarationContext classDeclarationContext ->
                    classDeclarationContext.identifier().getText();
            case JavaParser.InterfaceDeclarationContext interfaceDeclarationContext ->
                    interfaceDeclarationContext.identifier().getText();
            case JavaParser.EnumDeclarationContext enumDeclarationContext ->
                    enumDeclarationContext.identifier().getText();
            default -> "Unknown";
        };

        String modifier = extractModifiers(ctx);
        String packageName = ProjectController.getInstance().getCurrentPackage();
        String superClass = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNode node) {
                if ("extends".equals(node.getText())) {
                    if (i + 1 < ctx.getChildCount()) {
                        superClass = ctx.getChild(i + 1).getText();
                    }
                    break;
                }
            }
        }

        return new ClassContext(currentClassName, packageName, modifier, superClass);
    }


    @Override
    public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        String pkgText = ctx.getText();
        pkgText = pkgText.replace("package", "").replace(";", "").trim();
        ProjectController.getInstance().setCurrentPackage(pkgText);
    }

    private String computeStyle(Symbol symbol) {
        switch (symbol.getSymbolType()){
            case FIELD -> {
                return "variable";
            }
            case METHOD_DECLARATION -> {
                return "method-name";
            }
            case METHOD_CALL -> {
                if(isAccessible(symbol)){
                    return "method-call";
                }return "no-access";
            }
            case CLASS_REFERENCE -> {
                if(isAccessible(symbol)){
                    return "class-name";
                }else{
                    return "no-access";
                }
            }
        } return "default";
    }

    private String computeStyle(ClassContext ct){
        return switch (ct.getType()){
            case ENUM -> "enum-name";
            case INTERFACE -> "interface-name";
            case RUNNABLE, BASIC -> "class-name";
        };
    }

    private static boolean isAccessible(Symbol symbol) {
        String currentClass = ProjectController.getInstance().getCurrentClass().getClassName();
        if(symbol.getSymbolType().equals(Symbol.Type.METHOD_CALL)){
            Symbol s = ProjectController.getInstance().getMethod(symbol.getSymbolName());
            if(s != null){
                switch (s.getSymbolModifier()){
                    case "public" , "default" -> {
                        return true;
                    }
                    case "protected" -> {
                        return s.getSymbolPackageName().equals(symbol.getSymbolPackageName());
                    }
                    case "private" -> {
                        return currentClass.equals(s.getSymbolDeclaringClass().getClassName());
                    }
                }
            }
        }else if(symbol.getSymbolType().equals(Symbol.Type.CLASS_REFERENCE)){
            ClassContext c = ProjectController.getInstance().getClassContext(symbol.getSymbolName());
            if(c != null){
                switch (c.getModifier()){
                    case "public" , "default" -> {
                        return true;
                    }
                    case "protected" -> {
                        return c.getPackageName().equals(symbol.getSymbolPackageName());
                    }
                    case "private" -> {
                        return currentClass.equals(c.getClassName());
                    }
                }
            }
        }
        return true;
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
        String modifier = extractModifiersFromContext(ctx, tokenStream);
        if(ProjectController.getInstance().getCurrentClass() != null){
            Symbol symbol;
            if(ctx.typeTypeOrVoid().getText().isEmpty()){
                symbol = new Symbol(methodName, Symbol.Type.METHOD_CALL, modifier, null,
                        ProjectController.getInstance().getCurrentClass().getPackageName()
                        , ProjectController.getInstance().getCurrentClass());
            }else{
                if(ctx.formalParameters() != null){
                    JavaParser.FormalParameterListContext paramListCtx = ctx.formalParameters().formalParameterList();
                    if(paramListCtx != null){
                        List<String> paramNames = new ArrayList<>();
                        List<String> paramTypes = new ArrayList<>();
                        for(JavaParser.FormalParameterContext paramCtx : paramListCtx.formalParameter()){
                            String paramType = paramCtx.typeType().getText();
                            String paramName = paramCtx.variableDeclaratorId().getText();
                            paramNames.add(paramName);
                            paramTypes.add(paramType);
                        }
                        symbol = new Symbol(methodName, Symbol.Type.METHOD_DECLARATION, modifier, null,
                                ProjectController.getInstance().getCurrentClass().getPackageName()
                                , ProjectController.getInstance().getCurrentClass(), paramNames, paramTypes);
                        if(ProjectController.getInstance().SymbolExists(symbol)){
                            return;
                        }
                    }else{
                        symbol = new Symbol(methodName, Symbol.Type.METHOD_DECLARATION, modifier, null,
                                ProjectController.getInstance().getCurrentClass().getPackageName()
                                , ProjectController.getInstance().getCurrentClass(), null, null);
                    }
                }else{
                    return;
                }
            }
            symbol.setStyle(computeStyle(symbol));
            ProjectController.getInstance().addSymbolToCurrentClass(symbol);
        }
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
        ClassContext ct = createClassContext(ctx);
        ct.setType(ClassContext.JavaClassType.INTERFACE);
        ct.setStyle(computeStyle(ct));
        ProjectController.getInstance().addClass(ct);
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
        String modifier = extractModifiers(ctx);
        String packageName = ProjectController.getInstance().getCurrentClass().getPackageName();
        ClassContext ct = new ClassContext(enumName, packageName, modifier, null);
        ct.setType(ClassContext.JavaClassType.ENUM);
        ct.setStyle(computeStyle(ct));
        ProjectController.getInstance().addClass(ct);
    }

    /**
     * Called when a variable declaration is encountered during the parse process.
     *
     * @author Philip Boyde
     * @param ctx The context object containing the details of the variable declaration.
     */

    @Override
    public void enterVariableDeclarator(JavaParser.VariableDeclaratorContext ctx) {
        String modifier = extractModifiersFromContext(ctx, tokenStream);
        String fieldName = ctx.variableDeclaratorId().getText();
        String pkg = ProjectController.getInstance().getCurrentPackage();
        String style = "variable";
        Symbol symbol = new Symbol(fieldName, Symbol.Type.FIELD, modifier, null, pkg
                , ProjectController.getInstance().getCurrentClass());
        symbol.setStyle(style);
        ProjectController.getInstance().getCurrentClass().addSymbol(symbol);
    }

    @Override
    public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        String modifier = extractModifiersFromContext(ctx, tokenStream);
        if (ctx.variableDeclarators() != null) {
            for (JavaParser.VariableDeclaratorContext varCtx : ctx.variableDeclarators().variableDeclarator()) {
                String varName = varCtx.variableDeclaratorId().getText();
                if(ProjectController.getInstance().getCurrentClass() != null){
                    Symbol symbol = new Symbol(varName, Symbol.Type.FIELD, modifier, null
                            , ProjectController.getInstance().getCurrentClass().getPackageName()
                            , ProjectController.getInstance().getCurrentClass());
                    symbol.setStyle(computeStyle(symbol));
                    ProjectController.getInstance().getCurrentClass().addSymbol(symbol);
                }
            }
        }
    }
}
