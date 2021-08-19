package net.thevpc.nuts;

public enum NutsActionSupport {
    UNSUPPORTED,
    SUPPORTED,
    PREFERRED;

    public boolean acceptCondition(NutsActionSupportCondition request, NutsSession session) {
        if(session==null){
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
                    case SUPPORTED:
                    {
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
}
