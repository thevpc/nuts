/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.core;

import java.io.File;
import java.io.IOException;
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
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsCommandAutoCompleteComponent;
import net.vpc.app.nuts.NutsComponent;
import net.vpc.app.nuts.NutsConsole;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDependencyScope;
import net.vpc.app.nuts.NutsDependencySearch;
import net.vpc.app.nuts.NutsDescriptorContentParserComponent;
import net.vpc.app.nuts.NutsExecutorComponent;
import net.vpc.app.nuts.NutsExtensionInfo;
import net.vpc.app.nuts.NutsFile;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsInstallerComponent;
import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.NutsRepositoryFactoryComponent;
import net.vpc.app.nuts.NutsServerComponent;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsTransportComponent;
import net.vpc.app.nuts.NutsUnsupportedOperationException;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceArchetypeComponent;
import net.vpc.app.nuts.NutsWorkspaceExtension;
import net.vpc.app.nuts.NutsWorkspaceExtensionAlreadyRegisteredException;
import net.vpc.app.nuts.NutsWorkspaceExtensionManager;
import net.vpc.app.nuts.NutsWorkspaceFactory;
import net.vpc.app.nuts.URLLocation;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreJsonUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.extensions.util.ListMap;

/**
 *
 * @author vpc
 */
class DefaultNutsWorkspaceExtensionManager implements NutsWorkspaceExtensionManager {

    private Set<Class> SUPPORTED_EXTENSION_TYPES = new HashSet<Class>(
            Arrays.asList(
                    //order is important!!because autowiring should follow this very order
                    NutsPrintStream.class,
                    NutsTerminal.class,
                    NutsCommand.class,
                    NutsConsole.class,
                    NutsDescriptorContentParserComponent.class,
                    NutsExecutorComponent.class,
                    NutsInstallerComponent.class,
                    NutsRepositoryFactoryComponent.class,
                    NutsServerComponent.class,
                    NutsTransportComponent.class,
                    NutsWorkspace.class,
                    NutsWorkspaceArchetypeComponent.class,
                    NutsCommandAutoCompleteComponent.class
            )
    );
    private NutsURLClassLoader workspaceExtensionsClassLoader;
    private ListMap<String, String> defaultWiredComponents = new ListMap<>();
    private Map<NutsId, NutsWorkspaceExtension> extensions = new HashMap<NutsId, NutsWorkspaceExtension>();
    private final DefaultNutsWorkspace ws;
    private final NutsWorkspaceFactory factory;

    protected DefaultNutsWorkspaceExtensionManager(DefaultNutsWorkspace ws, NutsWorkspaceFactory factory) {
        this.ws = ws;
        this.factory = factory;
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
        return findExtensions(id.toString(), session);
    }

    @Override
    public List<NutsExtensionInfo> findExtensions(String id, NutsSession session) {
        NutsId nid = getFactory().parseNutsId(id);
        if (nid.getVersion().isEmpty()) {
            throw new NutsIllegalArgumentException("Missing version");
        }
        List<NutsExtensionInfo> ret = new ArrayList<>();
        List<String> allUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(id)) {
            String url = r + "/" + CoreNutsUtils.getPath(nid, ".extensions", "/");
            allUrls.add(url);
            URL u = expandURL(url);
            if (url != null) {
                JsonStructure s = null;
                try {
                    s = CoreJsonUtils.get().loadJsonStructure(u.openStream());
                } catch (Exception ex) {
                    ///
                }
                if (s != null && s instanceof JsonArray) {
                    JsonArray a = (JsonArray) s;
                    for (JsonValue v : a) {
                        if (v.getValueType() == JsonValue.ValueType.OBJECT) {
                            JsonObject o = (JsonObject) v;
                            try {
                                ret.add(new NutsExtensionInfo(CoreNutsUtils.parseNullableOrErrorNutsId(o.getString("id")),
                                        o.getString("name", null), o.getString("author", null), o.getString("description", null), o.getString("category", null), u.toString()));
                            } catch (Exception ex) {
                                //ignore any error
                            }
                        }
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
        List<Class> loadedExtensions = ws.getExtensionManager().getFactory().discoverTypes(NutsComponent.class, bootClassLoader);
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
        session = ws.validateSession(session);
        NutsId oldId = CoreNutsUtils.finNutsIdByFullName(CoreNutsUtils.parseOrErrorNutsId(id), extensions.keySet());
        NutsWorkspaceExtension old = null;
        if (oldId == null) {
            NutsId nutsId = ws.resolveId(id, session);
            NutsId eid = CoreNutsUtils.parseOrErrorNutsId(id);
            if (CoreStringUtils.isEmpty(eid.getGroup())) {
                eid = eid.setGroup(nutsId.getGroup());
            }
            ws.getConfigManager().getConfig().addExtension(eid);
            return wireExtension(eid, session);
        } else {
            old = extensions.get(oldId);
            ws.getConfigManager().getConfig().addExtension(CoreNutsUtils.parseOrErrorNutsId(id));
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
        session = ws.validateSession(session);
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
                this.workspaceExtensionsClassLoader.addFile(nutsFile.getFile());
            }
        }
        DefaultNutsWorkspaceExtension workspaceExtension = new DefaultNutsWorkspaceExtension(id, toWire, this.workspaceExtensionsClassLoader);
        //now will iterate over Extension classes to wire them ...
        List<Class> serviceLoader = ws.getExtensionManager().getFactory().discoverTypes(NutsComponent.class, workspaceExtension.getClassLoader());
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
        session = ws.validateSession(session);
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

    private boolean registerInstance(Class extensionPointType, Object extensionImpl) {
        if (!ws.getExtensionManager().getFactory().isRegisteredType(extensionPointType, extensionImpl.getClass().getName()) && !ws.getExtensionManager().getFactory().isRegisteredInstance(extensionPointType, extensionImpl)) {
            ws.getExtensionManager().getFactory().registerInstance(extensionPointType, extensionImpl);
            return true;
        }
        DefaultNutsWorkspace.log.log(Level.FINE, "Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionImpl.getClass().getName()});
        return false;
    }

    private boolean registerType(Class extensionPointType, Class extensionType) {
        if (!ws.getExtensionManager().getFactory().isRegisteredType(extensionPointType, extensionType.getName()) && !ws.getExtensionManager().getFactory().isRegisteredType(extensionPointType, extensionType)) {
            ws.getExtensionManager().getFactory().registerType(extensionPointType, extensionType);
            return true;
        }
        DefaultNutsWorkspace.log.log(Level.FINE, "Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionType.getName()});
        return false;
    }

    private List<Class> resolveComponentTypes(Class o) {
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

    private NutsTerminal createTerminal(Class ignoredClass) {
        NutsTerminal term = ws.getExtensionManager().getFactory().createSupported(NutsTerminal.class, ws.self());
        if (term == null) {
            throw new NutsUnsupportedOperationException("Should never happen ! Terminal could not be resolved.");
        } else {
            if (ignoredClass != null && ignoredClass.equals(term.getClass())) {
                return null;
            }
            term.install(ws.self(), null, null, null);
        }
        return term;
    }

    @Override
    public NutsWorkspaceFactory getFactory() {
        return factory;
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
        String repos = ws.getConfigManager().getEnv("bootstrapRepositoryLocations", "") + ";" + NutsConstants.URL_BOOTSTRAP_LOCAL + ";" + NutsConstants.URL_BOOTSTRAP_REMOTE;
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
            url = ws.expandPath(url);
            if (CoreIOUtils.isRemoteURL(url)) {
                return new URL(url);
            }
            return new File(url).toURI().toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }

}
