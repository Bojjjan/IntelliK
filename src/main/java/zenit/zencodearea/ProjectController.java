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
import java.util.*;
import java.util.stream.Stream;

public class ProjectController {

    private static ProjectController instance;
    private List<Path> sourcePaths;
    private final Map<String, Symbol> symbolTable = new HashMap<>();

    private ProjectController(){}

    public ProjectController(String sourcePath) {
        sourcePaths = new ArrayList<>();
        sourcePaths.add(Path.of(sourcePath));
    }

    public void addSourcePath(String path){
        sourcePaths.add(Path.of(path));
        buildProjectHierarchy();
    }

    private void buildProjectHierarchy() {
        symbolTable.clear();
        if(sourcePaths == null) return;
        for(Path sourcePath : sourcePaths) {
            try(Stream<Path> stream = Files.walk(sourcePath)) {
                stream.filter(file -> Files.isRegularFile(file) && file.toString().endsWith(".java"))
                        .forEach(this::parseAndAddSymbols);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void parseAndAddSymbols(Path file) {
        try {
            String content = Files.readString(file);
            CharStream input = CharStreams.fromString(content);
            JavaLexer lexer = new JavaLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            SemanticAnalyzer analyzer = new SemanticAnalyzer(tokens);
            ParseTree tree = parser.compilationUnit();
            ParseTreeWalker.DEFAULT.walk(analyzer, tree);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Symbol getSymbol(String className){
        return symbolTable.get(className);
    }

    public List<Symbol> getSubClasses(String className) {
        List<Symbol> result = new ArrayList<>();
        for(Symbol symbol : symbolTable.values()){
            if (className.equals(symbol.getSymbolSuperClass())) {
                result.add(symbol);
            }
        }
        return result;
    }


    public String getSuperClass(String className){
        Symbol s = getSymbol(className);
        return s != null ? s.getSymbolSuperClass() : null;
    }

    public Collection<Symbol> getAllSymbols(){
        return symbolTable.values();
    }

    public static ProjectController getInstance(){
        if(instance == null) instance = new ProjectController();
        return instance;
    }

    public void addSymbol(Symbol symbol) {
        symbolTable.put(symbol.getSymbolName(), symbol);
    }
}
