package main.java.zenit.ui.tree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	 * @author Philip Boyde
	 */
	public void setIcon() {
		try {
			String url = getIconPath();
			if (url == null) return;

			icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(url)).toExternalForm()));
			icon.setFitHeight(16);
			icon.setFitWidth(16);
			icon.setSmooth(true);
			this.setGraphic(icon);

		}catch (IllegalStateException e){
			System.err.println(e.getMessage());
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
			case CLASS -> "/zenit/ui/tree/class.png";

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