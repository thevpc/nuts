package net.thevpc.nuts.runtime.standalone.util;

import java.util.ArrayList;
import java.util.List;

public class CallOnceRunnable implements Runnable {
    private Runnable base;
    private boolean done;

    public static CallOnceRunnable ofMany(Runnable a,Runnable b) {
        if(a==null){
            return of(b);
        }
        if(b==null){
            return of(a);
        }
        return of(() -> {
            a.run();
            b.run();
        });
    }
    public static CallOnceRunnable ofMany(Runnable... base) {
        List<CallOnceRunnable> each = new ArrayList<>();
        if (base != null) {
            for (Runnable r : base) {
                CallOnceRunnable o = of(r);
                if (o != null) {
                    each.add(o);
                }
            }
        }
        switch (each.size()) {
            case 0:
                return null;
            case 1:
                return each.get(0);
            case 2: {
                CallOnceRunnable a1 = each.get(0);
                CallOnceRunnable a2 = each.get(1);
                return of(() -> {
                    a1.run();
                    a2.run();
                });
            }
        }
        return of(() -> {
            for (CallOnceRunnable c : each) {
                c.run();
            }
        });
    }


    public CallOnceRunnable(Runnable base) {
        this.base = base;
    }

    public static CallOnceRunnable of(Runnable other) {
        if (other == null) {
            return null;
        }
        if (other instanceof CallOnceRunnable) {
            return (CallOnceRunnable) other;
        }
        return new CallOnceRunnable(other);
    }

    @Override
    public void run() {
        if (!done) {
            base.run();
            done = true;
        }
    }
}
