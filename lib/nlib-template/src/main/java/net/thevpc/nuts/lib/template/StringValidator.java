package net.thevpc.nuts.lib.template;

public interface StringValidator {

    default String getHints() {
        return null;
    }

    StringValidatorType getType();

    String validate(String value);
}
