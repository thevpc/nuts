/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.ext;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.util.*;

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

    @Override
    public Set<NutsId> getCompanionIds() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(),getSession());
        NutsIdParser parser = session.id().parser();
        return Collections.unmodifiableSet(new HashSet<>(
                        Arrays.asList(parser.parse("net.thevpc.nuts.toolbox:nsh"))
                )
        );
    }


    @Override
    public boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl) {
        checkSession();
        return model.installWorkspaceExtensionComponent(extensionPointType, extensionImpl, session);
    }

    @Override
    public Set<Class> discoverTypes(NutsId id, ClassLoader classLoader) {
        checkSession();
        return model.discoverTypes(id, classLoader, session);
    }

    //    @Override
//    public Set<Class> discoverTypes(ClassLoader classLoader) {
//        return objectFactory.discoverTypes(classLoader);
//    }
    @Override
    public <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType) {
        checkSession();
        return model.createServiceLoader(serviceType, criteriaType, session);
    }

    @Override
    public <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        checkSession();
        return model.createServiceLoader(serviceType, criteriaType, classLoader, session);
    }

    public <T extends NutsComponent<V>, V> T createSupported(Class<T> serviceType, V criteriaType) {
        checkSession();
        return model.createSupported(serviceType, criteriaType, session);
    }

    @Override
    public <T extends NutsComponent<V>, V> T createSupported(Class<T> serviceType, V criteriaType, Class[] constructorParameterTypes, Object[] constructorParameters) {
        checkSession();
        return model.createSupported(serviceType, criteriaType, constructorParameterTypes, constructorParameters, session);
    }

    @Override
    public <T extends NutsComponent<V>, V> List<T> createAllSupported(Class<T> serviceType, V criteriaType) {
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
    public boolean registerType(Class extensionPointType, Class extensionType, NutsId source) {
        checkSession();
        return model.registerType(extensionPointType, extensionType, source, session);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, Class extensionType) {
        checkSession();
        return model.isRegisteredType(extensionPointType, extensionType, session);
    }

    @Override
    public boolean isLoadedExtensions(NutsId id) {
        checkSession();
        return model.isLoadedExtensions(id, session);
    }

    @Override
    public List<NutsId> getLoadedExtensions() {
        checkSession();
        return model.getLoadedExtensions(session);
    }

    @Override
    public NutsWorkspaceExtensionManager loadExtension(NutsId extension) {
        checkSession();
        model.loadExtension(extension, session);
        return this;
    }

    @Override
    public NutsWorkspaceExtensionManager unloadExtension(NutsId extension) {
        checkSession();
        model.unloadExtension(extension, session);
        return this;
    }

    @Override
    public List<NutsId> getConfigExtensions() {
        checkSession();
        return model.getConfigExtensions(session);
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsWorkspaceExtensionManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    public <T> T create(Class<T> type, String name, Class[] argTypes, Object[] args) {
        checkSession();
        return model.createApiTypeInstance(type, name, argTypes, args,session);
    }

}
