package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;


public abstract class AbstractFilterParser2<T> {
    protected StringReaderExt str;
    Set<String> preOps=new LinkedHashSet<>();
    Set<String> binOps=new LinkedHashSet<>();
    boolean acceptPars=true;
    String implicitOperator=null;

    public AbstractFilterParser2(String str) {
        this.str = new StringReaderExt(str);
    }

    protected void addBoolOps(){
        binOps.addAll(
                Arrays.asList("&&", "||", "&", "|")
        );
        preOps.add("!");
        acceptPars=true;
    }

    protected void skipWhites(){
        while(str.hasNext() && Character.isWhitespace(str.peekChar())){
            str.nextChar();
        }
    }

    protected abstract T nextDefault();

    public T parse(){
        T u = next();
        if(u!=null){
            return u;
        }
        return nextDefault();
    }

    private T next(){
        return next(-1);
    }

    private T next(int precedence){
        T a = null;
        while(true) {
            skipWhites();
            String preOp = peekPreOp();
            if(preOp!=null){
                T x = next(precedence);
                a = buildPreOp(preOp,x);
            }else if(acceptPars && str.readString("(")){
                a = next(-1);
                skipWhites();
                if (!str.readString(")")) {
                    throw new IllegalArgumentException("expected ')'");
                }
            }else if(acceptPars && str.readString(")")){
                break;
            }else{
                String w = nextWord();
                if(w!=null){
                    a = wordToPredicate(w);
                    String op = peekBinOp();
                    if(op==null && implicitOperator!=null){
                        op=implicitOperator;
                    }
                    if(op!=null){
                        int oprec=getOpPrecedence(op);
                        if(oprec<precedence){
                            break;
                        }
                        T r = next(precedence);
                        a= buildBinOp(op,a,r);
                    }else{
                        throw new IllegalArgumentException("expected operator at: "+str.peekChars(5)+"...");
                    }
                }else{
                    throw new IllegalArgumentException("unexpected at: "+str.peekChars(5)+"...");
                }
            }
        }
        return a;
    }

    protected abstract T buildPreOp(String op, T a) ;

    protected abstract T buildBinOp(String op, T a, T r) ;

    protected int getOpPrecedence(String op) {
        switch (op){
            case "&":
            case "&&":{
                return 1;
            }
            case "|":
            case "||":{
                return 2;
            }
        }
        return -1;
    }

    private String peekBinOp(){
        for (String s : binOps) {
            if(str.peekChars(s)){
                return s;
            }
        }
        return null;
    }

    private String peekPreOp(){
        for (String s : preOps) {
            if(str.peekChars(s)){
                return s;
            }
        }
        return null;
    }

    protected String nextWord(){
        if(str.hasNext() && Character.isLetterOrDigit(str.peekChar())){
           StringBuilder sb=new StringBuilder();
           while(str.hasNext() && (Character.isLetterOrDigit(str.peekChar()) || str.peekChar()=='_')){
               sb.append(str.nextChar());
           }
           return sb.toString();
        }
        return null;
    }

    protected abstract T wordToPredicate(String word);

}
