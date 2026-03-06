package net.thevpc.nuts.util;

public interface NBTreeSet<T extends Comparable<T>> {

    boolean add(T value) ;


    T remove(T value) ;



    public void clear() ;

    public boolean contains(T value) ;



    /**
     * Get the node with value.
     *
     * @param value
     *            to find in the tree.
     * @return T with value.
     */
    T getCurrentValue(T value) ;


    int size();

    boolean validate() ;


    java.util.Collection<T> toCollection();


}