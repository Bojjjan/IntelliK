package main.zenit.zencodearea;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.concurrent.Task;

import org.fxmisc.richtext.model.StyleSpan;
import main.zenit.ui.FileTab;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.Token;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import main.zenit.zencodearea.ProjectController;

import generated.JavaParser;
import generated.JavaLexer;

public class ZenCodeArea extends CodeArea {
	private final ExecutorService executor;
	private ProjectController projectController;
	private JavaClassType oldJClass;
	private static final Set<String> JAVA_LANG_CLASSES = Set.of(
			"String", "Object", "System", "StringBuilder", "Thread", "Exception", "Runtime",
			"Integer", "Double", "Float", "Character", "Boolean", "Math", "Void", "Short", "Long", "Byte"
	);

	public ZenCodeArea(int textSize, String font, String sourcePath) {
		setParagraphGraphicFactory(LineNumberFactory.get(this));
		projectController = new ProjectController(sourcePath);

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
					this.replaceSelection("\"\"");
					this.moveTo(this.getCaretPosition() - 1);
				}
		);

		InputMap<KeyEvent> imParentheses = InputMap.consume(
				EventPattern.keyTyped().onlyIf(e -> e.getCharacter().equals("(")),
				e -> {
					this.replaceSelection("()");
					this.moveTo(this.getCaretPosition() - 1);
				}
		);

		InputMap<KeyEvent> imHardBrackets = InputMap.consume(
				EventPattern.keyTyped().onlyIf(e -> e.getCharacter().equals("[")),
				e -> {
					this.replaceSelection("[]");
					this.moveTo(this.getCaretPosition() - 1);
				}
		);

		Nodes.addInputMap(this, imTab);
		Nodes.addInputMap(this, imBraces);
		Nodes.addInputMap(this, imQuotes);
		Nodes.addInputMap(this, imParentheses);
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

			SemanticAnalyzer analyzer = new SemanticAnalyzer();
			ParseTreeWalker.DEFAULT.walk(analyzer, tree);

			List<Token> tokens = tokenStream.getTokens();

			Set<Integer> errorLines = new HashSet<>();
			if (errorListener.hasSyntaxErrors()) {
				for (SyntaxError error : errorListener.getSyntaxErrors()) {
					int lineNum = computeLineNumber(text, error.startIndex());
					errorLines.add(lineNum);
				}
			}

			int lastIndex = 0;
			List<StyleSpan<Collection<String>>> spans = new ArrayList<>();
			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				int startIndex = token.getStartIndex();
				int stopIndex = token.getStopIndex() + 1;

				if (startIndex > lastIndex) {
					spans.add(new StyleSpan<>(Collections.emptyList(), startIndex - lastIndex));
				}
				String baseStyle = getStyleForToken(token.getType(), token.getText(), analyzer);
				Set<String> styleSet = new HashSet<>();
				if(token.getType() == JavaLexer.IDENTIFIER && i < tokens.size() -1){
					Token next = tokens.get(i + 1);
					if(next.getType() == JavaLexer.LPAREN){
						baseStyle = "method-call";
					}
				}
				if (!baseStyle.isEmpty()) {
					styleSet.add(baseStyle);
				}

				int tokenLine = computeLineNumber(text, startIndex);
				if (errorLines.contains(tokenLine) && !token.getText().trim().isEmpty()) {
					styleSet.add("error");
				}

				spans.add(new StyleSpan<>(styleSet, stopIndex - startIndex));
				lastIndex = stopIndex;
			}
			if (text.length() > lastIndex) {
				spans.add(new StyleSpan<>(Collections.emptyList(), text.length() - lastIndex));
			}

			spansBuilder.addAll(spans);
			return spansBuilder.create();
		} catch (Exception e) {
			e.printStackTrace();
			spansBuilder.add(Collections.emptyList(), text.length());
			return spansBuilder.create();
		}
	}

	private static String getStyleForToken(int tokenType, String tokenText, SemanticAnalyzer analyzer) {
		if (tokenType == JavaLexer.IDENTIFIER && analyzer.getClassNames().contains(tokenText)) {
			Symbol symbol = ProjectController.getInstance().getSymbol(tokenText);
			if(symbol != null && symbol.getSymbolType() == Symbol.Type.CLASS){
				if(!AccessUtil.isAccessible(symbol, analyzer.getContext())){
					return "no-access";
				}else{
					return "class-name";
				}
			}
			return "class-name";
		}
		if (JAVA_LANG_CLASSES.contains(tokenText)) {
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

			case JavaLexer.IDENTIFIER -> "I";
            default -> "T";
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


	private record LineBoundary(int start, int end) { }
}