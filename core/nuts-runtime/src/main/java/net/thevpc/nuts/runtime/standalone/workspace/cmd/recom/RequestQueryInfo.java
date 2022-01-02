package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.runtime.standalone.log.NutsLogUtils;

public class RequestQueryInfo {
    public String server;
    public RequestQuery q;

    public RequestQueryInfo(String id,String reason) {
        this.q = new RequestQuery();
        q.setId(id);
        q.setReason(reason);
    }
    public RequestQueryInfo(String id,Throwable reason) {
        this.q = new RequestQuery();
        q.setId(id);
        q.setReason(reason==null?null: NutsLogUtils.stacktrace(reason));
    }

    public String getServer() {
        return server;
    }

    public RequestQueryInfo setServer(String server) {
        this.server = server;
        return this;
    }

    public RequestQuery getQ() {
        return q;
    }

    public RequestQueryInfo setQ(RequestQuery q) {
        this.q = q;
        return this;
    }
}
