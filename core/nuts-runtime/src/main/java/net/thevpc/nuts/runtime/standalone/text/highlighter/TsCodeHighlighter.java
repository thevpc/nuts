package net.thevpc.nuts.runtime.standalone.text.highlighter;
import java.util.Arrays;

import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;

public class TsCodeHighlighter extends JsCodeHighlighter {

    @Override
    public String id() {
        return "ts";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) return NScorable.DEFAULT_SCORE;
        switch (s) {
            case "ts":
            case "typescript":
            case "tsx":
            case "mts":
            case "cts":
            case "text/typescript":
            case "application/typescript":
                return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    protected void initKeywords() {
        super.initKeywords();
        // TS-only keywords
        keywords.addAll(Arrays.asList(
                // type system
                "type", "interface", "enum", "namespace", "module",
                "declare", "abstract", "override",
                // modifiers
                "public", "private", "protected", "readonly", "static",
                // type operators
                "keyof", "infer", "never", "unknown", "any", "object",
                "asserts", "is", "satisfies",
                // utility
                "implements"
        ));
    }

    @Override
    protected void initBuiltins() {
        super.initBuiltins();
        // TS built-in types and utility types
        builtins.addAll(Arrays.asList(
                // primitive types (used as annotations)
                "string", "number", "boolean", "bigint", "symbol", "void",
                // utility types
                "Partial", "Required", "Readonly", "Record", "Pick", "Omit",
                "Exclude", "Extract", "NonNullable", "ReturnType", "InstanceType",
                "Parameters", "ConstructorParameters", "Awaited",
                // other common globals
                "HTMLElement", "Document", "Window", "Event", "Element",
                "NodeList", "RequestInit", "Response", "Request", "URL",
                "URLSearchParams", "FormData", "Headers", "AbortController",
                "AbortSignal", "ReadableStream", "WritableStream"
        ));
    }
}