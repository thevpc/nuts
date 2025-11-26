package net.thevpc.nuts.ext.ssh;

import com.jcraft.jsch.Channel;
import net.thevpc.nuts.io.NPathType;

import java.io.InputStream;
import java.util.List;

public class ISShConnexionAdapter implements ISShConnexion {
    protected ISShConnexion connection;

    public ISShConnexionAdapter(ISShConnexion connection) {
        this.connection = connection;
    }

    @Override
    public void reset() {
        getConnection().reset();
    }

    protected ISShConnexion getConnection() {
        return connection;
    }

    @Override
    public ISShConnexion addListener(SshListener listener) {
        getConnection().addListener(listener);
        return this;
    }

    @Override
    public ISShConnexion removeListener(SshListener listener) {
        getConnection().removeListener(listener);
        return this;
    }

    @Override
    public int exec(List<String> command, IOBindings io) {
        return getConnection().exec(command, io);
    }

    @Override
    public int execArrayCommand(String[] command, IOBindings io) {
        return getConnection().execArrayCommand(command, io);
    }

    @Override
    public int mv(String from, String to) {
        return getConnection().mv(from, to);
    }

    @Override
    public IOResult execArrayCommandGrabbed(String... command) {
        return getConnection().execArrayCommandGrabbed(command);
    }

    @Override
    public IOResult execStringCommandGrabbed(String command) {
        return getConnection().execStringCommandGrabbed(command);
    }

    @Override
    public int execStringCommand(String command, IOBindings io) {
        return getConnection().execStringCommand(command, io);
    }

    @Override
    public void rm(String from, boolean R) {
        getConnection().rm(from, R);
    }

    @Override
    public NPathType type(String path) {
        return getConnection().type(path);
    }

    @Override
    public void mkdir(String from, boolean p) {
        getConnection().mkdir(from, p);
    }

    @Override
    public byte[] readRemoteFile(String from) {
        return getConnection().readRemoteFile(from);
    }

    @Override
    public void copyRemoteToLocal(String from, String to, boolean mkdir) {
        getConnection().copyRemoteToLocal(from, to, mkdir);
    }

    @Override
    public InputStream getInputStream(String from) {
        return getConnection().getInputStream(from);
    }

    @Override
    public InputStream getInputStream(String from, boolean closeConnection) {
        return getConnection().getInputStream(from, closeConnection);
    }

    @Override
    public void copyLocalToRemote(String from, String to, boolean mkdirs) {
        getConnection().copyLocalToRemote(from, to, mkdirs);
    }

    @Override
    public boolean isAlive() {
        return getConnection().isAlive();
    }

    @Override
    public boolean isClosed() {
        return getConnection().isClosed();
    }

    @Override
    public List<String> list(String path) {
        return getConnection().list(path);
    }

    @Override
    public long contentLength(String basePath) {
        return getConnection().contentLength(basePath);
    }

    @Override
    public String getContentEncoding(String basePath) {
        return getConnection().getContentEncoding(basePath);
    }

    @Override
    public String getContentType(String basePath) {
        return getConnection().getContentType(basePath);
    }

    @Override
    public String getCharset(String basePath) {
        return getConnection().getCharset(basePath);
    }

    @Override
    public void cp(String path, String path1, boolean b) {
        getConnection().cp(path, path1, b);
    }

    @Override
    public List<String> walk(String path, boolean followLinks, int maxDepth) {
        return getConnection().walk(path, followLinks, maxDepth);
    }

    @Override
    public void close() {
        getConnection().close();
    }

    @Override
    public Channel openExecChannel(String cmd) {
        return getConnection().openExecChannel(cmd);
    }

    @Override
    public byte[] getDigestWithCommand(String algo, String path) {
        return getConnection().getDigestWithCommand(algo, path);
    }
}
