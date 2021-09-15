package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsVal;
import net.thevpc.nuts.NutsUtilStrings;

import java.util.Objects;

public class NutsBootStrValImpl implements NutsVal {
    private String value;

    public NutsBootStrValImpl(String value) {
        this.value = value;
    }

    @Override
    public boolean isBlank() {
        return NutsUtilStrings.isBlank(value);
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
        if (NutsUtilStrings.isBlank(value)) {
            return emptyValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return errorValue;
        }
    }
    @Override
    public int getInt() {
        if (NutsUtilStrings.isBlank(value)) {
            throw new NumberFormatException("empty value");
        }
        return Integer.parseInt(value);
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
        if (NutsUtilStrings.isBlank(value)) {
            return emptyValue;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return errorValue;
        }
    }
    @Override
    public long getLong() {
        if (NutsUtilStrings.isBlank(value)) {
            throw new NumberFormatException("empty value");
        }
        return Long.parseLong(value);
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
        if (NutsUtilStrings.isBlank(value)) {
            return emptyValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return errorValue;
        }
    }
    @Override
    public double getDouble() {
        if (NutsUtilStrings.isBlank(value)) {
            throw new NumberFormatException("empty value");
        }
        return Double.parseDouble(value);
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
        if (NutsUtilStrings.isBlank(value)) {
            return emptyValue;
        }
        return NutsUtilStrings.parseBoolean(value, emptyValue, errorValue);
    }

    @Override
    public String getString() {
        return value;
    }

    @Override
    public String getString(String defaultValue) {
        return value == null ? defaultValue : value;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsBootStrValImpl that = (NutsBootStrValImpl) o;
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

    public Object getObject(){
        return value;
    }

    public Object getObject(Object defaultValue){
        return value==null?defaultValue:value;
    }


}
