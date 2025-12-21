package net.thevpc.nuts.ext.ssh.bin;

import net.thevpc.nuts.command.NExec;
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

public class BinSshFileOutputStreamScp extends OutputStream {
    private final NPath temp;
    private final NConnectionString remotePath;
    private final OutputStream tempOS;
    private final BinSshConnection connection;

    public BinSshFileOutputStreamScp(BinSshConnection connection, NConnectionString remotePath) {
        this.connection = connection;
        this.remotePath = remotePath;

        // Create local temporary file
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
        NConnectionStringBuilder cbuilder = remotePath.builder();
        String identityFile = cbuilder.getQueryParam(SshConnection.IDENTITY_FILE).orNull();
        NExec scp = NExec.ofSystem("scp", "-q", temp.toString(),
                cbuilder.setPort(null).setQueryMap(null).toString());
        int port = NLiteral.of(remotePath.getPort()).asInt().orElse(-1);
        if(port<=0){
            port=22;
        }
        if(port!=22){
            scp.addCommand("-oPort",String.valueOf(port));
        }
        if(!NBlankable.isBlank(identityFile)){
            scp.addCommand("-oIdentityFile",identityFile);
        }
        scp
                .setIn(NExecInput.ofNull())
                .setOut(NExecOutput.ofNull())
                .setErr(NExecOutput.ofNull())
                .failFast()
                .run();
    }
}
