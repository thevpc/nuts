/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.util;

import java.util.function.Supplier;

/**
 * @author thevpc
 */
public class CachedValue<T> {

    private final Supplier<T> supplier;
    private T lastValue;
    private long lastDate;
    private long timeoutSeconds;
    private boolean updating = false;

    public CachedValue(Supplier<T> callable, long timeoutSeconds) {
        this.supplier = callable;
        this.timeoutSeconds = timeoutSeconds;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isValid() {
        return !isInvalid();
    }

    public boolean isInvalid() {
        if (lastDate == 0 || timeoutSeconds == 0) {
            return true;
        }
        if (timeoutSeconds < 0) {
            timeoutSeconds = 10;
        }
        long x = (System.currentTimeMillis() - lastDate) / 1000;
        if (x < 0 || x > timeoutSeconds) {
            return true;
        }
        return false;
    }

    public void updateAsync() {
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
            lastDate = now;
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
