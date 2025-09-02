package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NTextListSimplifier {
    private List<NText> values=new ArrayList<>();
    private boolean inlineLists = true;
    private boolean inlineBuilders = true;
    private boolean mergePlain = true;
    private boolean mergeStyled = true;

    public boolean isInlineLists() {
        return inlineLists;
    }

    public NTextListSimplifier setInlineLists(boolean inlineLists) {
        this.inlineLists = inlineLists;
        return this;
    }

    public boolean isInlineBuilders() {
        return inlineBuilders;
    }

    public NTextListSimplifier setInlineBuilders(boolean inlineBuilders) {
        this.inlineBuilders = inlineBuilders;
        return this;
    }

    public boolean isMergePlain() {
        return mergePlain;
    }

    public NTextListSimplifier setMergePlain(boolean mergePlain) {
        this.mergePlain = mergePlain;
        return this;
    }

    public boolean isMergeStyled() {
        return mergeStyled;
    }

    public NTextListSimplifier setMergeStyled(boolean mergeStyled) {
        this.mergeStyled = mergeStyled;
        return this;
    }

    public NTextListSimplifier addAll(NText[] a) {
        for (NText n : a) {
            add(n);
        }
        return this;
    }

    public NTextListSimplifier addAll(List<NText> a) {
        for (NText n : a) {
            add(n);
        }
        return this;
    }

    public NTextListSimplifier add(NText a) {
        if (a != null) {
            if (inlineLists && a instanceof NTextList) {
                for (NText child : ((NTextList) a).getChildren()) {
                    add(child.simplify());
                }
            } else if (inlineBuilders && a instanceof NTextBuilder) {
                for (NText child : ((NTextBuilder) a).getChildren()) {
                    add(child.simplify());
                }
            } else {
                NText last = null;
                if (!values.isEmpty()) {
                    last = values.get(values.size() - 1);
                }
                if (a instanceof NTextPlain) {
                    NTextPlain aa = (NTextPlain) a;
                    if (!((NTextPlain) a).getValue().isEmpty()) {
                        if (mergePlain && last instanceof NTextPlain) {
                            values.remove(values.size() - 1);
                            values.add(new DefaultNTextPlain(
                                    ((NTextPlain) last).getValue() +
                                            aa.getValue())
                            );
                        } else {
                            values.add(a);
                        }
                    }
                } else if (a instanceof NTextStyled) {
                    NTextStyled aa = (NTextStyled) a;
                    NTextStyles aas = aa.getStyles();
                    if (mergeStyled && last instanceof NTextStyled && Objects.equals(aa.getStyles(), ((NTextStyled) last).getStyles())) {
                        values.remove(values.size() - 1);
                        NText s = NText.ofList(((NTextStyled) last).getChild(), aa.getChild()).simplify();
                        if (s instanceof NTextPlain && ((NTextPlain) s).getValue().isEmpty()) {
                            //just ignore
                        } else {
                            values.add(new DefaultNTextStyled(s, aas));
                        }
                    } else {
                        values.add(a);
                    }
                }else{
                    values.add(a);
                }
            }
        }
        return this;
    }

    public List<NText> toList() {
        return new ArrayList<>(values);
    }
}
