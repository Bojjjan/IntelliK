package main.zenit.ui;

import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Opening different kinds of dialog boxes with dynamic text depending of input.
 * @author Alexander Libot
 *
 */
public class DialogBoxes {
	
	/**
	 * Opens an input dialog for reading text from user.
	 * @param stage Stage to open in.
	 * @param title The title of the input dialog
	 * @param header The header of the input dialog
	 * @param content The main text of the input dialog
	 * @param textInput The pre-written input text of the input dialog
	 * @return The text The put in by the user
	 */
	public static String inputDialog(Stage stage, String title, String header, String content, String textInput) {
		return inputDialog(stage, title, header, content, textInput, 0, 0);
	}

	/**
	 * Opens an input dialog for reading text from user and selects part of the pre-written input
	 * text.
	 * @param stage Stage to open in.
	 * @param title The title of the input dialog
	 * @param header The header of the input dialog
	 * @param content The main text of the input dialog
	 * @param textInput The pre-written input text of the input dialog
	 * @param startSelection Where the selection should start
	 * @param stopSelection Where the selection should end
	 * @return The text The put in by the user
	 */
	public static String inputDialog(Stage stage, String title, String header, String content, 
			String textInput, int startSelection, int stopSelection) {
		TextInputDialog dialog = new TextInputDialog(textInput);
		dialog.setTitle(title);
		dialog.setHeaderText(header);
		dialog.setContentText(content);
		TextField tf = dialog.getEditor();

		Platform.runLater(() -> {
			if (stopSelection <= 0) {
				tf.selectRange(content.length() - 1, startSelection);
			} else {
				tf.selectRange(stopSelection, startSelection);
			}
		});
		
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
		   return result.get();
		}
		return null;
	}

	/**
	 * This method is similar to above with the distinction of having a checkbox.
	 * It inherits the Window (JavaFx Stage) from the MainController.
	 * @param owner the window passed from the MainController
	 * @param header header for user instructions
	 * @param title for the window
	 * @param contentText user instructions
	 * @param promptText for the checkbox
	 * @param checkBox for selection
	 * @return the input text from the dialog input field
	 *
	 * @author Emrik
	 */

	public static String inputDialogWithCheckbox(Window owner, String header, String title, String contentText, String promptText, CheckBox checkBox) {
		Dialog<String> dialog = new Dialog<>();
		dialog.initOwner(owner);
		dialog.setTitle(title);
		dialog.setHeaderText(header);

		ButtonType okButton = new ButtonType("OK", ButtonType.OK.getButtonData());
		dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

		TextField inputField = new TextField();
		inputField.setPromptText(promptText);

		VBox vbox = new VBox(10);
		vbox.getChildren().addAll(new Label(contentText), inputField, checkBox);
		dialog.getDialogPane().setContent(vbox);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == okButton) {
				return inputField.getText();
			}
			return null;
		});

		Optional<String> result = dialog.showAndWait();
		return result.orElse(null);
	}
	
	/**
	 * Opens an error dialog
	 * @param title Title of the error dialog
	 * @param header Header of the error dialog
	 * @param content Content text of the error dialog
	 */
	public static void errorDialog(String title, String header, String content) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		alert.showAndWait();
	}
	
	/**
	 * Opens an information dialog
	 * @param title Title of the information dialog
	 * @param content Content text of the information dialog
	 */
	public static void informationDialog(String title, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);

		alert.showAndWait();
	}
	
	/**
	 * Presents a two choice dialog box with custom text and choices.
	 * @param title Text on title bar
	 * @param header Header text
	 * @param content Main text
	 * @param option1 The first option
	 * @param option2 The second option
	 * @return 1 if option1 is selected, 2 if option2 is selected, otherwise 0.
	 */
	public static int twoChoiceDialog(String title, String header, String content, String option1, String option2) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		ButtonType buttonTypeOne = new ButtonType(option1);
		ButtonType buttonTypeTwo = new ButtonType(option2);


		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == buttonTypeOne){
		    return 1;
		} else if (result.get() == buttonTypeTwo) {
		    return 2;
		} else {
			return 0;
		}
	}
}
