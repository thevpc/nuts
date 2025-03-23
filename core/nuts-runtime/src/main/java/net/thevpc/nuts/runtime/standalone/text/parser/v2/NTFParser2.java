package net.thevpc.nuts.runtime.standalone.text.parser.v2;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.AbstractNTextNodeParser;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTexts;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NTextVisitor;
import net.thevpc.nuts.text.NTexts;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class NTFParser2 extends AbstractNTextNodeParser {
    private StringBuffer buffer = new StringBuffer();
    private static String[] SHARPS = {
            "",
            "#",
            "##",
            "###",
            "####",
            "#####",
            "######",
            "#######",
            "########",
            "#########",
            "##########",
            "###########",
            "############",
            "#############",
            "##############",
            "###############"
    };

    enum StepEnum {
        CODE,
        TEXT,
        TITLE,
        SIMPLE_STYLE,
        COMPOSITE_STYLE,
    }

    private final NCharQueue q = new NCharQueue();
    private NTexts txt;
    private boolean wasNewLine = true;
    private Stack<Embedded> stackedStyles = new Stack<>();

    private static class Embedded {
        NTextStyles style;
        int level;
        StepEnum mode;
        List<NText> children = new ArrayList<>();

        public Embedded(StepEnum mode, NTextStyles style, int level) {
            this.style = style;
            this.mode = mode;
            this.level = level;
        }
    }

    public NTFParser2(NWorkspace workspace) {
        super(workspace);
        this.txt = new DefaultNTexts(workspace);//NTexts.of(
    }

    @Override
    public void offer(char c) {
        synchronized (q) {
            q.write(c);
        }
    }

    public void eof(boolean c) {
        synchronized (q) {
            q.eof(c);
        }
    }

    @Override
    public void offer(String c) {
        synchronized (q) {
            q.write(c);
        }
    }

    @Override
    public void offer(char[] c) {
        synchronized (q) {
            q.write(c);
        }
    }

    @Override
    public void offer(char[] c, int offset, int len) {
        synchronized (q) {
            q.write(c, offset, len);
        }
    }

    private void resetBuffer() {
        buffer.delete(0, buffer.length());
    }

    @Override
    public NText read() {
        return read(false);
    }

    @Override
    public NText readFully() {
        return read(true);
    }

    public NText read(boolean fully) {
        synchronized (q) {
            while (q.hasNext()) {
                Embedded embedded = stackedStyles.isEmpty() ? null : stackedStyles.peek();
                StepEnum mode = embedded == null ? StepEnum.TEXT : embedded.mode;
                switch (q.peek()) {
                    case '}': {
                        wasNewLine = false;
                        if (mode != StepEnum.COMPOSITE_STYLE) {
                            buffer.append(q.read());
                        } else {
                            NStringMatchResult n1 = q.peekString("}##", fully);
                            if (n1.mode() == NMatchType.FULL_MATCH) {
                                q.read(n1.count());
                                NText nText = pushUpCompositeStyle();
                                if(nText!=null) {
                                    return nText;
                                }
                            } else if (n1.mode() == NMatchType.NO_MATCH) {
                                buffer.append(q.read());
                            } else {
                                return null;
                            }
                        }
                        break;
                    }
                    case '#': {
                        if (mode == StepEnum.CODE) {
                            buffer.append(q.read());
                        } else {
                            int simpleLvl = (embedded == null ? 0 : embedded.level) + 1;
                            NRef<NText> ret = NRef.ofNull();
                            q.doWithPattern(
                                    new NMultiPattern()
                                            .setFully(fully)
                                            .onFullMatch("##:(?<n>[!a-zA-Z0-9_,(')/%+-]+)[: ]", m -> {
                                                String a = m.get();
                                                String s = m.get("n");
                                                wasNewLine = false;
                                                NTextStyles ss = NTextStyles.parse(s).orNull();
                                                q.read(m.count());
                                                NText p = pushUp(consumeBuffer());
                                                if (ss == null) {
                                                    //this is an invalid style
                                                    // just push all
                                                    buffer.append(a);
                                                } else {
                                                    pushSimpleStyle(ss);
                                                }
                                                if (p != null) {
                                                    ret.set(p);
                                                }
                                            })
                                            .onFullMatch("##\\{(?<n>[!a-zA-Z0-9_,(')/%+-]+)[: ]", m -> {
                                                wasNewLine = false;
                                                String a = m.get();
                                                String s = m.get("n");
                                                NTextStyles ss = NTextStyles.parse(s).orNull();
                                                q.read(m.count());
                                                NText p = pushUp(consumeBuffer());
                                                if (ss == null) {
                                                    //this is an invalid style
                                                    // just push all
                                                    buffer.append(a);
                                                } else {
                                                    pushCompositeStyle(ss);
                                                }
                                                if (p != null) {
                                                    ret.set(p);
                                                }
                                            })
                                            .onFullMatch("#+[)]",
                                                    wasNewLine && (mode == StepEnum.TEXT || mode == StepEnum.COMPOSITE_STYLE)
                                                    , m -> {
                                                        wasNewLine = false;
                                                        String s = m.get();
                                                        q.read(m.count());
                                                        if(q.hasNext() && q.peek()==' '){
                                                            //ignore leading space!
                                                            q.read();
                                                        }
                                                        NText p = pushUp(consumeBuffer());
                                                        pushTitle(s.length() - 1);
                                                        if (p != null) {
                                                            ret.set(p);
                                                        }
                                                    })
                                            .onFullMatch((simpleLvl<SHARPS.length?SHARPS[simpleLvl]:"") + "($|[^#])",
                                                    simpleLvl<SHARPS.length && mode == StepEnum.SIMPLE_STYLE,
                                                    m -> {
                                                        wasNewLine = false;
                                                        q.read(SHARPS[simpleLvl].length()); //ignore extra
                                                        NText p = pushUpSimpleStyle();
                                                        if(p!=null) {
                                                            ret.set(p);
                                                        }
                                                    }
                                            )
                                            .onFullMatch("##{" + (simpleLvl + 1) + ",}",
                                                    mode == StepEnum.SIMPLE_STYLE,
                                                    m -> {
                                                        wasNewLine = false;
                                                        q.read(m.count());
                                                        NText p = pushUp(consumeBuffer());
                                                        pushSimpleStyle(m.count() - 1);
                                                        if (p != null) {
                                                            ret.set(p);
                                                        }
                                                    }
                                            )
                                            .onFullMatch("##+",
                                                    mode != StepEnum.SIMPLE_STYLE,
                                                    m -> {
                                                        int count = m.count();
                                                        q.read(count);
                                                        wasNewLine = false;
                                                        NText p = pushUp(consumeBuffer());
                                                        pushSimpleStyle(count - 1);
                                                        if (p != null) {
                                                            ret.set(p);
                                                        }
                                                    }
                                            )
                                            .onNoMatch(() -> {
                                                wasNewLine = false;
                                                buffer.append(q.read());
                                            })
                                            .onMatch(c -> ret.set(null))
                                            .onPartialMatch(c -> ret.set(null))
                            );
                            if (ret.isSet()) {
                                return ret.get();
                            }
                            break;
                        }
                        break;
                    }
                    case '\\': {
                        if (mode == StepEnum.CODE) {
                            String s = q.peek(4);
                            if (s.length() == 4) {
                                wasNewLine = false;
                                if (s.equals("\\```")) {
                                    q.read();
                                    buffer.append(q.read());
                                } else {
                                    buffer.append(q.read(2));
                                }
                            } else {
                                if (q.isEOF()) {
                                    wasNewLine = false;
                                    buffer.append(q.read());
                                } else {
                                    return null;
                                }
                            }
                        } else {
                            String s = q.peek(4);
                            if(s.equals("\\```")){
                                q.read(4);
                                wasNewLine = false;
                                buffer.append("```");
                            }else {
                                s = q.peek(2);
                                if (s.length() == 2) {
                                    switch (s.charAt(1)) {
                                        case '\\': {
                                            wasNewLine = false;
                                            q.read(2);
                                            buffer.append("\\");
                                            break;
                                        }
                                        case '#': {
                                            wasNewLine = false;
                                            q.read(2);
                                            buffer.append("#");
                                            break;
                                        }
                                        case '\u001E': {
                                            wasNewLine = false;
                                            //just ignore
                                            q.read(2);
                                            break;
                                        }
                                        default: {
                                            wasNewLine = false;
                                            buffer.append(q.read(2));
                                        }
                                    }
                                } else {
                                    if (q.isEOF()) {
                                        wasNewLine = false;
                                        buffer.append(q.read());
                                    } else {
                                        return null;
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case '\u001E': {
                        if (mode == StepEnum.CODE) {
                            wasNewLine = false;
                            buffer.append(q.read());
                        } else {
                            // just ignore
                            wasNewLine = false;
                            q.read();
                        }
                        break;
                    }
                    case '\r':
                    case '\n': {
                        if (mode == StepEnum.CODE) {
                            wasNewLine = false;
                            buffer.append(q.read());
                        } else if (mode == StepEnum.TITLE) {
                            String s = q.readNewLine(true);
                            if (s != null) {
                                wasNewLine = true;
                                return pushUpTitle();
                            }else{
                                wasNewLine = false;
                            }
                            return null;
                        } else {
                            String s = q.readNewLine(fully);
                            if (s != null) {
                                wasNewLine = true;
                                buffer.append(s);
                            } else {
                                return null;
                            }
                        }
                        break;
                    }
                    case '`': {
                        NStringMatchResult n = q.peekPattern("```", fully);
                        if (mode == StepEnum.CODE) {
                            switch (n.mode()) {
                                case FULL_MATCH: {
                                    wasNewLine = false;
                                    q.read(3); // ignore extra // n.count()
//                                    if(containsNewline(buffer.toString().toCharArray())) {
//                                        q.readNewLine(true);
//                                    }
                                    NText nText = pushUpCode();
                                    if(nText!=null){
                                        return nText;
                                    }
                                    break;
                                }
                                case NO_MATCH: {
                                    wasNewLine = false;
                                    buffer.append(q.read());
                                    break;
                                }
                                default: {
                                    return null;
                                }
                            }
                        } else {
                            switch (n.mode()) {
                                case FULL_MATCH: {
                                    q.read(3); // ignore extra // n.count()
                                    NText p = pushUp(consumeBuffer());
                                    pushCode();
                                    if (p != null) {
                                        wasNewLine = false;
                                        return p;
                                    }
                                    break;
                                }
                                case NO_MATCH: {
                                    wasNewLine = false;
                                    buffer.append(q.read());
                                    break;
                                }
                                default: {
                                    return null;
                                }
                            }
                        }
                        break;
                    }
                    default: {
                        wasNewLine = false;
                        buffer.append(q.read());
                    }
                }
            }
            if (fully) {
                //push up remaining...
                while (!stackedStyles.isEmpty()) {
                    Embedded e = stackedStyles.peek();
                    switch (e.mode) {
                        case SIMPLE_STYLE: {
                            NText p = pushUpSimpleStyle();
                            if (p != null) {
                                return p;
                            }
                            break;
                        }
                        case COMPOSITE_STYLE: {
                            NText p = pushUpCompositeStyle();
                            if (p != null) {
                                return p;
                            }
                            break;
                        }
                        case CODE: {
                            NText p = pushUpCode();
                            if (p != null) {
                                return p;
                            }
                            break;
                        }
                        case TITLE: {
                            NText p = pushUpTitle();
                            if (p != null) {
                                return p;
                            }
                            break;
                        }
                    }
                }
            }
            Embedded embedded = stackedStyles.isEmpty() ? null : stackedStyles.peek();
            StepEnum mode = embedded == null ? StepEnum.TEXT : embedded.mode;
            if (mode == StepEnum.TEXT) {
                return pushUp(consumeBuffer());
            }
            return null;
        }
    }

    private NText pushUpCode() {
        Embedded embedded = stackedStyles.peek();
        String b = consumeBuffer();
        stackedStyles.pop();
        return pushUp(txt.ofCodeOrCommand(b));
    }

    private NText pushUpTitle() {
        Embedded embedded = stackedStyles.peek();
        pushUp(consumeBuffer());
        stackedStyles.pop();
        return pushUp(txt.ofTitle(txt.ofList(embedded.children), embedded.level));
    }

    private NText pushUpCompositeStyle() {
        Embedded embedded = stackedStyles.peek();
        pushUp(consumeBuffer());
        stackedStyles.pop();
        return pushUp(txt.ofStyled(txt.ofList(embedded.children).simplify(), embedded.style).simplify());
    }

    private NText pushUpSimpleStyle() {
        Embedded embedded = stackedStyles.peek();
        wasNewLine = false;
        pushUp(consumeBuffer());
        stackedStyles.pop();
        return pushUp(txt.ofStyled(txt.ofList(embedded.children).simplify(), embedded.style).simplify());
    }

    private void pushCompositeStyle(NTextStyles style) {
        stackedStyles.push(new Embedded(StepEnum.COMPOSITE_STYLE, style, 0));
    }

    private void pushCode() {
        stackedStyles.push(new Embedded(StepEnum.CODE, null, 0));
    }

    private void pushSimpleStyle(int level) {
        stackedStyles.push(new Embedded(StepEnum.SIMPLE_STYLE, NTextStyles.parse("p" + level).get(), level));
    }

    private void pushSimpleStyle(NTextStyles s) {
        stackedStyles.push(new Embedded(StepEnum.SIMPLE_STYLE, s, 1));
    }

    private void pushTitle(int level) {
        stackedStyles.push(new Embedded(StepEnum.TITLE, null, level));
    }


    private NText pushUp(NText t) {
        if (t == null) {
            return null;
        }
        if (!stackedStyles.isEmpty()) {
            stackedStyles.peek().children.add(t);
            return null;
        }
        return t;
    }

    private NText pushUp(String t) {
        if (t == null) {
            return null;
        }
        if (t.length() > 0) {
            return pushUp(txt.ofPlain(t));
        }
        return null;
    }

    private String consumeBuffer() {
        String s = buffer.toString();
        resetBuffer();
        return s;
    }

    @Override
    public void reset() {
        q.clear();
        stackedStyles.clear();
    }

    @Override
    public long parseRemaining(NTextVisitor visitor) {
        long b = 0;
        while (true) {
            NText n = readFully();
            if (n != null) {
                b++;
                visitor.visit(n);
            } else {
                if(q.hasNext()){
                    n = readFully();
                    if(n!=null){
                        visitor.visit(n);
                    }else {
                        throw new IllegalArgumentException("parseRemaining failed because readFull returns null whereas the stream is not empty?");
                    }
                }
                break;
            }
        }
        return b;
    }

    @Override
    public long parseIncremental(char[] buf, int off, int len, NTextVisitor visitor) {
        offer(buf, off, len);
        long b = 0;
        while (true) {
            NText n = read();
            if (n != null) {
                b++;
                visitor.visit(n);
            } else {
                break;
            }
        }
        return b;
    }

    @Override
    public boolean isIncomplete() {
        return q.length() > 0 || buffer.length() > 0 || !stackedStyles.isEmpty();
    }

    private static boolean containsNewline(char[] all){
        for (char c : all) {
            if(c=='\n' || c=='\r'){
                return true;
            }
        }
        return false;
    }
}
