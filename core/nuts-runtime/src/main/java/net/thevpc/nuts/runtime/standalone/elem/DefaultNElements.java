package net.thevpc.nuts.runtime.standalone.elem;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Consumer;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.expr.NOperatorAssociativity;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.elem.steps.NElementStepAnnotationParam;
import net.thevpc.nuts.runtime.standalone.elem.steps.NElementStepChild;
import net.thevpc.nuts.runtime.standalone.elem.steps.NElementStepParam;
import net.thevpc.nuts.runtime.standalone.elem.steps.NElementStepSubList;
import net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore.UserElementMapperStore;
import net.thevpc.nuts.runtime.standalone.elem.path.NElementSelectorFilters;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextManagerModel;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.reflect.NReflectRepository;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNElements implements NElements {

    private UserElementMapperStore userElementMapperStore;
    private boolean ntf;
    private boolean readOnly;

    public DefaultNElements() {
        this(false,false);
    }

    public DefaultNElements(boolean ntf,boolean readOnly) {
        this.ntf=ntf;
        this.readOnly=readOnly;
        this.userElementMapperStore = new UserElementMapperStore(readOnly);
        this.userElementMapperStore.setReflectRepository(NReflectRepository.of());
    }
    private void checkReadOnly(){
        if(readOnly){
            throw new NReadOnlyException(NMsg.ofC("mapper store is readonly"));
        }
    }

    public boolean isNtf() {
        return ntf;
    }

    @Override
    public NElements setNtf(boolean ntf) {
        if(ntf!=this.ntf) {
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
        if(o instanceof NElement){
            return  (NElement) o;
        }
        return createFactoryContext().toElement(o);
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

    private DefaultNElementFactoryContext createFactoryContext() {
        NReflectRepository reflectRepository = NWorkspaceUtils.of().getReflectRepository();
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(false, reflectRepository, userElementMapperStore);
        return c;
    }


    public Object elementToObject(NElement o, Type type) {
        return createFactoryContext().toObject(o, type);
    }

}
