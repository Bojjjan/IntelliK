package main.java.zenit.ui.tree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Extension of the TreeItem class with the ability to save a corresponding File-object
 * in the instance.
 * @author Alexander Libot
 *
 * @param <T>
 */
public class FileTreeItem<T> extends TreeItem<T> {
	private File file;
	private int type;
	
	public static final int WORKSPACE = 100;
	public static final int PROJECT = 101;
	public static final int PACKAGE = 102;
	public static final int CLASS = 103;
	public static final int SRC = 104;
	public static final int FOLDER = 105;
	public static final int FILE = 106;
	public static final int INCOMPATIBLE = 107;
	public static final int CONTROLLER = 108;
	public static final int VIEW = 109;
	public static final int MODEL = 110;
	public static final int RUNNABLE_CLASS = 111;
	public static final int INTERFACE_CLASS = 112;
	public static final int ENUM_CLASS = 113;


    private ImageView icon;
    
	
	/**
	 * @param file Corresponding file
	 * @param name
	 */
	public FileTreeItem(File file, T name, int type) {
		super(name);
		this.file = file;
		this.type = type;
		
		setIcon();
	}

	/**
	 * Sets the icon for the TreeItem based on its type.
	 * - Om det är en .java-fil med en main-metod, visas en grön ▶-ikon.
	 * - Annars visas standardikonen.
	 * Ikonen hämtas via getIconPath() och justeras till 16x16 pixlar.

	 * @author Philip Boyde, Abdulkadir Adde
	 */

	 public void setIcon() {
		try {
			String url = getIconPath();
			if (url == null) return;

			ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(url)).toExternalForm()));
			icon.setFitHeight(16);
			icon.setFitWidth(16);
			icon.setSmooth(true);

			if (type == CLASS && file.getName().endsWith(".java") && RunnableClassIndicator.containsMainMethod(file.toPath())) {
				Label playIcon = new Label("▶");
				playIcon.setTextFill(Color.LIMEGREEN);
				playIcon.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
				setGraphic(new HBox(5, icon, playIcon));
			} else {
				setGraphic(icon);
			}
		} catch (IOException | IllegalStateException e) {
			System.err.println("Icon error: " + e.getMessage());
		}
	}

	/**
	 * Returns the path to the icon image based on the type of the item.
	 *
	 * @author Philip Boyde
	 * @return The path to the icon image.
	 */
	private String getIconPath(){
		return switch (type){
			case PROJECT -> "/zenit/ui/tree/project.png";
			case PACKAGE -> "/zenit/ui/tree/package.png";

			// Java files
			case CLASS -> "/zenit/ui/tree/class.png";
			case RUNNABLE_CLASS -> "";
			case INTERFACE_CLASS -> "";
			case ENUM_CLASS -> "";

			//Folder
			case FOLDER -> "/zenit/ui/tree/folder.png";
			case SRC -> "/zenit/ui/tree/src.png";
			case CONTROLLER -> "/zenit/ui/tree/controller.png";
			case VIEW -> "/zenit/ui/tree/view.png";
			case MODEL -> "/zenit/ui/tree/model.png";

			//Files
			case FILE -> "/zenit/ui/tree/file.png";
			case INCOMPATIBLE -> "/zenit/ui/tree/incompatible.png";

			case WORKSPACE -> null;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
	}

	/**
	 * Set the corresponding file
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * Get the corresponding file
	 */
	public File getFile() {
		return file;
	}
	
	public int getType() {
		return type;
	}
	
	public String getStringType() {
		String stringType;
		switch (type) {
		case PROJECT: stringType = "project"; break;
		case PACKAGE: stringType = "package"; break;
		case CLASS: stringType = "class"; break;
		case SRC: stringType = "src-folder"; break;
		case FOLDER: stringType = "folder"; break;
		case FILE: stringType = "file"; break;
		case INCOMPATIBLE: stringType = "incompatible"; break;
		default: stringType = null;
		}
		
		return stringType;
	}
}