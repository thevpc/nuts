package net.thevpc.nuts.runtime.standalone.util.collections;

import java.util.*;

public class BTreeMap<K extends Comparable<K>,V> {
    private BTreeSet<BNodeEntry<K,V>> bt;

    public BTreeMap(int order) {
        this.bt = new BTreeSet<>(order);
    }

    public boolean put(K key, V value) {
        return this.bt.add(new BNodeEntry<>(
                key,value
        ));
    }

    public int size() {
        return bt.size();
    }

    public Set<K> keySet() {
        return new AbstractSet<K>() {
            @Override
            public Iterator<K> iterator() {
                Iterator<BNodeEntry<K, V>> iterator = bt.toCollection().iterator();
                return new Iterator<K>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public K next() {
                        return iterator.next().key;
                    }
                };
            }

            @Override
            public boolean contains(Object o) {
                K e=(K) o;
                return bt.contains(new BNodeEntry<>(e,null));
            }

            @Override
            public boolean isEmpty() {
                return BTreeMap.this.isEmpty();
            }

            @Override
            public int size() {
                return BTreeMap.this.size();
            }
        };
    }

    public Collection<V> values() {
        return new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
                Iterator<BNodeEntry<K, V>> iterator = bt.toCollection().iterator();
                return new Iterator<V>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public V next() {
                        return iterator.next().value;
                    }
                };
            }

            @Override
            public boolean isEmpty() {
                return BTreeMap.this.isEmpty();
            }

            @Override
            public int size() {
                return BTreeMap.this.size();
            }
        };
    }

    public Set<Map.Entry<K,V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                Iterator<BNodeEntry<K, V>> iterator = bt.toCollection().iterator();
                return new Iterator<Map.Entry<K, V>>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Map.Entry<K, V> next() {
                        return new BNodeEntry2<>(iterator.next());
                    }
                };
            }

            @Override
            public boolean contains(Object o) {
                Map.Entry<K, V> e=(Map.Entry<K, V>) o;
                BNodeEntry<K, V> u = bt.getCurrentValue(new BNodeEntry<>(e.getKey(),null));
                if(u==null){
                    return false;
                }
                return Objects.equals(e.getValue(),u.getValue());
            }

            @Override
            public boolean isEmpty() {
                return BTreeMap.this.isEmpty();
            }

            @Override
            public int size() {
                return BTreeMap.this.size();
            }
        };
    }

    public boolean isEmpty() {
        return size()==0;
    }

    public boolean contains(K key) {
        return bt.contains(new BNodeEntry<>(key, null));
    }

    public V get(K key) {
        BNodeEntry<K, V> f = bt.getCurrentValue(new BNodeEntry<>(key, null));
        return f==null?null:f.getValue();
    }

    public V remove(K key, V value) {
        BNodeEntry<K, V> old = this.bt.remove(new BNodeEntry<>(key, null));
        return old==null?null:old.getValue();
    }


    private static class BNodeEntry2<K extends Comparable<K>,V> implements Map.Entry<K,V>{
        private BNodeEntry<K,V> v;
        public BNodeEntry2(BNodeEntry<K, V> v) {
            this.v = v;
        }

        @Override
        public K getKey() {
            return v.key;
        }

        @Override
        public V getValue() {
            return v.value;
        }

        @Override
        public V setValue(V value) {
            return v.setValue(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BNodeEntry2<?, ?> that = (BNodeEntry2<?, ?>) o;
            return
                    Objects.equals(v.key, that.v.key)
                    && Objects.equals(v.value, that.v.value)
                    ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(v.key,v.value);
        }

        @Override
        public String toString() {
            return "Entry(" +
                    v.key +','+v.value+
                    ')';
        }
    }

    private static class BNodeEntry<K extends Comparable<K>,V> implements Comparable<BNodeEntry<K,V>>
    {
        private K key;
        private V value;

        public BNodeEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public int compareTo(BNodeEntry<K,V> o) {
            return key.compareTo(o.getKey());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BNodeEntry<?, ?> entry = (BNodeEntry<?, ?>) o;
            return Objects.equals(key, entry.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        public V setValue(V value) {
            V old=this.value;
            this.value=value;
            return old;
        }
    }
}
