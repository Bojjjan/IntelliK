package zenit.terminal.pty;

import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.techsenger.jeditermfx.core.ProcessTtyConnector;
import com.techsenger.jeditermfx.core.util.TermSize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author traff
 */
public class PtyProcessTtyConnector extends ProcessTtyConnector {

    private final PtyProcess myProcess;

    public PtyProcessTtyConnector(@NotNull PtyProcess process, @NotNull Charset charset) {
        this(process, charset, null);
    }

    public PtyProcessTtyConnector(@NotNull PtyProcess process, @NotNull Charset charset, @Nullable List<String> commandLine) {
        super(process, charset, commandLine);
        myProcess = process;
    }

    @Override
    public void resize(@NotNull TermSize termSize) {
        if (isConnected()) {
            myProcess.setWinSize(new WinSize(termSize.getColumns(), termSize.getRows()));
        }
    }

    @Override
    public boolean isConnected() {
        return myProcess.isAlive();
    }

    @Override
    public String getName() {
        return "Local";
    }

    public BufferedWriter getWriter(){
        return myProcess.outputWriter();
    }


}


