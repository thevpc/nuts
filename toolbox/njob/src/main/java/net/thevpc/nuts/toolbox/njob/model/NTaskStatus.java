package net.thevpc.nuts.toolbox.njob.model;

public enum NTaskStatus {
    TODO(true,false),
    WIP(true,false),
    DONE(false,true),
    CANCELLED(false,true);
    private boolean open;
    private boolean closed;

    NTaskStatus(boolean open, boolean closed) {
        this.open = open;
        this.closed = closed;
    }

    public static NTaskStatus parse(String stringValue) {
        if(stringValue==null || stringValue.isEmpty()){
            return TODO;
        }
        switch (stringValue.toUpperCase()){
            case "T":
            case "TODO":{
                return TODO;
            }
            case "W":
            case "WIP":{
                return WIP;
            }
            case "D":
            case "DONE":{
                return DONE;
            }
            case "C":
            case "CANCELLED":{
                return CANCELLED;
            }
            default:{
                return valueOf(stringValue);
            }
        }
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isClosed() {
        return closed;
    }
}
