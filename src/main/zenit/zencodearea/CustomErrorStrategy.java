package main.zenit.zencodearea;

import org.antlr.v4.runtime.*;

public class CustomErrorStrategy extends DefaultErrorStrategy {
    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        InputMismatchException e = new InputMismatchException(recognizer);
        reportError(recognizer, e);
        return recognizer.getCurrentToken();
    }

    @Override
    public void recover(Parser recognizer, RecognitionException e) throws RecognitionException {
        recognizer.consume();
    }

    @Override
    public void sync(Parser recognizer) throws RecognitionException {

    }
}
