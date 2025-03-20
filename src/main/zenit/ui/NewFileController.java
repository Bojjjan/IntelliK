package main.zenit.ui;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import main.zenit.exceptions.TypeCodeException;
import main.zenit.filesystem.helpers.CodeSnippets;
import main.zenit.filesystem.helpers.FileNameHelpers;

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
	private final String workspacePath;
    private File newFile;
	private final Stage mainStage;
	private File parent;
	private int typecode = CodeSnippets.EMPTY;
	
	
	public NewFileController(File workspace, Stage mainStage ,boolean darkmode) {
		this.workspace = workspace;
		this.darkmode = darkmode;
		this.mainStage = mainStage;
		this.workspacePath = workspace.getPath();
	}

	public void startBehavior(int typecode, File parent){
		this.parent = parent;
		this.typecode = typecode;
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

			if (typecode != CodeSnippets.EMPTY){
				for(int i = 0; i < fileExtensions.getItems().size(); i++){
					Item item = fileExtensions.getItems().get(i);
					if (item.getTypeCode() == typecode){
						fileExtensions.getSelectionModel().select(i);
						break;
					}
				}
			}
			stage.showAndWait();

				
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}

	private void initialize(Scene scene) {

		Item classItem = new Item("Class", "/zenit/ui/tree/class.png", CodeSnippets.CLASS);
		Item interfaceItem = new Item("Interface", "/zenit/ui/tree/interface.png", CodeSnippets.INTERFACE);
		Item enumItem = new Item("Enum", "/zenit/ui/tree/enum.png", CodeSnippets.ENUM);
		Item exceptionItem = new Item("Exception", "/zenit/ui/tree/zap.png", CodeSnippets.EXCEPTION);


		ImageView javaIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/zenit/ui/tree/java.png"))));
		javaIcon.setFitWidth(16);
		javaIcon.setFitHeight(16);
		MenuItem javaItem = new MenuItem(".java", javaIcon);

		ImageView txtIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/zenit/ui/tree/file.png"))));
		txtIcon.setFitWidth(16);
		txtIcon.setFitHeight(16);
		MenuItem txtItem = new MenuItem(".txt", txtIcon);

		setListCells(classItem,interfaceItem,enumItem,exceptionItem);



		javaItem.setOnAction(event -> {
			if(lastJava) return;
			lastJava = true;

			fileType.setText(javaItem.getText());
			setListCells(classItem, interfaceItem, enumItem, exceptionItem);
		});

		txtItem.setOnAction(event -> {
			lastJava = false;
			fileType.setText(txtItem.getText());
			fileExtensions.getItems().clear();
		});



		fileType.getItems().addAll(javaItem, txtItem);
		fileType.setText(javaItem.getText());

		filepath.getItems().clear();

		if (this.parent != null) {
			File file = new File(parent.getPath());
			String directoryPath = file.isFile() ? file.getParent() : file.getPath();
			filepath.getItems().add(directoryPath);

		} else {
			filepath.getItems().add(workspace.getPath());
		}


		filepath.getSelectionModel().selectFirst();


		filepath.addEventFilter(ScrollEvent.SCROLL, event -> {
			if (event.getDeltaY() != 0) { event.consume(); } // Make it impossible to scroll on Y axis in filepath
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


	private void setListCells(Item classItem, Item interfaceItem, Item enumItem, Item exceptionItem){
		fileExtensions.getItems().addAll(classItem, interfaceItem, enumItem, exceptionItem);

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
							Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(item.getImagePath())));
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

		String fileTypeExtension = fileType.getText();

		String filename = (tfName.getText() + fileTypeExtension);
		String filePath = null;
		short typeCode = CodeSnippets.EMPTY;


		if (!filename.isEmpty()) {
			if (fileTypeExtension.equals(".java")){

				char firstChar = filename.charAt(0);
				if (!Character.isDigit(firstChar)){

					if(!filename.contains(" ")) {

						filePath = ((parent == null ? workspacePath : parent.getPath()) + (File.separator + filename));
						String javaType =  fileExtensions.getSelectionModel().getSelectedItem().getName();

						typeCode = switch (javaType) {
							case "Class" -> CodeSnippets.CLASS;
							case "Interface" -> CodeSnippets.INTERFACE;
							case "Enum" -> CodeSnippets.ENUM;
							case "Exception" -> CodeSnippets.EXCEPTION;
							default -> typeCode;
						};


					}else {
						DialogBoxes.errorDialog("Invalid file name", "", "File name can't contain spaces");
						newFile = null;
						return;
					}

				}else {
					DialogBoxes.errorDialog("Invalid file name", "", "File name can't start with special characters or numbers");
					newFile = null;
					return;
				}

			} else {
				filePath = ((parent == null ? workspacePath : parent.getPath()) + (File.separator + filename));
			}

				try {
                    newFile = new File(filePath);

					String code;
					if(parent != null){
						code =  CodeSnippets.newSnippet(typeCode, filename,
								FileNameHelpers.getPackagenameFromFile(newFile,workspacePath, parent.getPath()));

					}else {
						code =  CodeSnippets.newSnippet(typeCode, filename,
								FileNameHelpers.getPackagenameFromFile(newFile, workspacePath));
					}

					if (!newFile.createNewFile()) {
						DialogBoxes.errorDialog("File name already exist", "", "A file with the name "
								+ filename + " already exist. Please input a different name.");
						newFile = null;
						return;
					}else {

						try (BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), StandardCharsets.UTF_8))) {
							br.write(code);
							br.flush();

						}catch (IOException e){
							e.getMessage();
						}
					}

					stage.close();

				} catch (IOException e) {
					DialogBoxes.errorDialog("Couldn't create new file", "", "Couldn't create new file");
					newFile = null;
				} catch (TypeCodeException e) {
                    throw new RuntimeException(e);
                }


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
		private final int typeCode;

		private Item(String name, String imagePath, int typeCode) {
			this.name = name;
			this.imagePath = imagePath;
			this.typeCode = typeCode;
		}

		private String getName() {
			return name;
		}

		public int getTypeCode() {
			return typeCode;
		}

		private String getImagePath() {
			return imagePath;
		}
	}

}
