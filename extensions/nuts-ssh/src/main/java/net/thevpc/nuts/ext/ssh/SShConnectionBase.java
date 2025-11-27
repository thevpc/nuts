package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.io.NPathType;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.util.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SShConnectionBase implements SshConnection {
    protected List<SshListener> listeners = new ArrayList<>();
    protected NConnectionString connectionString;
    private OsProbeInfo probedInfo;

    public SShConnectionBase() {
    }

    @Override
    public SshConnection addListener(SshListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    @Override
    public SshConnection removeListener(SshListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
        return this;
    }

    @Override
    public int execListCommand(List<String> command, IOBindings io) {
        return execArrayCommand(command.toArray(new String[0]), io);
    }

    @Override
    public int execArrayCommand(String[] command, IOBindings io) {
        String sb = cmdArrayToString(command);
        return execStringCommand(sb, io);
    }

    protected String cmdArrayToString(String[] command) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.length; i++) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            String s = command[i];
            sb.append(doEscapeArg(s));
        }
        return sb.toString();
    }

    protected String doEscapeArg(String str) {
        if (str.isEmpty()) {
            return "\"\"";
        }
        StringBuilder notEscaped = new StringBuilder();
        StringBuilder escaped = new StringBuilder();
        boolean escape = false;
        for (char c : str.toCharArray()) {
            if (escape) {
                switch (c) {
                    case '\\':
                    case '\"': {
                        escaped.append("\\");
                        escaped.append(c);
                        break;
                    }
                    default: {
                        escaped.append(c);
                        break;
                    }
                }
            } else {
                if (Character.isWhitespace(c)) {
                    escape = true;
                    escaped.append(notEscaped);
                    escaped.append(c);
                    notEscaped.delete(0, notEscaped.length());
                } else {
                    switch (c) {
                        case '\\':
                        case '\"': {
                            escape = true;
                            escaped.append(notEscaped);
                            escaped.append("\\");
                            escaped.append(c);
                            notEscaped.delete(0, notEscaped.length());
                            break;
                        }
                        case '\'': {
                            escape = true;
                            escaped.append(notEscaped);
                            escaped.append(c);
                            notEscaped.delete(0, notEscaped.length());
                            break;
                        }
                        default: {
                            notEscaped.append(c);
                            break;
                        }
                    }
                }
            }
        }
        if (escape) {
            escaped.append("\"");
            return escaped.toString();
        } else {
            return notEscaped.toString();
        }
    }

    @Override
    public int mv(String from, String to) {
        return execArrayCommandGrabbed("mv", from, to).code();
    }

    @Override
    public IOResult execArrayCommandGrabbed(String... command) {
        String sb = cmdArrayToString(command);
        return execStringCommandGrabbed(sb);
    }

    @Override
    public IOResult execStringCommandGrabbed(String command) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        int r = execStringCommand(command, new IOBindings(
                null, bos,
                berr
        ));
        return new IOResult(
                r,
                bos.toByteArray(),
                berr.toByteArray()
        );
    }

    @Override
    public void rm(String from, boolean R) {
        NOsFamily nOsFamily = resolveOsFamily();
        if (nOsFamily.isWindow()) {
            NPathType pathType = type(from);
            switch (pathType) {
                case DIRECTORY:
                    switch (resolveShellFamily()) {
                        case WIN_CMD:
                            String cmd = "rmdir " + (R ? "/s " : "") + "/q \"" + from + "\"";
                            execArrayCommandGrabbed(cmd);
                            break;
                        case WIN_POWER_SHELL:
                            String psCmd = "powershell -Command \"Remove-Item -Force " +
                                    (R ? "-Recurse " : "") + "'" + from + "'\"";
                            execArrayCommandGrabbed(psCmd);
                            break;
                    }
                    break;
                case FILE:
                    switch (resolveShellFamily()) {
                        case WIN_CMD:
                            execArrayCommandGrabbed("del /q /f \"" + from + "\"");
                            break;
                        case WIN_POWER_SHELL:
                            execArrayCommandGrabbed("powershell -Command \"Remove-Item -Force '" + from + "'\"");
                            break;
                    }
                    break;
                case NOT_FOUND:
                    System.out.println("-1");
                    break;
            }
        } else {
            execArrayCommandGrabbed("rm " + (R ? "-R " : "") + from);
        }
    }

    @Override
    public NPathType type(String path) {
        NOsFamily nOsFamily = resolveOsFamily();
        if (nOsFamily.isWindow()) {
            String cmd = "powershell -Command \"if (Test-Path '" + path + "' -PathType Container) { 'Directory' } " +
                    "elseif (Test-Path '" + path + "' -PathType Leaf) { 'File' } " +
                    "else { 'NotExist' }\"";
            IOResult i = execArrayCommandGrabbed(cmd);
            if (i.code() != 0) {
                return null;
            }
            String s = i.outString().trim();
            if (s.equals("Directory")) {
                return NPathType.DIRECTORY;
            } else if (s.equals("File")) {
                return NPathType.FILE;
            } else {
                return NPathType.NOT_FOUND;
            }
        } else {
            IOResult ii = execArrayCommandGrabbed("file", "-b", "-E", path);
            int i = ii.code();
            if (i > 0) {
                return null;
            }
            String s = ii.outString();
            s = s.trim();
            if (s.startsWith("directory")) {
                return NPathType.DIRECTORY;
            }
            if (s.startsWith("fifo (named pipe)")) {
                return NPathType.NAMED_PIPE;
            }
            if (s.startsWith("character special")) {
                return NPathType.CHARACTER;
            }
            if (s.startsWith("symbolic link")) {
                return NPathType.SYMBOLIC_LINK;
            }
            if (s.startsWith("block special")) {
                return NPathType.BLOCK;
            }
            return NPathType.FILE;
        }
    }


    @Override
    public void mkdir(String from, boolean p) {
        NOsFamily nOsFamily = resolveOsFamily();
        switch (resolveShellFamily()) {
            case WIN_CMD: {
                execArrayCommandGrabbed("mkdir", ensureWindowPath(from));
                break;
            }
            case WIN_POWER_SHELL: {
                if (p) {
                    execArrayCommandGrabbed("mkdir", "-p", ensureWindowPath(from));
                } else {
                    execArrayCommandGrabbed("mkdir", ensureWindowPath(from));
                }
            }
            default: {
                if (p) {
                    execArrayCommandGrabbed("mkdir", "-p", from);
                } else {
                    execArrayCommandGrabbed("mkdir", from);
                }
            }
        }
    }

    private String ensureWindowPath(String from) {
        from = from.replace("/", "\\");
        if (from.matches("\\\\[a-zA-Z]:.*")) {
            from = from.substring(1);
        }
        return from;
    }

    private NShellFamily resolveShellFamily() {
        NOsFamily os = resolveOsFamily();
        NShellFamily shellFamily = NShellFamily.WIN_POWER_SHELL;
        switch (os) {
            case WINDOWS: {
                shellFamily = NShellFamily.WIN_POWER_SHELL;
                break;
            }
            case LINUX: {
                shellFamily = NShellFamily.BASH;
                break;
            }
            case MACOS: {
                shellFamily = NShellFamily.ZSH;
                break;
            }
            case UNIX: {
                shellFamily = NShellFamily.SH;
                break;
            }
            default: {
                shellFamily = NShellFamily.BASH;
            }
        }
        if (getProbedInfo() != null) {
            shellFamily = getProbedInfo().shellFamily();
        }
        return shellFamily;
    }

    private OsProbeInfo getProbedInfo() {
        if (probedInfo == null) {
            OsProbeInfo osProbeInfo = OsProbeInfoCache.of().get(connectionString);
            osProbeInfo.tryUpdate();
            probedInfo = osProbeInfo;
        }
        return probedInfo;
    }

    private NOsFamily resolveOsFamily() {
        NOsFamily nOsFamily = NOsFamily.LINUX;
        if (getProbedInfo() != null) {
            nOsFamily = getProbedInfo().osFamily();
        }
        return nOsFamily;
    }

    @Override
    public List<String> list(String path) {
        switch (resolveShellFamily()) {
            case WIN_CMD:
            case WIN_POWER_SHELL: {
                IOResult i = execArrayCommandGrabbed("powershell", "-c", "Get-ChildItem '" + ensureWindowPath(path) + "'");
                if (i.code() == 0) {
                    //continue parsing
                    String[] s = i.outString().split("[\n|\r]");
                    Pattern compiled = Pattern.compile("(?<f>[a-z-]{6})[ \t]*(?<d>[0-9-/]{8,10})[ \t]*(?<t>[0-9]{2}:[0-9]{2})[ \t]*(?<p>.*)");
                    return NStream.ofArray(s).map(
                            x -> {
                                x = x.trim();
                                if (x.length() > 0) {
                                    Matcher m = compiled.matcher(x);
                                    if (m.find()) {
                                        String cc = path;
                                        if (!cc.endsWith("/")) {
                                            cc += "/";
                                        }
                                        cc += m.group("p");
                                        if (m.group("f").startsWith("d")) {
                                            cc += "/";
                                        }
                                        return cc;
                                    }
                                }
                                return null;
                            }
                    ).filter(x -> x != null).collect(Collectors.toList());
                }
                break;
            }
            case SH:
            case BASH:
            case ZSH:
            case FISH:
            case KSH:
            case CSH: {
                IOResult i = execArrayCommandGrabbed("ls", path);
                if (i.code() == 0) {
                    String[] s = i.outString().split("[\n|\r]");
                    return NStream.ofArray(s).map(
                            x -> {
                                String cc = path;
                                if (!cc.endsWith("/")) {
                                    cc += "/";
                                }
                                cc += x;
                                return cc;
                            }
                    ).collect(Collectors.toList());

                }
            }
        }
        return new ArrayList<>();
    }

    @Override
    public long contentLength(String basePath) {
        switch (resolveOsFamily()) {
            case WINDOWS:
                IOResult i = execStringCommandGrabbed("powershell -command (Get-Item " + basePath + ").Length");
                if (i.code() != 0) {
                    return -1;
                }
                String outputString_w = i.outString();
                NOptional<Long> size_windows = NLiteral.of(outputString_w.trim()).asLong();
                if (size_windows.isPresent()) {
                    return size_windows.get();
                }
                return -1;
            case LINUX:
                IOResult j = execStringCommandGrabbed("ls -l " + basePath);
                if (j.code() != 0) {
                    return -1;
                }
                String outputString_l = j.outString();
                String[] r = NStringUtils.trim(outputString_l).split(" ");
                if (r.length > 4) {
                    NOptional<Long> size_linux = NLiteral.of(r[4]).asLong();
                    if (size_linux.isPresent()) {
                        return size_linux.get();
                    }
                }
                return -1;
            default:
                return -1;
        }
    }


    @Override
    public String getContentEncoding(String basePath) {
        if(resolveOsFamily().isWindow()){
            return null;
        }
        IOResult i = execArrayCommandGrabbed("file", "-bi", basePath);
        if (i.code() != 0) {
            return null;
        }
        String outputString = NStringUtils.trim(i.outString());
        Pattern p = Pattern.compile(".*charset=(?<cs>\\S*).*");
        Matcher m = p.matcher(outputString);
        if (m.find()) {
            return m.group("cs");
        }
        return null;
    }

    @Override
    public String getContentType(String basePath) {
        if(resolveOsFamily().isWindow()){
            return null;
        }
        IOResult i = execArrayCommandGrabbed("file", "-bi", basePath);
        if (i.code() != 0) {
            return null;
        }
        String outputString = i.outString();
        String[] r = Arrays.stream(NStringUtils.trim(outputString).split("[ ;]")).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);
        if (r.length > 0) {
            return NStringUtils.trim(r[0]);
        }
        return null;
    }

    @Override
    public String getCharset(String basePath) {
        IOResult i = execArrayCommandGrabbed("file", "-bi", basePath);
        if (i.code() != 0) {
            return null;
        }
        String outputString = i.outString();
        String[] r = Arrays.stream(NStringUtils.trim(outputString).split("[ ;]")).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);
        if (r.length > 1) {
            String v = NStringUtils.trim(r[1]);
            if (v.startsWith("charset=")) {
                v = v.substring("charset=".length()).trim();
            }
            return v;
        }
        return null;
    }

    public void cp(String path, String path1, boolean b) {
        if(resolveOsFamily().isWindow()){
            //todo
            return ;
        }
        if (b) {
            execArrayCommandGrabbed("cp", path, path1);
        } else {
            execArrayCommandGrabbed("cp", "-r", path, path1);
        }
    }

    @Override
    public List<String> walk(String path, boolean followLinks, int maxDepth) {
        switch (resolveShellFamily()) {
            case WIN_CMD:
            case WIN_POWER_SHELL: {
                // Get-ChildItem -Path C:\ -Depth 2 -Force | ForEach-Object { $_.FullName }
                // TODO
                break;
            }
            case SH:
            case BASH:
            case ZSH:
            case FISH:
            case KSH:
            case CSH: {
                StringBuilder cmd = new StringBuilder();
                cmd.append("find");
                cmd.append(" ").append(path);
                if (followLinks) {
                    //all
                } else {
                    cmd.append(" -type d,f");
                }
                if (maxDepth > 0 && maxDepth != Integer.MAX_VALUE) {
                    cmd.append(" -maxdepth ").append(maxDepth);
                }
                IOResult i = execStringCommandGrabbed(cmd.toString());
                if (i.code() == 0) {
                    String[] s = i.outString().split("[\n|\r]");
                    return Stream.of(s).map(
                            x -> {
                                String cc = path;
                                if (!cc.endsWith("/")) {
                                    cc += "/";
                                }
                                cc += x;
                                return cc;
                            }
                    ).collect(Collectors.toList());
                }
                break;
            }
        }
        return new ArrayList<>();
    }


    public byte[] getDigestWithCommand(String algo, String basePath) {
        String cmdsum = null;
        switch (algo) {
            case "SHA-1": {
                cmdsum = "sha1sum";
                break;
            }
            case "SHA-256": {
                cmdsum = "sha256sum";
                break;
            }
            case "SHA-224": {
                cmdsum = "sha224sum";
                break;
            }
            case "SHA-512": {
                cmdsum = "sha512sum";
                break;
            }
            case "MD5": {
                cmdsum = "md5sum";
                break;
            }
        }
        if (cmdsum != null) {
            IOResult r = execArrayCommandGrabbed(cmdsum, basePath);
            if (r.code() == 0) {
                String z = NStringUtils.trim(r.outString());
                int i = z.indexOf(' ');
                if (i > 0) {
                    z = z.substring(0, i);
                    return NHex.toBytes(z);
                }
            }
        }
        return null;
    }

}
