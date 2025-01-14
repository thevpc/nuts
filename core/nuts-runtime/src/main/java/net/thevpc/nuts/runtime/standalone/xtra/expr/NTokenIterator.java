package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NToken;
import net.thevpc.nuts.util.NStreamTokenizer;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Iterator;

public class NTokenIterator implements Iterator<NToken> {
    private final NStreamTokenizer st;
    private NToken previous;
    private boolean returnSpace = false;
    private boolean returnComment = false;
    private boolean doReplay;

    public NTokenIterator(String r, NWorkspace workspace) {
        this(new StringReader(r==null?"":r),workspace);
    }

    public NTokenIterator(Reader r, NWorkspace workspace) {
        this.st = new NStreamTokenizer(r);
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
                case NToken.TT_EOF: {
                    previous = null;
                    return false;
                }
                case ' ':
                case '\t':
                case NToken.TT_EOL: {
                    if (returnSpace) {
                        previous = NToken.ofStr(NToken.TT_SPACE, st.sval,"SPACE", st.lineno());
                        return true;
                    }
                    break;
                }
                case '&':{
                    int i = st.nextToken();
                    if(i==NToken.TT_EOF){
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }else if(i=='&'){
                        previous = NToken.ofStr(NToken.TT_AND, "&&","AND", st.lineno());
                        return true;
                    }else{
                        st.pushBack();
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '|':{
                    int i = st.nextToken();
                    if(i==StreamTokenizer.TT_EOF){
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }else if(i=='|'){
                        previous = NToken.ofStr(NToken.TT_OR, "||","OR", st.lineno());
                        return true;
                    }else{
                        st.pushBack();
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '?':{
                    int i = st.nextToken();
                    if(i==StreamTokenizer.TT_EOF){
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }else if(i=='?'){
                        previous = NToken.ofStr(NToken.TT_COALESCE, "??", "COALESCE", st.lineno());
                        return true;
                    }else{
                        st.pushBack();
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '.':{
                    int i = st.nextToken();
                    if(i==StreamTokenizer.TT_EOF){
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }else if(i=='.'){
                        i = st.nextToken();
                        if(i==StreamTokenizer.TT_EOF){
                            previous = NToken.ofStr(NToken.TT_DOTS2, "..", "DOTS2", st.lineno());
                        }else if(i=='.'){
                            previous = NToken.ofStr(NToken.TT_DOTS3, "...", "DOTS3", st.lineno());
                        }else{
                            st.pushBack();
                            previous = NToken.ofStr(NToken.TT_DOTS2, "..", "DOTS2", st.lineno());
                        }
                        return true;
                    }else{
                        st.pushBack();
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '<':{
                    int i = st.nextToken();
                    if(i=='<'){
                        i = st.nextToken();
                        if(i=='<') {
                            previous = NToken.ofStr(NToken.TT_LEFT_SHIFT_UNSIGNED, "<<<", "LEFT_SHIFT_UNSIGNED", st.lineno());
                            return true;
                        }else if(i=='>'){
                            previous = NToken.ofStr(NToken.TT_LTGT, "<>", "LTGT", st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = NToken.ofStr(NToken.TT_LEFT_SHIFT, "<<", "LEFT_SHIFT", st.lineno());
                            return true;
                        }
                    }else if(i=='='){
                        previous = NToken.ofStr(NToken.TT_LTE, "<=", "LTE", st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '>':{
                    int i = st.nextToken();
                    if(i=='>'){
                        i = st.nextToken();
                        if(i=='>'){
                            previous = NToken.ofStr(NToken.TT_RIGHT_SHIFT_UNSIGNED, ">>>", "RIGHT_SHIFT_UNSIGNED", st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = NToken.ofStr(NToken.TT_RIGHT_SHIFT, ">>", "RIGHT_SHIFT", st.lineno());
                            return true;
                        }
                    }else if(i=='='){
                        previous = NToken.ofStr(NToken.TT_GTE, ">=", "GTE", st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '=':{
                    int i = st.nextToken();
                    if(i=='='){
                        i = st.nextToken();
                        if(i=='='){
                            previous = NToken.ofStr(NToken.TT_EQ3, "===", "EQ3", st.lineno());
                            return true;
                        }else if(i=='>'){
                            previous = NToken.ofStr(NToken.TT_RIGHT_ARROW2, "==>", "RIGHT_ARROW2", st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = NToken.ofStr(NToken.TT_EQ2, "==", "EQ2", st.lineno());
                            return true;
                        }
                    }else if(i=='>'){
                        previous = NToken.ofStr(NToken.TT_RIGHT_ARROW, "=>", "RIGHT_ARROW", st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '~':{
                    int i = st.nextToken();
                    if(i=='~'){
                        i = st.nextToken();
                        if(i=='~'){
                            previous = NToken.ofStr(NToken.TT_LIKE3, "~~~", "LIKE3", st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = NToken.ofStr(NToken.TT_LIKE2, "~~", "LIKE2", st.lineno());
                            return true;
                        }
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = NToken.ofChar((char)nt, st.lineno());
                        return true;
                    }
//                    break;
                }
                case '!':{
                    int i = st.nextToken();
                    if(i=='!'){
                        i = st.nextToken();
                        if(i=='!'){
                            previous = NToken.ofStr(NToken.TT_NOT3, "!!!", "NOT3", st.lineno());
                            return true;
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = NToken.ofStr(NToken.TT_NOT2, "!!", "NOT2", st.lineno());
                            return true;
                        }
                    }else if(i=='='){
                        i = st.nextToken();
                        if(i=='='){
                            previous = NToken.ofStr(NToken.TT_NEQ2, "!==", "NEQ2", st.lineno());
                        }else{
                            if(i!=StreamTokenizer.TT_EOF) {
                                st.pushBack();
                            }
                            previous = NToken.ofStr(NToken.TT_NEQ, "!=", "NEQ", st.lineno());
                        }
                        return true;
                    }else if(i=='~'){
                        previous = NToken.ofStr(NToken.TT_NOT_LIKE, "!~", "NOT_LIKE", st.lineno());
                        return true;
                    }else{
                        if(i!=StreamTokenizer.TT_EOF) {
                            st.pushBack();
                        }
                        previous = NToken.ofChar((char) nt, st.lineno());
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
                                previous = NToken.of(NToken.TT_SPACE, st.sval, 0, st.lineno(), st.image,"SPACE");
                                return true;
                            }
                            break;
                        }
                        case '\"':{
                            String sval = st.sval;
                            previous = NToken.of(NToken.TT_STRING_LITERAL, sval, 0, st.lineno(),st.image,"DOUBLE_QUOTED_STRING_LITERAL");
                            return true;
                        }
                        case '\'': {
                            String sval = st.sval;
                            previous = NToken.of(NToken.TT_STRING_LITERAL, sval, 0, st.lineno(),st.image,"SIMPLE_QUOTED_STRING_LITERAL");
                            return true;
                        }
                        case NToken.TT_ISTR_DQ: {
                            String sval = st.sval;
                            previous = NToken.of(NToken.TT_STRING_LITERAL, sval, 0, st.lineno(),st.image,"INTERPOLATED_DBL_QUOTED_STRING_LITERAL");
                            return true;
                        }
                        case NToken.TT_ISTR_SQ: {
                            String sval = st.sval;
                            previous = NToken.of(NToken.TT_STRING_LITERAL, sval, 0, st.lineno(),st.image,"INTERPOLATED_SIMPLE_QUOTED_STRING_LITERAL");
                            return true;
                        }
                        case NToken.TT_INT:{
                            previous = NToken.of(st.ttype, st.sval, st.nval, st.lineno(), st.image,"INT");
                            return true;
                        }
                        case NToken.TT_LONG:{
                            previous = NToken.of(st.ttype, st.sval, st.nval, st.lineno(), st.image,"LONG");
                            return true;
                        }
                        case NToken.TT_BIG_INT:{
                            previous = NToken.of(st.ttype, st.sval, st.nval, st.lineno(), st.image,"BIG_INT");
                            return true;
                        }
                        case NToken.TT_FLOAT:{
                            previous = NToken.of(st.ttype, st.sval, st.nval, st.lineno(), st.image,"FLOAT");
                            return true;
                        }
                        case NToken.TT_DOUBLE:{
                            previous = NToken.of(st.ttype, st.sval, st.nval, st.lineno(), st.image,"DOUBLE");
                            return true;
                        }
                        case NToken.TT_BIG_DECIMAL: {
                            previous = NToken.of(st.ttype, st.sval, st.nval, st.lineno(), st.image,"BIG_DECIMAL");
                            return true;
                        }
                        case NToken.TT_WORD: {
                            previous = NToken.ofStr(st.ttype, st.sval,"WORD", st.lineno());
                            return true;
                        }
                        default: {
                            previous = NToken.ofSpecial(st.ttype, st.image, st.lineno());
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
