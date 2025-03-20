package main.zenit.zencodearea;

import java.util.ArrayList;
import java.util.List;

public class ClassContext {

    private final String className;
    private final String packageName;
    private final String modifier;
    private final String superClass;
    private final List<Symbol> symbols;
    private String style;
    private JavaClassType type;

    public enum JavaClassType {
        BASIC,
        RUNNABLE,
        INTERFACE,
        ENUM
    }

    public ClassContext(String className, String packageName, String modifier, String superClass) {
        this.className = className;
        this.packageName = packageName;
        this.modifier = modifier;
        this.superClass = superClass;
        this.symbols = new ArrayList<>();
        style = null;
    }

    public void addSymbol(Symbol symbol) {
        symbols.add(symbol);
    }

    public void setType(JavaClassType type) {
        switch (type) {
            case BASIC:
                this.type = JavaClassType.BASIC;
                break;
            case RUNNABLE:
                this.type = JavaClassType.RUNNABLE;
                break;
            case INTERFACE:
                this.type = JavaClassType.INTERFACE;
                break;
            case ENUM:
                this.type = JavaClassType.ENUM;
                break;
        }
    }

    public void setStyle(String style) { this.style = style;}
    public String getStyle() { return style;}
    public JavaClassType getType() { return type;}
    public String getClassName() { return className;}
    public List<Symbol> getSymbols() { return symbols;}
    public String getPackageName() { return packageName;}
    public String getModifier() { return modifier;}
    public String getSuperClass() { return superClass;}
}
