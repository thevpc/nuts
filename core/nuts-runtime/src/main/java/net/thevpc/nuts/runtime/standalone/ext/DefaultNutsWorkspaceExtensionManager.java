/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.ext;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceFactory;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.io.NutsFormattedPrintStream;
import net.thevpc.nuts.runtime.core.DefaultNutsClassLoader;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.config.NutsWorkspaceConfigBoot;
import net.thevpc.nuts.runtime.standalone.DefaultNutsServiceLoader;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspaceFactory;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.core.terminals.DefaultNutsSessionTerminal;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.bundles.datastr.ListMap;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.NutsExecutorComponent;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.thevpc.nuts.runtime.bundles.parsers.StringTokenizerUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsClassLoaderNodeUtils;

/**
 * @author thevpc
 */
public class DefaultNutsWorkspaceExtensionManager implements NutsWorkspaceExtensionManager {

    private final NutsLogger LOG;
    private final Set<Class> SUPPORTED_EXTENSION_TYPES = new HashSet<>(
            Arrays.asList(//order is important!!because auto-wiring should follow this very order
                    //                    NutsPrintStreamFormattedNull.class,
                    NutsFormattedPrintStream.class,
                    NutsSystemTerminalBase.class,
                    NutsSessionTerminalBase.class,
                    NutsSessionTerminal.class,
                    NutsDescriptorContentParserComponent.class,
                    NutsExecutorComponent.class,
                    NutsInstallerComponent.class,
                    NutsRepositoryFactoryComponent.class,
                    NutsTransportComponent.class,
                    NutsWorkspace.class,
                    NutsWorkspaceArchetypeComponent.class
            )
    );
    private final ListMap<String, String> defaultWiredComponents = new ListMap<>();
    private final Set<String> exclusions = new HashSet<String>();
    private final NutsWorkspace ws;
    private final NutsBootWorkspaceFactory bootFactory;
    private final NutsWorkspaceFactory objectFactory;
    private DefaultNutsClassLoader workspaceExtensionsClassLoader;
    private Map<NutsURLClassLoaderKey, DefaultNutsClassLoader> cachedClassLoaders = new HashMap<>();
    private Map<NutsId, NutsWorkspaceExtension> extensions = new HashMap<>();
    private Set<NutsId> loadedExtensionIds = new LinkedHashSet<>();
    private Set<URL> loadedExtensionURLs = new LinkedHashSet<>();
    private Set<NutsId> unloadedExtensions = new LinkedHashSet<>();

    public DefaultNutsWorkspaceExtensionManager(NutsWorkspace ws, NutsBootWorkspaceFactory bootFactory, String[] excludedExtensions, NutsSession bootSession) {
        this.ws = ws;
        LOG = ws.log().of(DefaultNutsWorkspaceExtensionManager.class);
        this.objectFactory = new DefaultNutsWorkspaceFactory(ws);
        this.bootFactory = bootFactory;
        setExcludedExtensions(excludedExtensions, bootSession);
    }

    public boolean isExcludedExtension(NutsId excluded) {
        return this.exclusions.contains(excluded.getShortName());
    }

    public void setExcludedExtensions(String[] excluded, NutsSession session) {
        this.exclusions.clear();
        if (excluded != null) {
            for (String ex : excluded) {
                for (String e : StringTokenizerUtils.split(ex, ",; ")) {
                    NutsId ee = ws.id().parser().parse(e);
                    if (ee != null) {
                        this.exclusions.add(ee.getShortName());
                    }
                }
            }
        }
    }

    //    @Override
    public List<NutsExtensionInformation> findWorkspaceExtensions(NutsSession session) {
        return findWorkspaceExtensions(ws.getApiVersion(), session);
    }

    //  @Override
    public List<NutsExtensionInformation> findWorkspaceExtensions(String version, NutsSession session) {
        if (version == null) {
            version = ws.getApiVersion();
        }
        NutsId id = ws.getApiId().builder().setVersion(version).build();
        return findExtensions(id, "extensions", session);
    }

    //@Override
    public List<NutsExtensionInformation> findExtensions(String id, String extensionType, NutsSession session) {
        return findExtensions(ws.id().parser().setLenient(false).parse(id), extensionType, session);
    }

    // @Override
    public List<NutsExtensionInformation> findExtensions(NutsId id, String extensionType, NutsSession session) {
        if (id.getVersion().isBlank()) {
            throw new NutsIllegalArgumentException(ws, "missing version");
        }
        List<NutsExtensionInformation> ret = new ArrayList<>();
        List<String> allUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(id)) {
            String url = r + "/" + CoreIOUtils.getPath(id, "." + extensionType, '/');
            allUrls.add(url);
            URL u = expandURL(url);
            if (u != null) {
                NutsExtensionInformation[] s = new NutsExtensionInformation[0];
                try (Reader rr = new InputStreamReader(NutsWorkspaceUtils.of(ws).openURL(u))) {
                    s = ws.formats().element().setContentType(NutsContentType.JSON).parse(rr, DefaultNutsExtensionInformation[].class);
                } catch (IOException ex) {
                    LOG.with().session(session).level(Level.SEVERE).error(ex)
                            .log("failed to parse NutsExtensionInformation from {0} : {1}", u, ex);
                }
                if (s != null) {
                    for (NutsExtensionInformation nutsExtensionInfo : s) {
                        ((DefaultNutsExtensionInformation) nutsExtensionInfo).setSource(u.toString());
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

    public List<RegInfo> buildRegInfos(NutsSession session) {
        List<RegInfo> a = new ArrayList<>();
        Set<Class> loadedExtensions = getExtensionTypes(NutsComponent.class, session);
        for (Class extensionImpl : loadedExtensions) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                a.add(new RegInfo(extensionPointType, extensionImpl, null));
            }
        }
        return a;
    }

    public void onInitializeWorkspace(NutsWorkspaceInitInformation info, ClassLoader bootClassLoader, NutsSession session) {
        //now will iterate over Extension classes to wire them ...
        objectFactory.discoverTypes(
                ws.id().parser().parse(info.getRuntimeBootDependencyNode().getId()),
                info.getRuntimeBootDependencyNode().getURL(),
                bootClassLoader,
                session);
        for (NutsClassLoaderNode idurl : info.getExtensionBootDependencyNodes()) {
            objectFactory.discoverTypes(
                    ws.id().parser().parse(idurl.getId()),
                    idurl.getURL(),
                    bootClassLoader,
                    session);
        }
        this.workspaceExtensionsClassLoader = new DefaultNutsClassLoader("workspaceExtensionsClassLoader", ws, bootClassLoader);
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
    @Override
    public boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl, NutsSession session) {
        if (NutsComponent.class.isAssignableFrom(extensionPointType)) {
            if (extensionPointType.isInstance(extensionImpl)) {
                return registerInstance(extensionPointType, extensionImpl, session);
            }
            throw new ClassCastException(extensionImpl.getClass().getName());
        }
        throw new ClassCastException(NutsComponent.class.getName());
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
    @Override
    public Set<Class> discoverTypes(NutsId id, ClassLoader classLoader, NutsSession session) {
        URL url = ws.fetch().setId(id).setSession(session).setContent(true).getResultContent().getURL();
        return objectFactory.discoverTypes(id, url, classLoader, session);
    }

//    @Override
//    public Set<Class> discoverTypes(ClassLoader classLoader, NutsSession session) {
//        return objectFactory.discoverTypes(classLoader);
//    }
    @Override
    public <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, NutsSession session) {
        return createServiceLoader(serviceType, criteriaType, null);
    }

    @Override
    public <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader, NutsSession session) {
        return new DefaultNutsServiceLoader<T, B>(ws, serviceType, criteriaType, classLoader);
    }

    public <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, NutsSession session) {
        return objectFactory.createSupported(type, supportCriteria, session);
    }

    @Override
    public <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters, NutsSession session) {
        return objectFactory.createSupported(type, supportCriteria, constructorParameterTypes, constructorParameters, session);
    }

    @Override
    public <T extends NutsComponent<V>, V> List<T> createAllSupported(Class<T> type, V supportCriteria, NutsSession session) {
        return objectFactory.createAllSupported(type, supportCriteria, session);
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

    @Override
    public <T> List<T> createAll(Class<T> type, NutsSession session) {
        return objectFactory.createAll(type, session);
    }

//    @Override
//    public Set<Class> getExtensionPoints(NutsSession session) {
//        return objectFactory.getExtensionPoints();
//    }
    @Override
    public Set<Class> getExtensionTypes(Class extensionPoint, NutsSession session) {
        return objectFactory.getExtensionTypes(extensionPoint, session);
    }

    @Override
    public List<Object> getExtensionObjects(Class extensionPoint, NutsSession session) {
        return objectFactory.getExtensionObjects(extensionPoint);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, String name, NutsSession session) {
        return objectFactory.isRegisteredType(extensionPointType, name, session);
    }

    @Override
    public boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl, NutsSession session) {
        return objectFactory.isRegisteredInstance(extensionPointType, extensionImpl, session);
    }

    @Override
    public boolean registerInstance(Class extensionPointType, Object extensionImpl, NutsSession session) {
        if (!isRegisteredType(extensionPointType, extensionImpl.getClass().getName(), session) && !isRegisteredInstance(extensionPointType, extensionImpl, session)) {
            objectFactory.registerInstance(extensionPointType, extensionImpl, session);
            return true;
        }
        LOG.with().session(session).level(Level.FINE).verb(NutsLogVerb.WARNING).log("Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionImpl.getClass().getName()});
        return false;
    }

    @Override
    public boolean registerType(Class extensionPointType, Class extensionType, NutsId source, NutsSession session) {
        if (!isRegisteredType(extensionPointType, extensionType.getName(), session)
                && !isRegisteredType(extensionPointType, extensionType, session)) {
            objectFactory.registerType(extensionPointType, extensionType, source, session);
            return true;
        }
        LOG.with().session(session).level(Level.FINE).verb(NutsLogVerb.WARNING).log("Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionType.getName()});
        return false;
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, Class extensionType, NutsSession session) {
        return objectFactory.isRegisteredType(extensionPointType, extensionType, session);
    }

    @Override
    public boolean isLoadedExtensions(NutsId id, NutsSession session) {
        return loadedExtensionIds.stream().anyMatch(
                x -> x.getShortName().equals(id.getShortName())
        );
    }

    @Override
    public List<NutsId> getLoadedExtensions(NutsSession session) {
        return new ArrayList<>(loadedExtensionIds);
    }

    @Override
    public NutsWorkspaceExtensionManager loadExtension(NutsId extension, NutsSession session) {
        return loadExtensions(session, extension);
    }

    @Override
    public NutsWorkspaceExtensionManager unloadExtension(NutsId extension, NutsSession session) {
        unloadExtensions(new NutsId[]{extension}, session);
        return this;
    }

    @Override
    public List<NutsId> getConfigExtensions(NutsSession session) {
        if (getStoredConfig().getExtensions() != null) {
            return Collections.unmodifiableList(new ArrayList<>(getStoredConfig().getExtensions())
                    .stream().map(NutsWorkspaceConfigBoot.ExtensionConfig::getId).collect(Collectors.toList()));
        }
        return Collections.emptyList();
    }

    public NutsWorkspaceExtensionManager loadExtensions(NutsSession session, NutsId... extensions) {
        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        boolean someUpdates = false;
        for (NutsId extension : extensions) {
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
                    NutsDefinition def = ws.search()
                            .setSession(session)
                            .addId(extension).setTargetApiVersion(ws.getApiVersion())
                            .setContent(true)
                            .setDependencies(true)
                            .setDependencyFilter(ws.dependency().filter().byScope(NutsDependencyScopePattern.RUN).and(
                                    ws.dependency().filter().byOptional(false)
                            ))
                            .setLatest(true)
                            .getResultDefinitions().required();
                    if (def == null || def.getContent() == null) {
                        throw new NutsIllegalArgumentException(ws, "extension not found: " + extension);
                    }
                    if (def.getType() != NutsIdType.EXTENSION) {
                        throw new NutsIllegalArgumentException(ws, "not an extension: " + extension);
                    }
//                    ws.install().setSession(session).id(def.getId());
                    workspaceExtensionsClassLoader.add(NutsClassLoaderNodeUtils.definitionToClassLoaderNode(def, session));
                    objectFactory.discoverTypes(def.getId(), def.getContent().getURL(), workspaceExtensionsClassLoader, session);
                    //should check current classpath
                    //and the add to classpath
                    loadedExtensionIds.add(extension);
                    LOG.with().session(session).verb(NutsLogVerb.SUCCESS)
                            .style(NutsTextFormatStyle.CSTYLE).formatted(true)
                            .log("extension %s loaded", def.getId()
                            );
                    someUpdates = true;
                }
            }
        }
        if (someUpdates) {
            updateLoadedExtensionURLs(session);
        }
        return this;
    }

     private void updateLoadedExtensionURLs(NutsSession session) {
        loadedExtensionURLs.clear();
        for (NutsDefinition def : ws.search().addIds(loadedExtensionIds.toArray(new NutsId[0])).setTargetApiVersion(ws.getApiVersion())
                .setDependencies(true)
                .setDependencyFilter(ws.dependency().filter().byScope(NutsDependencyScopePattern.RUN))
                .setLatest(true)
                .setSession(session)
                .getResultDefinitions().list()) {
            loadedExtensionURLs.add(def.getContent().getURL());
        }
    }

    public NutsWorkspaceExtensionManager unloadExtensions(NutsId[] extensions, NutsSession session) {
        boolean someUpdates = false;
        for (NutsId extension : extensions) {
            NutsId u = loadedExtensionIds.stream().filter(
                    x -> x.getShortName().equals(extension.getShortName())
            ).findFirst().orElse(null);
            if (u != null) {
                if (session.isPlainTrace()) {
                    session.out().printf("extensions %s unloaded\n", u);
                }
                loadedExtensionIds.remove(u);
                unloadedExtensions.add(u);
                someUpdates = true;
            }
        }
        if (someUpdates) {
            updateLoadedExtensionURLs(session);
        }
        return this;
    }

    //    @Override
    public NutsWorkspaceExtension[] getWorkspaceExtensions() {
        return extensions.values().toArray(new NutsWorkspaceExtension[0]);
    }

    public NutsWorkspaceExtension wireExtension(NutsId id, NutsFetchCommand options) {
        NutsSession session = NutsWorkspaceUtils.of(ws).validateSession(options.getSession());
        NutsSession searchSession = session.setTrace(false);
        if (id == null) {
            throw new NutsIllegalArgumentException(ws, "extension Id could not be null");
        }
        NutsId wired = CoreNutsUtils.findNutsIdBySimpleName(id, extensions.keySet());
        if (wired != null) {
            throw new NutsExtensionAlreadyRegisteredException(ws, id.toString(), wired.toString());
        }

        LOG.with().session(session).level(Level.FINE).verb(NutsLogVerb.UPDATE).formatted().log("installing extension {0}", id);
        NutsDefinition nutsDefinitions = ws.search()
                .copyFrom(options)
                .setSession(searchSession)
                .addId(id).setSession(session)
                .addScope(NutsDependencyScopePattern.RUN)
                .setOptional(false)
                .setDependencies(true)
                .setLatest(true)
                .getResultDefinitions().required();
        if (!isLoadedClassPath(nutsDefinitions, session)) {
            this.workspaceExtensionsClassLoader.add(NutsClassLoaderNodeUtils.definitionToClassLoaderNode(nutsDefinitions, session));
        }
        DefaultNutsWorkspaceExtension workspaceExtension = new DefaultNutsWorkspaceExtension(id, nutsDefinitions.getId(), this.workspaceExtensionsClassLoader);
        //now will iterate over Extension classes to wire them ...
        objectFactory.discoverTypes(nutsDefinitions.getId(), nutsDefinitions.getContent().getURL(), workspaceExtension.getClassLoader(), session);
//        for (Class extensionImpl : getExtensionTypes(NutsComponent.class, session)) {
//            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
//                if (registerType(extensionPointType, extensionImpl, session)) {
//                    workspaceExtension.getWiredComponents().add(extensionPointType.getName(), ((Class<? extends NutsComponent>) extensionImpl).getName());
//                }
//            }
//        }
        extensions.put(id, workspaceExtension);
        LOG.with().session(session).level(Level.FINE).verb(NutsLogVerb.UPDATE).formatted().log("extension {0} installed successfully", id);
        NutsTerminalSpec spec = new NutsDefaultTerminalSpec();
        if (session.getTerminal() != null) {
            spec.put("ignoreClass", session.getTerminal().getClass());
        }
        NutsSessionTerminal newTerminal = createTerminal(spec);
        if (newTerminal != null) {
            LOG.with().session(session).level(Level.FINE).verb(NutsLogVerb.UPDATE).formatted().log("extension {0} changed Terminal configuration. Reloading Session Terminal", id);
            session.setTerminal(newTerminal);
        }
        return workspaceExtension;
    }

    private boolean isLoadedClassPath(NutsDefinition file, NutsSession session) {
        //session = CoreNutsUtils.validateSession(session,ws);
        if (file.getId().equalsShortName(ws.id().parser().setLenient(false).parse(NutsConstants.Ids.NUTS_API))) {
            return true;
        }
        try {
            //            NutsDefinition file = fetch(parse.toString(), session);
            if (file.getPath() != null) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file.getPath().toFile());
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
                            LOG.with().session(session).level(Level.SEVERE)
                                    .error(ex).log("failed to close zip file {0} : {1}",
                                    file.getPath(), ex);
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
    public NutsSessionTerminal createTerminal(NutsTerminalSpec spec) {
        NutsSessionTerminalBase termb = createSupported(NutsSessionTerminalBase.class, spec, spec.getSession());
        if (termb == null) {
            throw new NutsExtensionNotFoundException(ws, NutsSessionTerminalBase.class, "TerminalBase");
        }
        if (spec != null && spec.get("ignoreClass") != null && spec.get("ignoreClass").equals(termb.getClass())) {
            return null;
        }
        NutsSessionTerminal term = new DefaultNutsSessionTerminal();
        NutsWorkspaceUtils.setSession(term, spec.getSession());
        term.setParent(termb);
        return term;

    }

    //@Override
    public URL[] getExtensionURLLocations(NutsId nutsId, String appId, String extensionType) {
        List<URL> bootUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(nutsId)) {
            String url = r + "/" + CoreIOUtils.getPath(nutsId, "." + extensionType, '/');
            URL u = expandURL(url);
            bootUrls.add(u);
        }
        return bootUrls.toArray(new URL[0]);
    }

    //@Override
    public String[] getExtensionRepositoryLocations(NutsId appId) {
        //should parse this form config?
        //or should be parse from and extension component?
        String repos = ws.env().getEnv("bootstrapRepositoryLocations", "") + ";" //                + NutsConstants.BootstrapURLs.LOCAL_NUTS_FOLDER
                //                + ";" + NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT
                ;
        List<String> urls = new ArrayList<>();
        for (String r : StringTokenizerUtils.split(repos, "; ")) {
            if (!CoreStringUtils.isBlank(r)) {
                urls.add(r);
            }
        }
        return urls.toArray(new String[0]);
    }

    protected URL expandURL(String url) {
        try {
            url = ws.io().expandPath(url);
            if (CoreIOUtils.isPathHttp(url)) {
                return new URL(url);
            }
            if (CoreIOUtils.isPathFile(url)) {
                return CoreIOUtils.toPathFile(url, ws).toUri().toURL();
            }
            return new File(url).toURI().toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    private NutsWorkspaceConfigManagerExt configExt() {
        return NutsWorkspaceConfigManagerExt.of(ws.config());
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
    private NutsWorkspaceConfigBoot getStoredConfig() {
        return configExt().getStoredConfigBoot();
    }

    public synchronized DefaultNutsClassLoader getNutsURLClassLoader(String name, ClassLoader parent) {
        if (parent == null) {
            parent = workspaceExtensionsClassLoader;
        }
        return new DefaultNutsClassLoader(name, ws, parent);
//        NutsURLClassLoaderKey k = new NutsURLClassLoaderKey(urls, parent);
//        DefaultNutsClassLoader v = cachedClassLoaders.get(k);
//        if (v == null) {
//            v = new DefaultNutsClassLoader(name, ws, urls, ids, parent);
//            cachedClassLoaders.put(k, v);
//        }
//        return v;
    }

    public static class RegInfo {

        Class extensionPointType;
        Class extensionTypeImpl;
        NutsId extensionId;

        public RegInfo(Class extensionPointType, Class extensionTypeImpl, NutsId extensionId) {
            this.extensionPointType = extensionPointType;
            this.extensionTypeImpl = extensionTypeImpl;
            this.extensionId = extensionId;
        }

        public NutsId getExtensionId() {
            return extensionId;
        }

        public Class getExtensionPointType() {
            return extensionPointType;
        }

        public Class getExtensionTypeImpl() {
            return extensionTypeImpl;
        }
    }

    private static class NutsURLClassLoaderKey {

        private final URL[] urls;
        private final ClassLoader parent;

        public NutsURLClassLoaderKey(URL[] urls, ClassLoader parent) {
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
            final NutsURLClassLoaderKey other = (NutsURLClassLoaderKey) obj;
            if (!Arrays.deepEquals(this.urls, other.urls)) {
                return false;
            }
            if (!Objects.equals(this.parent, other.parent)) {
                return false;
            }
            return true;
        }

    }
}
