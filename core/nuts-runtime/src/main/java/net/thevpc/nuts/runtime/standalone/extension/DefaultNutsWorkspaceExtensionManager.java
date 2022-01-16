/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
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
        NutsSessionUtils.checkSession(model.getWorkspace(),getSession());
        NutsIdParser parser = NutsIdParser.of(getSession());
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
    public <T extends NutsComponent, B> NutsServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType) {
        checkSession();
        return model.createServiceLoader(serviceType, criteriaType, session);
    }

    @Override
    public <T extends NutsComponent, B> NutsServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        checkSession();
        return model.createServiceLoader(serviceType, criteriaType, classLoader, session);
    }

    public <T extends NutsComponent, V> T createSupported(Class<T> serviceType, boolean required, V criteriaType) {
        checkSession();
        return model.createSupported(serviceType, criteriaType, required, session);
    }

//    @Override
//    public <T extends NutsComponent<V>, V> T createSupported(Class<T> serviceType, V criteriaType, Class[] constructorParameterTypes, boolean required, Object[] constructorParameters) {
//        checkSession();
//        return model.createSupported(serviceType, criteriaType, constructorParameterTypes, constructorParameters, required, session);
//    }

    @Override
    public <T extends NutsComponent, V> List<T> createAllSupported(Class<T> serviceType, V criteriaType) {
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
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    private void checkSession() {
        NutsSessionUtils.checkSession(model.getWorkspace(), session);
    }

}
