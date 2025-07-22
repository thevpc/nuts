package net.thevpc.nuts.runtime.standalone.util.reflect;

import net.thevpc.nuts.NExceptions;
import net.thevpc.nuts.reflect.NReflectMethod;
import net.thevpc.nuts.reflect.NReflectParameter;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NSignature;
import net.thevpc.nuts.util.NMsg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

public class DefaultNReflectMethod implements NReflectMethod {
    private Method method;
    private NReflectType declaringType;
    private NReflectParameter[] cachedParams;
    private NSignature signature;
    private boolean accessible;

    public DefaultNReflectMethod(Method method, NReflectType declaringType) {
        this.method = method;
        try {
            method.setAccessible(true);
            accessible=true;
        }catch (Exception e){
            //ignore
        }
        this.declaringType = declaringType;
    }

    @Override
    public boolean isAccessible() {
        return accessible;
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public NReflectParameter[] getParameters() {
        if (cachedParams == null) {
            cachedParams = Arrays.stream(method.getParameters()).map(x -> {
                return new DefaultNReflectParameter(x, declaringType.getRepository());
            }).toArray(NReflectParameter[]::new);
        }
        return cachedParams;
    }

    @Override
    public Object invoke(Object instance, Object... args) {
        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException ex) {
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("illegal-access (%s) %s",toString(),NExceptions.getErrorMessage(ex)), ex);
        } catch (InvocationTargetException ex) {
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("illegal-invocation (%s) %s",toString(),NExceptions.getErrorMessage(ex)), ex);
        }
    }

    @Override
    public NReflectType getDeclaringType() {
        return declaringType;
    }

    @Override
    public boolean isVarArgs() {
        return getSignature().isVarArgs();
    }

    @Override
    public NSignature getSignature() {
        if(signature==null){
            Parameter[] mp = method.getParameters();
            boolean varargs=(mp.length>0 && mp[mp.length-1].isVarArgs());
            NReflectType[] p = Arrays.stream(getParameters()).map(x->x.getParameterType()).toArray(NReflectType[]::new);
            signature=varargs?NSignature.ofVarArgs(p) : NSignature.of(p);
        }
        return signature;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNReflectMethod that = (DefaultNReflectMethod) o;
        return Objects.equals(method, that.method) && Objects.equals(declaringType, that.declaringType) && Objects.deepEquals(cachedParams, that.cachedParams) && Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, declaringType, Arrays.hashCode(cachedParams), signature);
    }

    @Override
    public String toString() {
        return method.toString();
    }
}
