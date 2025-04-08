/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NBootWorkspaceFactory;
import net.thevpc.nuts.NBootOptions;
import net.thevpc.nuts.NConstants;


import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.ext.NExtensionAlreadyRegisteredException;
import net.thevpc.nuts.ext.NExtensionInformation;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NServiceLoader;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.dependency.util.NClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.io.printstream.NFormattedPrintStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.util.NListValueMap;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspaceFactory;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceFactory;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceConfigBoot;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.web.NWebCli;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author thevpc
 */
public class DefaultNWorkspaceExtensionModel {

    private static Set<String> JRE_JAR_FILE_NAMES = new HashSet<>(Arrays.asList(
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
    private final NListValueMap<String, String> defaultWiredComponents = new NListValueMap<>();
    private final Set<String> exclusions = new HashSet<String>();
    private final NWorkspace workspace;
    private final NBootWorkspaceFactory bootFactory;
    private final NWorkspaceFactory objectFactory;
    private DefaultNClassLoader workspaceExtensionsClassLoader;
    private Map<NURLClassLoaderKey, DefaultNClassLoader> cachedClassLoaders = new HashMap<>();
    private Map<NId, NWorkspaceExtension> extensions = new HashMap<>();
    private Set<NId> loadedExtensionIds = new LinkedHashSet<>();
    private Set<URL> loadedExtensionURLs = new LinkedHashSet<>();
    private Set<NId> unloadedExtensions = new LinkedHashSet<>();

    public DefaultNWorkspaceExtensionModel(NWorkspace workspace, NBootWorkspaceFactory bootFactory,
                                           List<String> excludedExtensions) {
        this.workspace = workspace;
        this.objectFactory = new DefaultNWorkspaceFactory(workspace);
        this.bootFactory = bootFactory;
        setExcludedExtensions(excludedExtensions);
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNWorkspaceExtensionModel.class);
    }

    public boolean isExcludedExtension(NId excluded) {
        return this.exclusions.contains(excluded.getShortName());
    }

    public void setExcludedExtensions(List<String> excluded) {
        this.exclusions.clear();
        if (excluded != null) {
            for (String ex : excluded) {
                for (String e : StringTokenizerUtils.splitDefault(ex)) {
                    NId ee = NId.get(e).orNull();
                    if (ee != null) {
                        this.exclusions.add(ee.getShortName());
                    }
                }
            }
        }
    }

    //    @Override
    public List<NExtensionInformation> findWorkspaceExtensions() {
        return findWorkspaceExtensions(workspace.getApiVersion().toString());
    }

    //  @Override
    public List<NExtensionInformation> findWorkspaceExtensions(String version) {
        if (version == null) {
            version = workspace.getApiVersion().toString();
        }
        NId id = workspace.getApiId().builder().setVersion(version).build();
        return findExtensions(id, "extensions");
    }

    //@Override
    public List<NExtensionInformation> findExtensions(String id, String extensionType) {
        return findExtensions(NId.get(id).get(), extensionType);
    }

    // @Override
    public List<NExtensionInformation> findExtensions(NId id, String extensionType) {
        NAssert.requireNonBlank(id.getVersion(), "version");
        List<NExtensionInformation> ret = new ArrayList<>();
        List<String> allUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(id)) {
            String url = r + "/" + ExtraApiUtils.resolveFilePath(id, extensionType);
            allUrls.add(url);
            URL u = expandURL(url);
            if (u != null) {
                NExtensionInformation[] s = new NExtensionInformation[0];
                try (Reader rr = new InputStreamReader(NPath.of(u).getInputStream())) {
                    s = NElements.of().json().parse(rr, DefaultNExtensionInformation[].class);
                } catch (IOException ex) {
                    _LOGOP().level(Level.SEVERE).error(ex)
                            .log(NMsg.ofC("failed to parse NutsExtensionInformation from %s : %s", u, ex));
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
        boolean resolveClassPathUrls=false;
        if(resolveClassPathUrls) {
            URL[] urls = NClassLoaderUtils.resolveClasspathURLs(contextClassLoader);
            class PathAndUrl {
                URL url;
                NPath path;

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
            _LOG().with().verb(NLogVerb.INFO).level(Level.FINE)
                    .log(NMsg.ofC("initialize workspace extensions from %s/%s urls : %s", valid.length, urls.length, Arrays.asList(urls)));
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
        if (!bOptions.getRuntimeBootDependencyNode().isBlank()) {
            objectFactory.discoverTypes(
                    bOptions.getRuntimeBootDependencyNode().get().getId(),
                    bOptions.getRuntimeBootDependencyNode().get().getURL(),
                    bootClassLoader
            );
        }

        // discover extensions path
        for (NClassLoaderNode idurl : bOptions.getExtensionBootDependencyNodes().orElseGet(Collections::emptyList)) {
            if(idurl.getId()!=null) {
                objectFactory.discoverTypes(
                        idurl.getId(),
                        idurl.getURL(),
                        bootClassLoader
                );
            }
        }
        this.workspaceExtensionsClassLoader = new DefaultNClassLoader("workspaceExtensionsClassLoader", bootClassLoader);
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
            if (extensionPointType.isInstance(extensionImpl)) {
                return registerInstance(extensionPointType, extensionImpl);
            }
            throw new ClassCastException(extensionImpl.getClass().getName());
        }
        throw new ClassCastException(NComponent.class.getName());
    }

    //    @Override
//    public NutsWorkspaceExtension addWorkspaceExtension(NutsId id) {
//        session = NutsWorkspaceUtils.validateSession(ws, session);
//        NutsWorkspaceConfigManagerExt cfg = NutsWorkspaceConfigManagerExt.of(ws.config());
//        NutsExtensionListHelper h = new NutsExtensionListHelper(cfg.getStoredConfig().getExtensions()).save().compress().add(id);
//        v2.add(id);
//        if (!v2.equals(old)) {
//            //some updates
//            cfg.getStoredConfig().setExtensions(v2.getIds());
//            for (NutsDefinition def : ws.search().ids(v2.getIds().toArray(new NutsId[0])).getResultDefinitions()) {
//                
//            }
//            cfg.fireConfigurationChanged();
//        }
//        NutsId oldId = CoreNutsUtils.findNutsIdBySimpleName(id, extensions.keySet());
//        NutsWorkspaceExtension old = null;
//        if (oldId == null) {
//            NutsWorkspaceExtension e = wireExtension(id, ws.fetch().setFetchStratery(NutsFetchStrategy.ONLINE).setSession(session));
//            addExtension(id);
//            return e;
//        } else {
//            old = extensions.get(oldId);
//            addExtension(id);
//            return old;
//        }
//    }
    public Set<Class<? extends NComponent>> discoverTypes(NId id, ClassLoader classLoader) {
        URL url = NFetchCmd.of(id).setContent(true).getResultContent().toURL().get();
        return objectFactory.discoverTypes(id, url, classLoader);
    }

    //    @Override
//    public Set<Class> discoverTypes(ClassLoader classLoader) {
//        return objectFactory.discoverTypes(classLoader);
//    }
    public <T extends NComponent, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType) {
        return createServiceLoader(serviceType, criteriaType, null);
    }

    public <T extends NComponent, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        return new DefaultNServiceLoader<T, B>(workspace, serviceType, criteriaType, classLoader);
    }

    public <T extends NComponent, V> NOptional<T> createSupported(Class<T> type, V supportCriteria) {
        return objectFactory.createComponent(type, supportCriteria);
    }

//    public <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters, boolean required) {
//        return objectFactory.createSupported(type, supportCriteria, constructorParameterTypes, constructorParameters, required, session);
//    }

    public <T extends NComponent, V> List<T> createAllSupported(Class<T> type, V supportCriteria) {
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

    public <T extends NComponent> List<T> createAll(Class<T> type) {
        return objectFactory.createAll(type);
    }

    //    @Override
//    public Set<Class> getExtensionPoints() {
//        return objectFactory.getExtensionPoints();
//    }
    public <T extends NComponent> Set<Class<? extends T>> getExtensionTypes(Class<T> extensionPoint) {
        return objectFactory.getExtensionTypes(extensionPoint);
    }

    public <T extends NComponent> List<T> getExtensionObjects(Class<T> extensionPoint) {
        return objectFactory.getExtensionObjects(extensionPoint);
    }

    public <T extends NComponent> boolean isRegisteredType(Class<T> extensionPointType, String name) {
        return objectFactory.isRegisteredType(extensionPointType, name);
    }

    public <T extends NComponent> boolean isRegisteredInstance(Class<T> extensionPointType, T extensionImpl) {
        return objectFactory.isRegisteredInstance(extensionPointType, extensionImpl);
    }

    public <T extends NComponent> boolean registerInstance(Class<T> extensionPointType, T extensionImpl) {
        if (!isRegisteredType(extensionPointType, extensionImpl.getClass().getName()) && !isRegisteredInstance(extensionPointType, extensionImpl)) {
            objectFactory.registerInstance(extensionPointType, extensionImpl);
            return true;
        }
        _LOGOP().level(Level.FINE).verb(NLogVerb.WARNING)
                .log(NMsg.ofC("Bootstrap Extension Point %s => %s ignored. Already registered", extensionPointType.getName(), extensionImpl.getClass().getName()));
        return false;
    }

    public boolean registerType(Class extensionPointType, Class extensionType, NId source) {
        if (!isRegisteredType(extensionPointType, extensionType.getName())
                && !isRegisteredType(extensionPointType, extensionType)) {
            objectFactory.registerType(extensionPointType, extensionType, source);
            return true;
        }
        _LOGOP().level(Level.FINE).verb(NLogVerb.WARNING)
                .log(NMsg.ofC("Bootstrap Extension Point %s => %s ignored. Already registered", extensionPointType.getName(), extensionType.getName()));
        return false;
    }

    public boolean isRegisteredType(Class extensionPointType, Class extensionType) {
        return objectFactory.isRegisteredType(extensionPointType, extensionType);
    }

    public boolean isLoadedExtensions(NId id) {
        return loadedExtensionIds.stream().anyMatch(
                x -> x.getShortName().equals(id.getShortName())
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
                extension = extension.builder().setVersion("").build();
                if (loadedExtensionIds.contains(extension)) {
                    //
                } else if (unloadedExtensions.contains(extension)) {
                    //reload
                    loadedExtensionIds.add(extension);
                    someUpdates = true;
                } else {
                    //load extension
                    NDefinition def = NSearchCmd.of()
                            .addId(extension).setTargetApiVersion(workspace.getApiVersion())
                            .setContent(true)
                            .setDependencies(true)
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .setLatest(true)
                            .getResultDefinitions().findFirst().get();
                    if (def == null || def.getContent().isNotPresent()) {
                        throw new NIllegalArgumentException(NMsg.ofC("extension not found: %s", extension));
                    }
                    if (def.getDescriptor().getIdType() != NIdType.EXTENSION) {
                        throw new NIllegalArgumentException(NMsg.ofC("not an extension: %s", extension));
                    }
//                    ws.install().setSession(session).id(def.getId());
                    workspaceExtensionsClassLoader.add(NClassLoaderUtils.definitionToClassLoaderNode(def, null));
                    Set<Class<? extends NComponent>> classes = objectFactory.discoverTypes(def.getId(), def.getContent().flatMap(NPath::toURL).orNull(), workspaceExtensionsClassLoader);
                    for (Class<? extends NComponent> aClass : classes) {
                        ((NWorkspaceExt) workspace).getModel().configModel.onNewComponent(aClass);
                    }
                    //should check current classpath
                    //and the add to classpath
                    loadedExtensionIds.add(extension);
                    _LOGOP().verb(NLogVerb.SUCCESS)
                            .log(NMsg.ofC("extension %s loaded", def.getId()
                            ));
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
        for (NDefinition def : NSearchCmd.of().addIds(loadedExtensionIds.toArray(new NId[0]))
                .setTargetApiVersion(workspace.getApiVersion())
                .setContent(true)
                .setDependencies(true)
                .setDependencyFilter(NDependencyFilters.of().byRunnable())
                .setLatest(true)
                .getResultDefinitions().toList()) {
            loadedExtensionURLs.add(def.getContent().flatMap(NPath::toURL).orNull());
        }
    }

    public void unloadExtensions(NId[] extensions) {
        boolean someUpdates = false;
        for (NId extension : extensions) {
            NId u = loadedExtensionIds.stream().filter(
                    x -> x.getShortName().equals(extension.getShortName())
            ).findFirst().orElse(null);
            if (u != null) {
                NSession session=getWorkspace().currentSession();
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
    public NWorkspaceExtension[] getWorkspaceExtensions() {
        return extensions.values().toArray(new NWorkspaceExtension[0]);
    }

    public NWorkspaceExtension wireExtension(NId id, NFetchCmd options) {
        NSession session=workspace.currentSession();
        NAssert.requireNonNull(id, "extension id");
        NId wired = CoreNUtils.findNutsIdBySimpleName(id, extensions.keySet());
        if (wired != null) {
            throw new NExtensionAlreadyRegisteredException(id, wired.toString());
        }

        _LOGOP().level(Level.FINE).verb(NLogVerb.ADD).log(NMsg.ofC("installing extension %s", id));
        NDefinition nDefinitions = NSearchCmd.of()
                .copyFrom(options)
                .addId(id)
                .setOptional(false)
                .addScope(NDependencyScopePattern.RUN)
                .setDependencyFilter(NDependencyFilters.of().byRunnable())
                //
                .setContent(true)
                .setDependencies(true)
                .setLatest(true)
                .getResultDefinitions().findFirst().get();
        if (!isLoadedClassPath(nDefinitions)) {
            this.workspaceExtensionsClassLoader.add(NClassLoaderUtils.definitionToClassLoaderNode(nDefinitions, null));
        }
        DefaultNWorkspaceExtension workspaceExtension = new DefaultNWorkspaceExtension(id, nDefinitions.getId(), this.workspaceExtensionsClassLoader);
        //now will iterate over Extension classes to wire them ...
        Set<Class<? extends NComponent>> discoveredTypes = objectFactory.discoverTypes(nDefinitions.getId(), nDefinitions.getContent().flatMap(NPath::toURL).orNull(), workspaceExtension.getClassLoader());
//        for (Class extensionImpl : getExtensionTypes(NutsComponent.class, session)) {
//            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
//                if (registerType(extensionPointType, extensionImpl, session)) {
//                    workspaceExtension.getWiredComponents().add(extensionPointType.getName(), ((Class<? extends NutsComponent>) extensionImpl).getName());
//                }
//            }
//        }
        extensions.put(id, workspaceExtension);
        _LOGOP().level(Level.FINE).verb(NLogVerb.ADD).log(NMsg.ofC("extension %s installed successfully", id));
        NTerminalSpec spec = new NDefaultTerminalSpec();
        if (session.getTerminal() != null) {
            spec.setProperty("ignoreClass", session.getTerminal().getClass());
        }
        NTerminal newTerminal = createTerminal(spec);
        if (newTerminal != null) {
            _LOGOP().level(Level.FINE).verb(NLogVerb.UPDATE)
                    .log(NMsg.ofC("extension %s changed Terminal configuration. Reloading Session Terminal", id));
            session.setTerminal(newTerminal);
        }
        for (Class<? extends NComponent> discoveredType : discoveredTypes) {
            if (NExtensionLifeCycle.class.isAssignableFrom(discoveredType)) {
                workspaceExtension.getEvents().add(
                        (NExtensionLifeCycle) objectFactory.createComponent(discoveredType, null).get()
                );
            }
        }
        for (NExtensionLifeCycle event : workspaceExtension.getEvents()) {
            event.onInitExtension(workspaceExtension);
        }
        return workspaceExtension;
    }

    private boolean isLoadedClassPath(NDefinition file) {
        //session = CoreNutsUtils.validateSession(session,ws);
        if (file.getId().equalsShortId(NId.get(NConstants.Ids.NUTS_API).get())) {
            return true;
        }
        try {
            //            NutsDefinition file = fetch(parse.toString(), session);
            if (file.getContent().isPresent()) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file.getContent().flatMap(NPath::toPath).map(Path::toFile).get());
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
                            _LOGOP().level(Level.SEVERE)
                                    .error(ex).log(NMsg.ofC("failed to close zip file %s : %s",
                                            file.getContent().orNull(), ex));
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
        if (spec != null && spec.get("ignoreClass") != null && spec.get("ignoreClass").equals(termb.getClass())) {
            return null;
        }
        return new DefaultNTerminalFromSystem(termb);
    }

    //@Override
    public URL[] getExtensionURLLocations(NId nutsId, String appId, String extensionType) {
        List<URL> bootUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(nutsId)) {
            String url = r + "/" + ExtraApiUtils.resolveFilePath(nutsId, extensionType);
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
                .toAbsolute(NWorkspace.of().getWorkspaceLocation())
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

    public synchronized DefaultNClassLoader getNutsURLClassLoader(String name, ClassLoader parent) {
        if (parent == null) {
            parent = workspaceExtensionsClassLoader;
        }
        return new DefaultNClassLoader(name, parent);
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
            if (!Objects.equals(this.parent, other.parent)) {
                return false;
            }
            return true;
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
