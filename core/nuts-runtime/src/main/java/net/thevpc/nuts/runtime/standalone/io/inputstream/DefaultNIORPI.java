package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.app.NApplication;
import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.*;

import net.thevpc.nuts.command.NExecutionEntry;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.mon.NProgressHandler;
import net.thevpc.nuts.mon.NProgressMonitor;
import net.thevpc.nuts.mon.NProgressRunner;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.platform.NGpuDevice;
import net.thevpc.nuts.platform.NGpuDeviceType;
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.io.ask.DefaultNAsk;
import net.thevpc.nuts.runtime.standalone.io.path.DefaultNPathInfo;
import net.thevpc.nuts.runtime.standalone.io.path.NPathFromSPI;
import net.thevpc.nuts.runtime.standalone.io.path.spi.FilePath;
import net.thevpc.nuts.runtime.standalone.io.path.spi.URLPath;
import net.thevpc.nuts.runtime.standalone.io.printstream.*;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNSessionTerminalFrom;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.io.util.AbstractNInputSource;
import net.thevpc.nuts.runtime.standalone.io.util.NInputStreamSource;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.NPomXmlParser;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomId;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.text.SimpleWriterOutputStream;
import net.thevpc.nuts.runtime.standalone.util.DefaultNTextCursorTracker;
import net.thevpc.nuts.runtime.standalone.util.NStringBuilderImpl;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaJarUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.runtime.standalone.xtra.idresolver.NMetaInfIdResolver;
import net.thevpc.nuts.runtime.standalone.xtra.mon.*;
import net.thevpc.nuts.runtime.standalone.xtra.time.NDefaultProgressRunner;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.base.NSystemTerminalBase;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgTemplate;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNIORPI implements NIORPI {
    public DefaultNWorkspaceConfigModel cmodel;
    public DefaultNBootModel bootModel;

    public DefaultNIORPI() {
        this.cmodel = NWorkspaceExt.of().getConfigModel();
        this.bootModel = NWorkspaceExt.of().getModel().bootModel;
    }


    @Override
    public <T> NAsk<T> createQuestion() {
        return createQuestion(NSession.of().terminal());
    }

    @Override
    public <T> NAsk<T> createQuestion(NTerminal terminal) {
        return new DefaultNAsk<>(terminal, terminal.out());
    }

    @Override
    public NMemoryPrintStream createInMemoryPrintStream() {
        return createInMemoryPrintStream(null);
    }

    @Override
    public NMemoryPrintStream createInMemoryPrintStream(NTerminalMode mode) {
        return new NByteArrayPrintStream(mode);
    }

    @Override
    public NPrintStream createPrintStream(OutputStream out, NTerminalMode expectedMode, NSystemTerminalBase term) {
        if (out == null) {
            return null;
        }
        NBootOptions woptions = NWorkspace.of().bootOptions();
        NTerminalMode expectedMode0 = woptions.terminalMode().orElse(NTerminalMode.DEFAULT);
        if (expectedMode0 == NTerminalMode.DEFAULT) {
            if (woptions.bot().orElse(false)) {
                expectedMode0 = NTerminalMode.FILTERED;
            } else {
                expectedMode0 = NTerminalMode.FORMATTED;
            }
        }
        if (expectedMode == null) {
            expectedMode = expectedMode0;
        }
        if (expectedMode == NTerminalMode.FORMATTED) {
            if (expectedMode0 == NTerminalMode.FILTERED) {
                //if nuts started with --no-color modifier, will disable FORMATTED terminal mode each time
                expectedMode = NTerminalMode.FILTERED;
            }
        }
        if (out instanceof NPrintStreamAdapter) {
            return ((NPrintStreamAdapter) out).basePrintStream().terminalMode(expectedMode);
        }
        switch (expectedMode) {
            case DEFAULT:
            case ANSI:
            case INHERITED: {
                return new NPrintStreamRaw(out, expectedMode,
                        null, null,
                        new NPrintStreamBase.Bindings(), term
                );
            }
            case FILTERED:
            case FORMATTED: {
                return new NPrintStreamRaw(out, NTerminalMode.INHERITED,
                        null, null,
                        new NPrintStreamBase.Bindings(), term
                ).terminalMode(expectedMode);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported mode %s", expectedMode));
    }
    @Override
    public NPrintStream createPrintStream(OutputStream out, NTerminalMode expectedMode, NTerminalMode baseMode) {
        if (out == null) {
            return null;
        }
        boolean baseAnsi=baseMode==NTerminalMode.ANSI;
        if (expectedMode == null) {
            expectedMode = baseAnsi?NTerminalMode.FORMATTED : NTerminalMode.FILTERED;
        }
        if (out instanceof NPrintStreamAdapter) {
            return ((NPrintStreamAdapter) out).basePrintStream().terminalMode(expectedMode);
        }
        switch (expectedMode) {
            case DEFAULT:
            case ANSI:
            case INHERITED: {
                return new NPrintStreamRaw(out, expectedMode,
                        null, null,
                        new NPrintStreamBase.Bindings(), null
                );
            }
            case FILTERED:
            case FORMATTED: {
                if(baseAnsi){
                    return new NPrintStreamRaw(out, NTerminalMode.ANSI,
                            null, null,
                            new NPrintStreamBase.Bindings(), null
                    ).terminalMode(expectedMode);
                }
                return new NPrintStreamRaw(out, NTerminalMode.INHERITED,
                        null, null,
                        new NPrintStreamBase.Bindings(), null
                ).terminalMode(expectedMode);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported mode %s", expectedMode));
    }

    @Override
    public NPrintStream createPrintStream(OutputStream out) {
        if (out instanceof NPrintStreamAdapter) {
            return ((NPrintStreamAdapter) out).basePrintStream();
        }
        return new NPrintStreamRaw(out, null, null, new NPrintStreamBase.Bindings(), null);
    }

    @Override
    public NPrintStream createPrintStream(Writer out, NTerminalMode mode) {
        return createPrintStream(out, mode, null);
    }

    @Override
    public NPrintStream createPrintStream(OutputStream out, NTerminalMode mode) {
        return createPrintStream(out, mode, (NSystemTerminalBase) null);
    }

    public NPrintStream createPrintStream(Writer out, NTerminalMode mode, NSystemTerminalBase terminal) {
        if (mode == null) {
            mode = NTerminalMode.INHERITED;
        }
        if (out == null) {
            return null;
        }
        if (out instanceof NPrintStreamAdapter) {
            return ((NPrintStreamAdapter) out).basePrintStream().terminalMode(mode);
        }
        SimpleWriterOutputStream w = new SimpleWriterOutputStream(out, terminal);
        return createPrintStream(w, mode, terminal);
    }

    @Override
    public NPrintStream createPrintStream(Writer out) {
        return createPrintStream(out, NTerminalMode.INHERITED, null);
    }

    @Override
    public NPrintStream createPrintStream(NPath out) {
        return createPrintStream(out.outputStream());
    }

    @Override
    public NPrintStream createNullPrintStream() {
        return bootModel.nullPrintStream();
    }

    @Override
    public NInputSource createInputSource(InputStream inputStream) {
        return createInputSource(inputStream, null);
    }

    @Override
    public NInputSource createInputSource(Reader inputStream, NContentMetadata metadata) {
        if (inputStream == null) {
            return null;
        }
        if (inputStream instanceof NInputSource) {
            return (NInputSource) inputStream;
        }
        return createInputSource(new ReaderInputStream(inputStream, null), metadata);
    }

    @Override
    public NInputSource createInputSource(Reader inputStream) {
        return createInputSource(inputStream, null);
    }

    @Override
    public NInputSource createInputSource(NInputStreamProvider inputStream) {
        return createInputSource(inputStream, null);
    }

    @Override
    public NInputSource createInputSource(NInputStreamProvider inputStreamProvider, NContentMetadata metadata) {
        if (inputStreamProvider == null) {
            return null;
        }
        if (inputStreamProvider instanceof NInputSource) {
            if (metadata == null) {
                return (NInputSource) inputStreamProvider;
            }
            NInputSource o = (NInputSource) inputStreamProvider;
            return new AbstractNInputSource() {
                @Override
                public boolean isMultiRead() {
                    return o.isMultiRead();
                }

                @Override
                public boolean isKnownContentLength() {
                    return o.isKnownContentLength();
                }

                @Override
                public long contentLength() {
                    return o.contentLength();
                }

                @Override
                public NContentMetadata metaData() {
                    return metadata;
                }

                @Override
                public InputStream inputStream() {
                    return o.inputStream();
                }
            };
        }
        if (metadata == null) {
            DefaultNContentMetadata metadata2 = new DefaultNContentMetadata(NMsg.ofP("Provider"), null, null, null, null);
            return new InputStreamProviderToNInputSourceAdapter(metadata2, inputStreamProvider);
        }
        return new InputStreamProviderToNInputSourceAdapter(metadata, inputStreamProvider);
    }

    @Override
    public NInputSource createInputSource(NReaderProvider readerProvider, NContentMetadata metadata) {
        if (readerProvider == null) {
            return null;
        }
        if (readerProvider instanceof NInputSource) {
            if (metadata == null) {
                return (NInputSource) readerProvider;
            }
            NInputSource o = (NInputSource) readerProvider;
            return new AbstractNInputSource() {
                @Override
                public boolean isMultiRead() {
                    return o.isMultiRead();
                }

                @Override
                public boolean isKnownContentLength() {
                    return o.isKnownContentLength();
                }

                @Override
                public long contentLength() {
                    return o.contentLength();
                }

                @Override
                public NContentMetadata metaData() {
                    return metadata;
                }

                @Override
                public InputStream inputStream() {
                    return o.inputStream();
                }
            };
        }
        if (metadata == null) {
            DefaultNContentMetadata metadata2 = new DefaultNContentMetadata(NMsg.ofP("Provider"), null, null, null, null);
            return new ReaderProviderToNInputSourceAdapter(metadata2, readerProvider);
        }
        return new ReaderProviderToNInputSourceAdapter(metadata, readerProvider);
    }

    @Override
    public NInputSource createInputSource(char[] chars) {
        if (chars == null) {
            return null;
        }
        return createInputSource(new CharArrayReader(chars));
    }

    @Override
    public NInputSource createInputSource(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        return createInputSource(new StringReader(stringValue));
    }

    @Override
    public NInputSource createInputSource(InputStream inputStream, NContentMetadata metadata) {
        if (inputStream == null) {
            return null;
        }
        if (inputStream instanceof NInputSource) {
            return (NInputSource) inputStream;
        }
        if (metadata == null) {
            NText str = null;
            Long contentLength = null;
            try {
                contentLength = (long) inputStream.available();
            } catch (IOException e) {
                //just ignore error
                //throw new UncheckedIOException(e);
            }
            if (inputStream instanceof ByteArrayInputStream) {
                str = NText.ofStyled("<memory-buffer>", NTextStyle.path());
            } else {
                str = NText.ofStyled(inputStream.toString(), NTextStyle.path());
            }
            metadata = new DefaultNContentMetadata(NMsg.ofNtf(str), contentLength, null, null, null);
        }

        InputStream inputStreamExt = createInputSourceBuilder(inputStream).metadata(metadata).createInputStream();
        return new NInputStreamSource(inputStreamExt, null);
    }


    @Override
    public NInputSource createMultiRead(NInputSource source) {
        if (source.isMultiRead()) {
            return source;
        }
        NPath tf = NPath.ofTempFile();
        try (InputStream in = source.inputStream()) {
            try (OutputStream out = tf.outputStream()) {
                NIOUtils.copy(in, out, 4096);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        return tf;
    }

    @Override
    public NInputSource createInputSource(byte[] bytes) {
        return createInputSource(new ByteArrayInputStream(bytes));
    }

    @Override
    public NInputSource createEmptyInputSource() {
        return createInputSource(NullInputStream.INSTANCE);
    }

    @Override
    public NInputSource createInputSource(byte[] inputStream, NContentMetadata metadata) {
        return createInputSource(new ByteArrayInputStream(inputStream), metadata);
    }

    @Override
    public NOutputTarget createOutputTarget(OutputStream outputStream) {
        return createOutputTarget(outputStream, null);
    }

    @Override
    public NOutputTarget createOutputTarget(OutputStream outputStream, NContentMetadata metadata) {
        return new OutputTargetExt(NOutputStreamBuilder.of(outputStream)
                .metadata(metadata).createOutputStream(), null);
    }

    @Override
    public NOutputTarget createOutputTarget(Writer writer, NContentMetadata metadata) {
        if (writer == null) {
            return null;
        }
        if (writer instanceof NOutputTarget) {
            return (NOutputTarget) writer;
        }
        return createOutputTarget(new WriterOutputStream(writer, StandardCharsets.UTF_8), metadata);
    }

    @Override
    public NOutputTarget createOutputTarget(Writer writer) {
        return createOutputTarget(writer, null);
    }

    @Override
    public NOutputStreamBuilder createOutputStreamBuilder(OutputStream base) {
        return new DefaultNOutputStreamBuilder().base(base);
    }

    public NNonBlockingInputStream createNonBlockingInputStream(InputStream base) {
        return createInputSourceBuilder(base).createNonBlockingInputStream();
    }

    public NInterruptible<InputStream> createInterruptible(InputStream base) {
        return createInputSourceBuilder(base).createInterruptibleInputStream();
    }

    public NInputSourceBuilder createInputSourceBuilder(InputStream inputStream) {
        return new DefaultNInputSourceBuilder().base(inputStream);
    }

    @Override
    public NTerminal createTerminal() {
        return cmodel.createTerminal();
    }

    @Override
    public NTerminal createTerminal(InputStream in, NPrintStream out, NPrintStream err) {
        return cmodel.createTerminal(in, out, err);
    }

    @Override
    public NTerminal createTerminal(NTerminal terminal) {
        if (terminal == null) {
            return createTerminal();
        }
        if (terminal instanceof DefaultNTerminalFromSystem) {
            DefaultNTerminalFromSystem t = (DefaultNTerminalFromSystem) terminal;
            return new DefaultNTerminalFromSystem(t);
        }
        if (terminal instanceof DefaultNSessionTerminalFrom) {
            DefaultNSessionTerminalFrom t = (DefaultNSessionTerminalFrom) terminal;
            return new DefaultNSessionTerminalFrom(t);
        }
        return new DefaultNSessionTerminalFrom(terminal);
    }

    @Override
    public NTerminal createInMemoryTerminal() {
        return createInMemoryTerminal(false);
    }

    @Override
    public NTerminal createInMemoryTerminal(boolean mergeErr) {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        NMemoryPrintStream out = NMemoryPrintStream.of();
        NMemoryPrintStream err = mergeErr ? out : NMemoryPrintStream.of();
        return createTerminal(in, out, err);
    }

    @Override
    public void enableRichTerm() {
        bootModel.enableRichTerm();
    }


    @Override
    public List<NExecutionEntry> parseExecutionEntries(NPath file) {
        if (file.name().toLowerCase().endsWith(".jar")) {
            try {
                try (InputStream in = file.inputStream()) {
                    return parseExecutionEntries(in, "jar", file.toAbsolute().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        } else if (file.name().toLowerCase().endsWith(".class")) {
            try {
                try (InputStream in = file.inputStream()) {
                    return parseExecutionEntries(in, "class", file.toAbsolute().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public NTextCursorTracker createTextCursorTracker() {
        return new DefaultNTextCursorTracker();
    }

    @Override
    public NTextCursorTracker createTextCursorTracker(int tabSize, int maxRewindDepth) {
        return new DefaultNTextCursorTracker(tabSize, maxRewindDepth);
    }

    public NPath createTempFile(String name) {
        return createAnyTempFile(name, false, null);
    }

    @Override
    public NPath createTempFolder(String name) {
        return createAnyTempFile(name, true, null);
    }

    @Override
    public NPath createTempFile() {
        return createAnyTempFile(null, false, null);
    }

    @Override
    public NPath createTempFolder() {
        return createAnyTempFile(null, true, null);
    }


    public NPath createTempRepositoryFile(String name, NRepository repository) {
        return createAnyTempFile(name, false, resolveRootPath(repository));
    }

    @Override
    public NPath createTempRepositoryFolder(String name, NRepository repository) {
        return createAnyTempFile(name, true, resolveRootPath(repository));
    }

    @Override
    public NPath createTempRepositoryFile(NRepository repository) {
        return createAnyTempFile(null, false, resolveRootPath(repository));
    }

    @Override
    public NPath createTempRepositoryFolder(NRepository repository) {
        return createAnyTempFile(null, true, resolveRootPath(repository));
    }


    @Override
    public NPath createTempIdFile(String name, NId repository) {
        return createAnyTempFile(name, false, resolveRootPath(repository));
    }

    @Override
    public NPath createTempIdFolder(String name, NId repository) {
        return createAnyTempFile(name, true, resolveRootPath(repository));
    }

    @Override
    public NPath createTempIdFile(NId repository) {
        return createAnyTempFile(null, false, resolveRootPath(repository));
    }

    @Override
    public NPath createTempIdFolder(NId repository) {
        return createAnyTempFile(null, true, resolveRootPath(repository));
    }

    private NPath resolveRootPath(NRepository repositoryId) {
        if (repositoryId == null) {
            return NPath.of(NStoreKey.ofTemp());
        } else {
            return NPath.of(NStoreKey.ofTemp().repo(repositoryId.uuid()));
        }
    }

    private NPath resolveRootPath(NId nId) {
        return NPath.of(NStoreKey.ofTemp(nId));
    }

    public NPath createAnyTempFile(String name, boolean folder, NPath rootFolder) {
        if (rootFolder == null) {
            rootFolder = NPath.of(NStoreKey.ofTemp());
        }
        NId appId = NApplication.of().id().orElseGet(() -> NWorkspace.of().runtimeId());
        if (appId != null) {
            rootFolder = rootFolder.resolve(NConstants.Folders.ID).resolve(NWorkspace.of().getDefaultIdBasedir(appId));
        }
        if (name == null) {
            name = "";
        }
        rootFolder.mkdirs();
        NStringBuilder ext = new NStringBuilderImpl(NIOUtils.getFileExtension(name, false, true));
        NStringBuilder prefix = new NStringBuilderImpl((ext.length() > 0) ? name.substring(0, name.length() - ext.length()) : name);
        if (ext.isEmpty() && prefix.isEmpty()) {
            prefix.append("nuts-");
            if (!folder) {
                ext.append(".tmp");
            }
        } else if (ext.isEmpty()) {
            if (!folder) {
                ext.append("-tmp");
            }
        } else if (prefix.isEmpty()) {
            prefix.append(ext);
            ext.clear();
            ext.append("-tmp");
        }
        if (!prefix.endsWith("-")) {
            prefix.append('-');
        }
        if (prefix.length() < 3) {
            if (prefix.length() < 3) {
                prefix.append('A');
                if (prefix.length() < 3) {
                    prefix.append('B');
                }
            }
        }

        if (folder) {
            for (int i = 0; i < 15; i++) {
                File temp = null;
                try {
                    temp = File.createTempFile(prefix.toString(), ext.toString(), rootFolder.toFile().get());
                    if (temp.delete() && temp.mkdir()) {
                        return NPath.of(temp.toPath())
                                .userTemporary(true);
                    }
                } catch (IOException ex) {
                    //
                }
            }
            throw new NIOException(NMsg.ofC("could not create temp directory: %s*%s", rootFolder + File.separator + prefix, ext));
        } else {
            try {
                return NPath.of(File.createTempFile(prefix.toString(), ext.toString(), rootFolder.toFile().get()).toPath())
                        .userTemporary(true);
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }
    }

    @Override
    public NPath createPath(String path) {
        return createPath(path, null);
    }

    @Override
    public NPath createPath(File path) {
        if (path == null) {
            return null;
        }
        return createPath(new FilePath(path.toPath()));
    }

    @Override
    public NPath createPath(Path path) {
        if (path == null) {
            return null;
        }
        return createPath(new FilePath(path));
    }

    @Override
    public NPath createPath(URL path) {
        if (path == null) {
            return null;
        }
        return createPath(new URLPath(path));
    }

    @Override
    public NPath createPath(String path, ClassLoader classLoader) {
        if (NStringUtils.isBlank(path)) {
            return null;
        }
        NPath p = cmodel.resolve(path, classLoader);
        if (p == null) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to resolve path from %s", path));
        }
        return p;
    }

    @Override
    public NPath createPath(NPathSPI path) {
        if (path == null) {
            return null;
        }
        return new NPathFromSPI(path);
    }


    @Override
    public NPath getStoreLocation(NStoreKey key) {
        return NWorkspaceExt.of().getModel().locationsModel.getStoreLocation(key);
    }

    @Override
    public List<NExecutionEntry> parseExecutionEntries(InputStream inputStream, String type, String sourceName) {
        if ("jar".equals(type)) {
            return JavaJarUtils.parseJarExecutionEntries(inputStream);
        } else if ("class".equals(type)) {
            NExecutionEntry u = JavaClassUtils.parseClassExecutionEntry(inputStream, sourceName);
            return u == null ? Collections.emptyList() : Collections.singletonList(u);
        }
        return Collections.emptyList();
    }

    @Override
    public List<NPath> createOrigins(Class<?> clazz) {
        return JavaClassUtils.resolveURLs(clazz).stream().map(x->NPath.of(x)).collect(Collectors.toList());
    }

    @Override
    public NOptional<NPath> createOrigin(Class<?> clazz) {
        List<NPath> c = createOrigins(clazz);
        return c.isEmpty() ?
                NOptional.ofNamedEmpty("LibPath fo "+clazz)
                : NOptional.of(c.get(0));
    }


    @Override
    public NOptional<NId> resolveId(Class<?> clazz) {
        clazz= NReflectUtils.unproxyType(clazz);
        List<NId> pomIds = resolveIds(clazz);
        NId defaultValue = null;
        if (pomIds.isEmpty()) {
            return NOptional.ofNamedEmpty("Id fo "+clazz);
        }
        if (pomIds.size() > 1) {
            NLog.of(NPomXmlParser.class)
                    .log(NMsg.ofC(
                                    "multiple ids found : %s for class %s and id %s",
                                    Arrays.asList(pomIds), clazz, defaultValue
                            ).withIntent(NMsgIntent.ALERT)
                            .withLevel(Level.FINEST));
        }
        return NOptional.of(pomIds.get(0));
    }

    @Override
    public NOptional<NId> resolveId(NPath path) {
        List<NId> pomIds = resolveIds(path);
        NId defaultValue = null;
        if (pomIds.isEmpty()) {
            return NOptional.ofNamedEmpty("Id fo "+path);
        }
        if (pomIds.size() > 1) {
            NLog.of(NPomXmlParser.class)

                    .log(NMsg.ofC(
                                    "multiple ids found : %s for path %s and id %s",
                                    Arrays.asList(pomIds), path, defaultValue
                            ).withIntent(NMsgIntent.ALERT)
                            .withLevel(Level.FINEST));
        }
        return NOptional.of(pomIds.get(0));
    }

    @Override
    public List<NId> resolveIds(NPath path) {
        LinkedHashSet<NId> all = new LinkedHashSet<>();
        NPomId[] u = MavenUtils.createPomIdResolver().resolvePomIds(path);
        all.addAll(
                Arrays.asList(new NMetaInfIdResolver().resolvePomIds(path))
        );
        for (NPomId uu : u) {
            all.add(NId.get(uu.getGroupId() + ":" + uu.getArtifactId() + "#" + uu.getVersion()).get());
        }
        return new ArrayList<>(all);
    }

    @Override
    public List<NId> resolveIds(Class<?> clazz) {
        clazz= NReflectUtils.unproxyType(clazz);
        LinkedHashSet<NId> all = new LinkedHashSet<>();
        NApp annotation = (NApp) clazz.getAnnotation(NApp.class);
        if (annotation != null) {
            if (!NBlankable.isBlank(annotation.id())) {
                all.add(NId.get(annotation.id()).get());
            }
        }
        NPomId[] u = MavenUtils.createPomIdResolver().resolvePomIds(clazz);
        all.addAll(
                Arrays.asList(new NMetaInfIdResolver().resolvePomIds(clazz))
        );
        for (NPomId uu : u) {
            all.add(NId.get(uu.getGroupId() + ":" + uu.getArtifactId() + "#" + uu.getVersion()).get());
        }
        return new ArrayList<>(all);
    }

    private class InputStreamProviderToNInputSourceAdapter extends AbstractNInputSource {
        private final NContentMetadata metadata2;
        private final NInputStreamProvider inputStreamProvider;

        public InputStreamProviderToNInputSourceAdapter(NContentMetadata metadata2, NInputStreamProvider inputStreamProvider) {
            super();
            this.metadata2 = metadata2;
            this.inputStreamProvider = inputStreamProvider;
        }

        @Override
        public boolean isMultiRead() {
            return false;
        }

        @Override
        public boolean isKnownContentLength() {
            return false;
        }

        @Override
        public long contentLength() {
            return -1;
        }

        @Override
        public NContentMetadata metaData() {
            return metadata2;
        }

        @Override
        public InputStream inputStream() {
            return inputStreamProvider.inputStream();
        }
    }

    private class ReaderProviderToNInputSourceAdapter extends AbstractNInputSource {
        private final NContentMetadata metadata2;
        private final NReaderProvider inputStreamProvider;

        public ReaderProviderToNInputSourceAdapter(NContentMetadata metadata2, NReaderProvider inputStreamProvider) {
            super();
            this.metadata2 = metadata2;
            this.inputStreamProvider = inputStreamProvider;
        }

        @Override
        public boolean isMultiRead() {
            return false;
        }

        @Override
        public boolean isKnownContentLength() {
            return false;
        }

        @Override
        public long contentLength() {
            return -1;
        }

        @Override
        public NContentMetadata metaData() {
            return metadata2;
        }

        @Override
        public InputStream inputStream() {
            return new ReaderInputStream(inputStreamProvider.reader(), null);
        }

        @Override
        public Reader asReader() {
            return inputStreamProvider.reader();
        }

        @Override
        public Reader asReader(Charset cs) {
            return inputStreamProvider.reader();
        }
    }

    @Override
    public NProgressRunner createProgressRunner() {
        return new NDefaultProgressRunner();
    }

    @Override
    public NProgressMonitor createSilentProgressMonitor() {
        return new DefaultProgressMonitor(null,
                new SilentProgressHandler(),
                null
        );
    }

    @Override
    public NOptional<NProgressMonitor> currentProgressMonitor() {
        NWorkspaceModel m = NWorkspaceExt.of().getModel();
        return NOptional.of(m.currentProgressMonitors.get());
    }

    @Override
    public boolean isSilentProgressMonitor(NProgressMonitor monitor) {
        return monitor == null || monitor.isSilent();
    }


    @Override
    public NProgressMonitor[] createSilentProgressMonitor(int count) {
        NProgressMonitor[] mon = new NProgressMonitor[count];
        for (int i = 0; i < count; i++) {
            mon[i] = createSilentProgressMonitor();
        }
        return mon;
    }

    @Override
    public NProgressMonitor createLoggerProgressMonitor(NMsgTemplate message, long freq) {
        return createLoggerProgressMonitor(message, (NLog)null).temporize(freq);
    }

    @Override
    public NProgressMonitor createLoggerProgressMonitor(NMsgTemplate message, long freq, Logger out) {
        return createLoggerProgressMonitor(message, out).temporize(freq);
    }

    @Override
    public NProgressMonitor createLoggerProgressMonitor(NMsgTemplate message, long freq, NLog out) {
        return createLoggerProgressMonitor(message, out).temporize(freq);
    }

    @Override
    public NProgressMonitor createOutProgressMonitor(long freq) {
        return createOutProgressMonitor().temporize(freq);
    }

    @Override
    public NProgressMonitor createOutProgressMonitor(NMsgTemplate message, long freq) {
        return createOutProgressMonitor(message).temporize(freq);
    }

    @Override
    public NProgressMonitor createOutProgressMonitor(NMsgTemplate message, long freq, PrintStream out) {
        return createPrintStreamProgressMonitor(message, out).temporize(freq);
    }


    @Override
    public NProgressMonitor createPrintStreamProgressMonitor(PrintStream printStream) {
        return createPrintStreamProgressMonitor(null, printStream);
    }

    @Override
    public NProgressMonitor createPrintStreamProgressMonitor(NMsgTemplate messageFormat, PrintStream printStream) {
        return new DefaultProgressMonitor(null,
                new PrintStreamProgressHandler(messageFormat, printStream),
                null
        );
    }

    @Override
    public NProgressMonitor createPrintStreamProgressMonitor(NPrintStream printStream) {
        return createPrintStreamProgressMonitor(null, printStream);
    }

    @Override
    public NProgressMonitor createPrintStreamProgressMonitor(NMsgTemplate messageFormat, NPrintStream printStream) {
        return new DefaultProgressMonitor(null,
                new NPrintStreamProgressHandler(messageFormat, printStream),
                null
        );
    }

    @Override
    public NProgressMonitor createLoggerProgressMonitor(NMsgTemplate messageFormat, Logger printStream) {
        return createLoggerProgressMonitor(messageFormat,printStream==null?null:NLog.of(printStream));
    }

    @Override
    public NProgressMonitor createLoggerProgressMonitor(NMsgTemplate messageFormat, NLog log) {
        return new DefaultProgressMonitor(null,
                new JLogProgressHandler(messageFormat, log),
                null
        );
    }

    @Override
    public NProgressMonitor createLoggerProgressMonitor(Logger logger) {
        return createLoggerProgressMonitor(null, logger);
    }

    @Override
    public NProgressMonitor createLoggerProgressMonitor(NLog logger) {
        return createLoggerProgressMonitor(null, logger);
    }

    @Override
    public NProgressMonitor createLoggerProgressMonitor(long milliseconds) {
        return createLoggerProgressMonitor().temporize(milliseconds);
    }

    @Override
    public NProgressMonitor createLoggerProgressMonitor() {
        return createLoggerProgressMonitor(null, (NLog) null);
    }

    @Override
    public NProgressMonitor createOutProgressMonitor(NMsgTemplate messageFormat) {
        return createPrintStreamProgressMonitor(messageFormat, System.out);
    }

    @Override
    public NProgressMonitor createSysOutProgressMonitor() {
        return createPrintStreamProgressMonitor(null, System.out);
    }

    @Override
    public NProgressMonitor createSysErrProgressMonitor() {
        return createPrintStreamProgressMonitor(null, System.err);
    }

    @Override
    public NProgressMonitor createSysErrProgressMonitor(NMsgTemplate messageFormat) {
        return createPrintStreamProgressMonitor(messageFormat, System.err);
    }

    @Override
    public NProgressMonitor createOutProgressMonitor() {
        return createPrintStreamProgressMonitor(null, NSession.of().out());
    }

    @Override
    public NProgressMonitor createErrProgressMonitor() {
        return createPrintStreamProgressMonitor(null, NSession.of().err());
    }

    @Override
    public NProgressMonitor createErrProgressMonitor(NMsgTemplate messageFormat) {
        return createPrintStreamProgressMonitor(messageFormat, System.err);
    }

    @Override
    public NProgressMonitor createProgressMonitor(NProgressHandler monitor) {
        if (monitor == null) {
            return createSilentProgressMonitor();
        }
        return new DefaultProgressMonitor(null, monitor, null);
    }

    @Override
    public NProgressMonitor createProgressMonitor(NProgressMonitor monitor) {
        if (monitor == null) {
            return createSilentProgressMonitor();
        }
        return monitor;
    }

    @Override
    public NPathInfo createPathInfoNotFound(String path) {
        int u = NStringUtils.lastIndexOf(path, new char[]{'/', '\\'});
        String name=u<0?path:path.substring(u+1);
        return new DefaultNPathInfo(name,path,NPathType.NOT_FOUND,null,null,-1,false,null,null, null, Collections.emptySet(),null,null);
    }

    @Override
    public NPathInfo createPathInfo(String name, String path, NPathType type, NPathType targetType, String targetPath, long size, boolean symbolicLink, Instant lastModified, Instant lastAccess, Instant creationTime, Set<NPathPermission> permissions, String owner, String group) {
        return new DefaultNPathInfo(name,path,targetType, targetType,targetPath,size,symbolicLink,lastModified,lastAccess, creationTime, permissions,owner,group);
    }

    @Override
    public NOptional<NGpuDevice> primaryGpu(List<NGpuDevice> gpus) {
        if (gpus == null || gpus.isEmpty()) {
            return NOptional.ofEmpty();
        }
        String forced = System.getProperty(NGpuDevice.PRIMARY_GPU_PROPERTY);
        if (forced != null && !forced.trim().isEmpty()) {
            String f = forced.trim();
            for (NGpuDevice g : gpus) {
                if (g != null && f.equals(g.capability(NGpuDevice.PCI_BUS_ID).orNull())) {
                    return NOptional.of(g);
                }
            }
        }
        NGpuDevice best = null;
        for (NGpuDevice g : gpus) {
            if (g == null || !g.isComputeCapable()) {
                continue;
            }
            if (best == null) {
                best = g;
            } else {
                boolean candidateDedicated = g.deviceType() == NGpuDeviceType.DEDICATED_GPU;
                boolean currentDedicated = best.deviceType() == NGpuDeviceType.DEDICATED_GPU;
                if (candidateDedicated != currentDedicated) {
                    if (candidateDedicated) {
                        best = g;
                    }
                } else {
                    long candidateMem = g.vram() == null ? -1 : g.vram().total();
                    long currentMem = best.vram() == null ? -1 : best.vram().total();
                    if (candidateMem > currentMem) {
                        best = g;
                    }
                }
            }
        }
        return best == null ? NOptional.ofEmpty() : NOptional.of(best);
    }

    @Override
    public NEnv createEnv(NConnectionString connectionString) {
        if (NBlankable.isBlank(connectionString) || NBlankable.isBlank(connectionString.host())) {
            return NExtensions.of(NEnv.class);
        }

        NConnectionStringBuilder connectionStringBuilder = connectionString.builder()
                //remove 'path' query param because target is independent of path
                .path(null);
        NConnectionString normalizedConnectionStringWithUse = connectionString.normalize();

        NConnectionString normalizedConnectionStringWithoutUse = connectionStringBuilder
                //remove 'use' query param because target is independent of transport
                .setQueryParam("use", null)
                .build();

        Map<NConnectionString, NEnv> cache = NWorkspace.of().getOrComputeProperty(NEnv.class + "::Cache", () -> (Map<NConnectionString, NEnv>) new ConcurrentHashMap<NConnectionString, NEnv>());
        return cache.computeIfAbsent(normalizedConnectionStringWithoutUse, x -> NExtensions.of().createSupported(NEnv.class, normalizedConnectionStringWithUse).get());

    }
}
