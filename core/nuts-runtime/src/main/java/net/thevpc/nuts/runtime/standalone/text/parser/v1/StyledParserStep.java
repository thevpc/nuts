package net.thevpc.nuts.runtime.standalone.text.parser.v1;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextPlain;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NDebugString;
import net.thevpc.nuts.util.NEvictingCharQueue;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StyledParserStep extends ParserStep {

    public static final IntPredicate EXIT_ON_CLOSE_ACCOLADES = ((cc) -> cc == '}' || cc == '#');
    private static Pattern NAME_AND_NUMBER = Pattern.compile("^(?<n>[a-zA-Z_-]+)(?<d>[0-9]*)$");
    int sharpsStartCount = 0;
    int sharpsEndCount = 0;
    CurState curState = CurState.EMPTY;
    List<NText> children = new ArrayList<>();
    NStringBuilder name = new NStringBuilder();
    NStringBuilder content = new NStringBuilder();
    boolean lineStart;
//    List<ParserStep> children = new ArrayList<>();
    int maxSize = 10;
    private NWorkspace workspace;
    private NEvictingCharQueue charQueue = new NEvictingCharQueue(5);
    private DefaultNTextNodeParser.State state;
    private StyledParserStepCmdParser parseHelper = new StyledParserStepCmdParser();
    private boolean wasEscape;
    private boolean exitOnBrace;

    public StyledParserStep(char c, boolean lineStart, NWorkspace workspace, DefaultNTextNodeParser.State state, boolean exitOnBrace) {
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
        this.workspace = workspace;
        this.state = state;
        this.exitOnBrace = exitOnBrace;
    }

    public StyledParserStep(String c, boolean lineStart, NWorkspace workspace, DefaultNTextNodeParser.State state, boolean exitOnBrace) {
        this.workspace = workspace;
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
        this.state = state;
        this.exitOnBrace = exitOnBrace;
    }

    public void consume(char c, DefaultNTextNodeParser.State state, boolean wasNewLine) {
        charQueue.add(c);
        NTexts text = NTexts.of();
        switch (curState) {
            case EMPTY: {
                throw new IllegalArgumentException("unexpected");
            }
            case SHARP: {
                switch (c) {
                    case '\\': {
                        wasEscape=true;
                        if(sharpsStartCount==1){
                            beforeChangingStep();
                            state.applyDropReplacePreParsedPlain(this, "#", exitOnBrace);
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
                        beforeChangingStep();
                        state.applyDropReplace(this, new TitleParserStep(
                                CoreStringUtils.fillString("#", sharpsStartCount) + ")", workspace.currentSession()));
                        break;
                    }
                    case NConstants.Ntf.SILENT: {
                        beforeChangingStep();
                        state.applyDropReplacePreParsedPlain(this, CoreStringUtils.fillString("#", sharpsStartCount), exitOnBrace);
                        break;
                    }
                    case ':': {
                        if (sharpsStartCount == 2) {
                            curState = CurState.SHARP2_COL_NAME;
                        } else {
                            content.append(c);
                            curState = CurState.SHARP_CONTENT;
                        }
                        break;
                    }
                    case '{': {
                        if (sharpsStartCount == 2) {
                            curState = CurState.SHARP2_OBRACE_NAME;
                        } else {
                            content.append(c);
                            curState = CurState.SHARP_CONTENT;
                        }
                        break;
                    }
                    default: {
                        if(c=='}' && exitOnBrace){
                            state.applyDropReplacePreParsedPlain(this, CoreStringUtils.fillString("#", sharpsStartCount),false);
                            state.applyPop(this);
                            state.applyNextChar(c);
                        }else {
                            if (sharpsStartCount == 1) {
                                beforeChangingStep();
                                state.applyDropReplacePreParsedPlain(this, CoreStringUtils.fillString("#", sharpsStartCount), exitOnBrace);
                                state.applyNextChar(c);
                            } else {
                                content.append(c);
                                curState = CurState.SHARP_CONTENT;
                            }
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
                            content.append(c);
                        }else {
                            wasEscape = true;
                        }
                        break;
                    }
                    case '#': {
                        if(wasEscape) {
                            wasEscape = false;
                            content.append(c);
                        }else {
                            if (!content.isEmpty()) {
                                children.add(text.ofPlain(content.removeAll()));
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
                            content.append(c);
                        }else {
                            if (!content.isEmpty()) {
                                children.add(text.ofPlain(content.removeAll()));
                            }
                            beforeChangingStep();
                            state.applyPush(c, true, false, exitOnBrace);
                        }
                        break;
                    }
                    case NConstants.Ntf.SILENT: {
                        if(wasEscape) {
                            wasEscape = false;
                            content.append(c);
                        }else {
                            //ignore!
                        }
                        break;
                    }
                    default: {
                        if(wasEscape) {
                            wasEscape = false;
                            content.append('\\');
                            content.append(c);
                        }else {
//                            if(c=='}' && exitOnBrace){
//                                logErr("encountered '}' whiled expected '#'. force closing styled text");
//                            }else {
                                content.append(c);
//                            }
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
                            state.applyPopReplay(this, c);
                        }else{
                            content.append(CoreStringUtils.fillString("#", sharpsEndCount));
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
                    case NConstants.Ntf.SILENT: {
                        if (sharpsStartCount == sharpsEndCount) {
                            //got the end!
                            if (curState == CurState.SHARP_CONTENT_SHARP) {
                                curState = CurState.SHARP_CONTENT_SHARP_END;
                            } else if (curState == CurState.SHARP2_COL_NAME_CONTENT_SHARP) {
                                curState = CurState.SHARP2_COL_NAME_CONTENT_SHARP_END;
                            }
                            state.applyPop(this);
                        } else {
                            logErr("expected " + CoreStringUtils.fillString("#", sharpsStartCount) + "<END>");
                            sharpsEndCount=0;
                            children.add(text.ofPlain(CoreStringUtils.fillString("#", sharpsStartCount)));
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
                            state.applyPopReplay(this, c);
                        } else {
                            if (curState == CurState.SHARP_CONTENT_SHARP) {
                                curState = CurState.SHARP_CONTENT;
                            } else if (curState == CurState.SHARP2_COL_NAME_CONTENT_SHARP) {
                                curState = CurState.SHARP2_COL_NAME_CONTENT;
                            }
                            sharpsEndCount=0;
                            beforeChangingStep();
                            state.applyPush(new StyledParserStep("#", false, workspace, state,false));
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
                            name.append(c);
                        }else {
                            wasEscape = true;
                        }
                        break;
                    }
                    case ':': {
                        if(wasEscape){
                            wasEscape = false;
                            name.append(c);
                        }else {
                            if(curState==CurState.SHARP2_COL_NAME) {
                                curState = CurState.SHARP2_COL_NAME_CONTENT;
                            }else {
                                curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                            }
                        }
                        break;
                    }
                    case NConstants.Ntf.SILENT:
                    case '#':
                    case '{':
                    case '}':
                    case '(':
                    case ')':
                    case '[':
                    case ']': {
                        if(wasEscape){
                            wasEscape = false;
                            if(c== NConstants.Ntf.SILENT) {
                                name.append(c);
                            }else{
                                name.append('\\');
                                name.append(c);
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
                                name.append(c);
                            } else {
                                name.append('\\');
                                name.append(c);
                            }
                            wasEscape=false;
                        }else {
                            if (c <= 32) {
                                if (name.isEmpty()) {
                                    //ignore
                                } else {
                                    if(curState==CurState.SHARP2_COL_NAME) {
                                        curState = CurState.SHARP2_COL_NAME_CONTENT;
                                    }else {
                                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                                    }
                                }
                            } else {
                                name.append(c);
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
                            content.append(c);
                        }else {
                            if (!content.isEmpty()) {
                                children.add(text.ofPlain(content.removeAll()));
                            }
                            beforeChangingStep();
                            state.applyPush(new StyledParserStep("#", false, workspace, state,true));
                        }
                        break;
                    }
                    case '}': {
                        if(wasEscape){
                            wasEscape=false;
                            content.append(c);
                        }else {
                            if (!content.isEmpty()) {
                                children.add(text.ofPlain(content.removeAll()));
                            }
                            curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE;
                        }
                        break;

                    }
                    case NConstants.Ntf.SILENT: {
                        if(wasEscape){
                            wasEscape=false;
                            content.append(c);
                        }else {
                            //ignore
                            if (!content.isEmpty()) {
                                children.add(text.ofPlain(content.removeAll()));
                            }
                        }
                        break;
                    }
                    case '`': {
                        if(wasEscape){
                            wasEscape=false;
                            content.append(c);
                        }else {
                            if (!content.isEmpty()) {
                                children.add(text.ofPlain(content.removeAll()));
                            }
                            beforeChangingStep();
                            state.applyPush(c, true, false, exitOnBrace);
                        }
                        break;
                    }
                    default: {
                        if(wasEscape){
                            wasEscape=false;
                            content.append('\\');
                            content.append(c);
                        }else {
                            content.append(c);
                        }
                    }
                }
                break;
            }
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE: {
                switch (c) {
                    case '\\': {
                        content.append('{');
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
                        children.add(text.ofPlain("}"));
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        beforeChangingStep();
                        state.applyPush(c, true, false, exitOnBrace);
                        break;
                    }
                    case NConstants.Ntf.SILENT: {
                        children.add(text.ofPlain("}"));
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        //ignore
                        break;
                    }
                    default: {
                        content.append(c);
                    }
                }
                break;
            }
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP: {
                switch (c) {
                    case '\\': {
                        sharpsEndCount=0;
                        content.append('{');
                        content.append('#');
                        curState=CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        wasEscape=true;
                        break;
                    }
                    case '#': {
                        sharpsEndCount++;
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP2_END;
                        state.applyPop(this);
                        break;
                    }
                    case '`': {
                        sharpsEndCount=0;
                        children.add(text.ofPlain("}#"));
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        beforeChangingStep();
                        state.applyPush(c, true, false, exitOnBrace);
                        break;
                    }
                    case NConstants.Ntf.SILENT: {
                        sharpsEndCount=0;
                        children.add(text.ofPlain("}#"));
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        //ignore
                        break;
                    }
                    default: {
                        sharpsEndCount=0;
                        children.add(text.ofPlain("}#"));
                        curState = CurState.SHARP2_OBRACE_NAME_COL_CONTENT;
                        content.append(c);
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
                        beforeChangingStep();
                        state.applyPush(new StyledParserStep("#", false, workspace, state,false));
                        for (int i = 0; i < _sharpsEndCount - 1; i++) {
                            state.applyNextChar('#');
                        }
                        state.applyPopReplay(this, c);
                        break;
                    }
                    default:{
                        if (!content.isEmpty()) {
                            children.add(text.ofPlain(content.removeAll()));
                        }
                        state.applyPopReplay(this, c);
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
                        beforeChangingStep();
                        state.applyPush(new StyledParserStep("#", false, workspace, state,false));
                        for (int i = 0; i < _sharpsEndCount - 1; i++) {
                            state.applyNextChar('#');
                        }
                        state.applyPopReplay(this, c);
                        break;
                    }
                    default:{
                        if (!content.isEmpty()) {
                            children.add(text.ofPlain(content.removeAll()));
                        }
                        state.applyPopReplay(this, c);
                    }
                }
                break;
            }
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP2_END:{
                if (!content.isEmpty()) {
                    children.add(text.ofPlain(content.removeAll()));
                }
                state.applyPopReplay(this, c);
                break;
            }
            default: {
                throw new IllegalArgumentException("unexpected " + curState);
            }
        }
    }

    private void beforeChangingStep() {
        charQueue.clear();
    }

    @Override
    public void appendChild(ParserStep tt) {
        NText n = tt.toText();
        if(n instanceof NTextPlain
            && !children.isEmpty()
            && children.get(children.size()-1) instanceof NTextPlain) {
            //consecutive plain text
            NTextPlain p1=(NTextPlain) children.remove(children.size()-1);
            NTextPlain p2=(NTextPlain) n;
            children.add(new DefaultNTextPlain(
                    workspace,p1.getText()+p2.getText()
            ));
        }else{
            children.add(n);
        }
    }

    @Override
    public NText toText() {
        List<NText> childrenTextNodes2 = new ArrayList<>(children);
        NTexts text = NTexts.of();
        if (!content.isEmpty()) {
            childrenTextNodes2.add(text.ofPlain(content.toString()));
        }

        NText a = text.ofList(childrenTextNodes2.toArray(new NText[0])).simplify();
        if(a==null){
            return  text.ofPlain("");
        }
        switch (curState) {
            case SHARP:
            {
                return text.ofPlain("#");
            }
            case EMPTY:{
                return text.ofPlain("");
            }
            case SHARP_CONTENT:
            case SHARP_CONTENT_SHARP:
            case SHARP_CONTENT_SHARP_END:
            {
                return text.ofStyled(a, NTextStyle.primary(sharpsStartCount));
            }
            case SHARP2_OBRACE_NAME:{
                return text.ofPlain("##{"+ name.toString());
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
                NTextStyles s = parseHelper.parseSimpleNutsTextStyles(name.toString());
                if (s != null) {
                    return text.ofStyled(a, s);
                }
                throw new NIllegalArgumentException(NMsg.ofC("unable to resolve style from %s",name.toString()));
            }
        }
        throw new NUnsupportedEnumException(curState);
    }

    @Override
    public void end(DefaultNTextNodeParser.State p) {
        p.applyPop(this);
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
        if(NDebugString.of(NBootManager.of().getBootOptions().getDebug().orNull()).isEnabled()) {
            workspace.currentSession().err().println(s);
        }
    }

    @Override
    public String toString() {
        String contentString = (children.stream().map(Object::toString).collect(Collectors.joining()))
                + content;
        switch (curState){
            case EMPTY:return "";
            case SHARP:return CoreStringUtils.fillString("#", sharpsStartCount);
            case SHARP_CONTENT:return CoreStringUtils.fillString("#", sharpsStartCount)+contentString;
            case SHARP_CONTENT_SHARP:
            case SHARP_CONTENT_SHARP_END:return CoreStringUtils.fillString("#", sharpsStartCount)+contentString+CoreStringUtils.fillString("#", sharpsEndCount);
            case SHARP2_COL_NAME:return CoreStringUtils.fillString("#", sharpsStartCount)+":"+name;
            case SHARP2_COL_NAME_SHARP1:return CoreStringUtils.fillString("#", sharpsStartCount)+":"+name+"#";
            case SHARP2_COL_NAME_SHARP2:return CoreStringUtils.fillString("#", sharpsStartCount)+":"+name+"##";
            case SHARP2_COL_NAME_CONTENT:return CoreStringUtils.fillString("#", sharpsStartCount)+":"+name+":"+contentString;
            case SHARP2_COL_NAME_CONTENT_SHARP:
            case SHARP2_COL_NAME_CONTENT_SHARP_END:return CoreStringUtils.fillString("#", sharpsStartCount)+":"+name+":"+contentString+CoreStringUtils.fillString("#", sharpsEndCount);
            case SHARP2_OBRACE_NAME:return CoreStringUtils.fillString("#", sharpsStartCount)+"{"+name;
            case SHARP2_OBRACE_NAME_COL_CONTENT:return CoreStringUtils.fillString("#", sharpsStartCount)+"{"+name+":"+contentString;
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE:return CoreStringUtils.fillString("#", sharpsStartCount)+"{"+name+":"+content+"}";
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP:
            case SHARP2_OBRACE_NAME_COL_CONTENT_CBRACE_SHARP2_END:return CoreStringUtils.fillString("#", sharpsStartCount)+"{"+name+":"+contentString+"}"+CoreStringUtils.fillString("#", sharpsEndCount);
            default:return "<unexpected>";
        }
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
//        StringBuilder sb = new StringBuilder();
//        for (NutsText parserStep : childrenTextNodes) {
//            sb.append(parserStep.toString());
//        }
//        return sb.toString();
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
