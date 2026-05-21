package net.thevpc.nuts.runtime.standalone.reflect;

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
    public String name() {
        return parameter.getName();
    }

    @Override
    public NReflectType parameterType() {
        return repository.getType(parameter.getType());
    }

}
