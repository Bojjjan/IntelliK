package main.zenit.filesystem.helpers;

import main.zenit.exceptions.TypeCodeException;

/**
 * Snippets of code to insert into classes. Accessed via newSnippet method and constants.
 * @author Alexander Libot, Philip Boyde
 *
 */
public class CodeSnippets {
	
	public static final int EMPTY = 99;
	public static final int CLASS = 100;
	public static final int INTERFACE = 101;
	public static final int ENUM = 102;
	public static final int EXCEPTION = 103;
	private static boolean createPackage = false;

	public static String newSnippet(int typeCode, String classname, String packagename) throws TypeCodeException {
		createPackage = packagename != null;

		StringBuilder st = new StringBuilder();
		if (createPackage) { st.append("package ").append(packagename).append(";\n"); }

		return switch (typeCode) {
			case (EMPTY) -> "";
			case (CLASS) -> newClass(classname, st);
			case (INTERFACE) -> newInterface(classname, st);
			case (ENUM) -> newEnum(classname, st);
			case (EXCEPTION) -> newException(classname, st);
			default -> throw new TypeCodeException();
		};
	}

	/**
	 * Template code for new class
	 * @param classname The name of the class
	 * @param st Stringbuilder which appends the code snippet
	 * @return Created code
	 */
	private static String newClass (String classname, StringBuilder st) {

		int index = classname.indexOf(".java");
		classname = classname.substring(0, index);

		st.append("\n").
				append("public class ").append(classname). append(" {\n").
				append("\n").
				append("}");

        return st.toString();
	}
	
	/**
	 * Template code for new interface
	 * @param classname The name of the interface
	 * @param st Stringbuilder which appends the code snippet
	 * @return Created code
	 */
	private static String newInterface (String classname, StringBuilder st) {
		
		int index = classname.indexOf(".java");
		classname = classname.substring(0, index);

		st.append("\n").
				append("public interface ").append(classname). append(" {\n").
				append("\n").
				append("}");

		return st.toString();
	}

	/**
	 * Template code for new Enum
	 * @param classname The name of the Enum
	 * @param st Stringbuilder which appends the code snippet
	 * @return Created code
	 */
	private static String newEnum (String classname, StringBuilder st) {

		int index = classname.indexOf(".java");
		classname = classname.substring(0, index);

		st.append("\n").
				append("public enum ").append(classname). append(" {\n").
				append("\n").
				append("}");

		return st.toString();
	}

	/**
	 * Template code for new Exception
	 * @param classname The name of the Exception
	 * @param st Stringbuilder which appends the code snippet
	 * @return Created code
	 */
	private static String newException (String classname, StringBuilder st) {

		int index = classname.indexOf(".java");
		classname = classname.substring(0, index);

		st.append("\n").
				append("public class ").append(classname). append(" extends RuntimeException {\n").
				append("public ").append(classname).append(" (String message) {\n").
				append("     super(message);\n").
				append( "   }\n").
				append("}");

		return st.toString();
	}
}
