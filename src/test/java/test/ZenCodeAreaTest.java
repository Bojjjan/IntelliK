package test;

import javafx.stage.Stage;
import main.zenit.Zenit;
import main.zenit.searchinfile.Search;
import main.zenit.zencodearea.ZenCodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.mockito.Mock;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.TestFx;
import java.io.*;
import java.util.Collection;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class using JUNIT5
 * This class test the syntax highlighting of the ZenCodeArea
 * Scope: FUI300 and FUI301
 * @author Mojtaba hauari
 */
class ZenCodeAreaTest extends ApplicationTest {

    @Mock
    private ZenCodeArea mockZenCodeArea;
    private static final String TEST_FILE_PATH = System.getProperty("user.home") + "\\Documents\\HighlightTest.java";


    @Override
    public void start(Stage stage) throws Exception {
        new Zenit().start(stage);
        //mockZenCodeArea = new ZenCodeArea(13,"Menlo");
    }

    @BeforeEach
    public void setUp() {
        writeinitialFile();
    }

    /**
     * Writes the initial file for the test
     */
    private void writeinitialFile() {
        try (BufferedWriter br = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(TEST_FILE_PATH), "UTF-8"))) {
            br.write("public class HighlightTest { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }");
            br.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Test case TUI107 -> Syntax highlighting
     * This test case tests the syntax highlighting of the ZenCodeArea
     * The expected spans are compared to the actual spans and is commentated to know what each span represents
     */
    @TestFx
    @Order(1)
    void syntaxHighlightingTesting() {
        String sampleCode = "public class Test { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }";
        interact(() -> mockZenCodeArea.replaceText(0, 0, sampleCode));
        //StyleSpans<Collection<String>> actualSpans = mockZenCodeArea.computeHighlighting(sampleCode);
        StyleSpans<Collection<String>> expectedSpans = new StyleSpansBuilder<Collection<String>>()
                .add(Collections.singleton("access-modifier"), 6) // public
                .add(Collections.singleton("default"), 1) // space
                .add(Collections.singleton("access-modifier"), 5) // class
                .add(Collections.singleton("default"), 1) // space
                .add(Collections.singleton("class-name"), 4) // Test
                .add(Collections.singleton("default"), 1) // space
                .add(Collections.singleton("bracket"), 1) // {
                .add(Collections.singleton("default"), 1) // space
                .add(Collections.singleton("access-modifier"), 6) // public
                .add(Collections.singleton("default"), 1) // space
                .add(Collections.singleton("access-modifier"), 6) // static
                .add(Collections.singleton("default"), 1) // space
                .add(Collections.singleton("access-modifier"), 4) // void
                .add(Collections.singleton("default"), 1) // space
                .add(Collections.singleton("method-name"), 4) // main
                .add(Collections.singleton("bracket"), 1) // (
                .add(Collections.singleton("identifier"), 6) // String
                .add(Collections.singleton("bracket"), 2) // []
                .add(Collections.singleton("default"), 1) // space
                .add(Collections.singleton("identifier"), 4) // args
                .add(Collections.singleton("bracket"), 1) // )
                .add(Collections.singleton("default"), 1) // space
                .add(Collections.singleton("bracket"), 1) // {
                .add(Collections.singleton("default"), 1) // space
                .add(Collections.singleton("identifier"), 6) // System
                .add(Collections.singleton("default"), 1) // .
                .add(Collections.singleton("identifier"), 3) // out
                .add(Collections.singleton("default"), 1) // .
                .add(Collections.singleton("identifier"), 7) // println
                .add(Collections.singleton("bracket"), 1) // (
                .add(Collections.singleton("strings"), 15) // "Hello, World!"
                .add(Collections.singleton("bracket"), 1) // )
                .add(Collections.singleton("default"), 2) // );
                .add(Collections.singleton("bracket"), 1) // }
                .add(Collections.singleton("default"), 1) // space
                .add(Collections.singleton("bracket"), 1) // }
                .create();
        //assertEquals(expectedSpans, actualSpans);
    }
}