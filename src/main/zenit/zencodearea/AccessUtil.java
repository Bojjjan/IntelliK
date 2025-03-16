package main.zenit.zencodearea;
import main.zenit.zencodearea.Symbol;

public class AccessUtil {

    public static boolean isAccessible(Symbol symbol, Context context){
        String modifier = symbol.getSymbolModifier();
        return switch (modifier) {
            case "default" -> context.getCurrentPackage().equals(symbol.getSymbolPackageName());
            case "public" -> true;
            case "protected" -> context.getCurrentPackage().equals(symbol.getSymbolPackageName()) ||
                    isSubclass(context.getCurrentClass(), symbol.getSymbolDeclaringClass());
            case "private" -> context.getCurrentClass().equals(symbol.getSymbolDeclaringClass());
            default -> false;
        };
    }

    private static boolean isSubclass(String currentClass, String declaringClass){
        ProjectController pc = ProjectController.getInstance();
        String superClass = pc.getSuperClass(currentClass);
        while(superClass != null){
            if(superClass.equals(declaringClass)){
                return true;
            }
            superClass = pc.getSuperClass(superClass);
        }
        return false;
    }
}
