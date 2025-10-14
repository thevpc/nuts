package net.thevpc.nuts.springboot;

import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.reflect.NBeanRef;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import org.springframework.context.ApplicationContext;

class NutsSpringBeanContainer implements NBeanContainer {
    private ApplicationContext sac;

    public NutsSpringBeanContainer(ApplicationContext sac) {
        this.sac = sac;
    }

    @Override
    public <T> NOptional<T> get(NBeanRef ref) {
        return NOptional.of((T)sac.getBean(ref.getId()), NMsg.ofC("bean %s",ref));
    }
}
