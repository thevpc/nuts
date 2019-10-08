package net.vpc.app.nuts.runtime.format.elem;

import net.vpc.app.nuts.NutsElementBuilder;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.format.DefaultFormatBase;
import net.vpc.app.nuts.runtime.format.json.DefaultNutsJsonFormat;

public class DefaultNutsElementFormat extends DefaultFormatBase<NutsElementFormat> implements NutsElementFormat, NutsElementFactoryContext {

    private final NutsElementFactoryService nvalueFactory;
    private NutsElementFactory fallback;
    private final Map<String, Object> properties = new HashMap<>();
    private Object value;
    private NutsElementBuilder builder;

    public DefaultNutsElementFormat(NutsWorkspace ws) {
        super(ws, "element-format");
        nvalueFactory = new DefaultNutsElementFactoryService(ws);
        builder = new DefaultNutsElementBuilder();
    }

    @Override
    public NutsElementBuilder builder() {
        return builder;
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
        if (NutsElement.class.isAssignableFrom(cls)) {
            return (T) element;
        }
        if (NutsElement.class.isAssignableFrom(cls)) {
            return (T) element;
        }
        if (org.w3c.dom.Node.class.isAssignableFrom(cls)) {
            if (org.w3c.dom.Document.class.isAssignableFrom(cls)) {
                return (T) ws.xml().toXmlDocument(element);
            }
            if (org.w3c.dom.Element.class.isAssignableFrom(cls)) {
                return (T) ws.xml().toXmlElement(element, null);
            }
        }
        DefaultNutsJsonFormat json = (DefaultNutsJsonFormat) ws.json();
        return json.convert(element, cls);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NutsElementFormat set(Object value) {
        return setValue(value);
    }

    @Override
    public NutsElementFormat setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public void print(Writer out) {
        getWorkspace().json().value(value).print(out);
    }

    @Override
    public NutsElementPath compilePath(String pathExpression) {
        NutsSession session = getSession();
        if (session == null) {
            session = ws.createSession();
        }
        return NutsElementPathFilter.compile(pathExpression, session);
    }

}
