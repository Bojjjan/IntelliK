package main.zenit.searchinfile;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.zenit.ui.MainController;


public class SearchInFileController extends AnchorPane {

	@FXML
	private TextField fldInputField;
	
	@FXML
	private TextField fldReplaceWord;
	
	@FXML
	private Button btnUp;
	
	@FXML
	private Button btnDown;
	
	@FXML 
	private Button btnEsc;
	
	@FXML
	private Button btnReplaceOne;
	
	@FXML
	private Button btnReplaceAll;
	
	@FXML
	private Label lblOccurrences;

	@FXML
	private ImageView imgBtnDown;

	@FXML
	private ImageView imgBtnUp;

	private Search search;
	
	private int occurrences = 0;
	
	private Stage window;
	
	private Scene scene;

	public SearchInFileController(Search search, MainController mainController) {

		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(getClass().getResource("/zenit/searchinfile/SearchInFileWindow.fxml"));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		window = new Stage();
		scene = new Scene(this);

		window.initStyle(StageStyle.UNDECORATED);

		window.setScene(scene);
		
		window.setX(1050);
		window.setY(160);
		
		fldInputField.requestFocus();
		
		initialize();
		scene.getStylesheets()
				.add(getClass().getResource("/zenit/searchinfile/searchInFileDarkMode.css").toExternalForm());

		window.show();
		
		this.search = search;
	}
	
	private void makeNewSearch(String searchWord) {
		
		occurrences = search.searchInFile(searchWord);

		if(occurrences < 1) {
			if(fldInputField.getText().length() > 0) {
				lblOccurrences.setText("0/" + occurrences);
			}else {
				lblOccurrences.setText("");
			}

		}else {
			lblOccurrences.setText("1/" + occurrences);
		}	
	}

	private void initialize() {
		fldInputField.textProperty().addListener((observable, oldValue, newValue) -> {	
			search.clearZen();
			makeNewSearch(fldInputField.getText());
		});

		fldInputField.setPromptText("Search...");
		fldReplaceWord.setPromptText("Replace with...");
		
		btnReplaceAll.setPickOnBounds(true);
		btnReplaceAll.setOnAction(event -> 
			search.replaceAll(fldReplaceWord.getText()));
		
		btnReplaceOne.setPickOnBounds(true);
		btnReplaceOne.setOnAction(event -> 
			search.replaceOne(fldReplaceWord.getText()));

		imgBtnUp.setImage(new Image(getClass().getResourceAsStream("/zenit/searchinfile/arrow_up.png")));
		btnUp.setPickOnBounds(true);
		btnUp.setOnAction(event ->{
			int i = search.jumpUp();
			i++;
			lblOccurrences.setText(i + "/" + occurrences);
		});

		imgBtnDown.setImage(new Image(getClass().getResourceAsStream("/zenit/searchinfile/arrow_down.png")));
		btnDown.setPickOnBounds(true);
		btnDown.setOnAction(event -> {
			int i = search.jumpDown();
			i++;
			lblOccurrences.setText(i + "/" + occurrences);
		
		});
		
		btnEsc.setPickOnBounds(true);
		btnEsc.setOnMouseClicked(event -> window.close());
		btnEsc.setOnAction(event -> window.close());
		btnEsc.setOnAction(event -> search.cleanZen());
		
		scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>
		  () {

			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ESCAPE) {
					window.close();
					search.cleanZen();
				}
			}
		});
	}
}
