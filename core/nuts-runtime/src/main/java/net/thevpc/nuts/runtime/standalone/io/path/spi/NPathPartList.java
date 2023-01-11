package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NPathPartList implements Iterable<NPathPart> {
    private List<NPathPart> list = new ArrayList<>();
    private NSession session;

    public NPathPartList(List<NPathPart> list, NSession session) {
        this.session = session;
        for (int i = 0; i < list.size(); i++) {
            NPathPart p = list.get(i);
            if (p.isName()) {
                if (i > 0) {
                    throw new NIllegalArgumentException(session, NMsg.ofC("invalid part %s at %i", p, i));
                }
            } else if (p.isTrailingSeparator()) {
                if (i != list.size() - 1) {
                    throw new NIllegalArgumentException(session, NMsg.ofC("invalid part %s at %i", p, i));
                }
            }
        }
        this.list.addAll(list);
    }

    public NPathPartList concat(NPathPartList other) {
        return new NPathPartList(concat(list, other.list), session);
    }

    private List<NPathPart> concat(List<NPathPart> a, List<NPathPart> b) {
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
                NPathPart p = b.get(0);
                p = new NPathPart(File.separator, p.getName());
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
                NPathPart p = b.get(0);
                p = new NPathPart(a.get(0).getSeparator(), p.getName());
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
                NPathPart p = b.get(0);
                p = new NPathPart(a.get(0).getSeparator(), p.getName());
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

    public NPathPart get(int i) {
        if (i < 0) {
            return list.get(list.size() + i);
        }
        return list.get(i);
    }

    public NPathPart last() {
        return list.get(list.size() - 1);
    }

    public NPathPart first() {
        return list.get(0);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public String toString() {
        return list.stream().map(NPathPart::toString).collect(Collectors.joining());
    }

    @Override
    public Iterator<NPathPart> iterator() {
        return list.iterator();
    }

    public NPathPartList subList(int beginIndex, int endIndex) {
        return new NPathPartList(list.subList(beginIndex, endIndex), session);
    }

    public List<String> toStringList() {
        return stream().map(NPathPart::getName).collect(Collectors.toList());
    }

    public String[] toStringArray() {
        return stream().map(NPathPart::getName).toArray(String[]::new);
    }

    public Stream<NPathPart> stream() {
        return list.stream();
    }
}
