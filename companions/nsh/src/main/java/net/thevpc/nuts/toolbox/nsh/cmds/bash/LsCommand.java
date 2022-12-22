/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds.bash;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsArgumentName;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPathPermission;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles.BytesSizeFormat;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.util.NutsComparator;
import net.thevpc.nuts.util.NutsStringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class LsCommand extends SimpleJShellBuiltin {

    private static final FileSorter FILE_SORTER = new FileSorter();
    private final HashSet<String> fileTypeArchive = new HashSet<String>(Arrays.asList("jar", "war", "ear", "rar", "zip", "tar", "gz"));
    private final HashSet<String> fileTypeExec2 = new HashSet<String>(Arrays.asList("jar", "war", "ear", "rar", "zip", "bin", "exe", "tar", "gz", "class", "sh"));
    private final HashSet<String> fileTypeConfig = new HashSet<String>(Arrays.asList("xml", "config", "cfg", "json", "iml", "ipr"));
    private final DateTimeFormatter SIMPLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    public LsCommand() {
        super("ls", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        NutsArgument a;
        if ((a = commandLine.nextBoolean("-d", "--dir").orNull()) != null) {
            options.d = a.getBooleanValue().get(session);
            return true;
        } else if ((a = commandLine.nextBoolean("-l", "--list").orNull()) != null) {
            options.l = a.getBooleanValue().get(session);
            return true;
        } else if ((a = commandLine.nextBoolean("-a", "--all").orNull()) != null) {
            options.a = a.getBooleanValue().get(session);
            return true;
        } else if ((a = commandLine.nextBoolean("-h").orNull()) != null) {
            options.h = a.getBooleanValue().get(session);
            return true;
        } else if (commandLine.peek().get(session).isNonOption()) {
            String path = commandLine.next(NutsArgumentName.of("file", session))
                    .flatMap(NutsValue::asString).get(session);
            options.paths.add(path);
            options.paths.addAll(Arrays.asList(commandLine.toStringArray()));
            commandLine.skip();
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        ResultSuccess success = new ResultSuccess();
        success.workingDir = context.getAbsolutePath(".");
        ResultError errors = null;
        int exitCode = 0;
        if (options.paths.isEmpty()) {
            options.paths.add(context.getAbsolutePath("."));
        }
        NutsSession session = context.getSession();
        LinkedHashMap<NutsPath, ResultGroup> filesTodos = new LinkedHashMap<>();
        LinkedHashMap<NutsPath, ResultGroup> foldersTodos = new LinkedHashMap<>();
        for (String path : options.paths) {
            if (NutsBlankable.isBlank(path)) {
                if (errors == null) {
                    errors = new ResultError();
                    errors.workingDir = context.getAbsolutePath(".");
                }
                errors.result.put(path, NutsMessage.ofCstyle("cannot access '%s': No such file or directory", path));
                continue;
            }
            NutsPath file = NutsPath.of(path, session);
            if (file == null) {
                if (errors == null) {
                    errors = new ResultError();
                    errors.workingDir = context.getAbsolutePath(".");
                }
                errors.result.put(path, NutsMessage.ofCstyle("cannot access '%s': No such file or directory", path));
                continue;
            }
            file = file.toAbsolute(NutsPath.of(context.getCwd(), session));
            if (!file.exists()) {
                exitCode = 1;
                if (errors == null) {
                    errors = new ResultError();
                    errors.workingDir = context.getAbsolutePath(".");
                }
                errors.result.put(path, NutsMessage.ofCstyle("cannot access '%s': No such file or directory", file));
            } else {
                ResultGroup g = new ResultGroup();
                g.name = path;
                if (!file.isDirectory() || options.d) {
                    filesTodos.put(file, g);
                } else {
                    foldersTodos.put(file, g);
                }
            }
        }
        for (Map.Entry<NutsPath, ResultGroup> e : filesTodos.entrySet()) {
            NutsPath file = e.getKey();
            ResultGroup g = e.getValue();
            g.file = build(file);
            success.result.add(g);
        }
        for (Map.Entry<NutsPath, ResultGroup> e : foldersTodos.entrySet()) {
            NutsPath file = e.getKey();
            ResultGroup g = e.getValue();
            g.children = file.list()
                    .sorted(FILE_SORTER)
                    .map(this::build, "build")
                    .filter(
                            b -> options.a || !b.hidden,
                            "all || !hidden"
                    )
                    .toList();
            success.result.add(g);
        }
        if (success != null) {
            NutsPrintStream out = session.out();
            switch (session.getOutputFormat()) {
                case XML:
                case JSON:
                case YAML:
                case TREE:
                case TSON:
                case PROPS: {
                    out.printlnf(success
                            .result
                            .stream().collect(Collectors.toMap(x -> x.name, x -> x.children))
                    );
                    break;
                }
                case TABLE: {
                    out.printlnf(success.result.stream()
                            .flatMap(x ->
                                    x.children == null ? Stream.empty() :
                                            x.children.stream().map(y -> {
                                                Map m = (Map) NutsElements.of(session).destruct(y);
                                                m.put("group", x.name);
                                                return m;
                                            })).collect(Collectors.toList()));
                    break;
                }
                case PLAIN: {
                    boolean first = true;
                    for (ResultGroup resultGroup : success.result) {
                        boolean wasFirst = first;
                        first = false;
                        if (resultGroup.children != null) {
                            if (!wasFirst) {
                                out.println();
                            }
                            if (options.paths.size() > 1) {
                                out.printf("%s:\n", resultGroup.name);
                            }
                            for (ResultItem resultItem : resultGroup.children) {
                                printPlain(resultItem, options, out, session);
                            }
                        } else {
                            printPlain(resultGroup.file, options, out, session);
                        }
                    }
                    break;
                }
            }
        }
        if (errors != null) {
            // if plain
//            ResultError s = context.getResult();
//            for (Map.Entry<String, NutsMessage> e : s.result.entrySet()) {
//                NutsTexts text = NutsTexts.of(session);
//                out.printf("%s%n",
//                        text.builder().append(e.getKey(),NutsTextStyle.primary5())
//                                .append(" : ")
//                                .append(e.getValue(),NutsTextStyle.error())
//                );
//            }
            throwExecutionException(errors.result, exitCode, session);
        }
    }

    private void printPlain(ResultItem item, Options options, NutsPrintStream out, NutsSession session) {
        if (options.l) {
            out.printf("%s%s  %s %s %s %s ",
                    item.type, item.uperms != null ? item.uperms : item.jperms, NutsStringUtils.trim(item.owner), NutsStringUtils.trim(item.group),
                    options.h ? options.byteFormat.format(item.length) : String.format("%9d", item.length),
                    item.modified == null ? "" : SIMPLE_DATE_FORMAT.format(item.modified)
            );
        }
        String name = NutsPath.of(item.path, session).getName();
        NutsTexts text = NutsTexts.of(session);
        if (item.hidden) {
            out.println(text.ofStyled(name, NutsTextStyle.pale()));
        } else if (item.type == 'd') {
            out.println(text.ofStyled(name, NutsTextStyle.primary3()));
        } else if (item.exec2 || item.jperms.charAt(2) == 'x') {
            out.println(text.ofStyled(name, NutsTextStyle.primary4()));
        } else if (item.config) {
            out.println(text.ofStyled(name, NutsTextStyle.primary5()));
        } else if (item.archive) {
            out.println(text.ofStyled(name, NutsTextStyle.primary1()));
        } else {
            out.println(text.ofPlain(name));
        }
    }

    private ResultItem build(NutsPath path) {
        ResultItem r = new ResultItem();
        r.path = path.toString();
        r.name = path.getName();
        boolean dir = path.isDirectory();
        boolean regular = path.isRegularFile();
        boolean link = path.isSymbolicLink();
        boolean other = false;
        Set<NutsPathPermission> permissions = path.getPermissions();
        r.jperms = (permissions.contains(NutsPathPermission.CAN_READ) ? "r" : "-") + (permissions.contains(NutsPathPermission.CAN_WRITE) ? "w" : "-") + (permissions.contains(NutsPathPermission.CAN_EXECUTE) ? "x" : "-");
        r.owner = path.owner();
        r.group = path.group();
        r.modified = path.getLastModifiedInstant();
        r.created = path.getCreationInstant();
        r.accessed = path.getLastAccessInstant();
        other = path.isOther();
        r.length = path.getContentLength();
        char[] perms = new char[9];
        perms[0] = permissions.contains(NutsPathPermission.OWNER_READ) ? 'r' : '-';
        perms[1] = permissions.contains(NutsPathPermission.OWNER_WRITE) ? 'w' : '-';
        perms[2] = permissions.contains(NutsPathPermission.OWNER_EXECUTE) ? 'x' : '-';
        perms[3] = permissions.contains(NutsPathPermission.GROUP_READ) ? 'r' : '-';
        perms[4] = permissions.contains(NutsPathPermission.GROUP_WRITE) ? 'w' : '-';
        perms[5] = permissions.contains(NutsPathPermission.GROUP_EXECUTE) ? 'x' : '-';
        perms[6] = permissions.contains(NutsPathPermission.OTHERS_READ) ? 'r' : '-';
        perms[7] = permissions.contains(NutsPathPermission.OTHERS_WRITE) ? 'w' : '-';
        perms[8] = permissions.contains(NutsPathPermission.OTHERS_EXECUTE) ? 'x' : '-';
        r.uperms = new String(perms);


        String p = path.getName().toLowerCase();
        if (!dir) {
            if (p.startsWith(".") || p.endsWith(".log") || p.contains(".log.")) {
                r.hidden = true;
            } else {
                int i = p.lastIndexOf('.');
                if (i > -1) {
                    String suffix = p.substring(i + 1);
                    if (fileTypeConfig.contains(suffix)) {
                        r.config = true;
                    }
                    if (fileTypeArchive.contains(suffix)) {
                        r.archive = true;
                    }
                    if (fileTypeExec2.contains(suffix)) {
                        r.exec2 = true;
                    }
                }
            }
        } else {
            if (p.startsWith(".")) {
                r.hidden = true;
            }
        }
        r.type = dir ? 'd' : regular ? '-' : link ? 'l' : other ? 'o' : '?';
        return r;
    }

    private static class Options {

        boolean a = false;
        boolean d = false;
        boolean l = false;
        boolean h = false;
        List<String> paths = new ArrayList<>();
        BytesSizeFormat byteFormat = new BytesSizeFormat("iD1F");
    }

    private static class ResultSuccess {

        String workingDir;
        List<ResultGroup> result = new ArrayList<>();
    }

    public static class ResultError {

        boolean error = true;
        String workingDir;
        Map<String, NutsMessage> result = new HashMap<>();
    }

    public static class ResultGroup {

        String name;
        ResultItem file;
        List<ResultItem> children;
    }

    public static class ResultItem {

        String name;
        String path;
        char type;
        String uperms;
        String jperms;
        String owner;
        String group;
        long length;
        Instant modified;
        Instant created;
        Instant accessed;
        //        boolean dir;
//        boolean regular;
//        boolean forLink;
//        boolean other;
        boolean config;
        boolean exec2;
        boolean archive;
        boolean hidden;
    }

    private static class FileSorter implements NutsComparator<NutsPath> {

        boolean foldersFirst = true;
        boolean groupCase = true;
//        boolean hiddenFirst = true;

        @Override
        public int compare(NutsPath o1, NutsPath o2) {
            int d1 = o1.isDirectory() ? 0 : o1.isRegularFile() ? 1 : 2;
            int d2 = o2.isDirectory() ? 0 : o2.isRegularFile() ? 1 : 2;
            int x = 0;
            if (foldersFirst) {
                x = d1 - d2;
                if (x != 0) {
                    return x;
                }
            }
            if (groupCase) {
                x = o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
                if (x != 0) {
                    return x;
                }
            }
            x = o1.toString().compareTo(o2.toString());
            return x;
        }

        @Override
        public NutsElement describe(NutsSession session) {
            return NutsElements.of(session).ofString("foldersFirst");
        }
    }
}
