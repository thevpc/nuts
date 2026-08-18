/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NWorkspaceOptions;
import net.thevpc.nuts.elem.NElementFactory;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.internal.rpi.NReflectRPI;
import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.internal.rpi.NLogRPI;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScoredValue;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactory;
import net.thevpc.nuts.runtime.standalone.collections.DefaultNUtilsRPI;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLogRPI;
import net.thevpc.nuts.runtime.standalone.reflect.DefaultNReflectRPI;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextRPI;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.text.NMsg;

import java.net.URL;
import java.util.*;

/**
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNExtensions implements NExtensions {

    private NWorkspaceModel wsModel;

    public DefaultNExtensions(NWorkspaceModel wsModel) {
        this.wsModel = wsModel;
    }

    public DefaultNWorkspaceExtensionModel getModel() {
        return wsModel.extensionModel;
    }

    @Override
    public Set<NId> companionIds() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(NId.get("net.thevpc.nsh:nsh").get())));
    }

    @Override
    public Set<Class<?>> discoverTypes(NId id, ClassLoader classLoader) {
        return wsModel.extensionModel.discoverTypes(id, classLoader);
    }


    public <T> NOptional<NScorable> getTypeScorable(Class<? extends T> implType, Class<T> apiType) {
        return wsModel.extensionModel.getObjectFactory().getTypeScorer(implType, apiType);
    }

    public <T> NOptional<NScorable> getInstanceScorable(T instance, Class<T> apiType) {
        return wsModel.extensionModel.getObjectFactory().getInstanceScorer(instance, apiType);
    }

    @Override
    public <T> NScoredValue<T> getTypeScoredValue(Class<? extends T> implType, Class<T> apiType, NScorableContext scorableContext) {
        return wsModel.extensionModel.getObjectFactory().resolveTypeScore(implType, apiType,scorableContext);
    }

    @Override
    public <T> NScoredValue<T> getInstanceScoredValue(T instance, Class<T> apiType, NScorableContext scorableContext) {
        return wsModel.extensionModel.getObjectFactory().resolveInstanceScore(instance, apiType,scorableContext);
    }

    @Override
    public <T> NOptional<T> createComponent(Class<T> type) {
        return createSupported(type, null);
    }

    public <T, V> NOptional<T> createSupported(Class<T> serviceType, V criteriaType) {
        T d = wsModel.createRPI(serviceType);
        if(d!=null){
            return NOptional.of((T) d);
        }
        if (wsModel.extensionModel == null) {
            throw NException.ofSafeUnexpectedException(NMsg.ofC("Workspace is still booting and component container is not yet available. but you asked for %s", serviceType.getName()));
        }
        return wsModel.extensionModel.createSupported(serviceType, criteriaType);
    }

    @Override
    public <T, V> List<T> createAllSupported(Class<T> serviceType, V criteriaType) {
        return wsModel.extensionModel.createAllSupported(serviceType, criteriaType);
    }

    @Override
    public <T> List<T> createAll(Class<T> serviceType) {
        return wsModel.extensionModel.createAll(serviceType);
    }

    @Override
    public <T> Set<Class<? extends T>> getExtensionTypes(Class<T> extensionPoint) {
        return wsModel.extensionModel.getExtensionTypes(extensionPoint);
    }

    @Override
    public <T> List<T> getExtensionObjects(Class<T> extensionPoint) {
        return wsModel.extensionModel.getExtensionObjects(extensionPoint);
    }

    @Override
    public <T> boolean isRegisteredType(Class<T> extensionPointType, String name) {
        return wsModel.extensionModel.isRegisteredType(extensionPointType, name);
    }

    @Override
    public <T> boolean isRegisteredInstance(Class<T> extensionPointType, T extensionImpl) {
        return wsModel.extensionModel.isRegisteredInstance(extensionPointType, extensionImpl);
    }

    @Override
    public <T> boolean registerInstance(Class<T> extensionPointType, T extensionImpl) {
        return wsModel.extensionModel.registerInstance(extensionPointType, extensionImpl);
    }

    @Override
    public <T> boolean registerType(Class<T> extensionPointType, Class<? extends T> implementation, NId source) {
        return wsModel.extensionModel.registerType(extensionPointType, implementation, source);
    }

    @Override
    public <T> boolean isRegisteredType(Class<T> extensionPointType, Class<? extends T> implementation) {
        return wsModel.extensionModel.isRegisteredType(extensionPointType, implementation);
    }

    @Override
    public boolean isLoadedExtensions(NId id) {
        return wsModel.extensionModel.isLoadedExtensions(id);
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
        URL pomXml = classLoader.getResource("META-INF/maven/" + id.groupId() + "/" + id.artifactId() + "/pom.xml");
        if (pomXml != null) {
            NDescriptor e = NDescriptorParser.of()
                    .descriptorStyle(NDescriptorStyle.MAVEN)
                    .parse(pomXml).orNull();
            if (e != null) {
                if (e.id() != null) {
                    NVersion v = e.id().version();
                    if (v != null) {
                        NVersion v2 = id.version();
                        if (v2 != null && !v2.isBlank()) {
                            return v2.equals(v);
                        }
                    }
                    return true;
                }
            }
        }

        URL nuts = classLoader.getResource("META-INF/nuts/"
                + id.shortId().mavenFolder() + "/nuts.json");
        if (nuts != null) {
            NDescriptor e = NDescriptorParser.of()
                    .descriptorStyle(NDescriptorStyle.NUTS)
                    .parse(nuts).orNull();
            if (e != null) {
                if (e.id() != null) {
                    NVersion v = e.id().version();
                    if (v != null) {
                        NVersion v2 = id.version();
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
    public List<NId> loadedExtensions() {
        return wsModel.extensionModel.getLoadedExtensions();
    }

    @Override
    public NExtensions loadExtension(NId extension) {
        wsModel.extensionModel.loadExtension(extension);
        return this;
    }

    @Override
    public NExtensions unloadExtension(NId extension) {
        wsModel.extensionModel.unloadExtension(extension);
        return this;
    }

    @Override
    public List<NId> configExtensions() {
        return wsModel.extensionModel.getConfigExtensions();
    }

    @Override
    public boolean isExcludedExtension(String extensionId, NWorkspaceOptions options) {
        return wsModel.configModel.isExcludedExtension(extensionId, options);
    }
}
