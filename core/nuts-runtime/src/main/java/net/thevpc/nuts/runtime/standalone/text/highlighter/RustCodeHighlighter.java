package net.thevpc.nuts.runtime.standalone.text.highlighter;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;

public class RustCodeHighlighter implements NCodeHighlighter {

    private final Set<String> keywords = new HashSet<>();
    private final Set<String> primitives = new HashSet<>();

    public RustCodeHighlighter() {
        // Rust keywords
        keywords.addAll(Arrays.asList(
                "as", "async", "await", "break", "const", "continue",
                "crate", "dyn", "else", "enum", "extern", "false",
                "fn", "for", "if", "impl", "in", "let", "loop",
                "match", "mod", "move", "mut", "pub", "ref", "return",
                "self", "Self", "static", "struct", "super", "trait",
                "true", "type", "unsafe", "use", "where", "while",
                // reserved for future use
                "abstract", "become", "box", "do", "final", "macro",
                "override", "priv", "try", "typeof", "unsized", "virtual", "yield"
        ));
        // primitive types — styled as option() to distinguish from user types
        primitives.addAll(Arrays.asList(
                "bool", "char", "f32", "f64",
                "i8", "i16", "i32", "i64", "i128", "isize",
                "u8", "u16", "u32", "u64", "u128", "usize",
                "str", "String", "Vec", "Option", "Result",
                "Box", "Rc", "Arc", "Cell", "RefCell",
                "HashMap", "HashSet", "BTreeMap", "BTreeSet",
                "Path", "PathBuf", "OsStr", "OsString"
        ));
    }

    @Override
    public String id() {
        return "rust";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) return NScorable.DEFAULT_SCORE;
        switch (s) {
            case "rust":
            case "rs":
            case "text/x-rust":
                return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt) {
        return txt.ofPlain(text);
    }

    @Override
    public NText stringToText(String text, NTexts txt) {
        List<NText> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);

        while (ar.hasNext()) {
            char c = ar.peekChar();

            if (Character.isWhitespace(c)) {
                all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
                continue;
            }

            // --- Outer attribute  #[...]  or  #![...] ---
            if (c == '#') {
                List<NText> attr = tryReadAttribute(ar, txt);
                if (attr != null) {
                    all.addAll(attr);
                } else {
                    all.add(txt.ofPlain(String.valueOf(ar.readChar())));
                }
                continue;
            }

            // --- Comments: // doc /// and /* */ ---
            if (c == '/') {
                if (ar.hasNext(1) && ar.peekChar(1) == '/') {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(ar)));
                    continue;
                }
                if (ar.hasNext(1) && ar.peekChar(1) == '*') {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(ar)));
                    continue;
                }
                all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                continue;
            }

            // --- Lifetime annotation: 'a, 'static, 'lifetime ---
            if (c == '\'' && ar.hasNext(1) && Character.isLetter(ar.peekChar(1))) {
                // distinguish lifetime 'a from char literal 'x'
                // char literal: 'x' (single char or escape, then closing quote)
                // lifetime: 'ident not followed by closing quote after identifier
                List<NText> lt = tryReadLifetimeOrChar(ar, txt);
                all.addAll(lt);
                continue;
            }

            // --- Raw strings: r"..." r#"..."# r##"..."## ---
            if (c == 'r' && ar.hasNext(1) && (ar.peekChar(1) == '"' || ar.peekChar(1) == '#')) {
                List<NText> raw = tryReadRawString(ar, txt);
                if (raw != null) {
                    all.addAll(raw);
                    continue;
                }
            }

            // --- Byte literals: b'x' and byte strings b"..." ---
            if (c == 'b' && ar.hasNext(1)) {
                char next = ar.peekChar(1);
                if (next == '\'') {
                    all.addAll(readByteChar(ar, txt));
                    continue;
                }
                if (next == '"') {
                    all.addAll(readByteString(ar, txt));
                    continue;
                }
                // br#"..."# raw byte strings
                if (next == 'r' && ar.hasNext(2) && (ar.peekChar(2) == '"' || ar.peekChar(2) == '#')) {
                    List<NText> raw = tryReadRawString(ar, txt); // prefix consumed inside
                    if (raw != null) { all.addAll(raw); continue; }
                }
            }

            // --- Regular string "..." ---
            if (c == '"') {
                all.addAll(Arrays.asList(StringReaderExtUtils.readJSDoubleQuotesString(ar)));
                continue;
            }

            // --- Numbers ---
            if (Character.isDigit(c)) {
                all.addAll(readNumber(ar, txt));
                continue;
            }

            // --- Identifiers, keywords, macros, primitives ---
            if (Character.isLetter(c) || c == '_') {
                all.addAll(readIdentifier(ar, txt));
                continue;
            }

            // --- Operators and separators ---
            switch (c) {
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':
                case ',':
                case ';':
                case ':':
                case '=':
                case '+':
                case '-':
                case '*':
                case '%':
                case '<':
                case '>':
                case '!':
                case '&':
                case '|':
                case '^':
                case '~':
                case '?':
                case '@':
                case '.':
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    break;
                default:
                    all.add(txt.ofPlain(String.valueOf(ar.readChar())));
                    break;
            }
        }

        return txt.ofList(all.toArray(new NText[0]));
    }

    // -------------------------------------------------------------------------
    // Attributes  #[derive(Debug, Clone)]  #![allow(unused)]
    // -------------------------------------------------------------------------

    private List<NText> tryReadAttribute(StringReaderExt ar, NTexts txt) {
        // Must be # followed by optional ! then [
        int i = 1;
        if (!ar.hasNext(i)) return null;
        if (ar.peekChar(i) == '!') i++;
        if (!ar.hasNext(i) || ar.peekChar(i) != '[') return null;

        // consume until matching ]  (depth-aware, ignoring nested strings for simplicity)
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // #
        if (ar.hasNext() && ar.peekChar() == '!') sb.append(ar.readChar()); // !
        sb.append(ar.readChar()); // [
        int depth = 1;
        while (ar.hasNext() && depth > 0) {
            char c = ar.readChar();
            sb.append(c);
            if (c == '[') depth++;
            else if (c == ']') depth--;
        }
        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.annotation()));
    }

    // -------------------------------------------------------------------------
    // Lifetime 'a  vs  char literal 'x'
    // -------------------------------------------------------------------------

    private List<NText> tryReadLifetimeOrChar(StringReaderExt ar, NTexts txt) {
        // Speculatively check: is it a char literal?
        // char literal pattern: ' (char|escape) '
        int i = 1;
        if (ar.peekChar(i) == '\\') {
            i += 2; // escape sequence (rough)
        } else {
            i++; // single char
        }
        if (ar.hasNext(i) && ar.peekChar(i) == '\'') {
            // it's a char literal — consume it as string style
            StringBuilder sb = new StringBuilder();
            sb.append(ar.readChar()); // opening '
            if (ar.hasNext() && ar.peekChar() == '\\') {
                sb.append(ar.readChar());
                if (ar.hasNext()) sb.append(ar.readChar());
            } else if (ar.hasNext()) {
                sb.append(ar.readChar());
            }
            if (ar.hasNext() && ar.peekChar() == '\'') sb.append(ar.readChar()); // closing '
            return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.string()));
        }

        // lifetime: 'ident
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // '
        while (ar.hasNext() && (Character.isLetterOrDigit(ar.peekChar()) || ar.peekChar() == '_')) {
            sb.append(ar.readChar());
        }
        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.annotation()));
    }

    // -------------------------------------------------------------------------
    // Raw strings  r"..."  r#"..."#  r##"..."##
    // -------------------------------------------------------------------------

    private List<NText> tryReadRawString(StringReaderExt ar, NTexts txt) {
        StringBuilder sb = new StringBuilder();
        // optional b prefix
        if (ar.peekChar() == 'b') sb.append(ar.readChar());
        sb.append(ar.readChar()); // 'r'
        // count opening hashes
        int hashes = 0;
        while (ar.hasNext() && ar.peekChar() == '#') {
            sb.append(ar.readChar());
            hashes++;
        }
        if (!ar.hasNext() || ar.peekChar() != '"') return null; // not a raw string
        sb.append(ar.readChar()); // opening "

        // read until closing " followed by exactly `hashes` '#'
        outer:
        while (ar.hasNext()) {
            char c = ar.readChar();
            sb.append(c);
            if (c == '"') {
                int h = 0;
                while (h < hashes && ar.hasNext() && ar.peekChar() == '#') {
                    sb.append(ar.readChar());
                    h++;
                }
                if (h == hashes) break outer;
            }
        }
        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.string()));
    }

    // -------------------------------------------------------------------------
    // Byte char  b'x'  and byte string  b"..."
    // -------------------------------------------------------------------------

    private List<NText> readByteChar(StringReaderExt ar, NTexts txt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // b
        sb.append(ar.readChar()); // '
        if (ar.hasNext() && ar.peekChar() == '\\') {
            sb.append(ar.readChar());
            if (ar.hasNext()) sb.append(ar.readChar());
        } else if (ar.hasNext()) {
            sb.append(ar.readChar());
        }
        if (ar.hasNext() && ar.peekChar() == '\'') sb.append(ar.readChar());
        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.string()));
    }

    private List<NText> readByteString(StringReaderExt ar, NTexts txt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // b
        // now delegate to a simple double-quote reader
        sb.append(ar.readChar()); // opening "
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '\\') { sb.append(ar.readChar()); if (ar.hasNext()) sb.append(ar.readChar()); continue; }
            sb.append(ar.readChar());
            if (c == '"') break;
        }
        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.string()));
    }

    // -------------------------------------------------------------------------
    // Identifiers — including macro invocations  name!
    // -------------------------------------------------------------------------

    private List<NText> readIdentifier(StringReaderExt ar, NTexts txt) {
        StringBuilder sb = new StringBuilder();
        while (ar.hasNext() && (Character.isLetterOrDigit(ar.peekChar()) || ar.peekChar() == '_')) {
            sb.append(ar.readChar());
        }
        String word = sb.toString();

        // macro invocation: ident!  (but not != operator)
        if (ar.hasNext() && ar.peekChar() == '!'
                && !(ar.hasNext(1) && ar.peekChar(1) == '=')) {
            sb.append(ar.readChar()); // '!'
            return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.annotation()));
        }

        if (keywords.contains(word)) {
            return Collections.singletonList(txt.ofStyled(word, NTextStyle.keyword()));
        }
        if (primitives.contains(word)) {
            return Collections.singletonList(txt.ofStyled(word, NTextStyle.option()));
        }
        return Collections.singletonList(txt.ofPlain(word));
    }

    // -------------------------------------------------------------------------
    // Numbers: decimal, 0x, 0o, 0b, float, type suffixes (u8, f32, i64 …)
    // -------------------------------------------------------------------------

    private List<NText> readNumber(StringReaderExt ar, NTexts txt) {
        StringBuilder sb = new StringBuilder();

        if (ar.peekChar() == '0' && ar.hasNext(1)) {
            char next = ar.peekChar(1);
            if (next == 'x' || next == 'X') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && isHexOrUnderscore(ar.peekChar())) sb.append(ar.readChar());
                readNumericSuffix(ar, sb);
                return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
            }
            if (next == 'o' || next == 'O') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && (ar.peekChar() >= '0' && ar.peekChar() <= '7' || ar.peekChar() == '_')) sb.append(ar.readChar());
                readNumericSuffix(ar, sb);
                return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
            }
            if (next == 'b' || next == 'B') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && (ar.peekChar() == '0' || ar.peekChar() == '1' || ar.peekChar() == '_')) sb.append(ar.readChar());
                readNumericSuffix(ar, sb);
                return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
            }
        }

        while (ar.hasNext() && (Character.isDigit(ar.peekChar()) || ar.peekChar() == '_')) sb.append(ar.readChar());

        // float decimal
        if (ar.hasNext() && ar.peekChar() == '.' && ar.hasNext(1) && Character.isDigit(ar.peekChar(1))) {
            sb.append(ar.readChar());
            while (ar.hasNext() && (Character.isDigit(ar.peekChar()) || ar.peekChar() == '_')) sb.append(ar.readChar());
        }

        // exponent
        if (ar.hasNext() && (ar.peekChar() == 'e' || ar.peekChar() == 'E')) {
            sb.append(ar.readChar());
            if (ar.hasNext() && (ar.peekChar() == '+' || ar.peekChar() == '-')) sb.append(ar.readChar());
            while (ar.hasNext() && Character.isDigit(ar.peekChar())) sb.append(ar.readChar());
        }

        readNumericSuffix(ar, sb);
        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
    }

    /**
     * Rust numeric type suffixes: u8 u16 u32 u64 u128 usize
     *                              i8 i16 i32 i64 i128 isize
     *                              f32 f64
     */
    private void readNumericSuffix(StringReaderExt ar, StringBuilder sb) {
        if (!ar.hasNext()) return;
        char c = ar.peekChar();
        if (c == 'u' || c == 'i' || c == 'f') {
            // peek ahead to confirm it's a type suffix and not a stray letter
            int i = 1;
            while (ar.hasNext(i) && Character.isLetterOrDigit(ar.peekChar(i))) i++;
            // consume
            for (int j = 0; j < i; j++) sb.append(ar.readChar());
        }
    }

    private boolean isHexOrUnderscore(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F') || c == '_';
    }
}