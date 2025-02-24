package zenit.terminal;


import com.techsenger.jeditermfx.core.TtyConnector;
import com.techsenger.jeditermfx.core.model.JediTerminal;
import com.techsenger.jeditermfx.ui.JediTermFxWidget;
import com.techsenger.jeditermfx.ui.TerminalSession;
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;


public class TerminalController {

	@FXML
	private TabPane tabPane;
	
	@FXML
	private AnchorPane basePane;

	private JediTermFxWidget terminal;

	public TerminalController(){
		terminal = new JediTermFxWidget(80, 24, new DefaultSettingsProvider());;
		initialize();
	}
	
	public void initialize() {
		addTerminalTab();
	}
	
	private void addTerminalTab() {
		terminal.start();
		Tab terminalTab = new Tab("Terminal");
/*
        try {
            //TtyConnector ttyConnector = new LocalTtyConnector();
			//TerminalSession terminalSession = terminal.createTerminalSession(ttyConnector);
			//terminalSession.start();

		} catch (IOException e) {
            throw new RuntimeException("Terminal Session Exception");
        }

 */

		//terminalTab.setContent(terminal.getPane());
		tabPane.getTabs().add(terminalTab);

		/*TerminalConfig darkConfig = new TerminalConfig();
		darkConfig.setBackgroundColor(Color.BLACK);
		darkConfig.setForegroundColor(Color.WHITE);
		darkConfig.setCursorBlink(true);
		darkConfig.setCursorColor(Color.WHITE);
		darkConfig.setFontFamily("consolas");


		TerminalBuilder builder = new TerminalBuilder(darkConfig);
		TerminalTab terminalTab = builder.newTerminal();
		tabPane.getTabs().add(terminalTab);*/
	}

	public Pane getTerminalPane(){
		return terminal.getPane();
	}

	public JediTermFxWidget getTerminal(){
		return terminal;
	}
}

