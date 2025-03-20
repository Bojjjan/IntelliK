package zenit.zencodearea;

import javafx.beans.value.ChangeListener;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.concurrent.Task;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import zenit.ui.FileTab;
import org.antlr.v4.runtime.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.tree.ParseTree;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import generated.JavaParser;
import generated.JavaLexer;

public class ZenCodeArea extends CodeArea {
	private final ExecutorService executor;
	private static final Set<String> JAVA_LANG_CLASSES = Set.of(
			"String", "Object", "System", "StringBuilder", "Thread", "Exception", "Runtime",
			"Integer", "Double", "Float", "Character", "Boolean", "Math", "Void", "Short", "Long", "Byte"
	);

	public ZenCodeArea(int textSize, String font, String sourcePath) {
		setParagraphGraphicFactory(LineNumberFactory.get(this));
		ProjectController.getInstance().buildProjectHierarchy(sourcePath);

		this.caretPositionProperty().addListener((ChangeListener<Number>)
				(observable, oldValue, newValue) -> updateProjectContext());

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

	private void updateProjectContext(){
		String text = getText();
		int caretPos = getCaretPosition();

		Pattern pkgPattern = Pattern.compile("package\\s+([\\w.]+);");
		Matcher pkgMatcher = pkgPattern.matcher(text);
		String packageName = "";
		if (pkgMatcher.find()) {
			packageName = pkgMatcher.group(1);
		}

		Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
		Matcher classMatcher = classPattern.matcher(text);
		String currentClass = "";
		while (classMatcher.find()) {
			if (classMatcher.start() < caretPos) {
				currentClass = classMatcher.group(1);
			} else {
				break;
			}
		}
		ProjectController.getInstance().setCurrentPackage(packageName);
		ProjectController.getInstance().setCurrentClass(currentClass);
	}

	public void update() {
		applyHighlighting(computeHighlighting(getText()));
	}

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

	private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
		setStyleSpans(0, highlighting);

		InputMap<KeyEvent> imTab = InputMap.consume(
				EventPattern.keyPressed(KeyCode.TAB),
				e -> this.replaceSelection("    ")
		);

		InputMap<KeyEvent> imBraces = InputMap.consume(
				EventPattern.keyTyped().onlyIf(e -> e.getCharacter().equals("{")),
				e -> {
					int caretPos = this.getCaretPosition();
					String currentLine = this.getParagraph(this.getCurrentParagraph()).getText();
					int caretColumn = this.getCaretColumn();
					String beforeCaret = currentLine.substring(0, caretColumn);

					boolean inlineMode = beforeCaret.trim().endsWith("=");

					if (!inlineMode && !beforeCaret.trim().isEmpty()) {
						Matcher m = Pattern.compile("^(\\s*)").matcher(currentLine);
						String indent = "";
						if (m.find()) {
							indent = m.group(1);
						}
						String indentBlock = indent + "    ";
						String toInsert = "{" + "\n" + indentBlock + "\n" + indent + "}";
						this.replaceSelection(toInsert);
						this.moveTo(caretPos + 2 + indentBlock.length());
					} else {
						this.replaceSelection("{}");
						this.moveTo(this.getCaretPosition() - 1);
					}
				}
		);

		InputMap<KeyEvent> imQuotes = InputMap.consume(
				EventPattern.keyTyped().onlyIf(e -> e.getCharacter().equals("\"")),
				e -> {
					int caretPos = this.getCaretPosition();
					if (caretPos < getLength() && getText().charAt(caretPos) == '"') {
						this.moveTo(caretPos + 1);
					} else {
						this.replaceSelection("\"\"");
						this.moveTo(caretPos + 1);
					}
				}
		);

		InputMap<KeyEvent> imParentheses = InputMap.consume(
				EventPattern.keyTyped().onlyIf(e -> e.getCharacter().equals("(")),
				e -> {
					int caretPos = this.getCaretPosition();
					this.replaceSelection("()");
					this.moveTo(caretPos + 1);
				}
		);

		InputMap<KeyEvent> imCloseParentheses = InputMap.consume(
				EventPattern.keyTyped().onlyIf(e -> e.getCharacter().equals(")")),
				e -> {
					int caretPos = this.getCaretPosition();
					if (caretPos < getLength() && getText().charAt(caretPos) == ')') {
						this.moveTo(caretPos + 1);
					} else {
						this.replaceSelection(")");
					}
				}
		);

		InputMap<KeyEvent> imHardBrackets = InputMap.consume(
				EventPattern.keyTyped().onlyIf(e -> e.getCharacter().equals("[")),
				e -> {
					int caretPos = this.getCaretPosition();
					if (caretPos < getLength() && getText().charAt(caretPos) == ']') {
						this.moveTo(caretPos + 1);
					} else {
						this.replaceSelection("[]");
						this.moveTo(caretPos + 1);
					}
				}
		);

		Nodes.addInputMap(this, imTab);
		Nodes.addInputMap(this, imBraces);
		Nodes.addInputMap(this, imQuotes);
		Nodes.addInputMap(this, imParentheses);
		Nodes.addInputMap(this, imCloseParentheses);
		Nodes.addInputMap(this, imHardBrackets);
	}

	private StyleSpans<Collection<String>> computeHighlighting(String text) {
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		try {
			SyntaxError.SyntaxErrorListener errorListener = new SyntaxError.SyntaxErrorListener();
			CharStream input = CharStreams.fromString(text);
			JavaLexer lexer = new JavaLexer(input);
			lexer.removeErrorListeners();
			CommonTokenStream tokenStream = new CommonTokenStream(lexer);

			JavaParser parser = new JavaParser(tokenStream);
			parser.removeErrorListeners();
			parser.addErrorListener(errorListener);
			parser.setErrorHandler(new CustomErrorStrategy());
			ParseTree tree = parser.compilationUnit();

			SemanticAnalyzer analyzer = new SemanticAnalyzer(tokenStream);
			ParseTreeWalker.DEFAULT.walk(analyzer, tree);

			Set<Integer> errorLines = new HashSet<>();
			for (SyntaxError error : errorListener.getSyntaxErrors()) {
				if (error.startIndex() >= 0) {
					int errorLine = computeLineNumber(text, error.startIndex());
					errorLines.add(errorLine);
				}
			}

			List<Token> tokens = tokenStream.getTokens();
			int lastIndex = 0;
			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				int tokenStart = token.getStartIndex();
				int tokenEnd = token.getStopIndex() + 1;
				if (tokenStart > lastIndex) {
					spansBuilder.add(Collections.emptyList(), tokenStart - lastIndex);
				}

				String style;
				if (token.getType() == JavaLexer.IDENTIFIER) {
					if (i + 1 < tokens.size() && tokens.get(i + 1).getType() == JavaLexer.LPAREN) {
						style = "method-call";
					}
					else if (i > 0 && ".".equals(tokens.get(i - 1).getText())) {
						boolean packageFound = false;
						int currentLine = token.getLine();
						for (int j = i - 1; j >= 0; j--) {
							Token prevToken = tokens.get(j);
							if (prevToken.getLine() != currentLine) {
								break;
							}
							if ("package".equals(prevToken.getText())) {
								packageFound = true;
								break;
							}
						}
						style = packageFound ? getStyleForToken(token.getType(), token.getText())
								: "unknown-identifier";
					} else {
						ClassContext currentClass = ProjectController.getInstance().getCurrentClass();
						if (currentClass != null && token.getText().equals(currentClass.getClassName())) {
							style = currentClass.getStyle();
						} else {
							Symbol sym = ProjectController.getInstance().getSymbol(token.getText());
							if (sym != null && sym.getStyle() != null && !sym.getStyle().isEmpty()) {
								style = sym.getStyle();
							} else {
								style = getStyleForToken(token.getType(), token.getText());
							}
						}
					}
				} else {
					style = getStyleForToken(token.getType(), token.getText());
				}

				if (errorLines.contains(token.getLine())) {
					style = "error";
				}

				Set<String> styleSet = new HashSet<>();
				if (style != null && !style.isEmpty()) {
					styleSet.add(style);
				}
				spansBuilder.add(styleSet, tokenEnd - tokenStart);
				lastIndex = tokenEnd;
			}
			if (text.length() > lastIndex) {
				spansBuilder.add(Collections.emptyList(), text.length() - lastIndex);
			}
			return spansBuilder.create();
		} catch (Exception e) {
			e.printStackTrace();
			spansBuilder.add(Collections.emptyList(), text.length());
			return spansBuilder.create();
		}
	}


	private static String getStyleForToken(int tokenType, String tokenText) {

		if (JAVA_LANG_CLASSES.contains(tokenText)) {
			return "class-name";
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

	public void updateAppearance(String fontFamily, int size) {setStyle("-fx-font-family: " + fontFamily + "; -fx-font-size: " + size + ";");}


	public void setTabAssociation(FileTab tab){
	}

	private static int computeLineNumber(String text, int offset) {
		int line = 1;
		for (int i = 0; i < offset && i < text.length(); i++) {
			if (text.charAt(i) == '\n') {
				line++;
			}
		}
		return line;
	}
}