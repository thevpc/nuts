package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsSession;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Iterator;

class TokenIterator implements Iterator<NutsToken> {
    private final StreamTokenizerExt st;
    private NutsToken previous;
    private boolean returnSpace = false;
    private boolean returnComment = false;
    private boolean doReplay;

    public TokenIterator(Reader r, NutsSession session) {
        this.st = new StreamTokenizerExt(r, session);
        this.st.ordinaryChar('.');
    }

    public void pushBack() {
        doReplay = true;
    }

    public NutsToken peek() {
        if (doReplay) {
            return previous;
        }
        if (hasNext()) {
            NutsToken n = next();
            doReplay = true;
            return n;
        }
        return null;
    }

    public NutsToken read() {
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
                        previous = new NutsToken(NutsToken.TT_SPACE, st.sval, 0, st.lineno());
                        return true;
                    }
                    break;
                }
                case '&':{
                    int i = st.nextToken();
                    if(i==StreamTokenizer.TT_EOF){
                        previous = new NutsToken(nt, "&", 0, st.lineno());
                        return true;
                    }else if(i=='&'){
                        previous = new NutsToken(NutsToken.TT_AND, "&&", 0, st.lineno());
                        return true;
                    }else{
                        st.pushBack();
                        previous = new NutsToken(nt, "&", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '|':{
                    int i = st.nextToken();
                    if(i==StreamTokenizer.TT_EOF){
                        previous = new NutsToken(nt, "|", 0, st.lineno());
                        return true;
                    }else if(i=='|'){
                        previous = new NutsToken(NutsToken.TT_OR, "||", 0, st.lineno());
                        return true;
                    }else{
                        st.pushBack();
                        previous = new NutsToken(nt, "|", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '?':{
                    int i = st.nextToken();
                    if(i==StreamTokenizer.TT_EOF){
                        previous = new NutsToken(nt, "?", 0, st.lineno());
                        return true;
                    }else if(i=='?'){
                        previous = new NutsToken(NutsToken.TT_COALESCE, "??", 0, st.lineno());
                        return true;
                    }else{
                        st.pushBack();
                        previous = new NutsToken(nt, "?", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '.':{
                    int i = st.nextToken();
                    if(i==StreamTokenizer.TT_EOF){
                        previous = new NutsToken(nt, ".", 0, st.lineno());
                        return true;
                    }else if(i=='.'){
                        i = st.nextToken();
                        if(i==StreamTokenizer.TT_EOF){
                            previous = new NutsToken(NutsToken.TT_DOTS2, "..", 0, st.lineno());
                        }else if(i=='.'){
                            previous = new NutsToken(NutsToken.TT_DOTS3, "...", 0, st.lineno());
                        }else{
                            st.pushBack();
                            previous = new NutsToken(NutsToken.TT_DOTS2, "..", 0, st.lineno());
                        }
                        return true;
                    }else{
                        st.pushBack();
                        previous = new NutsToken(nt, ".", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '<':{
                    int i = st.nextToken();
                    if(i=='<'){
                        i = st.nextToken();
                        if(i=='<') {
                            previous = new NutsToken(NutsToken.TT_LEFT_SHIFT_UNSIGNED, "<<<", 0, st.lineno());
                            return true;
                        }else if(i=='>'){
                            previous = new NutsToken(NutsToken.TT_LTGT, "<>", 0, st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NutsToken(NutsToken.TT_LEFT_SHIFT, "<<", 0, st.lineno());
                            return true;
                        }
                    }else if(i=='='){
                        previous = new NutsToken(NutsToken.TT_LTE, "<=", 0, st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = new NutsToken(nt, "<", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '>':{
                    int i = st.nextToken();
                    if(i=='>'){
                        i = st.nextToken();
                        if(i=='>'){
                            previous = new NutsToken(NutsToken.TT_RIGHT_SHIFT_UNSIGNED, ">>>", 0, st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NutsToken(NutsToken.TT_RIGHT_SHIFT, ">>", 0, st.lineno());
                            return true;
                        }
                    }else if(i=='='){
                        previous = new NutsToken(NutsToken.TT_GTE, ">=", 0, st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = new NutsToken(nt, ">", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '=':{
                    int i = st.nextToken();
                    if(i=='='){
                        i = st.nextToken();
                        if(i=='='){
                            previous = new NutsToken(NutsToken.TT_EQ3, "===", 0, st.lineno());
                            return true;
                        }else if(i=='>'){
                            previous = new NutsToken(NutsToken.TT_RIGHT_ARROW2, "==>", 0, st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NutsToken(NutsToken.TT_EQ2, "==", 0, st.lineno());
                            return true;
                        }
                    }else if(i=='>'){
                        previous = new NutsToken(NutsToken.TT_RIGHT_ARROW, "=>", 0, st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = new NutsToken(nt, "=", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '~':{
                    int i = st.nextToken();
                    if(i=='~'){
                        i = st.nextToken();
                        if(i=='~'){
                            previous = new NutsToken(NutsToken.TT_LIKE3, "~~~", 0, st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NutsToken(NutsToken.TT_LIKE2, "~~", 0, st.lineno());
                            return true;
                        }
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = new NutsToken(nt, "~", 0, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '!':{
                    int i = st.nextToken();
                    if(i=='!'){
                        i = st.nextToken();
                        if(i=='!'){
                            previous = new NutsToken(NutsToken.TT_NOT3, "!!!", 0, st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NutsToken(NutsToken.TT_NOT2, "!!", 0, st.lineno());
                            return true;
                        }
                    }else if(i=='='){
                        i = st.nextToken();
                        if(i=='='){
                            previous = new NutsToken(NutsToken.TT_NEQ2, "!==", 0, st.lineno());
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = new NutsToken(NutsToken.TT_NEQ, "!=", 0, st.lineno());
                        }
                        return true;
                    }else if(i=='~'){
                        previous = new NutsToken(NutsToken.TT_NOT_LIKE, "!~", 0, st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = new NutsToken(nt, "!", 0, st.lineno());
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
                                previous = new NutsToken(NutsToken.TT_SPACE, st.sval, 0, st.lineno());
                                return true;
                            }
                            break;
                        }
                        case '\"':
                        case '\'': {
                            String sval = st.sval;
                            previous = new NutsToken(NutsToken.TT_STRING_LITERAL, sval, 0, st.lineno());
                            return true;
                        }
                        case NutsToken.TT_INT:
                        case NutsToken.TT_LONG:
                        case NutsToken.TT_BIG_INT:
                        case NutsToken.TT_FLOAT:
                        case NutsToken.TT_DOUBLE:
                        case NutsToken.TT_BIG_DECIMAL: {
                            previous = new NutsToken(st.ttype, st.sval, st.nval, st.lineno());
                            return true;
                        }
                        case NutsToken.TT_WORD: {
                            String s = st.sval;
                            previous = new NutsToken(st.ttype, s, 0, st.lineno());
                            return true;
                        }
                        default: {
                            String s = st.sval;
                            if (st.ttype >= 0) {
                                if (st.ttype >= 32) {
                                    s = String.valueOf((char) st.ttype);
                                } else {
                                    s = null;
                                }
                            }
                            previous = new NutsToken(st.ttype, s, 0, st.lineno());
                            return true;
                        }
                    }
                }
            }
        }
    }

    @Override
    public NutsToken next() {
        if (doReplay) {
            doReplay = false;
        }
        return previous;
    }
}
