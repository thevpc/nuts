package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsStream;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NutsPathPartList implements Iterable<NutsPathPart> {
    private List<NutsPathPart> list = new ArrayList<>();
    private NutsSession session;

    public NutsPathPartList(List<NutsPathPart> list, NutsSession session) {
        this.session = session;
        for (int i = 0; i < list.size(); i++) {
            NutsPathPart p = list.get(i);
            if (p.isName()) {
                if (i > 0) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid part %s at %i", p, i));
                }
            } else if (p.isTrailingSeparator()) {
                if (i != list.size() - 1) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid part %s at %i", p, i));
                }
            }
        }
        this.list.addAll(list);
    }

    public NutsPathPartList concat(NutsPathPartList other) {
        return new NutsPathPartList(concat(list, other.list), session);
    }

    private List<NutsPathPart> concat(List<NutsPathPart> a, List<NutsPathPart> b) {
        a = new ArrayList<>(a);
        b = new ArrayList<>(b);
        if (b.isEmpty()) {
            return a;
        }
        if (a.isEmpty()) {
            return b;
        }
        if (!a.get(a.size() - 1).isTrailingSeparator()) {
            if (b.get(0).isSeparated()) {
                a.addAll(b);
                return a;
            } else if (b.get(0).isName()) {
                NutsPathPart p = b.get(0);
                p = new NutsPathPart(File.separator, p.getName());
                b.set(0, p);
                a.addAll(b);
                return a;
            } else {
                return a;
            }
        } else if (a.size() == 1) {
            if (b.get(0).isSeparated()) {
                return b;
            } else if (b.get(0).isName()) {
                NutsPathPart p = b.get(0);
                p = new NutsPathPart(a.get(0).getSeparator(), p.getName());
                b.set(0, p);
                return b;
            } else {
                return a;
            }
        } else {
            if (b.get(0).isSeparated()) {
                a.remove(0);
                a.addAll(b);
                return a;
            } else if (b.get(0).isName()) {
                NutsPathPart p = b.get(0);
                p = new NutsPathPart(a.get(0).getSeparator(), p.getName());
                b.set(0, p);
                a.remove(0);
                a.addAll(b);
                return a;
            } else {
                return a;
            }
        }
    }

    public int size() {
        return list.size();
    }

    public NutsPathPart get(int i) {
        if (i < 0) {
            return list.get(list.size() + i);
        }
        return list.get(i);
    }

    public NutsPathPart last() {
        return list.get(list.size() - 1);
    }

    public NutsPathPart first() {
        return list.get(0);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public String toString() {
        return list.stream().map(NutsPathPart::toString).collect(Collectors.joining());
    }

    @Override
    public Iterator<NutsPathPart> iterator() {
        return list.iterator();
    }

    public NutsPathPartList subList(int beginIndex, int endIndex) {
        return new NutsPathPartList(list.subList(beginIndex, endIndex), session);
    }

    public List<String> toStringList() {
        return stream().map(NutsPathPart::getName).collect(Collectors.toList());
    }

    public String[] toStringArray() {
        return stream().map(NutsPathPart::getName).toArray(String[]::new);
    }

    public Stream<NutsPathPart> stream() {
        return list.stream();
    }
}
