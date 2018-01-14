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
package net.vpc.app.nuts;

import java.awt.*;

/**
 * Created by vpc on 6/22/17.
 */
public final class NutsPrintColors {
    public static final Color BLACK = Color.BLACK;
    public static final Color RED = Color.RED;
    public static final Color BLUE = Color.BLUE;
    public static final Color GREEN = Color.GREEN;
    public static final Color CYAN = Color.CYAN;
    public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;
    public static final Color LIGHT_BLUE = new Color(173, 216, 230);
    public static final Color LIGHT_GREEN = new Color(144, 238, 144);
    public static final Color LIGHT_CYAN = new Color(224, 255, 255);
    public static final Color LIGHT_RED = new Color(250, 128, 114);
    public static final Color LIGHT_PURPLE = new Color(216, 191, 216);
    public static final Color DARK_GRAY = Color.DARK_GRAY;
    public static final Color PURPLE = new Color(128, 0, 128);
    public static final Color BROWN = new Color(165, 42, 42);
    public static final Color YELLOW = Color.YELLOW;
    public static final Color WHITE = Color.WHITE;

    private NutsPrintColors() {
    }
}
