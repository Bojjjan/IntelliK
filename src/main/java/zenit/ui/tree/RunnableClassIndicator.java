package main.java.zenit.ui.tree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RunnableClassIndicator {

    // Huvudmetod för att visa vilka klasser som är körbara
    public static void main(String[] args) throws IOException {
        Path projectPath = Paths.get("src/main/java");

        List<Path> javaFiles = findJavaFiles(projectPath);

        // Kolla om de innehåller en main-metod
        for (Path file : javaFiles) {
            boolean hasMain = containsMainMethod(file);
            String status = hasMain ? "RUNNABLE" : "NOT RUNNABLE";
            System.out.println(file.getFileName() + ": " + status);
        }
    }

    // Hitta alla .java-filer
    private static List<Path> findJavaFiles(Path start) throws IOException {
        List<Path> javaFiles = new ArrayList<>();
        Files.walk(start)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(javaFiles::add);
        return javaFiles;
    }

    // Kontrollera om filen innehåller en main-metod
    public static boolean containsMainMethod(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        for (String line : lines) {
            if (line.contains("public static void main(String[] args)")) {
                return true;
            }
        }
        return false;
    }
}
