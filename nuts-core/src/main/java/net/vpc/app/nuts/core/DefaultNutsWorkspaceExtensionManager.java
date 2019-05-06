/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

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
    private final ListMap<String, String> defaultWiredComponents = new ListMap<>();
    private Map<NutsId, NutsWorkspaceExtension> extensions = new HashMap<>();
    private final NutsWorkspace ws;
    private final NutsWorkspaceFactory objectFactory;

    protected DefaultNutsWorkspaceExtensionManager(NutsWorkspace ws, NutsWorkspaceFactory objectFactory) {
        this.ws = ws;
        this.objectFactory = objectFactory;
    }

    @Override
    public List<NutsExtensionInfo> findWorkspaceExtensions(NutsSession session) {
        return findWorkspaceExtensions(ws.config().getContext(NutsBootContextType.RUNTIME).getApiId().getVersion().toString(), session);
    }

    @Override
    public List<NutsExtensionInfo> findWorkspaceExtensions(String version, NutsSession session) {
        if (version == null) {
            version = ws.config().getContext(NutsBootContextType.RUNTIME).getApiId().getVersion().toString();
        }
        NutsId id = ws.config().getContext(NutsBootContextType.RUNTIME).getApiId().setVersion(version);
        return findExtensions(id, "extensions", session);
    }

    @Override
    public List<NutsExtensionInfo> findExtensions(String id, String extensionType, NutsSession session) {
        return findExtensions(ws.parser().parseRequiredId(id), extensionType, session);
    }

    @Override
    public List<NutsExtensionInfo> findExtensions(NutsId id, String extensionType, NutsSession session) {
        if (id.getVersion().isBlank()) {
            throw new NutsIllegalArgumentException("Missing version");
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
                    s = ws.io().json().read(rr, NutsExtensionInfo[].class);
                } catch (IOException e) {
                    //ignore!
                }
                if (s != null) {
                    for (NutsExtensionInfo nutsExtensionInfo : s) {
                        nutsExtensionInfo.setSource(u.toString());
                        ret.add(nutsExtensionInfo);
                    }
                }
            }
        }
        boolean latestVersion = true;
        if (latestVersion && ret.size() > 1) {
            return CoreNutsUtils.filterNutsExtensionInfoByLatestVersion(ret);
        }
        return ret;
    }

    protected void onInitializeWorkspace(ClassLoader bootClassLoader) {
        //now will iterate over Extension classes to wire them ...
        List<Class> loadedExtensions = discoverTypes(NutsComponent.class, bootClassLoader);
        for (Class extensionImpl : loadedExtensions) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                if (installExtensionComponentType(extensionPointType, extensionImpl)) {
                    defaultWiredComponents.add(extensionPointType.getName(), ((Class<? extends NutsComponent>) extensionImpl).getName());
                }
            }
        }
        //versionProperties = IOUtils.loadProperties(DefaultNutsWorkspace.class.getResource("/META-INF/nuts-core-version.properties"));
        this.workspaceExtensionsClassLoader = new NutsURLClassLoader(ws, new URL[0], bootClassLoader);
    }

    @Override
    public NutsWorkspaceExtension addWorkspaceExtension(NutsId id, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(ws, session);
        NutsId oldId = CoreNutsUtils.finNutsIdBySimpleName(id, extensions.keySet());
        NutsWorkspaceExtension old = null;
        if (oldId == null) {
            NutsWorkspaceExtension e = wireExtension(id, ws.fetch().setFetchStratery(NutsFetchStrategy.ONLINE), session);
            addExtension(id);
            return e;
        } else {
            old = extensions.get(oldId);
            addExtension(id);
            return old;
        }
    }

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

    @Override
    public NutsWorkspaceExtension[] getWorkspaceExtensions() {
        return extensions.values().toArray(new NutsWorkspaceExtension[0]);
    }

    protected NutsWorkspaceExtension wireExtension(NutsId id, NutsFetchCommand options, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(ws, session);
        if (id == null) {
            throw new NutsIllegalArgumentException("Extension Id could not be null");
        }
        NutsId wired = CoreNutsUtils.finNutsIdBySimpleName(id, extensions.keySet());
        if (wired != null) {
            throw new NutsWorkspaceExtensionAlreadyRegisteredException(id.toString(), wired.toString());
        }
        LOG.log(Level.FINE, "Installing extension {0}", id);
        List<NutsDefinition> nutsDefinitions = ws.find()
                .copyFrom(options)
                .addId(id).setSession(session)
                .addScope(NutsDependencyScope.PROFILE_RUN_STANDALONE)
                .setIncludeOptional(false)
                .mainAndDependencies().getResultDefinitions().list();
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
        List<Class> serviceLoader = discoverTypes(NutsComponent.class, workspaceExtension.getClassLoader());
        for (Class extensionImpl : serviceLoader) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                if (installExtensionComponentType(extensionPointType, extensionImpl)) {
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
        if (file.getId().equalsSimpleName(ws.parser().parseRequiredId(NutsConstants.Ids.NUTS_API))) {
            return true;
        }
        try {
            //            NutsDefinition file = fetch(id.toString(), session);
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

    public boolean registerInstance(Class extensionPointType, Object extensionImpl) {
        if (!isRegisteredType(extensionPointType, extensionImpl.getClass().getName()) && !isRegisteredInstance(extensionPointType, extensionImpl)) {
            objectFactory.registerInstance(extensionPointType, extensionImpl);
            return true;
        }
        LOG.log(Level.FINE, "Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionImpl.getClass().getName()});
        return false;
    }

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
            for (Class extensionPointType : SUPPORTED_EXTENSION_TYPES) {
                if (extensionPointType.isAssignableFrom(o)) {
                    a.add(extensionPointType);
                }
            }
        }
        return a;
    }

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
        NutsSessionTerminalBase termb = createSupported(NutsSessionTerminalBase.class, ws);
        if (termb == null) {
            throw new NutsExtensionMissingException(NutsSessionTerminalBase.class, "TerminalBase");
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

    @Override
    public NutsURLLocation[] getExtensionURLLocations(NutsId nutsId, String appId, String extensionType) {
        List<NutsURLLocation> bootUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(nutsId)) {
            String url = r + "/" + CoreIOUtils.getPath(nutsId, "." + extensionType, '/');
            URL u = expandURL(url);
            bootUrls.add(new NutsURLLocation(url, u));
        }
        return bootUrls.toArray(new NutsURLLocation[0]);
    }

    @Override
    public String[] getExtensionRepositoryLocations(NutsId appId) {
        //should read this form config?
        //or should be read from and extension component?
        String repos = ws.config().getEnv("bootstrapRepositoryLocations", "") + ";"
                + NutsConstants.BootsrapURLs.LOCAL_NUTS_FOLDER
                + ";" + NutsConstants.BootsrapURLs.REMOTE_NUTS_GIT;
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
    public List<Class> discoverTypes(Class type, ClassLoader bootClassLoader) {
        return objectFactory.discoverTypes(type, bootClassLoader);
    }

    @Override
    public <T> List<T> discoverInstances(Class<T> type, ClassLoader bootClassLoader) {
        return objectFactory.discoverInstances(type, bootClassLoader);
    }

    @Override
    public <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType) {
        return createServiceLoader(serviceType, criteriaType, null);
    }

    @Override
    public <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        return new DefaultNutsServiceLoader<T, B>(serviceType, criteriaType, classLoader);
    }

    @Override
    public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria) {
        return objectFactory.createSupported(type, supportCriteria);
    }

    @Override
    public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters) {
        return objectFactory.createSupported(type, supportCriteria, constructorParameterTypes, constructorParameters);
    }

    @Override
    public <T extends NutsComponent> List<T> createAllSupported(Class<T> type, Object supportCriteria) {
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

    @Override
    public boolean addExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new NutsIllegalArgumentException("Invalid Extension");
        }
        if (!containsExtension(extensionId)) {
            if (getStoredConfig().getExtensions() == null) {
                getStoredConfig().setExtensions(new ArrayList<>());
            }
            getStoredConfig().getExtensions().add(extensionId);
            fireConfigurationChanged();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new NutsIllegalArgumentException("Invalid Extension");
        }
        for (NutsId extension : getExtensions()) {
            if (extension.equalsSimpleName(extensionId)) {
                if (getStoredConfig().getExtensions() != null) {
                    getStoredConfig().getExtensions().remove(extension);
                }
                fireConfigurationChanged();
                return true;

            }
        }
        return false;
    }

    @Override
    public boolean updateExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new NutsIllegalArgumentException("Invalid Extension");
        }
        NutsId[] extensions = getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            NutsId extension = extensions[i];
            if (extension.equalsSimpleName(extensionId)) {
                extensions[i] = extensionId;
                getStoredConfig().setExtensions(new ArrayList<>(Arrays.asList(extensions)));
                fireConfigurationChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new NutsIllegalArgumentException("Invalid Extension");
        }
        for (NutsId extension : getExtensions()) {
            if (extension.equalsSimpleName(extension)) {
                return true;
            }
        }
        return false;
    }

    private void fireConfigurationChanged() {
        config0().fireConfigurationChanged();
    }

    private DefaultNutsWorkspaceConfigManager config0() {
        return (DefaultNutsWorkspaceConfigManager) ws.config();
    }

    private NutsWorkspaceConfig getStoredConfig() {
        return config0().getStoredConfig();
    }

}
