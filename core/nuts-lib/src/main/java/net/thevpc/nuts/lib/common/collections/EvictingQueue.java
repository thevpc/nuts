package net.thevpc.nuts.lib.common.collections;

import java.util.AbstractList;
import java.util.Arrays;

public class EvictingQueue<T> extends AbstractList<T>{
    private int from=0;
    private int len;
    private Object[] values;

    public EvictingQueue(int max) {
        this.values=new Object[max];
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
    public T get(int pos) {
        if(pos>=0 && pos<len) {
            int i=(from+pos)%values.length;
            return (T)values[i];
        }
        throw new IllegalArgumentException("invalid index "+pos);
    }


    @Override
    public boolean add(T t) {
        int pos=(from+len)%values.length;
        values[pos]=t;
        if(len<values.length){
            len++;
        }else{
            from=(from+1)%values.length;
        }
        return true;
    }

    public String dump() {
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < size(); i++) {
            sb.append(get(i));
        }
        return "EvictingQueue{" +
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
