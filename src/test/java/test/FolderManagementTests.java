package test;

import javafx.stage.Stage;
import main.java.zenit.Zenit;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.TestFx;
import org.testfx.matcher.control.TextMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.NodeQueryUtils.isVisible;

/**
 * Test class using JUNIT5 FxRobot and the ApplicationTest TestFx framework
 * Scope: TFH104, TFH106, TFH108.2
 * @author Emrik Dahl Jändel
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FolderManagementTests extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Zenit().start(stage);
    }

    /**
     * Test case TFH104.1 -> Create folder, error message
     */
    @TestFx
    @Order(1)
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
     * Test case TFH104.1 -> Create folder
     */
    @TestFx
    @Order(2)
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
     * Test case TFH108.2 -> Rename folder
     */
    @TestFx
    @Order(3)
    void renameFolderTest(){
        rightClickOn("TestFolder")
                .clickOn("Rename \"TestFolder\"");
        write("RenamedTestFolder");
        clickOn("OK");
        sleep(500);
        verifyThat("RenamedTestFolder", isVisible());
    }

    /**
     * Test case TFH106.1 -> Delete folders
     */
    @TestFx
    @Order(4)
    void deleteFolderTest(){
        rightClickOn("RenamedTestFolder")
                .clickOn("Delete \"RenamedTestFolder\"");
        sleep(500);
        assertEquals(0, lookup(TextMatchers.hasText("RenamedTestFolder")).queryAll().size());
    }
}
