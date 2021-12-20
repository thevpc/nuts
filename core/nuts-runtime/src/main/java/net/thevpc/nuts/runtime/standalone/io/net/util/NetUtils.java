package net.thevpc.nuts.runtime.standalone.io.net.util;

import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

public class NetUtils {
    public static boolean isFreeTcpPort(int port) {
        try (ServerSocket s = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int detectRandomFreeTcpPort(int from, int to) {
        Set<Integer> tested = new HashSet<>();
        int interval = to - from;
        if (interval > 0) {
            while (tested.size() < interval) {
                int x = from + (int) (Math.random() * interval);
                if (!tested.contains(x)) {
                    tested.add(x);
                    if (isFreeTcpPort(x)) {
                        return x;
                    }
                }
            }
        }
        return -1;
    }

    @Deprecated
    public static int detectFirstFreeTcpPort(int from, int to) {
        for (int i = from; i < to; i++) {
            if (isFreeTcpPort(i)) {
                return i;
            }
        }
        return -1;
    }
}
