package net.thevpc.nuts.installer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ProcessUtils {

    public static String[] parseCmdLine(String commandLineString) {
        if (commandLineString == null) {
            return new String[0];
        }
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        final int START = 0;
        final int IN_WORD = 1;
        final int IN_QUOTED_WORD = 2;
        final int IN_DBQUOTED_WORD = 3;
        int status = START;
        char[] charArray = commandLineString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            switch (status) {
                case START: {
                    switch (c) {
                        case ' ':
                        case '\n':
                        case '\r':
                        {
                            //ignore
                            break;
                        }
                        case '\'': {
                            status = IN_QUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '"': {
                            status = IN_DBQUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '\\': {
                            status = IN_WORD;
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            status = IN_WORD;
                            break;
                        }
                    }
                    break;
                }
                case IN_WORD: {
                    switch (c) {
                        case ' ': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            break;
                        }
                        case '\'': {
                            throw new IllegalArgumentException("Illegal char " + c);
                        }
                        case '"': {
                            throw new IllegalArgumentException("Illegal char " + c);
                        }
                        case '\\': {
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            break;
                        }
                    }
                    break;
                }
                case IN_QUOTED_WORD: {
                    switch (c) {
                        case '\'': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        case '\\': {
                            i = readEscaped(charArray, i + 1, sb);
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                    break;
                }
                case IN_DBQUOTED_WORD: {
                    switch (c) {
                        case '"': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        case '\\': {
                            i = readEscaped(charArray, i + 1, sb);
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                }
            }
        }
        switch (status) {
            case START: {
                break;
            }
            case IN_WORD: {
                args.add(sb.toString());
                sb.delete(0, sb.length());
                break;
            }
            case IN_QUOTED_WORD: {
                throw new IllegalArgumentException("Expected '");
            }
        }
        return args.toArray(new String[0]);
    }

    private static int readEscaped(char[] charArray, int i, StringBuilder sb) {
        char c = charArray[i];
        switch (c) {
            case 'n': {
                sb.append('\n');
                break;
            }
            case 't': {
                sb.append('\t');
                break;
            }
            case 'r': {
                sb.append('\r');
                break;
            }
            case 'f': {
                sb.append('\f');
                break;
            }
            default: {
                sb.append(c);
            }
        }
        return i;
    }

    private int runCommand(String[] command, StringBuilder out, StringBuilder err) {
        return runCommand(command, out::append, err::append);
    }

    private int runCommand(String[] command, Consumer<String> out, Consumer<String> err) {
        out.accept("start process...\n");
        ProcessBuilder sb = new ProcessBuilder();
        sb.command(Arrays.asList(command));
        Process p = null;
        try {
            p = sb.start();
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), err);
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), out);
            errorGobbler.start();
            outputGobbler.start();
            return p.waitFor();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
