package net.thevpc.nuts.ext.ssh.bin;

import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.ext.ssh.SshConnection;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BinSshFileOutputStreamSftp extends OutputStream {
    private final NPath temp;
    private final NConnectionString remotePath;
    private final OutputStream tempOS;
    private final BinSshConnection connection;

    public BinSshFileOutputStreamSftp(BinSshConnection connection, NConnectionString remotePath)  {
        this.connection = connection;
        this.remotePath = remotePath;

        // Local temporary file
        this.temp = NPath.ofTempFile();
        this.tempOS = this.temp.getOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        tempOS.write(b);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        tempOS.write(buffer, offset, length);
    }

    @Override
    public void flush() throws IOException {
        tempOS.flush();
    }

    @Override
    public void close() throws IOException {
        IOException error = null;

        try {
            tempOS.close();
        } catch (IOException e) {
            error = e;
        }

        try {
            uploadTempFile();
        } catch (IOException e) {
            if (error != null) {
                error.addSuppressed(e);
                throw error;
            }
            throw e;
        } finally {
            try {
                temp.delete();
            } catch (Exception ignored) {
            }
        }

        if (error != null) throw error;
    }

    private void uploadTempFile() throws IOException {
        NPath batchFile = NPath.ofTempFile();
        try (OutputStream os = batchFile.getOutputStream()) {
            String putCommand = "put " + temp.toString() + " " + remotePath.getPath() + "\n";
            os.write(putCommand.getBytes(StandardCharsets.UTF_8));
        }

        try {
            NConnectionStringBuilder cbuilder = remotePath.builder();
            String identityFile = cbuilder.getQueryParamValue(SshConnection.IDENTITY_FILE).orNull();
            int port = NLiteral.of(remotePath.getPort()).asInt().orElse(-1);
            NExecCmd sftp = NExecCmd.ofSystem("sftp");
            if (port > 0 && port != 22) {
                sftp.addCommand("-oPort");
                sftp.addCommand(String.valueOf(port));
            }
            if(!NBlankable.isBlank(identityFile)){
                sftp.addCommand("-oIdentityFile",identityFile);
            }
            sftp.addCommand("-b", batchFile.toString(),
                            cbuilder.setQueryMap(null).setPort(null).setPath(null).build().toString())
                    .setIn(NExecInput.ofNull())
                    .setOut(NExecOutput.ofNull())
                    .setErr(NExecOutput.ofNull())
                    .failFast()
                    .run();
        } finally {
            batchFile.delete();
        }
    }
}
