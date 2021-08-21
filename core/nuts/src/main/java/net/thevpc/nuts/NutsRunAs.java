package net.thevpc.nuts;

import java.util.Objects;

public class NutsRunAs {
    public static final NutsRunAs CURRENT_USER = new NutsRunAs(Mode.CURRENT_USER, null);
    public static final NutsRunAs ROOT = new NutsRunAs(Mode.ROOT, null);
    public static final NutsRunAs SUDO = new NutsRunAs(Mode.SUDO, null);
    private final Mode mode;
    private final String user;

    private NutsRunAs(Mode mode, String user) {
        this.mode = mode;
        this.user = user;
    }

    public static NutsRunAs currentUser() {
        return CURRENT_USER;
    }

    public static NutsRunAs root() {
        return ROOT;
    }

    public static NutsRunAs sudo() {
        return SUDO;
    }

    public static NutsRunAs user(String name) {
        if (NutsUtilStrings.isBlank(name)) {
            throw new IllegalArgumentException("invalid user name");
        }
        return new NutsRunAs(Mode.SUDO, name);
    }

    public Mode getMode(){
        return mode;
    }

    public String getUser() {
        return user;
    }

    public enum Mode implements NutsEnum{
        CURRENT_USER,
        USER,
        ROOT,
        SUDO;
        ;
        private String id;

        Mode() {
            this.id = name().toLowerCase().replace('_', '-');
        }

        @Override
        public String id() {
            return id;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsRunAs nutsRunAs = (NutsRunAs) o;
        return mode == nutsRunAs.mode && Objects.equals(user, nutsRunAs.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, user);
    }
}
