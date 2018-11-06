/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.core;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.terminals.NutsDefaultFormattedPrintStream;
import net.vpc.app.nuts.extensions.util.CoreJsonUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.io.URLUtils;
import net.vpc.common.util.ListMap;

/**
 * @author vpc
 */
class DefaultNutsWorkspaceExtensionManager implements NutsWorkspaceExtensionManager {

    private Set<Class> SUPPORTED_EXTENSION_TYPES = new HashSet<Class>(
            Arrays.asList(
                    //order is important!!because autowiring should follow this very order
                    NutsDefaultFormattedPrintStream.class,
                    NutsNonFormattedPrintStream.class,
                    NutsPrintStream.class,
                    NutsTerminal.class,
                    NutsDescriptorContentParserComponent.class,
                    NutsExecutorComponent.class,
                    NutsInstallerComponent.class,
                    NutsRepositoryFactoryComponent.class,
//                    NutsCommandAutoCompleteComponent.class
//                    NutsCommand.class,
//                    NutsConsole.class,
//                    NutsServerComponent.class,
                    NutsTransportComponent.class,
                    NutsWorkspace.class,
                    NutsWorkspaceArchetypeComponent.class
            )
    );
    private NutsURLClassLoader workspaceExtensionsClassLoader;
    private ListMap<String, String> defaultWiredComponents = new ListMap<>();
    private Map<NutsId, NutsWorkspaceExtension> extensions = new HashMap<NutsId, NutsWorkspaceExtension>();
    private final NutsWorkspace ws;
    private final NutsWorkspaceObjectFactory objectFactory;

    protected DefaultNutsWorkspaceExtensionManager(NutsWorkspace ws, NutsWorkspaceObjectFactory objectFactory) {
        this.ws = ws;
        this.objectFactory = objectFactory;
    }

    @Override
    public List<NutsExtensionInfo> findWorkspaceExtensions(NutsSession session) {
        return findWorkspaceExtensions(ws.getBootId().getVersion().toString(), session);
    }

    @Override
    public List<NutsExtensionInfo> findWorkspaceExtensions(String version, NutsSession session) {
        if (version == null) {
            version = ws.getBootId().getVersion().toString();
        }
        NutsId id = ws.getBootId().setVersion(version);
        return findExtensions(id.toString(), "extensions", session);
    }

    @Override
    public List<NutsExtensionInfo> findExtensions(String id, String extensionType, NutsSession session) {
        NutsId nid = parseNutsId(id);
        if (nid.getVersion().isEmpty()) {
            throw new NutsIllegalArgumentException("Missing version");
        }
        List<NutsExtensionInfo> ret = new ArrayList<>();
        List<String> allUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(id)) {
            String url = r + "/" + CoreNutsUtils.getPath(nid, "." + extensionType, "/");
            allUrls.add(url);
            URL u = expandURL(url);
            if (u != null) {
                NutsExtensionInfo[] s = new NutsExtensionInfo[0];
                try {
                    s = CoreJsonUtils.get().read(new InputStreamReader(u.openStream()), NutsExtensionInfo[].class);
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

    protected void oninitializeWorkspace(ClassLoader bootClassLoader) {
        //now will iterate over Extension classes to wire them ...
        List<Class> loadedExtensions = discoverTypes(NutsComponent.class, bootClassLoader);
        for (Class extensionImpl : loadedExtensions) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                Class<? extends NutsComponent> extensionImplType = extensionImpl;
                if (installExtensionComponentType(extensionPointType, extensionImplType)) {
                    defaultWiredComponents.add(extensionPointType.getName(), extensionImplType.getName());
                }
            }
        }
        //versionProperties = IOUtils.loadProperties(DefaultNutsWorkspace.class.getResource("/META-INF/nuts-core-version.properties"));
        this.workspaceExtensionsClassLoader = new NutsURLClassLoader(new URL[0], bootClassLoader);
    }

    @Override
    public NutsWorkspaceExtension addWorkspaceExtension(String id, NutsSession session) {
        session = validateSession(session);
        NutsId oldId = CoreNutsUtils.finNutsIdByFullName(CoreNutsUtils.parseOrErrorNutsId(id), extensions.keySet());
        NutsWorkspaceExtension old = null;
        if (oldId == null) {
            NutsId nutsId = ws.resolveId(id, session);
            NutsId eid = CoreNutsUtils.parseOrErrorNutsId(id);
            if (CoreStringUtils.isEmpty(eid.getGroup())) {
                eid = eid.setGroup(nutsId.getGroup());
            }
            ws.getConfigManager().addExtension(eid);
            return wireExtension(eid, session);
        } else {
            old = extensions.get(oldId);
            ws.getConfigManager().addExtension(CoreNutsUtils.parseOrErrorNutsId(id));
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
        return extensions.values().toArray(new NutsWorkspaceExtension[extensions.size()]);
    }

    protected NutsWorkspaceExtension wireExtension(NutsId id, NutsSession session) {
        session = validateSession(session);
        if (id == null) {
            throw new NutsIllegalArgumentException("Extension Id could not be null");
        }
        NutsId wired = CoreNutsUtils.finNutsIdByFullName(id, extensions.keySet());
        if (wired != null) {
            throw new NutsWorkspaceExtensionAlreadyRegisteredException(id.toString(), wired.toString());
        }
        DefaultNutsWorkspace.log.log(Level.FINE, "Installing extension {0}", id);
        NutsFile[] nutsFiles = ws.fetchDependencies(new NutsDependencySearch(id).setScope(NutsDependencyScope.RUN), session);
        NutsId toWire = null;
        for (NutsFile nutsFile : nutsFiles) {
            if (nutsFile.getId().isSameFullName(id)) {
                if (toWire == null || toWire.getVersion().compareTo(nutsFile.getId().getVersion()) < 0) {
                    toWire = nutsFile.getId();
                }
            }
        }
        if (toWire == null) {
            toWire = id;
        }
        for (NutsFile nutsFile : nutsFiles) {
            if (!isLoadedClassPath(nutsFile, session)) {
                this.workspaceExtensionsClassLoader.addFile(new File(nutsFile.getFile()));
            }
        }
        DefaultNutsWorkspaceExtension workspaceExtension = new DefaultNutsWorkspaceExtension(id, toWire, this.workspaceExtensionsClassLoader);
        //now will iterate over Extension classes to wire them ...
        List<Class> serviceLoader = discoverTypes(NutsComponent.class, workspaceExtension.getClassLoader());
        for (Class extensionImpl : serviceLoader) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                Class<? extends NutsComponent> extensionImplType = extensionImpl;
                if (installExtensionComponentType(extensionPointType, extensionImplType)) {
                    workspaceExtension.getWiredComponents().add(extensionPointType.getName(), extensionImplType.getName());
                }
            }
        }
        extensions.put(id, workspaceExtension);
        DefaultNutsWorkspace.log.log(Level.FINE, "Extension {0} installed successfully", id);
        NutsTerminal newTerminal = createTerminal(session.getTerminal() == null ? null : session.getTerminal().getClass());
        if (newTerminal != null) {
            DefaultNutsWorkspace.log.log(Level.FINE, "Extension {0} changed Terminal configuration. Reloading Session Terminal", id);
            session.setTerminal(newTerminal);
        }
        return workspaceExtension;
    }

    private boolean isLoadedClassPath(NutsFile file, NutsSession session) {
        session = validateSession(session);
        if (file.getId().isSameFullName(CoreNutsUtils.parseOrErrorNutsId(NutsConstants.NUTS_ID_BOOT))) {
            return true;
        }
        try {
            //            NutsFile file = fetch(id.toString(), session);
            if (file.getFile() != null) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file.getFile());
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
        DefaultNutsWorkspace.log.log(Level.FINE, "Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionImpl.getClass().getName()});
        return false;
    }

    public boolean registerType(Class extensionPointType, Class extensionType) {
        if (!isRegisteredType(extensionPointType, extensionType.getName()) && !isRegisteredType(extensionPointType, extensionType)) {
            objectFactory.registerType(extensionPointType, extensionType);
            return true;
        }
        DefaultNutsWorkspace.log.log(Level.FINE, "Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionType.getName()});
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
            throw new ClassCastException(extensionImplType.getClass().getName());
        }
        throw new ClassCastException(NutsComponent.class.getName());
    }

    public NutsTerminal createTerminal(Class ignoredClass) {
        NutsTerminal term = createSupported(NutsTerminal.class, ws);
        if (term == null) {
            throw new NutsUnsupportedOperationException("Should never happen ! Terminal could not be resolved.");
        } else {
            if (ignoredClass != null && ignoredClass.equals(term.getClass())) {
                return null;
            }
            term.install(ws, null, null, null);
        }
        return term;
    }

    @Override
    public URLLocation[] getExtensionURLLocations(String nutsId, String appId, String extensionType) {
        List<URLLocation> bootUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(nutsId)) {
            String url = r + "/" + CoreNutsUtils.getPath(CoreNutsUtils.parseNutsId(nutsId), "." + extensionType, "/");
            URL u = expandURL(url);
            bootUrls.add(new URLLocation(url, u));
        }
        return bootUrls.toArray(new URLLocation[bootUrls.size()]);
    }

    @Override
    public String[] getExtensionRepositoryLocations(String appId) {
        //should read this form config?
        //or should be read from and extension component?
        String repos = ws.getConfigManager().getEnv("bootstrapRepositoryLocations", "") + ";"
                + NutsConstants.URL_BOOTSTRAP_LOCAL
                + ";" + NutsConstants.URL_BOOTSTRAP_REMOTE;
        List<String> urls = new ArrayList<>();
        for (String r : CoreStringUtils.split(repos, "; ")) {
            if (!CoreStringUtils.isEmpty(r)) {
                urls.add(r);
            }
        }
        return urls.toArray(new String[urls.size()]);
    }

    protected URL expandURL(String url) {
        try {
            url = CoreNutsUtils.expandPath(url, ws);
            if (URLUtils.isRemoteURL(url)) {
                return new URL(url);
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
    public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria) {
        return objectFactory.createSupported(type, supportCriteria);
    }

    @Override
    public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters) {
        return objectFactory.createSupported(type, supportCriteria, constructorParameterTypes, constructorParameterTypes);
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

//    @Override
//    public NutsConsole createConsole(NutsSession session) {
//        session = validateSession(session);
//        NutsConsole cmd = objectFactory.createSupported(NutsConsole.class, ws);
//        if (cmd == null) {
//            throw new NutsExtensionMissingException(NutsConsole.class, "sh");
//        }
//        cmd.init(ws, session);
//        return cmd;
//    }

    @Override
    public NutsSession createSession() {
        NutsSession nutsSession = new NutsSessionImpl();
        nutsSession.setTerminal(createTerminal());
        return nutsSession;
    }

    @Override
    public NutsTerminal createTerminal() {
        return createTerminal(null, null, null);
    }

    @Override
    public NutsTerminal createTerminal(InputStream in, NutsPrintStream out, NutsPrintStream err) {
        NutsTerminal term = objectFactory.createSupported(NutsTerminal.class, null);
        if (term == null) {
            throw new NutsExtensionMissingException(NutsTerminal.class, "Terminal");
        }
        term.install(ws, in, out, err);
        return term;
    }

    protected NutsSession validateSession(NutsSession session) {
        if (session == null) {
            session = createSession();
        }
        return session;
    }

    @Override
    public ClassLoader createClassLoader(String[] nutsIds, ClassLoader parentClassLoader, NutsSession session) {
        return createClassLoader(nutsIds,null,parentClassLoader,session);
    }

    @Override
    public ClassLoader createClassLoader(String[] nutsIds, NutsDependencyScope scope, ClassLoader parentClassLoader, NutsSession session) {
        if(scope==null){
            scope=NutsDependencyScope.RUN;
        }
        session = validateSession(session);
        NutsFile[] nutsFiles = ws.fetchDependencies(
                new NutsDependencySearch(nutsIds)
                        .setIncludeMain(true)
                        .setScope(scope),
                session);
        URL[] all = new URL[nutsFiles.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = URLUtils.toURL(new File(nutsFiles[i].getFile()));
        }
        return new NutsURLClassLoader(all, parentClassLoader);
    }

    @Override
    public NutsPrintStream createPrintStream(OutputStream out, boolean formatted) {
        if (out == null) {
            return null;
        }
        if (out instanceof NutsPrintStream) {
            return (NutsPrintStream) out;
        }
        if(formatted){
            if("true".equals(ws.getProperties().getProperty("nocolors"))){
                return objectFactory.createSupported(NutsNonFormattedPrintStream.class, out, new Class[]{OutputStream.class}, new Object[]{out});
            }
            NutsFormattedPrintStream p = objectFactory.createSupported(NutsFormattedPrintStream.class, out, new Class[]{OutputStream.class}, new Object[]{out});
            if(p!=null) {
                return p;
            }
        }else{
            return objectFactory.createSupported(NutsNonFormattedPrintStream.class, out, new Class[]{OutputStream.class}, new Object[]{out});
        }
        return objectFactory.createSupported(NutsPrintStream.class, out, new Class[]{OutputStream.class}, new Object[]{out});
    }

    @Override
    public NutsPrintStream createPrintStream(File out) {
        if (out == null) {
            return null;
        }
        try {
            return new NutsNonFormattedPrintStream(out);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public InputStream createNullInputStream() {
        return NullInputStream.INSTANCE;
    }

    @Override
    public NutsPrintStream createNullPrintStream() {
        return createPrintStream(NullOutputStream.INSTANCE,false);
    }

    @Override
    public NutsId parseNutsId(String nutsId) {
        return CoreNutsUtils.parseNutsId(nutsId);
    }

    @Override
    public NutsDescriptorBuilder createDescriptorBuilder() {
        return new DefaultNutsDescriptorBuilder();
    }

    @Override
    public NutsIdBuilder createIdBuilder() {
        return new DefaultNutsIdBuilder();
    }

    @Override
    public NutsDependencyBuilder createDependencyBuilder() {
        return new DefaultNutsDependencyBuilder();
    }

    @Override
    public JsonSerializer createJsonSerializer() {
        return new GsonSerializer();
    }
}
