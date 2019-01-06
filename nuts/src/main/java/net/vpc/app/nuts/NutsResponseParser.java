package net.vpc.app.nuts;

public interface NutsResponseParser {
    Object[] getDefaultAcceptedValues(Class type);

    Object parse(Object response, Class type);
}
