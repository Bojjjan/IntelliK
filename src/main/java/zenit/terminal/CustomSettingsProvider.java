package zenit.terminal;

import com.techsenger.jeditermfx.core.HyperlinkStyle;
import com.techsenger.jeditermfx.core.TerminalColor;
import com.techsenger.jeditermfx.core.TextStyle;
import com.techsenger.jeditermfx.core.emulator.ColorPalette;
import com.techsenger.jeditermfx.core.emulator.ColorPaletteImpl;
import com.techsenger.jeditermfx.core.model.TerminalTypeAheadSettings;
import com.techsenger.jeditermfx.core.util.Platform;
import com.techsenger.jeditermfx.ui.FxTransformers;
import com.techsenger.jeditermfx.ui.TerminalActionPresentation;
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider;
import com.techsenger.jeditermfx.ui.settings.SettingsProvider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class CustomSettingsProvider implements SettingsProvider {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSettingsProvider.class);

    public CustomSettingsProvider() {
    }

    public @NotNull TerminalActionPresentation getOpenUrlActionPresentation() {
        return new TerminalActionPresentation("Open as URL", Collections.emptyList());
    }

    public @NotNull TerminalActionPresentation getCopyActionPresentation() {
        KeyCombination keyCombination = Platform.isMacOS() ? new KeyCodeCombination(KeyCode.C, new KeyCombination.Modifier[]{KeyCombination.META_DOWN}) : new KeyCodeCombination(KeyCode.C, new KeyCombination.Modifier[]{KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN});
        return new TerminalActionPresentation("Copy", keyCombination);
    }

    public @NotNull TerminalActionPresentation getPasteActionPresentation() {
        KeyCombination keyCombination = Platform.isMacOS() ? new KeyCodeCombination(KeyCode.V, new KeyCombination.Modifier[]{KeyCombination.META_DOWN}) : new KeyCodeCombination(KeyCode.V, new KeyCombination.Modifier[]{KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN});
        return new TerminalActionPresentation("Paste", keyCombination);
    }

    public @NotNull TerminalActionPresentation getClearBufferActionPresentation() {
        return new TerminalActionPresentation("Clear Buffer", Platform.isMacOS() ? new KeyCodeCombination(KeyCode.K, new KeyCombination.Modifier[]{KeyCombination.META_DOWN}) : new KeyCodeCombination(KeyCode.L, new KeyCombination.Modifier[]{KeyCombination.CONTROL_DOWN}));
    }

    public @NotNull TerminalActionPresentation getPageUpActionPresentation() {
        return new TerminalActionPresentation("Page Up", new KeyCodeCombination(KeyCode.PAGE_UP, new KeyCombination.Modifier[]{KeyCombination.SHIFT_DOWN}));
    }

    public @NotNull TerminalActionPresentation getPageDownActionPresentation() {
        return new TerminalActionPresentation("Page Down", new KeyCodeCombination(KeyCode.PAGE_DOWN, new KeyCombination.Modifier[]{KeyCombination.SHIFT_DOWN}));
    }

    public @NotNull TerminalActionPresentation getLineUpActionPresentation() {
        return new TerminalActionPresentation("Line Up", Platform.isMacOS() ? new KeyCodeCombination(KeyCode.UP, new KeyCombination.Modifier[]{KeyCombination.META_DOWN}) : new KeyCodeCombination(KeyCode.UP, new KeyCombination.Modifier[]{KeyCombination.CONTROL_DOWN}));
    }

    public @NotNull TerminalActionPresentation getLineDownActionPresentation() {
        return new TerminalActionPresentation("Line Down", Platform.isMacOS() ? new KeyCodeCombination(KeyCode.DOWN, new KeyCombination.Modifier[]{KeyCombination.META_DOWN}) : new KeyCodeCombination(KeyCode.DOWN, new KeyCombination.Modifier[]{KeyCombination.CONTROL_DOWN}));
    }

    public @NotNull TerminalActionPresentation getFindActionPresentation() {
        return new TerminalActionPresentation("Find", Platform.isMacOS() ? new KeyCodeCombination(KeyCode.F, new KeyCombination.Modifier[]{KeyCombination.META_DOWN}) : new KeyCodeCombination(KeyCode.F, new KeyCombination.Modifier[]{KeyCombination.CONTROL_DOWN}));
    }

    public @NotNull TerminalActionPresentation getSelectAllActionPresentation() {
        return new TerminalActionPresentation("Select All", Collections.emptyList());
    }

    public ColorPalette getTerminalColorPalette() {
        return getColorPalette();
    }

    public ColorPalette getColorPalette() {
        return new ColorPalette() {
            @Override
            protected com.techsenger.jeditermfx.core.@NotNull Color getForegroundByColorIndex(int i) {
                return new com.techsenger.jeditermfx.core.Color(255, 255, 255); // White text
            }

            @Override
            protected com.techsenger.jeditermfx.core.@NotNull Color getBackgroundByColorIndex(int i) {
                return new com.techsenger.jeditermfx.core.Color(0, 0, 0); // Black background
            }
        };
    }

    public Font getTerminalFont() {
        String fontName;
        if (Platform.isWindows()) {
            fontName = "Consolas";
        } else if (Platform.isMacOS()) {
            fontName = "Menlo";
        } else {
            fontName = "Monospaced";
        }

        Font font = Font.font(fontName, (double)this.getTerminalFontSize());
        logger.debug("Terminal font: {}", font);
        return font;
    }

    public float getTerminalFontSize() {
        return 14.0F;
    }

    public @NotNull TextStyle getSelectionColor() {
        return new TextStyle(TerminalColor.WHITE, TerminalColor.rgb(0, 255, 255));
    }

    public @NotNull TextStyle getFoundPatternColor() {
        return new TextStyle(TerminalColor.BLACK, TerminalColor.rgb(255, 255, 0));
    }

    public TextStyle getHyperlinkColor() {
        return new TextStyle(FxTransformers.fromFxToTerminalColor(Color.RED), TerminalColor.BLACK);
    }

    public HyperlinkStyle.HighlightMode getHyperlinkHighlightingMode() {
        return HyperlinkStyle.HighlightMode.HOVER_WITH_BOTH_COLORS;
    }

    public boolean useInverseSelectionColor() {
        return true;
    }

    public boolean copyOnSelect() {
        return this.emulateX11CopyPaste();
    }

    public boolean pasteOnMiddleMouseClick() {
        return this.emulateX11CopyPaste();
    }

    public boolean emulateX11CopyPaste() {
        return false;
    }

    public boolean useAntialiasing() {
        return true;
    }

    public int maxRefreshRate() {
        return 50;
    }

    public boolean audibleBell() {
        return true;
    }

    public boolean enableMouseReporting() {
        return true;
    }

    public int caretBlinkingMs() {
        return 505;
    }

    public boolean scrollToBottomOnTyping() {
        return true;
    }

    public boolean DECCompatibilityMode() {
        return true;
    }

    public boolean forceActionOnMouseReporting() {
        return false;
    }

    public int getBufferMaxLinesCount() {
        return 5000;
    }

    public boolean altSendsEscape() {
        return true;
    }

    public boolean ambiguousCharsAreDoubleWidth() {
        return false;
    }

    public @NotNull TerminalTypeAheadSettings getTypeAheadSettings() {
        return TerminalTypeAheadSettings.DEFAULT;
    }

    public boolean sendArrowKeysInAlternativeMode() {
        return true;
    }

}
