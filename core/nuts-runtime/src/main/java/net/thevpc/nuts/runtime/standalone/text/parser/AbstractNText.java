package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.pipeline.NStream;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextRPI;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextTransformerContext;
import net.thevpc.nuts.runtime.standalone.text.NTextNodeWriterStringer;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NRef;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractNText implements NText {

    public AbstractNText() {
    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NTextNodeWriterStringer ss = new NTextNodeWriterStringer(out);
        ss.writeNode(this);
        return out.toString();
    }

    @Override
    public String filteredText() {
        return immutable().filteredText();
    }

    @Override
    public int length() {
        return immutable().length();
    }

    @Override
    public boolean isEmpty() {
        return immutable().isEmpty();
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(filteredText());
    }

    @Override
    public NTextBuilder builder() {
        return NTextBuilder.of().append(this);
    }

    @Override
    public boolean isPrimitive() {
        return this instanceof NPrimitiveText;
    }

    @Override
    public boolean isNormalized() {
        return this instanceof NNormalizedText;
    }

    @Override
    public List<NText> split(char c) {
        return split(String.valueOf(c), false);
    }

    @Override
    public List<NText> split(char c, boolean returnSeparator) {
        return split(String.valueOf(c), returnSeparator);
    }

    @Override
    public List<NText> split(String separator) {
        return split(separator, false);
    }

    @Override
    public NPrimitiveText[] toCharArray() {
        return toCharList().toArray(new NPrimitiveText[0]);
    }

    @Override
    public List<NPrimitiveText> toCharList() {
        return toCharStream().collect(Collectors.toList());
    }

    @Override
    public NText repeat(int times) {
        if (times <= 0) {
            return NText.ofBlank();
        }
        if (times == 1) {
            return this;
        }
        NTextBuilder b = NTextBuilder.of();
        for (int i = 0; i < times; i++) {
            b.append(this);
        }
        return b.build();
    }

    @Override
    public NText repeatln(int times) {
        if (times <= 0) {
            return NText.ofBlank();
        }
        if (times == 1) {
            return this;
        }
        NTextBuilder b = NTextBuilder.of();
        for (int i = 0; i < times; i++) {
            if (i > 0) {
                b.append(NText.ofNewLine());
            }
            b.append(this);
        }
        return b.build();
    }

    @Override
    public NText concat(NText other) {
        if (other == null) {
            return this;
        }
        return NText.ofList(this, other).simplify();
    }

    @Override
    public NText concat(NText... others) {
        List<NText> aa = new ArrayList<>();
        aa.add(this);
        if (others != null) {
            aa.addAll(Arrays.asList(others));
        }
        return NText.ofList(aa).simplify();
    }

    public NNormalizedText normalize() {
        return normalize(null,null);
    }

    @Override
    public NNormalizedText normalize(NTextTransformConfig config) {
        return normalize(null, config);
    }

    @Override
    public NNormalizedText normalize(NTextTransformer transformer, NTextTransformConfig config) {
        List<NNormalizedText> li = normalizeStream(transformer, config).toList();
        if (li.isEmpty()) {
            return (NNormalizedText) NText.ofBlank();
        }
        if (li.size() == 1) {
            return li.get(0);
        }
        return NText.ofList(li.toArray(new NNormalizedText[0]));
    }

    @Override
    public boolean isString(String anyString) {
        return anyString != null && anyString.equals(filteredText());
    }

    @Override
    public boolean isNewLine() {
        Iterator<NPrimitiveText> it = toCharStream().iterator();
        if (!it.hasNext()) {
            return false;
        }
        String f = it.next().filteredText();
        if (f.equals("\n")) {
            return !it.hasNext();
        }
        if (!f.equals("\r")) {
            return false;
        }
        if (!it.hasNext()) {
            return true;
        }
        if (it.next().filteredText().equals("\n")) {
            return !it.hasNext();
        }
        return false;
    }

    public List<NPrimitiveText> toPrimitiveList() {
        NNormalizedText normalizeOne = normalize();
        List<NPrimitiveText> primitiveList = new ArrayList<>();
        if (normalizeOne instanceof NPrimitiveText || normalizeOne instanceof NTextPlain) {
            primitiveList.add((NPrimitiveText) normalizeOne);
        } else {
            primitiveList.addAll(((NTextList) normalizeOne).children().stream().map(x -> (NPrimitiveText) x).collect(Collectors.toList()));
        }
        return primitiveList;
    }

    public List<NText> split(Pattern regex, boolean returnSeparator) {
        List<NPrimitiveText> normalizedList = toPrimitiveList();
        // step 1: build offset map
        List<int[]> offsets = new ArrayList<>();
        List<NTextStyles> styles = new ArrayList<>();
        StringBuilder full = new StringBuilder();
        for (NText child : normalizedList) {
            int start = full.length();
            String t = child.filteredText();
            full.append(t);
            offsets.add(new int[]{start, full.length()});
            if (child instanceof NTextStyled) {
                styles.add(((NTextStyled) child).styles());
            } else {
                styles.add(null);
            }
        }

        // step 2: split the full string
        String s = full.toString();
        Matcher m = regex.matcher(s);

        List<NText> result = new ArrayList<>();
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                result.add(slice(s, styles, offsets, last, m.start()));
            } else {
                result.add(NText.ofPlain(""));
            }
            if (returnSeparator) {
                result.add(slice(s, styles, offsets, m.start(), m.end()));
            }
            last = m.end();
        }
        // remainder
        if (last < s.length()) {
            result.add(slice(s, styles, offsets, last, s.length()));
        } else if (last == s.length() && last > 0 && !returnSeparator) {
            // trailing empty — drop it
        }

        return result;
    }

    // reconstruct a styled NText from a substring range [from, to)
    private NText slice(String full, List<NTextStyles> styles, List<int[]> offsets, int from, int to) {
        NTextBuilder tb = NTextBuilder.of();
        for (int i = 0; i < offsets.size(); i++) {
            int cStart = offsets.get(i)[0];
            int cEnd = offsets.get(i)[1];
            int overlapStart = Math.max(from, cStart);
            int overlapEnd = Math.min(to, cEnd);
            if (overlapStart < overlapEnd) {
                NTextStyles si = styles.get(i);
                if (si == null) {
                    tb.append(full.substring(overlapStart, overlapEnd));
                } else {
                    tb.append(full.substring(overlapStart, overlapEnd), si);
                }
            }
        }
        return tb.build();
    }

    public List<NText> split(String separator, boolean returnSeparator) {
        return split(Pattern.compile(Pattern.quote(separator)), returnSeparator);
    }

    public List<NText> splitLines(boolean returnSeparator) {
        return split(Pattern.compile("\\r?\\n"), returnSeparator);
    }

    @Override
    public List<NText> splitLines() {
        return splitLines(false);
    }


    /**
     * Traverse bfs.
     *
     * @param visitor visitor
     */
    public NText traverseBFS(NTextVisitor visitor) {
        Queue<NText> q = new ArrayDeque<>();
        Deque<NText> entered = new ArrayDeque<>(); // stack of entered nodes, for reversed exit order
        q.add(this);
        while (!q.isEmpty()) {
            NText u = q.remove();
            visitor.enter(u);
            entered.push(u);
            switch (u.type()) {
                case PLAIN:
                case CODE:
                case ANCHOR:
                case LINK:
                case COMMAND:
                case INCLUDE: {
                    break;
                }
                case TITLE: {
                    NTextTitle t = (NTextTitle) u;
                    NText child = t.child();
                    if (child != null) {
                        q.add(child);
                    }
                    break;
                }
                case STYLED: {
                    NTextStyled t = (NTextStyled) u;
                    NText child = t.child();
                    if (child != null) {
                        q.add(child);
                    }
                    break;
                }
                case LIST: {
                    NTextList t = (NTextList) u;
                    for (NText child : t.children()) {
                        if (child != null) {
                            q.add(child);
                        }
                    }
                    break;
                }
                case BUILDER: {
                    NTextBuilder t = (NTextBuilder) u;
                    for (NText child : t.children()) {
                        if (child != null) {
                            q.add(child);
                        }
                    }
                    break;
                }
            }
        }
        // exit in reverse of enter order: since a node's children are always
        // enqueued (and thus entered) after the node itself, this guarantees
        // every node's exit fires only after all its descendants' enters have fired.
        while (!entered.isEmpty()) {
            visitor.exit(entered.pop());
        }
        return this;
    }

    @Override
    public NText transform(NTextTransformConfig config) {
        if (NBlankable.isBlank(config)) {
            return this;
        }
        return transform(null, config);
    }


    public NStream<NNormalizedText> normalizeStream(NTextTransformer transformer, NTextTransformConfig config) {
        if (config == null) {
            config = new NTextTransformConfig();
        }
        config.flatten(true);
        config.normalize(true);
        NText z = transform(transformer, config);
        return NStream.ofIterator(new Iterator<NText>() {
            final Deque<NText> queue = new ArrayDeque<>();

            {
                if (z != null) {
                    queue.addFirst(z);
                }
                refactorNext();
            }

            private void refactorNext() {
                while (!queue.isEmpty()) {
                    NText z = queue.peek();
                    switch (z.type()) {
                        case PLAIN:
                        case CODE:
                        case ANCHOR:
                        case LINK:
                        case COMMAND:
                        case TITLE:
                        case STYLED: {
                            return;
                        }
                        case LIST: {
                            NTextList t = (NTextList) z;
                            queue.removeFirst();
                            List<NText> children = t.children();
                            if (children.size() > 0) {
                                for (int i = children.size() - 1; i >= 0; i--) {
                                    queue.addFirst(children.get(i));
                                }
                            }
                            break;
                        }
                        case BUILDER: {
                            NTextBuilder t = (NTextBuilder) z;
                            queue.removeFirst();
                            List<NText> children = t.children();
                            if (children.size() > 0) {
                                for (int i = children.size() - 1; i >= 0; i--) {
                                    queue.addFirst(children.get(i));
                                }
                            }
                            break;
                        }
                        case INCLUDE:
                        default: {
                            //won't be processed!
                            queue.removeFirst();
                            break;
                        }
                    }
                }
            }

            @Override
            public boolean hasNext() {
                refactorNext();
                return !queue.isEmpty();
            }

            @Override
            public NText next() {
                refactorNext();
                return queue.remove();
            }
        }).instanceOf(NNormalizedText.class).withDescription(NDescribables.ofDesc("flattened text"));
    }

    @Override
    public NText transform(NTextTransformer transformer, NTextTransformConfig config) {
        NText text=this;
        if (NBlankable.isBlank(config) && transformer == null) {
            return text;
        }
        if (config == null) {
            config = new NTextTransformConfig();
        }
        // start by processing includes
        if (config.isProcessIncludes()) {
            NTextTransformConfig iconfig = config.copy();
            iconfig.processIncludes(true);
            iconfig.importClassLoader(config.importClassLoader());
            NTextTransformerContext c = new DefaultNTextTransformerContext(iconfig);
            text = transform(text,c.defaultTransformer(), c);
            config = config.copy().processIncludes(false).importClassLoader(null);
        }

        if (NBlankable.isBlank(config) && transformer == null) {
            return text;
        }

        Integer rootLevel = config.rootLevel();
        if (rootLevel != null) {
            config = config.copy().rootLevel(null);
            //find root level
            int level = resolveRootLevel(text);
            if (level != rootLevel) {
                int offset = rootLevel - level;
                NTextTransformerContext c = new DefaultNTextTransformerContext(config);
                text = transform(text,(text1, context) -> {
                    if (text1.type() == NTextType.TITLE) {
                        NTextTitle t = (NTextTitle) text1;
                        return NText.ofTitle(t.child(), t.level() + offset);
                    }
                    return text1;
                }, c);
            }
        }

        if (NBlankable.isBlank(config) && transformer == null) {
            return text;
        }

        String anchor = config.anchor();
        if (anchor != null) {
            config = config.copy().anchor(null);
        }

        if (transformer != null || !config.isBlank()) {
            NTextTransformerContext c = new DefaultNTextTransformerContext(config);
            if (transformer == null) {
                transformer = c.defaultTransformer();
            }
            text = transform(text, transformer == null ? c.defaultTransformer() : transformer, c);
        }

        if (anchor != null) {
            List<NText> ok = new ArrayList<>();
            boolean foundAnchor = false;
            if (text.type() == NTextType.LIST) {
                for (NText o : ((NTextList) text)) {
                    if (foundAnchor) {
                        ok.add(o);
                    } else if (o.type() == NTextType.ANCHOR) {
                        if (anchor.equals(((DefaultNTextAnchor) o).value())) {
                            foundAnchor = true;
                        }
                    }
                }
            }
            if (foundAnchor) {
                text = NText.ofList(ok).simplify();
            }
        }
        return text;
    }

    private static int resolveRootLevel(NText text) {
        NRef<Integer> level = NRef.ofNull();
        text.traverseDFS(NTextVisitor.ofExit(n -> {
            if (n.type() == NTextType.TITLE) {
                int lvl = ((NTextTitle) n).level();
                if (level.isNull() || level.get() > lvl) {
                    level.set(lvl);
                }
            }
        }));
        return level.isNull() ? 0 : level.get();
    }

    public NText transform(NText text, NTextTransformer transformer, NTextTransformConfig config) {
        if (text == null) {
            return null;
        }
        if (NBlankable.isBlank(config) && transformer == null) {
            return text;
        }
        if (config == null) {
            config = new NTextTransformConfig();
        }
        // start by processing includes
        if (config.isProcessIncludes()) {
            NTextTransformConfig iconfig = config.copy();
            iconfig.processIncludes(true);
            iconfig.importClassLoader(config.importClassLoader());
            NTextTransformerContext c = new DefaultNTextTransformerContext(iconfig);
            text = transform(text, c.defaultTransformer(), c);
            config = config.copy().processIncludes(false).importClassLoader(null);
        }

        if (NBlankable.isBlank(config) && transformer == null) {
            return text;
        }

        Integer rootLevel = config.rootLevel();
        if (rootLevel != null) {
            config = config.copy().rootLevel(null);
            //find root level
            int level = resolveRootLevel(text);
            if (level != rootLevel) {
                int offset = rootLevel - level;
                NTextTransformerContext c = new DefaultNTextTransformerContext(config);
                text = transform(text, (text1, context) -> {
                    if (text1.type() == NTextType.TITLE) {
                        NTextTitle t = (NTextTitle) text1;
                        return NText.ofTitle(t.child(), t.level() + offset);
                    }
                    return text1;
                }, c);
            }
        }

        if (NBlankable.isBlank(config) && transformer == null) {
            return text;
        }

        String anchor = config.anchor();
        if (anchor != null) {
            config = config.copy().anchor(null);
        }

        if (transformer != null || !config.isBlank()) {
            NTextTransformerContext c = new DefaultNTextTransformerContext(config);
            if (transformer == null) {
                transformer = c.defaultTransformer();
            }
            text = transform(text, transformer == null ? c.defaultTransformer() : transformer, c);
        }

        if (anchor != null) {
            List<NText> ok = new ArrayList<>();
            boolean foundAnchor = false;
            if (text.type() == NTextType.LIST) {
                for (NText o : ((NTextList) text)) {
                    if (foundAnchor) {
                        ok.add(o);
                    } else if (o.type() == NTextType.ANCHOR) {
                        if (anchor.equals(((DefaultNTextAnchor) o).value())) {
                            foundAnchor = true;
                        }
                    }
                }
            }
            if (foundAnchor) {
                text = NText.ofList(ok).simplify();
            }
        }
        return text;
    }


    @Override
    public NText traverseDFS(NTextVisitor visitor) {
        Deque<Frame> stack = new ArrayDeque<>();
        stack.push(new Frame(this, false));
        while (!stack.isEmpty()) {
            Frame f = stack.pop();
            if (f.exit) {
                visitor.exit(f.node);
                continue;
            }
            NText u = f.node;
            visitor.enter(u);
            // schedule this node's exit to fire only after all its children are fully processed
            stack.push(new Frame(u, true));
            switch (u.type()) {
                case PLAIN:
                case CODE:
                case ANCHOR:
                case LINK:
                case COMMAND:
                case INCLUDE: {
                    break;
                }
                case TITLE: {
                    NTextTitle t = (NTextTitle) u;
                    NText child = t.child();
                    if (child != null) {
                        stack.push(new Frame(child, false));
                    }
                    break;
                }
                case STYLED: {
                    NTextStyled t = (NTextStyled) u;
                    NText child = t.child();
                    if (child != null) {
                        stack.push(new Frame(child, false));
                    }
                    break;
                }
                case LIST: {
                    NTextList t = (NTextList) u;
                    pushChildrenReversed(stack, t.children());
                    break;
                }
                case BUILDER: {
                    NTextBuilder t = (NTextBuilder) u;
                    pushChildrenReversed(stack, t.children());
                    break;
                }
            }
        }
        return this;
    }

    private static void pushChildrenReversed(Deque<Frame> stack, Iterable<NText> children) {
        Deque<NText> tmp = new ArrayDeque<>();
        for (NText child : children) {
            if (child != null) {
                tmp.push(child);
            }
        }
        while (!tmp.isEmpty()) {
            stack.push(new Frame(tmp.pop(), false));
        }
    }

    private static final class Frame {
        final NText node;
        final boolean exit;

        Frame(NText node, boolean exit) {
            this.node = node;
            this.exit = exit;
        }
    }

    private NText transform(NText text, NTextTransformer transformer, NTextTransformerContext c) {
        if (text == null) {
            return null;
        }
        NText pt = transformer.preTransform(text, c);
        if (pt != text) {
            return pt;
        }
        switch (text.type()) {
            case PLAIN:
            case CODE:
            case ANCHOR:
            case LINK:
            case COMMAND: {
                return transformer.postTransform(text, c);
            }
            case TITLE: {
                NTextTitle t = (NTextTitle) text;
                NText child = t.child();
                if (child == null) {
                    return null;
                }
                child = transform(child, transformer, c);
                return transformer.postTransform(NText.ofTitle(child, t.level()), c);
            }
            case STYLED: {
                NTextStyled t = (NTextStyled) text;
                NText child = t.child();
                if (child == null) {
                    return null;
                }
                child = transform(child, transformer, c);
                return transformer.postTransform(NText.ofStyled(child, t.styles()), c);
            }
            case LIST: {
                NTextList t = (NTextList) text;
                List<NText> li = new ArrayList<>();
                boolean wasNullInclude = false; // used to track when a newline is
                for (NText child : t.children()) {
                    if (child != null) {
                        NText oldChild = child;
                        child = transform(child, transformer, c);
                        if (child != null) {
                            if (child.isNewLine() && wasNullInclude) {
                                //just ignore
                            } else {
                                li.add(child);
                            }
                            wasNullInclude = false;
                        } else if (oldChild instanceof NTextInclude) {
                            // starts with new line, then include, then newline
                            wasNullInclude = true;
                        }
                    }
                }
                if (li.size() > 0) {
                    if (li.size() == 1) {
                        return transformer.postTransform(li.get(0), c);
                    }
                    return transformer.postTransform(NText.ofList(li), c);
                }
                return null;
            }
            case BUILDER: {
                NTextBuilder t = (NTextBuilder) text;
                List<NText> li = new ArrayList<>();
                for (NText child : t.children()) {
                    if (child != null) {
                        child = transform(child, transformer, c);
                        if (child != null) {
                            li.add(child);
                        }
                    }
                }
                if (!li.isEmpty()) {
                    if (li.size() == 1) {
                        return transformer.postTransform(li.get(0), c);
                    }
                    return transformer.postTransform(NText.ofList(li), c);
                }
                return null;
            }
            case INCLUDE: {
                return null;
            }
        }
        return null;
    }
}
