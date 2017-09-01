package net.vpc.app.nuts.util;

abstract class AbstractPlatformBeanProperty implements PlatformBeanProperty {

    protected String name;
    protected Class fieldType;

    AbstractPlatformBeanProperty(String name, Class fieldType) {
        this.name = name;
        this.fieldType = fieldType;
    }

    @Override
    public String getName() {
        return name;
    }

    public Class getPlatformType() {
        return fieldType;
    }

}
