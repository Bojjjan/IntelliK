package test;

import javafx.stage.Stage;
import main.java.zenit.Zenit;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.TestFx;
import org.testfx.matcher.control.TextMatchers;
import org.testfx.util.NodeQueryUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Test class using JUNIT5 FxRobot and the ApplicationTest TestFx framework
 * Scope: TPI514.1, TPI514.2, TPI515, TPI516
 * @author Abdulkadir Adde
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProjectManagementTests extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Zenit().start(stage);
    }


    /**
     * Test case TPI514.1 -> Create project, error message
     */
   @TestFx
    @Order(1)
    void createProjectErrorTest() {
        clickOn("File")
                .clickOn("New...")
                .moveTo("New tab")
                .clickOn("New Project")
                .clickOn("OK");
    }

    /**
     * Test case: TPI514.2 -> Create a new project.
     */
    @Test
    @Order(2)
    void createNewProjectTest() {
        clickOn("File")
                .clickOn("New...")
                .moveTo("New tab")
                .clickOn("New Project")
        ;
        write("TestProject");
        clickOn("OK");
        sleep(500);
        verifyThat("TestProject", isVisible());
    }

    /**
     * Test case TPI515 -> Rename project
     */
    @TestFx
    @Order(3)
    void renameProjectTest() {
        rightClickOn("TestProject")
                .clickOn("Rename \"TestProject\"");
        write("RenamedTestProject");
        clickOn("OK");
        sleep(500);
        verifyThat("RenamedTestProject", NodeQueryUtils.isVisible());
    }

    /**
     * Test case TPI516 -> Delete projects
     */
    @TestFx
    @Order(4)
    void deleteProjectTest() {
        rightClickOn("RenamedTestProject")
                .clickOn("Delete \"RenamedTestProject\"");
        sleep(500);
        assertEquals(0, lookup(TextMatchers.hasText("RenamedTestProject")).queryAll().size());
    }
}
