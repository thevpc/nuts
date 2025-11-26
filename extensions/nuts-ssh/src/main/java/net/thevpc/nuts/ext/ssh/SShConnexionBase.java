package net.thevpc.nuts.ext.ssh;

import java.util.ArrayList;
import java.util.List;

public abstract class SShConnexionBase implements ISShConnexion{
    protected List<SshListener> listeners = new ArrayList<>();
    public SShConnexionBase() {
    }

    @Override
    public ISShConnexion addListener(SshListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    @Override
    public ISShConnexion removeListener(SshListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
        return this;
    }

    @Override
    public int execListCommand(List<String> command, IOBindings io) {
        return execArrayCommand(command.toArray(new String[0]), io);
    }

    @Override
    public int execArrayCommand(String[] command, IOBindings io) {
        String sb = cmdArrayToString(command);
        return execStringCommand(sb, io);
    }

    protected String cmdArrayToString(String[] command) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.length; i++) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            String s = command[i];
            sb.append(doEscapeArg(s));
        }
        return sb.toString();
    }

    protected String doEscapeArg(String str) {
        if (str.isEmpty()) {
            return "\"\"";
        }
        StringBuilder notEscaped = new StringBuilder();
        StringBuilder escaped = new StringBuilder();
        boolean escape = false;
        for (char c : str.toCharArray()) {
            if (escape) {
                switch (c) {
                    case '\\':
                    case '\"': {
                        escaped.append("\\");
                        escaped.append(c);
                        break;
                    }
                    default: {
                        escaped.append(c);
                        break;
                    }
                }
            } else {
                if (Character.isWhitespace(c)) {
                    escape = true;
                    escaped.append(notEscaped);
                    escaped.append(c);
                    notEscaped.delete(0, notEscaped.length());
                } else {
                    switch (c) {
                        case '\\':
                        case '\"': {
                            escape = true;
                            escaped.append(notEscaped);
                            escaped.append("\\");
                            escaped.append(c);
                            notEscaped.delete(0, notEscaped.length());
                            break;
                        }
                        case '\'': {
                            escape = true;
                            escaped.append(notEscaped);
                            escaped.append(c);
                            notEscaped.delete(0, notEscaped.length());
                            break;
                        }
                        default: {
                            notEscaped.append(c);
                            break;
                        }
                    }
                }
            }
        }
        if (escape) {
            escaped.append("\"");
            return escaped.toString();
        } else {
            return notEscaped.toString();
        }
    }



}
