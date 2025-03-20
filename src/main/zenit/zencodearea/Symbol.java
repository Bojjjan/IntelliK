package main.zenit.zencodearea;

import java.util.ArrayList;
import java.util.List;

public class Symbol {

    public enum Type{
        CLASS_REFERENCE, METHOD_DECLARATION, METHOD_CALL, FIELD
    }
    private final String name;
    private final Type type;
    private final String modifier;
    private final String packageName;
    private final ClassContext declaringClass;
    private final String superClass;
    private final List<Parameter> parameters = new ArrayList<>();
    private String style;

    public Symbol(String name, Type type, String modifier, String superClass, String packageName
            , ClassContext declaringClass) {
        this.name = name;
        this.type = type;
        this.modifier = modifier;
        this.packageName = packageName;
        this.declaringClass = declaringClass;
        this.superClass = superClass;
        style = null;
    }

    public Symbol(String name, Type type, String modifier, String packageName, String superClass
    , ClassContext declaringClass, List<String> paramNames, List<String> paramTypes){
        this.name = name;
        this.type = type;
        this.modifier = modifier;
        this.packageName = packageName;
        this.declaringClass = declaringClass;
        this.superClass = superClass;
        style = null;
        addParams(paramNames, paramTypes);
    }

    private void addParams(List<String> paramNames, List<String> paramTypes){
        if(paramNames != null && paramTypes != null){
            for(int i = 0; i < paramNames.size(); i++){
                String paramName = paramNames.get(i);
                String paramType = paramTypes.get(i);
                parameters.add(new Parameter(paramName, paramType));
            }
        }
    }

    public void setStyle(String style) { this.style = style;}
    public String getStyle() { return style;}
    public String getSymbolName() { return name;}
    public Type getSymbolType() { return type;}
    public String getSymbolModifier() { return modifier;}
    public String getSymbolPackageName() { return packageName;}
    public ClassContext getSymbolDeclaringClass() { return declaringClass;}
    public String getSymbolDeclaringClassName() { return declaringClass.getClassName();}
    public String getSymbolSuperClass() { return superClass;}

    @Override
    public String toString() {
        return "Symbol{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", modifier='" + modifier + '\'' +
                ", package='" + packageName + '\'' +
                ", declaringClass='" + declaringClass + '\'' +
                ", superClass='" + superClass + '\'' +
                ", style='" + style + '\'' +
                '}';
    }

    private static class Parameter {

        private String paramName;
        private String paramType;

        public Parameter(String paramName, String paramType) {
            this.paramName = paramName;
            this.paramType = paramType;
        }

        public String getParamName() { return paramName;}
        public String getParamType() { return paramType;}
    }
}