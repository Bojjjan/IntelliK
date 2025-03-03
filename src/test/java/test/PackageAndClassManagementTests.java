package test;

import javafx.scene.control.Labeled;
import javafx.stage.Stage;
import main.java.zenit.Zenit;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testfx.framework.junit5.ApplicationTest;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.matcher.control.TextMatchers.hasText;

/**
 * Test class using JUNIT5 FxRobot and the ApplicationTest TestFx framework
 * Scope: TPI517, TPI518, TPI519, TPI520, TPI521, TPI522
 * @author Abdulkadir Adde
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PackageAndClassManagementTests extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Zenit().start(stage);
    }

    /**
     * Test case TPI517 -> Create a new package inside the created project
     */
    @Test
    @Order(1)
    void createNewPackageTest() {
        doubleClickOn("src");
        rightClickOn("src")
                .clickOn("New...")
                .moveTo("New class")
                .clickOn("New package");

        write("testpackage");
        clickOn("OK");

        sleep(500);
        expandTreeItem("src");
        sleep(500);

        assertTrue(lookup(".tree-cell").queryAll().stream()
                        .anyMatch(node -> node instanceof Labeled && "testpackage".equals(((Labeled) node).getText())),
                "Paketet 'testpackage' hittades inte i TreeView.");
    }

    /**
     * Test case TPI518 -> Rename package inside the created project
     */
    @Test
    @Order(2)
    void renamePackageTest() {

        if (!lookup(".tree-cell").match(hasText("src")).tryQuery().isPresent()) {
            doubleClickOn("src");
            sleep(500);
        }

        expandTreeItem("src");
        sleep(500);

        rightClickOn("testpackage")
                .clickOn("Rename \"testpackage\"");

        write("renamedpackage");
        clickOn("OK");
        sleep(500);

        assertTrue(lookup(".tree-cell").queryAll().stream()
                        .anyMatch(node -> node instanceof Labeled && "renamedpackage".equals(((Labeled) node).getText())),
                "Paketet kunde inte byta namn till 'renamedpackage'!");
    }

    /**
     * Test case TPI519 -> Delete package inside the created project
     */
    @Test
    @Order(3)
    void deletePackageTest() {

        if (!lookup(".tree-cell").match(hasText("src")).tryQuery().isPresent()) {
            doubleClickOn("src");
            sleep(500);
        }

        expandTreeItem("src");
        sleep(500);

        rightClickOn("renamedpackage")
                .clickOn("Delete \"renamedpackage\"");

        sleep(500);


        assertEquals(0, lookup(".tree-cell").queryAll().stream()
                .filter(node -> node instanceof Labeled && "renamedpackage".equals(((Labeled) node).getText()))
                .count(), "Paketet 'renamedpackage' finns fortfarande i TreeView!");
    }

    @Test
    @Order(4)
    void createNewClassInPackageTest() {

        createNewPackageTest();
        expandTreeItem("src");

        assertTrue(lookup(".tree-cell").queryAll().stream()
                        .anyMatch(node -> node instanceof Labeled && "testpackage".equals(((Labeled) node).getText())),
                "Paketet 'testpackage' hittades inte i TreeView!");

        expandTreeItem("src/testpackage");
        rightClickOn("testpackage")
                .clickOn("New...")
                .clickOn("New class");

        write("TestClass");
        clickOn("OK");

        doubleClickOn("testpackage");

        assertTrue(lookup(".tree-cell").queryAll().stream()
                        .anyMatch(node -> node instanceof Labeled && "TestClass.java".equals(((Labeled) node).getText())),
                "Klassen 'TestClass' skapades inte i paketet 'testpackage'!");
    }

    /**
     * Test case TPI521 -> Rename class inside the created project
     */
    @Test
    @Order(5)
    void renameClassInPackageTest() {

        if (!lookup(".tree-cell").match(hasText("src")).tryQuery().isPresent()) {
            doubleClickOn("src");
            sleep(500);
        }

        expandTreeItem("src");
        sleep(500);

        if (lookup(".tree-cell").queryAll().stream()
                .anyMatch(node -> node instanceof Labeled && "testpackage".equals(((Labeled) node).getText()))) {
            doubleClickOn("testpackage");
            sleep(500);
        }

        expandTreeItem("src/testpackage");
        sleep(500);

        assertTrue(lookup(".tree-cell").queryAll().stream()
                        .anyMatch(node -> node instanceof Labeled && "TestClass.java".equals(((Labeled) node).getText())),
                "Klassen 'TestClass.java' hittades inte i paketet 'testpackage'!");

        rightClickOn("TestClass.java")
                .clickOn("Rename \"TestClass.java\"");

        write("RenamedTestClass");
        clickOn("OK");

        sleep(500);
        expandTreeItem("src/testpackage");

        assertEquals(0, lookup(".tree-cell").queryAll().stream()
                .filter(node -> node instanceof Labeled && "TestClass.java".equals(((Labeled) node).getText()))
                .count(), "Gamla klassen 'TestClass.java' finns fortfarande kvar!");
    }

    /**
     * Test case TPI522 -> Delete class inside the created project
     */
    @Test
    @Order(6)
    void deleteClassTest() {

        if (!lookup(".tree-cell").match(hasText("src")).tryQuery().isPresent()) {
            doubleClickOn("src");
            sleep(500);
        }

        expandTreeItem("src");
        sleep(500);

        if (lookup(".tree-cell").queryAll().stream()
                .anyMatch(node -> node instanceof Labeled && "testpackage".equals(((Labeled) node).getText()))) {
            doubleClickOn("testpackage");
            sleep(500);
        }

        expandTreeItem("src/testpackage");
        sleep(500);

        assertTrue(lookup(".tree-cell").queryAll().stream()
                        .anyMatch(node -> node instanceof Labeled && "RenamedTestClass.java".equals(((Labeled) node).getText())),
                "Klassen 'TestClass.java' hittades inte i paketet 'testpackage'!");

        rightClickOn("RenamedTestClass.java")
                .clickOn("Delete \"RenamedTestClass.java\"");

        sleep(500); // Vänta på raderingen

        assertEquals(0, lookup(".tree-cell").queryAll().stream()
                .filter(node -> node instanceof Labeled && "RenamedTestClass.java".equals(((Labeled) node).getText()))
                .count(), "Klassen 'RenamedTestClass.java' finns fortfarande kvar!");
    }
    private void expandTreeItem(String s) {
    }
}
