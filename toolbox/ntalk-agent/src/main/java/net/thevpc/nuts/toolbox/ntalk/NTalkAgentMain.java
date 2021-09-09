package net.thevpc.nuts.toolbox.ntalk;

import net.thevpc.nuts.lib.ntalk.NTalkAgent;
import net.thevpc.nuts.lib.ntalk.NTalkConstants;

import java.io.UncheckedIOException;

public class NTalkAgentMain {
    public static void main(String[] args) {
        int port=-1;
        int backlog=-1;
        String bindAddress="";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String key=null;
            String value=null;
            int eq = arg.indexOf('=');
            if(arg.startsWith("-") && eq >0){
                key=arg.substring(0,eq);
                value=arg.substring(eq+1);
            }else{
                key=arg;
            }
            switch (key){
                case "--help":{
                    System.out.println("ntalk-agent:");
                    System.out.println("  ntalk agent is a client to client communication agent.");
                    System.out.println("OPTIONS:");
                    System.out.println("  --port <port>");
                    System.out.println("      bind port for the agent. default is "+ NTalkConstants.DEFAULT_PORT);
                    System.out.println("  --backlog <backlog>");
                    System.out.println("      bind backlog for the agent. default is "+NTalkConstants.DEFAULT_BACKLOG);
                    System.out.println("  --bind <address>");
                    System.out.println("      bind address for the agent. default is "+NTalkConstants.DEFAULT_ADDRESS);
                    System.exit(0);
                    break;
                }
                case "--port":{
                    if(value==null){
                        i++;
                        value=args[i];
                    }
                    port=Integer.parseInt(value);
                    break;
                }
                case "--backlog":{
                    if(value==null){
                        i++;
                        value=args[i];
                    }
                    backlog=Integer.parseInt(value);
                    break;
                }
                case "--bind":{
                    if(value==null){
                        i++;
                        value=args[i];
                    }
                    bindAddress=value;
                }
                default:{
                    System.err.println("unexpected option "+arg);
                    System.exit(2);
                }
            }
        }
        NTalkAgent agent=new NTalkAgent(
                port,backlog,bindAddress
        );
        try {
            agent.runSync();
        }catch (UncheckedIOException e){
            System.exit(1);
        }
    }
}
