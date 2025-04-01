/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NFormats;
import net.thevpc.nuts.io.NServiceLoader;
import net.thevpc.nuts.runtime.standalone.format.NFormatsImpl;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTexts;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NOptional;

import java.net.URL;
import java.util.*;

/**
 * @author thevpc
 */
public class DefaultNExtensions implements NExtensions {

    private NWorkspaceModel wsModel;

    public DefaultNExtensions(NWorkspaceModel wsModel) {
        this.wsModel = wsModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public DefaultNWorkspaceExtensionModel getModel() {
        return wsModel.extensionModel;
    }

    @Override
    public Set<NId> getCompanionIds() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(NId.get("net.thevpc.nsh:nsh").get())));
    }


    @Override
    public <T extends NComponent> boolean installWorkspaceExtensionComponent(Class<T> extensionPointType, T extensionImpl) {
        return wsModel.extensionModel.installWorkspaceExtensionComponent(extensionPointType, extensionImpl);
    }

    @Override
    public Set<Class<? extends NComponent>> discoverTypes(NId id, ClassLoader classLoader) {
        return wsModel.extensionModel.discoverTypes(id, classLoader);
    }

    //    @Override
//    public Set<Class> discoverTypes(ClassLoader classLoader) {
//        return objectFactory.discoverTypes(classLoader);
//    }
    @Override
    public <T extends NComponent, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType) {
        return wsModel.extensionModel.createServiceLoader(serviceType, criteriaType);
    }

    @Override
    public <T extends NComponent, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        return wsModel.extensionModel.createServiceLoader(serviceType, criteriaType, classLoader);
    }

    @Override
    public <T extends NComponent> NOptional<T> createComponent(Class<T> type) {
        return createComponent(type, null);
    }

    public <T extends NComponent, V> NOptional<T> createComponent(Class<T> serviceType, V criteriaType) {
        switch (serviceType.getName()) {
            case "net.thevpc.nuts.text.NTexts": {
                NTexts t = wsModel.textModel.defaultNTexts;
                if (t == null) {
                    t = new DefaultNTexts();
                    wsModel.textModel.defaultNTexts = t;
                }
                return NOptional.of((T) t);
            }
            case "net.thevpc.nuts.format.NFormats": {
                NFormats t = wsModel.textModel.defaultNFormats;
                if (t == null) {
                    t = new NFormatsImpl();
                    wsModel.textModel.defaultNFormats = t;
                }
                return NOptional.of((T) t);
            }
        }
        return wsModel.extensionModel.createSupported(serviceType, criteriaType);
    }


//    @Override
//    public <T extends NutsComponent<V>, V> T createSupported(Class<T> serviceType, V criteriaType, Class[] constructorParameterTypes, boolean required, Object[] constructorParameters) {
//        checkSession();
//        return model.createSupported(serviceType, criteriaType, constructorParameterTypes, constructorParameters, required, session);
//    }

    @Override
    public <T extends NComponent, V> List<T> createComponents(Class<T> serviceType, V criteriaType) {
        return wsModel.extensionModel.createAllSupported(serviceType, criteriaType);
    }

    @Override
    public <T extends NComponent> List<T> createAll(Class<T> serviceType) {
        return wsModel.extensionModel.createAll(serviceType);
    }

    @Override
    public <T extends NComponent> Set<Class<? extends T>> getExtensionTypes(Class<T> extensionPoint) {
        return wsModel.extensionModel.getExtensionTypes(extensionPoint);
    }

    @Override
    public <T extends NComponent> List<T> getExtensionObjects(Class<T> extensionPoint) {
        return wsModel.extensionModel.getExtensionObjects(extensionPoint);
    }

    @Override
    public <T extends NComponent> boolean isRegisteredType(Class<T> extensionPointType, String name) {
        return wsModel.extensionModel.isRegisteredType(extensionPointType, name);
    }

    @Override
    public <T extends NComponent> boolean isRegisteredInstance(Class<T> extensionPointType, T extensionImpl) {
        return wsModel.extensionModel.isRegisteredInstance(extensionPointType, extensionImpl);
    }

    @Override
    public <T extends NComponent> boolean registerInstance(Class<T> extensionPointType, T extensionImpl) {
        return wsModel.extensionModel.registerInstance(extensionPointType, extensionImpl);
    }

    @Override
    public <T extends NComponent> boolean registerType(Class<T> extensionPointType, Class<? extends T> implementation, NId source) {
        return wsModel.extensionModel.registerType(extensionPointType, implementation, source);
    }

    @Override
    public <T extends NComponent> boolean isRegisteredType(Class<T> extensionPointType, Class<? extends T> implementation) {
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
        URL pomXml = classLoader.getResource("META-INF/maven/" + id.getGroupId() + "/" + id.getArtifactId() + "/pom.xml");
        if (pomXml != null) {
            NDescriptor e = NDescriptorParser.of()
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
                + ExtraApiUtils.resolveIdPath(id.getShortId()) + "/nuts.json");
        if (nuts != null) {
            NDescriptor e = NDescriptorParser.of()
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
    public List<NId> getConfigExtensions() {
        return wsModel.extensionModel.getConfigExtensions();
    }

    @Override
    public boolean isExcludedExtension(String extensionId, NWorkspaceOptions options) {
        return wsModel.configModel.isExcludedExtension(extensionId, options);
    }
}
