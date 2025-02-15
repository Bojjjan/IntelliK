package test;

import javafx.scene.Node;
import javafx.stage.Stage;
import main.java.zenit.Zenit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.TestFx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.TextMatchers.hasText;


/**
 * Test class using JUNIT5 FxRobot and the ApplicationTest TestFx framework
 * Scope: TFH103, TFH105
 * @author Emrik Dahl Jändel
 */
class FileManagementTests extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Zenit().start(stage);
    }

    /**
     * Test case TFH103.2 -> Create .java files through menu selection, error message
     */
    @TestFx
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
     * Test case TFH105.1/2 -> Delete a file through file tree, tab will close
     */
    @TestFx
    void newClassTest() {
        clickOn("File")
                .clickOn("New...")
                .moveTo("New tab")
                .clickOn("New file")
        ;
        write("TestClass");
        clickOn("Create");
        verifyThat("TestClass.java", isVisible());
        rightClickOn("TestClass.java")
                .clickOn("Delete \"TestClass.java\"")
        ;
        sleep(500);
        assertEquals(0, lookup(hasText("TestClass.java")).queryAll().size());
    }

    /**
     * Test case TFH103.3 -> Create .txt file
     */
    @TestFx
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
        rightClickOn("TestTxt.txt")
            .clickOn("Delete \"TestTxt.txt\"");
        sleep(500);
        assertEquals(0, lookup(hasText("TestTxt.txt")).queryAll().size());
    }
}