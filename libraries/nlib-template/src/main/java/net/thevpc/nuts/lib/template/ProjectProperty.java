package net.thevpc.nuts.lib.template;

public class ProjectProperty {

    private String key;
    private String title;
    private String value;
    private String defaultValue;
    private StringValidator validator;
    private ProjectTemplate service;
    private boolean askMe;

    public ProjectProperty(String key, String title, String value, String defaultValue, StringValidator validator, ProjectTemplate service, boolean askMe) {
        this.key = key;
        this.title = title;
        this.value = value;
        this.defaultValue = defaultValue;
        this.validator = validator;
        this.service = service;
        this.askMe = askMe;
    }

    public ProjectProperty setTitle(String title) {
        this.title = title;
        return this;
    }

    public ProjectProperty setValidator(StringValidator validator) {
        this.validator = validator;
        return this;
    }

    public ProjectProperty setValue(String value) {
        this.value = value;
        for (ProjectTemplateListener listener : service.getConfigListeners()) {
            listener.onSetProperty(key, value, service);
        }
        return this;
    }

    public ProjectProperty setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public StringValidator getValidator() {
        return validator;
    }

    public String get() {
        return get(defaultValue, validator);
    }

    public String get(String defaultValue, StringValidator validator) {
        String x = getValue();
        if (x != null) {
            return x;
        }
        if (defaultValue != null && !service.isAskAll() && !askMe) {
            setValue(defaultValue);
            return defaultValue;
        }
        String o = service.getConsole().ask(getKey(), getTitle(), validator, defaultValue);
        if (o != null) {
            setValue(o);
        }
        return o;
    }

    public boolean getBoolean(boolean defaultValue) {
        String s = get(String.valueOf(defaultValue), new ValidatorFactory(service.getSession()).BOOLEAN);
        return "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s);
    }

    public boolean isAskMe() {
        return askMe;
    }

    public void setAskMe(boolean askMe) {
        this.askMe = askMe;
    }

}
