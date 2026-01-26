package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNListElement;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DefaultNListElementBuilder extends AbstractNElementBuilder implements NListElementBuilder {
    private NElementType type;
    private int depth;
    private List<NListItemElement> items = new ArrayList<>();

    public DefaultNListElementBuilder(NElementType type, int depth) {
        this.type = type;
        this.depth = depth;
    }

    @Override
    public NListElementBuilder doWith(Consumer<NListElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }

    public NListElementBuilder addItem(NListItemElement item) {
        if (item != null) {
            this.items.add(item);
        }
        return this;
    }

    public NListElementBuilder addItems(NListItemElement[] items) {
        if (items != null) {
            for (NListItemElement item : items) {
                if (item != null) {
                    this.items.add(item);
                }
            }
        }
        return this;
    }

    public NListElement build() {
        List<NListItemElement> builtItems = new ArrayList<>();
        for (NListItemElement item : items) {
            builtItems.add(item);
        }
        return new DefaultNListElement(
                type == null ? NElementType.UNORDERED_LIST : type,
                depth <= 0 ? 1 : depth, items,
                affixes(),
                diagnostics()
        );
    }

    // For internal stack use
    public int depth() {
        return depth;
    }

    public NElementType type() {
        return type;
    }

    @Override
    public NListItemElement get(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public List<NListItemElement> items() {
        return new ArrayList<>(items);
    }

    @Override
    public NListElementBuilder setItemAt(int index, NListItemElement other) {
        if (other != null) {
            items.set(index, other);
        }
        return this;
    }

// ------------------------------------------

    @Override
    public NListElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NListElementBuilder) {
            NListElementBuilder b = (NListElementBuilder) other;
            this.type = b.type();
            this.depth = b.depth();
            this.items.addAll(b.items());
        }
        return this;
    }

    @Override
    public NListElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NListElement) {
            NListElement b = (NListElement) other;
            this.type = b.type();
            this.depth = b.depth();
            this.items.addAll(b.items());
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NListElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NListElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NListElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NListElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    @Override
    public NListElementBuilder removeAffix(int index) {
        super.removeAffix(index);
        return this;
    }

    @Override
    public NListElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NListElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NListElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    public NListElementBuilder addAffix(NBoundAffix affix) {
        super.addAffix(affix);
        return this;
    }


    @Override
    public NListElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NListElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NListElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    @Override
    public NListElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NListElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NListElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NListElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NListElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NListElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NListElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NListElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NListElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NListElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NListElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        if (other instanceof NListElementBuilder) {
            NListElementBuilder olist = (NListElementBuilder) other;
            depth = olist.depth();
            type = olist.type();
            items.addAll(olist.items());
        }
        return this;
    }

    @Override
    public NListElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        if (other instanceof NListElement) {
            NListElement olist = (NListElement) other;
            depth = olist.depth();
            type = olist.type();
            items.addAll(olist.items());
        }
        return this;
    }
}
