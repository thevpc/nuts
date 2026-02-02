package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.item.*;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NBoundAffixList {
    private final List<NBoundAffix> affixes = new ArrayList<>();
    private Predicate<NBoundAffix> filter;
    private Comparator<NBoundAffix> orderComparator;


    public Comparator<NBoundAffix> orderComparator() {
        return orderComparator;
    }

    public NBoundAffixList orderComparator(Comparator<NBoundAffix> orderComparator) {
        this.orderComparator = orderComparator;
        return this;
    }

    public Predicate<NBoundAffix> filter() {
        return filter;
    }

    public NBoundAffixList setFilter(Predicate<NBoundAffix> filter) {
        this.filter = filter;
        return this;
    }

    public NBoundAffixList filterAcceptAnchors(NAffixAnchor... anchors) {
        if (anchors == null || anchors.length == 0) {
            this.filter = null;
        } else {
            Set<NAffixAnchor> wl = new HashSet<>(Arrays.asList(anchors));
            for (NAffixAnchor a : anchors) {
                if (a != null) {
                    wl.add(a);
                }
            }
            filter = new Predicate<NBoundAffix>() {
                @Override
                public boolean test(NBoundAffix affix) {
                    return wl.contains(affix.anchor());
                }
            };
        }
        return this;
    }

    public NBoundAffixList filter(Predicate<NBoundAffix> filter) {
        this.filter = filter;
        return this;
    }

    public List<NBoundAffix> list() {
        return new ArrayList<>(affixes);
    }

    public List<NElementComment> comments() {
        return affixes.stream().filter(x ->
                        x.affix().type() == NAffixType.BLOC_COMMENT
                                || x.affix().type() == NAffixType.LINE_COMMENT
                ).map(x -> (NElementComment) x)
                .collect(Collectors.toList());
    }


    public NBoundAffixList clearComments() {
        affixes.removeIf(x ->
                x.affix().type() == NAffixType.BLOC_COMMENT
                        || x.affix().type() == NAffixType.LINE_COMMENT
        );
        return this;
    }


    public List<NElementComment> trailingComments() {
        return affixes.stream().filter(x ->
                        x.anchor() == NAffixAnchor.END && (
                                x.affix().type() == NAffixType.BLOC_COMMENT
                                        || x.affix().type() == NAffixType.LINE_COMMENT
                        )
                ).map(x -> (NElementComment) x)
                .collect(Collectors.toList());
    }


    public List<NElementComment> leadingComments() {
        return affixes.stream().filter(x ->
                        x.anchor() == NAffixAnchor.START && (
                                x.affix().type() == NAffixType.BLOC_COMMENT
                                        || x.affix().type() == NAffixType.LINE_COMMENT
                        )
                ).map(x -> (NElementComment) x)
                .collect(Collectors.toList());
    }


    public NBoundAffixList addAnnotations(List<NElementAnnotation> annotations) {
        return addAffixes(annotations, NAffixAnchor.START);
    }

    public NBoundAffixList addAffixes(List<NBoundAffix> affixes) {
        boolean someAdded = false;
        if (affixes != null) {
            for (NBoundAffix a : affixes) {
                if (a != null) {
                    if (filter == null || filter.test(a)) {
                        this.affixes.add(a);
                        someAdded = true;
                    }
                }
            }
        }
        if (!someAdded) {
            _reorderAffixes();
        }
        return this;
    }

    public NBoundAffixList addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        if (affixes != null) {
            for (NAffix a : affixes) {
                addAffix(a, anchor);
            }
        }
        return this;
    }

    public NBoundAffixList addAffix(NAffix affix, NAffixAnchor anchor) {
        return addAffix(affix, anchor, true);
    }

    public NBoundAffixList addAffix(NBoundAffix affix) {
        if (affix != null) {
            if (filter == null || filter.test(affix)) {
                affixes.add(affix);
                _reorderAffixes();
            }
        }
        return this;
    }

    private NBoundAffixList addAffix(NAffix affix, NAffixAnchor anchor, boolean reorder) {
        if (affix != null) {
            switch (affix.type()) {
                case ANNOTATION: {
                    anchor = NAffixAnchor.START;
                    break;
                }
                case SEPARATOR: {
                    if (anchor == null) {
                        anchor = NAffixAnchor.START;
                    }
                    break;
                }
                default: {
                    if (anchor == null) {
                        anchor = NAffixAnchor.START;
                    }
                }
            }
            DefaultNBoundAffix a = DefaultNBoundAffix.of(affix, anchor);
            if (filter == null || filter.test(a)) {
                affixes.add(a);
                if (reorder) {
                    _reorderAffixes();
                }
            }
        }
        return this;
    }

    private void _reorderAffixes() {
        affixes.sort((o1, o2) -> {
            if (orderComparator != null) {
                int r = orderComparator.compare(o1, o2);
                if (r != 0) {
                    return r;
                }
            }
            return Integer.compare(o1.anchor().ordinal(), o2.anchor().ordinal());
        });
    }

    public NBoundAffixList addAnnotation(String name, NElement... args) {
        return addAnnotation(new NElementAnnotationImpl(name, args == null ? null : Arrays.asList(args), null));
    }

    public NBoundAffixList addAnnotation(NElementAnnotation annotation) {
        return addAffix(annotation, NAffixAnchor.START);
    }

    public NBoundAffixList addAffixNewLine(int index, NNewLineMode newLineMode, NAffixAnchor anchor) {
        NAssert.requireNamedNonNull(anchor, "anchor");
        if (newLineMode != null) {
            addAffix(index, NBoundAffix.of(DefaultNElementNewLine.of(newLineMode), anchor));
        }
        return this;
    }

    public NBoundAffixList addAffix(int index, NBoundAffix affix) {
        if (affix != null) {
            if (filter == null || filter.test(affix)) {
                affixes.add(index, affix);
            }
        }
        return this;
    }

    public NBoundAffixList removeAnnotation(NElementAnnotation annotation) {
        affixes.removeIf(x -> x.affix().equals(annotation));
        return this;
    }

    public NBoundAffixList removeAffix(int index) {
        affixes.remove(index);
        return this;
    }

    public NBoundAffixList clearAnnotations() {
        affixes.removeIf(x -> x.affix().type() == NAffixType.ANNOTATION);
        return this;
    }

    public List<NElementAnnotation> annotations() {
        return affixes.stream()
                .filter(x -> x.affix().type() == NAffixType.ANNOTATION)
                .map(x -> (NElementAnnotation) x.affix()).collect(Collectors.toList());
    }

    public NBoundAffixList setAffix(int index, NBoundAffix affix) {
        if (filter == null || filter.test(affix)) {
            affixes.set(index, affix);
        }
        return this;
    }

    public NBoundAffixList addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        DefaultNBoundAffix a = DefaultNBoundAffix.of(affix, anchor);
        if (filter == null || filter.test(a)) {
            affixes.add(index, a);
        }
        return this;
    }

    public NBoundAffixList setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        DefaultNBoundAffix a = DefaultNBoundAffix.of(affix, anchor);
        if (filter == null || filter.test(a)) {
            affixes.set(index, a);
        }
        return this;
    }

    public NBoundAffixList removeAffixes(NAffixType type, NAffixAnchor anchor) {
        affixes.removeIf(x ->
                (type == null || x.affix().type() == type)
                        && (anchor == null || x.anchor() == anchor)
        );
        return this;
    }

    public NBoundAffixList addLeadingComment(NElementComment comment) {
        return addAffix(comment, NAffixAnchor.START);
    }

    public NBoundAffixList addLeadingComments(NElementComment... comments) {
        if (comments == null) {
            return this;
        }
        return addAffixes(Arrays.asList(comments), NAffixAnchor.START);
    }

    public NBoundAffixList addTrailingComment(NElementComment comment) {
        return addAffix(comment, NAffixAnchor.END);
    }

    public NBoundAffixList addTrailingComments(NElementComment... comments) {
        if (comments == null) {
            return this;
        }
        return addAffixes(Arrays.asList(comments), NAffixAnchor.END);
    }

    public NBoundAffixList addAffixSpace(String space, NAffixAnchor anchor) {
        NAssert.requireNamedNonNull(anchor, "anchor");
        if (!NStringUtils.isEmpty(space)) {
            addAffix(NBoundAffix.of(DefaultNElementSpace.of(space), anchor));
        }
        return this;
    }

    public NBoundAffixList addAffixNewLine(NNewLineMode newLineMode, NAffixAnchor anchor) {
        NAssert.requireNamedNonNull(anchor, "anchor");
        if (newLineMode != null) {
            addAffix(NBoundAffix.of(DefaultNElementNewLine.of(newLineMode), anchor));
        }
        return this;
    }

    public NBoundAffixList addAffixSeparator(String separator, NAffixAnchor anchor) {
        NAssert.requireNamedNonNull(anchor, "anchor");
        if (!NStringUtils.isEmpty(separator)) {
            addAffix(NBoundAffix.of(DefaultNElementSeparator.of(separator), anchor));
        }
        return this;
    }

    public NBoundAffixList addAffixSpace(int index, String space, NAffixAnchor anchor) {
        NAssert.requireNamedNonNull(anchor, "anchor");
        if (!NStringUtils.isEmpty(space)) {
            addAffix(index, NBoundAffix.of(DefaultNElementSpace.of(space), anchor));
        }
        return this;
    }

    public NBoundAffixList addAffixSeparator(int index, String separator, NAffixAnchor anchor) {
        NAssert.requireNamedNonNull(anchor, "anchor");
        if (!NStringUtils.isEmpty(separator)) {
            addAffix(index, NBoundAffix.of(DefaultNElementSeparator.of(separator), anchor));
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NBoundAffixList that = (NBoundAffixList) o;
        return Objects.equals(affixes, that.affixes) && Objects.equals(filter, that.filter) && Objects.equals(orderComparator, that.orderComparator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(affixes, filter, orderComparator);
    }

    public static List<NBoundAffix> filter(List<NBoundAffix> list, NAffixAnchor anchor, NAffixType... acceptableTypes) {
        Set<NAffixType> at = new HashSet<>(Arrays.asList(acceptableTypes));
        return list.stream().filter(x ->
                x.anchor() == anchor
                        && at.contains(x.affix().type())
        ).collect(Collectors.toList());
    }

    public void removeAffixIf(Predicate<NBoundAffix> predicate) {
        NAssert.requireNamedNonNull(predicate, "predicate");
        affixes.removeIf(predicate);
    }
}
