package main.zenit.zencodearea;

import generated.JavaLexer;
import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

public record SyntaxError(int startIndex, int stopIndex, String message) {

    public static class SyntaxErrorListener extends BaseErrorListener {

        private final List<SyntaxError> syntaxErrors = new ArrayList<>();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg,
                                RecognitionException e) {
            int startIndex = -1;
            int stopIndex = -1;
            if (offendingSymbol instanceof Token token) {
                startIndex = token.getStartIndex();
                stopIndex = token.getStopIndex();
            } else if (recognizer instanceof Parser parser) {
                TokenSource tokenSource = parser.getInputStream().getTokenSource();
                if (tokenSource instanceof Lexer lexer) {
                    CharStream input = lexer.getInputStream();
                    String text = input.toString();
                    startIndex = computeOffset(text, line, charPositionInLine);
                    stopIndex = startIndex;
                }
            }
            syntaxErrors.add(new SyntaxError(startIndex, stopIndex, msg));
        }

        private int computeOffset(String text, int line, int charPositionInLine) {
            String[] lines = text.split("\n", -1);
            int offset = 0;
            for (int i = 0; i < line - 1 && i < lines.length; i++) {
                offset += lines[i].length() + 1;
            }
            return offset + charPositionInLine;
        }


        public boolean hasSyntaxErrors() {
            return !syntaxErrors.isEmpty();
        }

        public List<SyntaxError> getSyntaxErrors() {
            return syntaxErrors;
        }
    }
}
