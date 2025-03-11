package test;

import javafx.stage.Stage;
import main.zenit.Zenit;
import main.zenit.filesystem.JavaFileHandler;
import main.zenit.searchinfile.Search;
import main.zenit.ui.MainController;
import main.zenit.zencodearea.ZenCodeArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.TestFx;


import java.io.*;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Test class using JUNIT5 FxRobot, ApplicationTest TestFx framework and Mockito mock testing
 * This class should test everything automatically and the file should be saved in your documents folder as searchTest.txt
 * Scope: FFH200, FFH201, FFH202, FFH203, FFH204 , FFH205
 * @author Mojtaba hauari
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SearchTest extends ApplicationTest {

    private Search search;
    @Mock
    private ZenCodeArea mockZenCodeArea;
    @Mock
    private MainController mainController;
    private JavaFileHandler javaFileHandler;
    private static final String TEST_FILE_PATH = System.getProperty("user.home") + "\\Documents\\searchTest.txt";


    @Override
    public void start(Stage stage) throws Exception {
        new Zenit().start(stage);
        mockZenCodeArea = Mockito.mock(ZenCodeArea.class);
        javaFileHandler = Mockito.mock(JavaFileHandler.class);
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
            br.write("String String String");
            br.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Test case TFH201.1 , TFH202 -> Search for a word in a file and see the number of occurrences
     */
    @TestFx
    @Order(1)
    void searchForWord() {
        when(mockZenCodeArea.getText()).thenReturn("String");
        int result = search.searchInFile("String");
        assertEquals(3, result);
    }

    /**
     * Test case TFH205 -> Replace all instance of a word in the file
     *
     */
    @TestFx
    @Order(2)
    void changeAllOfTheSearchedWords(){
        int result = search.searchInFile("String");
        assertEquals(3, result);
        search.replaceAll("replace");
        when(mockZenCodeArea.getText()).thenReturn("replace replace replace");
        String updatedText = mockZenCodeArea.getText();
        System.out.println(updatedText);
        try (BufferedWriter br = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(TEST_FILE_PATH), "UTF-8"))) {
            br.write(updatedText);
            br.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int resultAfter = search.searchInFile("replace");
        assertEquals(3,resultAfter);
    }

    /**
     * Test case TFH204 -> replace only on of the instances of the word
     */
    @TestFx
    @Order(3)
    void changeOneOfTheSearchedWords(){
        int result = search.searchInFile("String");
        assertEquals(3, result);
        search.replaceOne("replace");
        when(mockZenCodeArea.getText()).thenReturn("replace String String");
        String updatedText = mockZenCodeArea.getText();
        System.out.println(updatedText);
        try (BufferedWriter br = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(TEST_FILE_PATH), "UTF-8"))) {
            br.write(updatedText);
            br.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int resultAfter = search.searchInFile("replace");
        assertEquals(1,resultAfter);
    }

}
