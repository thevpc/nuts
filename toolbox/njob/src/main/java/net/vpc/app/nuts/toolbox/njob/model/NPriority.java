package net.vpc.app.nuts.toolbox.njob.model;

public enum NPriority {
    NONE,
    LOW,
    MEDIUM,
    NORMAL,
    HIGH,
    URGENT,
    CRITICAL;

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
