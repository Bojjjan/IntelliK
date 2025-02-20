package zenit.ui;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinDef;


/**
 * The Dwmapi interface provides access to the dwmapi library using JNA.
 * It includes methods to interact with Windows Desktop Window Manager (DWM) APIs.
 */
public interface Dwmapi extends Library {
    // Load the dwmapi library and create an instance of the Dwmapi interface
    Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class);

    /**
     * Sets the value of a specified attribute for a window.
     *
     * @param hwnd the handle to the window
     * @param dwAttribute the attribute to set
     * @param pvAttribute a pointer to the value of the attribute
     * @param cbAttribute the size of the attribute value
     * @return an integer indicating success or failure
     */
    int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, PointerType pvAttribute, int cbAttribute);
}