package net.thevpc.nuts.platform;

import net.thevpc.nuts.util.*;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * NSysEditorFamily enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NSysEditorFamily implements NEnum {
    INTELLIJ, KATE, GEDIT, VIM, JEDIT, NOTEPAD_PLUS_PLUS, VSCODE;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

  /**
   * N sys editor family.
   */
    NSysEditorFamily() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse set.
     *
     * @param value value
     * @return parse set result
     */
    public static NOptional<Set<NSysEditorFamily>> parseSet(String value) {
        LinkedHashSet<NSysEditorFamily> all = new LinkedHashSet<>();
        for (String s : NStringUtils.split(value, ";,", true, true)) {
            NOptional<NSysEditorFamily> u = parse(s);
            if (!u.isPresent()) {
                return NOptional.ofNamedEmpty(u.message().get());
            }
            all.add(u.get());
        }
        return NOptional.of(all);
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NSysEditorFamily> parse(String value) {
        return NEnumUtils.parseEnum(value, NSysEditorFamily.class, new NFunction<NEnumUtils.NEnumCandidate, NOptional<NSysEditorFamily>>() {
            @Override
            public NOptional<NSysEditorFamily> apply(NEnumUtils.NEnumCandidate enumValue) {
                switch (enumValue.normalizedValue()) {
                    case "IDEA":
                    case "INTELLIJI":
                    case "INTELLIJI_IDEA":
                    case "PY_CHARM":
                    case "PYCHARM":
                    case "WEB_STORM":
                    case "WEBSTORM":
                    {
                        return NOptional.of(INTELLIJ);
                    }
                    case "KATE": {
                        return NOptional.of(KATE);
                    }
                    case "GEDIT":
                    case "PLUMA":
                    case "XED":
                    {
                        return NOptional.of(GEDIT);
                    }
                    case "VI":
                    case "VIM": {
                        return NOptional.of(VIM);
                    }
                    case "JEDIT": {
                        return NOptional.of(JEDIT);
                    }
                    case "NOTEPAD++":
                    case "NOTEPADPP":
                    case "NOTEPADPLUSPLUS":
                    case "NOTEPAD-PLUS-PLUS": {
                        return NOptional.of(NOTEPAD_PLUS_PLUS);
                    }
                    case "VSCODE":
                    case "VSCODIUM":
                    case "ANTIGRAVITY":
                    {
                        return NOptional.of(VSCODE);
                    }
                }
                return null;
            }
        });
    }


    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }


}
