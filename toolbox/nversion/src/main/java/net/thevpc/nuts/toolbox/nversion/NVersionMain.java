package net.thevpc.nuts.toolbox.nversion;

import net.thevpc.nuts.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NVersionMain implements NutsApplication {

    private final List<PathVersionResolver> resolvers = new ArrayList<>();

    public NVersionMain() {
        resolvers.add(new JarPathVersionResolver());
        resolvers.add(new MavenFolderPathVersionResolver());
        resolvers.add(new ExePathVersionResolver());
    }

    public static void main(String[] args) {
        new NVersionMain().runAndExit(args);
    }

    public static NutsPath xfileOf(String expression, String cwd, NutsSession session) {
        NutsIOManager io = session.io();
        if (expression.startsWith("file:") || expression.contains("://")) {
            return io.path(expression);
        }
        return io.path(expression).builder().setBaseDir(cwd).build();
    }

    private Set<VersionDescriptor> detectVersions(String filePath, NutsApplicationContext context, NutsSession ws) throws IOException {
        for (PathVersionResolver r : resolvers) {
            Set<VersionDescriptor> x = r.resolve(filePath, context);
            if (x != null) {
                return x;
            }
        }
        try {
            Path p = Paths.get(filePath);
            if (!Files.exists(p)) {
                throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("nversion: file does not exist: %s" + p), 2);
            }
            if (Files.isDirectory(p)) {
                throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("nversion: unsupported directory: %s", p), 2);
            }
            if (Files.isRegularFile(p)) {
                throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("nversion: unsupported file: %s", filePath), 2);
            }
        } catch (NutsExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            //
        }
        throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("nversion: unsupported path: %s", filePath), 2);
    }

    @Override
    public void run(NutsApplicationContext context) {
        NutsSession session = context.getSession();
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
            if ((a = commandLine.nextBoolean("--maven")) != null) {
                maven = a.getValue().getBoolean();
            } else if ((a = commandLine.nextBoolean("--win-pe")) != null) {
                winPE = a.getValue().getBoolean();
            } else if ((a = commandLine.nextBoolean("--exe")) != null) {
                winPE = a.getValue().getBoolean();
            } else if ((a = commandLine.nextBoolean("--dll")) != null) {
                winPE = a.getValue().getBoolean();
            } else if ((a = commandLine.nextBoolean("--long")) != null) {
                longFormat = a.getValue().getBoolean();
            } else if ((a = commandLine.nextBoolean("--name")) != null) {
                nameFormat = a.getValue().getBoolean();
            } else if ((a = commandLine.nextBoolean("--sort")) != null) {
                sort = a.getValue().getBoolean();
            } else if ((a = commandLine.nextBoolean("--id")) != null) {
                idFormat = a.getValue().getBoolean();
            } else if ((a = commandLine.nextBoolean("--all")) != null) {
                all = a.getValue().getBoolean();
            } else if ((a = commandLine.nextBoolean("--table")) != null) {
                table = a.getValue().getBoolean();
            } else if ((a = commandLine.nextBoolean("--error")) != null) {
                error = a.getValue().getBoolean();
            } else if (commandLine.peek().isNonOption()) {
                a = commandLine.next();
                jarFiles.add(a.getString());
            } else {
                context.configureLast(commandLine);
            }
        }
        if (commandLine.isExecMode()) {

            for (String arg : jarFiles) {
                Set<VersionDescriptor> value = null;
                try {
                    processed++;
                    value = detectVersions(context.getSession().io()
                            .path(arg).builder().withAppBaseDir().build().toString(), context, session);
                } catch (IOException e) {
                    throw new NutsExecutionException(context.getSession(),NutsMessage.cstyle("nversion: unable to detect version for %s",arg), e, 2);
                }
                if (!value.isEmpty()) {
                    results.put(arg, value);
                }
            }
            if (processed == 0) {
                throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("nversion: missing file"), 2);
            }
            if (table && all) {
                throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("nversion: options conflict --table --all"), 1);
            }
            if (table && longFormat) {
                throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("nversion: options conflict --table --long"), 1);
            }

            NutsPrintStream out = context.getSession().out();
            NutsPrintStream err = context.getSession().out();
            NutsTextManager text = context.getSession().text();
            if (table) {
                NutsPropertiesFormat tt = context.getSession().formats().props().setSorted(sort);
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
                        File f = new File(context.getSession().io().path(t).builder().withAppBaseDir().build().toString());
                        if (f.isFile()) {
                            pp.setProperty(t, text.builder().append("<<ERROR>>", NutsTextStyle.error()).append(" unsupported file type").toString());
                        } else if (f.isDirectory()) {
                            pp.setProperty(t, text.builder().append("<<ERROR>>", NutsTextStyle.error()).append(" ignored folder").toString()
                            );
                        } else {
                            pp.setProperty(t, text.builder().append("<<ERROR>>", NutsTextStyle.error()).append(" file not found").toString()
                            );
                        }
                    }
                }
                tt.setValue(pp).print(out);
            } else {
                Set<String> keys = sort ? new TreeSet<>(results.keySet()) : new LinkedHashSet<>(results.keySet());
                for (String k : keys) {
                    if (results.size() > 1) {
                        if (longFormat || all) {
                            out.printf("%s:%n", text.ofStyled(k, NutsTextStyle.primary3()));
                        } else {
                            out.printf("%s: ", text.ofStyled(k, NutsTextStyle.primary3()));
                        }
                    }
                    Set<VersionDescriptor> v = results.get(k);
                    for (VersionDescriptor descriptor : v) {
                        if (nameFormat) {
                            out.printf("%s%n", text.ofStyled(descriptor.getId().getShortName(), NutsTextStyle.primary4()));
                        } else if (idFormat) {
                            out.printf("%s%n", text.toText(descriptor.getId()));
                        } else if (longFormat) {
                            out.printf("%s%n", text.toText(descriptor.getId()));
                            NutsPropertiesFormat f = context.getSession().formats().props()
                                    .setSorted(true);
                            f.setValue(descriptor.getProperties()).print(out);
                        } else {
                            out.printf("%s%n", text.toText(descriptor.getId().getVersion()));
                        }
                        if (!all) {
                            break;
                        }
                    }
                }
                if (error) {
                    if (!unsupportedFileTypes.isEmpty()) {
                        for (String t : unsupportedFileTypes) {
                            File f = new File(context.getSession().io().path(t).builder().withAppBaseDir().build().toString());
                            if (f.isFile()) {
                                err.printf("%s : unsupported file type%n", t);
                            } else if (f.isDirectory()) {
                                err.printf("%s : ignored folder%n", t);
                            } else {
                                err.printf("%s : file not found%n", t);
                            }
                        }
                    }
                }
            }
            if (!unsupportedFileTypes.isEmpty()) {
                throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("nversion: unsupported file types %s", unsupportedFileTypes), 3);
            }
        }
    }

}
