package net.thevpc.nuts.util;

import java.util.Arrays;

public class NEvictingCharQueue {
    private int from=0;
    private int len;
    private char[] values;

    public NEvictingCharQueue(int max) {
        this.values=new char[max];
    }

    public void clear() {
        from=0;
        len=0;
    }

    public int size() {
        return len;
    }

    public char get(int pos) {
        if(pos>=0 && pos<len) {
            int i=(from+pos)%values.length;
            return values[i];
        }
        throw new IllegalArgumentException("invalid index "+pos);
    }

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
