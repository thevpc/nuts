package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.collections.EvictingCharQueue;
import net.thevpc.nuts.runtime.bundles.string.StringBuilder2;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;

public class StyledParserStep extends ParserStep {

    public static final IntPredicate EXIT_ON_CLOSE_ACCOLADES = ((cc) -> cc == '}' || cc == '#');
    private static Pattern NAME_AND_NUMBER = Pattern.compile("^(?<n>[a-zA-Z_-]+)(?<d>[0-9]*)$");
    int sharpsStartCount = 0;
    int sharpsEndCount = 0;
    CurState curState = CurState.EMPTY;
    List<NutsText> childrenTextNodes = new ArrayList<>();
    StringBuilder2 sharp_name = new StringBuilder2();
    StringBuilder2 sharp_content = new StringBuilder2();
    boolean lineStart;
//    List<ParserStep> children = new ArrayList<>();
    int maxSize = 10;
    private NutsWorkspace ws;
    private EvictingCharQueue charQueue = new EvictingCharQueue(5);
    private DefaultNutsTextNodeParser.State state;
    private StyledParserStepCommandParser parseHelper = new StyledParserStepCommandParser();
    private boolean wasEscape;

    public StyledParserStep(char c, boolean lineStart, NutsWorkspace ws, DefaultNutsTextNodeParser.State state) {
        switch (c) {
            case '#': {
                curState = CurState.SHARP;
                sharpsStartCount = 1;
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid");
            }
        }
//        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.ws = ws;
        this.state = state;
    }

    public StyledParserStep(String c, boolean lineStart, NutsWorkspace ws, DefaultNutsTextNodeParser.State state) {
        if (c.charAt(0) == '#') {
            curState = CurState.SHARP;
            sharpsStartCount = 1;
            for (int i = 1; i < c.length(); i++) {
                consume(c.charAt(i), state, false);
            }
        } else {
            throw new IllegalArgumentException("unsupported");
        }
//        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.ws = ws;
        this.state = state;
    }

    public void consume(char c, DefaultNutsTextNodeParser.State state, boolean wasNewLine) {
        charQueue.add(c);
        switch (curState) {
            case EMPTY: {
                throw new IllegalArgumentException("unexpected");
            }
            case SHARP: {
                switch (c) {
                    case '\\': {
                        wasEscape=true;
                        if(sharpsStartCount==1){
                            state.applyDropReplace(new PlainParserStep("#",false,lineStart, ws,state,null));
                        }else{
                            curState=CurState.SHARP_CONTENT;
                        }
                        break;
                    }
                    case '#': {
                        //do not change state
                        sharpsStartCount++;
                        break;
                    }
                    case ')': {
                        state.applyDropReplace(new TitleParserStep(
                                CoreStringUtils.fillString("#", sharpsStartCount) + ")", ws));
                        break;
                    }
                    case NutsConstants.Ntf.SILENT: {
                        state.applyDropReplace(new PlainParserStep(CoreStringUtils.fillString("#", sharpsStartCount), lineStart, false, ws, state, null));
                        break;
                    }
                    case ':': {
                        if (sharpsStartCount == 2) {
                            curState = CurState.SHARP2_COL_NAME;
                        } else {
                            sharp_content.append(c);
                            curState = CurState.SHARP_CONTENT;
                        }
                        break;
                    }
                    case '{': {
                        if (sharpsStartCount == 2) {
                            curState = CurState.SHARP2_OBRACE_NAME;
                        } else {
                            sharp_content.append(c);
                            curState = CurState.SHARP_CONTENT;
                        }
                        break;
                    }
                    default: {
                        if (sharpsStartCount == 1) {
                            state.applyDropReplace(new PlainParserStep(CoreStringUtils.fillString("#", sharpsStartCount) + c, lineStart, false, ws, state, null));
                        } else {
                            sharp_content.append(c);
                            curState = CurState.SHARP_CONTENT;
                        }
                        break;
                    }
                }
                break;
            }
            case SHARP2_COL_NAME_CONTENT:
            case SHARP_CONTENT: {
                switch (c) {
                    case '\\': {
                        if(wasEscape){
                            wasEscape = false;
                            sharp_content.append(c);
                        }else {
                            wasEscape = true;
                        }
                        break;
                    }
                    case '#': {
                        if(wasEscape) {
                            wasEscape = false;
                            sharp_content.append(c);
                        }else {
                            if (!sharp_content.isEmpty()) {
                                childrenTextNodes.add(ws.text().forPlain(sharp_content.readAll()));
                            }
                            if (curState == CurState.SHARP_CONTENT) {
                                curState = CurState.SHARP_CONTENT_SHARP;
                            } else if (curState == CurState.SHARP2_COL_NAME_CONTENT) {
                                curState = CurState.SHARP2_COL_NAME_CONTENT_SHARP;
                            } else {
                                throw new IllegalArgumentException("unexpected");
                            }
                            sharpsEndCount = 1;
                        }
                        break;
                    }
                    case '`': {
                        if(wasEscape) {
                            wasEscape = false;
                            sharp_content.append(c);
                        }else {
                            if (!sharp_content.isEmpty()) {
                                childrenTextNodes.add(ws.text().forPlain(sharp_content.readAll()));
                            }
                            state.applyStart(c, true, false);
                        }
                        break;
                    }
                    case NutsConstants.Ntf.SILENT: {
                        if(wasEscape) {
                            wasEscape = false;
                            sharp_content.append(c);
                        }else {
                            //ignore!
                        }
                        break;
                    }
                    default: {
                        if(wasEscape) {
                            wasEscape = false;
                            sharp_content.append('\\');
                            sharp_content.append(c);
                        }else {
                            sharp_content.append(c);
                        }
                    }
                }
                break;
            }
            case SHARP_CONTENT_SHARP:
            case SHARP2_COL_NAME_CONTENT_SHARP: {
                switch (c) {
                    case '\\': {
                        if (sharpsStartCount == sharpsEndCount) {
                            //got the end!
                            if (curState == CurState.SHARP_CONTENT_SHARP) {
                                curState = CurState.SHARP_CONTENT_SHARP_END;
                            } else if (curState == CurState.SHARP2_COL_NAME_CONTENT_SHARP) {
                                curState = CurState.SHARP2_COL_NAME_CONTENT_SHARP_END;
                            }
                            state.applyPopReplay(c);
                        }else{
                            sharp_content.append(CoreStringUtils.fillString("#", sharpsEndCount));
                            sharpsEndCount=0;
                            if (curState == CurState.SHARP_CONTENT_SHARP) {
                                curState = CurState.SHARP_CONTENT;
                            } else if (curState == CurState.SHARP2_COL_NAME_CONTENT_SHARP) {
                                curState = CurState.SHARP2_COL_NAME_CONTENT;
                            }
                            wasEscape=true;
                        }
                    }
                    case '#': {
                        sharpsEndCount++;
                        break;
                    }
                    case NutsConstants.Ntf.SILENT: {
                        if (sharpsStartCount == sharpsEndCount) {
                            //got the end!
                            if (curState == CurState.SHARP_CONTENT_SHARP) {
                                curState = CurState.SHARP_CONTENT_SHARP_END;
                            } else if (curState == CurState.SHARP2_COL_NAME_CONTENT_SHARP) {
                                curState = CurState.SHARP2_COL_NAME_CONTENT_SHARP_END;
                            }
                            state.applyPop();
                        } else {
                            logErr("expected " + CoreStringUtils.fillString("#", sharpsStartCount) + "<END>");
                            childrenTextNodes.add(ws.text().forPlain(CoreStringUtils.fillString("#", sharpsStartCount)));
                            if (curState == CurState.SHARP_CONTENT_SHARP) {
                                curState = CurState.SHARP_CONTENT_SHARP_END;
                            } else if (curState == CurState.SHARP2_COL_NAME_CONTENT_SHARP) {
                                curState = CurState.SHARP2_COL_NAME_CONTENT_SHARP_END;
                            }
                        }
                        break;
                    }
                    default: {
                        if (sharpsStartCount == sharpsEndCount) {
                            if (curState == CurState.SHARP_CONTENT_SHARP) {
                                curState = CurState.SHARP_CONTENT_SHARP_END;
                            } else if (curState == CurState.SHARP2_COL_NAME_CONTENT_SHARP) {
                                curState = CurState.SHARP2_COL_NAME_CONTENT_SHARP_END;
                            }
                            state.applyPopReplay(c);
                        } else {
                            state.applyPush(new StyledParserStep("#", false, ws, state));
                            for (int i = 0; i < sharpsEndCount - 1; i++) {
                                state.applyNextChar('#');
                            }
                            state.applyNextChar(c);
                        }
                    }
                }
                break;
            }

            case SHARP2_OBRACE_NAME:
            case SHARP2_COL_NAME: {
                switch (c) {
                    case '\\': {
                        if(wasEscape){
                            wasEscape = false;
                            sharp_name.append(c);
                        }else {
                            wasEscape = true;
                        }
                        break;
                    }
                    case ':': {
                        if(wasEscape){
                            wasEscape = false;
                            sharp_name.append(c);
                        }else {
                            if(curState==CurState.SHARP2_COL_NAME) {
                                curState = CurState.SHARP2_COL_NAME_CONTENT;
                            }else {
                                curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                            }
                        }
                        break;
                    }
                    case NutsConstants.Ntf.SILENT:
                    case '#':
                    case '{':
                    case '}':
                    case '(':
                    case ')':
                    case '[':
                    case ']': {
                        if(wasEscape){
                            wasEscape = false;
                            if(c==NutsConstants.Ntf.SILENT) {
                                sharp_name.append(c);
                            }else{
                                sharp_name.append('\\');
                                sharp_name.append(c);
                            }
                        }else {
                            logErr("expected ':'");
                            if(curState==CurState.SHARP2_COL_NAME) {
                                curState = CurState.SHARP2_COL_NAME_CONTENT;
                            }else {
                                curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                            }
                        }
                        break;
                    }
                    default: {
                        if(wasEscape){
                            if (c <= 32 || c==':'|| c=='#') {
                                sharp_name.append(c);
                            } else {
                                sharp_name.append('\\');
                                sharp_name.append(c);
                            }
                            wasEscape=false;
                        }else {
                            if (c <= 32) {
                                if (sharp_name.isEmpty()) {
                                    //ignore
                                } else {
                                    sharp_name.append(c);
                                }
                            } else {
                                sharp_name.append(c);
                            }
                        }
                    }
                }
                break;
            }
            case SHARP2_OBRACE_NAME_COL_CONTENT: {
                switch (c) {
                    case '#': {
                        if(wasEscape){
                            wasEscape=false;
                            sharp_content.append(c);
                        }else {
                            if (!sharp_content.isEmpty()) {
                                childrenTextNodes.add(ws.text().forPlain(sharp_content.readAll()));
                            }
                            state.applyPush(new StyledParserStep("#", false, ws, state));
                        }
                        break;
                    }
                    case '}': {
                        if(wasEscape){
                            wasEscape=false;
                            sharp_content.append(c);
                        }else {
                            if (!sharp_content.isEmpty()) {
                                childrenTextNodes.add(ws.text().forPlain(sharp_content.readAll()));
                            }
                            curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        }
                        break;

                    }
                    case NutsConstants.Ntf.SILENT: {
                        if(wasEscape){
                            wasEscape=false;
                            sharp_content.append(c);
                        }else {
                            if (!sharp_content.isEmpty()) {
                                childrenTextNodes.add(ws.text().forPlain(sharp_content.readAll()));
                            }
                        }
                        break;
                    }
                    case '`': {
                        if(wasEscape){
                            wasEscape=false;
                            sharp_content.append(c);
                        }else {
                            if (!sharp_content.isEmpty()) {
                                childrenTextNodes.add(ws.text().forPlain(sharp_content.readAll()));
                            }
                            state.applyStart(c, true, false);
                        }
                        break;
                    }
                    default: {
                        if(wasEscape){
                            wasEscape=false;
                            sharp_content.append('\\');
                            sharp_content.append(c);
                        }else {
                            sharp_content.append(c);
                        }
                    }
                }
                break;
            }
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE: {
                switch (c) {
                    case '\\': {
                        sharp_content.append('{');
                        curState=CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        wasEscape=true;
                        break;
                    }
                    case '#': {
                        sharpsEndCount++;
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP;
                        break;
                    }
                    case '`': {
                        childrenTextNodes.add(ws.text().forPlain("}"));
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        state.applyStart(c, true, false);
                        break;
                    }
                    case NutsConstants.Ntf.SILENT: {
                        childrenTextNodes.add(ws.text().forPlain("}"));
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        //ignore
                        break;
                    }
                    default: {
                        sharp_content.append(c);
                    }
                }
                break;
            }
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP: {
                switch (c) {
                    case '\\': {
                        sharp_content.append('{');
                        sharp_content.append('#');
                        curState=CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        wasEscape=true;
                        break;
                    }
                    case '#': {
                        sharpsEndCount++;
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP2_END;
                        state.applyPop();
                        break;
                    }
                    case '`': {
                        childrenTextNodes.add(ws.text().forPlain("}#"));
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        state.applyStart(c, true, false);
                        break;
                    }
                    case NutsConstants.Ntf.SILENT: {
                        childrenTextNodes.add(ws.text().forPlain("}#"));
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        //ignore
                        break;
                    }
                    default: {
                        childrenTextNodes.add(ws.text().forPlain("}#"));
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        sharp_content.append(c);
                    }
                }
                break;
            }
            case SHARP_CONTENT_SHARP_END:{
                switch (c){
                    case '#':{
                        //too many sharps!
                        curState=CurState.SHARP_CONTENT;
                        int _sharpsEndCount=sharpsEndCount;
                        sharpsEndCount=0;
                        state.applyPush(new StyledParserStep("#", false, ws, state));
                        for (int i = 0; i < _sharpsEndCount - 1; i++) {
                            state.applyNextChar('#');
                        }
                        state.applyPopReplay(c);
                        break;
                    }
                    default:{
                        if (!sharp_content.isEmpty()) {
                            childrenTextNodes.add(ws.text().forPlain(sharp_content.readAll()));
                        }
                        state.applyPopReplay(c);
                    }
                }
                break;
            }
            case SHARP2_COL_NAME_CONTENT_SHARP_END:{
                switch (c){
                    case '#':{
                        //too many sharps!
                        curState=CurState.SHARP2_COL_NAME_CONTENT;
                        int _sharpsEndCount=sharpsEndCount;
                        sharpsEndCount=0;
                        state.applyPush(new StyledParserStep("#", false, ws, state));
                        for (int i = 0; i < _sharpsEndCount - 1; i++) {
                            state.applyNextChar('#');
                        }
                        state.applyPopReplay(c);
                        break;
                    }
                    default:{
                        if (!sharp_content.isEmpty()) {
                            childrenTextNodes.add(ws.text().forPlain(sharp_content.readAll()));
                        }
                        state.applyPopReplay(c);
                    }
                }
                break;
            }
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP2_END:{
                if (!sharp_content.isEmpty()) {
                    childrenTextNodes.add(ws.text().forPlain(sharp_content.readAll()));
                }
                state.applyPopReplay(c);
                break;
            }
            default: {
                throw new IllegalArgumentException("unexpected " + curState);
            }
        }
    }

    @Override
    public void appendChild(ParserStep tt) {
        childrenTextNodes.add(tt.toText());
    }

    @Override
    public NutsText toText() {
        List<NutsText> childrenTextNodes2 = new ArrayList<>(childrenTextNodes);
        if (!sharp_content.isEmpty()) {
            childrenTextNodes2.add(ws.text().forPlain(sharp_content.toString()));
        }

        NutsText a = ws.text().forList(childrenTextNodes2.toArray(new NutsText[0])).simplify();
        if(a==null){
            return  ws.text().forPlain("");
        }
        switch (curState) {
            case SHARP:
            {
                return ws.text().forPlain("#");
            }
            case EMPTY:{
                return ws.text().forPlain("");
            }
            case SHARP_CONTENT:
            case SHARP_CONTENT_SHARP:
            case SHARP_CONTENT_SHARP_END:
            {
                return ws.text().forStyled(a, NutsTextStyle.primary(sharpsStartCount));
            }
            case SHARP2_OBRACE_NAME:{
                return ws.text().forPlain("##{"+sharp_name.toString());
            }
            case SHARP2_OBRACE_NAME_COL:{
                return ws.text().forPlain("##{"+sharp_name.toString()+":");
            }
            case SHARP2_OBRACE_NAME_COL_CONTENT:
            case SHARP2_COL_NAME_SHARP1:
            case SHARP2_COL_NAME_SHARP2:
            case SHARP2_COL_NAME:
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE:
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP:
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP2_END:
            case SHARP2_COL_NAME_CONTENT:
            case SHARP2_COL_NAME_CONTENT_SHARP:
            case SHARP2_COL_NAME_CONTENT_SHARP_END:
            {
                NutsTextStyle s = parseHelper.parseSimpleNutsTextStyle(sharp_name.toString());
                if (s != null) {
                    return ws.text().forStyled(a, s);
                }
                throw new NutsIllegalArgumentException(ws.createSession(), "unable to resolve style from " + s);
            }
        }
        throw new IllegalArgumentException("unexpected");
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        p.applyPop();
    }

    public boolean isComplete() {
        switch (curState) {
            case SHARP_CONTENT_SHARP_END:
            case SHARP2_COL_NAME_CONTENT_SHARP_END:
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP2_END: {
                return true;
            }
        }
        return false;
    }

    private void logErr(String s) {
        System.err.println(s);
    }

    @Override
    public String toString() {
//        StringBuilder sb = new StringBuilder("Typed(" + CoreStringUtils.dblQuote(start.toString()));
//        if (!started) {
//            sb.append(",<NEW>");
//        }
//        for (ParserStep parserStep : children) {
//            sb.append(",");
//            sb.append(parserStep.toString());
//        }
//        sb.append(",END(").append(CoreStringUtils.dblQuote(end.toString())).append(")");
//        sb.append(isComplete() ? "" : ",incomplete");
//        return sb.append(")").toString();
        StringBuilder sb = new StringBuilder();
        for (NutsText parserStep : childrenTextNodes) {
            sb.append(parserStep.toString());
        }
        return sb.toString();
    }

    enum CurState {
        EMPTY,
        // #
        SHARP,
        // ##<name>
        SHARP_CONTENT,
        SHARP_CONTENT_SHARP,
        SHARP_CONTENT_SHARP_END,
        // ##:
        SHARP2_COL_NAME,
        // ##:<name>
        SHARP2_COL_NAME_CONTENT,
        SHARP2_COL_NAME_CONTENT_SHARP,
        SHARP2_COL_NAME_CONTENT_SHARP_END,
        // ##:<name>#
        SHARP2_COL_NAME_SHARP1,
        // ##:<name>##
        SHARP2_COL_NAME_SHARP2,
        // ##{<name>
        SHARP2_OBRACE_NAME,
        // ##{<name>:
        SHARP2_OBRACE_NAME_COL,
        // ##{<name>:<sub-node>
        SHARP2_OBRACE_NAME_COL_CONTENT,
        // ##{<name>:<sub-node>}
        SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE,
        // ##{<name>:<sub-node>}#
        SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP,
        SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP2_END,
    }

    enum StyleMode {
        SIMPLE, // ##anything##
        COLON, // ##:12:anything##
        EMBEDDED,     // ##{12:anything}##
        // ##:sh:anything##
    }

}
