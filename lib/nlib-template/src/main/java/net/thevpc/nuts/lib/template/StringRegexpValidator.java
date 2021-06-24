/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package net.thevpc.nuts.lib.template;

/**
 *
 * @author thevpc
 */
public class StringRegexpValidator implements StringValidator {

    private String regexp;

    public StringRegexpValidator(String regexp) {
        this.regexp = regexp;
    }

    @Override
    public String validate(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Nul not accepted");
        }
        if (!value.matches(regexp)) {
            throw new IllegalArgumentException("Invalid characters. expected : " + regexp);
        }
        return value;
    }

    @Override
    public StringValidatorType getType() {
        return StringValidatorType.STRING;
    }

    @Override
    public String getHints() {
        return "use format ==x.y.z==";
    }

}
