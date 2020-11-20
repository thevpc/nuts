package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.NutsArrayElement;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsObjectElement;
import net.thevpc.nuts.NutsPrimitiveElement;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ItemFactory {
    public static Item itemOf(Object o) {
        if (o == null) {
            return new Literal(null);
        }
        if (o instanceof String) {
            return new Literal(o);
        }
        if (o instanceof Number) {
            return new Literal(o);
        }
        if (o instanceof Boolean) {
            return new Literal(o);
        }
        if (o instanceof List) {
            return new Arr(o);
        }
        if (o instanceof Map) {
            return new Obj(o);
        }
        if (o instanceof NutsElement) {
            if (o instanceof NutsObjectElement) {
                return new Obj(o);
            }
            if (o instanceof NutsArrayElement) {
                return new Arr(o);
            }
            if (o instanceof NutsPrimitiveElement) {
                return new Literal(((NutsPrimitiveElement) o).getValue());
            }
        }
        throw new IllegalArgumentException("Invalid");
    }

    public interface Item {

        boolean isLiteral();

        Literal asLiteral();

        boolean isArray();

        Arr asArray();

        boolean isObject();

        Obj asObject();

        boolean isString();

        boolean isNull();

        boolean isNumber();

        boolean isBoolean();

        String asString();

        boolean asBoolean();

        Number asNumber();
    }

    public static class AbstractItem implements Item {
        protected Object value;

        public AbstractItem(Object value) {
            this.value = value;
        }

        @Override
        public boolean isLiteral() {
            return this instanceof Literal;
        }

        @Override
        public Literal asLiteral() {
            return (Literal) this;
        }

        @Override
        public boolean isArray() {
            return this instanceof Arr;
        }

        @Override
        public boolean isObject() {
            return this instanceof Obj;
        }

        @Override
        public Arr asArray() {
            return (Arr) this;
        }

        @Override
        public Obj asObject() {
            return (Obj) this;
        }

        @Override
        public boolean isString() {
            return false;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public boolean isNumber() {
            return false;
        }

        @Override
        public boolean isBoolean() {
            return false;
        }

        @Override
        public String asString() {
            throw new IllegalArgumentException("Not a string");
        }

        @Override
        public boolean asBoolean() {
            throw new IllegalArgumentException("Not a boolean");
        }

        @Override
        public Number asNumber() {
            throw new IllegalArgumentException("Not a number");
        }
    }

    public static class Arr extends AbstractItem implements Iterable<Item> {
        public Arr(Object o) {
            super(o);
        }

        public boolean isEmpty() {
            return length()==0;
        }
        public int length() {
            if(value instanceof NutsArrayElement){
                return ((NutsArrayElement) value).size();
            }
            return ((List) value).size();
        }

        public Item get(int i) {
            if(value instanceof NutsArrayElement){
                return itemOf(((NutsArrayElement) value).get(i));
            }
            return itemOf(((List) value).get(i));
        }

        public Stream<Item> stream() {
            if(value instanceof NutsArrayElement){
                return ((NutsArrayElement) value).children().stream().map(x->itemOf(x));
            }
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED),
                    false);
        }

        public Iterator<Item> iterator() {
            return new Iterator<Item>() {
                int i = 0;

                @Override
                public boolean hasNext() {
                    return i < length();
                }

                @Override
                public Item next() {
                    i++;
                    return get(i - 1);
                }
            };
        }
    }

    public static class Literal extends AbstractItem {
        public Literal(Object o) {
            super(o);
        }
        @Override
        public Arr asArray() {
            if(value==null){
                return new Arr(new ArrayList<>());
            }
            return new Arr(new ArrayList<>(Arrays.asList(value)));
        }

        @Override
        public Obj asObject() {
            if(value==null){
                return new Obj(new HashMap<>());
            }
            HashMap<Object, Object> m = new HashMap<>();
            m.put("value",value);
            return new Obj(m);
        }

        public boolean isString() {
            return value != null && value instanceof String;
        }

        public boolean isNull() {
            return value == null;
        }

        public boolean isNumber() {
            return value != null && value instanceof Number;
        }

        public boolean isBoolean() {
            return value != null && value instanceof Boolean;
        }

        public String asString() {
            return value == null ? "" : value.toString();
        }

        public boolean asBoolean() {
            return value == null ? false : (Boolean) value;
        }

        public Number asNumber() {
            if (value == null) {
                return null;
            }
            if (value instanceof Number) {
                return (Number) value;
            }
            if (value instanceof String) {
                String s = (String) value;
                return Double.parseDouble(s);
            }
            throw new IllegalArgumentException("Not a number");
        }

    }

    public static class Obj extends AbstractItem implements Iterable<Map.Entry<String, Item>> {
        public Obj(Object o) {
            super(o);
        }

        public int length() {
            if(value instanceof NutsObjectElement){
                return ((NutsObjectElement) value).size();
            }
            return ((Map) value).size();
        }

        public Obj getObject(String i) {
            return get(i).asObject();
        }

        public Arr getArray(String i) {
            return get(i).asArray();
        }

        public String getString(String i) {
            return get(i).asString();
        }

        public boolean getBoolean(String i) {
            Item r = get(i);
            if(r.isNull()){
                return false;
            }
            return r.asBoolean();
        }

        public Item get(String i) {
            Object oo =null;
            if(value instanceof NutsObjectElement){
                oo=((NutsObjectElement) value).get(i);
            }else{
                oo=((Map) value).get(i);
            }
            return itemOf(oo);
        }

        public Stream<Map.Entry<String, Item>> stream() {
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED),
                    false);
        }

        @Override
        public Iterator<Map.Entry<String, Item>> iterator() {
            if(value instanceof NutsObjectElement){
                return ((NutsObjectElement) value).children().stream()
                        .map(x -> (Map.Entry<String, Item>) new StringItemEntry(x.getName(),itemOf(x.getValue()))).iterator();
            }
            return ((Map<String, Object>) value).entrySet().stream().map(x -> (Map.Entry<String, Item>) new StringItemEntry(x.getKey(),itemOf(x.getValue()))).iterator();
        }

        public boolean isEmpty() {
            return length()==0;
        }

        private class StringItemEntry implements Map.Entry<String, Item> {
            private final String k;
            private final Item v;

            public StringItemEntry(String k,Item v) {
                this.k = k;
                this.v = v;
            }

            @Override
            public String getKey() {
                return k;
            }

            @Override
            public Item getValue() {
                return v;
            }

            @Override
            public Item setValue(Item value) {
                return null;
            }
        }
    }
}
