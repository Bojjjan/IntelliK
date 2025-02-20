package zenit.filesystem.jreversions;

import zenit.GetOperatingSystem;
import zenit.Zenit;

import java.io.File;

public class JDKVerifier {
	
	protected static boolean validJDK(File JDK) {
		
		File java = new File(getExecutablePath(JDK.getPath(), "java"));
		File javac = new File(getExecutablePath(JDK.getPath(), "javac"));
		
		return (java != null && javac != null && java.exists() && javac.exists());
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

		switch (OS){
			case MAC:
				path = JDKPath + File.separator + "Contents" + File.separator +
						"Home" + File.separator + "bin" + File.separator + tool;
				break;

			case LINUX, WINDOWS:
				path = JDKPath + File.separator + "bin" + File.separator + tool;
				break;

			default:
				System.err.println("Something went wrong when finding the ExecutablePath");
				break;
		}
		
		return path;
	}
}
