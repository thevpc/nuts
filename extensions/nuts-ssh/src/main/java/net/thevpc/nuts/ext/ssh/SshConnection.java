package net.thevpc.nuts.ext.ssh;

import com.jcraft.jsch.Channel;
import net.thevpc.nuts.io.NPathType;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface SshConnection extends AutoCloseable {
    void reset();

    void close();

    SshConnection addListener(SshListener listener);

    SshConnection removeListener(SshListener listener);

    int execListCommand(List<String> command, IOBindings io);

    int execArrayCommand(String[] command, IOBindings io);

    int mv(String from, String to);

    IOResult execArrayCommandGrabbed(String... command);

    IOResult execStringCommandGrabbed(String command);

    int execStringCommand(String command, IOBindings io);

    void rm(String from, boolean R);

    NPathType type(String path);

    void mkdir(String from, boolean p);

//    byte[] readRemoteFile(String from);

//    void copyRemoteToLocal(String from, String to, boolean mkdir);

    InputStream getInputStream(String from);

    OutputStream getOutputStream(String from);

//    void copyLocalToRemote(String from, String to, boolean mkdirs);

    boolean isAlive();

    boolean isClosed();

    List<String> list(String path);

    long contentLength(String basePath);

    String getContentEncoding(String basePath);

    String getContentType(String basePath);

    String getCharset(String basePath);

    void cp(String path, String path1, boolean b);

    List<String> walk(String path, boolean followLinks, int maxDepth);

    Channel openExecChannel(String cmd);

    byte[] getDigestWithCommand(String algo, String path);
}
