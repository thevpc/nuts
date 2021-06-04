package net.thevpc.nuts.runtime.core.format.tree;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public class DefaultNutsFormatDestructTypePredicate implements Predicate<Type> {
    public static final Predicate<Type> INSTANCE=new DefaultNutsFormatDestructTypePredicate();
    @Override
    public boolean test(Type x) {
        if (x instanceof Class) {
            Class c = (Class) x;
            return !(
//                    NutsId.class.isAssignableFrom(c)
//                    || NutsDependency.class.isAssignableFrom(c)
                    NutsString.class.isAssignableFrom(c)
                    || NutsElement.class.isAssignableFrom(c)
                    || NutsFormattable.class.isAssignableFrom(c)
                    || NutsMessage.class.isAssignableFrom(c)
            );
        }
        return false;
    }
}