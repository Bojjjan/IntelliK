package zenit.terminal;

import com.techsenger.jeditermfx.core.TtyConnector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class LocalTtyConnector implements TtyConnector {
    private Process process;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isConnected = false;

    public LocalTtyConnector() throws IOException {
        // Choose the right command based on OS
        String[] command = System.getProperty("os.name").toLowerCase().contains("win")
                ? new String[]{"cmd.exe"}
                : new String[]{"/bin/bash", "-l"};

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        process = builder.start();
        inputStream = process.getInputStream();
        outputStream = process.getOutputStream();
        isConnected = true;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if (isConnected) {
            outputStream.write(bytes);
            outputStream.flush();
        }
    }

    @Override
    public void close() {
        if (process != null) {
            process.destroy();
        }
        isConnected = false;
    }

    @Override
    public void write(String string) throws IOException {
        outputStream.write(string.getBytes());
        outputStream.flush();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return process.waitFor();
    }

    @Override
    public boolean ready() throws IOException {
        return inputStream.available() > 0;
    }

    @Override
    public String getName() {
        return Charset.defaultCharset().name();
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        if (!isConnected) return -1;

        byte[] bytes = new byte[length];
        int len = inputStream.read(bytes, 0, length);
        if (len == -1) return -1;

        String str = new String(bytes, 0, len, getName());
        if (len > 0) {
            str.getChars(0, str.length(), buf, offset);
        }
        return str.length();
    }

    @Override
    public boolean isConnected() {
        return isConnected && process.isAlive();
    }
}