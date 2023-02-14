package net.thevpc.nuts.toolbox.nversion;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NVersionMain implements NApplication {

    private final List<PathVersionResolver> resolvers = new ArrayList<>();

    public NVersionMain() {
        resolvers.add(new JarPathVersionResolver());
        resolvers.add(new MavenFolderPathVersionResolver());
        resolvers.add(new ExePathVersionResolver());
    }

    public static void main(String[] args) {
        new NVersionMain().runAndExit(args);
    }

    private Set<VersionDescriptor> detectVersions(String filePath, NApplicationContext context) throws IOException {
        for (PathVersionResolver r : resolvers) {
            Set<VersionDescriptor> x = r.resolve(filePath, context);
            if (x != null) {
                return x;
            }
        }
        try {
            Path p = Paths.get(filePath);
            if (!Files.exists(p)) {
                throw new NExecutionException(context.getSession(), NMsg.ofC("nversion: file does not exist: %s" , p), 2);
            }
            if (Files.isDirectory(p)) {
                throw new NExecutionException(context.getSession(), NMsg.ofC("nversion: unsupported directory: %s", p), 2);
            }
            if (Files.isRegularFile(p)) {
                throw new NExecutionException(context.getSession(), NMsg.ofC("nversion: unsupported file: %s", filePath), 2);
            }
        } catch (NExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            //
        }
        throw new NExecutionException(context.getSession(), NMsg.ofC("nversion: unsupported path: %s", filePath), 2);
    }

    @Override
    public void run(NApplicationContext context) {
        NSession session = context.getSession();
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
        NCmdLine commandLine = context.getCommandLine();
        NArg a;
        int processed = 0;
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextFlag("--maven").orNull())!=null) {
                maven = a.getBooleanValue().get(session);
            } else if ((a = commandLine.nextFlag("--win-pe").orNull())!=null) {
                winPE = a.getBooleanValue().get(session);
            } else if ((a = commandLine.nextFlag("--exe").orNull())!=null) {
                winPE = a.getBooleanValue().get(session);
            } else if ((a = commandLine.nextFlag("--dll").orNull())!=null) {
                winPE = a.getBooleanValue().get(session);
            } else if ((a = commandLine.nextFlag("--long").orNull())!=null) {
                longFormat = a.getBooleanValue().get(session);
            } else if ((a = commandLine.nextFlag("--name").orNull())!=null) {
                nameFormat = a.getBooleanValue().get(session);
            } else if ((a = commandLine.nextFlag("--sort").orNull())!=null) {
                sort = a.getBooleanValue().get(session);
            } else if ((a = commandLine.nextFlag("--id").orNull())!=null) {
                idFormat = a.getBooleanValue().get(session);
            } else if ((a = commandLine.nextFlag("--all").orNull())!=null) {
                all = a.getBooleanValue().get(session);
            } else if ((a = commandLine.nextFlag("--table").orNull())!=null) {
                table = a.getBooleanValue().get(session);
            } else if ((a = commandLine.nextFlag("--error").orNull())!=null) {
                error = a.getBooleanValue().get(session);
            } else if (commandLine.peek().get(session).isNonOption()) {
                a = commandLine.next().get(session);
                jarFiles.add(a.asString().get(session));
            } else {
                context.configureLast(commandLine);
            }
        }
        if (commandLine.isExecMode()) {

            for (String arg : jarFiles) {
                Set<VersionDescriptor> value = null;
                try {
                    processed++;
                    value = detectVersions(NPath.of(arg, session).toAbsolute().toString(), context);
                } catch (IOException e) {
                    throw new NExecutionException(session, NMsg.ofC("nversion: unable to detect version for %s",arg), e, 2);
                }
                if (!value.isEmpty()) {
                    results.put(arg, value);
                }
            }
            if (processed == 0) {
                throw new NExecutionException(session, NMsg.ofPlain("nversion: missing file"), 2);
            }
            if (table && all) {
                throw new NExecutionException(session, NMsg.ofPlain("nversion: options conflict --table --all"), 1);
            }
            if (table && longFormat) {
                throw new NExecutionException(session, NMsg.ofPlain("nversion: options conflict --table --long"), 1);
            }

            NPrintStream out = session.out();
            NPrintStream err = session.out();
            NTexts text = NTexts.of(session);
            if (table) {
                NPropertiesFormat tt = NPropertiesFormat.of(session).setSorted(sort);
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
                        File f = new File(NPath.of(t,session).toAbsolute().toString());
                        if (f.isFile()) {
                            pp.setProperty(t, text.ofBuilder().append("<<ERROR>>", NTextStyle.error()).append(" unsupported file type").toString());
                        } else if (f.isDirectory()) {
                            pp.setProperty(t, text.ofBuilder().append("<<ERROR>>", NTextStyle.error()).append(" ignored folder").toString()
                            );
                        } else {
                            pp.setProperty(t, text.ofBuilder().append("<<ERROR>>", NTextStyle.error()).append(" file not found").toString()
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
                            out.println(NMsg.ofC("%s:", text.ofStyled(k, NTextStyle.primary3())));
                        } else {
                            out.print(NMsg.ofC("%s: ", text.ofStyled(k, NTextStyle.primary3())));
                        }
                    }
                    Set<VersionDescriptor> v = results.get(k);
                    for (VersionDescriptor descriptor : v) {
                        if (nameFormat) {
                            out.println(NMsg.ofC("%s", text.ofStyled(descriptor.getId().getShortName(), NTextStyle.primary4())));
                        } else if (idFormat) {
                            out.println(NMsg.ofC("%s", text.ofText(descriptor.getId())));
                        } else if (longFormat) {
                            out.println(NMsg.ofC("%s", text.ofText(descriptor.getId())));
                            NPropertiesFormat f = NPropertiesFormat.of(session)
                                    .setSorted(true);
                            f.setValue(descriptor.getProperties()).print(out);
                        } else {
                            out.println(NMsg.ofC("%s", text.ofText(descriptor.getId().getVersion())));
                        }
                        if (!all) {
                            break;
                        }
                    }
                }
                if (error) {
                    if (!unsupportedFileTypes.isEmpty()) {
                        for (String t : unsupportedFileTypes) {
                            File f = NPath.of(t,session).toAbsolute().toFile().toFile();
                            if (f.isFile()) {
                                err.println(NMsg.ofC("%s : unsupported file type%n", t));
                            } else if (f.isDirectory()) {
                                err.println(NMsg.ofC("%s : ignored folder%n", t));
                            } else {
                                err.println(NMsg.ofC("%s : file not found%n", t));
                            }
                        }
                    }
                }
            }
            if (!unsupportedFileTypes.isEmpty()) {
                throw new NExecutionException(session, NMsg.ofC("nversion: unsupported file types %s", unsupportedFileTypes), 3);
            }
        }
    }

}
