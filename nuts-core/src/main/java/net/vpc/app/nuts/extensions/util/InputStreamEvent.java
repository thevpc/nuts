/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.util;

/**
 *
 * @author vpc
 */
public class InputStreamEvent {
    
    private final Object source;
    private final String sourceName;
    private final long globalCount;
    private final long globalMillis;
    private final long partialCount;
    private final long partialMillis;
    private final long length;

    public InputStreamEvent(Object source, String sourceName, long globalCount, long globalMillis, long partialCount, long partialMillis,long length) {
        this.source = source;
        this.length = length;
        this.sourceName = sourceName;
        this.globalCount = globalCount;
        this.globalMillis = globalMillis;
        this.partialCount = partialCount;
        this.partialMillis = partialMillis;
    }

    public long getLength() {
        return length;
    }

    
    public Object getSource() {
        return source;
    }

    public String getSourceName() {
        return sourceName;
    }

    public long getGlobalCount() {
        return globalCount;
    }

    public long getGlobalMillis() {
        return globalMillis;
    }

    public long getPartialCount() {
        return partialCount;
    }

    public long getPartialMillis() {
        return partialMillis;
    }
    
}
