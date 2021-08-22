package net.thevpc.nuts;

public enum NutsActionSupport implements NutsEnum{
    UNSUPPORTED,
    SUPPORTED,
    PREFERRED;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsActionSupport() {
        this.id = name().toLowerCase().replace('_', '-');
    }



    public static NutsActionSupport parseLenient(String any) {
        return parseLenient(any, null);
    }

    public static NutsActionSupport parseLenient(String any, NutsActionSupport emptyOrErrorValue) {
        return parseLenient(any, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsActionSupport parseLenient(String any, NutsActionSupport emptyValue, NutsActionSupport errorValue) {
        if (any == null) {
            any = "";
        }
        any = any.toLowerCase();
        switch (any) {
            case "unsupported":
                return UNSUPPORTED;
            case "supported":
                return SUPPORTED;
            case "preferred":
                return PREFERRED;
            case "":
                return emptyValue;
        }
        return errorValue;
    }

    public boolean acceptCondition(NutsActionSupportCondition request, NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("missing session");
        }
        if (request == null) {
            request = NutsActionSupportCondition.NEVER;
        }
        switch (this) {
            case UNSUPPORTED: {
                return false;
            }
            case SUPPORTED: {
                switch (request) {
                    case NEVER:
                        return false;
                    case ALWAYS:
                    case SUPPORTED: {
                        return true;
                    }
                    case PREFERRED: {
                        return false;
                    }
                    default: {
                        throw new NutsUnsupportedEnumException(session, request);
                    }
                }
            }
            case PREFERRED: {
                switch (request) {
                    case NEVER:
                        return false;
                    case ALWAYS:
                    case PREFERRED:
                    case SUPPORTED: {
                        return true;
                    }
                    default: {
                        throw new NutsUnsupportedEnumException(session, request);
                    }
                }
            }
            default: {
                throw new NutsUnsupportedEnumException(session, this);
            }
        }
    }

    @Override
    public String id() {
        return id;
    }
}
