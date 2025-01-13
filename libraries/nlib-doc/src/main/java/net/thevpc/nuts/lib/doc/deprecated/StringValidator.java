package net.thevpc.nuts.lib.doc.deprecated;

public interface StringValidator {

    default String getHints() {
        return null;
    }

    StringValidatorType getType();

    String validate(String value);
}
