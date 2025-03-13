package test;


import javafx.stage.Stage;
import main.zenit.setup.SetupController;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.TextMatchers.hasText;


/**
 * This class tests the core functionality of the SetupScreen, including error handling and the automatic import of JDKs.
 * Scope: FUI700, FUI704, FUI705, FUI707, FUI708, FUI709, FUI710
 * @author Philip Boyde
 */

public class projectSetupTests extends ApplicationTest {

    @Mock
    private SetupController setupController;

    @Override
    public void start(Stage stage) throws Exception {
        System.setProperty("testfx.environment", "true");
        setupController = new SetupController(stage);
        setupController.start();
    }

    @BeforeAll
    public static void setup() throws IOException {
        deleteFileIfExists(Paths.get("res/JDK/DefaultJDK.dat"));
        deleteFileIfExists(Paths.get("res/JDK/JDK.dat"));
        deleteFileIfExists(Paths.get("res/workspace/workspace.dat"));
    }

    private static void deleteFileIfExists(Path path) throws IOException {
        if (Files.exists(path)) { Files.delete(path); }
    }

    public static List<String> getInstalledJDKs() {
        String jdkPath;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            jdkPath = "C:\\Program Files\\Java\\";
        } else {
            jdkPath = "/Library/Java/JavaVirtualMachines/";
        }

        File jdkDirectory = new File(jdkPath);
        List<String> jdkFolders = new ArrayList<>();

        if (jdkDirectory.exists() && jdkDirectory.isDirectory()) {
            File[] files = jdkDirectory.listFiles(File::isDirectory);
            if (files != null) {
                for (File file : files) {
                    jdkFolders.add(file.getName());
                }
            }
        } else {
            System.err.println("JDK directory not found: " + jdkPath);
        }
        return jdkFolders;
    }


    @Nested
    public class ErrorTests {
        @Test
        public void testDoneWithInvalidInput() throws IOException {
            deleteFileIfExists(Paths.get("res/JDK/DefaultJDK.dat"));
            clickOn("Done");
            verifyThat(lookup(hasText("Please enter the required information to launch IntelliK")), isVisible());
        }

        @Test
        public void testSetDefaultWithInvalidInput(){
            clickOn("Set default JDK");
            verifyThat(lookup(hasText("Choose a JDK from the list to make it the default")), isVisible());
        }

        @Test
        public void testRemoveJDKWithInvalidInput(){
            clickOn("Remove JDK");
            verifyThat(lookup(hasText("Choose a JDK from the list to remove it")), isVisible());
        }
    }

    @Nested
    public class jdkTest {
        // FUI705 --> Automatically added JDKs
        @Test
        public void testIfAllJDKAreAdded(){
            List<String> jdkFiles = readJDKData("res/JDK/JDK.dat");
            List<String> installedJDKs = getInstalledJDKs();
            assertTrue(compareJDKLists(jdkFiles, installedJDKs));
        }

        public static boolean compareJDKLists(List<String> list1, List<String> list2) {
            return new HashSet<>(list1).equals(new HashSet<>(list2));
        }

        // FUI710 --> Display the JDK's name
        @Test
        public void testIfAllJDKAreInTheGUI(){
            List<String> installedJDKs = getInstalledJDKs();
            for (String jdk : installedJDKs) {
                assertTrue(!lookup(jdk).queryAll().isEmpty(), "JDK not found in GUI: " + jdk);
            }
        }

        // FUI709 --> Default JDK
        @TestFactory
        Stream<DynamicTest> testIfEachJDKCanBeSetAssDefault(){
            List<String> installedJDKs = getInstalledJDKs();

            return installedJDKs.stream()
                    .map(jdk -> DynamicTest.dynamicTest("Test JDK: " + jdk, () -> {
                        clickOn(jdk).clickOn("Set default JDK");
                        assertTrue(!lookup(jdk + " [default]").queryAll().isEmpty(), "JDK not set as Default: " + jdk);
                    }));
        }

        // FUI707 --> Remove JDK
        @TestFactory
        Stream<DynamicTest> testIfEachJDKCanBeRemoved(){
            List<String> installedJDKs = getInstalledJDKs();
            AtomicInteger length = new AtomicInteger(installedJDKs.size());

            return installedJDKs.stream()
                    .map(jdk -> DynamicTest.dynamicTest("Test JDK: " + jdk, () -> {

                        clickOn(jdk).clickOn("Remove JDK");
                        length.getAndDecrement();

                        if (length.get() == 0){
                            clickOn("Yes, remove");
                            deleteFileIfExists(Paths.get("res/JDK/JDK.dat"));
                        }

                        assertTrue(lookup(jdk).queryAll().isEmpty(), "JDK not removed: " + jdk);
                    }));

        }


        public static List<String> readJDKData(String filePath) {
            List<String> jdkFiles = new ArrayList<>();
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filePath)))) {
                File file;

                while ((file = (File) ois.readObject()) != null) {
                    String filePathStr = file.getAbsolutePath();
                    String jdkName = filePathStr.substring(filePathStr.lastIndexOf("\\") + 1);
                    jdkFiles.add(jdkName);
                }

            } catch (EOFException e) {
                //End of the file
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error reading JDK data: " + e.getMessage());
            }
            return jdkFiles;
        }
    }
}
