package zenit.zencodearea;

import generated.JavaLexer;
import generated.JavaParser;
import generated.JavaParserBaseListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class SemanticAnalyzer extends JavaParserBaseListener {
    private final Set<String> classNames = new HashSet<>();
    private final Set<String> methodNames = new HashSet<>();
    private final Set<String> variables = new HashSet<>();
    private final Context context = new Context("", "");
    private final CommonTokenStream tokenStream;

    public SemanticAnalyzer(CommonTokenStream tokenStream) {
        this.tokenStream = tokenStream;
    }

    private boolean hasClass = false;  // Tracks if a class has a main method
    private JavaClassType JClassType;

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
        String currentClassName = ctx.identifier().getText();
        context.setCurrentClass(currentClassName);
        String modifier = extractModifiers(ctx);
        String superClass = null;
        hasClass = false;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) != null && ctx.getChild(i) instanceof TerminalNode node) {
                if (node.getText().equals("extends")) {
                    if (i + 1 < ctx.getChildCount()) {
                        superClass = ctx.getChild(i + 1).getText();
                    }
                    break;
                }
            }
        }

        if (consistOfMainMethod(ctx)){
            hasClass = true;
            JClassType = JavaClassType.RUNNABLE;
        }

        ProjectController.getInstance().addSymbol(new Symbol(currentClassName, Symbol.Type.CLASS,
                modifier, superClass, context));
        classNames.add(currentClassName);
    }

    @Override
    public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        String fullText = ctx.getText();
        if(fullText.startsWith("package")){
            fullText = fullText.substring(7).trim();
            if(fullText.endsWith(";")){
                fullText = fullText.substring(0, fullText.length() - 1).trim();
            }
        }
        context.setCurrentPackage(fullText);
    }


    @Override
    public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        String methodName = ctx.identifier().getText();
        String modifier = extractModifiersFromContext(ctx, tokenStream);
        ProjectController.getInstance().addSymbol(new Symbol(methodName, Symbol.Type.METHOD, modifier,
                null, context));
        methodNames.add(methodName);
    }

    @Override
    public void enterVariableDeclarator(JavaParser.VariableDeclaratorContext ctx) {
        String varName = ctx.variableDeclaratorId().getText();
        variables.add(varName);
    }

    @Override
    public void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
        String interfaceName = ctx.identifier().getText();
        if (!hasClass) {
            hasClass = true;
            JClassType = JavaClassType.INTERFACE;
        }
        classNames.add(interfaceName);
    }

    @Override
    public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
        String enumName = ctx.identifier().getText();
        if (!hasClass) {
            hasClass = true;
            JClassType = JavaClassType.ENUM;
        }
        classNames.add(enumName);
    }

    @Override
    public void enterCatchClause(JavaParser.CatchClauseContext ctx) {
        String exceptionVar = ctx.identifier().getText();
        variables.add(exceptionVar);
    }

    @Override
    public void enterLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
        if (ctx.typeType() != null) {
            String typeName = ctx.typeType().getText();
            if (!isPrimitive(typeName)) {
                classNames.add(typeName);
            }
        }
    }

    private boolean isPrimitive(String typeName) {
        return typeName.equals("int") || typeName.equals("boolean") ||
                typeName.equals("float") || typeName.equals("double") ||
                typeName.equals("long") || typeName.equals("short") ||
                typeName.equals("byte") || typeName.equals("char");
    }

    @Override
    public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        if(ctx.typeType() != null){
            String typeName = ctx.typeType().getText();
            classNames.add(typeName);
        }
        String modifier = extractModifiersFromContext(ctx, tokenStream);
        if (ctx.variableDeclarators() != null) {
            for (JavaParser.VariableDeclaratorContext varCtx : ctx.variableDeclarators().variableDeclarator()) {
                String varName = varCtx.variableDeclaratorId().getText();
                ProjectController.getInstance().addSymbol(new Symbol(varName, Symbol.Type.FIELD, modifier,
                         null, context));
                variables.add(varName);
            }
        }
    }

    @Override
    public void enterMemberReferenceExpression(JavaParser.MemberReferenceExpressionContext ctx) {
        if (ctx.identifier() != null && ctx.identifier().getText() != null) {
            String fieldName = ctx.identifier().getText();
            variables.add(fieldName);
        }
    }

    public Set<String> getClassNames() { return classNames; }

    public Set<String> getMethodNames() {
        return methodNames;
    }

    public Set<String> getVariables() { return variables; }

    public JavaClassType getClassType() {return JClassType;}

    public Context getContext() {return context;}
}
