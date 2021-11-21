package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

import java.util.Iterator;

public class NamedIterator<T> extends AbstractNamedIterator<T> {

    private final Iterator<T> from;

    public NamedIterator(Iterator<T> from, String name) {
        super(name);
        this.from = from;

    }

    @Override
    public boolean hasNext() {
        return from != null && from.hasNext();
    }

    @Override
    public T next() {
        return from.next();
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return info("Named",
                IterInfoNode.resolveOrNull("base",from, session),
                IterInfoNode.resolveOrNull("name",super.name, session)
        );
    }
}
