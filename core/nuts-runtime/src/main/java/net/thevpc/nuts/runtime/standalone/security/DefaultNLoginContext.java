package net.thevpc.nuts.runtime.standalone.security;

public class DefaultNLoginContext  {
    private String userName;

    public DefaultNLoginContext(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
