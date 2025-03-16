package zenit.zencodearea;

public class Symbol {

    public enum Type{
        CLASS, METHOD, FIELD
    }
    private final String name;
    private final Type type;
    private final String modifier;
    private final String packageName;
    private final String declaringClass;
    private final String superClass;

    public Symbol(String name, Type type, String modifier, String packageName, String declaringClass, String superClass) {
        this.name = name;
        this.type = type;
        this.modifier = modifier;
        this.packageName = packageName;
        this.declaringClass = declaringClass;
        this.superClass = superClass;
    }

    public String getSymbolName() { return name;}
    public Type getSymbolType() { return type;}
    public String getSymbolModifier() { return modifier;}
    public String getSymbolPackageName() { return packageName;}
    public String getSymbolDeclaringClass() { return declaringClass;}
    public String getSymbolSuperClass() { return superClass;}

    @Override
    public String toString() {
        return "Symbol{" +
                "name='" + name + '\'' +
                ", kind=" + type +
                ", modifier='" + modifier + '\'' +
                ", packageName='" + packageName + '\'' +
                ", declaringClass='" + declaringClass + '\'' +
                ", superClass='" + superClass + '\'' +
                '}';
    }
}