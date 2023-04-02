package net.thevpc.nuts.toolbox.nsh.cmds.util;

import java.util.Iterator;
import java.util.LinkedList;

public class NNumberedIterator<T> implements Iterator<NNumberedObject<T>> {

    private long curr = 0;
    private long from;
    private long to;
    private final Iterator<T> base;
    private final LinkedList<NNumberedObject<T>> buffer = new LinkedList<>();
    private boolean ended = false;

    public NNumberedIterator(Iterator<T> base, Long from0, Long to0) {
        this.base = base;
        this.from = from0 == null ? 0L : from0;
        this.to = to0 == null ? 0L : to0;
        if (from == 1) {
            from = 0;
        }
        if (to == -1) {
            to = 0;
        }
        if (from != 0 && to != 0) {
            if ((to > 0 && from > 0 && to < from) || (to < 0 && from < 0 && to < from)) {
                ended = true;
            }
        }
    }

    @Override
    public boolean hasNext() {
        while (true) {
            if (ended) {
                return false;
            }
            NNumberedObject<T> r = read();
            if (r != null) {
                buffer.add(r);
            } else if (buffer.isEmpty()) {
                ended = true;
                return false;
            }
            NNumberedObject<T> t = buffer.getFirst();
            long n = t.getNumber();
            boolean accept = true;
            if (from > 0) {
                if (n < from) {
                    buffer.removeFirst();
                    accept = false;
                }
            } else if (from < 0) {
                ensureRead(((int) -from) + 1);
                if (buffer.size() <= -from) {
                    //ok
                } else {
                    buffer.removeFirst();
                    accept = false;
                }
            } else {
                //ok
            }
            if (to > 0) {
                if (n > to) {
                    ended = true;
                    return false;
                }
            } else if (to < 0) {
                ensureRead(((int) -to));
                if (buffer.size() < -to) {
                    ended = true;
                    return false;
                }
            } else {
                //ok
            }
            if (accept) {
                return true;
            }
        }
    }


    private void ensureRead(int n) {
        int size = buffer.size();
        while (size < n) {
            NNumberedObject<T> r = read();
            if (r != null) {
                buffer.add(r);
                size++;
            } else {
                break;
            }
        }
    }

    private NNumberedObject<T> read() {
        T l = null;
        if (base.hasNext()) {
            l = base.next();
            curr++;
            return new NNumberedObject<T>(l, curr);
        }
        return null;
    }

    @Override
    public NNumberedObject<T> next() {
        if (buffer.size() > 0) {
            return buffer.removeFirst();
        }
        return null;
    }
}
