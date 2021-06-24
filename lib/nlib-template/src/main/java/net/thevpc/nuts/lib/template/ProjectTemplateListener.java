package net.thevpc.nuts.lib.template;

public interface ProjectTemplateListener {

    void onSetProperty(String propertyName, String value, ProjectTemplate project);
}
