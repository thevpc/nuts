package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextNodeParser;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class DefaultNutsTextNodeBuilder implements NutsTextBuilder {

    private final List<NutsText> children = new ArrayList<>();
    private final NutsSession session;
    private final NutsTexts text1;
    private NutsTextWriteConfiguration writeConfiguration;
    private NutsTextStyleGenerator styleGenerator;
    private boolean flattened = true;

    public DefaultNutsTextNodeBuilder(NutsSession session) {
        this.session = session;
        text1 = NutsTexts.of(session);
    }

    @Override
    public NutsTextStyleGenerator getStyleGenerator() {
        if (styleGenerator == null) {
            styleGenerator = new DefaultNutsTextStyleGenerator();
        }
        return styleGenerator;
    }

    @Override
    public DefaultNutsTextNodeBuilder setStyleGenerator(NutsTextStyleGenerator styleGenerator) {
        this.styleGenerator = styleGenerator;
        return this;
    }

    @Override
    public NutsTextWriteConfiguration getConfiguration() {
        return writeConfiguration;
    }

    @Override
    public NutsTextBuilder setConfiguration(NutsTextWriteConfiguration writeConfiguration) {
        this.writeConfiguration = writeConfiguration;
        return this;
    }

    @Override
    public NutsTextBuilder appendCommand(NutsTerminalCommand command) {
        append(text1.ofCommand(command));
        return this;
    }

    @Override
    public NutsTextBuilder appendCode(String lang, String text) {
        append(text1.ofCode(lang, text));
        return this;
    }

    //
//    @Override
//    public NutsTextBuilder append(String text, NutsTextStyle... styles) {
//        return append(text1.text().forPlain(text), styles);
//    }
    @Override
    public NutsTextBuilder appendHash(Object text) {
        return appendHash(text, text);
    }

    @Override
    public NutsTextBuilder appendRandom(Object text) {
        if (text == null) {
            return this;
        }
        return append(text, getStyleGenerator().random());
    }

    @Override
    public NutsTextBuilder appendHash(Object text, Object hash) {
        if (text == null) {
            return this;
        }
        if (hash == null) {
            hash = text;
        }
        return append(text, getStyleGenerator().hash(hash));
    }

    @Override
    public NutsTextBuilder append(Object text, NutsTextStyle style) {
        return append(text, NutsTextStyles.of(style));
    }

    @Override
    public NutsTextBuilder append(Object text, NutsTextStyles styles) {
        if (text != null) {
            if (styles.size() == 0) {
                append(NutsTexts.of(session).toText(text));
            } else {
                append(text1.applyStyles(NutsTexts.of(session).toText(text), styles));
            }
        }
        return this;
    }

    @Override
    public NutsTextBuilder append(Object node) {
        if (node != null) {
            return append(NutsTexts.of(session).toText(node));
        }
        return this;
    }

    @Override
    public NutsTextBuilder append(NutsText node) {
        if (node != null) {
            children.add(node);
            flattened = false;
        }
        return this;
    }

    @Override
    public NutsTextBuilder appendJoined(Object separator, Collection<?> others) {
        if (others != null) {
            boolean first = true;
            for (Object other : others) {
                if (other != null) {
                    if (first) {
                        first = false;
                    } else {
                        if (separator != null) {
                            append(separator);
                        }
                    }
                    append(other);
                }
            }
        }
        return this;
    }
    //    @Override
//    public NutsTextBuilder append(NutsString str) {
//        if (str != null) {
//            NutsText n = ws.text().parser().parse(new StringReader(str.toString()));
//            if (n != null) {
//                append(n);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public NutsTextBuilder append(NutsFormattable str) {
//        if (str != null) {
//            append(ws.text().toText(str));
//        }
//        return this;
//    }

    @Override
    public NutsTextBuilder appendAll(Collection<?> others) {
        if (others != null) {
            for (Object node : others) {
                if (node != null) {
                    append(node);
                }
            }
        }
        return this;
    }

    @Override
    public NutsText build() {
        if (children.size() == 0) {
            return text1.ofPlain("");
        }
        if (children.size() == 1) {
            return children.get(0);
        }
        return text1.ofList(children).simplify();
    }

    @Override
    public NutsTextParser parser() {
        return new DefaultNutsTextNodeParser(session);
    }

    @Override
    public List<NutsText> getChildren() {
        return new ArrayList<>(children);
    }

    @Override
    public NutsText subChildren(int from, int to) {
        if (from < 0) {
            from = 0;
        }
        if (to >= size()) {
            to = size() - 1;
        }
        if (to <= from) {
            return NutsTexts.of(session).ofPlain("");
        }
        return NutsTexts.of(session).builder().appendAll(children.subList(from, to)).build();
    }

    public NutsText substring(int from, int to) {
        if (to <= from) {
            return NutsTexts.of(session).ofPlain("");
        }
        int firstIndex = ensureCut(from);
        if (firstIndex < 0) {
            return NutsTexts.of(session).ofPlain("");
        }
        int secondIndex = ensureCut(to);
        if (secondIndex < 0) {
            //the cut is till the end
            return NutsTexts.of(session).builder().appendAll(children.subList(firstIndex, children.size())).build();
        }
        return NutsTexts.of(session).builder().appendAll(children.subList(firstIndex, secondIndex)).build();
//        int index=0;
//        List<NutsText> ok=new ArrayList<>();
//        for (NutsText child : p) {
//            int len = child.textLength();
//            int start=index;
//            int end=index+len;
//            index=end;
//            if(from<start){
//                if(to>=end){
//                    ok.add(child);
//                }else if(to>=start){
//                    ok.add(substring(0,to-start,child));
//                }
//            }else if(from<end){
//                if(to>=end){
//                    ok.add(substring(from-start,start+len,child));
//                }else if(to>=start){
//                    ok.add(substring(from-start,to-start,child));
//                }
//            }else if(from>=to){
//                break;
//            }
//        }
//        if(ok.isEmpty()){
//            return NutsTexts.of(session).ofPlain("");
//        }
//        if(ok.size()==1){
//            return ok.get(0);
//        }
//        return new DefaultNutsTextList(session,ok.toArray(new NutsText[0]));
    }

    @Override
    public NutsTextBuilder insert(int at, NutsText... newTexts) {
        return replaceChildren(at, at + 1, newTexts);
    }

    @Override
    public NutsTextBuilder replace(int from, int to, NutsText... newTexts) {
        if (to <= from) {
            return this;
        }
        int firstIndex = ensureCut(from);
        if (firstIndex < 0) {
            return this;
        }
        int secondIndex = ensureCut(to);
        if (secondIndex < 0) {
            //the cut is till the end
            replaceChildren(firstIndex, children.size(), newTexts);
        }
        replaceChildren(firstIndex, secondIndex, newTexts);
        return this;
    }

    @Override
    public NutsTextBuilder replaceChildren(int from, int to, NutsText... newTexts) {
        if (newTexts == null) {
            newTexts = new NutsText[0];
        } else {
            newTexts = Arrays.stream(newTexts).filter(x -> x != null && !x.isEmpty()).toArray(NutsText[]::new);
        }
        if (from < to) {
            children.subList(from, to).clear();
            if (newTexts.length > 0) {
                children.addAll(from, Arrays.asList(newTexts));
            }
        }
        return this;
    }

    @Override
    public int size() {
        return children.size();
    }

    public NutsText get(int index) {
        return children.get(index);
    }

    @Override
    public Iterable<NutsText> items() {
        return children;
    }

    @Override
    public NutsTextBuilder flatten() {
        if (!flattened) {
            NutsTextNodeWriterRaw ss = new NutsTextNodeWriterRaw(session);
            ss.flattenNode(build(), getConfiguration());
            List<NutsText> i = ss.getItems();
            if (i.isEmpty()) {
                return this;
            }
            this.children.clear();
            this.children.addAll(i);
            flattened = true;
        }
        return this;
    }

//    private NutsText substring(int from, int to,NutsText t) {
//        if(from<=0){
//            from=0;
//        }
//        if(to<=from){
//            return NutsTexts.of(session).ofPlain("");
//        }
//        switch (t.getType()){
//            case PLAIN:{
//                NutsTextPlain p=(NutsTextPlain) t;
//                String text = p.getText();
//                int x=Math.min(to, text.length());
//                return NutsTexts.of(session).ofPlain(text.substring(from,x));
//            }
//            case STYLED:{
//                NutsTextStyled p=(NutsTextStyled) t;
//                NutsTextPlain pp=(NutsTextPlain) p.getChild();
//                String text = pp.getText();
//                int x=Math.min(to, text.length());
//                return NutsTexts.of(session).ofStyled(text.substring(from,to),p.getStyles());
//            }
//            case LIST:{
//                NutsTextList p=(NutsTextList) t;
//                return substring(from,to,p.getChildren());
//            }
//            default:{
//                NutsTextBuilder builder = t.builder().flatten().builder();
//                return builder.substring(from,to);
//            }
//        }
//    }

    @Override
    public NutsTextBuilder removeAt(int index) {
        children.remove(index);
        return this;
    }

    @Override
    public NutsStream<NutsTextBuilder> lines() {
        DefaultNutsTextNodeBuilder z = (DefaultNutsTextNodeBuilder) copy().flatten();
        return NutsStream.of(
                new Iterator<NutsTextBuilder>() {
                    NutsTextBuilder n;

                    @Override
                    public boolean hasNext() {
                        n = z.readLine();
                        return n != null;
                    }

                    @Override
                    public NutsTextBuilder next() {
                        return n;
                    }
                }, session
        );
    }

    @Override
    public NutsTextBuilder readLine() {
        if (this.size() == 0) {
            return null;
        }
        List<NutsText> r = new ArrayList<>();
        while (this.size() > 0) {
            NutsText t = this.get(0);
            this.removeAt(0);
            if (isNewLine(t)) {
                break;
            }
            r.add(t);
        }
        return NutsTexts.of(session).builder().appendAll(r);
    }

    private boolean isNewLine(NutsText t) {
        if (t.getType() == NutsTextType.PLAIN) {
            String txt = ((NutsTextPlain) t).getText();
            return (txt.equals("\n") || txt.equals("\r\n"));
        }
        return false;
    }


    public NutsTextBuilder copy() {
        DefaultNutsTextNodeBuilder c = new DefaultNutsTextNodeBuilder(session);
        c.appendAll(children);
        c.flattened = flattened;
        return c;
    }

    public int ensureCut(int at) {
//        List<NutsText> newValues=new ArrayList<>();
        if (at <= 0) {
            return 0;
        }
        NutsTexts text = NutsTexts.of(session);
        int charPos = 0;
        int index = 0;
        while (index < children.size()) {
            NutsText c = children.get(index);
            int start = charPos;
            int len = c.textLength();
            int end = start + len;
            if (at < start) {
                //continue
            } else if (at == start) {
                return index;
            } else if (at == end) {
                if (index + 1 < children.size()) {
                    return index + 1;
                }
                return -1;
            } else if (at > start && at < end) {
                List<NutsText> rv = c.builder().flatten().getChildren();
                List<NutsText> rv2 = new ArrayList<>(rv.size() + 1);
                int toReturn = -1;
                for (int i = 0; i < rv.size(); i++) {
                    NutsText child = rv.get(i);
                    start = charPos;
                    len = child.textLength();
                    end = start + len;
                    if (at < start) {
                        rv2.add(child);
                    } else if (at == start) {
                        rv2.add(child);
                        toReturn = i + index;
                    } else if (at >= end) {
                        rv2.add(child);
                    } else {
                        if (child.getType() == NutsTextType.PLAIN) {
                            NutsTextPlain p = (NutsTextPlain) child;
                            String tp = p.getText();
                            String a = tp.substring(0, at - start);
                            String b = tp.substring(at - start);
                            rv2.add(text.ofPlain(a));
                            rv2.add(text.ofPlain(b));
                            toReturn = index + i + 1;
                        } else if (child.getType() == NutsTextType.STYLED) {
                            NutsTextStyled p = (NutsTextStyled) child;
                            String tp = ((NutsTextPlain) p.getChild()).getText();
                            String a = tp.substring(0, at - start);
                            String b = tp.substring(at - start);
                            rv2.add(text.ofStyled(a, p.getStyles()));
                            rv2.add(text.ofStyled(b, p.getStyles()));
                            toReturn = index + i + 1;
                        }
                    }
                    charPos = end;
                }
                replaceChildren(index, index + 1, rv2.toArray(new NutsText[0]));
                return toReturn;
            }
            charPos = end;
            index++;
        }
        return -1;
    }

    @Override
    public NutsString immutable() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NutsTextNodeWriterStringer ss = new NutsTextNodeWriterStringer(out, session);
        ss.writeNode(build(), getConfiguration());
        return new NutsImmutableString(session, out.toString());
    }

    @Override
    public String filteredText() {
        StringBuilder sb = new StringBuilder();
        for (NutsText child : children) {
            sb.append(child.filteredText());
        }
        return sb.toString();
    }

    @Override
    public int textLength() {
        return immutable().textLength();
    }

    @Override
    public NutsText toText() {
        return build();
    }

    @Override
    public boolean isEmpty() {
        return immutable().isEmpty();
    }

    @Override
    public NutsTextBuilder builder() {
        return NutsTexts.of(session).builder().append(this);
    }

    @Override
    public String toString() {
        return immutable().toString();
    }

    @Override
    public boolean isBlank() {
        return NutsBlankable.isBlank(filteredText());
    }

}
