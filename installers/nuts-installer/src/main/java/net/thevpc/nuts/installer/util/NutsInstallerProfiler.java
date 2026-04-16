package net.thevpc.nuts.installer.util;

import net.thevpc.nuts.installer.NutsInstaller;

public class NutsInstallerProfiler {
    private final NutsInstaller mi;

    public NutsInstallerProfiler(NutsInstaller mi) {
        this.mi = mi;
    }

    public void run() {
        mi.showFrame();
        new Thread() {
            @Override
            public void run() {
                waitAndSendAction(2, "light");
                waitAndSendAction(1, "dark");
                waitAndSendAction(1, "light");
                waitAndSendAction(1, "next");
                waitAndSendAction(1, "next");
                waitAndSendAction(1, "accept");
                waitAndSendAction(1, "reject");
                waitAndSendAction(1, "accept");
                waitAndSendAction(1, "next");

                waitAndSendAction(1, "wait-loading");
                waitAndSendAction(1, "lts");
                waitAndSendAction(1, "standard");
                waitAndSendAction(1, "lts");
                waitAndSendAction(1, "standard");
                waitAndSendAction(1, "next");
                waitAndSendAction(1, "wait-loading");
                for (int i = 0; i < 20; i++) {
                    waitAndSendAction(1, "change","rand");
                }
                waitAndSendAction(1, "next");
                waitAndSendAction(1, "next");
                waitAndSendAction(1, "next");
                waitAndSendAction(1, "next");
                waitAndSendAction(1, "wait-loading");
                waitAndSendAction(1, "next");
                waitAndSendAction(1, "finish");
            }
        }.start();
    }

    public void waitAndSendAction(int seconds, String... action) {
        sleepSeconds(seconds);
        sendAction(action);
    }

    public void sendAction(String[] action) {
        mi.sendAction(action);
    }

    public void sleepSeconds(int s) {
        try {
            Thread.sleep(s * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
