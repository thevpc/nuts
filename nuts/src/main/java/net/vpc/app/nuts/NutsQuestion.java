package net.vpc.app.nuts;

public class NutsQuestion<T> {
    private String message;
    private Object[] messageParameters;
    private Object[] acceptedValues;
    private Object defautValue;
    private Class<T> valueType;
    private NutsResponseParser parse;

    public static NutsQuestion<Boolean> forBoolean(String msg,Object... params){
        return new NutsQuestion<>(Boolean.class).setMessage(msg,params);
    }

    public static NutsQuestion<String> forString(String msg,Object... params){
        return new NutsQuestion<>(String.class).setMessage(msg,params);
    }
    public static NutsQuestion<Integer> forInteger(String msg,Object... params){
        return new NutsQuestion<>(Integer.class).setMessage(msg,params);
    }
    public static NutsQuestion<Long> forLong(String msg,Object... params){
        return new NutsQuestion<>(Long.class).setMessage(msg,params);
    }
    public static NutsQuestion<Float> forFloat(String msg,Object... params){
        return new NutsQuestion<>(Float.class).setMessage(msg,params);
    }

    public static NutsQuestion<Double> forDouble(String msg,Object... params){
        return new NutsQuestion<>(Double.class).setMessage(msg,params);
    }

    public static <K extends Enum> NutsQuestion<K> forEnum(Class<K> enumType,String msg,Object... params){
        K[] values = enumType.getEnumConstants();
        return new NutsQuestion<>(enumType)
                .setMessage(msg,params)
                .setAcceptedValues(values);
    }

    public NutsQuestion(Class<T> valueType) {
        this.valueType=valueType;
    }

    public String getMessage() {
        return message;
    }

    public NutsQuestion<T> setMessage(String message,Object... messageParameters) {
        this.message = message;
        this.messageParameters=messageParameters;
        return this;
    }

    public NutsQuestion<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object[] getMessageParameters() {
        return messageParameters;
    }

    public NutsQuestion<T> setMessageParameters(Object... messageParameters) {
        this.messageParameters = messageParameters;
        return this;
    }

    public Object[] getAcceptedValues() {
        return acceptedValues;
    }

    public NutsQuestion<T> setAcceptedValues(Object[] acceptedValues) {
        this.acceptedValues = acceptedValues;
        return this;
    }

    public Object getDefautValue() {
        return defautValue;
    }

    public NutsQuestion<T> setDefautValue(Object defautValue) {
        this.defautValue = defautValue;
        return this;
    }

    public Class getValueType() {
        return valueType;
    }

    public NutsQuestion<T> setValueType(Class valueType) {
        this.valueType = valueType;
        return this;
    }

    public NutsResponseParser getParse() {
        return parse;
    }

    public NutsQuestion<T> setParse(NutsResponseParser parse) {
        this.parse = parse;
        return this;
    }
}
