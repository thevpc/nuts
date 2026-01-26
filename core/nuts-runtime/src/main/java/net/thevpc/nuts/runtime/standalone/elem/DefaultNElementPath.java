package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.NElementPath;
import net.thevpc.nuts.util.NAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultNElementPath implements NElementPath {
    private List<Item> items;

    public static NElementPath ROOT = new DefaultNElementPath();

    public DefaultNElementPath() {
        items = new ArrayList<>();
    }

    public DefaultNElementPath(List<Item> items) {
        this.items = items;
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public NElementPath child(String name) {
        ArrayList<Item> u = new ArrayList<>(items);
        u.add(new Item(null, name));
        return new DefaultNElementPath(u);
    }

    public NElementPath child(int name) {
        return child(String.valueOf(name + 1));
    }

    public NElementPath param(int name) {
        return param(String.valueOf(name + 1));
    }

    public NElementPath ann(int name) {
        return ann(String.valueOf(name + 1));
    }

    @Override
    public NElementPath param(String name) {
        ArrayList<Item> u = new ArrayList<>(items);
        u.add(new Item("param", name));
        return new DefaultNElementPath(u);
    }

    @Override
    public NElementPath ann(String name) {
        ArrayList<Item> u = new ArrayList<>(items);
        u.add(new Item("ann", name));
        return new DefaultNElementPath(u);
    }

    @Override
    public NElementPath group(String group, int index) {
        return group(group,String.valueOf(index + 1));
    }

    @Override
    public NElementPath group(String group, String name) {
        NAssert.requireNonBlank(group,"group");
        ArrayList<Item> u = new ArrayList<>(items);
        u.add(new Item(group, name));
        return new DefaultNElementPath(u);
    }

    @Override
    public NElementPath parent() {
        if (items.isEmpty()) {
            return null;
        }
        return new DefaultNElementPath(
                new ArrayList<>(items.subList(0, items.size() - 1))
        );
    }

    @Override
    public boolean isRoot() {
        return size() == 0;
    }

    @Override
    public String toString() {
        return "/" + items.stream().map(x -> x.toString()).collect(Collectors.joining("/"));
    }

    static class Item {
        private String mode;
        private String name;

        public Item(String mode, String name) {
            this.mode = mode;
            this.name = name;
        }

        @Override
        public String toString() {
            if (mode == null) {
                return name;
            }
            return mode + "[" + name + "]";
        }
    }
}
