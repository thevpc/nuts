/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.spi.NutsWorkspaceFactory;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.terminals.NutsDefaultFormattedPrintStream;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.vpc.app.nuts.core.filters.CoreFilterUtils;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;

import net.vpc.app.nuts.core.terminals.DefaultNutsSessionTerminal;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.ListMap;

/**
 * @author vpc
 */
public class DefaultNutsWorkspaceExtensionManager implements NutsWorkspaceExtensionManager {

    public static final Logger LOG = Logger.getLogger(DefaultNutsWorkspaceExtensionManager.class.getName());
    private final Set<Class> SUPPORTED_EXTENSION_TYPES = new HashSet<>(
            Arrays.asList(
                    //order is important!!because autowiring should follow this very order
                    NutsDefaultFormattedPrintStream.class,
                    NutsNonFormattedPrintStream.class,
                    NutsFormattedPrintStream.class,
                    NutsFormatFilteredPrintStream.class,
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
    private NutsURLClassLoader workspaceExtensionsClassLoader;
    private Map<NutsURLClassLoaderKey, NutsURLClassLoader> cachedClassLoaders = new HashMap<>();
    private final ListMap<String, String> defaultWiredComponents = new ListMap<>();
    private Map<NutsId, NutsWorkspaceExtension> extensions = new HashMap<>();
    private final Set<String> exclusions = new HashSet<String>();
    private final NutsWorkspace ws;
    private final NutsWorkspaceFactory objectFactory;

    protected DefaultNutsWorkspaceExtensionManager(NutsWorkspace ws, NutsWorkspaceFactory objectFactory) {
        this.ws = ws;
        this.objectFactory = objectFactory;
    }

    public boolean isExcludedExtension(NutsId excluded) {
        return this.exclusions.contains(excluded.getSimpleName());
    }

    public void setExcludedExtensions(String[] excluded) {
        this.exclusions.clear();
        if (excluded != null) {
            for (String e : CoreStringUtils.split(Arrays.asList(excluded), ",; ")) {
                if (e != null && !e.trim().isEmpty()) {
                    NutsId ee = ws.id().parse(e);
                    if (ee != null) {
                        this.exclusions.add(ee.getSimpleName());
                    }
                }
            }
        }
    }

//    @Override
    public List<NutsExtensionInfo> findWorkspaceExtensions(NutsSession session) {
        return findWorkspaceExtensions(ws.config().getContext(NutsBootContextType.RUNTIME).getApiId().getVersion().toString(), session);
    }

  //  @Override
    public List<NutsExtensionInfo> findWorkspaceExtensions(String version, NutsSession session) {
        if (version == null) {
            version = ws.config().getContext(NutsBootContextType.RUNTIME).getApiId().getVersion().toString();
        }
        NutsId id = ws.config().getContext(NutsBootContextType.RUNTIME).getApiId().setVersion(version);
        return findExtensions(id, "extensions", session);
    }

    //@Override
    public List<NutsExtensionInfo> findExtensions(String id, String extensionType, NutsSession session) {
        return findExtensions(ws.id().parseRequired(id), extensionType, session);
    }

   // @Override
    public List<NutsExtensionInfo> findExtensions(NutsId id, String extensionType, NutsSession session) {
        if (id.getVersion().isBlank()) {
            throw new NutsIllegalArgumentException(ws, "Missing version");
        }
        List<NutsExtensionInfo> ret = new ArrayList<>();
        List<String> allUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(id)) {
            String url = r + "/" + CoreIOUtils.getPath(id, "." + extensionType, '/');
            allUrls.add(url);
            URL u = expandURL(url);
            if (u != null) {
                NutsExtensionInfo[] s = new NutsExtensionInfo[0];
                try (Reader rr = new InputStreamReader(u.openStream())) {
                    s = ws.json().parse(rr, DefaultNutsExtensionInfo[].class);
                } catch (IOException e) {
                    //ignore!
                }
                if (s != null) {
                    for (NutsExtensionInfo nutsExtensionInfo : s) {
                        ((DefaultNutsExtensionInfo) nutsExtensionInfo).setSource(u.toString());
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

    protected void onInitializeWorkspace(ClassLoader bootClassLoader) {
        //now will iterate over Extension classes to wire them ...
        discoverTypes(bootClassLoader);
        List<Class> loadedExtensions = getImplementationTypes(NutsComponent.class);
        for (Class extensionImpl : loadedExtensions) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                if (registerType(extensionPointType, extensionImpl)) {
                    defaultWiredComponents.add(extensionPointType.getName(), ((Class<? extends NutsComponent>) extensionImpl).getName());
                }
            }
        }
        this.workspaceExtensionsClassLoader = new NutsURLClassLoader(ws, new URL[0], bootClassLoader);
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
//            NutsWorkspaceExtension e = wireExtension(id, ws.fetch().setFetchStratery(NutsFetchStrategy.ONLINE).session(session));
//            addExtension(id);
//            return e;
//        } else {
//            old = extensions.get(oldId);
//            addExtension(id);
//            return old;
//        }
//    }

    @Override
    public boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl) {
        if (NutsComponent.class.isAssignableFrom(extensionPointType)) {
            if (extensionPointType.isInstance(extensionImpl)) {
                return registerInstance(extensionPointType, extensionImpl);
            }
            throw new ClassCastException(extensionImpl.getClass().getName());
        }
        throw new ClassCastException(NutsComponent.class.getName());
    }

//    @Override
    public NutsWorkspaceExtension[] getWorkspaceExtensions() {
        return extensions.values().toArray(new NutsWorkspaceExtension[0]);
    }

    protected NutsWorkspaceExtension wireExtension(NutsId id, NutsFetchCommand options) {
        NutsSession session = NutsWorkspaceUtils.validateSession(ws, options.getSession());
        NutsSession searchSession = session.trace(false);
        if (id == null) {
            throw new NutsIllegalArgumentException(ws, "Extension Id could not be null");
        }
        NutsId wired = CoreNutsUtils.findNutsIdBySimpleName(id, extensions.keySet());
        if (wired != null) {
            throw new NutsWorkspaceExtensionAlreadyRegisteredException(ws, id.toString(), wired.toString());
        }
        LOG.log(Level.FINE, "Installing extension {0}", id);
        List<NutsDefinition> nutsDefinitions = ws.search()
                .copyFrom(options)
                .session(searchSession)
                .addId(id).setSession(session)
                .addScope(NutsDependencyScopePattern.RUN)
                .optional(false)
                .inlineDependencies().getResultDefinitions().list();
        NutsId toWire = null;
        for (NutsDefinition nutsDefinition : nutsDefinitions) {
            if (nutsDefinition.getId().equalsSimpleName(id)) {
                if (toWire == null || toWire.getVersion().compareTo(nutsDefinition.getId().getVersion()) < 0) {
                    toWire = nutsDefinition.getId();
                }
            }
        }
        if (toWire == null) {
            toWire = id;
        }
        for (NutsDefinition nutsDefinition : nutsDefinitions) {
            if (!isLoadedClassPath(nutsDefinition, session)) {
                this.workspaceExtensionsClassLoader.addPath(nutsDefinition.getPath());
            }
        }
        DefaultNutsWorkspaceExtension workspaceExtension = new DefaultNutsWorkspaceExtension(id, toWire, this.workspaceExtensionsClassLoader);
        //now will iterate over Extension classes to wire them ...
        discoverTypes(workspaceExtension.getClassLoader());
        List<Class> serviceLoader = getImplementationTypes(NutsComponent.class);
        for (Class extensionImpl : serviceLoader) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                if (registerType(extensionPointType, extensionImpl)) {
                    workspaceExtension.getWiredComponents().add(extensionPointType.getName(), ((Class<? extends NutsComponent>) extensionImpl).getName());
                }
            }
        }
        extensions.put(id, workspaceExtension);
        LOG.log(Level.FINE, "Extension {0} installed successfully", id);
        NutsSessionTerminal newTerminal = createTerminal(session.getTerminal() == null ? null : session.getTerminal().getClass());
        if (newTerminal != null) {
            LOG.log(Level.FINE, "Extension {0} changed Terminal configuration. Reloading Session Terminal", id);
            session.setTerminal(newTerminal);
        }
        return workspaceExtension;
    }

    private boolean isLoadedClassPath(NutsDefinition file, NutsSession session) {
        //session = CoreNutsUtils.validateSession(session,ws);
        if (file.getId().equalsSimpleName(ws.id().parseRequired(NutsConstants.Ids.NUTS_API))) {
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
                        } catch (IOException e) {
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

    @Override
    public boolean registerInstance(Class extensionPointType, Object extensionImpl) {
        if (!isRegisteredType(extensionPointType, extensionImpl.getClass().getName()) && !isRegisteredInstance(extensionPointType, extensionImpl)) {
            objectFactory.registerInstance(extensionPointType, extensionImpl);
            return true;
        }
        LOG.log(Level.FINE, "Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionImpl.getClass().getName()});
        return false;
    }

    @Override
    public boolean registerType(Class extensionPointType, Class extensionType) {
        if (!isRegisteredType(extensionPointType, extensionType.getName()) && !isRegisteredType(extensionPointType, extensionType)) {
            objectFactory.registerType(extensionPointType, extensionType);
            return true;
        }
        LOG.log(Level.FINE, "Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionType.getName()});
        return false;
    }

    public List<Class> resolveComponentTypes(Class o) {
        List<Class> a = new ArrayList<>();
        if (o != null) {
            HashSet<Class> v=new HashSet<>();
            Stack<Class> s=new Stack<>();
            s.push(o);
            while(!s.isEmpty()){
                Class c=s.pop();
                v.add(c);
                if(SUPPORTED_EXTENSION_TYPES.contains(c)){
                    a.add(c);
                }
                for (Class aa : c.getInterfaces()) {
                    if(!v.contains(aa)){
                        s.push(aa);
                    }
                }
                Class sc = c.getSuperclass();
                if(sc!=null && !v.contains(sc)){
                    s.push(sc);
                }
            }
        }
        return a;
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

    public boolean installExtensionComponentType(Class extensionPointType, Class extensionImplType) {
        if (NutsComponent.class.isAssignableFrom(extensionPointType)) {
            if (extensionPointType.isAssignableFrom(extensionImplType)) {
                return registerType(extensionPointType, extensionImplType);
            }
            throw new ClassCastException(extensionImplType.getName());
        }
        throw new ClassCastException(NutsComponent.class.getName());
    }

    public NutsSessionTerminal createTerminal(Class ignoredClass) {
        NutsSessionTerminalBase termb = createSupported(NutsSessionTerminalBase.class, new DefaultNutsSupportLevelContext<>(ws,ignoredClass));
        if (termb == null) {
            throw new NutsExtensionMissingException(ws, NutsSessionTerminalBase.class, "TerminalBase");
        } else {
            if (ignoredClass != null && ignoredClass.equals(termb.getClass())) {
                return null;
            }
            NutsSessionTerminal term = new DefaultNutsSessionTerminal();
            //termb, true
            term.install(ws);
            term.setParent(termb);
            return term;
        }
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
        String repos = ws.config().getEnv("bootstrapRepositoryLocations", "") + ";"
                + NutsConstants.BootstrapURLs.LOCAL_NUTS_FOLDER
                + ";" + NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT;
        List<String> urls = new ArrayList<>();
        for (String r : CoreStringUtils.split(repos, "; ")) {
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
                return CoreIOUtils.toPathFile(url).toUri().toURL();
            }
            return new File(url).toURI().toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    @Override
    public List<Class> getImplementationTypes(Class type) {
        return objectFactory.getImplementationTypes(type);
    }

    @Override
    public List<Class> discoverTypes(ClassLoader classLoader) {
        return objectFactory.discoverTypes(classLoader);
    }

    @Override
    public <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType) {
        return createServiceLoader(serviceType, criteriaType, null);
    }

    @Override
    public <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        return new DefaultNutsServiceLoader<T, B>(serviceType, criteriaType, classLoader);
    }

    public <T extends NutsComponent<V>,V> T createSupported(Class<T> type, NutsSupportLevelContext<V> supportCriteria){
        return objectFactory.createSupported(type, supportCriteria);
    }

    @Override
    public <T extends NutsComponent<V>,V> T createSupported(Class<T> type, NutsSupportLevelContext<V> supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters) {
        return objectFactory.createSupported(type, supportCriteria, constructorParameterTypes, constructorParameters);
    }

    @Override
    public <T extends NutsComponent<V>,V> List<T> createAllSupported(Class<T> type, NutsSupportLevelContext<V> supportCriteria) {
        return objectFactory.createAllSupported(type, supportCriteria);
    }

    @Override
    public <T> List<T> createAll(Class<T> type) {
        return objectFactory.createAll(type);
    }

    @Override
    public Set<Class> getExtensionPoints() {
        return objectFactory.getExtensionPoints();
    }

    @Override
    public Set<Class> getExtensionTypes(Class extensionPoint) {
        return objectFactory.getExtensionTypes(extensionPoint);
    }

    @Override
    public List<Object> getExtensionObjects(Class extensionPoint) {
        return objectFactory.getExtensionObjects(extensionPoint);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, String name) {
        return objectFactory.isRegisteredType(extensionPointType, name);
    }

    @Override
    public boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl) {
        return objectFactory.isRegisteredInstance(extensionPointType, extensionImpl);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, Class extensionType) {
        return objectFactory.isRegisteredType(extensionPointType, extensionType);
    }

    @Override
    public NutsId[] getExtensions() {
        if (getStoredConfig().getExtensions() != null) {
            return getStoredConfig().getExtensions().toArray(new NutsId[0]);
        }
        return new NutsId[0];
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

    private NutsWorkspaceConfigManagerExt configExt() {
        return NutsWorkspaceConfigManagerExt.of(ws.config());
    }

    private NutsWorkspaceConfig getStoredConfig() {
        return configExt().getStoredConfig();
    }

    public synchronized NutsURLClassLoader getNutsURLClassLoader(URL[] urls, ClassLoader parent) {
        NutsURLClassLoaderKey k = new NutsURLClassLoaderKey(urls, parent);
        NutsURLClassLoader v = cachedClassLoaders.get(k);
        if (v == null) {
            v = new NutsURLClassLoader(ws, urls, parent);
            cachedClassLoaders.put(k, v);
        }
        return v;
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
