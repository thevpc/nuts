package net.thevpc.nuts.runtime.standalone.format.elem;

import java.lang.reflect.Type;
import java.util.function.Consumer;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.runtime.standalone.format.elem.parser.mapperstore.UserElementMapperStore;
import net.thevpc.nuts.runtime.standalone.format.elem.path.NElementPathFilter;
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
        this.userElementMapperStore.setReflectRepository(NReflectRepository.of());
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
    public NElements doWithMapperStore(Consumer<NElementMapperStore> doWith) {
        if (doWith != null) {
            doWith.accept(mapperStore());
        }
        return this;
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
        return model.getStreamFormat(contentType == null ? NContentType.JSON : contentType).normalize(e == null ? NElement.ofNull() : e);
    }


    private DefaultNElementFactoryContext createFactoryContext() {
        NReflectRepository reflectRepository = NWorkspaceUtils.of().getReflectRepository();
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(false, reflectRepository, userElementMapperStore);
        return c;
    }


    public Object elementToObject(NElement o, Type type) {
        return createFactoryContext().createObject(o, type);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public NElementType commonNumberType(NElementType aa, NElementType bb) {
        if (aa != null) {
            NAssert.requireEquals(NElementTypeGroup.NUMBER, aa.typeGroup(), "aa typeGroup");
        }
        if (bb != null) {
            NAssert.requireEquals(NElementTypeGroup.NUMBER, bb.typeGroup(), "bb typeGroup");
        }

        if (aa == null && bb == null) {
            return null;
        }
        if (aa == null) {
            return bb;
        }
        if (bb == null) {
            return aa;
        }
        if (NElementType.BIG_COMPLEX == aa || NElementType.BIG_COMPLEX.equals(bb)) {
            return NElementType.BIG_COMPLEX;
        }

        if (NElementType.DOUBLE_COMPLEX == aa || NElementType.DOUBLE_COMPLEX.equals(bb)) {
            if (
                    NElementType.BIG_DECIMAL == aa || NElementType.BIG_DECIMAL.equals(bb)
                            || NElementType.BIG_INT == aa || NElementType.BIG_INT.equals(bb)
            ) {
                return NElementType.BIG_COMPLEX;
            }
            return NElementType.DOUBLE_COMPLEX;
        }

        if (NElementType.FLOAT_COMPLEX == aa || NElementType.FLOAT_COMPLEX.equals(bb)) {
            if (
                    NElementType.BIG_DECIMAL == aa || NElementType.BIG_DECIMAL.equals(bb)
                            || NElementType.BIG_INT == aa || NElementType.BIG_INT.equals(bb)
            ) {
                return NElementType.BIG_COMPLEX;
            }
            if (
                    NElementType.DOUBLE == aa || NElementType.DOUBLE == bb
            ) {
                return NElementType.DOUBLE_COMPLEX;
            }
            return NElementType.FLOAT_COMPLEX;
        }


        if (NElementType.BIG_DECIMAL == aa || NElementType.BIG_DECIMAL.equals(bb)) {
            return NElementType.BIG_DECIMAL;
        }
        if (NElementType.BIG_INT.equals(aa) || NElementType.BIG_INT.equals(bb)) {
            if (NElementType.DOUBLE.equals(aa) || NElementType.DOUBLE.equals(bb) || NElementType.FLOAT.equals(aa) || NElementType.FLOAT.equals(bb)) {
                return NElementType.BIG_DECIMAL;
            }
            return NElementType.BIG_INT;
        }
        if (NElementType.DOUBLE.equals(aa) || NElementType.DOUBLE.equals(bb)) {
            return NElementType.DOUBLE;
        }
        if (NElementType.FLOAT.equals(aa) || NElementType.FLOAT.equals(bb)) {
            if (NElementType.LONG.equals(aa) || NElementType.LONG.equals(bb)) {
                return NElementType.DOUBLE;
            }
            return NElementType.FLOAT;
        }
        if (NElementType.LONG.equals(aa) || NElementType.LONG.equals(bb)) {
            return NElementType.LONG;
        }
        if (NElementType.INT.equals(aa) || NElementType.INT.equals(bb)) {
            return NElementType.INT;
        }
        if (NElementType.SHORT.equals(aa) || NElementType.SHORT.equals(bb)) {
            return NElementType.SHORT;
        }
        if (NElementType.BYTE.equals(aa) || NElementType.BYTE.equals(bb)) {
            return NElementType.BYTE;
        }
        return aa;
    }
}
