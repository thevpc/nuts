package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.NDoubleList;import java.util.Arrays;
import java.util.List;

public class NDoubleArrayList implements NDoubleList {
    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private double[] values;
    private int size;

    public NDoubleArrayList(int initialSize) {
        values = new double[initialSize];
        size = 0;
    }

    public NDoubleArrayList() {
        values = new double[0];
        size = 0;
    }

    public NDoubleArrayList(double[] values, int offset, int size) {
        this.values = new double[size];
        System.arraycopy(values, offset, this.values, 0, Math.min(this.values.length, size));
    }

    public NDoubleArrayList(double[] values) {
        this(values,values.length);
    }

    public NDoubleArrayList(double[] values, int size) {
        if (size < values.length) {
            this.values = Arrays.copyOf(values, size);
            this.size = size;
        } else {
            this.values = values;
            ensureSize(size);
            this.size = size;
        }
    }

    @Override
    public NDoubleList add(int index, double value){
        ensureSize(size + 1);  // Increments modCount!!
        System.arraycopy(values, index, values, index + 1,
                size - index);
        values[index] = value;
        size++;
        return this;
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
    }

    /**
     * @param offset
     * @param count
     * @return number of elements removed
     */
    @Override
    public int removeAll(int offset, int count) {
        if (offset < 0) {
            count += offset;
            offset = 0;
        }
        if (offset + count > size) {
            count = size - offset;
        }
        int numMoved = size - offset - count;
        if (numMoved > 0) {
            System.arraycopy(values, offset + count, values, offset, numMoved);
            size -= count;
        }
        //values[size] = 0;
        return numMoved;
    }

    @Override
    public double remove(int index){
        rangeCheck(index);

//        modCount++;
        double oldValue = values[index];

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(values, index + 1, values, index, numMoved);
            --size;
            values[size] = 0;
        }
        return oldValue;
    }

    @Override
    public NDoubleList addAll(NDoubleList values) {
        int toAddLength = values.size();
        ensureSize(size + toAddLength);
        System.arraycopy(values.toArray(), 0, this.values, size, toAddLength);
        this.size += toAddLength;
        return this;
    }

    @Override
    public NDoubleList addAll(double... values) {
        int toAddLength = values.length;
        ensureSize(size + toAddLength);
        System.arraycopy(values, 0, this.values, size, toAddLength);
        this.size += toAddLength;
        return this;
    }

    @Override
    public NDoubleList insertAll(int offset, double... values) {
        if(offset<0){
            throw new IllegalArgumentException("offset should be >=0");
        }
        int toAddLength = values.length;
        ensureSize(size + toAddLength);
        System.arraycopy(this.values, offset, this.values, offset + values.length, size - offset);
        System.arraycopy(values, 0, this.values, offset, toAddLength);
        this.size += toAddLength;
        return this;
    }

    @Override
    public NDoubleArrayList subList(int offset, int count) {
        if (offset < 0) {
            count += offset;
            offset = 0;
        }
        if (offset + count > size) {
            count = size - offset;
        }
        return new NDoubleArrayList(this.values, offset, count);
    }

    @Override
    public NDoubleList replaceSubList(int offset, int count, double... replacement) {
        if (offset < 0) {
            count += offset;
            offset = 0;
        }
        if (offset + count > size) {
            count = size - offset;
        }
        int newSize = this.size - count + replacement.length;
        ensureSize(newSize);
        System.arraycopy(this.values, offset + count, this.values, offset + replacement.length, this.size - offset - count);
        System.arraycopy(replacement, 0, this.values, offset, replacement.length);
        this.size = newSize;
        return this;
    }

    @Override
    public NDoubleList add(double value){
        ensureSize(size+1);
        values[size++]=value;
        return this;
    }

    @Override
    public NDoubleList trimToSize(){
        if(size<values.length){
            values= Arrays.copyOf(values,size);
        }
        return this;
    }

    @Override
    public NDoubleList copy() {
        return new NDoubleArrayList(Arrays.copyOf(values, values.length),size);
    }

    @Override
    public double[] toArray(){
        return Arrays.copyOf(values,size);
    }

    @Override
    public NLongArrayList toLongList() {
        long[] r = new long[size];
        for (int i = 0; i < size; i++) {
            r[i] = (long)values[i];
        }
        return new NLongArrayList(r, r.length);
    }

    @Override
    public Double[] toDoubleArray(){
        Double[] all=new Double[size];
        for (int i = 0; i < size; i++) {
            all[i]=values[i];
        }
        return all;
    }

    @Override
    public List<Double> toDoubleList() {
        return Arrays.asList(toDoubleArray());
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public double get(int index) {
        rangeCheck(index);
        return values[index];
    }


    @Override
    public double set(int index, double element) {
        double oldValue = values[index];
        values[index] = element;
        return oldValue;
    }

    @Override
    public int indexOf(double o) {
        return indexOf(o, 0, size);
    }

    @Override
    public boolean contains(double o) {
        return indexOf(o, 0, size) != -1;
    }

    @Override
    public int indexOf(double o, int from) {
        return indexOf(o, from, size);
    }

    @Override
    public int indexOf(double o, int from, int to) {
        double[] a = this.values;
        int max = Math.min(to, size);
        for (int i = from; i < max; i++) {
            if (o == a[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean contains(double o, int from) {
        return contains(o, from, size);
    }

    @Override
    public boolean contains(double o, int from, int to) {
        return indexOf(o, from, to) != -1;
    }


    @Override
    public int lastIndexOf(double o) {
        return lastIndexOf(o, 0, size);
    }

    @Override
    public int lastIndexOf(double o, int from) {
        return lastIndexOf(o, from, size);
    }

    @Override
    public int lastIndexOf(double o, int from, int to) {
        double[] a = this.values;
        int max = Math.min(to, size);
        for (int i = to - 1; i >= from; i--) {
            if (o == a[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toStringDebug() {
        return "DoubleArrayList{" +
                "values=" + Arrays.toString(Arrays.copyOf(values,size)) +
                ", size=" + size +"/"+values.length+
                '}';
    }

    @Override
    public int committedSize(){
        return values.length;
    }

    @Override
    public String toString() {
        return format("[", ",", "]");
    }

    @Override
    public String format(String first, String sep, String last) {
        StringBuilder sb = new StringBuilder(Math.min(16, first.length() + last.length() + size * (2 + sep.length())));
        sb.append(first);
        if (size > 0) {
            sb.append(values[0]);
        }
        for (int i = 1; i < size; i++) {
            sb.append(sep);
            sb.append(values[i]);
        }
        sb.append(last);
        return sb.toString();
    }

    @Override
    public NDoubleList grow(int minCapacity) {
        if(minCapacity<values.length){
            return this;
        }
        int oldCapacity = values.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        values = Arrays.copyOf(values, newCapacity);
//        System.out.println(">> "+oldCapacity+" -> "+newCapacity);
        return this;
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) { // overflow
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    @Override
    public NDoubleList ensureSize(int size) {
        grow(size);
        if (size > values.length) {
            values = Arrays.copyOf(values, size);
        }
        return this;
    }
}
