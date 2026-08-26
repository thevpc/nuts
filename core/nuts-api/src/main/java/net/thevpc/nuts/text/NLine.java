package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * NLine class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NLine {
    private String content;
    private NNewLineMode newLine;

    /**
     * Parse list.
     *
     * @param input input
     * @return parse list result
     */
    public static List<NLine> parseList(String input) {
        List<NLine> lines = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return lines;
        }

        int len = input.length();
        int start = 0;

        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);

            if (c == '\n' || c == '\r') {
                NNewLineMode delimiter;
                int endOfDelimiter = i + 1;
                if (c == '\r' && i + 1 < len && input.charAt(i + 1) == '\n') {
                    delimiter = NNewLineMode.CRLF;
                    endOfDelimiter = i + 2;
                } else {
                    delimiter = c == '\n' ? NNewLineMode.LF : NNewLineMode.CR;
                    ;
                }
                lines.add(new NLine(input.substring(start, i), delimiter, true));

                i = endOfDelimiter - 1;
                start = endOfDelimiter;
            }
        }
        if (start < len) {
            lines.add(new NLine(input.substring(start), null, true));
        } else {
            lines.add(new NLine("", null, true));
        }
        return lines;
    }

    /**
     * N line.
     *
     * @param content content
     * @param newLine new line
     * @return n line result
     */
    public NLine(String content, NNewLineMode newLine) {
      /**
       * This.
       *
       * @param content content
       * @param newLine new line
       * @param false false
       */
        this(content, newLine, false);
    }

    /**
     * N line.
     *
     * @param content content
     * @param newLine new line
     * @param safe safe
     * @return n line result
     */
    private NLine(String content, NNewLineMode newLine, boolean safe) {
        if (safe) {
            this.content = content;
            this.newLine = newLine;
        } else {
            if (content == null) {
                this.content = "";
            } else {
                for (int i = 0; i < content.length(); i++) {
                    char c = content.charAt(i);
                    if (c == '\r' || c == '\n') {
                        /**
                         * N illegal argument exception.
                         *
                         * @param newLine") new line")
                         * @return n illegal argument exception result
                         */
                        throw new NIllegalArgumentException(NMsg.ofC("unexpected newLine"));
                    }
                }
            }
            this.newLine = newLine;
        }
    }

    /**
     * Content.
     *
     * @return content result
     */
    public String content() {
        return content;
    }

    /**
     * New line.
     *
     * @return new line result
     */
    public NNewLineMode newLine() {
        return newLine;
    }

    @Override
    public String toString() {
        if (newLine == null) {
            return content;
        }
        return content + newLine.value();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NLine nLine = (NLine) o;
        return Objects.equals(content, nLine.content) && newLine == nLine.newLine;
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, newLine);
    }

}
