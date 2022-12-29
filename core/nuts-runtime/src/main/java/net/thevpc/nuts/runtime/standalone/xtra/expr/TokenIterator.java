package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NSession;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Iterator;

class TokenIterator implements Iterator<NToken> {
    private final StreamTokenizerExt st;
    private NToken previous;
    private boolean returnSpace = false;
    private boolean returnComment = false;
    private boolean doReplay;

    public TokenIterator(Reader r, NSession session) {
        this.st = new StreamTokenizerExt(r, session);
        this.st.ordinaryChar('.');
    }

    public void pushBack() {
        doReplay = true;
    }

    public NToken peek() {
        if (doReplay) {
            return previous;
        }
        if (hasNext()) {
            NToken n = next();
            doReplay = true;
            return n;
        }
        return null;
    }

    public NToken read() {
        if (hasNext()) {
            return next();
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        if (doReplay) {
            return true;
        }
        while (true) {
            int nt = st.nextToken();
            switch (nt) {
                case StreamTokenizer.TT_EOF: {
                    previous = null;
                    return false;
                }
                case ' ':
                case '\t':
                case StreamTokenizer.TT_EOL: {
                    if (returnSpace) {
                        previous = new NToken(NToken.TT_SPACE, st.sval, 0, st.lineno());
                        return true;
                    }
                    break;
                }
                case '&':{
                    int i = st.nextToken();
                    if(i==StreamTokenizer.TT_EOF){
                        previous = new NToken(nt, "&", 0, st.lineno());
                        return true;
                    }else if(i=='&'){
                        previous = new NToken(NToken.TT_AND, "&&", 0, st.lineno());
                        return true;
                    }else{
                        st.pushBack();
                        previous = new NToken(nt, "&", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '|':{
                    int i = st.nextToken();
                    if(i==StreamTokenizer.TT_EOF){
                        previous = new NToken(nt, "|", 0, st.lineno());
                        return true;
                    }else if(i=='|'){
                        previous = new NToken(NToken.TT_OR, "||", 0, st.lineno());
                        return true;
                    }else{
                        st.pushBack();
                        previous = new NToken(nt, "|", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '?':{
                    int i = st.nextToken();
                    if(i==StreamTokenizer.TT_EOF){
                        previous = new NToken(nt, "?", 0, st.lineno());
                        return true;
                    }else if(i=='?'){
                        previous = new NToken(NToken.TT_COALESCE, "??", 0, st.lineno());
                        return true;
                    }else{
                        st.pushBack();
                        previous = new NToken(nt, "?", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '.':{
                    int i = st.nextToken();
                    if(i==StreamTokenizer.TT_EOF){
                        previous = new NToken(nt, ".", 0, st.lineno());
                        return true;
                    }else if(i=='.'){
                        i = st.nextToken();
                        if(i==StreamTokenizer.TT_EOF){
                            previous = new NToken(NToken.TT_DOTS2, "..", 0, st.lineno());
                        }else if(i=='.'){
                            previous = new NToken(NToken.TT_DOTS3, "...", 0, st.lineno());
                        }else{
                            st.pushBack();
                            previous = new NToken(NToken.TT_DOTS2, "..", 0, st.lineno());
                        }
                        return true;
                    }else{
                        st.pushBack();
                        previous = new NToken(nt, ".", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '<':{
                    int i = st.nextToken();
                    if(i=='<'){
                        i = st.nextToken();
                        if(i=='<') {
                            previous = new NToken(NToken.TT_LEFT_SHIFT_UNSIGNED, "<<<", 0, st.lineno());
                            return true;
                        }else if(i=='>'){
                            previous = new NToken(NToken.TT_LTGT, "<>", 0, st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NToken(NToken.TT_LEFT_SHIFT, "<<", 0, st.lineno());
                            return true;
                        }
                    }else if(i=='='){
                        previous = new NToken(NToken.TT_LTE, "<=", 0, st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = new NToken(nt, "<", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '>':{
                    int i = st.nextToken();
                    if(i=='>'){
                        i = st.nextToken();
                        if(i=='>'){
                            previous = new NToken(NToken.TT_RIGHT_SHIFT_UNSIGNED, ">>>", 0, st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NToken(NToken.TT_RIGHT_SHIFT, ">>", 0, st.lineno());
                            return true;
                        }
                    }else if(i=='='){
                        previous = new NToken(NToken.TT_GTE, ">=", 0, st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = new NToken(nt, ">", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '=':{
                    int i = st.nextToken();
                    if(i=='='){
                        i = st.nextToken();
                        if(i=='='){
                            previous = new NToken(NToken.TT_EQ3, "===", 0, st.lineno());
                            return true;
                        }else if(i=='>'){
                            previous = new NToken(NToken.TT_RIGHT_ARROW2, "==>", 0, st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NToken(NToken.TT_EQ2, "==", 0, st.lineno());
                            return true;
                        }
                    }else if(i=='>'){
                        previous = new NToken(NToken.TT_RIGHT_ARROW, "=>", 0, st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = new NToken(nt, "=", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '~':{
                    int i = st.nextToken();
                    if(i=='~'){
                        i = st.nextToken();
                        if(i=='~'){
                            previous = new NToken(NToken.TT_LIKE3, "~~~", 0, st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NToken(NToken.TT_LIKE2, "~~", 0, st.lineno());
                            return true;
                        }
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = new NToken(nt, "~", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '!':{
                    int i = st.nextToken();
                    if(i=='!'){
                        i = st.nextToken();
                        if(i=='!'){
                            previous = new NToken(NToken.TT_NOT3, "!!!", 0, st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NToken(NToken.TT_NOT2, "!!", 0, st.lineno());
                            return true;
                        }
                    }else if(i=='='){
                        i = st.nextToken();
                        if(i=='='){
                            previous = new NToken(NToken.TT_NEQ2, "!==", 0, st.lineno());
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NToken(NToken.TT_NEQ, "!=", 0, st.lineno());
                        }
                        return true;
                    }else if(i=='~'){
                        previous = new NToken(NToken.TT_NOT_LIKE, "!~", 0, st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = new NToken(nt, "!", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                default: {
                    switch (st.ttype) {
                        case ' ':
                        case '\n':
                        case '\r':
                        case '\t': {
                            if (returnSpace) {
                                previous = new NToken(NToken.TT_SPACE, st.sval, 0, st.lineno());
                                return true;
                            }
                            break;
                        }
                        case '\"':
                        case '\'': {
                            String sval = st.sval;
                            previous = new NToken(NToken.TT_STRING_LITERAL, sval, 0, st.lineno());
                            return true;
                        }
                        case NToken.TT_INT:
                        case NToken.TT_LONG:
                        case NToken.TT_BIG_INT:
                        case NToken.TT_FLOAT:
                        case NToken.TT_DOUBLE:
                        case NToken.TT_BIG_DECIMAL: {
                            previous = new NToken(st.ttype, st.sval, st.nval, st.lineno());
                            return true;
                        }
                        case NToken.TT_WORD: {
                            String s = st.sval;
                            previous = new NToken(st.ttype, s, 0, st.lineno());
                            return true;
                        }
                        default: {
                            String s = st.image;
                            previous = new NToken(st.ttype, s, 0, st.lineno());
                            return true;
                        }
                    }
                }
            }
        }
    }

    @Override
    public NToken next() {
        if (doReplay) {
            doReplay = false;
        }
        return previous;
    }
}
