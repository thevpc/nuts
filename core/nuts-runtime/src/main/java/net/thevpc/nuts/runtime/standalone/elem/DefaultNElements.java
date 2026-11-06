package net.thevpc.nuts.runtime.standalone.elem;

import java.lang.reflect.Type;
import java.util.function.Consumer;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.elem.mapper.NElementMapperObjReflect;
import net.thevpc.nuts.runtime.standalone.elem.mapper.builder.NElementSerializerContextImpl;
import net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore.UserElementMapperStore;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.reflect.NReflectRepository;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNElements implements NElements {

    private static final NElementMapperObjReflect DEFAULT_OBJECT_MAPPER = new NElementMapperObjReflect();
    private final UserElementMapperStore userElementMapperStore;
    private boolean ntf;
    private final boolean readOnly;

    public DefaultNElements() {
        this(false, false);
    }

    public DefaultNElements(boolean ntf, boolean readOnly) {
        this.ntf = ntf;
        this.readOnly = readOnly;
        this.userElementMapperStore = new UserElementMapperStore(readOnly);
        this.userElementMapperStore.setReflectRepository(NReflectRepository.of());
    }

    private void checkReadOnly() {
        if (readOnly) {
            throw new NReadOnlyException(NMsg.ofC("mapper store is readonly"));
        }
    }

    public boolean isNtf() {
        return ntf;
    }

    @Override
    public NElements setNtf(boolean ntf) {
        if (ntf != this.ntf) {
            checkReadOnly();
            this.ntf = ntf;
        }
        return this;
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
    public Object toSimple(Object any) {
        return createFactoryContext().toSimple(any, null);
    }

    @Override
    public NElement toElement(Object o) {
        if (o instanceof NElement) {
            return (NElement) o;
        }
        return createFactoryContext().toElement(o);
    }

    @Override
    public <T> T fromElement(NElement o, Class<T> to) {
        return convert(o, to);
    }

    @Override
    public NElement defaultToElement(Object o) {
        if (o instanceof NElement) {
            return (NElement) o;
        }
        DefaultNElementFactoryContext context = createFactoryContext();
        if (o instanceof NToElement) {
            // the default form of an NToElement is its reflective mapping, not its own toElement().
            // this bypass must stay here : the internal default path (defaultCreateElement) is used
            // by the builtin mappers (NDependency, NText, ...) and must keep honoring NToElement
            return DEFAULT_OBJECT_MAPPER.toElement(NElementSerializerContextImpl.of(o, o.getClass(), context));
        }
        return context.defaultCreateElement(o, null);
    }


    @Override
    public <T> NOptional<T> defaultFromElement(NElement any, Class<T> to) {
        if (to == null || to.isInstance(any)) {
            return NOptional.of((T) any);
        }
        try {
            return NOptional.ofNamed(createFactoryContext().defaultToObject(any, to), to.getSimpleName());
        } catch (NException e) {
            return NOptional.ofError(e.formattedMessage());
        } catch (Exception e) {
            return NOptional.ofError(NMsg.ofP(e.getMessage()));
        }
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

    private DefaultNElementFactoryContext createFactoryContext() {
        NReflectRepository reflectRepository = NWorkspaceUtils.of().getReflectRepository();
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(false, reflectRepository, userElementMapperStore);
        return c;
    }


    public Object elementToObject(NElement o, Type type) {
        return createFactoryContext().toObject(o, type);
    }

}
