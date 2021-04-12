/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.ext;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.*;
import java.util.*;
import net.thevpc.nuts.runtime.standalone.ext.DefaultNutsWorkspaceExtensionModel;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 * @author thevpc
 */
public class DefaultNutsWorkspaceExtensionManager implements NutsWorkspaceExtensionManager {

    private DefaultNutsWorkspaceExtensionModel model;
    private NutsSession session;

    public DefaultNutsWorkspaceExtensionManager(DefaultNutsWorkspaceExtensionModel model) {
        this.model = model;
    }

    public DefaultNutsWorkspaceExtensionModel getModel() {
        return model;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsWorkspaceExtensionManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.installWorkspaceExtensionComponent(extensionPointType, extensionImpl, session);
    }

    @Override
    public Set<Class> discoverTypes(NutsId id, ClassLoader classLoader) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.discoverTypes(id, classLoader, session);
    }

//    @Override
//    public Set<Class> discoverTypes(ClassLoader classLoader) {
//        return objectFactory.discoverTypes(classLoader);
//    }
    @Override
    public <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.createServiceLoader(serviceType, criteriaType, session);
    }

    @Override
    public <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.createServiceLoader(serviceType, criteriaType, classLoader, session);
    }

    public <T extends NutsComponent<V>, V> T createSupported(Class<T> serviceType, V criteriaType) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.createSupported(serviceType, criteriaType, session);
    }

    @Override
    public <T extends NutsComponent<V>, V> T createSupported(Class<T> serviceType, V criteriaType, Class[] constructorParameterTypes, Object[] constructorParameters) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.createSupported(serviceType, criteriaType, constructorParameterTypes, constructorParameters, session);
    }

    @Override
    public <T extends NutsComponent<V>, V> List<T> createAllSupported(Class<T> serviceType, V criteriaType) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.createAllSupported(serviceType, criteriaType, session);
    }

    @Override
    public <T> List<T> createAll(Class<T> serviceType) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.createAll(serviceType, session);
    }

    @Override
    public Set<Class> getExtensionTypes(Class extensionPoint) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.getExtensionTypes(extensionPoint, session);
    }

    @Override
    public List<Object> getExtensionObjects(Class extensionPoint) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.getExtensionObjects(extensionPoint, session);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, String name) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.isRegisteredType(extensionPointType, name, session);
    }

    @Override
    public boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.isRegisteredInstance(extensionPointType, extensionImpl, session);
    }

    @Override
    public boolean registerInstance(Class extensionPointType, Object extensionImpl) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.registerInstance(extensionPointType, extensionImpl, session);
    }

    @Override
    public boolean registerType(Class extensionPointType, Class extensionType, NutsId source) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.registerType(extensionPointType, extensionType, source, session);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, Class extensionType) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.isRegisteredType(extensionPointType, extensionType, session);
    }

    @Override
    public boolean isLoadedExtensions(NutsId id) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.isLoadedExtensions(id, session);
    }

    @Override
    public List<NutsId> getLoadedExtensions() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.getLoadedExtensions(session);
    }

    @Override
    public NutsWorkspaceExtensionManager loadExtension(NutsId extension) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        model.loadExtension(extension, session);
        return this;
    }

    @Override
    public NutsWorkspaceExtensionManager unloadExtension(NutsId extension) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        model.unloadExtension(extension, session);
        return this;
    }

    @Override
    public List<NutsId> getConfigExtensions() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.getConfigExtensions(session);
    }

}
