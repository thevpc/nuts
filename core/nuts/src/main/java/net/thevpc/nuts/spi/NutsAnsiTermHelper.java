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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NutsApiUtils;

public class NutsAnsiTermHelper {
    private static final int[] FG8 = {30, 31, 32, 33, 34, 35, 36, 37, 90, 91, 92, 93, 94, 95, 96, 97};
    private static final int[] BG8 = {40, 41, 42, 43, 44, 45, 46, 47, 100, 101, 102, 103, 104, 105, 106, 107};


    public static NutsAnsiTermHelper of(NutsSession session) {
        return new NutsAnsiTermHelper();
    }

    public String plain() {
        return "\u001B[0m";
    }

    public String styled(NutsTextStyles styles, NutsSession session) {
        NutsColor foreground = null;
        NutsColor background = null;
        boolean bold = false;
        boolean blink = false;
        boolean underlined = false;
        boolean striked = false;
        boolean italic = false;
        boolean reversed = false;
        int intensity = 0;
        if(styles!=null) {
            for (NutsTextStyle style : styles) {
                switch (style.getType()) {
                    case PRIMARY:
                    case FORE_COLOR: {
                        foreground = NutsColor.of8(style.getVariant());
                        break;
                    }
                    case SECONDARY:
                    case BACK_COLOR: {
                        background = NutsColor.of8(style.getVariant());
                        break;
                    }
                    case FORE_TRUE_COLOR: {
                        foreground = (NutsColor.of24(style.getVariant()));
                        break;
                    }
                    case BACK_TRUE_COLOR: {
                        background = NutsColor.of24(style.getVariant());
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
        return styled(foreground, background, bold, blink, underlined, striked, italic, reversed, intensity, session);
    }

    public String foreColor(NutsColor c, NutsSession session) {
        if (c != null) {
            int intColor = c.getColor();
            switch (c.getType()) {
                case 4: {
                    if (intColor <= 0) {
                        intColor = 0;
                    }
                    if (intColor >= 15) {
                        intColor = 15;
                    }
                    return ("" + FG8[intColor]);
                }
                case 8: {
                    if (intColor <= 0) {
                        intColor = 0;
                    }
                    if (intColor >= 255) {
                        intColor = 255;
                    }
                    return ("38;5;" + intColor);
                }
                case 24: {
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

    public String backColor(NutsColor c, NutsSession session) {
        if (c != null) {
            int intColor = c.getColor();
            switch (c.getType()) {
                case 4: {
                    if (intColor <= 0) {
                        intColor = 0;
                    }
                    if (intColor >= 15) {
                        intColor = 15;
                    }
                    return "" + BG8[intColor];
                }
                case 8: {
                    if (intColor <= 0) {
                        intColor = 0;
                    }
                    if (intColor >= 255) {
                        intColor = 255;
                    }
                    return ("48;5;" + intColor);
                }
                case 24: {
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

    public String styled(NutsColor foreground, NutsColor background,
                         boolean bold,
                         boolean blink,
                         boolean underlined,
                         boolean striked,
                         boolean italic,
                         boolean reversed,
                         int intensity,
                         NutsSession session) {
        boolean plain = !bold && !blink && !underlined && !italic && !striked && !reversed
                && NutsBlankable.isBlank(foreground)
                && NutsBlankable.isBlank(background);
        if (plain) {
            return plain();
        }
        StringBuilder sb = new StringBuilder("\u001B[");
        boolean first = true;
        if (foreground != null) {
            first = false;
            sb.append(foreColor(foreground, session));
        }
        if (background != null) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append(backColor(background, session));
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

    public String command(NutsTerminalCommand command, NutsSession session) {
        switch (command.getName()) {
            case NutsTerminalCommand.Ids.MOVE_LINE_START: {
                return ("\r");
            }
            case NutsTerminalCommand.Ids.MOVE_TO: {
                String a = command.getArgs();
                if (a != null) {
                    String[] split = a.split("[;v, x]");
                    if (split.length >= 2) {
                        Integer count1 = NutsApiUtils.parseInt(split[0], null, null);
                        Integer count2 = NutsApiUtils.parseInt(split[1], null, null);
                        if (count1 != null && count2 != null) {
                            return ("\u001b[" + count1 + ";" + count2 + "H");
                        }
                    }
                }
                return null;
            }

            case NutsTerminalCommand.Ids.MOVE_UP: {
                Integer count1 = NutsApiUtils.parseInt(command.getArgs(), null, null);
                if (count1 != null) {
                    return ("\u001b[" + count1 + "A");
                }
                return null;
            }
            case NutsTerminalCommand.Ids.MOVE_DOWN: {
                Integer count1 = NutsApiUtils.parseInt(command.getArgs(), null, null);
                if (count1 != null) {
                    return ("\u001b[" + count1 + "B");
                }
                return null;
            }
            case NutsTerminalCommand.Ids.MOVE_RIGHT: {
                Integer count1 = NutsApiUtils.parseInt(command.getArgs(), null, null);
                if (count1 != null) {
                    return ("\u001b[" + count1 + "C");
                }
                return null;
            }
            case NutsTerminalCommand.Ids.MOVE_LEFT: {
                Integer count1 = NutsApiUtils.parseInt(command.getArgs(), null, null);
                if (count1 != null) {
                    return ("\u001b[" + count1 + "D");
                }
                return null;
            }
            case NutsTerminalCommand.Ids.CLEAR_SCREEN: {
                return ("\u001b[" + 2 + "J");
            }
            case NutsTerminalCommand.Ids.CLEAR_SCREEN_FROM_CURSOR: {
                return ("\u001b[" + 0 + "J");
            }
            case NutsTerminalCommand.Ids.CLEAR_SCREEN_TO_CURSOR: {
                return ("\u001b[" + 1 + "J");
            }
            case NutsTerminalCommand.Ids.CLEAR_LINE: {
                return ("\u001b[" + 2 + "K");
            }
            case NutsTerminalCommand.Ids.CLEAR_LINE_FROM_CURSOR: {
                return ("\u001b[" + 0 + "K");
            }
            case NutsTerminalCommand.Ids.CLEAR_LINE_TO_CURSOR: {
                return ("\u001b[" + 1 + "K");
            }
        }
        return null;
    }
}
