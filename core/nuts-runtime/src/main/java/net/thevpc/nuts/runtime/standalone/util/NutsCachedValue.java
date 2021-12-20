/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.NutsSession;

import java.util.function.Function;

/**
 * @author thevpc
 */
public class NutsCachedValue<T> {

    private final Function<NutsSession,T> supplier;
    private T lastValue;
    private long lastDate;
    private long timeoutMilliSeconds;
    private boolean updating = false;

    public NutsCachedValue(Function<NutsSession,T> callable, long timeoutMilliSeconds) {
        this.supplier = callable;
        this.timeoutMilliSeconds = timeoutMilliSeconds;
    }

    public long getTimeoutMilliSeconds() {
        return timeoutMilliSeconds;
    }

    public void setTimeoutMilliSeconds(long timeoutMilliSeconds) {
        this.timeoutMilliSeconds = timeoutMilliSeconds;
    }

    public boolean isValid() {
        return !isInvalid();
    }

    public boolean isInvalid() {
        if (lastDate == 0 || timeoutMilliSeconds == 0) {
            return true;
        }
        if (timeoutMilliSeconds < 0) {
            timeoutMilliSeconds = 1000;
        }
        long x = (System.currentTimeMillis() - lastDate);
        if (x < 0 || x > timeoutMilliSeconds) {
            return true;
        }
        return false;
    }

    public void updateAsync(NutsSession session) {
        if (!updating) {
            new Thread() {
                @Override
                public void run() {
                    update(session);
                }
            }.start();
        }
    }

    public T update(NutsSession session) {
        updating = true;
        try {
            long now = System.currentTimeMillis();
            try {
                lastValue = supplier.apply(session);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            if(lastValue!=null) {
                //only if not failing!
                lastDate = now;
            }
        } finally {
            updating = false;
        }
        return lastValue;
    }

    public boolean tryUpdate(NutsSession session) {
        if (isInvalid()) {
            update(session);
            return true;
        }
        return false;
    }

    public T getValue(NutsSession session) {
        tryUpdate(session);
        return lastValue;
    }

    public T getLastValue() {
        return lastValue;
    }

    public long getLastDate() {
        return lastDate;
    }

}
