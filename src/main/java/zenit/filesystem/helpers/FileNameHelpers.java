package main.java.zenit.filesystem.helpers;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Static classes for manipulating filenames and structures
 * @author Alexander Libot
 *
 */
public class FileNameHelpers {
	
	/**
	 * Returns the projectname from a filepath, if the project contains a src-folder.
	 * Otherwise returns the last folder.
	 * @param file Filepath to search through
	 * @return Returns the name of the project if found.
	 */
	public static String getProjectnameFromFile(File file) {
		String projectname = null;
	
		if (file != null) {
			String[] folders = getFoldersAsStringArray(file);
			
			int srcIndex = getSrcFolderIndex(folders);

			if (srcIndex != -1) {
				projectname = folders[srcIndex - 1]; //Projectfolder is one step up from src-folder
			} else {
				projectname = folders[folders.length-1];
			}
		}
		
		return projectname;
	}
	
	/**
	 * Returns the packagename from a filepath, if the project contains a src-folder and
	 * the package is put in that src-folder.
	 *
	 * @param file          Filepath to search through
	 * @param workspacePath
	 * @return Returns the name of the package if found, otherwise null
	 */
	public static String getPackagenameFromFile(File file, String workspacePath) {
		String packagename = null;
		if (file != null) {
			packagename = internalPathExtractor(file.getAbsolutePath(), workspacePath);
		}
		
		return packagename;
	}
	
	/**
	 * Returns the classname from a filepath, if the project contains a src-folder and
	 * the class is put in a package inside that src-folder.
	 * @param file Filepath to search through
	 * @return Returns the name of the class if found, otherwise null
	 */
	public static String getClassnameFromFile(File file) {
		String classname = null;
		
		if (file != null) {
			String[] folders = getFoldersAsStringArray(file);
			
			int srcIndex = getSrcFolderIndex(folders);
			
			if (srcIndex != -1 && folders.length > srcIndex+2 ) { //Filepath is atleast two folders deeper than src-folder
				classname = folders[srcIndex +2]; //Class-file is two steps down from src-folder
			}
		}
		return classname;
	}
	
	/**
	 * Removes the last file/folder in a filepath
	 * @param filepath The filepath to alter
	 * @return The altered file
	 */
	public static File getFilepathWithoutTopFile(File filepath) {
		File newFilepath;
		
		String[] folders = getFoldersAsStringArray(filepath);
		String newFilepathString = "";
		
		for (int index = 0; index < folders.length-1; index++) {
			newFilepathString += folders[index] + "/";
		}
		
		newFilepath = new File(newFilepathString);
		
		return newFilepath;
	}
	
	/**
	 * Removes all folders up until the src-folder
	 * @param file The file to alter
	 * @return The new file
	 */
	public static File getFilepathWithoutPackageName(File file) {
		File newFilepath;
		
		String[] folders = getFoldersAsStringArray(file);
		int srcIndex = getSrcFolderIndex(folders);
		String newFilepathString = "";
		
		for (int index = 0; index <= srcIndex; index++) {
			newFilepathString += folders[index] + "/";
		}
		
		newFilepath = new File(newFilepathString);
		
		return newFilepath;
	}
	
	/**
	 * Returns the filepath of the project
	 * @param filepath The whole filepath
	 * @return The filepath until the project
	 */
	public static File getProjectFilepath(File filepath) {
		String[] folders = getFoldersAsStringArray(filepath);
		int srcIndex = getSrcFolderIndex(folders);
		
		String newFilepath = "";
		for (int index = 0; index < srcIndex; index++) {
			newFilepath += folders[index] + "/";
		}
		
		System.out.println(newFilepath);
		return new File(newFilepath);
	}
	
	/**
	 * Renames a folder in a filepath
	 * @param file The file to be altered
	 * @param oldName The old name of the folder
	 * @param newName The new name of the folder
	 * @return The renamed file
	 */
	public static File renameFolderInFile(File file, String oldName, String newName) {
		String[] folders = getFoldersAsStringArray(file);
		String newFilepath ="";
		
		for (String folder : folders) {
			if (folder.equals(oldName)) {
				folder = newName;
			}
			newFilepath += folder + "/";
		}
		
		return new File(newFilepath);
		
	}
	
	/**
	 * Returns the index of the src-folder inside a String-array
	 * @param folders The array of folders to search through.
	 * @return Returns index of src-folder if found, otherwise -1
	 */
	public static int getSrcFolderIndex(String[] folders) {
		int srcIndex = -1; //Indicates how deep in the filestructure the src-folder is
		int counter = 0;


		for (String folder : folders) {
			if (folder.equals("src")) {
				srcIndex = counter;
				break;
			}
			counter++;
		}
		return srcIndex;
	}
	
	/**
	 * Converts a filepath into a string-array of folder names
	 *
	 * @author Philip Boyde
	 * @param file The filepath to convert
	 * @return A string-array of folder names
	 */
	public static String[] getFoldersAsStringArray(File file) {
		String[] folders;
		String filepath = file.getAbsolutePath(); //Get the path in string
		folders = filepath.split(Pattern.quote(File.separator));// Split path into the different folders

		return folders;
	}

	/**
	 * Extracts the internal path from the given file path relative to the workspace path.
	 * Converts the internal path to a package name format.
	 *
	 * @author Philip Boyde
	 * @param filePath      The absolute file path.
	 * @param workspacePath The workspace path to be removed from the file path.
	 * @return The package name derived from the internal path, or a single space if the file path is shorter than the workspace path.
	 */
	private static String internalPathExtractor(String filePath, String workspacePath){
		int rmLength = workspacePath.length() + 1;
		StringBuilder packagename = new StringBuilder();

		if (filePath.length() > rmLength) {
			String internalPath = filePath.substring(rmLength);
			String[] folders = internalPath.split(Pattern.quote(File.separator));

			// remove the file type from file name
			String lastFolder = folders[folders.length - 1];
			int dotIndex = lastFolder.lastIndexOf('.');
			if (dotIndex != -1) {
				folders[folders.length - 1] = lastFolder.substring(0, dotIndex);
			}

			// make a string with package format e.g. (src.main.test)
			for (int i = 0; i <= (folders.length-1); i++){
				packagename.append(folders[i]);

				if (i < folders.length - 1) {
					packagename.append(".");
				}

			}

			return packagename.toString();
		} else {
			return " ";
		}
	}
}
