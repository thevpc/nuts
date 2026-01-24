package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NContentType;

public interface NElementFormatter extends NElementTransform {
    static NElementFormatter ofTsonPretty() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.TSON)
                .setStyle(NElementFormatterStyle.PRETTY)
                .build()
                ;
    }

    static NElementFormatter ofTsonCompact() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.TSON)
                .setStyle(NElementFormatterStyle.COMPACT)
                .build()
                ;
    }

    static NElementFormatter ofJsonPretty() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.JSON)
                .setStyle(NElementFormatterStyle.PRETTY)
                .build()
                ;
    }

    static NElementFormatter ofJsonCompact() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.JSON)
                .setStyle(NElementFormatterStyle.COMPACT)
                .build()
                ;
    }

    static NElementFormatter ofXmlPretty() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.XML)
                .setStyle(NElementFormatterStyle.PRETTY)
                .build()
                ;
    }

    static NElementFormatter ofXmlCompact() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.XML)
                .setStyle(NElementFormatterStyle.COMPACT)
                .build()
                ;
    }

    static NElementFormatter ofYamlPretty() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.YAML)
                .setStyle(NElementFormatterStyle.PRETTY)
                .build()
                ;
    }

    static NElementFormatter ofYamlCompact() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.YAML)
                .setStyle(NElementFormatterStyle.COMPACT)
                .build()
                ;
    }
    static NElementFormatterBuilder ofTsonPrettyBuilder() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.TSON)
                .setStyle(NElementFormatterStyle.PRETTY)
                ;
    }

    static NElementFormatterBuilder ofTsonCompactBuilder() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.TSON)
                .setStyle(NElementFormatterStyle.COMPACT)
                ;
    }

    static NElementFormatterBuilder ofJsonPrettyBuilder() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.JSON)
                .setStyle(NElementFormatterStyle.PRETTY)
                ;
    }

    static NElementFormatterBuilder ofJsonCompactBuilder() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.JSON)
                .setStyle(NElementFormatterStyle.COMPACT)
                ;
    }

    static NElementFormatterBuilder ofXmlPrettyBuilder() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.XML)
                .setStyle(NElementFormatterStyle.PRETTY)
                ;
    }

    static NElementFormatterBuilder ofXmlCompactBuilder() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.XML)
                .setStyle(NElementFormatterStyle.COMPACT)
                ;
    }

    static NElementFormatterBuilder ofYamlPrettyBuilder() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.YAML)
                .setStyle(NElementFormatterStyle.PRETTY)
                ;
    }

    static NElementFormatterBuilder ofYamlCompactBuilder() {
        return NElementFormatterBuilder.of()
                .setContentType(NContentType.YAML)
                .setStyle(NElementFormatterStyle.COMPACT)
                ;
    }
}
