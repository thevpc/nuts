package net.thevpc.nuts.runtime.standalone.util.reflect;

import net.thevpc.nuts.reflect.NReflectParameter;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.reflect.NReflectType;

import java.lang.reflect.Parameter;

public class DefaultNReflectParameter implements NReflectParameter {
    private Parameter parameter;
    private NReflectRepository repository;

    public DefaultNReflectParameter(Parameter parameter, NReflectRepository repository) {
        this.parameter = parameter;
        this.repository = repository;
    }

    @Override
    public String getName() {
        return parameter.getName();
    }

    @Override
    public NReflectType getParameterType() {
        return repository.getType(parameter.getType());
    }

}
