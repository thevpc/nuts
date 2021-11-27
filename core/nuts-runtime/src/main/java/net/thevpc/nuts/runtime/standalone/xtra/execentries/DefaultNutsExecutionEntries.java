package net.thevpc.nuts.runtime.standalone.xtra.execentries;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamVisitor;
import net.thevpc.nuts.runtime.standalone.io.util.SimpleClassStream;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;

public class DefaultNutsExecutionEntries implements NutsExecutionEntries {
    private final NutsWorkspace ws;
    private NutsSession session;

    public DefaultNutsExecutionEntries(NutsSession session) {
        this.ws = session.getWorkspace();
        this.session = session;
    }

    /**
     * @param stream stream
     * @return main class type for the given
     */
    public static MainClassType getMainClassType(InputStream stream,NutsSession session) {
        final NutsRef<Boolean> mainClass = new NutsRef<>();
        final NutsRef<Boolean> nutsApp = new NutsRef<>();
        final NutsRef<String> nutsAppVer = new NutsRef<>();
        final NutsRef<String> className = new NutsRef<>();
        SimpleClassStream.Visitor cl = new SimpleClassStream.Visitor() {
            @Override
            public void visitClassDeclaration(int access, String name, String superName, String[] interfaces) {
                //v0.8.0
                //TODO remove me
                if (superName != null && superName.equals("net/thevpc/nuts/NutsApplication")) {
                    nutsApp.set(true);
                    nutsAppVer.set("0.8.0");
                    //TODO remove me
                } else if (superName != null && superName.equals("net/vpc/app/nuts/NutsApplication")) {
                    //this is nut version < 0.8.0
                    nutsApp.set(true);
                    nutsAppVer.set("0.7.0");
                }
                if (interfaces != null) {
                    for (String anInterface : interfaces) {
                        //v0.8.1
                        if (anInterface != null && anInterface.equals("net/thevpc/nuts/NutsApplication")) {
                            nutsApp.set(true);
                            nutsAppVer.set("0.8.1");
                        }
                    }
                }
                className.set(name.replace('/', '.'));
            }

            @Override
            public void visitMethod(int access, String name, String desc) {
                if (name.equals("main") && desc.equals("([Ljava/lang/String;)V")
                        && Modifier.isPublic(access)
                        && Modifier.isStatic(access)) {
                    mainClass.set(true);
                }
            }
        };
        SimpleClassStream classReader = new SimpleClassStream(new BufferedInputStream(stream), cl,session);
        if (mainClass.isSet() || nutsApp.isSet()) {
            return new MainClassType(className.get(), mainClass.isSet(), nutsApp.isSet());
        }
        return null;
    }

    @Override
    public NutsExecutionEntry[] parse(File file) {
        return parse(file.toPath());
    }

    @Override
    public NutsExecutionEntry[] parse(Path file) {
        if (file.getFileName().toString().toLowerCase().endsWith(".jar")) {
            try {
                try (InputStream in = Files.newInputStream(file)) {
                    return parse(in, "java", file.toAbsolutePath().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
        } else if (file.getFileName().toString().toLowerCase().endsWith(".class")) {
            try {
                try (InputStream in = Files.newInputStream(file)) {
                    return parse(in, "class", file.toAbsolutePath().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
        } else {
            return new NutsExecutionEntry[0];
        }
    }

    @Override
    public NutsExecutionEntry[] parse(InputStream inputStream, String type, String sourceName) {
        if ("java".equals(type)) {
            return parseJarExecutionEntries(inputStream, sourceName,session);
        } else if ("class".equals(type)) {
            NutsExecutionEntry u = parseClassExecutionEntry(inputStream, sourceName,getSession());
            return u == null ? new NutsExecutionEntry[0] : new NutsExecutionEntry[]{u};
        }
        return new NutsExecutionEntry[0];
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsExecutionEntries setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    public static NutsExecutionEntry parseClassExecutionEntry(InputStream classStream, String sourceName,NutsSession session) {
        MainClassType mainClass = null;
        try {
            mainClass = getMainClassType(classStream,session);
        } catch (Exception ex) {
            NutsLoggerOp.of(CorePlatformUtils.class,session).level(Level.FINE).error(ex)
                    .log(NutsMessage.jstyle("invalid file format {0}", sourceName));
        }
        if (mainClass != null) {
            return new DefaultNutsExecutionEntry(
                    mainClass.getName(),
                    false,
                    mainClass.isApp() && mainClass.isMain()
            );
        }
        return null;
    }

    public static NutsExecutionEntry[] parseJarExecutionEntries(InputStream jarStream, String sourceName,NutsSession session) {
        if (!(jarStream instanceof BufferedInputStream)) {
            jarStream = new BufferedInputStream(jarStream);
        }
        final List<NutsExecutionEntry> classes = new ArrayList<>();
        final List<String> manifestClass = new ArrayList<>();
        try {
            ZipUtils.visitZipStream(jarStream, new Predicate<String>() {
                @Override
                public boolean test(String path) {
                    return path.endsWith(".class")
                            || path.equals("META-INF/MANIFEST.MF");
                }
            }, new InputStreamVisitor() {
                @Override
                public boolean visit(String path, InputStream inputStream) throws IOException {
                    if (path.endsWith(".class")) {
                        NutsExecutionEntry mainClass = parseClassExecutionEntry(inputStream, path,session);
                        if (mainClass != null) {
                            classes.add(mainClass);
                        }
                    } else {
                        Manifest manifest = new Manifest(inputStream);
                        Attributes a = manifest.getMainAttributes();
                        if (a != null && a.containsKey("Main-Class")) {
                            String v = a.getValue("Main-Class");
                            if (!NutsBlankable.isBlank(v)) {
                                manifestClass.add(v);
                            }
                        }
                    }
                    return true;
                }
            });
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
        List<NutsExecutionEntry> entries = new ArrayList<>();
        String defaultEntry = null;
        if (manifestClass.size() > 0) {
            defaultEntry = manifestClass.get(0);
        }
        boolean defaultFound = false;
        for (NutsExecutionEntry entry : classes) {
            if (defaultEntry != null && defaultEntry.equals(entry.getName())) {
                entries.add(new DefaultNutsExecutionEntry(entry.getName(), true, entry.isApp()));
                defaultFound = true;
            } else {
                entries.add(entry);
            }
        }
        if (defaultEntry != null && !defaultFound) {
            NutsLoggerOp.of(CorePlatformUtils.class,session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("invalid default entry " + defaultEntry + " in " + sourceName));
//            entries.add(new DefaultNutsExecutionEntry(defaultEntry, true, false));
        }
        return entries.toArray(new NutsExecutionEntry[0]);
    }

    public static class MainClassType {

        private final String name;
        private final boolean app;
        private final boolean main;

        public MainClassType(String name, boolean main, boolean app) {
            this.name = name;
            this.app = app;
            this.main = main;
        }

        public String getName() {
            return name;
        }

        public boolean isApp() {
            return app;
        }

        public boolean isMain() {
            return main;
        }

    }
}
