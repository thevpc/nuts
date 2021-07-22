package net.thevpc.nuts.lib.md.util;

import net.thevpc.nuts.lib.md.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MdElementAndChildren {
    MdElement e;
    List<MdElementAndChildren> c = new ArrayList<>();

    public MdElementAndChildren(MdElement e) {
        this.e = e;
    }

    MdElementAndChildren addChild(MdElementAndChildren i) {
        switch (e.type().group()){
            case UNNUMBERED_LIST:{
                if(i.e.type().group()!= MdElementTypeGroup.UNNUMBERED_ITEM){
                    throw new IllegalArgumentException("unexpected");
                }
                break;
            }
            case NUMBERED_LIST:{
                if(i.e.type().group()!= MdElementTypeGroup.NUMBERED_ITEM){
                    throw new IllegalArgumentException("unexpected");
                }
                break;
            }
        }
        c.add(i);
        return this;
    }

    MdElementAndChildren build() {
        if (!c.isEmpty()) {
            switch (e.type().group()) {
                case TITLE: {
                    MdTitle o = e.asTitle();
                    ArrayList<MdElement> c2 = new ArrayList<>(Arrays.asList(o.getChildren()));
                    c2.addAll(c.stream().map(x->x.build().e).collect(Collectors.toList()));
                    e = new MdTitle(
                            o.getCode(),
                            o.getValue(),
                            o.type().depth(),
                            c2.toArray(new MdElement[0])
                    );
                    c.clear();
                    return this;
                }
                case UNNUMBERED_ITEM: {
                    MdUnNumberedItem o = e.asUnNumItem();
                    ArrayList<MdElement> c2 = new ArrayList<>(Arrays.asList(o.getChildren()));
                    c2.addAll(c.stream().map(x->x.build().e).collect(Collectors.toList()));
                    e = new MdUnNumberedItem(
                            o.getType(),
                            o.type().depth(),
                            o.getValue(),
                            c2.toArray(new MdElement[0])
                    );
                    c.clear();
                    return this;
                }
                case NUMBERED_ITEM: {
                    MdNumberedItem o = e.asNumItem();
                    ArrayList<MdElement> c2 = new ArrayList<>(Arrays.asList(o.getChildren()));
                    c2.addAll(c.stream().map(x->x.build().e).collect(Collectors.toList()));
                    e = new MdNumberedItem(
                            o.getNumber(),
                            o.type().depth(),
                            o.getSep(),
                            o.getValue(),
                            c2.toArray(new MdElement[0])
                    );
                    c.clear();
                    return this;
                }
                case UNNUMBERED_LIST: {
                    MdUnNumberedList o = e.asUnNumList();
                    ArrayList<MdElement> c2 = new ArrayList<>(Arrays.asList(o.getChildren()));
                    c2.addAll(c.stream().map(x->x.build().e).collect(Collectors.toList()));
                    e = new MdUnNumberedList(
                            o.type().depth(),
                            c2.toArray(new MdUnNumberedItem[0])
                    );
                    c.clear();
                    return this;
                }
                case NUMBERED_LIST: {
                    MdNumberedList o = e.asNumList();
                    ArrayList<MdElement> c2 = new ArrayList<>(Arrays.asList(o.getChildren()));
                    c2.addAll(c.stream().map(x->x.build().e).collect(Collectors.toList()));
                    e = new MdNumberedList(
                            o.type().depth(),
                            c2.toArray(new MdNumberedItem[0])
                    );
                    c.clear();
                    return this;
                }
                case BODY: {
                    MdBody o = e.asBody();
                    ArrayList<MdElement> c2 = new ArrayList<>(Arrays.asList(o.getChildren()));
                    c2.addAll(c.stream().map(x->x.build().e).collect(Collectors.toList()));
                    e = new MdBody(c2.toArray(new MdElement[0]));
                    c.clear();
                    return this;
                }
                default:{
                    if(c.size()>0){
                        throw new IllegalArgumentException("unexpected");
                    }
                    return this;
                }
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return e +"::"+c;
    }
}
