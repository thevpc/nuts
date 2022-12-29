package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NDebugString {
    private boolean all;
    private boolean enabled;
    private int port;
    private int maxPort;
    private boolean tcp;
    private boolean suspend;
    private final List<NArgument> options = new ArrayList<>();

    public NDebugString() {
    }

    public static NDebugString of(String str, NSession session) {
        NDebugString d=new NDebugString();
        if (str == null) {
            d.setEnabled(false);
        } else {
            d.setEnabled(true);
            d.setSuspend(true);
            for (String a : StringTokenizerUtils.splitDefault(str)) {
                NArgument na = NArgument.of(a);
                switch (na.getKey().asString().orElse("")) {
                    case "s":
                    case "suspend": {
                        d.setSuspend(!na.isNegated());
                        break;
                    }
                    case "a":
                    case "all": {
                        d.setAll(!na.isNegated());
                        break;
                    }
                    case "tcp": {
                        d.setTcp(!na.isNegated());
                        break;
                    }
                    case "port": {
                        String s = na.getValue().asString().orElse("").trim();
                        if(s.matches("[0-9]+-[0-9]+")){
                            int sep = s.indexOf('-');
                            d.setPort(Integer.parseInt(s.substring(0,sep)));
                            d.setMaxPort(Integer.parseInt(s.substring(sep+1)));
                        }else{
                            d.setPort(na.getValue().asInt().get(session));
                        }
                        break;
                    }
                    default: {
                        if (na.getValue().isNull()) {
                            if (na.getKey().isBoolean()) {
                                boolean v = na.getKey().asBoolean().get(session);
                                d.setEnabled(na.isNegated() != v);
                            } else if (na.getKey().isInt()) {
                                d.setPort(na.getKey().asInt().get(session));
                            } else {
                                d.options.add(na);
                            }
                        } else {
                            d.options.add(na);
                        }
                    }
                }
            }
        }
        return d;
    }

    public List<NArgument> getOptions() {
        return options;
    }

    public NArgument getOption(String key) {
        for (NArgument option : options) {
            if (Objects.equals(option.getKey().asString(), key)) {
                return option;
            }
        }
        return null;
    }

    public boolean isAll() {
        return all;
    }

    public NDebugString setAll(boolean all) {
        this.all = all;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NDebugString setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public int getPort() {
        return port;
    }

    public NDebugString setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isTcp() {
        return tcp;
    }

    public NDebugString setTcp(boolean tcp) {
        this.tcp = tcp;
        return this;
    }

    public boolean isSuspend() {
        return suspend;
    }

    public NDebugString setSuspend(boolean suspend) {
        this.suspend = suspend;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NDebugString that = (NDebugString) o;
        return all == that.all && enabled == that.enabled && port == that.port && maxPort == that.maxPort && tcp == that.tcp && suspend == that.suspend && Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(all, enabled, port, maxPort, tcp, suspend, options);
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append(isEnabled()?"enabled":"disabled");
        if(all){
            sb.append(",all");
        }
        if(port>0){
            sb.append(",").append(port);
        }
        if(maxPort>0){
            sb.append(",max-port=").append(maxPort);
        }
        if(isSuspend()){
            sb.append(",suspend");
        }
        for (NArgument option : options) {
            sb.append(",").append(option);
        }
        return sb.toString();
    }

    public int getMaxPort() {
        return maxPort;
    }

    public NDebugString setMaxPort(int maxPort) {
        this.maxPort = maxPort;
        return this;
    }
}
