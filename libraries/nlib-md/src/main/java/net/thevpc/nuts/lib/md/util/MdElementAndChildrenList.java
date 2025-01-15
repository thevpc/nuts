package net.thevpc.nuts.lib.md.util;

import net.thevpc.nuts.lib.md.*;

import java.util.ArrayList;
import java.util.List;

public class MdElementAndChildrenList {
    private Object frontMatter;
    List<MdElementAndChildren> currPath = new ArrayList<>();

    public MdElementAndChildrenList() {
        currPath.add(new MdElementAndChildren(new MdBody(new MdElement[0])));
    }

    public MdElement build() {
        MdElementAndChildren b = currPath.get(0).build();
        ((MdAbstractElement)b.e).setFrontMatter(frontMatter);
        return b.e;
    }

    public MdElementAndChildren last() {
        return currPath.get(currPath.size() - 1);
    }

    public int indexOfParent(MdElement n) {
        for (int i = currPath.size() - 1; i >= 0; i--) {
            if (isChild(n, currPath.get(i).e)) {
                return i;
            }
        }
        return 0;
    }

    public void addAll(List<MdElement> all) {
        for (MdElement e : all) {
            add(e);
        }
    }

    public void add(MdElement n) {
        int i = indexOfParent(n);
        while (i < currPath.size() - 1) {
            currPath.remove(currPath.size() - 1);
        }

        MdElementAndChildren nn = new MdElementAndChildren(n);
        MdElement pp = currPath.get(i).e;
        if (n instanceof MdUnNumberedItem) {
            MdUnNumberedItem ni = (MdUnNumberedItem) n;
            if (pp instanceof MdUnNumberedList && pp.type().depth() == ni.type().depth()) {
                currPath.get(i).addChild(nn);
                currPath.add(nn);
            } else {
                int d = ni.type().depth();
                MdElementAndChildren p0 = new MdElementAndChildren(new MdUnNumberedList(d, new MdUnNumberedItem[0]));
                currPath.get(i).addChild(p0);
                currPath.add(p0);
                p0.addChild(nn);
                currPath.add(nn);
            }
        } else if (n instanceof MdNumberedItem) {
            MdNumberedItem ni = (MdNumberedItem) n;
            if (pp instanceof MdNumberedList && pp.type().depth() == ni.type().depth()) {
                currPath.get(i).addChild(nn);
                currPath.add(nn);
            } else {
                int d = ni.type().depth();
                MdElementAndChildren p0 = new MdElementAndChildren(new MdNumberedList(d, new MdNumberedItem[0]));
                currPath.get(i).addChild(p0);
                currPath.add(p0);
                p0.addChild(nn);
                currPath.add(nn);
            }
        } else {
            currPath.get(i).addChild(nn);
            currPath.add(nn);
        }

    }

    public boolean isChild(MdElement a, MdElement b) {
        MdElementTypeGroup childType = a.type().group();
        int childDepth = a.type().depth();
        MdElementTypeGroup parentType = b.type().group();
        int parentDepth = b.type().depth();
        switch (parentType) {
            case TITLE: {
                switch (childType) {
                    case TITLE:
                        return childDepth > parentDepth;
                    default:
                        return true;
                }
            }
            case NUMBERED_LIST: {
                switch (childType) {
                    case NUMBERED_ITEM: {
                        return childDepth >= parentDepth;
                    }
                    default:
                        return false;
                }
            }
            case UNNUMBERED_LIST: {
                switch (childType) {
                    case UNNUMBERED_ITEM: {
                        return childDepth >= parentDepth;
                    }
                    default:
                        return false;
                }
            }
            case UNNUMBERED_ITEM:{
                switch (childType) {
                    case UNNUMBERED_ITEM: {
                        return childDepth > parentDepth;
                    }
                    case NUMBERED_ITEM: {
                        return childDepth > parentDepth;
                    }
                    default:
                        return false;
                }
            }
            case NUMBERED_ITEM: {
                switch (childType) {
                    case NUMBERED_ITEM: {
                        return childDepth > parentDepth;
                    }
                    case UNNUMBERED_ITEM: {
                        return childDepth > parentDepth;
                    }
                    default:
                        return false;
                }
            }
            case BODY: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public Object getFrontMatter() {
        return frontMatter;
    }

    public void setFrontMatter(Object frontMatter) {
        this.frontMatter = frontMatter;
    }
}
