package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.util.NEvictingCharQueue;

import java.util.Arrays;

public class NEvictingCharQueueImpl implements NEvictingCharQueue {
    private int from=0;
    private int len;
    private char[] values;

    public NEvictingCharQueueImpl(int max) {
        this.values=new char[max];
    }

    @Override
    public void clear() {
        from=0;
        len=0;
    }

    @Override
    public int size() {
        return len;
    }

    @Override
    public char get(int pos) {
        if(pos>=0 && pos<len) {
            int i=(from+pos)%values.length;
            return values[i];
        }
        throw new IllegalArgumentException("invalid index "+pos);
    }

    @Override
    public void add(char t) {
        int pos=(from+len)%values.length;
        values[pos]=t;
        if(len<values.length){
            len++;
        }else{
            from=(from+1)%values.length;
        }
    }

    public String dump() {
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < size(); i++) {
            sb.append(get(i));
        }
        return "EvictingCharQueue{" +
                "from=" + from +
                ", len=" + len +
                ", raw=" + Arrays.toString(values) +
                ", values='" + sb +"'"+
                '}';
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < size(); i++) {
            sb.append(get(i));
        }
        return sb.toString();
    }
}
