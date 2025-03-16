package main.zenit.zencodearea;

public class Context {
    private String currentPackage;
    private String currentClass;

    public Context(String currentPackage, String currentClass) {
        this.currentPackage = currentPackage;
        this.currentClass = currentClass;
    }

    public String getCurrentPackage() {
        return currentPackage;
    }

    public String getCurrentClass() {
        return currentClass;
    }

    public void setCurrentPackage(String currentPackage) {
        this.currentPackage = currentPackage;
    }

    public void setCurrentClass(String currentClass) {
        this.currentClass = currentClass;
    }
}
