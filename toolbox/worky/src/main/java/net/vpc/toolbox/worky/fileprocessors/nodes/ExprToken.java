package net.vpc.toolbox.worky.fileprocessors.nodes;

import java.io.StreamTokenizer;

public class ExprToken {
    public int ttype;
    public Object value;

    public ExprToken(int ttype, Object value) {
        this.ttype = ttype;
        this.value = value;
    }

    @Override
    public String toString() {
        switch (ttype){
            case StreamTokenizer
                    .TT_EOF:{
                return "<EOF>";
            }
            case StreamTokenizer
                    .TT_EOL:{
                return "<EOL>";
            }
            case StreamTokenizer
                    .TT_NUMBER:{
                return String.valueOf(value);
            }
            case StreamTokenizer
                    .TT_WORD:{
                return String.valueOf(value);
            }
            case '\"':{
                return "\""+String.valueOf(value)+"\"";
            }
            case '\'':{
                return "'"+String.valueOf(value)+"'";
            }
        }
        if(ttype>0){
            return "'"+(char)ttype+"'";
        }
        return String.valueOf((char)ttype)+" : "+value;
    }
}
