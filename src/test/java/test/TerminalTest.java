package test;
import com.techsenger.jeditermfx.ui.JediTermFxWidget;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import main.zenit.Zenit;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.TestFx;
import org.testfx.matcher.control.TextMatchers;
import org.testfx.robot.ClickRobot;
import org.testfx.robot.Motion;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.NodeQueryUtils.isVisible;


class TerminalTest extends ApplicationTest{

    @Override
    public void start(Stage stage) throws Exception {
        new Zenit().start(stage);
    }

    @TestFx
    void showTerminalTabs() {
        clickOn("TERMINAL");
        verifyThat("TERMINAL", isVisible());
    }
    @TestFx
    @Order(2)
    void testMultipleTerminalInstances() {
        clickOn("TERMINAL");
        verifyThat("TERMINAL", isVisible());
        clickOn("+");
        clickOn("#terminalChoiceBox");

        ChoiceBox<JediTermFxWidget> terminalChoiceBox = lookup("#terminalChoiceBox").query();
        List<String> terminalNames = terminalChoiceBox.getItems().stream()
                .map(widget -> widget.getPane().getId()) // Assuming getPane().getId() returns a comprehensible name
                .collect(Collectors.toList());

        assertTrue(terminalNames.size() > 1, "There should be multiple terminal instances");
        for (String name : terminalNames) {
            assertFalse(name.contains("@"), "Terminal name should be comprehensible");
        }
    }

    @TestFx
    @Order(3)
    void testResizeTerminalWindow() throws InterruptedException {
        clickOn("TERMINAL");
        verifyThat("TERMINAL", isVisible());
        moveTo("#rootNode").moveBy(0, -60).drag().moveBy(0,-200);
        assertTrue(lookup("#rootNode").query().getBoundsInParent().getHeight() > 100);
    }

    @TestFx
    @Order(4)
    void testTerminalDefaultPath() {

    }

}