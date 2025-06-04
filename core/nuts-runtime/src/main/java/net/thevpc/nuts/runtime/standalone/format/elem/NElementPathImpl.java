package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NPairElement;
import net.thevpc.nuts.util.NToken;
import net.thevpc.nuts.util.NStreamTokenizer;

import java.util.*;

public class NElementPathImpl {
    private List<Item> items = new ArrayList<>();

    public NElementPathImpl(String pattern) {
        NStreamTokenizer tit = new NStreamTokenizer(pattern);
        tit.wordChar('*');
        while (tit.hasNext()) {
            _readItem(tit);
            int t = tit.nextToken();
            if (t != '/' && t != NToken.TT_EOF) {
                throw new IllegalArgumentException("expected '/'");
            }
        }
    }

    private ItemDepth consumeDepth(ItemDepth d) {
        while (!items.isEmpty() && items.get(items.size() - 1) instanceof ItemAny) {
            ItemAny u = (ItemAny) items.remove(items.size() - 1);
            if (u.depth.ordinal() > d.ordinal()) {
                d = u.depth;
            }
        }
        return d;
    }

    private void _readItem(NStreamTokenizer tit) {
        int t = tit.nextToken();
        switch (t) {
            case NToken.TT_EOF: {
                throw new IllegalArgumentException("Expected token");
            }
            case NToken.TT_WORD: {
                if (tit.image.equals("**")) {
                    items.add(new ItemAny(consumeDepth(ItemDepth.ANY)));
                } else if (tit.image.equals("*")) {
                    items.add(new ItemAny(consumeDepth(ItemDepth.CHILD)));
                } else {
                    items.add(new ItemName(tit.image, consumeDepth(ItemDepth.CURR)));
                }
                break;
            }
            case '\'':
            case '\"': {
                items.add(new ItemName(tit.sval, consumeDepth(ItemDepth.CURR)));
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid token");
            }
        }
    }

    private NElement[] searchRecursive(Item i, NElementOrEntry e) {
        LinkedList<NElementOrEntry> queue = new LinkedList<>();
        queue.add(e);
        List<NElement> okkay = new ArrayList<>();
        while (!queue.isEmpty()) {
            NElementOrEntry n = queue.removeFirst();
            if (i.accept(n)) {
                okkay.add(n.value);
            } else {
                queue.addAll(e.children());
            }
        }
        return okkay.toArray(new NElement[0]);
    }

    public NElement[] resolveReversed(NElement... e) {
        List<NElement> all = new ArrayList<>(Arrays.asList(e));
        for (Item item : items) {
            switch (item.type()) {
                case THIS: {
                    break;
                }
                default: {
                    Arrays.stream(e).map(x -> new NElementOrEntry(-1, x)).flatMap(x->{
                        if(item.depth==ItemDepth.CHILD){
                            return x.children().stream().filter(item::accept);
                        }
                        return Arrays.stream(searchRecursive(item, x));
                    }).toArray(NElement[]::new);
                }
            }
        }
        return all.toArray(new NElement[0]);
    }

    public Item[] getItems() {
        return items.toArray(new Item[0]);
    }

    public enum ItemType {
        THIS, NAME, ANY, INDEXED, RANGED_INDEX;
    }

    public static abstract class Item {
        ItemDepth depth;

        public Item(ItemDepth depth) {
            this.depth = depth;
        }

        public abstract ItemType type();

        public abstract boolean accept(NElementOrEntry n) ;
    }

    public enum ItemDepth {
        CURR,
        CHILD,
        ANY
    }

    public static class ItemName extends Item {
        private String name;

        public ItemName(String name, ItemDepth depth) {
            super(depth);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean accept(NElementOrEntry n) {
            return n.key.toString().equals(name);
        }

        @Override
        public ItemType type() {
            return ItemType.NAME;
        }
    }

    public static class ItemStr extends Item {
        private String str;

        public ItemStr(String str, ItemDepth depth) {
            super(depth);
            this.str = str;
        }

        public String getStr() {
            return str;
        }
        @Override
        public boolean accept(NElementOrEntry n) {
            return n.key.toString().equals(str);
        }

        @Override
        public ItemType type() {
            return ItemType.THIS;
        }
    }

    public static class ItemAny extends Item {

        public ItemAny(ItemDepth depth) {
            super(depth);
        }

        @Override
        public boolean accept(NElementOrEntry n) {
            return true;
        }

        @Override
        public ItemType type() {
            return ItemType.ANY;
        }
    }

    public static class NElementOrEntry {
        int index;
        NElement key;
        NElement value;

        public NElementOrEntry(int index, NElement value) {
            this.index = index;
            if(value instanceof NPairElement){
                NPairElement ee=(NPairElement) value;
                this.key = ee.key();
                this.value = ee.value();
            }else{
                this.key = NElement.ofInt(index);
                this.value = value;
            }
        }

        List<NElementOrEntry> children() {
            List<NElementOrEntry> all = new ArrayList<>();
            if (value.isArray() || value.isObject()) {
                int i = 0;
                for (NElement item : value.asListContainer().get().children()) {
                    all.add(new NElementOrEntry(i, item));
                    i++;
                }
            }
            return all;
        }
    }
}
