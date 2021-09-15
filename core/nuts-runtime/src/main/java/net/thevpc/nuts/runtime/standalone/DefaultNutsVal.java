package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NutsVal;
import net.thevpc.nuts.NutsUtilStrings;

import java.util.Objects;

public class DefaultNutsVal implements NutsVal {
    private Object value;

    public DefaultNutsVal(Object value) {
        this.value = value;
    }

    @Override
    public boolean isBlank() {
        if(value instanceof CharSequence){
            return NutsUtilStrings.isBlank(value.toString());
        }
        return value==null;
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public boolean isInt() {
        return !isBlank() && getInt(null, null) != null;
    }

    @Override
    public Integer getInt(Integer emptyOrErrorValue) {
        return getInt(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Integer getInt(Integer emptyValue, Integer errorValue) {
        if (isBlank()) {
            return emptyValue;
        }
        if(value instanceof Number){
            return ((Number) value).intValue();
        }
        if(value instanceof CharSequence) {
            try {
                return Integer.parseInt(value.toString());
            } catch (Exception ex) {
                return errorValue;
            }
        }
        return errorValue;
    }
    @Override
    public int getInt() {
        if (isBlank()) {
            throw new NumberFormatException("empty value");
        }
        if(value instanceof Number){
            return ((Number) value).intValue();
        }
        if(value instanceof CharSequence) {
            return Integer.parseInt(value.toString());
        }
        throw new NumberFormatException("not a number");
    }

    ////////////

    @Override
    public boolean isLong() {
        return !isBlank() && getLong(null, null) != null;
    }

    @Override
    public Long getLong(Long emptyOrErrorValue) {
        return getLong(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Long getLong(Long emptyValue, Long errorValue) {
        if (isBlank()) {
            return emptyValue;
        }
        if(value instanceof Number){
            return ((Number) value).longValue();
        }
        if(value instanceof CharSequence) {
            try {
                return Long.parseLong(value.toString());
            } catch (Exception ex) {
                return errorValue;
            }
        }
        return errorValue;
    }
    @Override
    public long getLong() {
        if (isBlank()) {
            throw new NumberFormatException("empty value");
        }
        if(value instanceof Number){
            return ((Number) value).longValue();
        }
        if(value instanceof CharSequence) {
            return Long.parseLong(value.toString());
        }
        throw new NumberFormatException("not a number");
    }


    @Override
    public boolean isDouble() {
        return !isBlank() && getDouble(null, null) != null;
    }

    @Override
    public Double getDouble(Double emptyOrErrorValue) {
        return getDouble(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Double getDouble(Double emptyValue, Double errorValue) {
        if (isBlank()) {
            return emptyValue;
        }
        if(value instanceof Number){
            return ((Number) value).doubleValue();
        }
        if(value instanceof CharSequence) {
            try {
                return Double.parseDouble(value.toString());
            } catch (Exception ex) {
                return errorValue;
            }
        }
        return errorValue;
    }
    @Override
    public double getDouble() {
        if (isBlank()) {
            throw new NumberFormatException("empty value");
        }
        if(value instanceof Number){
            return ((Number) value).doubleValue();
        }
        if(value instanceof CharSequence){
            return Double.parseDouble(value.toString());
        }
        throw new NumberFormatException("not a number");
    }

    @Override
    public boolean isBoolean() {
        return !isBlank() && getBoolean(null, null) != null;
    }


    @Override
    public boolean getBoolean() {
        return getBoolean(false);
    }

    @Override
    public Boolean getBoolean(Boolean emptyOrErrorValue) {
        return getBoolean(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Boolean getBoolean(Boolean emptyValue, Boolean errorValue) {
        if (isBlank()) {
            return emptyValue;
        }
        if(value instanceof Boolean){
            return (Boolean) value;
        }
        if(value instanceof String){
            return NutsUtilStrings.parseBoolean(value.toString(), emptyValue, errorValue);
        }
        return errorValue;
    }

    @Override
    public String getString() {
        return value==null?null:value.toString();
    }

    @Override
    public String getString(String defaultValue) {
        return value == null ? defaultValue : value.toString();
    }

    @Override
    public boolean isString() {
        return value instanceof String;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsVal that = (DefaultNutsVal) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Object getObject() {
        return value;
    }

    @Override
    public Object getObject(Object defaultValue) {
        return value==null?defaultValue:value;
    }
}
