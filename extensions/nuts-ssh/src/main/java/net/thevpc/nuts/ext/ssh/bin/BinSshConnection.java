package net.thevpc.nuts.ext.ssh.bin;

import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.ext.ssh.IOBindings;
import net.thevpc.nuts.ext.ssh.SshConnection;
import net.thevpc.nuts.ext.ssh.SshConnectionBase;
import net.thevpc.nuts.ext.ssh.SshListener;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.net.DefaultNConnectionStringBuilder;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NStringMapFormat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BinSshConnection extends SshConnectionBase {
    private boolean closed;
    private boolean useSftp = false;
    private List<String> sshCommandPrefix = new ArrayList<>();

    public BinSshConnection(NConnectionString connectionString, boolean useSftp) {
        this.useSftp = useSftp;
        init(connectionString);
    }


    private void init(NConnectionString connectionString) {
        this.connectionString = connectionString;
        String user = connectionString.getUserName();
        String host = connectionString.getHost();
        int port = NLiteral.of(connectionString.getPort()).asInt().orElse(-1);
        String keyFilePath = connectionString.builder().getQueryParamValue(SshConnection.IDENTITY_FILE).orNull();
        String keyPassword = connectionString.getPassword();
        if (port <= 0) {
            port = 22;
        }
        sshCommandPrefix.add("ssh");
        if (!NBlankable.isBlank(keyFilePath)) {
            sshCommandPrefix.add("-i");
            sshCommandPrefix.add(keyFilePath);
        }
        if (keyPassword != null) {
            //TODO not supported, just ignore...
        }
        if (NBlankable.isBlank(user)) {
            user = System.getProperty("user.name");
        }
        if (port != 22) {
            sshCommandPrefix.add("-p");
            sshCommandPrefix.add(String.valueOf(port));
        }
        sshCommandPrefix.add(user + "@" + host);
        if (false) {
            throw new UncheckedIOException(new IOException("unable to run ssh command (" +
                    new DefaultNConnectionStringBuilder().setUserName(user).setHost(host).setPort(String.valueOf(port)).setPassword(keyPassword).setQueryString(
                            keyFilePath == null ? null : NStringMapFormat.URL_FORMAT
                                    .format(
                                            NMaps.of(SshConnection.IDENTITY_FILE, keyFilePath)
                                    )
                    ) + ")"));
        }

    }


    @Override
    public int execStringCommand(String command, IOBindings io) {
        if (io == null) {
            io = new IOBindings(null, null, null);
        }
        OutputStream out = new net.thevpc.nuts.io.NonClosableOutputStream(io.out() == null ? NullOutputStream.INSTANCE : io.out());
        OutputStream err = (io.out() == io.err()) ? out : new net.thevpc.nuts.io.NonClosableOutputStream(io.err() == null ? NullOutputStream.INSTANCE : io.err());
        InputStream in = io.in() == null ? new ByteArrayInputStream(new byte[0]) : new net.thevpc.nuts.io.NonClosableInputStream(io.in());

        int status = 205;
        for (SshListener listener : listeners) {
            listener.onExec(command);
        }
        return NExecCmd.ofSystem(sshCommandPrefix.toArray(new String[0]))
                .addCommand(command)
                .failFast()
                .setOut(NExecOutput.ofStream(out))
                .setErr(NExecOutput.ofStream(err))
                .setIn(NExecInput.ofStream(in))
                .run()
                .getResultCode();
    }


    @Override
    public InputStream getInputStream(String from) {
        NConnectionStringBuilder cbuilder = connectionString.builder();
        String identityFile = cbuilder.getQueryParamValue(SshConnection.IDENTITY_FILE).orNull();
        int port = NLiteral.of(connectionString.getPort()).asInt().orElse(-1);
        if (port <= 0) {
            port = 22;
        }
        if (useSftp) {
            try {
                // Create temporary batch file
                NPath batchFile = NPath.ofTempFile();

                // We rely on:   get <remote> -
                // The '-' writes file contents to stdout.
                try (OutputStream os = batchFile.getOutputStream()) {
                    String getCommand = "get " + escapeRemotePath(from) + " -\n";
                    os.write(getCommand.getBytes(StandardCharsets.UTF_8));
                }

                // Build remote "host" string including port + user if needed
                String target = getNConnectionStringBuilder(from)
                        .setPort(null)
                        .setQueryMap(null)
                        .setPath(null)
                        .build()
                        .toString();

                NExecCmd exec = NExecCmd.ofSystem("sftp");
                if (port != 22) {
                    exec.addCommand("-oPort", String.valueOf(port));
                }
                if (!NBlankable.isBlank(identityFile)) {
                    exec.addCommand("-oIdentityFile", identityFile);
                }
                exec.addCommand("-b", batchFile.toString(),
                                target)
                        .setIn(NExecInput.ofNull())
                        .setOut(NExecOutput.ofPipe())  // capture stdout
                        .setErr(NExecOutput.ofNull())
                        .failFast()
                        .run();

                batchFile.delete();

                return exec.getOut().getResult().getInputStream();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            NExecCmd exec = NExecCmd.ofSystem();
            exec.addCommand("scp");
            if (port != 22) {
                exec.addCommand("-oPort", String.valueOf(port));
            }
            if (!NBlankable.isBlank(identityFile)) {
                exec.addCommand("-oIdentityFile", identityFile);
            }
            exec.addCommand("-q"); // quiet
            exec.addCommand(connectionString.builder().setPort(null).setQueryMap(null).toString());
            exec.addCommand("-"); // output to stdout
                    exec
                    .setIn(NExecInput.ofNull())
                    .setOut(NExecOutput.ofPipe()) // capture remote file via stdout
                    .setErr(NExecOutput.ofNull())
                    .failFast()
                    .run();

            return exec.getOut().getResult().getInputStream();
        }
    }

    private String escapeRemotePath(String p) {
        // minimalist but enough for sftp batch mode
        if (p.contains(" ")) {
            return "\"" + p + "\"";
        }
        return p;
    }

    private NConnectionStringBuilder getNConnectionStringBuilder(String path) {
        return connectionString.builder().setQueryMap(null).setPath(path);
    }

    @Override
    public OutputStream getOutputStream(String path) {
        if (useSftp) {
            return new BinSshFileOutputStreamSftp(this, getNConnectionStringBuilder(path).build());
        }
        return new BinSshFileOutputStreamScp(this, getNConnectionStringBuilder(path).build());
    }


    @Override
    public boolean isAlive() {
        return !closed;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public void close() {
        if (!closed) {
            closed = true;
        }
    }

    public InputStream prepareStream(File file) throws FileNotFoundException {
        FileInputStream in = new FileInputStream(file);
        NMsg path = NMsg.ofStyledPath(file.getPath());
        for (SshListener listener : listeners) {
            InputStream v = listener.monitorInputStream(in, file.length(), path);
            if (v != null) {
                return v;
            }
        }
        return in;
    }

}
