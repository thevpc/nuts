package net.vpc.app.nuts.toolbox.njob.model;

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

    public boolean isOpen() {
        return open;
    }

    public boolean isClosed() {
        return closed;
    }
}
