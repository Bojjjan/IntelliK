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
    private final boolean accessible;

    public Symbol(String name, Type type, String modifier, String superClass, Context context) {
        this.name = name;
        this.type = type;
        this.modifier = modifier;
        this.packageName = context.getCurrentPackage();
        this.declaringClass = context.getCurrentClass();
        this.superClass = superClass;
        accessible = AccessUtil.isAccessible(this, context);
    }

    public String getSymbolName() { return name;}
    public Type getSymbolType() { return type;}
    public String getSymbolModifier() { return modifier;}
    public String getSymbolPackageName() { return packageName;}
    public String getSymbolDeclaringClass() { return declaringClass;}
    public String getSymbolSuperClass() { return superClass;}
    public boolean isAccessible() { return accessible;}

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