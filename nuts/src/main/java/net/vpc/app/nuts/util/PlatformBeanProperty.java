package net.vpc.app.nuts.util;

public interface PlatformBeanProperty {

    String getName();

    Class getPlatformType();

    Object getValue(Object o);

    void setValue(Object o, Object value);

    boolean isWriteSupported();

    boolean isReadSupported();

    boolean isTransient();

    boolean isDeprecated();
}
