package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.runtime.standalone.format.elem.parser.mapperstore.UserElementMapperStore;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextManagerModel;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.reflect.NReflectRepository;

public class DefaultNElements implements NElements {

    private final DefaultNTextManagerModel model;
    private UserElementMapperStore userElementMapperStore;
    private boolean ntf;

    public DefaultNElements() {
        this.model = NWorkspaceExt.of().getModel().textModel;
        this.userElementMapperStore = new UserElementMapperStore();
    }

    public boolean isNtf() {
        return ntf;
    }

    @Override
    public NElements setNtf(boolean ntf) {
        this.ntf = ntf;
        return this;
    }

    @Override
    public NElementPath compilePath(String pathExpression) {
        return NElementPathFilter.compile(pathExpression);
    }

    @Override
    public <T> T convert(Object any, Class<T> to) {
        if (to == null || to.isInstance(any)) {
            return (T) any;
        }
        NElement e = toElement(any);
        return (T) elementToObject(e, to);
    }

    @Override
    public Object destruct(Object any) {
        return createFactoryContext().destruct(any, null);
    }

    @Override
    public NElement toElement(Object o) {
        return createFactoryContext().createElement(o);
    }

    @Override
    public <T> T fromElement(NElement o, Class<T> to) {
        return convert(o, to);
    }


    @Override
    public NElementMapperStore mapperStore() {
        return userElementMapperStore;
    }

    @Override
    public NElement normalizeJson(NElement e) {
        return normalize(e, NContentType.JSON);
    }

    @Override
    public NElement normalizeTson(NElement e) {
        return normalize(e, NContentType.TSON);
    }

    @Override
    public NElement normalizeYaml(NElement e) {
        return normalize(e, NContentType.YAML);
    }

    @Override
    public NElement normalizeXml(NElement e) {
        return normalize(e, NContentType.XML);
    }

    public NElement normalize(NElement e, NContentType contentType) {
        return resolveStructuredFormat(contentType).normalize(e == null ? NElement.ofNull() : e);
    }

    private NElementStreamFormat resolveStructuredFormat(NContentType contentType) {
        switch (contentType) {
            case JSON: {
                return model.getJsonMan();
            }
            case YAML: {
                return model.getYamlMan();
            }
            case XML: {
                return model.getXmlMan();
            }
            case TSON: {
                return model.getTsonMan();
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    private DefaultNElementFactoryContext createFactoryContext() {
        NReflectRepository reflectRepository = NWorkspaceUtils.of().getReflectRepository();
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(false, reflectRepository, userElementMapperStore, indestructibleObjects);
        return c;
    }


    public Object elementToObject(NElement o, Type type) {
        return createFactoryContext().createObject(o, type);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

}
