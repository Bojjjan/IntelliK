package main.zenit.ui.tree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class RunnableClassIndicatorTest {


    /**
     * FPI513
     * @param tempDir
     * @throws IOException
     */
    @Test
    public void testClassWithMainMethodIsRunnable(@TempDir Path tempDir) throws IOException {
        Path fileWithMain = tempDir.resolve("TestWithMain.java");
        Files.write(fileWithMain, "public class TestWithMain { public static void main(String[] args) { System.out.println(\"Hello World\"); } }".getBytes());

        Path fileWithoutMain = tempDir.resolve("TestWithoutMain.java");
        Files.write(fileWithoutMain, "public class TestWithoutMain { }".getBytes());

        boolean hasMain1 = RunnableClassIndicator.containsMainMethod(fileWithMain);
        boolean hasMain2 = RunnableClassIndicator.containsMainMethod(fileWithoutMain);

        assertTrue(hasMain1);
        assertFalse(hasMain2);

    }
}