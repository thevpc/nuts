/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting
 * a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.text.art.figlet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextArtRenderer;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author vpc
 */
public class FigletNTextArtImageRenderer implements NTextArtRenderer, Cloneable {

    public static final int SM_SMUSH_EQUAL = 1;       // bit 0: Equal character smushing
    public static final int SM_SMUSH_UNDERSCORE = 2;  // bit 1: Underscore smushing  
    public static final int SM_SMUSH_HIERARCHY = 4;   // bit 2: Hierarchy smushing
    public static final int SM_SMUSH_PAIR = 8;        // bit 3: Opposite pair smushing
    public static final int SM_SMUSH_BIGX = 16;       // bit 4: Big X smushing
    public static final int SM_SMUSH_HARDBLANK = 32;  // bit 5: Hardblank smushing

    public static final int SM_KERN = 64;             // bit 6: Kerning
    public static final int SM_SMUSH = 128;           // bit 7: Smushing

    private final static int CHARS_REGULAR = 102;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final static int[] CHARS_POS_MAPPER = new int[CHARS_REGULAR];

    static {
        int j = 0;
        for (int c = 32; c <= 126; ++c) {
            CHARS_POS_MAPPER[j++] = c;
        }
        for (int additional : new int[]{196, 214, 220, 228, 246, 252, 223}) {
            CHARS_POS_MAPPER[j++] = additional;
        }
    }

    private char hardBlank;
    private int height;
    private int heightWithoutDescenders;
    private int maxLine;
    private int smushingMode;
    private Map<Integer, FigletChar> chars = new HashMap<>();
    private String fontName = "";

    public static NOptional<FigletNTextArtImageRenderer> ofName(String name) {
        try {
            URL u = Thread.currentThread().getContextClassLoader().getResource("META-INF/textart/" + name + ".flf");
            if (u != null) {
                return NOptional.of(new FigletNTextArtImageRenderer(NPath.of(u)));
            }
        } catch (Exception ex) {
            return NOptional.ofNamedEmpty(NMsg.ofC("font %s not found", name));
        }
        try {
            URL u = FigletNTextArtImageRenderer.class.getClassLoader().getResource("META-INF/textart/" + name + ".flf");
            if (u != null) {
                return NOptional.of(new FigletNTextArtImageRenderer(NPath.of(u)));
            }
        } catch (Exception ex) {
            return NOptional.ofNamedEmpty(NMsg.ofC("font %s not found", name));
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("font %s not found", name));
    }

    public FigletNTextArtImageRenderer(NPath file) {
        try (InputStream in = file.getInputStream()) {
            load(in);
            fontName=file.nameParts().getBaseName();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static boolean acceptContent(String content) {
        if (content != null && content.startsWith("flf2")) {
            return true;
        }
        return false;
    }

    public FigletNTextArtImageRenderer(InputStream stream) {
        load(stream);
    }

    protected FigletNTextArtImageRenderer clone() {
        try {
            return (FigletNTextArtImageRenderer) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new IllegalArgumentException("unsupported clone");
        }
    }

    @Override
    public String getName() {
        return "figlet:" + NStringUtils.firstNonBlank(fontName, "noname");
    }

    private void load(InputStream stream) {
        chars.clear();
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(stream), "UTF-8"))) {
                String line = br.readLine();
                String[] st = NStringUtils.split(line.trim(), " ").toArray(new String[0]);
                hardBlank = st[0].charAt(st[0].length() - 1);
                height = Integer.parseInt(st[1]);
                heightWithoutDescenders = Integer.parseInt(st[2]);
                maxLine = Integer.parseInt(st[3]);
                smushingMode = Integer.parseInt(st[4]);
                int remaining = Integer.parseInt(st[5]);

                // Skip comment lines
                for (int i = 0; i < remaining; i++) {
                    line = br.readLine();
                    if (i == 0 && line != null) {
                        String[] parts = NStringUtils.split(line.trim(), " ").toArray(new String[0]);
                        if (parts.length > 0) {
                            fontName = parts[0];
                        }
                    }
                }

                // Read regular characters (32-126 + German chars)
                for (int charPos = 0; charPos < CHARS_REGULAR; charPos++) {
                    int charCode = CHARS_POS_MAPPER[charPos];
                    char[][] charData = readCharacterData(br);
                    if (charData != null) {
                        chars.put(charCode, new FigletCharSimple(charData));
                    }
                }

                // Read extended characters
                String nextLine;
                while ((nextLine = br.readLine()) != null) {
                    nextLine = nextLine.trim();

                    // Skip empty lines
                    if (nextLine.isEmpty()) {
                        continue;
                    }

                    try {
                        int charCode = parseInt(nextLine);
                        char[][] charData = readCharacterData(br);
                        if (charData != null) {
                            chars.put(charCode, new FigletCharSimple(charData));
                        }
                    } catch (NumberFormatException e) {
                        // This line is not a character code, it might be a mapping section
                        // Skip until we find the end of the mapping section (line ending with @@)
                        if (nextLine.endsWith("@@")) {
                            // Single line mapping section, already consumed
                            continue;
                        } else {
                            // Multi-line mapping section, read until we find @@
                            String mappingLine;
                            while ((mappingLine = br.readLine()) != null) {
                                if (mappingLine.trim().endsWith("@@")) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private char[][] readCharacterData(BufferedReader br) throws IOException {
        char[][] map = new char[height][];
        for (int i = 0; i < height; i++) {
            String line = br.readLine();
            if (line == null) {
                return null; // End of file
            }
            map[i] = parseCharacterLine(line, i);
        }
        return map;
    }

    private char[] parseCharacterLine(String line, int lineIndex) {
        if (line == null || line.isEmpty()) {
            return new char[0];
        }

        // Find the end mark character(s)
        // For the last line of a character, there are 2 end marks
        // For other lines, there is 1 end mark
        int endMarkCount = (lineIndex == height - 1) ? 2 : 1;

        // Special case: if height is 1, only remove 1 end mark
        if (height == 1) {
            endMarkCount = 1;
        }

        int charWidth = Math.max(0, line.length() - endMarkCount);
        char[] result = new char[charWidth];

        for (int j = 0; j < charWidth; j++) {
            char c = line.charAt(j);
            result[j] = (c == hardBlank) ? ' ' : c;
        }

        return result;
    }

    private int parseInt(String input) {
        List<String> s = NStringUtils.split(input.trim(), " ");
        String codeTag = s.isEmpty() ? input : s.get(0);
        if (codeTag.matches("^0[xX][0-9A-Fa-f]+$")) {
            return Integer.parseInt(codeTag.substring(2), 16);
        } else if (codeTag.matches("^0[0-7]+$")) {
            return Integer.parseInt(codeTag.substring(1), 8);
        } else {
            return Integer.parseInt(codeTag);
        }
    }

    @Override
    public NText render(NText text) {
        String filteredText = text.filteredText();

        // Convert Unicode characters that we can't handle to spaces or question marks
        StringBuilder processedText = new StringBuilder();
        for (int i = 0; i < filteredText.length(); i++) {
            char c = filteredText.charAt(i);
            if (chars.containsKey((int) c)) {
                processedText.append(c);
            } else if (c >= 32 && c <= 126) {
                processedText.append(c);
            } else {
                // Skip unsupported Unicode characters like emojis
                // or replace with space: processedText.append(' ');
            }
        }

        String textToRender = processedText.toString();
        if (textToRender.isEmpty()) {
            return NText.of("");
        }

        NTextBuilder[] rows = new NTextBuilder[height];
        for (int i = 0; i < height; i++) {
            rows[i] = NTextBuilder.of();
        }

        for (int c = 0; c < textToRender.length(); c++) {
            char currentChar = textToRender.charAt(c);
            FigletChar fc = chars.get((int) currentChar);

            if (fc == null) {
                // Try space as fallback
                fc = chars.get(32);
                if (fc == null) {
                    continue;
                }
            }

            for (int row = 0; row < height; row++) {
                NText charRow = fc.getRow(row);

                if (c == 0) {
                    // First character - just append
                    rows[row].append(charRow);
                } else {
                    // Subsequent characters - apply smushing/kerning
                    appendWithSmushing(rows[row], charRow, smushingMode);
                }
            }
        }

        NTextBuilder result = NTextBuilder.of();
        for (int i = 0; i < rows.length; i++) {
            result.append(rows[i]);
            if (i < rows.length - 1) {
                result.append(LINE_SEPARATOR);
            }
        }
        return result.build();
    }

    private void appendWithSmushing(NTextBuilder currentRow, NText newRow, int mode) {
        String current = currentRow.filteredText();
        String newText = newRow.filteredText();

        if (current.isEmpty()) {
            currentRow.append(newRow);
            return;
        }

        if (newText.isEmpty()) {
            return;
        }

        // Determine the spacing method based on the mode
        int spacing = calculateSpacing(current, newText, mode);

        if (spacing < 0) {
            // Overlap/smush - remove characters from current and merge
            int overlap = -spacing;
            performSmush(currentRow, newRow, overlap, mode);
        } else if (spacing == 0) {
            // No gap - direct concatenation
            currentRow.append(newRow);
        } else {
            // Add spacing
            for (int i = 0; i < spacing; i++) {
                currentRow.append(' ');
            }
            currentRow.append(newRow);
        }
    }

    private int calculateSpacing(String current, String newText, int mode) {
        // If no smushing/kerning is enabled, add one space
        if ((mode & (SM_SMUSH | SM_KERN)) == 0) {
            return 1;
        }

        // Try to find maximum overlap
        int maxOverlap = Math.min(current.length(), newText.length());

        for (int overlap = maxOverlap; overlap >= 1; overlap--) {
            if (canOverlapAt(current, newText, overlap, mode)) {
                return -overlap; // Negative indicates overlap
            }
        }

        // If kerning is enabled but no overlap possible, use kerning (no space)
        if ((mode & SM_KERN) != 0) {
            return 0;
        }

        // Default: add one space
        return 1;
    }

    private boolean canOverlapAt(String current, String newText, int overlap, int mode) {
        // Check if we can overlap by the specified amount
        for (int i = 0; i < overlap; i++) {
            char leftChar = current.charAt(current.length() - overlap + i);
            char rightChar = newText.charAt(i);

            if (!canSmushPair(leftChar, rightChar, mode)) {
                return false;
            }
        }
        return true;
    }

    private boolean canSmushPair(char left, char right, int mode) {
        // Space always allows overlap
        if (left == ' ' || right == ' ') {
            return true;
        }

        // If smushing is not enabled, only spaces can overlap
        if ((mode & SM_SMUSH) == 0) {
            return false;
        }

        // Apply smushing rules in order
        if ((mode & SM_SMUSH_EQUAL) != 0 && left == right) {
            return true;
        }

        if ((mode & SM_SMUSH_UNDERSCORE) != 0) {
            String smushChars = "|/\\[]{}()<>";
            if ((left == '_' && smushChars.indexOf(right) >= 0)
                    || (right == '_' && smushChars.indexOf(left) >= 0)) {
                return true;
            }
        }

        if ((mode & SM_SMUSH_HIERARCHY) != 0) {
            String hierarchy = "|/\\[]{}()<>";
            if (hierarchy.indexOf(left) >= 0 && hierarchy.indexOf(right) >= 0) {
                return true;
            }
        }

        if ((mode & SM_SMUSH_PAIR) != 0) {
            if ((left == '[' && right == ']') || (left == ']' && right == '[')
                    || (left == '{' && right == '}') || (left == '}' && right == '{')
                    || (left == '(' && right == ')') || (left == ')' && right == '(')) {
                return true;
            }
        }

        if ((mode & SM_SMUSH_BIGX) != 0) {
            if ((left == '/' && right == '\\') || (left == '\\' && right == '/')
                    || (left == '>' && right == '<') || (left == '<' && right == '>')) {
                return true;
            }
        }

        if ((mode & SM_SMUSH_HARDBLANK) != 0 && left == hardBlank && right == hardBlank) {
            return true;
        }

        return false;
    }

    private void performSmush(NTextBuilder currentRow, NText newRow, int overlap, int mode) {
        String current = currentRow.filteredText();
        String newText = newRow.filteredText();

        // Build the smushed section
        StringBuilder smushed = new StringBuilder();
        for (int i = 0; i < overlap; i++) {
            char leftChar = current.charAt(current.length() - overlap + i);
            char rightChar = newText.charAt(i);
            smushed.append(smushChars(leftChar, rightChar, mode));
        }

        // Remove the overlapped portion from current
        int currentLength = currentRow.textLength();
        currentRow.delete(currentLength - overlap, currentLength);

        // Add the smushed section
        currentRow.append(smushed.toString());

        // Add the remaining part of the new text
        if (overlap < newText.length()) {
            currentRow.append(newText.substring(overlap));
        }
    }

    private char smushChars(char left, char right, int mode) {
        if (left == ' ') {
            return right;
        }
        if (right == ' ') {
            return left;
        }

        if ((mode & SM_SMUSH_EQUAL) != 0 && left == right) {
            return left;
        }

        if ((mode & SM_SMUSH_UNDERSCORE) != 0) {
            String smushChars = "|/\\[]{}()<>";
            if (left == '_' && smushChars.indexOf(right) >= 0) {
                return right;
            }
            if (right == '_' && smushChars.indexOf(left) >= 0) {
                return left;
            }
        }

        if ((mode & SM_SMUSH_HIERARCHY) != 0) {
            String hierarchy = "|/\\[]{}()<>";
            int leftPos = hierarchy.indexOf(left);
            int rightPos = hierarchy.indexOf(right);
            if (leftPos >= 0 && rightPos >= 0) {
                return (leftPos < rightPos) ? left : right;
            }
        }

        if ((mode & SM_SMUSH_PAIR) != 0) {
            if ((left == '[' && right == ']') || (left == ']' && right == '[')) {
                return '|';
            }
            if ((left == '{' && right == '}') || (left == '}' && right == '{')) {
                return '|';
            }
            if ((left == '(' && right == ')') || (left == ')' && right == '(')) {
                return '|';
            }
        }

        if ((mode & SM_SMUSH_BIGX) != 0) {
            if ((left == '/' && right == '\\') || (left == '\\' && right == '/')) {
                return '|';
            }
            if ((left == '>' && right == '<') || (left == '<' && right == '>')) {
                return 'X';
            }
        }

        if ((mode & SM_SMUSH_HARDBLANK) != 0 && left == hardBlank && right == hardBlank) {
            return ' ';
        }

        // Default: return right character
        return right;
    }
}
