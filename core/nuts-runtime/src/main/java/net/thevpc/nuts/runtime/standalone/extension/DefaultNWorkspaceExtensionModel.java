/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NClassLoaderNode;
import net.thevpc.nuts.boot.NBootOptions;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.ext.NExtensionAlreadyRegisteredException;
import net.thevpc.nuts.ext.NExtensionInformation;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NServiceLoader;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.dependency.util.NClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.io.printstream.NFormattedPrintStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNSessionTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.ListMap;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspaceFactory;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceFactory;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceConfigBoot;
import net.thevpc.nuts.runtime.standalone.workspace.config.NConfigsExt;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

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

    private NLog LOG;
    private final Set<Class> SUPPORTED_EXTENSION_TYPES = new HashSet<>(
            Arrays.asList(//order is important!!because auto-wiring should follow this very order
                    //                    NutsPrintStreamFormattedNull.class,
                    NFormattedPrintStream.class,
                    NSystemTerminalBase.class,
                    NSessionTerminal.class,
                    NDescriptorContentParserComponent.class,
                    NExecutorComponent.class,
                    NInstallerComponent.class,
                    NRepositoryFactoryComponent.class,
                    NTransportComponent.class,
                    NWorkspace.class,
                    NWorkspaceArchetypeComponent.class
            )
    );
    private final ListMap<String, String> defaultWiredComponents = new ListMap<>();
    private final Set<String> exclusions = new HashSet<String>();
    private final NWorkspace ws;
    private final NBootWorkspaceFactory bootFactory;
    private final NWorkspaceFactory objectFactory;
    private DefaultNClassLoader workspaceExtensionsClassLoader;
    private Map<NURLClassLoaderKey, DefaultNClassLoader> cachedClassLoaders = new HashMap<>();
    private Map<NId, NWorkspaceExtension> extensions = new HashMap<>();
    private Set<NId> loadedExtensionIds = new LinkedHashSet<>();
    private Set<URL> loadedExtensionURLs = new LinkedHashSet<>();
    private Set<NId> unloadedExtensions = new LinkedHashSet<>();

    public DefaultNWorkspaceExtensionModel(NWorkspace ws, NBootWorkspaceFactory bootFactory,
                                           List<String> excludedExtensions) {
        this.ws = ws;
        this.objectFactory = new DefaultNWorkspaceFactory(ws);
        this.bootFactory = bootFactory;
        setExcludedExtensions(excludedExtensions);
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNWorkspaceExtensionModel.class, session);
        }
        return LOG;
    }

    public boolean isExcludedExtension(NId excluded) {
        return this.exclusions.contains(excluded.getShortName());
    }

    public void setExcludedExtensions(List<String> excluded) {
        this.exclusions.clear();
        if (excluded != null) {
            for (String ex : excluded) {
                for (String e : StringTokenizerUtils.splitDefault(ex)) {
                    NId ee = NId.of(e).orNull();
                    if (ee != null) {
                        this.exclusions.add(ee.getShortName());
                    }
                }
            }
        }
    }

    //    @Override
    public List<NExtensionInformation> findWorkspaceExtensions(NSession session) {
        return findWorkspaceExtensions(ws.getApiVersion().toString(), session);
    }

    //  @Override
    public List<NExtensionInformation> findWorkspaceExtensions(String version, NSession session) {
        if (version == null) {
            version = ws.getApiVersion().toString();
        }
        NId id = ws.getApiId().builder().setVersion(version).build();
        return findExtensions(id, "extensions", session);
    }

    //@Override
    public List<NExtensionInformation> findExtensions(String id, String extensionType, NSession session) {
        return findExtensions(NId.of(id).get(session), extensionType, session);
    }

    // @Override
    public List<NExtensionInformation> findExtensions(NId id, String extensionType, NSession session) {
        NAssert.requireNonBlank(id.getVersion(), "version", session);
        List<NExtensionInformation> ret = new ArrayList<>();
        List<String> allUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(id)) {
            String url = r + "/" + NIdUtils.resolveFilePath(id, extensionType);
            allUrls.add(url);
            URL u = expandURL(url, session);
            if (u != null) {
                NExtensionInformation[] s = new NExtensionInformation[0];
                try (Reader rr = new InputStreamReader(NPath.of(u, session).getInputStream())) {
                    s = NElements.of(session).json().parse(rr, DefaultNExtensionInformation[].class);
                } catch (IOException ex) {
                    _LOGOP(session).level(Level.SEVERE).error(ex)
                            .log(NMsg.ofJ("failed to parse NutsExtensionInformation from {0} : {1}", u, ex));
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

    public List<RegInfo> buildRegInfos(NSession session) {
        List<RegInfo> a = new ArrayList<>();
        Set<Class> loadedExtensions = getExtensionTypes(NComponent.class, session);
        for (Class extensionImpl : loadedExtensions) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                a.add(new RegInfo(extensionPointType, extensionImpl, null));
            }
        }
        return a;
    }

    public void onInitializeWorkspace(NBootOptions bOptions, ClassLoader bootClassLoader, NSession session) {
        objectFactory.discoverTypes(
                NId.of(bOptions.getRuntimeBootDependencyNode().get().getId()).get(session),
                bOptions.getRuntimeBootDependencyNode().get().getURL(),
                bootClassLoader,
                session);
        for (NClassLoaderNode idurl : bOptions.getExtensionBootDependencyNodes().orElseGet(Collections::emptyList)) {
            objectFactory.discoverTypes(
                    NId.of(idurl.getId()).get(session),
                    idurl.getURL(),
                    bootClassLoader,
                    session);
        }
        this.workspaceExtensionsClassLoader = new DefaultNClassLoader("workspaceExtensionsClassLoader", session, bootClassLoader);
    }

    //    public void registerType(RegInfo regInfo, NutsSession session) {
//        if (registerType(regInfo.extensionPointType, regInfo.extensionTypeImpl, session)) {
//            defaultWiredComponents.add(regInfo.extensionPointType.getName(), ((Class<? extends NutsComponent>) regInfo.extensionTypeImpl).getName());
//        }
//    }
//    public void registerTypes(List<RegInfo> all, NutsSession session) {
//        for (RegInfo regInfo : all) {
//            registerType(regInfo, session);
//        }
//    }
    public boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl, NSession session) {
        if (NComponent.class.isAssignableFrom(extensionPointType)) {
            if (extensionPointType.isInstance(extensionImpl)) {
                return registerInstance(extensionPointType, extensionImpl, session);
            }
            throw new ClassCastException(extensionImpl.getClass().getName());
        }
        throw new ClassCastException(NComponent.class.getName());
    }

    //    @Override
//    public NutsWorkspaceExtension addWorkspaceExtension(NutsId id, NutsSession session) {
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
    public Set<Class> discoverTypes(NId id, ClassLoader classLoader, NSession session) {
        URL url = NFetchCommand.of(id, session).setContent(true).getResultContent().toURL().get();
        return objectFactory.discoverTypes(id, url, classLoader, session);
    }

    //    @Override
//    public Set<Class> discoverTypes(ClassLoader classLoader, NutsSession session) {
//        return objectFactory.discoverTypes(classLoader);
//    }
    public <T extends NComponent, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, NSession session) {
        return createServiceLoader(serviceType, criteriaType, null);
    }

    public <T extends NComponent, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader, NSession session) {
        return new DefaultNServiceLoader<T, B>(session, serviceType, criteriaType, classLoader);
    }

    public <T extends NComponent, V> NOptional<T> createSupported(Class<T> type, V supportCriteria, NSession session) {
        return objectFactory.createComponent(type, supportCriteria, session);
    }

//    public <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters, boolean required, NutsSession session) {
//        return objectFactory.createSupported(type, supportCriteria, constructorParameterTypes, constructorParameters, required, session);
//    }

    public <T extends NComponent, V> List<T> createAllSupported(Class<T> type, V supportCriteria, NSession session) {
        return objectFactory.createComponents(type, supportCriteria, session);
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

    public <T> List<T> createAll(Class<T> type, NSession session) {
        return objectFactory.createAll(type, session);
    }

    //    @Override
//    public Set<Class> getExtensionPoints(NutsSession session) {
//        return objectFactory.getExtensionPoints();
//    }
    public Set<Class> getExtensionTypes(Class extensionPoint, NSession session) {
        return objectFactory.getExtensionTypes(extensionPoint, session);
    }

    public List<Object> getExtensionObjects(Class extensionPoint, NSession session) {
        return objectFactory.getExtensionObjects(extensionPoint);
    }

    public boolean isRegisteredType(Class extensionPointType, String name, NSession session) {
        return objectFactory.isRegisteredType(extensionPointType, name, session);
    }

    public boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl, NSession session) {
        return objectFactory.isRegisteredInstance(extensionPointType, extensionImpl, session);
    }

    public boolean registerInstance(Class extensionPointType, Object extensionImpl, NSession session) {
        if (!isRegisteredType(extensionPointType, extensionImpl.getClass().getName(), session) && !isRegisteredInstance(extensionPointType, extensionImpl, session)) {
            objectFactory.registerInstance(extensionPointType, extensionImpl, session);
            return true;
        }
        _LOGOP(session).level(Level.FINE).verb(NLogVerb.WARNING)
                .log(NMsg.ofJ("Bootstrap Extension Point {0} => {1} ignored. Already registered", extensionPointType.getName(), extensionImpl.getClass().getName()));
        return false;
    }

    public boolean registerType(Class extensionPointType, Class extensionType, NId source, NSession session) {
        if (!isRegisteredType(extensionPointType, extensionType.getName(), session)
                && !isRegisteredType(extensionPointType, extensionType, session)) {
            objectFactory.registerType(extensionPointType, extensionType, source, session);
            return true;
        }
        _LOGOP(session).level(Level.FINE).verb(NLogVerb.WARNING)
                .log(NMsg.ofJ("Bootstrap Extension Point {0} => {1} ignored. Already registered", extensionPointType.getName(), extensionType.getName()));
        return false;
    }

    public boolean isRegisteredType(Class extensionPointType, Class extensionType, NSession session) {
        return objectFactory.isRegisteredType(extensionPointType, extensionType, session);
    }

    public boolean isLoadedExtensions(NId id, NSession session) {
        return loadedExtensionIds.stream().anyMatch(
                x -> x.getShortName().equals(id.getShortName())
        );
    }

    public List<NId> getLoadedExtensions(NSession session) {
        return new ArrayList<>(loadedExtensionIds);
    }

    public void loadExtension(NId extension, NSession session) {
        loadExtensions(session, extension);
    }

    public void unloadExtension(NId extension, NSession session) {
        unloadExtensions(new NId[]{extension}, session);

    }

    public List<NId> getConfigExtensions(NSession session) {
        if (getStoredConfig().getExtensions() != null) {
            return Collections.unmodifiableList(new ArrayList<>(getStoredConfig().getExtensions())
                    .stream().map(NWorkspaceConfigBoot.ExtensionConfig::getId).collect(Collectors.toList()));
        }
        return Collections.emptyList();
    }

    public void loadExtensions(NSession session, NId... extensions) {
        NSessionUtils.checkSession(ws, session);
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
                    NDefinition def = NSearchCommand.of(session)
                            .addId(extension).setTargetApiVersion(ws.getApiVersion())
                            .setContent(true)
                            .setDependencies(true)
                            .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                            .setLatest(true)
                            .getResultDefinitions().findFirst().get();
                    if (def == null || def.getContent().isNotPresent()) {
                        throw new NIllegalArgumentException(session, NMsg.ofC("extension not found: %s", extension));
                    }
                    if (def.getDescriptor().getIdType() != NIdType.EXTENSION) {
                        throw new NIllegalArgumentException(session, NMsg.ofC("not an extension: %s", extension));
                    }
//                    ws.install().setSession(session).id(def.getId());
                    workspaceExtensionsClassLoader.add(NClassLoaderUtils.definitionToClassLoaderNode(def, session));
                    Set<Class> classes = objectFactory.discoverTypes(def.getId(), def.getContent().flatMap(NPath::toURL).orNull(), workspaceExtensionsClassLoader, session);
                    for (Class aClass : classes) {
                        ((NWorkspaceExt) ws).getModel().configModel.onNewComponent(aClass, session);
                    }
                    //should check current classpath
                    //and the add to classpath
                    loadedExtensionIds.add(extension);
                    _LOGOP(session).verb(NLogVerb.SUCCESS)
                            .log(NMsg.ofJ("extension {0} loaded", def.getId()
                            ));
                    someUpdates = true;
                }
            }
        }
        if (someUpdates) {
            updateLoadedExtensionURLs(session);
        }
    }

    private void updateLoadedExtensionURLs(NSession session) {
        loadedExtensionURLs.clear();
        for (NDefinition def : NSearchCommand.of(session).addIds(loadedExtensionIds.toArray(new NId[0]))
                .setTargetApiVersion(ws.getApiVersion())
                .setContent(true)
                .setDependencies(true)
                .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                .setLatest(true)
                .getResultDefinitions().toList()) {
            loadedExtensionURLs.add(def.getContent().flatMap(NPath::toURL).orNull());
        }
    }

    public void unloadExtensions(NId[] extensions, NSession session) {
        boolean someUpdates = false;
        for (NId extension : extensions) {
            NId u = loadedExtensionIds.stream().filter(
                    x -> x.getShortName().equals(extension.getShortName())
            ).findFirst().orElse(null);
            if (u != null) {
                if (session.isPlainTrace()) {
                    session.out().println(NMsg.ofC("extensions %s unloaded", u));
                }
                loadedExtensionIds.remove(u);
                unloadedExtensions.add(u);
                someUpdates = true;
            }
        }
        if (someUpdates) {
            updateLoadedExtensionURLs(session);
        }
    }

    //    @Override
    public NWorkspaceExtension[] getWorkspaceExtensions() {
        return extensions.values().toArray(new NWorkspaceExtension[0]);
    }

    public NWorkspaceExtension wireExtension(NId id, NFetchCommand options) {
        NSession session = options.getSession();
        NSessionUtils.checkSession(ws, session);
        NAssert.requireNonNull(id, "extension id", session);
        NId wired = CoreNUtils.findNutsIdBySimpleName(id, extensions.keySet());
        if (wired != null) {
            throw new NExtensionAlreadyRegisteredException(session, id, wired.toString());
        }

        _LOGOP(session).level(Level.FINE).verb(NLogVerb.ADD).log(NMsg.ofJ("installing extension {0}", id));
        NDefinition nDefinitions = NSearchCommand.of(session)
                .setAll(options)
                .addId(id)
                .setOptional(false)
                .addScope(NDependencyScopePattern.RUN)
                .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                //
                .setContent(true)
                .setDependencies(true)
                .setLatest(true)
                .getResultDefinitions().findFirst().get();
        if (!isLoadedClassPath(nDefinitions, session)) {
            this.workspaceExtensionsClassLoader.add(NClassLoaderUtils.definitionToClassLoaderNode(nDefinitions, session));
        }
        DefaultNWorkspaceExtension workspaceExtension = new DefaultNWorkspaceExtension(id, nDefinitions.getId(), this.workspaceExtensionsClassLoader);
        //now will iterate over Extension classes to wire them ...
        Set<Class> discoveredTypes = objectFactory.discoverTypes(nDefinitions.getId(), nDefinitions.getContent().flatMap(NPath::toURL).orNull(), workspaceExtension.getClassLoader(), session);
//        for (Class extensionImpl : getExtensionTypes(NutsComponent.class, session)) {
//            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
//                if (registerType(extensionPointType, extensionImpl, session)) {
//                    workspaceExtension.getWiredComponents().add(extensionPointType.getName(), ((Class<? extends NutsComponent>) extensionImpl).getName());
//                }
//            }
//        }
        extensions.put(id, workspaceExtension);
        _LOGOP(session).level(Level.FINE).verb(NLogVerb.ADD).log(NMsg.ofJ("extension {0} installed successfully", id));
        NTerminalSpec spec = new NDefaultTerminalSpec();
        if (session.getTerminal() != null) {
            spec.setProperty("ignoreClass", session.getTerminal().getClass());
        }
        NSessionTerminal newTerminal = createTerminal(spec, session);
        if (newTerminal != null) {
            _LOGOP(session).level(Level.FINE).verb(NLogVerb.UPDATE)
                    .log(NMsg.ofJ("extension {0} changed Terminal configuration. Reloading Session Terminal", id));
            session.setTerminal(newTerminal);
        }
        for (Class discoveredType : discoveredTypes) {
            if (NExtensionLifeCycle.class.isAssignableFrom(discoveredType)) {
                workspaceExtension.getEvents().add(
                        (NExtensionLifeCycle) objectFactory.createComponent(discoveredType, null, session).get()
                );
            }
        }
        for (NExtensionLifeCycle event : workspaceExtension.getEvents()) {
            event.onInitExtension(workspaceExtension, session);
        }
        return workspaceExtension;
    }

    private boolean isLoadedClassPath(NDefinition file, NSession session) {
        //session = CoreNutsUtils.validateSession(session,ws);
        if (file.getId().equalsShortId(NId.of(NConstants.Ids.NUTS_API).get(session))) {
            return true;
        }
        try {
            //            NutsDefinition file = fetch(parse.toString(), session);
            if (file.getContent().isPresent()) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file.getContent().flatMap(NPath::toPath).map(Path::toFile).get(session));
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
                            _LOGOP(session).level(Level.SEVERE)
                                    .error(ex).log(NMsg.ofJ("failed to close zip file {0} : {1}",
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

    public List<Class> resolveComponentTypes(Class o) {
        List<Class> a = new ArrayList<>();
        if (o != null) {
            HashSet<Class> v = new HashSet<>();
            Stack<Class> s = new Stack<>();
            s.push(o);
            while (!s.isEmpty()) {
                Class c = s.pop();
                v.add(c);
                if (SUPPORTED_EXTENSION_TYPES.contains(c)) {
                    a.add(c);
                }
                for (Class aa : c.getInterfaces()) {
                    if (!v.contains(aa)) {
                        s.push(aa);
                    }
                }
                Class sc = c.getSuperclass();
                if (sc != null && !v.contains(sc)) {
                    s.push(sc);
                }
            }
        }
        return a;
    }

    //    public boolean installExtensionComponentType(Class extensionPointType, Class extensionImplType, NutsSession session) {
//        if (NutsComponent.class.isAssignableFrom(extensionPointType)) {
//            if (extensionPointType.isAssignableFrom(extensionImplType)) {
//                return registerType(extensionPointType, extensionImplType, session);
//            }
//            throw new ClassCastException(extensionImplType.getName());
//        }
//        throw new ClassCastException(NutsComponent.class.getName());
//    }
    public NSessionTerminal createTerminal(NTerminalSpec spec, NSession session) {
        NSystemTerminalBase termb = createSupported(NSystemTerminalBase.class, spec, session).get();
        if (spec != null && spec.get("ignoreClass") != null && spec.get("ignoreClass").equals(termb.getClass())) {
            return null;
        }
        return new DefaultNSessionTerminalFromSystem(session, termb);
    }

    //@Override
    public URL[] getExtensionURLLocations(NId nutsId, String appId, String extensionType, NSession session) {
        List<URL> bootUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(nutsId)) {
            String url = r + "/" + NIdUtils.resolveFilePath(nutsId, extensionType);
            URL u = expandURL(url, session);
            bootUrls.add(u);
        }
        return bootUrls.toArray(new URL[0]);
    }

    //@Override
    public String[] getExtensionRepositoryLocations(NId appId) {
        //should parse this form config?
        //or should be parse from and extension component?
        String repos = NConfigs.of(NSessionUtils.defaultSession(ws))
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

    protected URL expandURL(String url, NSession session) {
        return NPath.of(url, session)
                .toAbsolute(NLocations.of(session).getWorkspaceLocation())
                .toURL().get();
    }

    private NConfigsExt configExt() {
        return NConfigsExt.of(NConfigs.of(NSessionUtils.defaultSession(ws)));
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
        return configExt().getModel().getStoredConfigBoot();
    }

    public synchronized DefaultNClassLoader getNutsURLClassLoader(String name, ClassLoader parent, NSession session) {
        if (parent == null) {
            parent = workspaceExtensionsClassLoader;
        }
        return new DefaultNClassLoader(name, session, parent);
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
        return ws;
    }


    //TODO fix me!
    public <T> T createFirst(Class<T> type, NSession session) {
        return objectFactory.createFirst(type, session);
    }

    public NWorkspaceFactory getObjectFactory() {
        return objectFactory;
    }
}
