package net.thevpc.nuts.runtime.bundles.collections;

import java.util.Arrays;

public class EvictingIntQueue {
    private int from=0;
    private int len;
    private int[] values;

    public EvictingIntQueue(int max) {
        this.values=new int[max];
    }

    public void clear() {
        from=0;
        len=0;
    }

    public int size() {
        return len;
    }

    public int get(int pos) {
        if(pos>=0 && pos<len) {
            int i=(from+pos)%values.length;
            return values[i];
        }
        throw new IllegalArgumentException("invalid index "+pos);
    }

    public void add(int t) {
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
        return "EvictingIntQueue{" +
                "from=" + from +
                ", len=" + len +
                ", raw=" + Arrays.toString(values) +
                ", values='" + sb +"'"+
                '}';
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder("{");
        for (int i = 0; i < size(); i++) {
            if(i>0){
                sb.append(",");
            }
            sb.append(get(i));
        }
        sb.append("}");
        return sb.toString();
    }
}
