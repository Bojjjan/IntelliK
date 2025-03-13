package main.zenit.ui.projectinfo;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import main.zenit.Zenit;
import main.zenit.ui.TestUI;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.TestFx;
import org.testfx.service.query.PointQuery;
import static org.testfx.api.FxAssert.verifyThat;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProjectMetadataControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new TestUI().start(stage);
    }

    @Test
    @TestFx
    @Order(1)
    void setUp() throws Exception {
        clickOn("File")
                .moveTo("New...")
                .moveTo("New tab")
                .clickOn("New Project")
                .moveTo(960,485)
                .clickOn()
                .write("Test Project")
                .clickOn("Include default Main class")
                .clickOn("OK")

                .doubleClickOn("Test Project")
                .doubleClickOn("src")
                .moveTo(600,485)
                .rightClickOn()

                .moveTo(600, 481)
                        .clickOn("New class")
                        .write("Main2")
                        .clickOn("OK");

        //TODO: Fixa så att main2 skapas.
    }

    @TestFx
    void warnForUnsavedProgramArguement() {
            rightClickOn("Test Project")
                    .clickOn("Properties")
                    .clickOn("Advanced Settings")
                    .clickOn("Main.java")
                    .moveTo(960,485)
                    .clickOn()
                    .write("Test123")
                    .clickOn("Main2.java");

                  //TODO:  verifyThat()
    }
}
