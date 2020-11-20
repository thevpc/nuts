package net.thevpc.nuts.toolbox.fileversion;

import net.thevpc.nuts.*;
import net.thevpc.common.io.FileUtils;
import net.thevpc.common.xfile.XFile;

import java.io.*;
import java.util.*;

public class NVersionMain extends NutsApplication {

    private final List<PathVersionResolver> resolvers = new ArrayList<>();

    public static void main(String[] args) {
        new NVersionMain().runAndExit(args);
    }

    public NVersionMain() {
        resolvers.add(new JarPathVersionResolver());
        resolvers.add(new MavenFolderPathVersionResolver());
        resolvers.add(new ExePathVersionResolver());
    }

    private Set<VersionDescriptor> detectVersions(String filePath, NutsApplicationContext context, NutsWorkspace ws) throws IOException {
        for (PathVersionResolver r : resolvers) {
            Set<VersionDescriptor> x = r.resolve(filePath,context);
            if (x != null) {
                return x;
            }
        }
        throw new NutsExecutionException(context.getWorkspace(), "file-version: unsupported file : " + filePath, 2);
    }

    @Override
    public void run(NutsApplicationContext context) {
        NutsWorkspace ws = context.getWorkspace();
        Set<String> unsupportedFileTypes = new HashSet<>();
        Set<String> jarFiles = new HashSet<>();
        Set<String> exeFiles = new HashSet<>();
        Map<String, Set<VersionDescriptor>> results = new HashMap<>();
        boolean maven = false;
        boolean winPE = false;
        boolean all = false;
        boolean longFormat = false;
        boolean nameFormat = false;
        boolean idFormat = false;
        boolean sort = false;
        boolean table = false;
        boolean error = false;
        NutsCommandLine commandLine = context.getCommandLine();
        NutsArgument a;
        int processed = 0;
        while (commandLine.hasNext()) {
            if (context.configureFirst(commandLine)) {
                //
            } else if ((a = commandLine.nextBoolean("--maven")) != null) {
                maven = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--win-pe")) != null) {
                winPE = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--exe")) != null) {
                winPE = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--dll")) != null) {
                winPE = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--long")) != null) {
                longFormat = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--name")) != null) {
                nameFormat = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--sort")) != null) {
                sort = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--id")) != null) {
                idFormat = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--all")) != null) {
                all = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--table")) != null) {
                table = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--error")) != null) {
                error = a.getBooleanValue();
            } else {
                a = commandLine.next();
                jarFiles.add(a.getString());
            }
        }
        if (commandLine.isExecMode()) {

            for (String arg : jarFiles) {
                Set<VersionDescriptor> value = null;
                try {
                    processed++;
                    value = detectVersions(context.getWorkspace().io().expandPath(arg), context, ws);
                } catch (IOException e) {
                    throw new NutsExecutionException(context.getWorkspace(), e, 2);
                }
                if (!value.isEmpty()) {
                    results.put(arg, value);
                }
            }
            if (processed == 0) {
                throw new NutsExecutionException(context.getWorkspace(), "file-version: Missing file", 2);
            }
            if (table && all) {
                throw new NutsExecutionException(context.getWorkspace(), "file-version: Options conflict --table --all", 1);
            }
            if (table && longFormat) {
                throw new NutsExecutionException(context.getWorkspace(), "file-version: Options conflict --table --long", 1);
            }

            PrintStream out = context.getSession().out();
            PrintStream err = context.getSession().out();

            if (table) {
                NutsPropertiesFormat tt = context.getWorkspace().formats().props().setSort(sort);
                Properties pp = new Properties();
                for (Map.Entry<String, Set<VersionDescriptor>> entry : results.entrySet()) {
                    VersionDescriptor o = entry.getValue().toArray(new VersionDescriptor[0])[0];
                    if (nameFormat) {
                        pp.setProperty(entry.getKey(), o.getId().getShortName());
                    } else if (idFormat) {
                        pp.setProperty(entry.getKey(), o.getId().toString());
                    } else if (longFormat) {
                        //should never happen
                    } else {
                        pp.setProperty(entry.getKey(), o.getId().toString());
                    }
                }
                if (error) {
                    for (String t : unsupportedFileTypes) {
                        File f = new File(context.getWorkspace().io().expandPath(t));
                        if (f.isFile()) {
                            pp.setProperty(t, "<<ERROR>> Unsupported File type");
                        } else if (f.isDirectory()) {
                            pp.setProperty(t, "<<ERROR>> Ignored Folder");
                        } else {
                            pp.setProperty(t, "<<ERROR>> File not found");
                        }
                    }
                }
                tt.setValue(pp).print(out);
            } else {
                Set<String> keys = sort ? new TreeSet<>(results.keySet()) : new LinkedHashSet<>(results.keySet());
                for (String k : keys) {
                    if (results.size() > 1) {
                        if (longFormat || all) {
                            out.printf("==%s==:%n", k);
                        } else {
                            out.printf("==%s==: ", k);
                        }
                    }
                    Set<VersionDescriptor> v = results.get(k);
                    for (VersionDescriptor descriptor : v) {
                        if (nameFormat) {
                            out.printf("[[%s]]%n", descriptor.getId().getShortName());
                        } else if (idFormat) {
                            out.printf("[[%s]]%n", descriptor.getId());
                        } else if (longFormat) {
                            out.printf("[[%s]]%n", descriptor.getId());
                            NutsPropertiesFormat f = context.getWorkspace().formats().props()
                                    .setSort(true);
                            f.setValue(descriptor.getProperties()).print(out);
                        } else {
                            out.printf("[[%s]]%n", descriptor.getId().getVersion());
                        }
                        if (!all) {
                            break;
                        }
                    }
                }
                if (error) {
                    if (!unsupportedFileTypes.isEmpty()) {
                        for (String t : unsupportedFileTypes) {
                            File f = new File(context.getWorkspace().io().expandPath(t));
                            if (f.isFile()) {
                                err.printf("%s : Unsupported File type%n", t);
                            } else if (f.isDirectory()) {
                                err.printf("%s : Ignored Folder%n", t);
                            } else {
                                err.printf("%s : File not found%n", t);
                            }
                        }
                    }
                }
            }
            if (!unsupportedFileTypes.isEmpty()) {
                throw new NutsExecutionException(context.getWorkspace(), "file-version: Unsupported File types " + unsupportedFileTypes, 3);
            }
        }
    }

    public static XFile xfileOf(String expression, String cwd) {
        if (expression.startsWith("file:") || expression.contains("://")) {
            return XFile.of(expression);
        }
        return XFile.of(FileUtils.getAbsoluteFile2(expression, cwd));
    }
}
