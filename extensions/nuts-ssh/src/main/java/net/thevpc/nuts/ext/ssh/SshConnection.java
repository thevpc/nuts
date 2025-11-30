package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.io.NPathInfo;
import net.thevpc.nuts.io.NPathType;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface SshConnection extends AutoCloseable {
    String IDENTITY_FILE = "identity-file";

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

    InputStream getInputStream(String from);

    OutputStream getOutputStream(String from);

    boolean isAlive();

    boolean isClosed();

    List<String> list(String path);

    long getContentLength(String basePath);

    String getContentEncoding(String basePath);

    String getContentType(String basePath);

    String getCharset(String basePath);

    void cp(String path, String path1, boolean b);

    List<String> walk(String path, boolean followLinks, int maxDepth);

    byte[] getDigestWithCommand(String algo, String path);

    NPathInfo getInfo(String path);

    List<NPathInfo> listInfos(String path);
}
