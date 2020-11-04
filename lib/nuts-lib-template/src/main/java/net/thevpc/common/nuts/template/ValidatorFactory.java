package net.thevpc.common.nuts.template;

import net.thevpc.nuts.NutsWorkspace;

public class ValidatorFactory {

    private NutsWorkspace ws;

    public ValidatorFactory(NutsWorkspace ws) {
        this.ws = ws;
    }

    public final StringValidator STRING = new StringValidator() {
        @Override
        public String validate(String value) {
            return value;
        }

        @Override
        public StringValidatorType getType() {
            return StringValidatorType.STRING;
        }
    };

    public final StringValidator DASHED_NAME = new StringRegexpValidator("[a-z][a-z0-9-]+[a-z-09]") {
        @Override
        public String validate(String value) {
            String r = super.validate(value);
            for (String s : r.split("-")) {
                if (JavaUtils.JAVA_KEYWORDS.contains(s)) {
                    throw new IllegalArgumentException("Invalid name " + value);
                }
            }
            return value;
        }
    };
    public final StringValidator JAVA_NAME = new StringRegexpValidator("[A-Z][a-zA-Z0-9_]+") {
        @Override
        public String validate(String value) {
            String r = super.validate(value);
            for (String s : r.split("-")) {
                if (JavaUtils.JAVA_KEYWORDS.contains(s)) {
                    throw new IllegalArgumentException("Invalid name " + value);
                }
            }
            return value;
        }
    };
    public final StringValidator MENU_PATH = new StringRegexpValidator("/|((/[a-zA-Z0-9-_]+)+)") {
        @Override
        public String validate(String value) {
            String r = super.validate(value);
            for (String s : r.split("/")) {
                if (JavaUtils.JAVA_KEYWORDS.contains(s.toLowerCase())) {
                    throw new IllegalArgumentException("Invalid name " + value);
                }
            }
            return value;
        }
    };

    public final StringValidator NAME = new StringValidator() {
        @Override
        public String validate(String value) {
            return value;
        }

        @Override
        public StringValidatorType getType() {
            return StringValidatorType.STRING;
        }

        @Override
        public String getHints() {
            return "consider lower case '-' separated name, like ==my-name==";
        }
    };

    public final StringValidator URL = new StringRegexpValidator("http[s]?://.*");

    public final StringValidator LABEL = new StringValidator() {
        @Override
        public String validate(String value) {
            return value;
        }

        @Override
        public StringValidatorType getType() {
            return StringValidatorType.STRING;
        }

        @Override
        public String getHints() {
            return "consider capitalized ' ' separated name, like ==My Name==";
        }
    };

    public final StringValidator VERSION = new StringRegexpValidator("[a-zA-Z0-9-_]([.][a-zA-Z0-9-_])*");
    public final StringValidator FOLDER = new StringValidator() {
        @Override
        public String validate(String value) {
            return value;
        }

        @Override
        public StringValidatorType getType() {
            return StringValidatorType.STRING;
        }
    };

    public final StringValidator PACKAGE = new StringRegexpValidator("[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*") {
        @Override
        public String validate(String value) {
            String r = super.validate(value);
            for (String s : r.split("\\.")) {
                if (JavaUtils.JAVA_KEYWORDS.contains(s)) {
                    throw new IllegalArgumentException("Invalid package " + value);
                }
            }
            return value;
        }
    };

    public final StringValidator BOOLEAN = new StringValidator() {
        @Override
        public String validate(String value) {
            Boolean b = ws.commandLine().createArgument(value).getBoolean(null);
            if (b == null) {
                throw new IllegalArgumentException("Invalid boolean");
            }
            return String.valueOf(b);
        }

        @Override
        public String getHints() {
            return "Accepted values are ==yes==, ==no==, ==true==, ==false==";
        }

        @Override
        public StringValidatorType getType() {
            return StringValidatorType.BOOLEAN;
        }
    };

}
