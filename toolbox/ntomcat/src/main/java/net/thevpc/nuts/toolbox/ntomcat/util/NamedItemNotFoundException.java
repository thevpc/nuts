package net.thevpc.nuts.toolbox.ntomcat.util;

public class NamedItemNotFoundException extends RuntimeException{
    private String name;

    public NamedItemNotFoundException(String message, String name) {
        super(message);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
