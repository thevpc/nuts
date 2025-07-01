/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NAnsiTermHelper {
    private static final int[] FG8 = {30, 31, 32, 33, 34, 35, 36, 37, 90, 91, 92, 93, 94, 95, 96, 97};
    private static final int[] BG8 = {40, 41, 42, 43, 44, 45, 46, 47, 100, 101, 102, 103, 104, 105, 106, 107};
    public static java.util.List<Color> ANSI_COLORS_16= Collections.unmodifiableList(Arrays.asList(
            new Color(0, 0, 0),         // 0: Black
            new Color(128, 0, 0),       // 1: Red
            new Color(0, 128, 0),       // 2: Green
            new Color(128, 128, 0),     // 3: Yellow
            new Color(0, 0, 128),       // 4: Blue
            new Color(128, 0, 128),     // 5: Magenta
            new Color(0, 128, 128),     // 6: Cyan
            new Color(192, 192, 192),   // 7: White (light gray)
            new Color(128, 128, 128),   // 8: Bright Black (dark gray)
            new Color(255, 0, 0),       // 9: Bright Red
            new Color(0, 255, 0),       //10: Bright Green
            new Color(255, 255, 0),     //11: Bright Yellow
            new Color(0, 0, 255),       //12: Bright Blue
            new Color(255, 0, 255),     //13: Bright Magenta
            new Color(0, 255, 255),     //14: Bright Cyan
            new Color(255, 255, 255)    //15: Bright White
    ));
    public static final List<Color> ANSI_COLORS_256;
    static {
        List<Color> ansiColors = new ArrayList<>(256);
        ansiColors.addAll(ANSI_COLORS_16);
        // 16–231: 6×6×6 RGB cube
        int[] levels = {0, 95, 135, 175, 215, 255};
        int index = 16;
        for (int r = 0; r < 6; r++) {
            for (int g = 0; g < 6; g++) {
                for (int b = 0; b < 6; b++) {
                    ansiColors.add(new Color(levels[r], levels[g], levels[b]));
                }
            }
        }
        // 232–255: Grayscale from 8 to 238 in steps of 10
        for (int i = 0; i < 24; i++) {
            int gray = 8 + i * 10;
            ansiColors.add(new Color(gray, gray, gray));
        }
        ANSI_COLORS_256=Collections.unmodifiableList(ansiColors);
    }

    public static NAnsiTermHelper of() {
        return new NAnsiTermHelper();
    }

    public NAnsiTermHelper() {
    }

    public String plain() {
        return "\u001B[0m";
    }

    public String styled(NTextStyles styles) {
        NColor foreground = null;
        NColor background = null;
        boolean bold = false;
        boolean blink = false;
        boolean underlined = false;
        boolean striked = false;
        boolean italic = false;
        boolean reversed = false;
        int intensity = 0;
        if (styles != null) {
            for (NTextStyle style : styles) {
                switch (style.getType()) {
                    case PRIMARY:
                    case FORE_COLOR: {
                        foreground = NColor.of8(style.getVariant());
                        break;
                    }
                    case SECONDARY:
                    case BACK_COLOR: {
                        background = NColor.of8(style.getVariant());
                        break;
                    }
                    case FORE_TRUE_COLOR: {
                        foreground = (NColor.of24(style.getVariant()));
                        break;
                    }
                    case BACK_TRUE_COLOR: {
                        background = NColor.of24(style.getVariant());
                        break;
                    }
                    case ITALIC: {
                        italic = true;
                        break;
                    }
                    case BOLD: {
                        bold = true;
                        break;
                    }
                    case BLINK: {
                        blink = true;
                        break;
                    }
                    case STRIKED: {
                        striked = true;
                        break;
                    }
                    case REVERSED: {
                        reversed = true;
                        break;
                    }
                    case UNDERLINED: {
                        underlined = true;
                        break;
                    }
                }
            }
        }
        return styled(foreground, background, bold, blink, underlined, striked, italic, reversed, intensity);
    }

    public String foreColor(NColor c) {
        if (c != null) {
            switch (c.getType()) {
                case 4: {
                    int intColor = c.getIntColor();
                    if (intColor <= 0) {
                        intColor = 0;
                    }
                    if (intColor >= 15) {
                        intColor = 15;
                    }
                    return ("" + FG8[intColor]);
                }
                case 8: {
                    int intColor = c.getIntColor();
                    if (intColor <= 0) {
                        intColor = 0;
                    }
                    if (intColor >= 255) {
                        intColor = 255;
                    }
                    return ("38;5;" + intColor);
                }
                case 24: {
                    int intColor = c.getIntColor();
                    java.awt.Color color = new java.awt.Color(intColor);
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    return ("38;2;" + red + ";" + green + ";" + blue);
                }
                default: {
                    int intColor = (int) c.getLongColor();
                    java.awt.Color color = new java.awt.Color(intColor);
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    return ("38;2;" + red + ";" + green + ";" + blue);
                }
            }
        }
        return null;
    }

    public String backColor(NColor c) {
        if (c != null) {
            switch (c.getType()) {
                case 4: {
                    int intColor = c.getIntColor();
                    if (intColor <= 0) {
                        intColor = 0;
                    }
                    if (intColor >= 15) {
                        intColor = 15;
                    }
                    return "" + BG8[intColor];
                }
                case 8: {
                    int intColor = c.getIntColor();
                    if (intColor <= 0) {
                        intColor = 0;
                    }
                    if (intColor >= 255) {
                        intColor = 255;
                    }
                    return ("48;5;" + intColor);
                }
                case 24: {
                    int intColor = c.getIntColor();
                    java.awt.Color color = new java.awt.Color(intColor);
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    return ("48;2;" + red + ";" + green + ";" + blue);
                }
                default: {
                    int intColor = (int) c.getLongColor();
                    java.awt.Color color = new java.awt.Color(intColor);
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    return ("48;2;" + red + ";" + green + ";" + blue);
                }
            }
        }
        return null;
    }

    public String styled(NColor foreground, NColor background,
                         boolean bold,
                         boolean blink,
                         boolean underlined,
                         boolean striked,
                         boolean italic,
                         boolean reversed,
                         int intensity) {
        boolean plain = !bold && !blink && !underlined && !italic && !striked && !reversed
                && NBlankable.isBlank(foreground)
                && NBlankable.isBlank(background);
        if (plain) {
            return plain();
        }
        StringBuilder sb = new StringBuilder("\u001B[");
        boolean first = true;
        if (foreground != null) {
            first = false;
            sb.append(foreColor(foreground));
        }
        if (background != null) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append(backColor(background));
        }
        if (bold) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("1");
        }
        if (blink) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("5");
        }
        if (underlined) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("4");
        }
        if (striked) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("9");
        }
        if (italic) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("3");
        }
        if (reversed) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("7");
        }
        sb.append("m");
        return sb.toString();
    }

    public String command(NTerminalCmd command) {
        switch (command.getName()) {
            case NTerminalCmd.Ids.MOVE_LINE_START: {
                return ("\r");
            }
            case NTerminalCmd.Ids.MOVE_TO: {
                List<String> a = command.getArgs();
                if (a.size() >= 2) {
                    Integer count1 = NLiteral.of(a.get(0)).asInt().orNull();
                    Integer count2 = NLiteral.of(a.get(1)).asInt().orNull();
                    if (count1 != null && count2 != null) {
                        return ("\u001b[" + count1 + ";" + count2 + "H");
                    }
                }
                return null;
            }

            case NTerminalCmd.Ids.MOVE_UP: {
                List<String> a = command.getArgs();
                if (a.size() >= 1) {
                    Integer count1 = NLiteral.of(a.get(0)).asInt().orNull();
                    if (count1 != null) {
                        return ("\u001b[" + count1 + "A");
                    }
                }
                return null;
            }
            case NTerminalCmd.Ids.MOVE_DOWN: {
                List<String> a = command.getArgs();
                if (a.size() >= 1) {
                    Integer count1 = NLiteral.of(a.get(0)).asInt().orNull();
                    if (count1 != null) {
                        return ("\u001b[" + count1 + "B");
                    }
                }
                return null;
            }
            case NTerminalCmd.Ids.MOVE_RIGHT: {
                List<String> a = command.getArgs();
                if (a.size() >= 1) {
                    Integer count1 = NLiteral.of(a.get(0)).asInt().orNull();
                    if (count1 != null) {
                        return ("\u001b[" + count1 + "C");
                    }
                }
                return null;
            }
            case NTerminalCmd.Ids.MOVE_LEFT: {
                List<String> a = command.getArgs();
                if (a.size() >= 1) {
                    Integer count1 = NLiteral.of(a.get(0)).asInt().orNull();
                    if (count1 != null) {
                        return ("\u001b[" + count1 + "D");
                    }
                }
                return null;
            }
            case NTerminalCmd.Ids.CLEAR_SCREEN: {
                return ("\u001b[" + 2 + "J");
            }
            case NTerminalCmd.Ids.CLEAR_SCREEN_FROM_CURSOR: {
                return ("\u001b[" + 0 + "J");
            }
            case NTerminalCmd.Ids.CLEAR_SCREEN_TO_CURSOR: {
                return ("\u001b[" + 1 + "J");
            }
            case NTerminalCmd.Ids.CLEAR_LINE: {
                return ("\u001b[" + 2 + "K");
            }
            case NTerminalCmd.Ids.CLEAR_LINE_FROM_CURSOR: {
                return ("\u001b[" + 0 + "K");
            }
            case NTerminalCmd.Ids.CLEAR_LINE_TO_CURSOR: {
                return ("\u001b[" + 1 + "K");
            }
        }
        return null;
    }
}
