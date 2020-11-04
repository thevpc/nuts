package net.thevpc.nuts.toolbox.njob.model;

public enum NFlag {
    NONE,
    STAR1,
    STAR2,
    STAR3,
    STAR4,
    STAR5,

    FLAG1,
    FLAG2,
    FLAG3,
    FLAG4,
    FLAG5,

    KING1,
    KING2,
    KING3,
    KING4,
    KING5,

    HEART1,
    HEART2,
    HEART3,
    HEART4,
    HEART5,

    PHONE1,
    PHONE2,
    PHONE3,
    PHONE4,
    PHONE5,
    ;

    public static NFlag parse(String v) {
        if(v==null || v.length()==0){
            return null;
        }
        switch (v.toUpperCase()){
            case "RAND":
            case "RANDOM":{
                return  values()[(int) (Math.random() * NFlag.values().length)];
            }
            default:{
                return valueOf(v);
            }
        }
    }
}
