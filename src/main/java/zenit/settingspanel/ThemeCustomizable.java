package zenit.settingspanel;

import javafx.stage.Stage;

import java.io.File;

public interface ThemeCustomizable {
	
	public File getCustomThemeCSS();
	
	public Stage getStage();
	
	public String getActiveStylesheet();

}
