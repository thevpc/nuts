package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;

import java.util.Arrays;

public class GoCodeHighlighter extends AbstractCodeHighlighter {

    public GoCodeHighlighter() {
        super("go");
        keywords.addAll(Arrays.asList(
                "break", "case", "chan", "const", "continue", "default", "defer", "else", "fallthrough",
                "for", "func", "go", "goto", "if", "import", "interface", "map", "package", "range",
                "return", "select", "struct", "switch", "type", "var"
        ));
        types.addAll(Arrays.asList(
                "any", "bool", "byte", "comparable", "complex64", "complex128", "error", "float32",
                "float64", "int", "int8", "int16", "int32", "int64", "rune", "string", "uint", "uint8",
                "uint16", "uint32", "uint64", "uintptr",
                "append", "cap", "clear", "close", "complex", "copy", "delete", "imag", "len", "make",
                "max", "min", "new", "panic", "print", "println", "real", "recover"
        ));
        booleanLiterals.addAll(Arrays.asList("true", "false"));
        nullLiterals.add("nil");
        keywords.add("iota");
        lineComments.add("//");
        blockCommentOpen = "/*";
        blockCommentClose = "*/";
        stringQuotes.add('\'');
        charLiterals = true;
        backtickStrings = true;
        numberSuffixChars = "iI";
        operatorChars = "+-*/%=<>!&|^~:.";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "go", "golang", "text/x-go", "text/go", "application/x-go");
    }
}
