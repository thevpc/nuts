package net.thevpc.nuts;

public interface NutsVal {
    static NutsVal of(Object value, NutsSession session) {
        return session.getWorkspace().util().valOf(value);
    }

    boolean isBlank();

    boolean isNull();

    int getInt();

    Integer getInt(Integer emptyOrErrorValue);

    Integer getInt(Integer emptyValue, Integer errorValue);

    boolean isLong();

    Long getLong(Long emptyOrErrorValue);

    Long getLong(Long emptyValue, Long errorValue);

    long getLong();

    boolean isDouble();

    Double getDouble(Double emptyOrErrorValue);

    Double getDouble(Double emptyValue, Double errorValue);

    double getDouble();

    boolean isBoolean();

    boolean isInt();

    boolean getBoolean();

    Boolean getBoolean(Boolean emptyOrErrorValue);

    Boolean getBoolean(Boolean emptyValue, Boolean errorValue);

    boolean isString();

    String getString();

    String getString(String defaultValue);

    Object getObject();

    Object getObject(Object defaultValue);

}
