package net.vpc.app.nuts.core.format.elem;

import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.format.DefaultFormatBase0;
import net.vpc.app.nuts.core.format.json.DefaultNutsJsonFormat;

public class DefaultNutsElementFormat extends DefaultFormatBase0<NutsElementFormat> implements NutsElementFormat, NutsElementFactoryContext {

    private final NutsElementFactoryService nvalueFactory;
    private NutsElementFactory fallback;
    private final Map<String, Object> properties = new HashMap<>();

    public DefaultNutsElementFormat(NutsWorkspace ws) {
        super(ws,"element-format");
        nvalueFactory = new DefaultNutsElementFactoryService(ws);
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public NutsElement toElement(Object object) {
        return nvalueFactory.create(object, this);
    }

    @Override
    public NutsElementFactory getFallback() {
        return fallback;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void setFallback(NutsElementFactory fallback) {
        this.fallback = fallback;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        return false;
    }

    @Override
    public <T> T fromElement(NutsElement element, Class<T> cls) {
        if(NutsElement.class.isAssignableFrom(cls)){
            return (T)element;
        }
        if(NutsElement.class.isAssignableFrom(cls)){
            return (T)element;
        }
        if(org.w3c.dom.Node.class.isAssignableFrom(cls)){
            if(org.w3c.dom.Document.class.isAssignableFrom(cls)){
                return (T)ws.format().xml().toXmlDocument(element);
            }
            if(org.w3c.dom.Element.class.isAssignableFrom(cls)){
                return (T)ws.format().xml().toXmlElement(element,null);
            }
        }
        DefaultNutsJsonFormat json = (DefaultNutsJsonFormat)ws.format().json();
        return json.convert(element,cls);
    }
    
}
