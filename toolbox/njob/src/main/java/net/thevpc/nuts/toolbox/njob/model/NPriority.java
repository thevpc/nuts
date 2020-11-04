package net.thevpc.nuts.toolbox.njob.model;

public enum NPriority {
    NONE,
    LOW,
    MEDIUM,
    NORMAL,
    HIGH,
    URGENT,
    CRITICAL;

    public static NPriority parse(String v) {
        if(v==null){
            v="";
        }
        if(v.length()==0){
            return NONE;
        }
        switch (v.toUpperCase()){
            case "0":
            case "NN":
            case "NONE":
                {
                return NONE;
            }
            case "N":
            case "NO":
            case "NOR":
            case "NORM":
            case "NORMAL":
                {
                return NORMAL;
            }
            case "U":
            case "UR":
            case "URG":
            case "URGENT":
                {
                return URGENT;
            }
            case "H":
            case "HI":
            case "HIG":
            case "HIGH":
                {
                return HIGH;
            }
            case "L":
            case "LO":
            case "LOW":
                {
                return LOW;
            }
            case "ME":
            case "MED":
            case "MEDIUM":
                {
                return MEDIUM;
            }
            case "C":
            case "CR":
            case "CRI":
            case "CRIT":
            case "CRITICAL":
                {
                return MEDIUM;
            }
            default:{
                return valueOf(v);
            }
        }
    }

    public NPriority higher(){
        int o = ordinal();
        if(o< values().length-1){
            return values()[o+1];
        }
        return this;
    }
    public NPriority lower(){
        int o = ordinal();
        if(o>0){
            return values()[o-1];
        }
        return this;
    }
}
