package main.zenit.filesystem.jreversions;

import java.io.File;

import main.zenit.GetOperatingSystem;
import main.zenit.Zenit;

public class JDKVerifier {

	/**
	 * This method checks if the passed JDK path is valid by searching
	 * the bin folder for java.exe and javac.exe.
	 * Supports Windows and Unix-based systems.
	 *
	 * @param JDK passed JDK path -> C:\Program Files\Java\JDKOfChoice.
	 * @return true if BOTH java.exe and javac.exe were found, false if not.
	 */

	protected static boolean validJDK(File JDK) {
		File binFolder = new File(JDK, "bin");

		if (!binFolder.exists() || !binFolder.isDirectory()) {
			return false;
		}

		File javaFile = new File(binFolder, isWindows() ? "java.exe" : "java");
		File javacFile = new File(binFolder, isWindows() ? "javac.exe" : "javac");

		return javaFile.exists() && javacFile.exists();
	}

	/**
	 * Helper method to check if the OS is windows. Used to determine which filepath to look for in validJDK.
	 *
	 * @return true if the user is running a Windows system, false if not.
	 */
	private static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}



	/**
	 * Gets the executable path for a given tool within a JDK directory.
	 *
	 * @param JDKPath The path to the JDK directory.
	 * @param tool The name of the tool (e.g., "java" or "javac").
	 * @return The full path to the executable tool within the JDK directory.
	 */
	public static String getExecutablePath(String JDKPath, String tool) {
		GetOperatingSystem.OperatingSystem OS = Zenit.OS;
		String path = null;
		JDKPath = JDKPath.replace("C:", "\"C:");
		switch (OS){
			case MAC:
				path = JDKPath + File.separator + "Contents" + File.separator +
						"Home" + File.separator + "bin" + File.separator + tool;
				break;

			case LINUX, WINDOWS:
				path = JDKPath + File.separator + "bin" + File.separator + tool + "\"";
				break;

			default:
				System.err.println("Something went wrong when finding the ExecutablePath");
				break;
		}
		
		return path;
	}
}
