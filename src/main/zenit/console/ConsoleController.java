package main.zenit.console;

import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

import com.pty4j.PtyProcessBuilder;
import com.techsenger.jeditermfx.core.TerminalColor;
import com.techsenger.jeditermfx.core.TextStyle;
import com.techsenger.jeditermfx.core.TtyConnector;
import com.techsenger.jeditermfx.ui.JediTermFxWidget;
import com.techsenger.jeditermfx.ui.TerminalSession;
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider;
import javafx.application.Application;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.javafx.FontIcon;



import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import main.zenit.ConsoleRedirect;
import main.zenit.terminal.AbstractTerminalApplication;
import main.zenit.terminal.JediTermFx;
import zenit.terminal.oldcode.LocalTtyConnector;
import zenit.terminal.pty.PtyProcessTtyConnector;
import main.zenit.ui.MainController;


/**
 * ConsoleController manages the console and terminal components of the application.
 * It handles creation, switching, and management of both console and terminal instances.
 *
 * This controller provides the following functionality:
 * - Creating and managing multiple console instances
 * - Creating and managing multiple terminal instances
 * - Switching between console/terminal modes
 * - Process termination for console instances
 * - UI management for the console component
 *
 * @author Kevin Nordkvist
 * @author Huy Khanh Dang
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

	private final Map<String, String> configureEnvironmentVariables() {
		HashMap envs = new HashMap<String, String>(System.getenv());
		if (com.techsenger.jeditermfx.core.util.Platform.isMacOS()) {
			envs.put("LC_CTYPE", Charsets.UTF_8.name());
		}
		if (!com.techsenger.jeditermfx.core.util.Platform.isWindows()) {
			envs.put("TERM", "xterm-256color");
		}
		return envs;
	}

	/**
	 * Creates a TtyConnector for terminal communication.
	 * Sets up the appropriate shell based on the platform (PowerShell for Windows, bash for others).
	 * Configures and starts a PTY process for terminal interaction.
	 *
	 * @return A configured TtyConnector for terminal communication
	 * @throws IllegalStateException if process creation fails
	 * @Author Kevin Nordkvist, Huy Khanh Dang
	 */

	public TtyConnector createTtyConnector() {
		try {
			var envs = configureEnvironmentVariables();
			String[] command;
			if (com.techsenger.jeditermfx.core.util.Platform.isWindows()) {
				command = new String[]{"powershell.exe"};
			} else {
				String shell = (String) envs.get("SHELL");
				if (shell == null) {
					shell = "/bin/bash";
				}
				if (com.techsenger.jeditermfx.core.util.Platform.isMacOS()) {
					command = new String[]{shell, "--login"};
				} else {
					command = new String[]{shell};
				}
			}
			String workspaceDirectory = Path.of(mainController.getWorkspace()).toAbsolutePath().normalize().toString();
			//logger.info("Starting {} in {}", String.join(" ", command), workspaceDirectory);

			var process = new PtyProcessBuilder()
					.setDirectory(workspaceDirectory)
					.setInitialColumns(120)
					.setInitialRows(20)
					.setCommand(command)
					.setEnvironment(envs)
					.setConsole(false)
					.setUseWinConPty(true)
					.start();

			return new JediTermFx.LoggingPtyProcessTtyConnector(process, StandardCharsets.UTF_8, Arrays.asList(command));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	/**
	 * Creates a new terminal instance.
	 * Sets up the terminal widget with appropriate styling and connects it to a TTY process.
	 * Organizes terminals in the UI to enable proper switching between instances.
	 * Handles error cases by creating an error console if terminal creation fails.
	 * @Author Kevin Nordkvist, Huy Khanh Dang
	 */

	public void newTerminal() {
		try {
			JediTermFx terminalApp = new JediTermFx();
			JediTermFxWidget terminalWidget = terminalApp.getMyWidget();

			TtyConnector ttyConnector = createTtyConnector();
			terminalApp.openSession(terminalWidget, ttyConnector);

			if (terminalAnchorPane == null) {
				terminalAnchorPane = new AnchorPane();
				terminalAnchorPane.setStyle("-fx-background-color:black");
				rootAnchor.getChildren().add(terminalAnchorPane);
			}

			AnchorPane individualTerminalPane = new AnchorPane();
			individualTerminalPane.getChildren().add(terminalWidget.getPane());
			individualTerminalPane.setStyle("-fx-background-color:black");

			terminalWidget.getPane().setMinHeight(5);
			fillAnchor(terminalWidget.getPane());
			fillAnchor(individualTerminalPane);

			terminalAnchorPane.getChildren().add(individualTerminalPane);
			fillAnchor(terminalAnchorPane);

			for (JediTermFxWidget terminal : terminalList) {
				Node parent = terminal.getPane().getParent();
				if (parent != null) {
					parent.setVisible(false);
				}
			}

			individualTerminalPane.setVisible(true);

			terminalList.add(terminalWidget);
			terminalChoiceBox.getItems().add(terminalWidget);
			terminalChoiceBox.getSelectionModel().select(terminalWidget);

			terminalWidget.getPane().setId("Terminal " + terminalList.size());


			activeTerminal = terminalWidget;
			terminalWidget.getPane().requestFocus();

			showTerminalTabs();
		} catch (Exception e) {
			ConsoleArea errorConsole = new ConsoleArea("Terminal Error", null, "-fx-background-color:#ff6666");
			newConsole(errorConsole);
			errorConsole.appendText("Failed to initialize terminal: " + e.getMessage() + "\n");
			e.printStackTrace(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) {
					errorConsole.appendText(String.valueOf((char) b));
				}
			}));
		}
	}

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

		terminalChoiceBox.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> {
			if (newValue != null) {
				for (JediTermFxWidget t : terminalList) {
					Node parent = t.getPane().getParent();
					if (parent != null) {
						parent.setVisible(false);
					}
				}

				for (JediTermFxWidget t : terminalList) {
					if (newValue.equals(t)) {
						Node parent = t.getPane().getParent();
						if (parent != null) {
							parent.setVisible(true);
							parent.toFront();
						}
						activeTerminal = t;
						t.getPane().requestFocus();
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

			if(consoleList.isEmpty()) {
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
				activeTerminal = terminalChoiceBox.getSelectionModel().getSelectedItem();
				activeTerminal.close();
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