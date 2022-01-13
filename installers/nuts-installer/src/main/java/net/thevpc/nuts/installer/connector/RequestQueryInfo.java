package net.thevpc.nuts.installer.connector;


public class RequestQueryInfo {
//    public String url="https://thevpc.net/nuts";
    public String url="{protocol}://{host}:{port}/{context}";
    public String protocol;
    public String host;
    public String context;
    public int port;
    public RequestQuery q;

    public String getUrl() {
        return url;
    }

    public RequestQueryInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public RequestQueryInfo setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getHost() {
        return host;
    }

    public RequestQueryInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public String getContext() {
        return context;
    }

    public RequestQueryInfo setContext(String context) {
        this.context = context;
        return this;
    }

    public int getPort() {
        return port;
    }

    public RequestQueryInfo setPort(int port) {
        this.port = port;
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
