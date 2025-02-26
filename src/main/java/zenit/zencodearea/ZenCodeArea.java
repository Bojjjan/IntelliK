package zenit.zencodearea;

import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.concurrent.Task;

import zenit.ui.FileTab;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.Token;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import generated.JavaParser;
import generated.JavaLexer;

/**
 * ZenCodeArea is a custom code editor component that extends CodeArea.
 * It provides syntax highlighting and semantic analysis for Java code.
 *
 * The class uses ANTLR for lexical and syntactic analysis and provides
 * asynchronous syntax highlighting to improve performance.
 *
 * The class also supports updating the appearance of the code area
 * and handling indentation with the TAB key.
 * @author Philip Boyde
 */
public class ZenCodeArea extends CodeArea {
	private final ExecutorService executor;
	private JavaClassType oldJClass;
	private FileTab tab;
	private ContextMenu contextMenu;
	private ArrayList<Character> typedChars;
	private String wordBeforeDot = null;

	/**
	 * Constructs a {@code ZenCodeArea} with the specified text size and font.
	 * <p>
	 * This constructor initializes the code area with line numbers and sets up
	 * asynchronous syntax highlighting. The highlighting task is scheduled to
	 * run after a short delay whenever the text changes, ensuring that updates
	 * do not block the UI thread. The method also initializes a single-threaded
	 * executor for background tasks and applies the given font and text size.
	 * </p>
	 *
	 * @author Philip Boyde
	 * @param textSize The size of the text in the code area.
	 * @param font The font to be used for displaying text.
	 */
	public ZenCodeArea(int textSize, String font) {
		contextMenu = new ContextMenu();
		typedChars = new ArrayList<>();
		this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.BACK_SPACE) {
				if (!typedChars.isEmpty()) {
					typedChars.remove(typedChars.size() - 1);
					showSuggestions();
				} else {
					contextMenu.hide();
				}
				event.consume(); // Prevents unwanted behavior
			}
		});

		this.addEventHandler(KeyEvent.KEY_TYPED, event -> {
			if (!event.getCharacter().isEmpty()) {
				char typedChar = event.getCharacter().charAt(0);

				if (Character.isAlphabetic(typedChar) || typedChar == '.' || typedChar == '(') {
					typedChars.add(typedChar);
					showSuggestions();
				} else if (typedChar == ' ' || typedChar == '\t') {
					wordBeforeDot = null;
					typedChars.clear();
				}
			}
		});

		setParagraphGraphicFactory(LineNumberFactory.get(this));

		// Async syntax highlighting
		multiPlainChanges().successionEnds(Duration.ofMillis(200))
				.supplyTask(this::computeHighlightingAsync)
				.awaitLatest(multiPlainChanges())  // Ensures that the task runs only after the last change is finished
				.filterMap(t -> {
					if (t.isSuccess()) return Optional.of(t.get());
					t.getFailure().printStackTrace();
					return Optional.empty();
				})
				.subscribe(this::applyHighlighting);


		executor = Executors.newSingleThreadExecutor();
		updateAppearance(font, textSize);
	}



	/**
	 * Updates the syntax highlighting of the code area.
	 */
	public void update() {
		applyHighlighting(computeHighlighting(getText()));
	}

	/**
	 * Computes syntax highlighting asynchronously.
	 * <p>
	 * This method retrieves the current text from the editor and creates a {@link Task}
	 * that runs the syntax highlighting computation in a background thread. The task
	 * is executed using the single-threaded executor to ensure that syntax highlighting
	 * does not block the UI thread.
	 * </p>
	 *
	 * @author Philip Boyde
	 * @return A {@link Task} that will compute and return the {@link StyleSpans}
	 *  containing syntax highlighting information.
	 */
	private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
		String text = getText();
		Task<StyleSpans<Collection<String>>> task = new Task<>() {
			@Override
			protected StyleSpans<Collection<String>> call() {
				return computeHighlighting(text);
			}
		};
		executor.execute(task);
		return task;
	}

	/**
	 * Applies syntax highlighting and sets up key bindings for indentation and auto-closing braces.
	 * <p>
	 * This method applies syntax highlighting to the text area using the provided {@link StyleSpans}.
	 * Additionally, it sets up custom key bindings for better code editing experience:
	 * </p>
	 * <ul>
	 *     <li><b>Tab key ({@code TAB}):</b> Inserts four spaces instead of moving focus.</li>
	 *     <li><b>Auto-closing braces (When {@code { }} is typed, it automatically inserts a matching closing brace with a space in between and moves the caret inside. )</li>
	 * </ul>
	 *
	 * @author Philip Boyde
	 * @param highlighting The {@link StyleSpans} containing syntax highlighting information.
	 */
	private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
		setStyleSpans(0, highlighting);

		// TAB Key for indentation
		InputMap<KeyEvent> imTab = InputMap.consume(
				EventPattern.keyPressed(KeyCode.TAB),
				e -> this.replaceSelection("    ")
		);

		// Auto-closing braces: When { is typed, insert "{ }" and move the caret inside
		InputMap<KeyEvent> imBraces = InputMap.consume(
				EventPattern.keyTyped().onlyIf(e -> e.getCharacter().equals("{")),
				e -> {
					this.replaceSelection("{ }");
					this.moveTo(this.getCaretPosition() - 2); // Move caret inside the braces
				}
		);


		Nodes.addInputMap(this, imTab);
		Nodes.addInputMap(this, imBraces);
	}

	/**
	 * Computes syntax highlighting for a given Java source code snippet.
	 * <p>
	 * This method tokenizes the input text, analyzes its semantic structure using an ANTLR-based
	 * parser, and assigns appropriate syntax highlighting styles to each token. The highlighting
	 * information is returned as a {@link StyleSpans} object, where each span is associated
	 * with a collection of style classes.
	 * </p>
	 *
	 * @author Philip Boyde
	 * @param text The Java source code to be highlighted.
	 * @return A {@link StyleSpans} object containing style information for syntax highlighting.
	 */
	private StyleSpans<Collection<String>> computeHighlighting(String text) {
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		List<Token> tokens = tokenize(text);

		// Analyze code for semantic information
		SemanticAnalyzer analyzer = new SemanticAnalyzer();
		CharStream input = CharStreams.fromString(text);
		JavaLexer lexer = new JavaLexer(input);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		JavaParser parser = new JavaParser(tokenStream);
		ParseTreeWalker.DEFAULT.walk(analyzer, parser.compilationUnit());

		int lastIndex = 0;
		for (Token token : tokens) {
			String styleClass = getStyleForToken(token.getType(), token.getText(), analyzer);

			int startIndex = token.getStartIndex();
			int stopIndex = token.getStopIndex() + 1;
			spansBuilder.add(Collections.emptyList(), startIndex - lastIndex);
			spansBuilder.add(Collections.singleton(styleClass), stopIndex - startIndex);
			lastIndex = stopIndex;
		}

		spansBuilder.add(Collections.emptyList(), text.length() - lastIndex);

		return spansBuilder.create();
	}

	/**
	 * Tokenizes a given Java source code string.
	 * <p>
	 * This method uses ANTLR to tokenize the provided Java source code. It creates a lexer
	 * and parser, processes the input, and returns a list of tokens. Additionally, it
	 * performs semantic analysis by walking the abstract syntax tree (AST).
	 * </p>
	 *
	 * @author Philip Boyde
	 * @param code The Java source code to tokenize.
	 * @return A {@link List} of {@link Token} objects representing the lexical tokens in the source code.
	 */
	private static List<Token> tokenize(String code) {
		CharStream input = CharStreams.fromString(code);
		JavaLexer lexer = new JavaLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaParser parser = new JavaParser(tokens);

		parser.removeErrorListeners(); // Disable default error handling

		ParseTree tree = parser.compilationUnit(); // Start parsing
		SemanticAnalyzer analyzer = new SemanticAnalyzer();
		ParseTreeWalker.DEFAULT.walk(analyzer, tree); // Walk the AST

		return tokens.getTokens(); // Return the tokenized list
	}

	/**
	 * Determines the syntax highlighting style for a given token.
	 * <p>
	 * This method assigns a CSS style class based on the token's type and text.
	 * It first checks if the token represents a class name, method name, or variable
	 * using the provided {@link SemanticAnalyzer}. If not, it categorizes the token
	 * based on its type using a {@code switch} statement.
	 * </p>
	 *
	 * <p><b>Token Categories:</b></p>
	 * <ul>
	 *     <li><b>Class Names:</b> Tokens that match known class names are styled as {@code "class-name"}.</li>
	 *     <li><b>Method Names:</b> Tokens that match known method names are styled as {@code "method-name"}.</li>
	 *     <li><b>Variables:</b> Tokens that match known variable names are styled as {@code "variable"}.</li>
	 *     <li><b>Access Modifiers:</b> {@code public, private, protected, static, final}, etc., are styled as {@code "access-modifier"}.</li>
	 *     <li><b>Keywords:</b> Control flow and Java-specific keywords (e.g., {@code if, else, while}) are styled as {@code "keyword"}.</li>
	 *     <li><b>Data Types:</b> Primitive types (e.g., {@code int, boolean, double}) are styled as {@code "datatype"}.</li>
	 *     <li><b>Strings:</b> String and character literals are styled as {@code "strings"}.</li>
	 *     <li><b>Literals:</b> Numeric and boolean literals are styled as {@code "literal"}.</li>
	 *     <li><b>Operators:</b> Arithmetic, logical, and bitwise operators are styled as {@code "operator"}.</li>
	 *     <li><b>Brackets:</b> Parentheses, curly braces, and square brackets are styled as {@code "bracket"}.</li>
	 *     <li><b>Comments:</b> Line and block comments are styled as {@code "comment"}.</li>
	 *     <li><b>Identifiers:</b> Other identifiers that do not match specific categories are styled as {@code "identifier"}.</li>
	 *     <li><b>Default:</b> Any unrecognized token is assigned the {@code "default"} style.</li>
	 * </ul>
	 *
	 * @author Philip Boyde
	 * @param tokenType   The type of the token, as defined by {@link JavaLexer}.
	 * @param tokenText   The text representation of the token.
	 * @param analyzer    The {@link SemanticAnalyzer} used to identify class names, method names, and variables.
	 * @return A string representing the CSS style class for syntax highlighting.
	 */
	private static String getStyleForToken(int tokenType, String tokenText, SemanticAnalyzer analyzer) {
		if (analyzer.getClassNames().contains(tokenText)) {
			return "class-name";
		} else if (analyzer.getMethodNames().contains(tokenText)) {
			return "method-name";
		} else if (analyzer.getVariables().contains(tokenText)) {
			return "variable";
		}

		return switch (tokenType) {
			case JavaLexer.PUBLIC, JavaLexer.PRIVATE, JavaLexer.PROTECTED, JavaLexer.STATIC,
				 JavaLexer.CLASS, JavaLexer.EXTENDS, JavaLexer.FINAL, JavaLexer.SUPER, JavaLexer.THIS,
				 JavaLexer.VOLATILE, JavaLexer.PACKAGE, JavaLexer.BOOL_LITERAL, JavaLexer.NULL_LITERAL,
				 JavaLexer.VOID, JavaLexer.ENUM, JavaLexer.INTERFACE, JavaLexer.IMPORT -> "access-modifier";

			case JavaLexer.ABSTRACT, JavaLexer.ASSERT, JavaLexer.BREAK,
				 JavaLexer.CASE, JavaLexer.CATCH, JavaLexer.CONST, JavaLexer.CONTINUE,
				 JavaLexer.DEFAULT, JavaLexer.DO, JavaLexer.ELSE, JavaLexer.FINALLY, JavaLexer.FOR,
				 JavaLexer.IF, JavaLexer.GOTO, JavaLexer.IMPLEMENTS, JavaLexer.INSTANCEOF,
				 JavaLexer.NATIVE, JavaLexer.NEW, JavaLexer.RETURN, JavaLexer.STRICTFP, JavaLexer.SWITCH,
				 JavaLexer.SYNCHRONIZED, JavaLexer.THROW, JavaLexer.THROWS, JavaLexer.TRANSIENT, JavaLexer.TRY,
				 JavaLexer.WHILE, JavaLexer.MODULE, JavaLexer.OPEN, JavaLexer.REQUIRES, JavaLexer.EXPORTS, JavaLexer.OPENS,
				 JavaLexer.TO, JavaLexer.USES, JavaLexer.PROVIDES, JavaLexer.WITH, JavaLexer.TRANSITIVE,
				 JavaLexer.YIELD, JavaLexer.RECORD, JavaLexer.SEALED, JavaLexer.PERMITS, JavaLexer.NON_SEALED -> "keyword";

			case JavaLexer.BOOLEAN, JavaLexer.BYTE, JavaLexer.CHAR, JavaLexer.DOUBLE, JavaLexer.FLOAT,
				 JavaLexer.INT, JavaLexer.LONG, JavaLexer.SHORT, JavaLexer.VAR -> "datatype";

			case  JavaLexer.CHAR_LITERAL, JavaLexer.STRING_LITERAL, JavaLexer.TEXT_BLOCK  -> "strings";

			case JavaLexer.DECIMAL_LITERAL, JavaLexer.HEX_LITERAL, JavaLexer.OCT_LITERAL,
				 JavaLexer.BINARY_LITERAL, JavaLexer.FLOAT_LITERAL, JavaLexer.HEX_FLOAT_LITERAL -> "literal";

			case JavaLexer.ASSIGN, JavaLexer.GT, JavaLexer.LT, JavaLexer.BANG, JavaLexer.TILDE,
				 JavaLexer.QUESTION, JavaLexer.COLON, JavaLexer.EQUAL, JavaLexer.LE, JavaLexer.GE,
				 JavaLexer.NOTEQUAL, JavaLexer.AND, JavaLexer.OR, JavaLexer.INC, JavaLexer.DEC, JavaLexer.ADD,
				 JavaLexer.SUB, JavaLexer.MUL, JavaLexer.DIV, JavaLexer.BITAND, JavaLexer.BITOR, JavaLexer.CARET,
				 JavaLexer.MOD, JavaLexer.ADD_ASSIGN, JavaLexer.SUB_ASSIGN, JavaLexer.MUL_ASSIGN,
				 JavaLexer.DIV_ASSIGN, JavaLexer.AND_ASSIGN, JavaLexer.OR_ASSIGN, JavaLexer.XOR_ASSIGN,
				 JavaLexer.MOD_ASSIGN, JavaLexer.LSHIFT_ASSIGN, JavaLexer.RSHIFT_ASSIGN,
				 JavaLexer.URSHIFT_ASSIGN, JavaLexer.ARROW, JavaLexer.COLONCOLON, JavaLexer.AT,
				 JavaLexer.ELLIPSIS -> "operator";

			case JavaLexer.LPAREN, JavaLexer.RPAREN, JavaLexer.LBRACE, JavaLexer.RBRACE,
				 JavaLexer.LBRACK, JavaLexer.RBRACK -> "bracket";

			case JavaLexer.LINE_COMMENT, JavaLexer.COMMENT -> "comment";

			case JavaLexer.IDENTIFIER -> "identifier";
			default -> "default";
		};
	}

	/**
	 * Updates the appearance of the code area with the specified font family and size.
	 *
	 * @param fontFamily the font family
	 * @param size the font size
	 */
	public void updateAppearance(String fontFamily, int size) {setStyle("-fx-font-family: " + fontFamily + "; -fx-font-size: " + size + ";");}

	public void setTabAssociation(FileTab tab){this.tab =  tab;}

	private void showSuggestions() {
		if (typedChars.isEmpty()) {
			displaySuggestions(Collections.emptyList()); // Clear suggestions if no characters are typed
			return;
		}
		String currentWord = getCurrentWord();
		System.out.println("Current Word: " + currentWord);

		char lastTypedChar = typedChars.get(typedChars.size() - 1);

		if (lastTypedChar == '.' || lastTypedChar == '(') {
			wordBeforeDot = getCurrentWord();
			typedChars.clear();
			System.out.println("Word before dot: " + wordBeforeDot);
			currentWord = wordBeforeDot;
		}
		List<String> suggestions = getSuggestionsForWord(currentWord);

		displaySuggestions(suggestions);
	}

	private String getCurrentWord() {
		StringBuilder word = new StringBuilder();

		for (Character c : typedChars) {
			word.append(c);
		}

		System.out.println(word + " 2");
		return word.toString();
	}

	private List<String> getSuggestionsForWord(String word) {
		List<String> suggestions = new ArrayList<>();

		if (wordBeforeDot == null) {
			if (word.startsWith("S")) {
				suggestions.add("System");
				suggestions.add("String");
				suggestions.add("Scanner");
				suggestions.add("static");
			}
			if (word.startsWith("i")) {
				suggestions.add("int");
				suggestions.add("if()");
				suggestions.add("Integer");
				suggestions.add("import");
			}
			if (word.startsWith("f")) {
				suggestions.add("for()");
				suggestions.add("float");
				suggestions.add("final");
				suggestions.add("false");
				suggestions.add("File");
				suggestions.add("FileReader");
				suggestions.add("FileWriter");
			}
			if (word.startsWith("w")) {
				suggestions.add("while()");
				suggestions.add("Window");
				suggestions.add("wait()");
			}
			if (word.startsWith("d")) {
				suggestions.add("double");
				suggestions.add("do");
				suggestions.add("default");
			}
			if (word.startsWith("c")) {
				suggestions.add("char");
				suggestions.add("class");
				suggestions.add("continue");
				suggestions.add("catch()");
				suggestions.add("case");
			}
			if (word.startsWith("b")) {
				suggestions.add("boolean");
				suggestions.add("break");
				suggestions.add("byte");
				suggestions.add("BufferedReader");
				suggestions.add("BufferedWriter");
			}
			if (word.startsWith("p")) {
				suggestions.add("psvm");
				suggestions.add("public");
				suggestions.add("protected");
				suggestions.add("private");
				suggestions.add("package");
			}
			if (word.startsWith("r")) {
				suggestions.add("return");
				suggestions.add("Runnable");
				suggestions.add("Random");
			}
			if (word.startsWith("t")) {
				suggestions.add("true");
				suggestions.add("this");
				suggestions.add("Thread");
				suggestions.add("try");
			}
			if (word.startsWith("e")) {
				suggestions.add("else");
				suggestions.add("enum");
				suggestions.add("Exception");
				suggestions.add("extends");
			}
			if (word.startsWith("v")) {
				suggestions.add("void");
				suggestions.add("volatile");
				suggestions.add("var");
			}
			if (word.startsWith("n")) {
				suggestions.add("null");
				suggestions.add("new");
				suggestions.add("Native");
			}
		} else {
			if (wordBeforeDot.equals("System.")) {
				suggestions.add("out");
				suggestions.add("err");
				suggestions.add("gc()");
				suggestions.add("exit()");
				suggestions.add("currentTimeMillis()");
			}
			if (wordBeforeDot.equals("out.") || wordBeforeDot.equals("err.")) {
				suggestions.add("println()");
				suggestions.add("print()");
				suggestions.add("printf()");
				suggestions.add("flush()");
			}
			if (wordBeforeDot.equals("String.")) {
				suggestions.add("length()");
				suggestions.add("charAt()");
				suggestions.add("substring()");
				suggestions.add("indexOf()");
				suggestions.add("equals()");
				suggestions.add("equalsIgnoreCase()");
				suggestions.add("toLowerCase()");
				suggestions.add("toUpperCase()");
				suggestions.add("trim()");
				suggestions.add("replace()");
				suggestions.add("contains()");
			}
			if (wordBeforeDot.equals("Integer.")) {
				suggestions.add("parseInt()");
				suggestions.add("valueOf()");
				suggestions.add("toString()");
				suggestions.add("bitCount()");
			}
			if (wordBeforeDot.equals("Math.")) {
				suggestions.add("abs()");
				suggestions.add("ceil()");
				suggestions.add("floor()");
				suggestions.add("max()");
				suggestions.add("min()");
				suggestions.add("pow()");
				suggestions.add("sqrt()");
				suggestions.add("random()");
			}
			if (wordBeforeDot.equals("Thread.")) {
				suggestions.add("start()");
				suggestions.add("run()");
				suggestions.add("sleep()");
				suggestions.add("join()");
				suggestions.add("interrupt()");
			}
			if (wordBeforeDot.equals("File.")) {
				suggestions.add("createNewFile()");
				suggestions.add("delete()");
				suggestions.add("exists()");
				suggestions.add("getAbsolutePath()");
				suggestions.add("getName()");
				suggestions.add("isDirectory()");
				suggestions.add("isFile()");
				suggestions.add("length()");
			}
		}

		return suggestions;
	}

	private void displaySuggestions(List<String> suggestions) {
		contextMenu.hide();
		contextMenu.getItems().clear();

		for (String suggestion : suggestions) {
			MenuItem item = new MenuItem(suggestion);
			item.setOnAction(event -> {
				replaceCurrentWordWithSuggestion(suggestion);
			});
			contextMenu.getItems().add(item);
		}

		Optional<Bounds> optBounds = getCaretBounds();
		if (optBounds.isPresent()) {
			Bounds wordBounds = optBounds.get();
			contextMenu.show(this, wordBounds.getMinX(), wordBounds.getMinY() + 20);
		}
	}

	private void replaceCurrentWordWithSuggestion(String suggestion) {
		int caretPosition = getCaretPosition();
		int start = caretPosition;

		while (start > 0 && Character.isLetterOrDigit(getText().charAt(start - 1))) {
			start--;
		}
		if(Objects.equals(suggestion, "psvm")){
			replaceText(start, caretPosition, "public static void main(String[] args) {\n" +
					"\t\t\n" +
					"\t}");
		}
		else{
			replaceText(start, caretPosition, suggestion);
		}
	}


}