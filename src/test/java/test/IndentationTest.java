package test;

import javafx.application.Platform;
import javafx.stage.Stage;
import main.zenit.ui.FileTab;
import main.zenit.ui.MainController;
import main.zenit.zencodearea.ZenCodeArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import java.io.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class using JUNIT5 , ApplicationTest TestFx framework
 * This class test the implementation of indentation of the code
 * Scope: No requirements are yet written for this
 * @author Mojtaba hauari
 */

class IndentationTest extends ApplicationTest {

    private MainController mainController;
    private ZenCodeArea zenCodeArea;
    private FileTab fileTab;
    private static final String TEST_FILE_PATH = System.getProperty("user.home") + "\\Documents\\indentationTest.txt";

    @Override
    public void start(Stage stage) throws Exception {
        Platform.runLater(() -> {
            mainController = new MainController(stage);
            zenCodeArea = new ZenCodeArea(13, "Menlo");
            fileTab = new FileTab(zenCodeArea, mainController);
            fileTab.setFile(new File(TEST_FILE_PATH),false);
        });
    }

    /**
     * This method test if a new line is created when ENTER is pressed
     */
    @Test
    void emptyIndentationTest() {
        interact(() -> {
            String result = fileTab.commentsShortcutsTrigger();
            assertEquals("\n", result);
        });
    }

    /**
     *This tests if it creates the closing bracket of a comment.
     */
    @Test
    void testAfterSlashStar() {
        interact(() -> {
            zenCodeArea.replaceText("/*");
            zenCodeArea.moveTo(2);
            String result = fileTab.commentsShortcutsTrigger();
            assertEquals("/*\n* \n*/", result);
            assertEquals(5, zenCodeArea.getCaretPosition());
        });
    }

    /**
     * This tests if it creates the closing brackets for the javadoc comments.
     */
    @Test
    void testAfterSlashStarStar() {
        interact(() -> {
            zenCodeArea.replaceText("/**");
            zenCodeArea.moveTo(3);
            String result = fileTab.commentsShortcutsTrigger();
            assertEquals("/**\n* \n* @author \n*/", result);
            assertEquals(6, zenCodeArea.getCaretPosition());
        });
    }


    @BeforeEach
    void setUp() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @AfterEach
    void tearDown() throws Exception {
        FxToolkit.cleanupStages();
    }
}