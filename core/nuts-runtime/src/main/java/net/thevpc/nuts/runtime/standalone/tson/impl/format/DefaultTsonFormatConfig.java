package net.thevpc.nuts.runtime.standalone.tson.impl.format;

class DefaultTsonFormatConfig implements Cloneable {
    boolean ignoreObjectEmptyArrayFields;
    boolean ignoreObjectNullFields;
    String indent0 = " ";
    String afterMultiLineComments = " ";
    String afterAnnotation = " ";
    String afterAnnotations = " ";
    String afterComma = " ";
    String afterKey = " ";
    String beforeValue = " ";
    String indent = indent0;
    boolean showFormatNumber = true;
    boolean indentList = false;
    boolean indentBraces = true;
    boolean indentBrackets = true;
    boolean showComments = true;
    boolean showAnnotations = true;
    boolean compact = false;
    int lineLength = 80;

    public Object get(String optionName) {
        if (optionName != null) {
            switch (optionName) {
                case "compact":
                    return compact;
                case "indent":
                    return indent0;
                case "ignoreObjectNullFields":
                    return ignoreObjectNullFields;
                case "ignoreObjectEmptyArrayFields":
                    return ignoreObjectEmptyArrayFields;
                case "lineLength":
                    return lineLength;
            }
        }
        return null;
    }

    public DefaultTsonFormatConfig set(String optionName, Object configValue) {
        if (optionName != null) {
            switch (optionName) {
                case "compact": {
                    setCompact(Boolean.valueOf(String.valueOf(configValue)));
                    break;
                }
                case "ignoreObjectNullFields": {
                    setIgnoreObjectNullFields(Boolean.valueOf(String.valueOf(configValue)));
                    break;
                }
                case "ignoreObjectEmptyArrayFields": {
                    setIgnoreObjectEmptyArrayFields(Boolean.valueOf(String.valueOf(configValue)));
                    break;
                }
                case "lineLength": {
                    setLineLength(((Number)configValue).intValue());
                    break;
                }
                case "indent": {
                    if (configValue instanceof Integer) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < (Integer) configValue; i++) {
                            sb.append(" ");
                        }
                        indent0 = sb.toString();
                    } else if (configValue instanceof String) {
                        String s = configValue.toString();
                        if (s.length() > 0 && s.charAt(0) >= '0' && s.charAt(0) <= '9') {
                            //in
                            set("indent", Integer.parseInt(s));
                        } else {
                            this.indent0 = s;
                        }
                    } else {
                        throw new IllegalArgumentException("Unaccepted indent " + configValue);
                    }
                    break;
                }
            }
        }
        return this;
    }

    public boolean isIgnoreObjectEmptyArrayFields() {
        return ignoreObjectEmptyArrayFields;
    }

    public DefaultTsonFormatConfig setIgnoreObjectEmptyArrayFields(boolean ignoreObjectEmptyArrayFields) {
        this.ignoreObjectEmptyArrayFields = ignoreObjectEmptyArrayFields;
        return this;
    }

    public boolean isIgnoreObjectNullFields() {
        return ignoreObjectNullFields;
    }

    public DefaultTsonFormatConfig setIgnoreObjectNullFields(boolean ignoreObjectNullFields) {
        this.ignoreObjectNullFields = ignoreObjectNullFields;
        return this;
    }

    public DefaultTsonFormatConfig setCompact(boolean compact) {
        this.compact = compact;
        if (compact) {
            setBeforeValue("")
                    .setAfterComma("")
                    .setAfterKey("")
                    .setIndent("")
                    .setIndentBraces(false)
                    .setIndentBrackets(false)
                    .setIndentList(false)
                    .setShowComments(false)
                    .setShowAnnotations(true)
                    .setAfterComments(" ")
                    .setAfterAnnotation(" ")
                    .setAfterAnnotations(" ");
        } else {
            setBeforeValue(" ")
                    .setAfterComma(" ")
                    .setAfterKey(" ")
                    .setIndent(indent0)
                    .setIndentBraces(true)
                    .setIndentBrackets(true)
                    .setIndentList(false)
                    .setShowComments(true)
                    .setShowAnnotations(true)
                    .setAfterComments("\n")
                    .setAfterAnnotation("\n")
                    .setAfterAnnotations("\n");
        }
        return this;
    }

    public boolean isCompact() {
        return compact;
    }


    public String getAfterComments() {
        return afterMultiLineComments;
    }

    public DefaultTsonFormatConfig setAfterComments(String afterComments) {
        this.afterMultiLineComments = afterComments;
        return this;
    }

    public String getAfterAnnotation() {
        return afterAnnotation;
    }

    public DefaultTsonFormatConfig setAfterAnnotation(String afterAnnotation) {
        this.afterAnnotation = afterAnnotation;
        return this;
    }

    public String getAfterAnnotations() {
        return afterAnnotations;
    }

    public DefaultTsonFormatConfig setAfterAnnotations(String afterAnnotations) {
        this.afterAnnotations = afterAnnotations;
        return this;
    }

    public boolean isShowAnnotations() {
        return showAnnotations;
    }

    public DefaultTsonFormatConfig setShowAnnotations(boolean showAnnotations) {
        this.showAnnotations = showAnnotations;
        return this;
    }

    public boolean isShowComments() {
        return showComments;
    }

    public DefaultTsonFormatConfig setShowComments(boolean showComments) {
        this.showComments = showComments;
        return this;
    }

    public String getAfterComma() {
        return afterComma;
    }

    public DefaultTsonFormatConfig setAfterComma(String afterComma) {
        this.afterComma = afterComma;
        return this;
    }

    public String getAfterKey() {
        return afterKey;
    }

    public DefaultTsonFormatConfig setAfterKey(String afterKey) {
        this.afterKey = afterKey;
        return this;
    }

    public String getBeforeValue() {
        return beforeValue;
    }

    public DefaultTsonFormatConfig setBeforeValue(String beforeValue) {
        this.beforeValue = beforeValue;
        return this;
    }

    public String getIndent() {
        return indent;
    }

    public DefaultTsonFormatConfig setIndent(String indent) {
        this.indent = indent;
        return this;
    }

    public boolean isIndentList() {
        return indentList;
    }

    public DefaultTsonFormatConfig setIndentList(boolean indentList) {
        this.indentList = indentList;
        return this;
    }

    public boolean isIndentBraces() {
        return indentBraces;
    }

    public DefaultTsonFormatConfig setIndentBraces(boolean indentBraces) {
        this.indentBraces = indentBraces;
        return this;
    }

    public boolean isIndentBrackets() {
        return indentBrackets;
    }

    public DefaultTsonFormatConfig setIndentBrackets(boolean indentBrackets) {
        this.indentBrackets = indentBrackets;
        return this;
    }

    public int getLineLength() {
        return lineLength;
    }

    public DefaultTsonFormatConfig setLineLength(int lineLength) {
        this.lineLength = lineLength;
        return this;
    }

    public DefaultTsonFormatConfig copy() {
        try {
            return (DefaultTsonFormatConfig) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("Cannot clone");
        }
    }
}
