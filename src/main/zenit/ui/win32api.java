package main.zenit.ui;

import javafx.stage.Stage;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import javafx.stage.Window;
import java.lang.reflect.Method;


public class win32api {

    /**
     * Retrieves the native window handle for a given JavaFX Stage.
     *
     * @author Philip Boyde
     * @param stage the JavaFX Stage for which to get the native window handle
     * @return the native window handle (HWND) or null if an error occurs
     */
    private static WinDef.HWND getNativeHandleForStage(Stage stage) {
        try {
            // Use reflection to access the private getPeer method of the Window class
            Method getPeer = Window.class.getDeclaredMethod("getPeer");
            getPeer.setAccessible(true);
            Object tkStage = getPeer.invoke(stage);

            // Use reflection to access the private getRawHandle method of the tkStage object
            Method getRawHandle = tkStage.getClass().getMethod("getRawHandle");
            getRawHandle.setAccessible(true);
            Pointer pointer = new Pointer((Long) getRawHandle.invoke(tkStage));

            return new WinDef.HWND(pointer);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            return null;
        }
    }

    /**
     * Sets the dark mode attribute for a given JavaFX Stage.
     *
     * @param stage the JavaFX Stage for which to set the dark mode attribute
     * @param darkMode true to enable dark mode, false to disable it
     */
    protected static void setDarkMode(Stage stage, boolean darkMode) {
        WinDef.HWND hwnd = win32api.getNativeHandleForStage(stage);
        if(hwnd ==  null) return;

        Dwmapi dwmapi = Dwmapi.INSTANCE;

        // Create a BOOLByReference object to hold the dark mode value
        WinDef.BOOLByReference darkModeRef = new WinDef.BOOLByReference(new WinDef.BOOL(darkMode));

        // Call the DwmSetWindowAttribute method to set the dark mode attribute
        dwmapi.DwmSetWindowAttribute(hwnd, 20, darkModeRef, WinDef.BOOL.SIZE);
    }
}

