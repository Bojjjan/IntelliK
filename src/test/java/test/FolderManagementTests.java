package test;

import javafx.stage.Stage;
import main.java.zenit.Zenit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.TestFx;
import org.testfx.matcher.control.TextMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.NodeQueryUtils.isVisible;

/**
 * Test class using JUNIT5 FxRobot and the ApplicationTest TestFx framework
 * Scope: TFH104, TFH106
 * @author Emrik Dahl Jändel
 */

public class FolderManagementTests extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Zenit().start(stage);
    }

    /**
     * Test case TFH104.1 -> Create folders, error message
     */
    @TestFx
    void createFolderErrorTest() {
        clickOn("File")
                .clickOn("New...")
                .moveTo("New tab")
                .clickOn("New folder")
                .clickOn("Create")
                .clickOn("OK")
                .clickOn("Cancel")
        ;
    }

    /**
     * Test case TFH104.1 -> Create folders
     */
    @TestFx
    void createFolderTest(){
        clickOn("File")
                .clickOn("New...")
                .moveTo("New tab")
                .clickOn("New folder")
        ;
        write("TestFolder");
        clickOn("Create");
        sleep(500);
        verifyThat("TestFolder", isVisible());
    }

    /**
     * Test case TFH106.1 -> Delete folders
     */
    @TestFx
    void deleteFolderTest(){
        rightClickOn("TestFolder")
                .clickOn("Delete \"TestFolder\"");
        sleep(500);
        assertEquals(0, lookup(TextMatchers.hasText("TestFolder")).queryAll().size());
    }
}
