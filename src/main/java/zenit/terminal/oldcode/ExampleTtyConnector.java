package zenit.terminal.oldcode;

import com.techsenger.jeditermfx.core.TtyConnector;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

public class ExampleTtyConnector implements TtyConnector {

    private final PipedReader myReader;
    private final PipedWriter myWriter;

    public ExampleTtyConnector(@NotNull PipedWriter writer) {
        try {
            this.myWriter = writer;  // Store the writer to allow writing input back
            myReader = new PipedReader(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        return myReader.read(buf, offset, length);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        String input = new String(bytes);
        myWriter.write(input);
    }


    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void write(String string) throws IOException {
        System.out.print(string); // Print input to console for debugging
        myWriter.write(string);
        myWriter.flush();
    }

    @Override
    public int waitFor() {
        return 0;
    }

    @Override
    public boolean ready() throws IOException {
        return myReader.ready();
    }
}