package net.vpc.app.nuts;

public interface NutsResponseParser {
    Object parse(Object response,Class type);
}
