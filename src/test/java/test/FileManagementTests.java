package test;

import javafx.scene.Node;
import javafx.stage.Stage;
import main.java.zenit.Zenit;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.TestFx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.TextMatchers.hasText;


/**
 * Test class using JUNIT5 FxRobot and the ApplicationTest TestFx framework
 * Scope: TFH103, TFH105, TFH108.1
 * @author Emrik Dahl Jändel
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileManagementTests extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Zenit().start(stage);
    }

    /**
     * Test case TFH103.2 -> Create .java files through menu selection, error message
     */
    @TestFx
    @Order(1)
    void newClassErrorTest() {
        clickOn("File")
                .clickOn("New...")
                .moveTo("New tab")
                .clickOn("New file")
                .clickOn("Create")
                .clickOn("OK")
                .clickOn("Cancel")
        ;
    }

    /**
     * Test case TFH103.2 -> Create .java files through menu selection
     */
    @TestFx
    @Order(2)
    void newClassTest() {
        clickOn("File")
                .clickOn("New...")
                .moveTo("New tab")
                .clickOn("New file")
        ;
        write("TestClass");
        clickOn("Create");
        verifyThat("TestClass.java", isVisible());
    }

    /**
     * Test case TFH103.3 -> Create .txt file
     */
    @TestFx
    @Order(3)
    void newTxtTest(){
        clickOn("File")
                .clickOn("New...")
                .moveTo("New tab")
                .clickOn("New file")
        ;
        write("TestTxt");
        clickOn((Node) lookup(hasText(".java")).query())
                .clickOn((Node) lookup(hasText(".txt")).query());
        clickOn("Create");
        verifyThat(lookup(hasText("TestTxt.txt")), isVisible());
    }

    /**
     * Test case TFH108.1 -> Rename .txt file
     */
    @TestFx
    @Order(4)
    void renameTxtTest(){
        rightClickOn("TestTxt.txt")
                .clickOn("Rename \"TestTxt.txt\"");
        write("RenamedTestTxt");
        clickOn("OK");
        verifyThat(lookup(hasText("RenamedTestTxt.txt")), isVisible());
    }

    /**
     * Test case TFH108.1 -> Rename .java file
     */
    @TestFx
    @Order(5)
    void renameJavaTest(){
        rightClickOn("TestClass.java")
                .clickOn("Rename \"TestClass.java\"");
        write("RenamedTestClass");
        clickOn("OK");
        verifyThat(lookup(hasText("RenamedTestClass.java")), isVisible());
    }

    /**
     * Test case TFH105.1/2 -> Delete a .java file through file tree, tab will close
     */
    @TestFx
    @Order(6)
    void deleteJavaTest(){
        sleep(500);
        rightClickOn("RenamedTestClass.java")
                .clickOn("Delete \"RenamedTestClass.java\"")
        ;
        sleep(500);
        assertEquals(0, lookup(hasText("RenamedTestClass.java")).queryAll().size());
    }

    /**
     * Test case TFH105.1/2 -> Delete a .txt file through file tree, tab will close
     */
    @TestFx
    @Order(7)
    void deleteTxtTest(){
        sleep(500);
        rightClickOn("RenamedTestTxt.txt")
                .clickOn("Delete \"RenamedTestTxt.txt\"");
        sleep(500);
        assertEquals(0, lookup(hasText("RenamedTestTxt.txt")).queryAll().size());
    }
}