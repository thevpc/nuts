/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NServiceLoader;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NIdUtils;

import java.net.URL;
import java.util.*;

/**
 * @author thevpc
 */
public class DefaultNExtensions implements NExtensions {

    private DefaultNWorkspaceExtensionModel model;
    private NSession session;
    private final DefaultNWorkspaceConfigModel cmodel;

    public DefaultNExtensions(NSession session) {
        this.session = session;
        NWorkspace w = this.session.getWorkspace();
        NWorkspaceExt e = (NWorkspaceExt) w;
        this.model = e.getModel().extensionModel;
        this.cmodel = e.getModel().configModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NCallableSupport.DEFAULT_SUPPORT;
    }

    public DefaultNWorkspaceExtensionModel getModel() {
        return model;
    }

    @Override
    public Set<NId> getCompanionIds() {
        NSessionUtils.checkSession(model.getWorkspace(), getSession());
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(NId.of("net.thevpc.nuts.toolbox:nsh").get())));
    }


    @Override
    public boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl) {
        checkSession();
        return model.installWorkspaceExtensionComponent(extensionPointType, extensionImpl, session);
    }

    @Override
    public Set<Class> discoverTypes(NId id, ClassLoader classLoader) {
        checkSession();
        return model.discoverTypes(id, classLoader, session);
    }

    //    @Override
//    public Set<Class> discoverTypes(ClassLoader classLoader) {
//        return objectFactory.discoverTypes(classLoader);
//    }
    @Override
    public <T extends NComponent, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType) {
        checkSession();
        return model.createServiceLoader(serviceType, criteriaType, session);
    }

    @Override
    public <T extends NComponent, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        checkSession();
        return model.createServiceLoader(serviceType, criteriaType, classLoader, session);
    }

    @Override
    public <T extends NComponent> NOptional<T> createComponent(Class<T> type) {
        return createComponent(type, null);
    }

    public <T extends NComponent, V> NOptional<T> createComponent(Class<T> serviceType, V criteriaType) {
        checkSession();
        return model.createSupported(serviceType, criteriaType, session);
    }


//    @Override
//    public <T extends NutsComponent<V>, V> T createSupported(Class<T> serviceType, V criteriaType, Class[] constructorParameterTypes, boolean required, Object[] constructorParameters) {
//        checkSession();
//        return model.createSupported(serviceType, criteriaType, constructorParameterTypes, constructorParameters, required, session);
//    }

    @Override
    public <T extends NComponent, V> List<T> createComponents(Class<T> serviceType, V criteriaType) {
        checkSession();
        return model.createAllSupported(serviceType, criteriaType, session);
    }

    @Override
    public <T> List<T> createAll(Class<T> serviceType) {
        checkSession();
        return model.createAll(serviceType, session);
    }

    @Override
    public Set<Class> getExtensionTypes(Class extensionPoint) {
        checkSession();
        return model.getExtensionTypes(extensionPoint, session);
    }

    @Override
    public List<Object> getExtensionObjects(Class extensionPoint) {
        checkSession();
        return model.getExtensionObjects(extensionPoint, session);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, String name) {
        checkSession();
        return model.isRegisteredType(extensionPointType, name, session);
    }

    @Override
    public boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl) {
        checkSession();
        return model.isRegisteredInstance(extensionPointType, extensionImpl, session);
    }

    @Override
    public boolean registerInstance(Class extensionPointType, Object extensionImpl) {
        checkSession();
        return model.registerInstance(extensionPointType, extensionImpl, session);
    }

    @Override
    public boolean registerType(Class extensionPointType, Class extensionType, NId source) {
        checkSession();
        return model.registerType(extensionPointType, extensionType, source, session);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, Class extensionType) {
        checkSession();
        return model.isRegisteredType(extensionPointType, extensionType, session);
    }

    @Override
    public boolean isLoadedExtensions(NId id) {
        checkSession();
        return model.isLoadedExtensions(id, session);
    }

    @Override
    public boolean isLoadedId(NId id) {
        return isLoadedId(id, null);
    }

    @Override
    public boolean isLoadedId(NId id, ClassLoader classLoader) {
        if (id == null) {
            return false;
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        URL pomXml = classLoader.getResource("META-INF/maven/" + id.getGroupId() + "/" + id.getArtifactId() + "/pom.xml");
        if (pomXml != null) {
            NDescriptor e = NDescriptorParser.of(getSession())
                    .setDescriptorStyle(NDescriptorStyle.MAVEN)
                    .parse(pomXml).orNull();
            if (e != null) {
                if (e.getId() != null) {
                    NVersion v = e.getId().getVersion();
                    if (v != null) {
                        NVersion v2 = id.getVersion();
                        if (v2 != null && !v2.isBlank()) {
                            return v2.equals(v);
                        }
                    }
                    return true;
                }
            }
        }

        URL nuts = classLoader.getResource("META-INF/nuts/"
                + NIdUtils.resolveIdPath(id.getShortId()) + "/nuts.json");
        if (nuts != null) {
            NDescriptor e = NDescriptorParser.of(getSession())
                    .setDescriptorStyle(NDescriptorStyle.NUTS)
                    .parse(nuts).orNull();
            if (e != null) {
                if (e.getId() != null) {
                    NVersion v = e.getId().getVersion();
                    if (v != null) {
                        NVersion v2 = id.getVersion();
                        if (v2 != null && !v2.isBlank()) {
                            return v2.equals(v);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<NId> getLoadedExtensions() {
        checkSession();
        return model.getLoadedExtensions(session);
    }

    @Override
    public NExtensions loadExtension(NId extension) {
        checkSession();
        model.loadExtension(extension, session);
        return this;
    }

    @Override
    public NExtensions unloadExtension(NId extension) {
        checkSession();
        model.unloadExtension(extension, session);
        return this;
    }

    @Override
    public List<NId> getConfigExtensions() {
        checkSession();
        return model.getConfigExtensions(session);
    }

    public NSession getSession() {
        return session;
    }

    public NExtensions setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    private void checkSession() {
        NSessionUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public boolean isExcludedExtension(String extensionId, NWorkspaceOptions options) {
        checkSession();
        return cmodel.isExcludedExtension(extensionId, options, session);
    }
}
