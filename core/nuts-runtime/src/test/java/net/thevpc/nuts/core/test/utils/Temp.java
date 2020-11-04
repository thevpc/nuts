package net.thevpc.nuts.core.test.utils;

import net.thevpc.nuts.runtime.util.console.CProgressBar;

public class Temp {
    public static void main(String[] args) {
        CProgressBar rr = new CProgressBar(null);
        for (int i = 0; i < 12; i++) {
            int finalI = i;
            rr.setIndeterminatePosition(new CProgressBar.IndeterminatePosition() {
                @Override
                public int evalIndeterminatePos(CProgressBar bar, int size) {
                    return finalI % size;
                }
            });
            System.out.printf("%2d ::" + rr.progress(-1) + "\n", i);
        }
    }
}
