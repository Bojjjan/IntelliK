package zenit.filesystem.jreversions;

import zenit.GetOperatingSystem;
import zenit.Zenit;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JREVersions {


	/**
	 * Creates a new JDK.dat file and writes the list of JVMs on the local computer to it.
	 * @author Philip Boyde
	 */
	public static void createNew() {

		try {
			File file = new File("res/JDK/JDK.dat");

			if (!file.exists()) {
				file.getParentFile().mkdirs();

				try {
					file.createNewFile();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}

			ArrayList<File> JVMsList = new ArrayList<File>();
			File javaFolder = getJVMDirectory();

			if (javaFolder.exists()) {
				File[] JVMs = javaFolder.listFiles();
				for (File JVM : JVMs) {
					JVMsList.add(JVM);
				}

				if (!JVMsList.isEmpty()) {
					write(JVMsList);
				}
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}


	/**
	 * Reads the list of JDK files from JDK.dat.
	 *
	 * @author Philip Boyde
	 * @return List of JDK files.
	 */
	public static List<File> read() {
		
		ArrayList<File> JDKs = new ArrayList<File>();
		File file;
		
		try (ObjectInputStream ois = new ObjectInputStream(
				new BufferedInputStream(
						new FileInputStream("res/JDK/JDK.dat")))) {


			file = (File) ois.readObject();
			
			while (file != null) {
				JDKs.add(file);
				file = (File) ois.readObject();
			}
			
		
		} catch (IOException | ClassNotFoundException e) {
			System.err.println(e.getMessage());
		}
		
		return JDKs;
	}


	/**
	 * Reads the list of JDK file names from JDK.dat.
	 *
	 * @return List of JDK file names.
	 */
	public static List<String> readString() {
		List<String> JDKsString = new ArrayList<String>();
		List<File> JDKs = read();

		if (JDKs.size() > 0) {
			for (File JDK : JDKs) {
				JDKsString.add(JDK.getName());
			}
		}

		return JDKsString;

	}


	/**
	 * Writes the list of JDK files to JDK.dat.
	 *
	 * @param files List of JDK files to write.
	 */
	public static void write(List<File> files) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
				new FileOutputStream("res/JDK/JDK.dat")))) {
			
			for (File file : files) {
				oos.writeObject(file);
			}
			oos.flush();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}


	/**
	 * Appends a JDK file to the list in JDK.dat.
	 *
	 * @param file JDK file to append.
	 * @return true if the file was appended successfully, false otherwise.
	 */
	public static boolean append(File file) {
		boolean success = false;
		
		if (JDKVerifier.validJDK(file)) {
			List<File> files = read();
			files.add(file);
			
			write(files);
			
			success = true;
		}
		
		return success;

	}

	/**
	 * Removes a JDK file from the list in JDK.dat.
	 *
	 * @param file JDK file to remove.
	 * @return true if the file was removed successfully, false otherwise.
	 */
	public static boolean remove(File file) {
		List<File> files = read();
		boolean success = files.remove(file);
		
		if (success) {
			write(files);
		}
		
		return success;
	}


	/**
	 * Gets the JVM directory based on the operating system.
	 *
	 * @author Philip Boyde
	 * @return JVM directory file.
	 */
	public static File getJVMDirectory() {
		GetOperatingSystem.OperatingSystem OS = Zenit.OS;

		return switch (OS){
			case MAC ->   new File("/library/java/javavirtualmachines");
			case LINUX -> new File("/usr/lib/jvm");
			case WINDOWS -> new File("C:\\Program Files\\Java\\");
			default -> null;
		};
	}

	/**
	 * Gets the full path of a JDK file by its name.
	 *
	 * @param name Name of the JDK file.
	 * @return Full path of the JDK file, or null if not found.
	 */
	public static String getFullPathFromName(String name) {
		List<File> JDKs = read();
		
		for (File JDK : JDKs) {
			if (JDK.getName().equals(name)) {
				return JDK.getPath();
			}
		}
		
		return null;
	}

	/**
	 * Sets the default JDK file.
	 *
	 * @param file JDK file to set as default.
	 */
	public static void setDefaultJDKFile(File file) {
		File defaultJDK = new File("res/JDK/DefaultJDK.dat");
			
		try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
				new FileOutputStream(defaultJDK)))) {

			if (!defaultJDK.exists()) {
				defaultJDK.createNewFile();
			}
			
			oos.writeObject(file);
			oos.flush();

		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Gets the default JDK file.
	 *
	 * @return Default JDK file, or null if not found.
	 */
	public static File getDefaultJDKFile() {
		
		File defaultJDK = new File("res/JDK/DefaultJDK.dat");
		
		try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(defaultJDK)))) {
			return (File) ois.readObject();
			
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
		
	}
}
