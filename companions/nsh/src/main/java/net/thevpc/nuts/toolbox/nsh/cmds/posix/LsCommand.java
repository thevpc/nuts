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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds.posix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathPermission;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.bundles.BytesSizeFormat;
import net.thevpc.nuts.util.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class LsCommand extends NShellBuiltinDefault {

    private static final FileSorter FILE_SORTER = new FileSorter();
    private final HashSet<String> fileTypeArchive = new HashSet<String>(Arrays.asList("jar", "war", "ear", "rar", "zip", "tar", "gz"));
    private final HashSet<String> fileTypeExec2 = new HashSet<String>(Arrays.asList("jar", "war", "ear", "rar", "zip", "bin", "exe", "tar", "gz", "class", "sh"));
    private final HashSet<String> fileTypeConfig = new HashSet<String>(Arrays.asList("xml", "config", "cfg", "json", "iml", "ipr"));
    private final DateTimeFormatter SIMPLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    public LsCommand() {
        super("ls", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a;
        if ((a = cmdLine.nextFlag("-d", "--dir").orNull()) != null) {
            options.d = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-l", "--list").orNull()) != null) {
            options.l = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-a", "--all").orNull()) != null) {
            options.a = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-h").orNull()) != null) {
            options.h = a.getBooleanValue().get();
            return true;
        } else if (cmdLine.peek().get().isNonOption()) {
            String path = cmdLine.next(NArgName.of("file"))
                    .flatMap(NLiteral::asString).get();
            options.paths.add(path);
            options.paths.addAll(Arrays.asList(cmdLine.toStringArray()));
            cmdLine.skip();
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        ResultSuccess success = new ResultSuccess();
        success.workingDir = context.getAbsolutePath(".");
        ResultError errors = null;
        int exitCode = 0;
        if (options.paths.isEmpty()) {
            options.paths.add(context.getAbsolutePath("."));
        }
        NSession session = context.getSession();
        LinkedHashMap<NPath, ResultGroup> filesTodos = new LinkedHashMap<>();
        LinkedHashMap<NPath, ResultGroup> foldersTodos = new LinkedHashMap<>();
        for (String path : options.paths) {
            if (NBlankable.isBlank(path)) {
                if (errors == null) {
                    errors = new ResultError();
                    errors.workingDir = context.getAbsolutePath(".");
                }
                errors.result.put(path, NMsg.ofC("cannot access '%s': No such file or directory", path));
                continue;
            }
            NPath file = NPath.of(path);
            if (file == null) {
                if (errors == null) {
                    errors = new ResultError();
                    errors.workingDir = context.getAbsolutePath(".");
                }
                errors.result.put(path, NMsg.ofC("cannot access '%s': No such file or directory", path));
                continue;
            }
            file = file.toAbsolute(NPath.of(context.getDirectory()));
            if (!file.exists()) {
                exitCode = 1;
                if (errors == null) {
                    errors = new ResultError();
                    errors.workingDir = context.getAbsolutePath(".");
                }
                errors.result.put(path, NMsg.ofC("cannot access '%s': No such file or directory", file));
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
        for (Map.Entry<NPath, ResultGroup> e : filesTodos.entrySet()) {
            NPath file = e.getKey();
            ResultGroup g = e.getValue();
            g.file = build(file);
            success.result.add(g);
        }
        for (Map.Entry<NPath, ResultGroup> e : foldersTodos.entrySet()) {
            NPath file = e.getKey();
            ResultGroup g = e.getValue();
            g.children = file.stream()
                    .sorted(FILE_SORTER)
                    .map(NFunction.of(this::build).withDesc(NEDesc.of("build")))
                    .filter(
                            NPredicate.of((ResultItem b) -> options.a || !b.hidden).withDesc(NEDesc.of("all || !hidden"))
                    )
                    .toList();
            success.result.add(g);
        }
        if (success != null) {
            NPrintStream out = session.out();
            switch (session.getOutputFormat().orDefault()) {
                case XML:
                case JSON:
                case YAML:
                case TREE:
                case TSON:
                case PROPS: {
                    out.println(success
                            .result
                            .stream().collect(Collectors.toMap(x -> x.name, x -> x.children))
                    );
                    break;
                }
                case TABLE: {
                    out.println(success.result.stream()
                            .flatMap(x ->
                                    x.children == null ? Stream.empty() :
                                            x.children.stream().map(y -> {
                                                Map m = (Map) NElements.of().destruct(y);
                                                m.put("group", x.name);
                                                return m;
                                            })).collect(Collectors.toList()));
                    break;
                }
                case PLAIN: {
                    boolean first = true;
                    Map<String, Integer> sizes = new HashMap<>();
                    for (ResultGroup resultGroup : success.result) {
                        if (resultGroup.children != null) {
                            for (ResultItem resultItem : resultGroup.children) {
                                sizes.put("owner", Math.max(4, Math.max((String.valueOf(resultItem.owner)).length(), NUtils.firstNonNull(sizes.get("owner"), 0))));
                                sizes.put("group", Math.max(4, Math.max((String.valueOf(resultItem.group)).length(), NUtils.firstNonNull(sizes.get("group"), 0))));
                            }
                        }
                    }
                    for (ResultGroup resultGroup : success.result) {
                        boolean wasFirst = first;
                        first = false;
                        if (resultGroup.children != null) {
                            if (!wasFirst) {
                                out.println();
                            }
                            if (options.paths.size() > 1) {
                                out.println(NMsg.ofC("%s:", resultGroup.name));
                            }
                            for (ResultItem resultItem : resultGroup.children) {
                                printPlain(resultItem, options, out, sizes, session);
                            }
                        } else {
                            printPlain(resultGroup.file, options, out, sizes, session);
                        }
                    }
                    break;
                }
            }
        }
        if (errors != null) {
            // if plain
//            ResultError s = context.getResult();
//            for (Map.Entry<String, NMsg> e : s.result.entrySet()) {
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

    private void printPlain(ResultItem item, Options options, NPrintStream out, Map<String, Integer> sizes, NSession session) {
        if (options.l) {
            String owner = String.format("%-" + (Math.max(4, Math.max(NUtils.firstNonNull(sizes.get("owner"), 0), NStringUtils.trim(item.owner).length()))) + "s", NStringUtils.trim(item.owner));
            String group = String.format("%-" + (Math.max(4, Math.max(NUtils.firstNonNull(sizes.get("group"), 0), NStringUtils.trim(item.group).length()))) + "s", NStringUtils.trim(item.group));
            out.print(NMsg.ofC("%s%s  %s %s %s %s ",
                    item.type, item.uperms != null ? item.uperms : item.jperms,
                    owner,
                    group   ,
                    options.h ? options.byteFormat.format(item.length) : String.format("%9d", item.length),
                    item.modified == null ? "" : SIMPLE_DATE_FORMAT.format(item.modified)
            ));
        }
        String name = NPath.of(item.path).getName();
        NTexts text = NTexts.of();
        if (item.hidden) {
            out.println(text.ofStyled(name, NTextStyle.pale()));
        } else if (item.type == 'd') {
            out.println(text.ofStyled(name, NTextStyle.primary3()));
        } else if (item.exec2 || item.jperms.charAt(2) == 'x') {
            out.println(text.ofStyled(name, NTextStyle.primary4()));
        } else if (item.config) {
            out.println(text.ofStyled(name, NTextStyle.primary5()));
        } else if (item.archive) {
            out.println(text.ofStyled(name, NTextStyle.primary1()));
        } else {
            out.println(text.ofPlain(name));
        }
    }

    private ResultItem build(NPath path) {
        ResultItem r = new ResultItem();
        r.path = path.toString();
        r.name = path.getName();
        boolean dir = path.isDirectory();
        boolean regular = path.isRegularFile();
        boolean link = path.isSymbolicLink();
        boolean other = false;
        Set<NPathPermission> permissions = path.getPermissions();
        r.jperms = (permissions.contains(NPathPermission.CAN_READ) ? "r" : "-") + (permissions.contains(NPathPermission.CAN_WRITE) ? "w" : "-") + (permissions.contains(NPathPermission.CAN_EXECUTE) ? "x" : "-");
        r.owner = path.owner();
        r.group = path.group();
        r.modified = path.getLastModifiedInstant();
        r.created = path.getCreationInstant();
        r.accessed = path.getLastAccessInstant();
        other = path.isOther();
        r.length = path.getContentLength();
        char[] perms = new char[9];
        perms[0] = permissions.contains(NPathPermission.OWNER_READ) ? 'r' : '-';
        perms[1] = permissions.contains(NPathPermission.OWNER_WRITE) ? 'w' : '-';
        perms[2] = permissions.contains(NPathPermission.OWNER_EXECUTE) ? 'x' : '-';
        perms[3] = permissions.contains(NPathPermission.GROUP_READ) ? 'r' : '-';
        perms[4] = permissions.contains(NPathPermission.GROUP_WRITE) ? 'w' : '-';
        perms[5] = permissions.contains(NPathPermission.GROUP_EXECUTE) ? 'x' : '-';
        perms[6] = permissions.contains(NPathPermission.OTHERS_READ) ? 'r' : '-';
        perms[7] = permissions.contains(NPathPermission.OTHERS_WRITE) ? 'w' : '-';
        perms[8] = permissions.contains(NPathPermission.OTHERS_EXECUTE) ? 'x' : '-';
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
        Map<String, NMsg> result = new HashMap<>();
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

    private static class FileSorter implements NComparator<NPath> {

        boolean foldersFirst = true;
        boolean groupCase = true;
//        boolean hiddenFirst = true;

        @Override
        public int compare(NPath o1, NPath o2) {
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
        public NElement describe() {
            return NElements.of().ofString("foldersFirst");
        }
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
