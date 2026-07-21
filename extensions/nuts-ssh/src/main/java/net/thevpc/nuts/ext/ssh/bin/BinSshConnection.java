package net.thevpc.nuts.ext.ssh.bin;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.ext.ssh.IOBindings;
import net.thevpc.nuts.ext.ssh.SshConnection;
import net.thevpc.nuts.ext.ssh.SshConnectionBase;
import net.thevpc.nuts.ext.ssh.SshListener;
import net.thevpc.nuts.io.*;
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
        String user = connectionString.userName();
        String host = connectionString.host();
        int port = NLiteral.of(connectionString.port()).asInt().orElse(-1);
        String keyFilePath = connectionString.builder().getQueryParam(SshConnection.IDENTITY_FILE).orNull();
        String keyPassword = connectionString.password();
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
                    NConnectionStringBuilder.of().userName(user).host(host).port(String.valueOf(port)).password(keyPassword).queryString(
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
        return NExec.ofSystem(sshCommandPrefix.toArray(new String[0]))
                .command(command)
                .failFast(true)
                .out(NExecOutput.ofStream(out))
                .err(NExecOutput.ofStream(err))
                .in(NExecInput.ofStream(in))
                .run()
                .exitCode();
    }


    @Override
    public InputStream getInputStream(String from) {
        NConnectionStringBuilder cbuilder = connectionString.builder();
        String identityFile = cbuilder.getQueryParam(SshConnection.IDENTITY_FILE).orNull();
        int port = NLiteral.of(connectionString.port()).asInt().orElse(-1);
        if (port <= 0) {
            port = 22;
        }
        if (useSftp) {
            try {
                // Create temporary batch file
                NPath batchFile = NPath.ofTempFile();

                // We rely on:   get <remote> -
                // The '-' writes file contents to stdout.
                try (OutputStream os = batchFile.outputStream()) {
                    String getCommand = "get " + escapeRemotePath(from) + " -\n";
                    os.write(getCommand.getBytes(StandardCharsets.UTF_8));
                }

                // Build remote "host" string including port + user if needed
                String target = getNConnectionStringBuilder(from)
                        .port(null)
                        .queryMap(null)
                        .path(null)
                        .build()
                        .toString();

                NExec exec = NExec.ofSystem("sftp");
                if (port != 22) {
                    exec.command("-oPort", String.valueOf(port));
                }
                if (!NBlankable.isBlank(identityFile)) {
                    exec.command("-oIdentityFile", identityFile);
                }
                exec.command("-b", batchFile.toString(),
                                target)
                        .in(NExecInput.ofNull())
                        .out(NExecOutput.ofPipe())  // capture stdout
                        .err(NExecOutput.ofNull())
                        .failFast(true)
                        .run();

                batchFile.delete();

                return exec.out().result().inputStream();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            NExec exec = NExec.ofSystem();
            exec.command("scp");
            if (port != 22) {
                exec.command("-oPort", String.valueOf(port));
            }
            if (!NBlankable.isBlank(identityFile)) {
                exec.command("-oIdentityFile", identityFile);
            }
            exec.command("-q"); // quiet
            exec.command(connectionString.builder().port(null).queryMap(null).toString());
            exec.command("-"); // output to stdout
                    exec
                    .in(NExecInput.ofNull())
                    .out(NExecOutput.ofPipe()) // capture remote file via stdout
                    .err(NExecOutput.ofNull())
                    .failFast(true)
                    .run();

            return exec.out().result().inputStream();
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
        return connectionString.builder().queryMap(null).path(path);
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
