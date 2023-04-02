package net.thevpc.nuts.toolbox.nsh.cmds.util;

public class NumberRange {
    private Long from;
    private Long to;

    public NumberRange() {
    }

    public NumberRange(Long from, Long to) {
        this.from = from;
        this.to = to;
    }

    public Long getFrom() {
        return from;
    }

    public NumberRange setFrom(Long from) {
        this.from = from;
        return this;
    }

    public Long getTo() {
        return to;
    }

    public NumberRange setTo(Long to) {
        this.to = to;
        return this;
    }
}
