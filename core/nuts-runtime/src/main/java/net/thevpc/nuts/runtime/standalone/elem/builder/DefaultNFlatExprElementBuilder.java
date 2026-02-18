package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.CoreNElementUtils;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNFlatExprElement;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DefaultNFlatExprElementBuilder extends AbstractNElementBuilder implements NFlatExprElementBuilder {
    private List<NElement> values = new ArrayList<>();

    public DefaultNFlatExprElementBuilder() {
    }

    @Override
    public List<NElement> children() {
        return new ArrayList<>(values);
    }

    @Override
    public NFlatExprElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NOptional<NElement> get(int index) {
        if (index >= 0 && index < values.size()) {
            return NOptional.of(values.get(index));
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("index %s", index));
    }

    @Override
    public NFlatExprElementBuilder add(NOperatorSymbol op) {
        if (op != null) {
            add(NElement.ofOperatorSymbol(op));
        }
        return this;
    }

    @Override
    public NFlatExprElementBuilder add(NElement element) {
        CoreNElementUtils.add(element, values);
        return this;
    }

    @Override
    public NFlatExprElementBuilder setChildren(List<NElement> elements) {
        CoreNElementUtils.setAll(elements, values);
        return this;
    }

    @Override
    public NFlatExprElementBuilder setAt(int index, NElement element) {
        CoreNElementUtils.setAt(index, element, values);
        return this;
    }

    @Override
    public NFlatExprElementBuilder setAt(int index, NOperatorSymbol element) {
        CoreNElementUtils.setAt(index, element == null ? null : NElement.ofOperatorSymbol(element), values);
        return this;
    }

    @Override
    public NFlatExprElement build() {
        List<Object> ok = new ArrayList<>();
        for (NElement nElement : children()) {
            if (ok.isEmpty()) {
                ok.add(nElement);
            } else if (nElement.isOperatorSymbol()) {
                ok.add(nElement);
            } else {
                int mi = ok.size() - 1;
                Object last = ok.get(mi);
                if (last instanceof NOperatorSymbolElement) {
                    ok.add(nElement);
                } else if (last instanceof List) {
                    ((List) last).add(nElement);
                } else {
                    ArrayList<Object> a = new ArrayList<>();
                    a.add(ok.remove(mi));
                    a.add(nElement);
                    ok.add(a);
                }
            }
        }
        List<NElement> elems = new ArrayList<>();
        for (Object o : ok) {
            if (o instanceof NElement) {
                elems.add((NElement) o);
            } else {
                List<NElement> li = (List) o;
                elems.add(NElement.ofUplet(li.toArray(new NElement[0])));
            }
        }
        return new DefaultNFlatExprElement(elems,
                affixes(), diagnostics(), metadata()
        );
    }

    @Override
    public NElementType type() {
        return NElementType.FLAT_EXPR;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NFlatExprElementBuilder clearChildren() {
        values.clear();
        return this;
    }

    @Override
    public NFlatExprElementBuilder doWith(Consumer<NFlatExprElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }

    // ------------------------------------------

    @Override
    public NFlatExprElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NFlatExprElementBuilder) {
            NFlatExprElementBuilder b = (NFlatExprElementBuilder) other;
            this.values.addAll(b.children());
        }
        return this;
    }

    @Override
    public NFlatExprElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NFlatExprElement) {
            NFlatExprElement b = (NFlatExprElement) other;
            this.values.addAll(b.children());
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NFlatExprElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    @Override
    public NFlatExprElementBuilder removeAffix(int index) {
        super.removeAffix(index);
        return this;
    }

    @Override
    public NFlatExprElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NFlatExprElementBuilder clearAffixes() {
        super.clearAffixes();
        return this;
    }

    @Override
    public NFlatExprElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NFlatExprElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    public NFlatExprElementBuilder addAffix(NBoundAffix affix) {
        super.addAffix(affix);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NFlatExprElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NFlatExprElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    public NFlatExprElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NFlatExprElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NFlatExprElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NFlatExprElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NFlatExprElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NFlatExprElementBuilder metadata(NElementMetadata metadata) {
        super.metadata(metadata);
        return this;
    }
}
