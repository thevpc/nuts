package net.thevpc.nuts;

import net.thevpc.nuts.lib.ntalk.NTalkAgent;
import net.thevpc.nuts.lib.ntalk.NTalkClient;
import net.thevpc.nuts.lib.ntalk.NTalkServer;

public class TestBus {
    public static void main(String[] args) {
        NTalkAgent bus = new NTalkAgent();
        bus.runAsync();
        NTalkAgent bus2 = new NTalkAgent();
        bus2.runAsync();
        NTalkServer srv = new NTalkServer("hello", (jobId, msg, clientId, serverId, serviceName) -> (new String(msg) + " from server").getBytes());
        srv.runAsync();

        for (int i = 0; i < 10; i++) {
            int i0=i;
            new Thread(()->{
                NTalkClient cli=new NTalkClient();
                for (int j = 0; j < 200; j++) {
                    byte[] resp = cli.request("hello", ("world cli"+i0+" msg"+(j+1)).getBytes());
                    System.out.println(new String(resp));
                }
            }).start();
        }



//        srv.stop();
//        bus.stop();
    }
}
