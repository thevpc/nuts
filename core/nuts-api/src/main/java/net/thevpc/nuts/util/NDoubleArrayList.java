package net.thevpc.nuts.util;

import java.util.Arrays;
import java.util.List;

public class NDoubleArrayList {
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

    public void add(int index,double value){
        ensureSize(size + 1);  // Increments modCount!!
        System.arraycopy(values, index, values, index + 1,
                size - index);
        values[index] = value;
        size++;
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

    public void addAll(NDoubleArrayList values) {
        int toAddLength = values.size;
        ensureSize(size + toAddLength);
        System.arraycopy(values.values, 0, this.values, size, toAddLength);
        this.size += toAddLength;
    }

    public void addAll(double... values) {
        int toAddLength = values.length;
        ensureSize(size + toAddLength);
        System.arraycopy(values, 0, this.values, size, toAddLength);
        this.size += toAddLength;
    }

    public void insertAll(int offset, double... values) {
        if(offset<0){
            throw new IllegalArgumentException("offset should be >=0");
        }
        int toAddLength = values.length;
        ensureSize(size + toAddLength);
        System.arraycopy(this.values, offset, this.values, offset + values.length, size - offset);
        System.arraycopy(values, 0, this.values, offset, toAddLength);
        this.size += toAddLength;
    }

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

    public void replaceSubList(int offset, int count, double... replacement) {
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
    }

    public void add(double value){
        ensureSize(size+1);
        values[size++]=value;
    }

    public void trimToSize(){
        if(size<values.length){
            values= Arrays.copyOf(values,size);
        }
    }

    public NDoubleArrayList copy() {
        return new NDoubleArrayList(Arrays.copyOf(values, values.length),size);
    }

    public double[] toArray(){
        return Arrays.copyOf(values,size);
    }

    public NLongArrayList toLongCastArrayList() {
        long[] r = new long[size];
        for (int i = 0; i < size; i++) {
            r[i] = (long)values[i];
        }
        return new NLongArrayList(r, r.length);
    }

    public Double[] toDoubleArray(){
        Double[] all=new Double[size];
        for (int i = 0; i < size; i++) {
            all[i]=values[i];
        }
        return all;
    }

    public List<Double> toDoubleList() {
        return Arrays.asList(toDoubleArray());
    }

    public int size() {
        return size;
    }

    public double get(int index) {
        rangeCheck(index);
        return values[index];
    }


    public double set(int index, double element) {
        double oldValue = values[index];
        values[index] = element;
        return oldValue;
    }

    public int indexOf(double o) {
        return indexOf(o, 0, size);
    }

    public boolean contains(double o) {
        return indexOf(o, 0, size) != -1;
    }

    public int indexOf(double o, int from) {
        return indexOf(o, from, size);
    }

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

    public boolean contains(double o, int from) {
        return contains(o, from, size);
    }

    public boolean contains(double o, int from, int to) {
        return indexOf(o, from, to) != -1;
    }


    public int lastIndexOf(double o) {
        return lastIndexOf(o, 0, size);
    }

    public int lastIndexOf(double o, int from) {
        return lastIndexOf(o, from, size);
    }

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

    public String toStringDebug() {
        return "DoubleArrayList{" +
                "values=" + Arrays.toString(Arrays.copyOf(values,size)) +
                ", size=" + size +"/"+values.length+
                '}';
    }

    public int getCommittedSize(){
        return values.length;
    }

    @Override
    public String toString() {
        return format("[", ",", "]");
    }

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

    public void grow(int minCapacity) {
        if(minCapacity<values.length){
            return;
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
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) { // overflow
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    public void ensureSize(int size) {
        grow(size);
        if (size > values.length) {
            values = Arrays.copyOf(values, size);
        }
    }
}
