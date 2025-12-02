package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.command.NExecutableInformation;
import net.thevpc.nuts.concurrent.NScoredCallable;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.ext.ssh.bin.BinSshConnection;
import net.thevpc.nuts.ext.ssh.jcsh.JCshConnection;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.util.NLiteral;

public class ScoredConnectionFactory {
    public static NScoredCallable<SshConnection> resolveBinSshConnectionPool(NConnectionString connectionString) {
        boolean usePortable = NLiteral.of(connectionString.builder().getQueryParamValue("portable"))
                .asBoolean().orElse(false);
        boolean useJcsh = NLiteral.of(connectionString.builder().getQueryParamValue("jcsh"))
                .asBoolean().orElse(false);
        boolean useSftp = NLiteral.of(connectionString.builder().getQueryParamValue("sftp"))
                .asBoolean().orElse(false);
        boolean useScp = NLiteral.of(connectionString.builder().getQueryParamValue("scp"))
                .asBoolean().orElse(false);
        boolean useBin = NLiteral.of(connectionString.builder().getQueryParamValue("bin"))
                .asBoolean().orElse(false);
        int score = 1;
        if (useJcsh) {
            score += 200;
            useBin = false;
            usePortable = true;
        } else if (useScp || useSftp) {
            score += 100;
            useBin = true;
        } else if (usePortable) {
            score += 50;
        } else if (useBin) {
            score += 50;
        } else {
            score += 25;
        }
        NExecutableInformation ssh = null;
        NExecutableInformation scp = null;
        NExecutableInformation sftp = null;
        if (!usePortable && !useBin) {
            switch (NWorkspace.of().getOsFamily()) {
                case LINUX:
                case UNIX:
                case MACOS: {
//                    useBin = true;
                    usePortable = true;
                    break;
                }
                case WINDOWS:
                default: {
                    usePortable = true;
                    break;
                }
            }
        }
        if (useJcsh) {
            return NScoredCallable.of(score, () -> new JCshConnection(connectionString.builder().setQueryParam("use", "jcsh").build()));
        }
        if (usePortable) {
            return NScoredCallable.of(score, () -> new JCshConnection(connectionString.builder().setQueryParam("use", "jcsh").build()));
        }
        if (useBin) {
            boolean sftpChecked = false;
            boolean scpChecked = false;
            ssh = NExecCmd.ofSystem("ssh").which();
            if (ssh == null) {
                return NScoredCallable.of(score, () -> new JCshConnection(connectionString.builder().setQueryParam("use", "jcsh").build()));
            }
            if (useScp) {
                scp = NExecCmd.ofSystem("scp").which();
                scpChecked = true;
                if (scp != null) {
                    score += 25;
                    return NScoredCallable.of(score, () -> new BinSshConnection(connectionString.builder().setQueryParam("use", "scp").build(), false));
                }
            }
            if (useSftp) {
                sftp = NExecCmd.ofSystem("sftp").which();
                sftpChecked = true;
                if (sftp != null) {
                    score += 25;
                    return NScoredCallable.of(score, () -> new BinSshConnection(connectionString.builder().setQueryParam("use", "sftp").build(), true));
                }
            }
            if (!scpChecked) {
                scp = NExecCmd.ofSystem("scp").which();
                if (scp != null) {
                    score += 25;
                    return NScoredCallable.of(score, () -> new BinSshConnection(connectionString.builder().setQueryParam("use", "scp").build(), false));
                }
            }
            if (sftpChecked) {
                sftp = NExecCmd.ofSystem("sftp").which();
                if (sftp != null) {
                    score += 25;
                    return NScoredCallable.of(score, () -> new BinSshConnection(connectionString.builder().setQueryParam("use", "sftp").build(), true));
                }
            }
            return NScoredCallable.of(score, () -> new JCshConnection(connectionString.builder().setQueryParam("use", "jcsh").build()));
        }
        return NScoredCallable.of(score, () -> new JCshConnection(connectionString.builder().setQueryParam("use", "jcsh").build()));
    }

}
