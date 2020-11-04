package net.thevpc.nuts.toolbox.njob.time;

import java.util.NoSuchElementException;

public enum WeekDay {
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY;

    public String code(){
        return toString().toLowerCase().substring(0,3);
    }

    public static WeekDay parse(String s){
        return parse(s,false);
    }

    public static WeekDay parse(String s,boolean lenient){
        if(s!=null && s.length()>0) {
            if(s.length()==3){
                for (WeekDay value : values()) {
                    if(value.toString().substring(0,3).equalsIgnoreCase(s)){
                        return value;
                    }
                }
            }
            try {
                return WeekDay.valueOf(s.toUpperCase());
            }catch (Exception ex){
                //
            }
        }else{
            return null;
        }
        if(lenient){
            return null;
        }
        throw new NoSuchElementException("invalid week day "+s);
    }
}
