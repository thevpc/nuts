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
package net.vpc.app.nuts.extensions.terminals;

import java.awt.*;

/**
 *
 * @author vpc
 */
public class NutsTextFormats {

    public static final NutsTextFormat MOVE_UP = new NutsTextCursor("MOVE_UP");
    public static final NutsTextFormat MOVE_LINE_START = new NutsTextCursor("MOVE_LINE_START");
    public static final NutsTextFormat UNDERLINED = new NutsTextCursor("UNDERLINED");
    public static final NutsTextFormat ITALIC = new NutsTextCursor("ITALIC");
    public static final NutsTextFormat STRIKED = new NutsTextCursor("STRIKED");
    public static final NutsTextFormat REVERSED = new NutsTextCursor("REVERSED");

    public static final NutsTextFormat FG_BLUE = new NutsTextForeground("FG_BLUE", Color.BLUE);
    public static final NutsTextFormat FG_GREEN = new NutsTextForeground("FG_GREEN", Color.GREEN);
    public static final NutsTextFormat FG_CYAN = new NutsTextForeground("FG_CYAN", Color.CYAN);
    public static final NutsTextFormat FG_MAGENTA = new NutsTextForeground("FG_PURPLE", new Color(128, 0, 128));
    public static final NutsTextFormat FG_YELLOW = new NutsTextForeground("FG_YELLOW", Color.YELLOW);
    public static final NutsTextFormat FG_RED = new NutsTextForeground("FG_RED", Color.RED);
    public static final NutsTextFormat FG_WHITE = new NutsTextForeground("FG_WHITE", Color.WHITE);
    public static final NutsTextFormat FG_BLACK = new NutsTextForeground("FG_BLACK", Color.BLACK);

//    public static final NutsTextFormat FG_LIGHT_RED = new NutsTextForeground("FG_LIGHT_RED", new Color(250, 128, 114));
//    public static final NutsTextFormat FG_BROWN = new NutsTextForeground("FG_BROWN", new Color(165, 42, 42));
//    public static final NutsTextFormat FG_DARK_GRAY = new NutsTextForeground("FG_DARK_GRAY", Color.DARK_GRAY);
//    public static final NutsTextFormat FG_LIGHT_BLUE = new NutsTextForeground("FG_LIGHT_BLUE", new Color(173, 216, 230));
//    public static final NutsTextFormat FG_LIGHT_GRAY = new NutsTextForeground("FG_LIGHT_GRAY", Color.LIGHT_GRAY);
//    public static final NutsTextFormat FG_LIGHT_PURPLE = new NutsTextForeground("FG_LIGHT_PURPLE", new Color(216, 191, 216));
//    public static final NutsTextFormat FG_LIGHT_CYAN = new NutsTextForeground("FG_LIGHT_CYAN", new Color(224, 255, 255));
//    public static final NutsTextFormat FG_LIGHT_GREEN = new NutsTextForeground("FG_LIGHT_GREEN", new Color(144, 238, 144));

    public static final NutsTextFormat BG_CYAN = new NutsTextBackground("BG_CYAN", Color.CYAN);
    public static final NutsTextFormat BG_MAGENTA = new NutsTextBackground("BG_PURPLE", new Color(128, 0, 128));
    public static final NutsTextFormat BG_BLACK = new NutsTextBackground("BG_BLACK", Color.BLACK);
    public static final NutsTextFormat BG_YELLOW = new NutsTextBackground("BG_YELLOW", Color.YELLOW);
    public static final NutsTextFormat BG_GREEN = new NutsTextBackground("BG_GREEN", Color.GREEN);
    public static final NutsTextFormat BG_RED = new NutsTextBackground("BG_RED", Color.RED);
    public static final NutsTextFormat BG_BLUE = new NutsTextBackground("BG_BLUE", Color.BLUE);
    public static final NutsTextFormat BG_WHITE = new NutsTextBackground("BG_WHITE", Color.WHITE);

//    public static final NutsTextFormat BG_BROWN = new NutsTextBackground("BG_BROWN", new Color(165, 42, 42));
//    public static final NutsTextFormat BG_DARK_GRAY = new NutsTextBackground("BG_DARK_GRAY", Color.DARK_GRAY);
//    public static final NutsTextFormat BG_LIGHT_RED = new NutsTextBackground("BG_LIGHT_RED", new Color(250, 128, 114));
//    public static final NutsTextFormat BG_LIGHT_CYAN = new NutsTextBackground("BG_LIGHT_CYAN", new Color(224, 255, 255));
//    public static final NutsTextFormat BG_LIGHT_GREEN = new NutsTextBackground("BG_LIGHT_GREEN", new Color(144, 238, 144));
//    public static final NutsTextFormat BG_LIGHT_BLUE = new NutsTextBackground("BG_LIGHT_BLUE", new Color(173, 216, 230));
//    public static final NutsTextFormat BG_LIGHT_GRAY = new NutsTextBackground("BG_LIGHT_GRAY", Color.LIGHT_GRAY);
//    public static final NutsTextFormat BG_LIGHT_PURPLE = new NutsTextBackground("BG_LIGHT_PURPLE", new Color(216, 191, 216));

    public static final NutsTextFormat list(NutsTextFormat... all) {
        if(all==null){
            return null;
        }
        if(all.length==1){
            return all[0];
        }
        return new NutsTextFormatList(all);
    }
}
