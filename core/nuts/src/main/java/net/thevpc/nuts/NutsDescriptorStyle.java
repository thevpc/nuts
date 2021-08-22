package net.thevpc.nuts;

public enum NutsDescriptorStyle implements NutsEnum {
    MAVEN,
    NUTS;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsDescriptorStyle() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsDescriptorStyle parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsDescriptorStyle parseLenient(String value, NutsDescriptorStyle emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsDescriptorStyle parseLenient(String value, NutsDescriptorStyle emptyValue, NutsDescriptorStyle errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsDescriptorStyle.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
