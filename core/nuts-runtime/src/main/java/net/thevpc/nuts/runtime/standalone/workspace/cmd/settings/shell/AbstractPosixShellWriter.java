package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.shell;

import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.util.NStringUtils;

public abstract class AbstractPosixShellWriter extends AbstractShellWriter {
    public AbstractPosixShellWriter(NShellFamily family) {
        super(NShellFamily.BASH);
        switch (family) {
            case BASH: {
                out().println("#!/bin/bash");
                break;
            }
            case SH: {
                out().println("#!/bin/sh");
                break;
            }
            case ZSH: {
                out().println("#!/bin/zsh");
                break;
            }
            default: {
                throw new IllegalArgumentException("unsupported shell family " + family);
            }
        }
    }

    @Override
    protected String lineCommentImpl(String anyString) {
        return "## " + anyString;
    }

    @Override
    protected String codeCommentImpl(String anyString) {
        return "# " + anyString;
    }

    @Override
    public NShellWriter echoOff() {
        // do nothing
        out().println("set +x");
        return this;
    }

    @Override
    public NShellWriter echoOn() {
        // do nothing
        out().println("set -x");
        return this;
    }

    @Override
    public NShellWriter printlnSetVarScriptPath(String varName) {
        printlnCommandImpl(varName + "=\"${BASH_SOURCE[0]:-${(%):-%x}}\"");
        return this;
    }

    @Override
    public NShellWriter printlnSetVarFolderPath(String varName, String fromPathVarName) {
        printlnCommandImpl(varName + "=\"$(cd -- \"$(dirname -- \"$" + fromPathVarName + "\")\" && pwd)\"");
        return this;
    }

    public NShellWriter printlnPrepareJavaCommand(String javaCommand, String javaHomeVarName, int minJavaVersion, boolean preferJavaW) {
        out().println("if [ -n \"$" + javaHomeVarName + "\" ] && [ -x \"$" + javaHomeVarName + "/bin/java\" ];  then");
        out().println("    " + javaCommand + "=\"$" + javaHomeVarName + "/bin/java\"\n");
        out().println("elif (type -p java > /dev/null) ; then\n");
        out().println("    " + javaCommand + "=java\n");
        out().println("elif [ -n \"$JAVA_HOME\" ] && [ -x \"$JAVA_HOME/bin/java\" ];  then\n");
        out().println("#    echo found java executable in JAVA_HOME\n");
        out().println("    " + javaCommand + "=\"$JAVA_HOME/bin/java\"\n");
        out().println("else\n");
        out().println("    " + javaCommand + "=\"java\"\n");
        out().println("fi\n");
        out().println("\n");
        out().println("if [ \"$" + javaCommand + "\" ]; then\n");
        out().println("    version=$(\"$" + javaCommand + "\" -version 2>&1 | awk -F '\"' '/version/ {print $2}')\n");
        out().println("    major=`echo $version | cut -d. -f1`\n");
        out().println("    minor=`echo $version | cut -d. -f2`\n");
        out().println("    if [ \"$major\" -eq \"1\" ]; then\n");
        out().println("        major=$minor\n");
        out().println("    fi\n");
        out().println("    if [ \"$major\" -lt \"" + minJavaVersion + "\" ]; then\n");
        out().println("        echo expected " + minJavaVersion + "+ java version, found $version;\n");
        out().println("        exit 204;\n");
        out().println("    fi\n");
        out().println("fi\n");
        return this;
    }

    @Override
    public NShellWriter printlnSetVar(String varName, String varExpr) {
        String u = replaceDollarVar(varExpr);
        StringBuilder sb = new StringBuilder();
        for (char c : u.toCharArray()) {
            switch (c) {
                case '\\': {
                    sb.append("\\\\");
                    break;
                }
                case '\n': {
                    sb.append("\\n");
                    break;
                }
                case '\t': {
                    sb.append("\\t");
                    break;
                }
                case '"': {
                    sb.append("\\\"");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        printlnCommandImpl(varName + "=\"" + sb + "\"");
        return this;
    }

    @Override
    public String varValue(String varName) {
        if (NStringUtils.isEmpty(varName)) {
            return "${NULL}";
        }
        switch (varName) {
            case "*": {
                return "\"$@\"";
            }
        }
        return "${" + varName + "}";
    }

}
