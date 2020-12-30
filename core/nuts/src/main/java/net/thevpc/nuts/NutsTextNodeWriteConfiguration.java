package net.thevpc.nuts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @category Format
 */
public class NutsTextNodeWriteConfiguration {
    private boolean filtered;
    private boolean numberTitles;
    private Seq seq;

    public boolean isNumberTitles() {
        return numberTitles;
    }

    public NutsTextNodeWriteConfiguration setNumberTitles(boolean numberTitles) {
        this.numberTitles = numberTitles;
        return this;
    }

    public Seq getSeq() {
        return seq;
    }

    public NutsTextNodeWriteConfiguration setSeq(Seq seq) {
        this.seq = seq;
        return this;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public NutsTextNodeWriteConfiguration setFiltered(boolean filtered) {
        this.filtered = filtered;
        return this;
    }

    /**
     * @category Format
     */
    public interface Seq {
        TextNumber[] getPattern();
        Seq newLevel(int level);
        TextNumber[] getValue();
        String getString(String delimiter);
//        Seq setValue(TextNumber[] newValue);
    }

    /**
     * @category Format
     */
    public static class DefaultSeq implements Seq {
        private TextNumber[] value;

        public DefaultSeq(String pattern) {
            List<TextNumber> p=new ArrayList<>();
            for (char c : pattern.toCharArray()) {
                switch (c){
                    case '0':
                    case '1':{
                        p.add(new IntTextNumber(0));
                        break;
                    }
                    case 'A':{
                        p.add(AlphabetTextNumber.ofUpperCased());
                        break;
                    }
                    case 'a':{
                        p.add(AlphabetTextNumber.ofLowerCased());
                        break;
                    }
                    default:{
                        throw new UnsupportedOperationException("unsupported sequence type "+pattern+" (error at '"+c+"')");
                    }
                }
            }
            this.value=p.toArray(new TextNumber[0]);
        }

        public DefaultSeq(TextNumber ... pattern) {
            this.value=new TextNumber[pattern.length];
            for (int i = 0; i <pattern.length; i++) {
                this.value[i]=pattern[i];
            }
        }

        @Override
        public TextNumber[] getPattern() {
            TextNumber[] pattern=new TextNumber[value.length];
            for (int i = 0; i <pattern.length; i++) {
                pattern[i]=value[i].none();
            }
            return pattern;
        }

        public TextNumber numberAt(int level,TextNumber[] all0,TextNumber[] all) {
            int mm=Math.min(all0.length,all.length);
            int a=Math.min(level,mm-1);
            for (int i = a; i >=0; i--) {
                if(i<all0.length && all0[i]!=null){
                    return all0[i];
                }
                if(i<all.length && all[i]!=null){
                    return all[i];
                }
            }
            return new IntTextNumber(0);
        }

        public Seq newLevel(int level) {
            level=level-1;
            if(level<=0){
                throw new IllegalArgumentException("Invalid level. must be >= 1");
            }
            int max=level>=value.length?level+1:value.length;
            TextNumber[] pattern=new TextNumber[max];
            for (int i = level+1; i <value.length; i++) {
                pattern[i]=value[i];
            }
            for (int i = 0; i < level; i++) {
                TextNumber nn = numberAt(i, value,pattern);
                if(nn.isNone()){
                    nn=nn.first();
                }
                pattern[i]= nn;
            }
            TextNumber nn = numberAt(level, value,pattern);
            pattern[level]= nn.next();
            for (int i = level+1; i <pattern.length; i++) {
                pattern[i]=value[i].none();
            }
            return setValue(pattern);
        }

        private int depth(){
            for (int i = 0; i < value.length; i++) {
                if(value[i].isNone()){
                    return i;
                }
            }
            return value.length;
        }

        @Override
        public TextNumber[] getValue() {
            List<TextNumber> ok=new ArrayList<>();
            for (int i = 0; i < value.length; i++) {
                if(value[i]==null || value[i].isNone()){
                    break;
                }
                ok.add(value[i]);
            }
            return ok.toArray(new TextNumber[0]);
        }

//        @Override
        public Seq setValue(TextNumber[] newValue) {
            return new DefaultSeq(newValue);
        }

        @Override
        public String getString(String delimiter) {
            return Arrays.stream(getValue()).map(x -> x.toString()).collect(Collectors.joining(delimiter));
        }

        @Override
        public String toString() {
            return getString(".");
        }
    }

    /**
     * @category Format
     */
    public interface TextNumber{
        TextNumber next();
        TextNumber first();
        TextNumber none();
        boolean isNone();
    }

    /**
     * @category Format
     */
    public static class AlphabetTextNumber implements TextNumber{
        String[] names;
        String separator;
        String[] value;

        public static AlphabetTextNumber ofUpperCased() {
            String[] all=new String[26];
            for (int i = 'A'; i <= 'Z'; i++) {
                all[i-'A']=String.valueOf((char)i);
            }
            return new AlphabetTextNumber(all,"",new String[0]);
        }
        public static AlphabetTextNumber ofLowerCased() {
            String[] all=new String[26];
            for (int i = 'a'; i <= 'z'; i++) {
                all[i-'a']=String.valueOf((char)i);
            }
            return new AlphabetTextNumber(all,"",new String[0]);
        }
        public AlphabetTextNumber(String[] names, String separator,String[] value) {
            this.names = names;
            this.separator = separator;
            this.value = value;
        }

        @Override
        public TextNumber none() {
            return new AlphabetTextNumber(names,separator,new String[0]);
        }

        @Override
        public boolean isNone() {
            return value.length==0;
        }

        @Override
        public TextNumber first() {
            return new AlphabetTextNumber(names,separator,new String[]{names[0]});
        }

        @Override
        public TextNumber next() {
            return new AlphabetTextNumber(names,separator,inc(0,value));
        }

        private int index(String n){
            for (int i = 0; i < names.length; i++) {
                if(names[i].equals(n)){
                    return i;
                }
            }
            throw new IllegalArgumentException("invalid name "+n);
        }
        private String[] copy(String[] value){
            String[] t = new String[value.length];
            System.arraycopy(value, 0, t, 0, value.length);
            return t;
        }

        private String[] ensureSize(int size,String[] value){
            if(size>value.length) {
                String[] t = new String[value.length + 1];
                System.arraycopy(value, 0, t, 0, value.length);
                value = t;
            }
            return value;
        }

        private String[] inc(int pos,String[] value){
            value=copy(value);
            value=ensureSize(pos+1,value);
            if(value[pos]==null){
                value[pos]=names[0];
            }else{
                int o = index(value[pos]);
                if(o<names.length-1){
                    value[pos]=names[o+1];
                }else{
                    for (int i = 0; i <= pos; i++) {
                        value[i]=names[0];
                    }
                    return inc(pos+1,value);
                }
            }
            return value;
        }

        @Override
        public String toString() {
            StringBuilder sb=new StringBuilder();
            for (int i = 0; i < value.length; i++) {
                if(value[i]==null){
                    break;
                }
                if(sb.length()>0) {
                    sb.insert(0, separator);
                }
                sb.insert(0,value[i]);
            }
            return sb.toString();
        }
    }

    /**
     * @category Format
     */
    public static class IntTextNumber implements TextNumber{
        private int value;

        public IntTextNumber(int value) {
            this.value = value;
        }

        @Override
        public TextNumber next() {
            return new IntTextNumber(value+1);
        }

        @Override
        public TextNumber first() {
            return new IntTextNumber(1);
        }

        @Override
        public TextNumber none() {
            return new IntTextNumber(0);
        }

        @Override
        public boolean isNone() {
            return value==0;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

}
