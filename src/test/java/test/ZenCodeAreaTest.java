package test;

import javafx.stage.Stage;
import main.java.zenit.Zenit;
import main.java.zenit.filesystem.JavaFileHandler;
import main.java.zenit.searchinfile.Search;
import main.java.zenit.ui.MainController;
import main.java.zenit.zencodearea.ZenCodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.TestFx;

import java.io.*;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ZenCodeAreaTest extends ApplicationTest {

    private Search search;
    @Mock
    private ZenCodeArea mockZenCodeArea;
    private static final String TEST_FILE_PATH = System.getProperty("user.home") + "\\Documents\\highlightTest.txt";


    @Override
    public void start(Stage stage) throws Exception {
        new Zenit().start(stage);
        mockZenCodeArea = Mockito.mock(ZenCodeArea.class);
        //Change your path here to the path of the file you want to search in or just change the username for your pc
        search = new Search(mockZenCodeArea, new File(TEST_FILE_PATH), true, null);
    }

    @BeforeEach
    public void setUp() {
        when(mockZenCodeArea.getAbsolutePosition(anyInt(), anyInt())).thenReturn(0);
        when(mockZenCodeArea.getCaretPosition()).thenReturn(0);
        writeinitialFile();
    }

    private void writeinitialFile() {
        try (BufferedWriter br = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(TEST_FILE_PATH), "UTF-8"))) {
            br.write("public class Test { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }");
            br.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Test case TUI107 -> Syntax highlighting
     */
    @TestFx
    @Order(1)
    void updateHighlighting() {
        String sampleCode = "public class Test { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }";
        interact(() -> mockZenCodeArea.replaceText(0, 0, sampleCode));
        StyleSpans<Collection<String>> actualSpans = mockZenCodeArea.update();
        StyleSpans<Collection<String>> expectedSpans = new StyleSpansBuilder<Collection<String>>()
                .add(Collections.singleton("access-modifier"), 6) // public
                .add(Collections.emptyList(), 1) // space
                .add(Collections.singleton("keyword"), 5) // class
                .add(Collections.emptyList(), 1) // space
                .add(Collections.singleton("class-name"), 4) // Test
                .add(Collections.emptyList(), 1) // space
                .add(Collections.singleton("bracket"), 1) // {
                .add(Collections.emptyList(), 1) // space
                .add(Collections.singleton("access-modifier"), 6) // public
                .add(Collections.emptyList(), 1) // space
                .add(Collections.singleton("keyword"), 6) // static
                .add(Collections.emptyList(), 1) // space
                .add(Collections.singleton("datatype"), 4) // void
                .add(Collections.emptyList(), 1) // space
                .add(Collections.singleton("method-name"), 4) // main
                .add(Collections.emptyList(), 1) // (
                .add(Collections.singleton("datatype"), 6) // String
                .add(Collections.emptyList(), 2) // []
                .add(Collections.singleton("variable"), 4) // args
                .add(Collections.emptyList(), 3) // ) {
                .add(Collections.singleton("class-name"), 6) // System
                .add(Collections.emptyList(), 1) // .
                .add(Collections.singleton("method-name"), 3) // out
                .add(Collections.emptyList(), 1) // .
                .add(Collections.singleton("method-name"), 7) // println
                .add(Collections.emptyList(), 1) // (
                .add(Collections.singleton("strings"), 14) // "Hello, World!"
                .add(Collections.emptyList(), 3) // );
                .add(Collections.singleton("bracket"), 1) // }
                .add(Collections.emptyList(), 1) // space
                .add(Collections.singleton("bracket"), 1) // }
                .create();
        assertEquals(expectedSpans, actualSpans);
    }
}