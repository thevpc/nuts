package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

public class RequestQuery {
    private String id;
    private String reason;
    private RequestAgent agent=new RequestAgent();

    public String getId() {
        return id;
    }

    public RequestQuery setId(String id) {
        this.id = id;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public RequestQuery setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public RequestAgent getAgent() {
        return agent;
    }

    public RequestQuery setAgent(RequestAgent agent) {
        this.agent = agent;
        return this;
    }
}
