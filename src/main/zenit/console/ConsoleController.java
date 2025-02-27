package zenit.console;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.techsenger.jeditermfx.ui.JediTermFxWidget;
import com.techsenger.jeditermfx.ui.TerminalSession;
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider;
import javafx.application.Application;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;



import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import zenit.ConsoleRedirect;
import zenit.terminal.AbstractTerminalApplication;
import zenit.terminal.JediTermFx;
import zenit.terminal.oldcode.LocalTtyConnector;
import zenit.ui.MainController;

/**
 * The controller class for ConsoleArea
 * 
 * @author siggelabor
 *
 */
/**
 * @author Admin
 *
 */
public class ConsoleController implements Initializable {

	/*
	 * These HashMaps are ugly.
	 */
//	private HashMap<ConsoleArea, Process> consoleList = new HashMap<ConsoleArea, Process>();
	private ArrayList<ConsoleArea> consoleList = new ArrayList<ConsoleArea>();

	private ArrayList<JediTermFxWidget> terminalList = new ArrayList<JediTermFxWidget>();
	
	@FXML 
	private TabPane consoleTabPane;

	@FXML
	private Button btnTerminal;

	@FXML
	private Button btnConsole;

	@FXML
	private ChoiceBox<ConsoleArea> consoleChoiceBox; 

	@FXML
	private ChoiceBox<JediTermFxWidget> terminalChoiceBox;
	
	@FXML
	private AnchorPane rootAnchor;
	
	@FXML
	private AnchorPane rootNode;

	@FXML
	private Button btnNewTerminal;
	
	@FXML
	private Button btnNewConsole;
	
	@FXML
	private Button btnClearConsole;
	
	@FXML
	private FontIcon iconCloseConsoleInstance;
	
	@FXML
	private FontIcon iconTerminateProcess;
	
	@FXML
	private FontIcon iconCloseTerminalInstance;
		
	private AnchorPane terminalAnchorPane; 
	
	private AnchorPane consoleAnchorPane;
	
	private ConsoleArea activeConsole;
	
	private JediTermFxWidget activeTerminal;
	
	private AnchorPane noConsolePane;
		
	private MainController mainController;

	//private JediTermFxWidget terminal;


	public void setMainController(MainController mainController) {
		this.mainController = mainController;
	}
	
	public List<String> getStylesheets() {
		return rootNode.getStylesheets();
	}
	
	
	/**
	 * Shows the choiceBox with console areas, and sets the choiceBox with terminal tabs to not 
	 * visible. Also sets text color of the labels.
	 */
	

	public void showConsoleTabs() {
		
		btnTerminal.setStyle("");
		btnConsole.setStyle("-fx-text-fill:white; -fx-border-color:#666; -fx-border-width: 0 0 2 0;");
		

		terminalChoiceBox.setVisible(false);
		terminalChoiceBox.setDisable(true);
		consoleChoiceBox.setVisible(true);
		consoleChoiceBox.setDisable(false);
		btnNewTerminal.setVisible(false);
		btnNewConsole.setVisible(true);
		btnClearConsole.setDisable(false);
		btnClearConsole.setVisible(true);
		iconTerminateProcess.setVisible(true);
		iconTerminateProcess.setDisable(false);
		iconCloseConsoleInstance.setVisible(true);
		iconCloseConsoleInstance.setDisable(false);
		iconCloseTerminalInstance.setVisible(false);
		iconCloseTerminalInstance.setDisable(true);
		
			
		if (consoleAnchorPane != null) {
				consoleAnchorPane.toFront();
		}
		
		if(consoleList.size() == 0) {
			createEmptyConsolePane();
		}
		
	
		
	}
	
	
	/*
	 * Creates and displays an anchorPane when there is no console to display in the console-window
	 */
	private void createEmptyConsolePane() {
		noConsolePane = new AnchorPane();
		fillAnchor(noConsolePane);
		Label label = new Label("No Console To Display");
		noConsolePane.getChildren().add(label);
		label.setFont(new Font(14));
		label.setTextFill(Color.BLACK);
		label.setMaxWidth(Double.MAX_VALUE);
		AnchorPane.setLeftAnchor(label, 0.0);
		AnchorPane.setRightAnchor(label, 0.0);
		label.setAlignment(Pos.CENTER);
		noConsolePane.setId("empty");
		rootAnchor.getChildren().add(noConsolePane);
		noConsolePane.toFront();
	}
	
	
	/**
	 * Shows the choiceBox with terminal panes, and sets the choiceBox with console tabs to not 
	 * visible. Also sets text color of the labels.
	 */
	public void showTerminalTabs() {
		btnConsole.setStyle("");
		btnTerminal.setStyle("-fx-text-fill:white; -fx-border-color:#666; -fx-border-width: 0 0 2 0;");
		
		
		if(terminalList.isEmpty()) {
			newTerminal();
		}
		else {
			terminalAnchorPane.toFront();
		}

		consoleChoiceBox.setVisible(false);
		consoleChoiceBox.setDisable(true);
		terminalChoiceBox.setVisible(true);
		terminalChoiceBox.setDisable(false);
		btnNewTerminal.setVisible(true);
		btnNewConsole.setVisible(false);
		btnClearConsole.setDisable(true);
		btnClearConsole.setVisible(false);
		iconTerminateProcess.setVisible(false);
		iconTerminateProcess.setDisable(true);
		iconCloseConsoleInstance.setVisible(false);
		iconCloseConsoleInstance.setDisable(true);
		iconCloseTerminalInstance.setVisible(true);
		iconCloseTerminalInstance.setDisable(false);
				
	}

	/**
	 * Creates a new ConsoleArea, adds it to the console AnchorPane and puts it as an option in the
	 * choiceBox.
	 */
	
	public void newConsole(ConsoleArea consoleArea) {
		consoleAnchorPane = new AnchorPane();
		consoleArea.setId("consoleArea");
		consoleAnchorPane.setId("consoleAnchor");
		fillAnchor(consoleArea);
		fillAnchor(consoleAnchorPane);
		
		
		
		consoleAnchorPane.getChildren().add(consoleArea);
		rootAnchor.getChildren().add(consoleAnchorPane);
		
		consoleList.add(consoleArea);
		
		consoleChoiceBox.getItems().add(consoleArea);
		consoleChoiceBox.getSelectionModel().select(consoleArea);
		
		new ConsoleRedirect(consoleArea);	
		showConsoleTabs();
	}

//	public JediTermFxWidget getTerminal(){
//		return terminal;
//	}

	
	/*
	 * Creates a new Terminal, adds it to the terminal
	 *  AnchorPane and puts it as an option in the
	 * choiceBox.
	 */
	/*
	public void newTerminal() {

		PipedWriter terminalWriter = new PipedWriter();
		//PipedReader terminalReader = new PipedReader(terminalWriter);




		JediTermFxWidget terminal = new JediTermFxWidget(80, 24, new DefaultSettingsProvider());

		try{
			TtyConnector ttyConnector = new ExampleTtyConnector(terminalWriter);
			//TtyConnector ttyConnector = new LocalTtyConnector();
			terminal.setTtyConnector(ttyConnector);
			terminal.start();
			TerminalSession terminalSession = terminal.createTerminalSession(ttyConnector);
			terminalSession.start();
		} catch(Exception e){
			throw new RuntimeException(e);
		}

		terminal.getPane().setId("Terminal ("+terminalList.size()+")");
		terminalAnchorPane = new AnchorPane();
		terminalAnchorPane.setStyle("-fx-background-color:black");

		terminal.getPane().setMinHeight(5);
		fillAnchor(terminal.getPane());
		fillAnchor(terminalAnchorPane);
		
		terminalAnchorPane.getChildren().add(terminal.getPane());
		rootAnchor.getChildren().add(terminalAnchorPane);
		terminalList.add(terminal);
		terminalChoiceBox.getItems().add(terminal);
		terminalChoiceBox.getSelectionModel().select(terminal);
		terminal.start();
		showTerminalTabs();
    }

	 */

	public void newTerminal() {
		try {
			JediTermFx terminalApp = new JediTermFx();
			//terminalApp.start();
			JediTermFxWidget terminalWidget = terminalApp.createTerminalWidget(new DefaultSettingsProvider());


			Platform.runLater(() -> {
				terminalAnchorPane.getChildren().add(terminalWidget.getPane());
			});
			terminalAnchorPane.requestLayout();
			terminalAnchorPane.setStyle("-fx-background-color:black");
			terminalAnchorPane.setVisible(true);
			terminalAnchorPane.setMinSize(100, 100);
			terminalWidget.getPane().setVisible(true);
			//terminalWidget.start();

			terminalWidget.getPane().setMinHeight(5);
			fillAnchor(terminalWidget.getPane());
			fillAnchor(terminalAnchorPane);

			// Add the terminal to the UI
			rootAnchor.getChildren().add(terminalAnchorPane);


			// Update tracking lists and UI state
			terminalList.add(terminalWidget);
			terminalChoiceBox.getItems().add(terminalWidget);
			terminalChoiceBox.getSelectionModel().select(terminalWidget);

			// Focus the terminal
			Platform.runLater(() -> terminalWidget.getPane().requestFocus());

			// Show terminal tabs
			showTerminalTabs();
		} catch (Exception e) {
			// Create a console to display the error
			ConsoleArea errorConsole = new ConsoleArea("Terminal Error", null, "-fx-background-color:#ff6666");
			newConsole(errorConsole);
			errorConsole.appendText("Failed to initialize terminal: " + e.getMessage() + "\n");
			e.printStackTrace(new java.io.PrintStream(new java.io.OutputStream() {
				@Override
				public void write(int b) {
					errorConsole.appendText(String.valueOf((char) b));
				}
			}));
		}
	}



/*

	private TerminalConfig createTerminalConfig() {
		TerminalConfig windowsConfig = new TerminalConfig();
		windowsConfig.setBackgroundColor(Color.BLACK);
		windowsConfig.setForegroundColor(Color.WHITE);
		windowsConfig.setCursorBlink(true);
		windowsConfig.setCursorColor(Color.WHITE);
		windowsConfig.setFontFamily("consolas");
		windowsConfig.setFontSize(12);
		windowsConfig.setScrollbarVisible(false);
		
		//TODO Non-windows config (if needed).
		
		
		return (System.getProperty("os.name").startsWith("W") ? windowsConfig : new TerminalConfig());
	}
 */
	

	/**
	 * sets the anchor of a node to fill parent 
	 * 
	 * @param node to fill to parent anchor
	 */
	public void fillAnchor(Node node) {
		AnchorPane.setTopAnchor(node, 0.0);
		AnchorPane.setRightAnchor(node, 0.0);
		AnchorPane.setBottomAnchor(node, 0.0);
		AnchorPane.setLeftAnchor(node, 0.0);
	}
	
	
	/**
	 * Clears the active consoleArea
	 */
	public void clearConsole() {
		if (activeConsole != null) {
			activeConsole.clear();
		}
	}
	
	
	public void closeComponent() {
		mainController.closeConsoleComponent();
	}
	
	
	public void changeAllConsoleAreaColors(String color) {
		for(ConsoleArea c : consoleList) {
			c.setBackgroundColor(color);
		}
	}
	
	/**
	 * Performs initialization steps.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		consoleChoiceBox.getSelectionModel().selectedItemProperty().addListener( (v, oldValue, newValue) -> {
						
			if(newValue != null) {
				for(ConsoleArea console : consoleList) {
					if(newValue.equals(console)) {
						console.getParent().toFront();
						activeConsole = console;
					}
				}
			}
			
		});
				
					
				
		terminalChoiceBox.getSelectionModel().selectedItemProperty().addListener( (v, oldValue, newValue) -> {
			if(newValue != null) {
				for(JediTermFxWidget t : terminalList) {
					if(newValue.equals(t)) {
						t.getPane().toFront();
						activeTerminal = t;

						/* t.onTerminalFxReady(()-> {
							t.focusCursor();
						}); */

					}
				}
			}
		});
		
		showConsoleTabs();
		
		//Console
		iconCloseConsoleInstance.setOnMouseClicked(e -> {
				rootAnchor.getChildren().remove(activeConsole.getParent());
				consoleList.remove(activeConsole);
				consoleChoiceBox.getItems().remove(activeConsole);
				consoleChoiceBox.getSelectionModel().selectLast();

				if(consoleList.size() == 0) {
					createEmptyConsolePane();
				}
		});
		
		//Terminal
		iconCloseTerminalInstance.setOnMouseClicked(e ->{
			
			if(terminalList.size() > 1) {
				rootAnchor.getChildren().remove(activeTerminal.getPane());
				terminalList.remove(activeTerminal);
				terminalChoiceBox.getItems().remove(activeTerminal);
				terminalChoiceBox.getSelectionModel().selectLast();
			}
			
			
		});
		
		
		btnNewConsole.setOnMouseClicked(e -> {
			if(mainController.isDarkmode()) {
				newConsole(new ConsoleArea("Console(" + consoleList.size() + ")", null, "-fx-background-color:#444"));
			}else {
				newConsole(new ConsoleArea("Console(" + consoleList.size() + ")", null, "-fx-background-color:#989898"));
			}
			
		});
		
		iconTerminateProcess.setOnMouseClicked(e -> {
			for(var item : consoleList) {
				if(item.equals(activeConsole)) {
					if(item != null) {
						item.getProcess().destroy();
					}	
				}
			}
							
		});
		
		
	}
}