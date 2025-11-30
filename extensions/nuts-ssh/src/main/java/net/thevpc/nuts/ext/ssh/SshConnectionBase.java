package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.command.NExecTargetInfo;
import net.thevpc.nuts.elem.NElementNotFoundException;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SshConnectionBase implements SshConnection {
    protected List<SshListener> listeners = new ArrayList<>();
    protected NConnectionString connectionString;
    private NExecTargetInfo probedInfo;
    private boolean failFast;

    public SshConnectionBase() {
    }

    @Override
    public void reset() {
        failFast = false;
    }

    @Override
    public SshConnection addListener(SshListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public SshConnection failFast() {
        return setFailFast(true);
    }

    public SshConnection sailFast(boolean failFast) {
        return setFailFast(failFast);
    }

    public SshConnection setFailFast(boolean failFast) {
        this.failFast = failFast;
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
        switch (resolveOsFamily()) {
            case WINDOWS:
                String source = ensureWindowPath(from);
                String destination = ensureWindowPath(to);
                return execArrayCommandGrabbed("powershell", "-Command", "Move-Item", source, destination, "-Verbose").code();

            case LINUX:
                return execArrayCommandGrabbed("mv", from, to).code();
            default:
                return -1;
        }
    }

    @Override
    public IOResult execArrayCommandGrabbed(String... command) {
        String sb = cmdArrayToString(command);
        return execStringCommandGrabbed(sb);
    }

    @Override
    public IOResult execStringCommandGrabbed(String command) {
        NLog.of(SshConnectionBase.class)
                .log(
                        NMsg.ofC("[%s] grab ssh command : %s", connectionString, command)
                                .withLevel(Level.FINER)
                                .withIntent(NMsgIntent.START)
                );
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
            from = ensureWindowPath(from);
            NPathType pathType = type(from);
            switch (pathType) {
                case DIRECTORY: {
                    switch (resolveShellFamily()) {
                        case WIN_CMD:
                            IOResult result = execArrayCommandGrabbed(
                                    "cmd", "/c", "rmdir " + (R ? "/s " : "") + "/q \"" + from + "\""
                            );
                            if (result.code() != 0) {
                                System.out.println("Failed to delete Directory" + from);
                            }
                            break;
                        case WIN_POWER_SHELL:
                            String psCmd = "Remove-Item -Force " + (R ? "-Recurse " : "") + "'" + from + "'";
                            IOResult psResult = execArrayCommandGrabbed(
                                    "powershell", "-Command", psCmd
                            );
                            if (psResult.code() != 0) {
                                System.out.println("Failed to delete Directory" + from);
                            }
                            break;
                    }
                    break;
                }
                case FILE: {
                    switch (resolveShellFamily()) {
                        case WIN_CMD:
                            IOResult fileResult = execArrayCommandGrabbed(
                                    "cmd", "/c", "del /q /f \"" + from + "\""
                            );
                            if (fileResult.code() != 0) {
                                System.out.println("Failed to delete file: " + from);
                            }
                            break;
                        case WIN_POWER_SHELL:
                            IOResult psFileResult = execArrayCommandGrabbed(
                                    "powershell", "-Command", "Remove-Item -Force '" + from + "'"
                            );
                            if (psFileResult.code() != 0) {
                                System.out.println("Failed to delete file" + from);
                            }
                            break;
                    }
                    break;
                }
                case NOT_FOUND: {
                    throw new NElementNotFoundException(NMsg.ofC("Path not found: %s", from));
                }
            }
        } else {
            execArrayCommandGrabbed("rm " + (R ? "-R " : "") + from);
        }
    }

    @Override
    public NPathType type(String path) {
        NOsFamily nOsFamily = resolveOsFamily();
        if (nOsFamily.isWindow()) {
            path = ensureWindowPath(path);
            String psCommand = "if (Test-Path '" + path + "') { if ((Get-Item '" + path + "').PSIsContainer) { 'Directory' } else { 'File' } } else { 'NotExist' }";
            IOResult i = execArrayCommandGrabbed(
                    "powershell",
                    "-Command",
                    psCommand
            );
            if (i.code() != 0) {
                return NPathType.NOT_FOUND;
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
                return NPathType.NOT_FOUND;
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
                return NPathType.CHARACTER_DEVICE;
            }
            if (s.startsWith("symbolic link")) {
                return NPathType.SYMBOLIC_LINK;
            }
            if (s.startsWith("block special")) {
                return NPathType.BLOCK_DEVICE;
            }
            return NPathType.FILE;
        }
    }


    @Override
    public void mkdir(String from, boolean p) {
        switch (resolveShellFamily()) {
            case WIN_CMD: {
                if (!p) {
                    NPath parent = NPath.of(from).getParent();
                    if (parent != null) {
                        NPathType d = type(parent.toString());
                        if (d != NPathType.DIRECTORY) {
                            if (d == NPathType.NOT_FOUND) {
                                throw new NElementNotFoundException(NMsg.ofC("Path not found: %s", parent));
                            } else {
                                throw new NElementNotFoundException(NMsg.ofC("Path not a directory : %s", parent));
                            }
                        }
                    }
                }
                execArrayCommandGrabbed("mkdir", ensureWindowPath(from));
                break;
            }
            case WIN_POWER_SHELL: {
                if (p) {
                    execArrayCommandGrabbed("mkdir", "-p", ensureWindowPath(from));
                } else {
                    NPath parent = NPath.of(from).getParent();
                    if (parent != null) {
                        NPathType d = type(parent.toString());
                        if (d != NPathType.DIRECTORY) {
                            if (d == NPathType.NOT_FOUND) {
                                throw new NElementNotFoundException(NMsg.ofC("Path not found: %s", parent));
                            } else {
                                throw new NElementNotFoundException(NMsg.ofC("Path not a directory : %s", parent));
                            }
                        }
                    }
                    execArrayCommandGrabbed("mkdir", ensureWindowPath(from));
                }
                break;
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
            shellFamily = getProbedInfo().getShellFamily();
        }
        return shellFamily;
    }

    private NExecTargetInfo getProbedInfo() {
        if (probedInfo == null) {
            probedInfo = NExecCmd.of().at(connectionString).probeTarget();
        }
        return probedInfo;
    }

    private NOsFamily resolveOsFamily() {
        NOsFamily nOsFamily = NOsFamily.LINUX;
        if (getProbedInfo() != null) {
            nOsFamily = getProbedInfo().getOsFamily();
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
    public long getContentLength(String basePath) {
        switch (resolveOsFamily()) {
            case WINDOWS: {
                basePath = ensureWindowPath(basePath);

                String psCmd = "$ErrorActionPreference='Stop';(Get-Item -LiteralPath '" + basePath + "').Length";
                IOResult i = execArrayCommandGrabbed(
                        "powershell", "-Command", psCmd
                );
                if (i.code() != 0) {
                    return -1;
                }
                String output = i.outString().trim();
                String[] lines = output.split("\\r?\\n");
                String lastLine = lines[lines.length - 1].trim();
                NOptional<Long> size_windows = NLiteral.of(lastLine).asLong();
                if (size_windows.isPresent()) {
                    return size_windows.get();
                }
                return -1;
            }
            case LINUX: {
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
            }
            default: {
                return -1;
            }
        }
    }


    @Override
    public String getContentEncoding(String basePath) {
        if (resolveOsFamily().isWindow()) {
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
        if (resolveOsFamily().isWindow()) {
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
        if (resolveOsFamily().isWindow()) {
            return null;
        }
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

    public void cp(String path, String path1, boolean recursive) {
        switch (resolveOsFamily()) {
            case WINDOWS: {
                String from = path;
                String to = path1;
                if (from.startsWith("/") && from.length() > 2) {
                    from = from.substring(1);
                }
                if (to.startsWith("/") && to.length() > 2) {
                    to = to.substring(1);
                }
                from = ensureWindowPath(from);
                to = ensureWindowPath(to);
                NPathType sourceType = type(from);
                switch (resolveShellFamily()) {
                    case WIN_CMD: {
                        IOResult result;
                        if (sourceType == NPathType.DIRECTORY || recursive) {
                            result = execArrayCommandGrabbed(
                                    "cmd", "/c", "xcopy \"" + from + "\" \"" + to + "\" /E /I /H /Y"
                            );
                        } else {
                            result = execArrayCommandGrabbed(
                                    "cmd", "/c", "copy /Y \"" + from + "\" \"" + to + "\""
                            );
                        }
                        if (result.code() != 0) {
                            System.out.println("-1");
                        }
                        break;
                    }

                    case WIN_POWER_SHELL: {
                        String psCmd = (sourceType == NPathType.DIRECTORY || recursive) ?
                                "Copy-Item -Path '" + from + "' -Destination '" + to + "' -Recurse -Force" :
                                "Copy-Item -Path '" + from + "' -Destination '" + to + "' -Force";
                        IOResult result = execArrayCommandGrabbed(
                                "powershell", "-Command", psCmd
                        );
                        if (result.code() != 0) {
                            System.out.println("-1");
                        }
                        break;
                    }
                    default:
                        System.out.println("-1");
                }
                break;
            }
            case LINUX: {
                IOResult result;
                if (recursive) {
                    result = execArrayCommandGrabbed("cp", "-r", path, path1);
                } else {
                    result = execArrayCommandGrabbed("cp", path, path1);
                }

                if (result.code() != 0) {
                    System.out.println("-1");
                }
                break;
            }
            default:
                System.out.println("-1");
        }
    }

    @Override
    public List<String> walk(String path, boolean followLinks, int maxDepth) {
        switch (resolveShellFamily()) {
            case WIN_CMD:
            case WIN_POWER_SHELL: {
                String remote_path = ensureWindowPath(path);
                StringBuilder cmd = new StringBuilder();
                cmd.append("powershell -Command \"Get-ChildItem -Path '")
                        .append(remote_path.replace("'", "''"))
                        .append("' -Recurse -Force -ErrorAction SilentlyContinue");
                if (maxDepth > 0 && maxDepth != Integer.MAX_VALUE) {
                    cmd.append(" -Depth ").append(maxDepth - 1);
                }
                cmd.append(" | ForEach-Object { $_.FullName }\"");
                IOResult result = execStringCommandGrabbed(cmd.toString());
                if (result.code() == 0) {
                    String output = result.outString();
                    if (output == null || output.isEmpty()) {
                        return new ArrayList<>();
                    }
                    String[] lines = output.split("[\\r\\n]+");
                    return Arrays.stream(lines)
                            .filter(l -> l != null && !l.trim().isEmpty())
                            .collect(Collectors.toList());
                }
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
        switch (resolveOsFamily()) {
            case WINDOWS:
                String psAlgo;
                if (basePath.startsWith("/") && basePath.length() > 2) {
                    basePath = basePath.substring(1);
                }
                basePath = ensureWindowPath(basePath);
                switch (algo.toUpperCase()) {
                    case "SHA-1":
                        psAlgo = "SHA1";
                        break;
                    case "SHA-256":
                        psAlgo = "SHA256";
                        break;
                    case "SHA-512":
                        psAlgo = "SHA512";
                        break;
                    case "MD5":
                        psAlgo = "MD5";
                        break;
                    default:
                        return null;
                }
                String psCmd = "(Get-FileHash -Path '" + basePath + "' -Algorithm " + psAlgo + ").Hash";
                IOResult r = execArrayCommandGrabbed("powershell", "-Command", psCmd);
                if (r.code() == 0) {
                    String z = r.outString();
                    return NHex.toBytes(z);
                } else {
                    return null;
                }
            case LINUX:
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
                    IOResult j = execArrayCommandGrabbed(cmdsum, basePath);
                    if (j.code() == 0) {
                        String z = NStringUtils.trim(j.outString());
                        int i = z.indexOf(' ');
                        if (i > 0) {
                            z = z.substring(0, i);
                            return NHex.toBytes(z);
                        }
                    }
                }
                return null;
            default:
                return null;
        }
    }

    @Override
    public List<NPathInfo> listInfos(String path) {
        switch (resolveOsFamily()) {
            case WINDOWS: {
                String script = "Get-ChildItem -LiteralPath '" + ensureWindowPath(path) + "' | ForEach-Object {\n" +
                        "    $t = if ($_.PSIsContainer) {'directory'}\n" +
                        "         elseif ($_.Attributes -band [System.IO.FileAttributes]::ReparsePoint) {'symlink'}\n" +
                        "         else {'file'}\n" +
                        "    $target = if ($_.Attributes -band [System.IO.FileAttributes]::ReparsePoint) { $_.Target } else { '' }\n" +
                        "    $perms = if (($_.Attributes -band [System.IO.FileAttributes]::ReadOnly) -ne 0) {'readonly'} else {''}\n" +
                        "    $owner = (Get-Acl $_.FullName).Owner\n" +
                        "    $group = '' # placeholder\n" +
                        "    \"$($_.FullName);$t;$($_.Length);$($_.LastWriteTimeUtc.ToFileTimeUtc());$($_.LastAccessTimeUtc.ToFileTimeUtc());$($_.CreationTimeUtc.ToFileTimeUtc());$perms;$owner;$group;$target\"\n" +
                        "}";
                // Convert to UTF-16LE bytes
                byte[] utf16leBytes = script.getBytes(StandardCharsets.UTF_16LE);
                String base64 = Base64.getEncoder().encodeToString(utf16leBytes);
                IOResult stat = execArrayCommandGrabbed("powershell", "-NoProfile","-EncodedCommand", base64);
                List<NPathInfo> found = new ArrayList<>();
                if (stat.code() == 0) {
                    for (String line : NStringUtils.splitLines(stat.outString())) {
                        if (!NBlankable.isBlank(line)) {
                            NPathInfo nPathInfo = parseWindowsStatLine(line);
                            found.add(nPathInfo);
                        }
                    }
                }
                return found;
            }
            default: {
                IOResult stat = execArrayCommandGrabbed("stat", "--printf=%n;%F;%s;%Y;%X;%W;%A;%U;%G;%N\\n", path);
                if (stat.code() == 0) {
                    List<NPathInfo> found = new ArrayList<>();
                    Map<String,NPathInfo> lazy = new LinkedHashMap<>();
                    for (String line : NStringUtils.splitLines(stat.outString())) {
                        if (!NBlankable.isBlank(line)) {
                            NPathInfo nPathInfo = parseStatLine(line);
                            if(nPathInfo.isSymbolicLink()){
                                lazy.put(nPathInfo.getPath(), nPathInfo);
                            }else {
                                found.add(nPathInfo);
                            }
                        }
                    }
                    if(lazy.size()>0){
                        List<String> cmd=new ArrayList<>(
                                Arrays.asList(
                                        "stat","-L", "--printf=%n;%F;%s;%Y;%X;%W;%A;%U;%G;%N\\n"
                                )
                        );
                        cmd.addAll(lazy.keySet());
                        stat = execArrayCommandGrabbed(cmd.toArray(new String[0]));
                        if (stat.code() == 0) {
                            for (String line : NStringUtils.splitLines(stat.outString())) {
                                if (!NBlankable.isBlank(line)) {
                                    NPathInfo nPathInfo = parseStatLine(line);
                                    NPathInfo o = lazy.get(nPathInfo.getPath());
                                    found.add(
                                            new  DefaultNPathInfo(
                                                    o.getPath(), o.getType(), nPathInfo.getType(), o.getTargetPath(), o.getContentLength(), o.isSymbolicLink(), o.getLastModifiedInstant(), o.getLastAccessInstant(), o.getCreationInstant(), o.getPermissions(), o.getOwner(), o.getGroup()
                                            )
                                    );
                                }
                            }
                        }
                    }
                    return found;
                }
                return Collections.emptyList();
            }
        }
    }

    private NPathInfo parseWindowsStatLine(String line) {
        // Expected format: FullName;type;Length;LastWriteTimeUtc;CreationTimeUtc;perms;owner;group;target
        // Example:
        // C:\Users;directory;4096;132465132465132465;132465132465132465;;Administrator;;

        String[] parts = line.split(";", 11); // 9 fields expected
        if (parts.length < 11) {
            throw new IllegalArgumentException("Invalid stat line: " + line);
        }

        String pathStr = parts[0];
        String fType = parts[1];
        long size = 0;
        try {
            size = Long.parseLong(parts[2]);
        } catch (NumberFormatException ignored) {}

        long lastModifiedEpoch = 0;
        try {
            lastModifiedEpoch = Long.parseLong(parts[3]);
        } catch (NumberFormatException ignored) {}

        long lastAccessEpoch = 0;
        try {
            lastAccessEpoch = Long.parseLong(parts[4]);
        } catch (NumberFormatException ignored) {}

        long creationEpoch = 0;
        try {
            creationEpoch = Long.parseLong(parts[5]);
        } catch (NumberFormatException ignored) {}

        String permsStr = parts[6];
        String owner = parts[4];
        String group = parts[8];
        String targetPathStr = parts[9];

        // Map type string to NPathType
        NPathType type;
        boolean isSymlink = false;
        switch (fType.toLowerCase()) {
            case "directory": type = NPathType.DIRECTORY; break;
            case "symlink":
                type = NPathType.SYMBOLIC_LINK;
                isSymlink = true;
                break;
            case "file": type = NPathType.FILE; break;
            default: type = NPathType.OTHER;
        }

        // Parse permissions
        Set<NPathPermission> perms = new LinkedHashSet<>();
        if ("readonly".equalsIgnoreCase(permsStr)) {
            perms.add(NPathPermission.CAN_READ);
            perms.add(NPathPermission.OWNER_READ);
        } else {
            perms.add(NPathPermission.CAN_READ);
            perms.add(NPathPermission.CAN_WRITE);
            perms.add(NPathPermission.OWNER_READ);
            perms.add(NPathPermission.OWNER_WRITE);
        }

        return new DefaultNPathInfo(
                pathStr,
                type,
                null,               // targetType unknown on Windows for now
                targetPathStr.isEmpty() ? null : targetPathStr,
                size,
                isSymlink,
                lastModifiedEpoch > 0 ? Instant.ofEpochSecond(lastModifiedEpoch) : null,
                lastAccessEpoch > 0 ? Instant.ofEpochSecond(lastAccessEpoch) : null,
                creationEpoch > 0 ? Instant.ofEpochSecond(creationEpoch) : null,
                perms,
                owner,
                group
        );
    }

    private NPathInfo parseStatLine(String line) {
// Expected format: %n;%F;%s;%Y;%W;%A;%U;%G;%N
        // Example:
        // /bin;symbolic link;7;1624338419;1640892655;lrwxrwxrwx;root;root;'/bin' -> 'usr/bin'

        String[] parts = line.split(";", 9); // 9 fields expected
        if (parts.length < 9) {
            throw new IllegalArgumentException("Invalid stat line: " + line);
        }

        String pathStr = parts[0];
        String fType = parts[1];
        long size = Long.parseLong(parts[2]);
        long lastModifiedEpoch = Long.parseLong(parts[3]);
        long lastAccessEpoch = Long.parseLong(parts[4]);
        long creationEpoch = Long.parseLong(parts[5]);
        String permissionsStr = parts[6];
        String owner = parts[7];
        String group = parts[8];
        String nField = parts[9];

        // Determine type
        NPathType type = mapStatType(fType);

        // Symlink handling
        boolean isSymlink = "symbolic link".equals(fType);
        String targetPathStr = null;
        if (isSymlink) {
            // Parse the 'link' -> 'target' format
            int arrowIndex = nField.indexOf("->");
            if (arrowIndex >= 0) {
                String targetPart = nField.substring(arrowIndex + 2).trim();
                targetPart = targetPart.replaceAll("^'+|'+$", ""); // remove quotes
                targetPathStr = targetPart;
            }
            if (targetPathStr != null) {
                String parent = pathStr.substring(0, pathStr.lastIndexOf('/'));
                if (!targetPathStr.startsWith("/")) { // relative target
                    targetPathStr = parent + "/" + targetPathStr;
                    // normalize .. and .
                    Deque<String> stack = new ArrayDeque<>();
                    for (String part : targetPathStr.split("/")) {
                        if (part.isEmpty() || part.equals(".")) continue;
                        if (part.equals("..")) {
                            if (!stack.isEmpty()) stack.removeLast();
                        } else {
                            stack.addLast(part);
                        }
                    }
                    targetPathStr = "/" + String.join("/", stack);
                }
                // if targetPathStr starts with /, already absolute → keep as-is
            }
        }
        final NPathType targetType = null;
        Set<NPathPermission> perms = new LinkedHashSet<>();

        if (permissionsStr.length() == 10) {
            if (permissionsStr.charAt(1) == 'r') perms.add(NPathPermission.OWNER_READ);
            if (permissionsStr.charAt(2) == 'w') perms.add(NPathPermission.OWNER_WRITE);
            if (permissionsStr.charAt(3) == 'x') perms.add(NPathPermission.OWNER_EXECUTE);
            if (permissionsStr.charAt(4) == 'r') perms.add(NPathPermission.GROUP_READ);
            if (permissionsStr.charAt(5) == 'w') perms.add(NPathPermission.GROUP_WRITE);
            if (permissionsStr.charAt(6) == 'x') perms.add(NPathPermission.GROUP_EXECUTE);
            if (permissionsStr.charAt(7) == 'r') perms.add(NPathPermission.OTHERS_READ);
            if (permissionsStr.charAt(8) == 'w') perms.add(NPathPermission.OTHERS_WRITE);
            if (permissionsStr.charAt(9) == 'x') perms.add(NPathPermission.OTHERS_EXECUTE);

            // convenience flags
            if (permissionsStr.charAt(1) == 'r' || permissionsStr.charAt(4) == 'r' || permissionsStr.charAt(7) == 'r') perms.add(NPathPermission.CAN_READ);
            if (permissionsStr.charAt(2) == 'w' || permissionsStr.charAt(5) == 'w' || permissionsStr.charAt(8) == 'w') perms.add(NPathPermission.CAN_WRITE);
            if (permissionsStr.charAt(3) == 'x' || permissionsStr.charAt(6) == 'x' || permissionsStr.charAt(9) == 'x') perms.add(NPathPermission.CAN_EXECUTE);
        }
        return new DefaultNPathInfo(pathStr, type, targetType, targetPathStr, size, isSymlink,
                Instant.ofEpochSecond(lastModifiedEpoch),
                lastAccessEpoch > 0 ? Instant.ofEpochSecond(lastAccessEpoch) : null,
                creationEpoch > 0 ? Instant.ofEpochSecond(creationEpoch) : null,
                perms,
                owner,group
        );
    }

    // Helper to map stat %F strings to NPathType
    private NPathType mapStatType(String fType) {
        switch (fType) {
            case "regular file": return NPathType.FILE;
            case "directory": return NPathType.DIRECTORY;
            case "symbolic link": return NPathType.SYMBOLIC_LINK;
            case "block special file": return NPathType.BLOCK_DEVICE;
            case "character special file": return NPathType.CHARACTER_DEVICE;
            case "fifo": return NPathType.NAMED_PIPE;
            case "socket": return NPathType.SOCKET;
            default: return NPathType.OTHER;
        }
    }

    @Override
    public NPathInfo getInfo(String path) {
        switch (resolveOsFamily()) {
            case WINDOWS: {
                String script = "Get-Item -LiteralPath '" + ensureWindowPath(path) + "' | ForEach-Object {\n" +
                        "    $t = if ($_.PSIsContainer) {'directory'}\n" +
                        "         elseif ($_.Attributes -band [System.IO.FileAttributes]::ReparsePoint) {'symlink'}\n" +
                        "         else {'file'}\n" +
                        "    $target = if ($_.Attributes -band [System.IO.FileAttributes]::ReparsePoint) { $_.Target } else { '' }\n" +
                        "    $perms = if (($_.Attributes -band [System.IO.FileAttributes]::ReadOnly) -ne 0) {'readonly'} else {''}\n" +
                        "    $owner = (Get-Acl $_.FullName).Owner\n" +
                        "    $group = '' # Windows doesn't have a native 'group'\n" +
                        "    \"$($_.FullName);$t;$($_.Length);$($_.LastWriteTimeUtc.ToFileTimeUtc());$($_.CreationTimeUtc.ToFileTimeUtc());$perms;$owner;$group;$target\"\n" +
                        "}";

                // Encode script to Base64 UTF-16LE for PowerShell
                byte[] utf16leBytes = script.getBytes(StandardCharsets.UTF_16LE);
                String base64 = Base64.getEncoder().encodeToString(utf16leBytes);

                IOResult result = execArrayCommandGrabbed(
                        "powershell", "-NoProfile", "-EncodedCommand", base64
                );

                if (result.code() == 0) {
                    List<String> lines = NStringUtils.splitLines(result.outString());
                    if (!lines.isEmpty() && !NBlankable.isBlank(lines.get(0))) {
                        return parseWindowsStatLine(lines.get(0));
                    }
                }

                // Fallback: not found
                return DefaultNPathInfo.ofNotFound(path);
            }
            default: {
                IOResult stat = execStringCommandGrabbed("stat '--printf=%n;%F;%s;%Y;%X;%W;%A;%U;%G;%N\\n' "+doEscapeArg(path)+" ; stat -L '--printf=%n;%F;%s;%Y;%X;%W;%A;%U;%G;%N\\n' "+doEscapeArg(path)+" ; ");
                if (stat.code() == 0) {
                    List<String> lines = NStringUtils.splitLines(stat.outString());
                    NPathInfo o = parseStatLine(lines.get(0));
                    if(o.isSymbolicLink()){
                        NPathInfo line2 = parseStatLine(lines.get(1));
                        return new  DefaultNPathInfo(
                                o.getPath(), o.getType(), line2.getTargetType(), o.getTargetPath(), o.getContentLength(), o.isSymbolicLink(), o.getLastModifiedInstant(), o.getCreationInstant(), o.getLastAccessInstant(), o.getPermissions(), o.getOwner(), o.getGroup()
                        );
                    }
                    return o;
                }
                break;
            }
        }
        return DefaultNPathInfo.ofNotFound(path);
    }
}
