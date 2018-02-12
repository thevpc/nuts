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
    private final long globalNanos;
    private final long partialCount;
    private final long partialNanos;
    private final long length;

    public InputStreamEvent(Object source, String sourceName, long globalCount, long globalNanos, long partialCount, long partialNanos,long length) {
        this.source = source;
        this.length = length;
        this.sourceName = sourceName;
        this.globalCount = globalCount;
        this.globalNanos = globalNanos;
        this.partialCount = partialCount;
        this.partialNanos = partialNanos;
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

    public long getGlobalNanos() {
        return globalNanos;
    }

    public long getPartialCount() {
        return partialCount;
    }

    public long getPartialNanos() {
        return partialNanos;
    }
    
}
