/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.util;

/**
 * Color Model
 */
public class NColor {
    public static final byte TYPE4 = 4;
    public static final byte TYPE8 = 8;
    public static final byte TYPE24 = 24;
    public static final byte TYPE32 = 32;
    public static final byte TYPE64 = 64;
    private final byte type;
    private final long color;

    public NColor(byte type, long color) {
        this.type = type;
        this.color = color;
    }

    public static NColor of4(int color) {
        return new NColor(TYPE4, color);
    }

    public static NColor of8(int color) {
        return new NColor(TYPE8, color);
    }

    public static NColor of24(int color) {
        return new NColor(TYPE24, color);
    }

    public static NColor of32(int color) {
        return new NColor(TYPE32, color);
    }

    public static NColor of64(long color) {
        return new NColor(TYPE64, color);
    }

    public int getType() {
        return type;
    }

    public int getIntColor() {
        return (int) color;
    }

    public long getLongColor() {
        return color;
    }
}
