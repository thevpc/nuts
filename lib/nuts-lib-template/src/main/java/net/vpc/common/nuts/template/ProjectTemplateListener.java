package net.vpc.common.nuts.template;

public interface ProjectTemplateListener {
    void onSetProperty(String propertyName, String value, ProjectTemplate project);
}
