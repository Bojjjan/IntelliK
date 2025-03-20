package zenit.zencodearea;

import generated.JavaLexer;
import generated.JavaParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ProjectController {

    private static ProjectController instance;
    private String currentPackage;
    private ClassContext currentClass = new ClassContext("", "", "", "");
    private final List<ClassContext> knownClasses = new ArrayList<>();

    ProjectController() {}

    public static ProjectController getInstance(){
        if(instance == null) instance = new ProjectController();
        return instance;
    }

    public void setCurrentClass(String className){
        for(ClassContext c : knownClasses){
            if(c.getClassName().equals(className)){
                currentClass = c;
            }
        }
    }

    public void setCurrentPackage(String currentPackage) { this.currentPackage = currentPackage;}

    public ClassContext getCurrentClass(){ return currentClass;}

    public ClassContext getClassContext(String className) {
        for(ClassContext currentClass : knownClasses){
            if(currentClass.getClassName().equals(className)) return currentClass;
        }
        return null;
    }

    public Symbol getMethod(String methodName){
        for(ClassContext currentClass : knownClasses){
            for(Symbol s : currentClass.getSymbols()){
                if(s.getSymbolName().equals(methodName) && s.getSymbolType().equals(Symbol.Type.METHOD_DECLARATION)){
                    return s;
                }
            }
        }return null;
    }

    public boolean SymbolExists(Symbol symbol){
        return currentClass.getSymbols().contains(symbol);
    }

    public Symbol getSymbol(String symbol) {
        for(Symbol sym : currentClass.getSymbols()){
            if(sym.getSymbolName().equals(symbol)) return sym;
        }return null;
    }

    public void addSymbolToCurrentClass(Symbol symbol){
        for(Symbol sym : currentClass.getSymbols()){
            if(sym.getSymbolName().equals(symbol.getSymbolName())
                    && sym.getSymbolDeclaringClassName().equals(symbol.getSymbolDeclaringClassName())
                        && (sym.getSymbolType().equals(symbol.getSymbolType()))){
                return;
            }
        }
        currentClass.addSymbol(symbol);
    }

    public String getCurrentPackage() { return currentPackage;}

    public void buildProjectHierarchy(String sourcePath) {
        knownClasses.clear();
        Path path = Path.of(sourcePath);
        try (Stream<Path> stream = Files.walk(path)) {
            stream.filter(file -> Files.isRegularFile(file) && file.toString().endsWith(".java"))
                    .forEach(this::parseAndAddSymbols);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void parseAndAddSymbols(Path file) {
        try {
            String content = Files.readString(file);
            CharStream input = CharStreams.fromString(content);
            JavaLexer lexer = new JavaLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            ParseTree tree = parser.compilationUnit();
            SemanticAnalyzer analyzer = new SemanticAnalyzer(tokens);
            ParseTreeWalker.DEFAULT.walk(analyzer, tree);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addClass(ClassContext classContext) {
        if (!(knownClasses.contains(classContext))){
            knownClasses.add(classContext);
        }
    }
}
