package main.zenit.ui;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

public class NewFileController extends AnchorPane {
	
	private Stage stage;
	private final boolean darkmode;

	@FXML private AnchorPane header;
	@FXML private ListView<String> filepath;
	@FXML private ListView<Item> fileExtensions;
	@FXML private MenuButton fileType;
	@FXML private TextField tfName;


	private boolean lastJava = true;
    private double xOffset = 0;
    private double yOffset = 0;
    
    private final File workspace;
    private File newFile;
	private final Stage mainStage;
	
	
	public NewFileController(File workspace, Stage mainStage ,boolean darkmode) {
		this.workspace = workspace;
		this.darkmode = darkmode;
		this.mainStage = mainStage;

	}

	/**
	 * Opens new Project Info window.
	 */
	public void start() {
		try {
			//setup scene
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/zenit/ui/NewFile.fxml"));
			loader.setController(this);
			AnchorPane root = (AnchorPane) loader.load();
			Scene scene = new Scene(root);

			stage = new Stage();
			stage.setResizable(false);
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(scene);

			ifDarkModeChanged(darkmode);
			stage.initModality(Modality.WINDOW_MODAL);

			stage.initOwner(mainStage);

			stage.setOnShown(event -> {
				Stage owner = (Stage) stage.getOwner();
				if (owner != null) {
					double centerX = owner.getX() + (owner.getWidth() - stage.getWidth()) / 2;
					double centerY = owner.getY() + (owner.getHeight() - stage.getHeight()) / 2;
					stage.setX(centerX);
					stage.setY(centerY);
				}
			});


			scene.setFill(Color.TRANSPARENT);
			stage.initStyle(StageStyle.TRANSPARENT);
			root.setStyle("-fx-background-color: transparent;");
			root.requestFocus();

			initialize(scene);
			stage.showAndWait();

				
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}

	private void initialize(Scene scene) {

		Item classItem = new Item("Class", "/zenit/ui/tree/class.png");
		Item interfaceItem = new Item("Interface", "/zenit/ui/tree/class.png");
		Item enumItem = new Item("Enum", "/zenit/ui/tree/class.png");


		ImageView javaIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/zenit/ui/tree/java.png"))));
		javaIcon.setFitWidth(16);
		javaIcon.setFitHeight(16);
		MenuItem javaItem = new MenuItem(".java", javaIcon);

		ImageView txtIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/zenit/ui/tree/file.png"))));
		txtIcon.setFitWidth(16);
		txtIcon.setFitHeight(16);
		MenuItem txtItem = new MenuItem(".txt", txtIcon);

		javaItem.setOnAction(event -> {
			if(lastJava) return;
			lastJava = true;

			fileType.setText(javaItem.getText());
			setListCells(classItem,interfaceItem,enumItem);
		});

		setListCells(classItem,interfaceItem,enumItem);


		txtItem.setOnAction(event -> {
			lastJava = false;
			fileType.setText(txtItem.getText());
			fileExtensions.getItems().clear();
		});



		fileType.getItems().addAll(javaItem, txtItem);
		fileType.setText(javaItem.getText());

		filepath.getItems().clear();
		filepath.getItems().add(workspace.getPath());
		filepath.getSelectionModel().selectFirst();



		scene.setOnKeyPressed(event -> {
			int currentIndex = fileExtensions.getSelectionModel().getSelectedIndex();

			if (event.getCode() == KeyCode.UP) {
				if (currentIndex > 0) {
					fileExtensions.getSelectionModel().selectPrevious();
				} else {
					fileExtensions.getSelectionModel().selectFirst();
				}
				fileExtensions.scrollTo(fileExtensions.getSelectionModel().getSelectedIndex());
				System.out.println("Up arrow key pressed");

			} else if (event.getCode() == KeyCode.DOWN) {
				if (currentIndex < (fileExtensions.getItems().size() - 1)) {
					fileExtensions.getSelectionModel().selectNext();
				} else {
					fileExtensions.getSelectionModel().selectLast();
				}


				fileExtensions.scrollTo(fileExtensions.getSelectionModel().getSelectedIndex());
				System.out.println("Down arrow key pressed");
			}

			System.out.println("index: " + currentIndex);
		});
		
	    header.setOnMousePressed(new EventHandler<MouseEvent>() {
	    	   @Override
	    	   public void handle(MouseEvent event) {
	    	       xOffset = event.getSceneX();
	    	       yOffset = event.getSceneY();
	    	   }
	    	});

	    header.setOnMouseDragged(new EventHandler<MouseEvent>() {
	    	   @Override
	    	   public void handle(MouseEvent event) {
	    	       stage.setX(event.getScreenX() - xOffset);
	    	       stage.setY(event.getScreenY() - yOffset);
	    	   }
	    	});
	}


	private void setListCells(Item classItem, Item interfaceItem, Item enumItem){

		fileExtensions.getItems().addAll(classItem, interfaceItem, enumItem);

		if (!fileExtensions.getItems().isEmpty()) {
			fileExtensions.getSelectionModel().select(0);
		}

		fileExtensions.setCellFactory(new Callback<ListView<Item>, ListCell<Item>>() {
			@Override
			public ListCell<Item> call(ListView<Item> param) {
				return new ListCell<Item>() {
					private final ImageView imageView = new ImageView();
					private final Label label = new Label();
					private final HBox hBox = new HBox(10, imageView, label);

					@Override
					protected void updateItem(Item item, boolean empty) {
						super.updateItem(item, empty);
						if (empty || item == null) {
							setGraphic(null);
						} else {
							Image image = new Image(getClass().getResourceAsStream(item.getImagePath()));
							imageView.setImage(image);
							imageView.setFitWidth(16);
							imageView.setFitHeight(16);
							label.setText(item.getName());
							setGraphic(hBox);
						}
					}
				};
			}
		});
	}

	@FXML
	private void create() {
		String filename = tfName.getText();

		if (!filename.equals("")) {
			//filename += fileEnding.getSelectionModel().getSelectedItem();

			System.out.println(filename);

			String filePath = this.filepath.getSelectionModel().getSelectedItem() + File.separator + filename;
			newFile = new File(filePath);
			try {
				if (!newFile.createNewFile()) {
					DialogBoxes.errorDialog("File name already exist", "", "A file with the name "
							+ filename + " already exist. Please input a different name.");
					newFile = null;
				}
			} catch (IOException e) {
				DialogBoxes.errorDialog("Couldn't create new file", "", "Couldn't create new file");
				newFile = null;
			}

			stage.close();
		} else {
			DialogBoxes.errorDialog("No name selected", "", "No name has been given to the new file"
					+ ". Please input a new name to create file.");
		}

	}

	@FXML
	private void cancel() {
		stage.close();
	}
	
	@FXML
	private void browse() {
		DirectoryChooser dc = new DirectoryChooser();
		dc.setInitialDirectory(workspace);
		dc.setTitle("Select directory to create new file in");
		File chosen = dc.showDialog(stage);
		
		if (chosen != null) {
			filepath.getItems().clear();
			filepath.getItems().add(chosen.getPath());
			filepath.getSelectionModel().selectFirst();
		}
	}
	
	
	public void ifDarkModeChanged(boolean isDarkMode) {
		var stylesheets = stage.getScene().getStylesheets();
		var darkMode = getClass().getResource("/zenit/ui/projectinfo/mainStyle.css").toExternalForm();
		var lightMode = getClass().getResource("/zenit/ui/projectinfo/mainStyle-lm.css").toExternalForm();
		
		if (isDarkMode) {
			if (stylesheets.contains(lightMode)) {
				stylesheets.remove(lightMode);
			}
			
			stylesheets.add(darkMode);
		} else {
			if (stylesheets.contains(darkMode)) {
				stylesheets.remove(darkMode);
			}
			stylesheets.add(lightMode);
		}	
	}
	
	public File getNewFile() {
		return newFile;
	}

	private static class Item {
		private final String name;
		private final String imagePath;

		public Item(String name, String imagePath) {
			this.name = name;
			this.imagePath = imagePath;
		}

		public String getName() {
			return name;
		}

		public String getImagePath() {
			return imagePath;
		}
	}

}
