package net.thevpc.common.nuts.template;

public interface StringValidator {

    default String getHints() {
        return null;
    }

    StringValidatorType getType();

    String validate(String value);
}
