/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.boot.NBootWorkspaceFactory;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.command.NSearch;
import net.thevpc.nuts.core.*;


import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.ext.NServiceLoader;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.reflect.NMutableClassLoader;
import net.thevpc.nuts.runtime.standalone.util.collections.NListMultiValueMapImpl;
import net.thevpc.nuts.spi.base.NSystemTerminalBase;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.ext.NExtensionAlreadyRegisteredException;
import net.thevpc.nuts.ext.NExtensionInformation;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.runtime.standalone.dependency.util.NClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.io.printstream.NFormattedPrintStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.util.NListMultiValueMap;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspaceFactory;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceFactory;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceConfigBoot;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.net.NWebCli;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * @author thevpc
 */
public class DefaultNWorkspaceExtensionModel {

    private static final Set<String> JRE_JAR_FILE_NAMES = new HashSet<>(Arrays.asList(
            "rt.jar",
            "charsets.jar",
            "jce.jar",
            "jfr.jar",
            "jsse.jar",
            "management-agent.jar",
            "resources.jar"
    ));
    private NLog LOG;
    private final Set<Class> SUPPORTED_EXTENSION_TYPES = new HashSet<>(
            Arrays.asList(//order is important!!because auto-wiring should follow this very order
                    //                    NutsPrintStreamFormattedNull.class,
                    NFormattedPrintStream.class,
                    NSystemTerminalBase.class,
                    NTerminal.class,
                    NDescriptorContentParserComponent.class,
                    NExecutorComponent.class,
                    NInstallerComponent.class,
                    NRepositoryFactoryComponent.class,
                    NWebCli.class,
                    NWorkspace.class,
                    NWorkspaceArchetypeComponent.class
            )
    );
    private final NListMultiValueMap<String, String> defaultWiredComponents = new NListMultiValueMapImpl<>();
    private final Set<String> exclusions = new HashSet<String>();
    private final NWorkspace workspace;
    private final NBootWorkspaceFactory bootFactory;
    private final NWorkspaceFactory objectFactory;
    private NMutableClassLoader workspaceExtensionsClassLoader;
    private final ConcurrentHashMap<CachedNutsURLClassLoaderKey, NClassLoader> cachedWorkspaceExtensionsClassLoadersImmutable = new ConcurrentHashMap<>();
    private final Map<NURLClassLoaderKey, NClassLoaderBase> cachedClassLoaders = new HashMap<>();
    private final Map<NId, NWorkspaceExtension> extensions = new HashMap<>();
    private final Set<NId> loadedExtensionIds = new LinkedHashSet<>();
    private final Set<URL> loadedExtensionURLs = new LinkedHashSet<>();
    private final Set<NId> unloadedExtensions = new LinkedHashSet<>();

    public DefaultNWorkspaceExtensionModel(NWorkspace workspace, NBootWorkspaceFactory bootFactory,
                                           List<String> excludedExtensions) {
        this.workspace = workspace;
        this.objectFactory = new DefaultNWorkspaceFactory(workspace);
        this.bootFactory = bootFactory;
        setExcludedExtensions(excludedExtensions);
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNWorkspaceExtensionModel.class);
    }

    public boolean isExcludedExtension(NId excluded) {
        return this.exclusions.contains(excluded.shortName());
    }

    public void setExcludedExtensions(List<String> excluded) {
        this.exclusions.clear();
        if (excluded != null) {
            for (String ex : excluded) {
                for (String e : StringTokenizerUtils.splitDefault(ex)) {
                    NId ee = NId.get(e).orNull();
                    if (ee != null) {
                        this.exclusions.add(ee.shortName());
                    }
                }
            }
        }
    }

    //    @Override
    public List<NExtensionInformation> findWorkspaceExtensions() {
        return findWorkspaceExtensions(workspace.apiVersion().toString());
    }

    //  @Override
    public List<NExtensionInformation> findWorkspaceExtensions(String version) {
        if (version == null) {
            version = workspace.apiVersion().toString();
        }
        NId id = workspace.apiId().builder().version(version).build();
        return findExtensions(id, "extensions");
    }

    //@Override
    public List<NExtensionInformation> findExtensions(String id, String extensionType) {
        return findExtensions(NId.get(id).get(), extensionType);
    }

    // @Override
    public List<NExtensionInformation> findExtensions(NId id, String extensionType) {
        NAssert.requireNamedNonBlank(id.version(), "version");
        List<NExtensionInformation> ret = new ArrayList<>();
        List<String> allUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(id)) {
            String url = r + "/" + id.getMavenPath(extensionType);
            allUrls.add(url);
            URL u = expandURL(url);
            if (u != null) {
                NExtensionInformation[] s = new NExtensionInformation[0];
                try (Reader rr = new InputStreamReader(NPath.of(u).inputStream())) {
                    s = NElementReader.ofJson().read(rr, DefaultNExtensionInformation[].class);
                } catch (IOException ex) {
                    _LOG()
                            .log(NMsg.ofC("failed to parse NutsExtensionInformation from %s : %s", u, ex).asError(ex));
                }
                if (s != null) {
                    for (NExtensionInformation nutsExtensionInfo : s) {
                        ((DefaultNExtensionInformation) nutsExtensionInfo).setSource(u.toString());
                        ret.add(nutsExtensionInfo);
                    }
                }
            }
        }
        boolean latestVersion = true;
        if (latestVersion && ret.size() > 1) {
            return CoreFilterUtils.filterNutsExtensionInfoByLatestVersion(ret);
        }
        return ret;
    }

    public List<RegInfo> buildRegInfos() {
        List<RegInfo> a = new ArrayList<>();
        Set<Class<? extends NComponent>> loadedExtensions = getExtensionTypes(NComponent.class);
        for (Class<? extends NComponent> extensionImpl : loadedExtensions) {
            for (Class<? extends NComponent> extensionPointType : resolveComponentTypes(extensionImpl)) {
                a.add(new RegInfo(extensionPointType, extensionImpl, null));
            }
        }
        return a;
    }

    private boolean isJRELib(NPath path) {
        String jh = System.getProperty("java.home");
        try {
            if (path.isFile()) {
                File f = path.toFile().orNull();
                if (f != null) {
                    String p = f.getPath();
                    if (
                            p.startsWith(jh + "/")
                                    || p.startsWith(jh + "\\")
                    ) {
                        return true;
                    }

                }
            }
        } catch (Exception e) {
            //
        }
        return false;
    }

    public void onInitializeWorkspace(NBootOptions bOptions, ClassLoader bootClassLoader) {
        // add discover classpath
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        boolean resolveClassPathUrls = false;
        if (resolveClassPathUrls) {
            URL[] urls = NClassLoaderUtils.resolveClasspathURLs(contextClassLoader);
            class PathAndUrl {
                final URL url;
                final NPath path;

                public PathAndUrl(URL url, NPath path) {
                    this.url = url;
                    this.path = path;
                }
            }
            PathAndUrl[] valid = Arrays.stream(urls).map(url -> {
                try {
                    NPath path = NPath.of(url);
                    if (!isJRELib(path)) {
                        return new PathAndUrl(url, path);
                    }
                } catch (Exception ex) {
                    //
                }
                return null;
            }).filter(Objects::nonNull).toArray(PathAndUrl[]::new);
            _LOG()
                    .log(NMsg.ofC("initialize workspace extensions from %s/%s urls : %s", valid.length, urls.length, Arrays.asList(urls))
                            .asFine().withIntent(NMsgIntent.NOTICE));
            for (PathAndUrl v : valid) {
                objectFactory.discoverTypes(
                        CoreNIdUtils.resolveOrGenerateIdFromFileName(v.path),
                        v.url,
                        bootClassLoader
                );
            }
        }
        objectFactory.discoverTypes(
                null,
                null,
                bootClassLoader
        );

        // discover runtime path
        if (!bOptions.runtimeBootDependencyNode().isBlank()) {
            objectFactory.discoverTypes(
                    bOptions.runtimeBootDependencyNode().get().id(),
                    bOptions.runtimeBootDependencyNode().get().url(),
                    bootClassLoader
            );
        }

        // discover extensions path
        for (NClassLoaderNode idurl : bOptions.extensionBootDependencyNodes().orElseGet(Collections::emptyList)) {
            if (idurl.id() != null) {
                objectFactory.discoverTypes(
                        idurl.id(),
                        idurl.url(),
                        bootClassLoader
                );
            }
        }
        this.workspaceExtensionsClassLoader = createMutableClassLoader("workspaceExtensionsClassLoader", bootClassLoader, new NClasspathEntry[0], null, null);
    }

    //    public void registerType(RegInfo regInfo) {
//        if (registerType(regInfo.extensionPointType, regInfo.extensionTypeImpl, session)) {
//            defaultWiredComponents.add(regInfo.extensionPointType.getName(), ((Class<? extends NutsComponent>) regInfo.extensionTypeImpl).getName());
//        }
//    }
//    public void registerTypes(List<RegInfo> all) {
//        for (RegInfo regInfo : all) {
//            registerType(regInfo, session);
//        }
//    }
    public <T extends NComponent> boolean installWorkspaceExtensionComponent(Class<T> extensionPointType, T extensionImpl) {
        if (NComponent.class.isAssignableFrom(extensionPointType)) {
            return registerInstance(extensionPointType, extensionImpl);
        }
        throw new ClassCastException(NComponent.class.getName());
    }

    public Set<Class<?>> discoverTypes(NId id, ClassLoader classLoader) {
        URL url = NFetch.of(id)
                .dependencyFilter(NDependencyFilters.of().byRunnable())
                .getResultContent().toURL().get();
        return objectFactory.discoverTypes(id, url, classLoader);
    }

    //    @Override
//    public Set<Class> discoverTypes(ClassLoader classLoader) {
//        return objectFactory.discoverTypes(classLoader);
//    }
    public <T, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType) {
        return createServiceLoader(serviceType, criteriaType, null);
    }

    public <T, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        return new DefaultNServiceLoader<T, B>(serviceType, criteriaType, classLoader);
    }

    public <T, V> NOptional<T> createSupported(Class<T> type, V supportCriteria) {
        return objectFactory.createComponent(type, supportCriteria);
    }

//    public <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters, boolean required) {
//        return objectFactory.createSupported(type, supportCriteria, constructorParameterTypes, constructorParameters, required, session);
//    }

    public <T, V> List<T> createAllSupported(Class<T> type, V supportCriteria) {
        return objectFactory.createComponents(type, supportCriteria);
    }
//    public List<Class> resolveComponentTypesOld(Class o) {
//        List<Class> a = new ArrayList<>();
//        if (o != null) {
//            for (Class extensionPointType : SUPPORTED_EXTENSION_TYPES) {
//                if (extensionPointType.isAssignableFrom(o)) {
//                    a.add(extensionPointType);
//                }
//            }
//        }
//        return a;
//    }

    public <T> List<T> createAll(Class<T> type) {
        return objectFactory.createAll(type);
    }

    //    @Override
//    public Set<Class> getExtensionPoints() {
//        return objectFactory.getExtensionPoints();
//    }
    public <T> Set<Class<? extends T>> getExtensionTypes(Class<T> extensionPoint) {
        return objectFactory.getExtensionTypes(extensionPoint);
    }

    public <T> List<T> getExtensionObjects(Class<T> extensionPoint) {
        return objectFactory.getExtensionObjects(extensionPoint);
    }

    public <T> boolean isRegisteredType(Class<T> extensionPointType, String name) {
        return objectFactory.isRegisteredType(extensionPointType, name);
    }

    public <T> boolean isRegisteredInstance(Class<T> extensionPointType, T extensionImpl) {
        return objectFactory.isRegisteredInstance(extensionPointType, extensionImpl);
    }

    public <T> boolean registerInstance(Class<T> extensionPointType, T extensionImpl) {
        if (!extensionPointType.isInstance(extensionImpl)) {
            throw new ClassCastException(extensionImpl.getClass().getName());
        }
        if (!isRegisteredType(extensionPointType, extensionImpl.getClass().getName()) && !isRegisteredInstance(extensionPointType, extensionImpl)) {
            objectFactory.registerInstance(extensionPointType, extensionImpl);
            return true;
        }
        _LOG()
                .log(NMsg.ofC("Bootstrap Extension Point %s => %s ignored. Already registered", extensionPointType.getName(), extensionImpl.getClass().getName()).asFineAlert());
        return false;
    }

    public boolean registerType(Class extensionPointType, Class extensionType, NId source) {
        if (!isRegisteredType(extensionPointType, extensionType.getName())
                && !isRegisteredType(extensionPointType, extensionType)) {
            objectFactory.registerType(extensionPointType, extensionType, source);
            return true;
        }
        _LOG()
                .log(NMsg.ofC("Bootstrap Extension Point %s => %s ignored. Already registered", extensionPointType.getName(), extensionType.getName())
                        .withLevel(Level.FINE).withIntent(NMsgIntent.ALERT)
                );
        return false;
    }

    public boolean isRegisteredType(Class extensionPointType, Class extensionType) {
        return objectFactory.isRegisteredType(extensionPointType, extensionType);
    }

    public boolean isLoadedExtensions(NId id) {
        return loadedExtensionIds.stream().anyMatch(
                x -> x.shortName().equals(id.shortName())
        );
    }

    public List<NId> getLoadedExtensions() {
        return new ArrayList<>(loadedExtensionIds);
    }

    public void loadExtension(NId extension) {
        loadExtensions(extension);
    }

    public void unloadExtension(NId extension) {
        unloadExtensions(new NId[]{extension});

    }

    public List<NId> getConfigExtensions() {
        if (getStoredConfig().getExtensions() != null) {
            return Collections.unmodifiableList(new ArrayList<>(getStoredConfig().getExtensions())
                    .stream().map(NWorkspaceConfigBoot.ExtensionConfig::getId).collect(Collectors.toList()));
        }
        return Collections.emptyList();
    }

    public void loadExtensions(NId... extensions) {
        boolean someUpdates = false;
        for (NId extension : extensions) {
            if (extension != null) {
                extension = extension.builder().version("").build();
                if (loadedExtensionIds.contains(extension)) {
                    //
                } else if (unloadedExtensions.contains(extension)) {
                    //reload
                    loadedExtensionIds.add(extension);
                    someUpdates = true;
                } else {
                    //load extension
                    NDefinition def = NSearch.of()
                            .addId(extension).targetApiVersion(workspace.apiVersion())
                            .dependencyFilter(NDependencyFilters.of().byRunnable())
                            .latest(true)
                            .getResultDefinitions().findFirst().orNull();
                    if (def == null || def.content().isNotPresent()) {
                        throw new NIllegalArgumentException(NMsg.ofC("extension not found: %s", extension));
                    }
                    if (def.descriptor().idType() != NIdType.EXTENSION) {
                        throw new NIllegalArgumentException(NMsg.ofC("not an extension: %s", extension));
                    }
//                    ws.install().setSession(session).id(def.getId());
                    workspaceExtensionsClassLoader.add(def);
                    Set<Class<?>> classes = objectFactory.discoverTypes(def.id(), def.content().flatMap(NPath::toURL).orNull(), workspaceExtensionsClassLoader.asClassLoader());
                    //should check current classpath
                    //and the add to classpath
                    loadedExtensionIds.add(extension);
                    _LOG()
                            .log(NMsg.ofC("extension %s loaded", def.id())
                                    .withIntent(NMsgIntent.SUCCESS)
                                    .withLevel(Level.CONFIG)
                            );
                    someUpdates = true;
                }
            }
        }
        if (someUpdates) {
            updateLoadedExtensionURLs();
        }
    }

    private void updateLoadedExtensionURLs() {
        loadedExtensionURLs.clear();
        for (NDefinition def : NSearch.of().addIds(loadedExtensionIds.toArray(new NId[0]))
                .targetApiVersion(workspace.apiVersion())
                .dependencyFilter(NDependencyFilters.of().byRunnable())
                .latest(true)
                .getResultDefinitions().toList()) {
            loadedExtensionURLs.add(def.content().flatMap(NPath::toURL).orNull());
        }
    }

    public void unloadExtensions(NId[] extensions) {
        boolean someUpdates = false;
        for (NId extension : extensions) {
            NId u = loadedExtensionIds.stream().filter(
                    x -> x.shortName().equals(extension.shortName())
            ).findFirst().orElse(null);
            if (u != null) {
                NSession session = getWorkspace().currentSession();
                if (session.isPlainTrace()) {
                    NOut.println(NMsg.ofC("extensions %s unloaded", u));
                }
                loadedExtensionIds.remove(u);
                unloadedExtensions.add(u);
                someUpdates = true;
            }
        }
        if (someUpdates) {
            updateLoadedExtensionURLs();
        }
    }

    //    @Override
    public NOptional<NWorkspaceExtension> getWorkspaceExtension(NId id) {
        return NOptional.ofNamed(extensions.get(id), String.valueOf(id));
    }

    public NWorkspaceExtension[] getWorkspaceExtensions() {
        return extensions.values().toArray(new NWorkspaceExtension[0]);
    }


    public static class ExtensionCacheNode {
        private String id;
        private String path;
        private NDefinition definition;
        private NId[] dependencies;

        public ExtensionCacheNode() {
        }

        public ExtensionCacheNode(String id, String path, NId[] dependencies) {
            this.id = id;
            this.path = path;
            this.dependencies = dependencies;
        }
    }

    public NWorkspaceExtension wireExtension(NId id, NFetch options) {
        NSession session = workspace.currentSession();
        NAssert.requireNamedNonNull(id, "extension id");
        NId wired = CoreNUtils.findNutsIdBySimpleName(id, extensions.keySet());
        if (wired != null) {
            throw new NExtensionAlreadyRegisteredException(id, wired.toString());
        }

        _LOG().log(NMsg.ofC("installing extension %s", id)
                .withLevel(Level.FINE).withIntent(NMsgIntent.ADD)
        );
        NPath cacheFile = NPath.of(NStoreKey.ofCache(NWorkspace.of().runtimeId())).resolve("extensions-" + id.getMavenFileName("cache"));
        ExtensionCacheNode ec = null;
//        NClassLoaderNode node = null;
        if (cacheFile.isRegularFile()) {
            try {
                ec = NElementReader.ofJson().read(cacheFile, ExtensionCacheNode.class);
            } catch (Exception ex) {
                //
            }
        }
        NId ecId;
        NPath ecPath;
        if (ec != null) {
            if (ec.id == null || ec.definition == null || ec.dependencies == null) {
                ec = null;
            }
        }
        if (ec == null) {
            NDefinition nDefinitions = NSearch.of()
                    .copyFrom(options)
                    .addId(id)
                    .dependencyFilter(NDependencyFilters.of().byRunnable())
                    //
                    .latest(true)
                    .getResultDefinitions().findFirst().get();
            ec = new ExtensionCacheNode();
            ecId = nDefinitions.id();
            ecPath = nDefinitions.content().orNull();
            ec.id = ecId.toString();
            ec.definition = nDefinitions;
            ec.path = nDefinitions.content().map(x -> x.toString()).orNull();
            ec.dependencies = nDefinitions.dependencies().stream().flatMapStream(x -> x.transitive())
                    .map(x -> x.toId())
                    .toArray(NId[]::new);
            NElementWriter.ofJson().write(ec, cacheFile);
        } else {
            ecId = NId.of(ec.id);
            ecPath = NPath.of(ec.path);
        }
        if (!isLoadedClassPath(ecId, ecPath)) {
            this.workspaceExtensionsClassLoader.add(ec.definition);
        }
        DefaultNWorkspaceExtension workspaceExtension = new DefaultNWorkspaceExtension(id, ecId, this.workspaceExtensionsClassLoader.asClassLoader());
        extensions.put(id, workspaceExtension);
        //now will iterate over Extension classes to wire them ...
        Set<Class<?>> discoveredTypes = objectFactory.discoverTypes(ecId, ecPath == null ? null : ecPath.toURL().orNull(), workspaceExtension.getClassLoader());
        for (NExtensionLifeCycle eventLifeCycle : workspaceExtension.getEventLifeCycles()) {
            eventLifeCycle.onInitExtension(workspaceExtension);
        }
//        for (Class extensionImpl : getExtensionTypes(NutsComponent.class, session)) {
//            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
//                if (registerType(extensionPointType, extensionImpl, session)) {
//                    workspaceExtension.getWiredComponents().add(extensionPointType.getName(), ((Class<? extends NutsComponent>) extensionImpl).getName());
//                }
//            }
//        }
        _LOG().log(NMsg.ofC("extension %s installed successfully", id)
                .withLevel(Level.FINE).withIntent(NMsgIntent.ADD)
        );
        NTerminalSpec spec = new NDefaultTerminalSpec();
        if (session.terminal() != null) {
            spec.property("ignoreClass", session.terminal().getClass());
        }
        NTerminal newTerminal = createTerminal(spec);
        if (newTerminal != null) {
            _LOG()
                    .log(NMsg.ofC("extension %s changed Terminal configuration. Reloading Session Terminal", id)
                            .withLevel(Level.FINE).withIntent(NMsgIntent.UPDATE)
                    );
            session.terminal(newTerminal);
        }

        return workspaceExtension;
    }

//    private ExtensionCacheNode toExtensionCacheNode(NClassLoaderNode x) {
//        return new ExtensionCacheNode(
//                x.id().longName(),
//                x.url() == null ? null : x.url().toString(),
//                x.dependencies().stream().map(y -> toExtensionCacheNode(y)).toArray(ExtensionCacheNode[]::new)
//        );
//    }

//    private NClassLoaderNode fromExtensionCacheNode(ExtensionCacheNode x) {
//        return new NDefaultClassLoaderNode(
//                NId.of(x.id),
//                NPath.of(x.path).toURL().get(),
//                true,
//                true,
//                Arrays.stream(x.dependencies).map(y -> fromExtensionCacheNode(y)).toArray(NClassLoaderNode[]::new)
//        );
//    }

    private boolean isLoadedClassPath(NDefinition file) {
        return isLoadedClassPath(file.id(), file.content().orNull());
    }

    private boolean isLoadedClassPath(NId id, NPath content) {
        //session = CoreNutsUtils.validateSession(session,ws);
        if (id.equalsShortId(NId.get(NConstants.Ids.NUTS_API).get())) {
            return true;
        }
        try {
            //            NutsDefinition file = fetch(parse.toString(), session);
            if (content != null) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(content.toFile().get());
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String zname = zipEntry.getName();
                        if (zname.endsWith(".class")) {
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            try {
                                Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(clz);
                                return true;
                            } catch (ClassNotFoundException e) {
                                return false;
                            }
                        }
                    }
                } finally {
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException ex) {
                            _LOG().log(NMsg.ofC("failed to close zip file %s : %s",
                                    content, ex).asError(ex));
                            //ignore return false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public List<Class<? extends NComponent>> resolveComponentTypes(Class<?> o) {
        List<Class<? extends NComponent>> a = new ArrayList<>();
        if (o != null) {
            HashSet<Class<?>> v = new HashSet<>();
            Stack<Class<?>> s = new Stack<>();
            s.push(o);
            while (!s.isEmpty()) {
                Class<?> c = s.pop();
                v.add(c);
                if (SUPPORTED_EXTENSION_TYPES.contains(c)) {
                    a.add((Class<? extends NComponent>) c);
                }
                for (Class<?> aa : c.getInterfaces()) {
                    if (!v.contains(aa)) {
                        s.push(aa);
                    }
                }
                Class<?> sc = c.getSuperclass();
                if (sc != null && !v.contains(sc)) {
                    s.push(sc);
                }
            }
        }
        return a;
    }

    //    public boolean installExtensionComponentType(Class extensionPointType, Class extensionImplType) {
//        if (NutsComponent.class.isAssignableFrom(extensionPointType)) {
//            if (extensionPointType.isAssignableFrom(extensionImplType)) {
//                return registerType(extensionPointType, extensionImplType, session);
//            }
//            throw new ClassCastException(extensionImplType.getName());
//        }
//        throw new ClassCastException(NutsComponent.class.getName());
//    }
    public NTerminal createTerminal(NTerminalSpec spec) {
        NSystemTerminalBase termb = createSupported(NSystemTerminalBase.class, spec).get();
        if (spec != null && spec.getProperty("ignoreClass") != null && spec.getProperty("ignoreClass").equals(termb.getClass())) {
            return null;
        }
        return new DefaultNTerminalFromSystem(termb);
    }

    //@Override
    public URL[] getExtensionURLLocations(NId nutsId, String appId, String extensionType) {
        List<URL> bootUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(nutsId)) {
            String url = r + "/" + nutsId.getMavenPath(extensionType);
            URL u = expandURL(url);
            bootUrls.add(u);
        }
        return bootUrls.toArray(new URL[0]);
    }

    //@Override
    public String[] getExtensionRepositoryLocations(NId appId) {
        //should parse this form config?
        //or should be parse from and extension component?
        String repos = workspace
                .getConfigProperty("nuts.bootstrap-repository-locations").flatMap(NLiteral::asString).orElse("") + ";" //                + NutsConstants.BootstrapURLs.LOCAL_NUTS_FOLDER
                //                + ";" + NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT
                ;
        List<String> urls = new ArrayList<>();
        for (String r : StringTokenizerUtils.splitDefault(repos)) {
            if (!NBlankable.isBlank(r)) {
                urls.add(r);
            }
        }
        return urls.toArray(new String[0]);
    }

    protected URL expandURL(String url) {
        return NPath.of(url)
                .toAbsolute(NWorkspace.of().workspaceLocation())
                .toURL().get();
    }

    //    @Override
//    public boolean addExtension(NutsId extensionId) {
//        if (extensionId == null) {
//            throw new NutsIllegalArgumentException(ws, "Invalid Extension");
//        }
//        if (!containsExtension(extensionId)) {
//            if (getStoredConfig().getExtensions() == null) {
//                getStoredConfig().setExtensions(new ArrayList<>());
//            }
//            getStoredConfig().getExtensions().add(extensionId);
//            fireConfigurationChanged();
//            return true;
//        }
//        return false;
//    }

    //    @Override
//    public boolean removeExtension(NutsId extensionId) {
//        if (extensionId == null) {
//            throw new NutsIllegalArgumentException(ws, "Invalid Extension");
//        }
//        for (NutsId extension : getExtensions()) {
//            if (extension.equalsSimpleName(extensionId)) {
//                if (getStoredConfig().getExtensions() != null) {
//                    getStoredConfig().getExtensions().remove(extension);
//                }
//                fireConfigurationChanged();
//                return true;
//
//            }
//        }
//        return false;
//    }
//    @Override
//    public boolean updateExtension(NutsId extensionId) {
//        if (extensionId == null) {
//            throw new NutsIllegalArgumentException(ws, "Invalid Extension");
//        }
//        NutsId[] extensions = getExtensions();
//        for (int i = 0; i < extensions.length; i++) {
//            NutsId extension = extensions[i];
//            if (extension.equalsSimpleName(extensionId)) {
//                extensions[i] = extensionId;
//                getStoredConfig().setExtensions(new ArrayList<>(Arrays.asList(extensions)));
//                fireConfigurationChanged();
//                return true;
//            }
//        }
//        return false;
//    }
//    @Override
//    public boolean containsExtension(NutsId extensionId) {
//        if (extensionId == null) {
//            throw new NutsIllegalArgumentException(ws, "Invalid Extension");
//        }
//        for (NutsId extension : getExtensions()) {
//            if (extension.equalsSimpleName(extension)) {
//                return true;
//            }
//        }
//        return false;
//    }
//    private void fireConfigurationChanged() {
//        configExt().fireConfigurationChanged();
//    }
    private NWorkspaceConfigBoot getStoredConfig() {
        return NWorkspaceExt.of(workspace).getConfigModel().getStoredConfigBoot();
    }

    public NMutableClassLoader getWorkspaceExtensionsClassLoader() {
        return workspaceExtensionsClassLoader;
    }

    private static class CachedNutsURLClassLoaderKey {
        private final String name;
        ClassLoader parent;
        List<NId> nodes;
        NRepositoryFilter repositoryFilter;
        NDependencyFilter dependencyFilter;

        public CachedNutsURLClassLoaderKey(String name, ClassLoader parent, NId[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
            this.name = name;
            this.parent = parent;
            this.repositoryFilter = repositoryFilter;
            this.dependencyFilter = dependencyFilter;
            this.nodes = nodes == null ? Collections.emptyList() : Arrays.stream(nodes).filter(Objects::nonNull).collect(Collectors.toList());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            CachedNutsURLClassLoaderKey that = (CachedNutsURLClassLoaderKey) o;
            return Objects.equals(name, that.name) && Objects.equals(parent, that.parent) && Objects.equals(nodes, that.nodes) && Objects.equals(repositoryFilter, that.repositoryFilter) && Objects.equals(dependencyFilter, that.dependencyFilter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, parent, repositoryFilter, dependencyFilter);
        }
    }


    public synchronized NMutableClassLoader createMutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        if (nodes == null) {
            nodes = new NClasspathEntry[0];
        } else {
            nodes = Arrays.stream(nodes).filter(Objects::nonNull).toArray(NClasspathEntry[]::new);
        }
        if (repositoryFilter == null) {
            repositoryFilter = NRepositoryFilters.of().always();
        }
        if (dependencyFilter == null) {
            dependencyFilter = NDependencyFilters.of().byRunnable(false);
        }
        return new DefaultNMutableClassLoader(name, parent, nodes, repositoryFilter, dependencyFilter);
    }

    public synchronized NClassLoader createImmutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, boolean usePreferred, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        if (nodes == null) {
            nodes = new NClasspathEntry[0];
        } else {
            nodes = Arrays.stream(nodes).filter(Objects::nonNull).toArray(NClasspathEntry[]::new);
        }
        if (repositoryFilter == null) {
            repositoryFilter = NRepositoryFilters.of().always();
        }
        if (dependencyFilter == null) {
            dependencyFilter = NDependencyFilters.of().byRunnable(false);
        }
        ClassLoader validParent = parent == null ? workspaceExtensionsClassLoader.asClassLoader() : parent;
        String validName = NStringUtils.firstNonEmpty(name, "nclassloader");
        CachedNutsURLClassLoaderKey withName = new CachedNutsURLClassLoaderKey(validName, validParent, Arrays.stream(nodes).map(x -> x.id().longId()).toArray(NId[]::new), repositoryFilter, dependencyFilter);
        CachedNutsURLClassLoaderKey withoutName = new CachedNutsURLClassLoaderKey("", validParent, Arrays.stream(nodes).map(x -> x.id().longId()).toArray(NId[]::new), repositoryFilter, dependencyFilter);
        synchronized (cachedWorkspaceExtensionsClassLoadersImmutable) {
            NClassLoader nnold = cachedWorkspaceExtensionsClassLoadersImmutable.get(withoutName);
            if (usePreferred) {
                if (nnold != null) {
                    if (Objects.equals(nnold.name(), name)) {
                        return nnold;
                    }
                }
            }
            NClassLoader wnold = cachedWorkspaceExtensionsClassLoadersImmutable.get(withName);
            if (wnold != null) {
                return wnold;
            }
            DefaultImmutableNClassLoader i = new DefaultImmutableNClassLoader(validName, validParent, nodes, repositoryFilter, dependencyFilter);
            if (nnold == null) {
                cachedWorkspaceExtensionsClassLoadersImmutable.put(withoutName, i);
            }
            cachedWorkspaceExtensionsClassLoadersImmutable.put(withName, i);
            return i;
        }
    }

    public static class RegInfo {

        Class extensionPointType;
        Class extensionTypeImpl;
        NId extensionId;

        public RegInfo(Class extensionPointType, Class extensionTypeImpl, NId extensionId) {
            this.extensionPointType = extensionPointType;
            this.extensionTypeImpl = extensionTypeImpl;
            this.extensionId = extensionId;
        }

        public NId getExtensionId() {
            return extensionId;
        }

        public Class getExtensionPointType() {
            return extensionPointType;
        }

        public Class getExtensionTypeImpl() {
            return extensionTypeImpl;
        }
    }

    private static class NURLClassLoaderKey {

        private final URL[] urls;
        private final ClassLoader parent;

        public NURLClassLoaderKey(URL[] urls, ClassLoader parent) {
            this.urls = urls;
            this.parent = parent;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 13 * hash + Arrays.deepHashCode(this.urls);
            hash = 13 * hash + Objects.hashCode(this.parent);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NURLClassLoaderKey other = (NURLClassLoaderKey) obj;
            if (!Arrays.deepEquals(this.urls, other.urls)) {
                return false;
            }
            return Objects.equals(this.parent, other.parent);
        }

    }

    public NWorkspace getWorkspace() {
        return workspace;
    }


    //TODO fix me!
    public <T extends NComponent> T createFirst(Class<T> type) {
        return objectFactory.createFirst(type);
    }

    public NWorkspaceFactory getObjectFactory() {
        return objectFactory;
    }
}
