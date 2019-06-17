/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util.fprint;

import java.awt.*;

/**
 *
 * @author vpc
 */
public class TextFormats {

    public static final TextFormat MOVE_UP = new TextCursor("MOVE_UP");
    public static final TextFormat MOVE_LINE_START = new TextCursor("MOVE_LINE_START");
    public static final TextFormat UNDERLINED = new TextCursor("UNDERLINED");
    public static final TextFormat ITALIC = new TextCursor("ITALIC");
    public static final TextFormat STRIKED = new TextCursor("STRIKED");
    public static final TextFormat REVERSED = new TextCursor("REVERSED");
    public static final TextFormat BOLD = new TextCursor("BOLD");

    public static final TextFormat FG_BLUE = new TextForeground("FG_BLUE", Color.BLUE);
    public static final TextFormat FG_GREEN = new TextForeground("FG_GREEN", Color.GREEN);
    public static final TextFormat FG_CYAN = new TextForeground("FG_CYAN", Color.CYAN);
    public static final TextFormat FG_MAGENTA = new TextForeground("FG_PURPLE", new Color(128, 0, 128));
    public static final TextFormat FG_YELLOW = new TextForeground("FG_YELLOW", Color.YELLOW);
    public static final TextFormat FG_RED = new TextForeground("FG_RED", Color.RED);
    public static final TextFormat FG_WHITE = new TextForeground("FG_WHITE", Color.WHITE);
    public static final TextFormat FG_BLACK = new TextForeground("FG_BLACK", Color.BLACK);
    public static final TextFormat FG_GREY = new TextForeground("FG_GREY", Color.GRAY);
    public static final TextFormat BG_GREY = new TextBackground("BG_GREY", Color.GRAY);

    public static final TextFormat BG_CYAN = new TextBackground("BG_CYAN", Color.CYAN);
    public static final TextFormat BG_MAGENTA = new TextBackground("BG_PURPLE", new Color(128, 0, 128));
    public static final TextFormat BG_BLACK = new TextBackground("BG_BLACK", Color.BLACK);
    public static final TextFormat BG_YELLOW = new TextBackground("BG_YELLOW", Color.YELLOW);
    public static final TextFormat BG_GREEN = new TextBackground("BG_GREEN", Color.GREEN);
    public static final TextFormat BG_RED = new TextBackground("BG_RED", Color.RED);
    public static final TextFormat BG_BLUE = new TextBackground("BG_BLUE", Color.BLUE);
    public static final TextFormat BG_WHITE = new TextBackground("BG_WHITE", Color.WHITE);

    public static final TextFormat list(TextFormat... all) {
        if (all == null) {
            return null;
        }
        if (all.length == 1) {
            return all[0];
        }
        return new TextFormatList(all);
    }
}
