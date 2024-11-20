/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author thevpc
 */
public class NCachedValue<T> {

    private final Supplier<T> supplier;
    private T lastValue;
    private long lastDate;
    private long timeoutMilliSeconds;
    private boolean updating = false;
    private NWorkspace workspace;

    public NCachedValue(NWorkspace workspace,Supplier<T> callable, long timeoutMilliSeconds) {
        this.supplier = callable;
        this.timeoutMilliSeconds = timeoutMilliSeconds;
        this.workspace = workspace;
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

    public void updateAsync() {
        NSession session = workspace.currentSession();
        if (!updating) {
            new Thread() {
                @Override
                public void run() {
                    update();
                }
            }.start();
        }
    }

    public T update() {
        updating = true;
        try {
            long now = System.currentTimeMillis();
            try {
                lastValue = supplier.get();
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

    public boolean tryUpdate() {
        if (isInvalid()) {
            update();
            return true;
        }
        return false;
    }

    public T getValue() {
        tryUpdate();
        return lastValue;
    }

    public T getLastValue() {
        return lastValue;
    }

    public long getLastDate() {
        return lastDate;
    }

}
