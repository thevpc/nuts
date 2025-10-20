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

import java.util.*;
import java.util.List;

/**
 * Color Model
 */
public class NColor {
    // start static
    private static final java.util.List<NColor> _ALL_REGISTERED = new ArrayList<>();
    private static final java.util.List<NColor> _ALL_CANONICAL = new ArrayList<>();
    private static final Map<String, NColor> _ALL_BY_NAME = new LinkedHashMap<>();
    private static final Map<String, List<NColor>> _ALL_BY_CANONICAL_NAME = new LinkedHashMap<>();

    public static final NColor BLACK = _reg("Black", "Black", 0, 0, 0);
    public static final NColor MAROON = _reg("Maroon", "Maroon", 128, 0, 0);
    public static final NColor GREEN = _reg("Green", "Green", 0, 128, 0);
    public static final NColor OLIVE = _reg("Olive", "Olive", 128, 128, 0);
    public static final NColor NAVY = _reg("Navy", "Navy", 0, 0, 128);
    public static final NColor PURPLE = _reg("Purple", "Purple", 128, 0, 128);
    public static final NColor TEAL = _reg("Teal", "Teal", 0, 128, 128);
    public static final NColor SILVER = _reg("Silver", "Silver", 192, 192, 192);
    public static final NColor RED = _reg("Red", "Red", 255, 0, 0);
    public static final NColor LIME = _reg("Lime", "Lime", 0, 255, 0);
    public static final NColor YELLOW = _reg("Yellow", "Yellow", 255, 255, 0);
    public static final NColor DARK_YELLOW = _reg("DarkYellow", "Yellow", 186, 142, 35);
    public static final NColor BLUE = _reg("Blue", "Blue", 0, 0, 255);
    public static final NColor FUCHSIA = _reg("Fuchsia", "Fuchsia", 255, 0, 255);
    public static final NColor AQUA = _reg("Aqua", "Aqua", 0, 255, 255);
    public static final NColor WHITE = _reg("White", "White", 255, 255, 255);
    public static final NColor NAVY_BLUE = _reg("NavyBlue", "NavyBlue", 0, 0, 95);
    public static final NColor DARK_BLUE = _reg("DarkBlue", "Blue", 0, 0, 135);
    public static final NColor LIGHT_BLUE = _reg("LightBlue", "Blue", 173, 216, 230);
    public static final NColor BLUE_2 = _reg("Blue2", "Blue", 0, 0, 175);
    public static final NColor BLUE_3 = _reg("Blue3", "Blue", 0, 0, 215);
    public static final NColor BLUE_4 = _reg("Blue4", "Blue", 0, 0, 255);
    public static final NColor DARK_GREEN = _reg("DarkGreen", "Green", 0, 95, 0);
    public static final NColor DEEP_SKY_BLUE = _reg("DeepSkyBlue", "DeepSkyBlue", 0, 95, 95);
    public static final NColor DEEP_SKY_BLUE_2 = _reg("DeepSkyBlue2", "DeepSkyBlue", 0, 95, 135);
    public static final NColor DEEP_SKY_BLUE_3 = _reg("DeepSkyBlue3", "DeepSkyBlue", 0, 95, 175);
    public static final NColor DODGER_BLUE = _reg("DodgerBlue", "DodgerBlue", 0, 95, 215);
    public static final NColor DODGER_BLUE_2 = _reg("DodgerBlue2", "DodgerBlue", 0, 95, 255);
    public static final NColor GREEN_2 = _reg("Green2", "Green", 0, 135, 0);
    public static final NColor SPRING_GREEN = _reg("SpringGreen", "SpringGreen", 0, 135, 95);
    public static final NColor LIGHT_SPRING_GREEN = _reg("LightSpringGreen", "SpringGreen", 139, 231, 185);
    public static final NColor DARK_SPRING_GREEN = _reg("DarkSpringGreen", "SpringGreen", 23, 114, 69);
    public static final NColor TURQUOISE = _reg("Turquoise", "Turquoise", 0, 135, 135);
    public static final NColor DEEP_SKY_BLUE_4 = _reg("DeepSkyBlue4", "DeepSkyBlue", 0, 135, 175);
    public static final NColor DEEP_SKY_BLUE_5 = _reg("DeepSkyBlue5", "DeepSkyBlue", 0, 135, 215);
    public static final NColor DODGER_BLUE_3 = _reg("DodgerBlue3", "DodgerBlue", 0, 135, 255);
    public static final NColor GREEN_3 = _reg("Green3", "Green", 0, 175, 0);
    public static final NColor SPRING_GREEN_2 = _reg("SpringGreen2", "SpringGreen", 0, 175, 95);
    public static final NColor DARK_CYAN = _reg("DarkCyan", "Cyan", 0, 175, 135);
    public static final NColor LIGHT_SEA_GREEN = _reg("LightSeaGreen", "SeaGreen", 0, 175, 175);
    public static final NColor DEEP_SKY_BLUE_6 = _reg("DeepSkyBlue6", "DeepSkyBlue", 0, 175, 215);
    public static final NColor DEEP_SKY_BLUE_7 = _reg("DeepSkyBlue7", "DeepSkyBlue", 0, 175, 255);
    public static final NColor GREEN_4 = _reg("Green4", "Green", 0, 215, 0);
    public static final NColor SPRING_GREEN_3 = _reg("SpringGreen3", "SpringGreen", 0, 215, 95);
    public static final NColor SPRING_GREEN_4 = _reg("SpringGreen4", "SpringGreen", 0, 215, 135);
    public static final NColor CYAN = _reg("Cyan", "Cyan", 0, 215, 175);
    public static final NColor DARK_TURQUOISE = _reg("DarkTurquoise", "Turquoise", 0, 215, 215);
    public static final NColor LIGHT_TURQUOISE = _reg("LightTurquoise", "Turquoise", 175, 228, 222);
    public static final NColor TURQUOISE_2 = _reg("Turquoise2", "Turquoise", 0, 215, 255);
    public static final NColor GREEN_5 = _reg("Green5", "Green", 0, 255, 0);
    public static final NColor SPRING_GREEN_5 = _reg("SpringGreen5", "SpringGreen", 0, 255, 95);
    public static final NColor SPRING_GREEN_6 = _reg("SpringGreen6", "SpringGreen", 0, 255, 135);
    public static final NColor MEDIUM_SPRING_GREEN = _reg("MediumSpringGreen", "SpringGreen", 0, 255, 175);
    public static final NColor CYAN_2 = _reg("Cyan2", "Cyan", 0, 255, 215);
    public static final NColor CYAN_3 = _reg("Cyan3", "Cyan", 0, 255, 255);
    public static final NColor DARK_RED = _reg("DarkRed", "Red", 95, 0, 0);
    public static final NColor LIGHT_RED = _reg("LightRed", "Red", 255, 114, 118);
    public static final NColor CERISE = _reg("Cerise", "Cerise", 223, 70, 97);
    public static final NColor STRAWBERRY = _reg("Strawberry", "Strawberry", 197, 70, 68);
    public static final NColor MANGO = _reg("Mango", "Mango", 183, 94, 74);
    public static final NColor SCARLET = _reg("Scarlet", "Scarlet", 255, 36, 0);
    public static final NColor BEIGE = _reg("Beige", "Beige", 245, 245, 220);
    public static final NColor BRICK = _reg("Brick", "Brick", 188, 74, 60);
    public static final NColor DEEP_PINK = _reg("DeepPink", "DeepPink", 95, 0, 95);
    public static final NColor PURPLE_2 = _reg("Purple2", "Purple", 95, 0, 135);
    public static final NColor PURPLE_3 = _reg("Purple3", "Purple", 95, 0, 175);
    public static final NColor PURPLE_4 = _reg("Purple4", "Purple", 95, 0, 215);
    public static final NColor BLUE_VIOLET = _reg("BlueViolet", "BlueViolet", 95, 0, 255);
    public static final NColor ORANGE = _reg("Orange", "Orange", 95, 95, 0);
    public static final NColor LIGHT_ORANGE = _reg("LightOrange", "Orange", 252, 210, 153);
    public static final NColor MEDIUM_PURPLE = _reg("MediumPurple", "Purple", 95, 95, 135);
    public static final NColor DARK_SLATE_BLUE = _reg("DarkSlateBlue", "SlateBlue", 72, 61, 139);
    public static final NColor SLATE_BLUE = _reg("SlateBlue", "SlateBlue", 95, 95, 175);
    public static final NColor SLATE_BLUE_2 = _reg("SlateBlue2", "SlateBlue", 95, 95, 215);
    public static final NColor ROYAL_BLUE = _reg("RoyalBlue", "RoyalBlue", 95, 95, 255);
    public static final NColor CHARTREUSE = _reg("Chartreuse", "Chartreuse", 95, 135, 0);
    public static final NColor DARK_SEA_GREEN = _reg("DarkSeaGreen", "SeaGreen", 95, 135, 95);
    public static final NColor PALE_TURQUOISE = _reg("PaleTurquoise", "PaleTurquoise", 95, 135, 135);
    public static final NColor DARK_STEEL_BLUE = _reg("DarkSteelBlue", "SteelBlue", 41, 93, 138);
    public static final NColor STEEL_BLUE = _reg("SteelBlue", "SteelBlue", 95, 135, 175);
    public static final NColor STEEL_BLUE_2 = _reg("SteelBlue2", "SteelBlue", 95, 135, 215);
    public static final NColor CORNFLOWER_BLUE = _reg("CornflowerBlue", "CornflowerBlue", 95, 135, 255);
    public static final NColor CHARTREUSE_2 = _reg("Chartreuse2", "Chartreuse", 95, 175, 0);
    public static final NColor DARK_SEA_GREEN_2 = _reg("DarkSeaGreen2", "SeaGreen", 95, 175, 95);
    public static final NColor CADET_BLUE = _reg("CadetBlue", "CadetBlue", 95, 175, 135);
    public static final NColor CADET_BLUE_2 = _reg("CadetBlue2", "CadetBlue", 95, 175, 175);
    public static final NColor SKY_BLUE = _reg("SkyBlue", "Blue", 95, 175, 215);
    public static final NColor STEEL_BLUE_3 = _reg("SteelBlue3", "SteelBlue", 95, 175, 255);
    public static final NColor CHARTREUSE_3 = _reg("Chartreuse3", "Chartreuse", 95, 215, 0);
    public static final NColor PALE_GREEN = _reg("PaleGreen", "PaleGreen", 95, 215, 95);
    public static final NColor SEA_GREEN = _reg("SeaGreen", "SeaGreen", 95, 215, 135);
    public static final NColor AQUAMARINE = _reg("Aquamarine", "Aquamarine", 95, 215, 175);
    public static final NColor MEDIUM_TURQUOISE = _reg("MediumTurquoise", "Turquoise", 95, 215, 215);
    public static final NColor STEEL_BLUE_4 = _reg("SteelBlue4", "SteelBlue", 95, 215, 255);
    public static final NColor CHARTREUSE_4 = _reg("Chartreuse4", "Chartreuse", 95, 255, 0);
    public static final NColor SEA_GREEN_2 = _reg("SeaGreen2", "SeaGreen", 95, 255, 95);
    public static final NColor SEA_GREEN_3 = _reg("SeaGreen3", "SeaGreen", 95, 255, 135);
    public static final NColor SEA_GREEN_4 = _reg("SeaGreen4", "SeaGreen", 95, 255, 175);
    public static final NColor AQUAMARINE_2 = _reg("Aquamarine2", "Aquamarine", 95, 255, 215);
    public static final NColor DARK_SLATE_GRAY = _reg("DarkSlateGray", "SlateGray", 95, 255, 255);
    public static final NColor DARK_RED_2 = _reg("DarkRed2", "Red", 135, 0, 0);
    public static final NColor DEEP_PINK_2 = _reg("DeepPink2", "DeepPink", 135, 0, 95);
    public static final NColor LIGHT_MAGENTA = _reg("LightMagenta", "Magenta", 255, 119, 255);
    public static final NColor DARK_MAGENTA = _reg("DarkMagenta", "Magenta", 135, 0, 135);
    public static final NColor DARK_MAGENTA_2 = _reg("DarkMagenta2", "Magenta", 135, 0, 175);
    public static final NColor DARK_VIOLET = _reg("DarkViolet", "Violet", 135, 0, 215);
    public static final NColor PURPLE_5 = _reg("Purple5", "Purple", 135, 0, 255);
    public static final NColor ORANGE_2 = _reg("Orange2", "Orange", 135, 95, 0);
    public static final NColor LIGHT_PINK = _reg("LightPink", "Pink", 135, 95, 95);
    public static final NColor PLUM = _reg("Plum", "Plum", 135, 95, 135);
    public static final NColor MEDIUM_PURPLE_2 = _reg("MediumPurple2", "Purple", 135, 95, 175);
    public static final NColor MEDIUM_PURPLE_3 = _reg("MediumPurple3", "Purple", 135, 95, 215);
    public static final NColor SLATE_BLUE_3 = _reg("SlateBlue3", "SlateBlue", 135, 95, 255);
    public static final NColor YELLOW_2 = _reg("Yellow2", "Yellow", 135, 135, 0);
    public static final NColor WHEAT = _reg("Wheat", "Wheat", 135, 135, 95);
    public static final NColor LIGHT_SLATE_GRAY = _reg("LightSlateGray", "SlateGray", 135, 135, 175);
    public static final NColor MEDIUM_PURPLE_4 = _reg("MediumPurple4", "Purple", 135, 135, 215);
    public static final NColor LIGHT_SLATE_BLUE = _reg("LightSlateBlue", "SlateBlue", 135, 135, 255);
    public static final NColor YELLOW_3 = _reg("Yellow3", "Yellow", 135, 175, 0);
    public static final NColor DARK_OLIVE_GREEN = _reg("DarkOliveGreen", "OliveGreen", 135, 175, 95);
    public static final NColor DARK_SEA_GREEN_3 = _reg("DarkSeaGreen3", "SeaGreen", 135, 175, 135);
    public static final NColor LIGHT_SKY_BLUE = _reg("LightSkyBlue", "SkyBlue", 135, 175, 175);
    public static final NColor LIGHT_SKY_BLUE_2 = _reg("LightSkyBlue2", "SkyBlue", 135, 175, 215);
    public static final NColor SKY_BLUE_2 = _reg("SkyBlue2", "SkyBlue", 135, 175, 255);
    public static final NColor CHARTREUSE_5 = _reg("Chartreuse5", "Chartreuse", 135, 215, 0);
    public static final NColor DARK_OLIVE_GREEN_2 = _reg("DarkOliveGreen2", "OliveGreen", 135, 215, 95);
    public static final NColor PALE_GREEN_2 = _reg("PaleGreen2", "PaleGreen", 135, 215, 135);
    public static final NColor DARK_SEA_GREEN_4 = _reg("DarkSeaGreen4", "SeaGreen", 135, 215, 175);
    public static final NColor DARK_SLATE_GRAY_2 = _reg("DarkSlateGray2", "SlateGray", 135, 215, 215);
    public static final NColor SKY_BLUE_3 = _reg("SkyBlue3", "SkyBlue", 135, 215, 255);
    public static final NColor CHARTREUSE_6 = _reg("Chartreuse6", "Chartreuse", 135, 255, 0);
    public static final NColor LIGHT_GREEN = _reg("LightGreen", "Green", 135, 255, 95);
    public static final NColor LIGHT_GREEN_2 = _reg("LightGreen2", "Green", 135, 255, 135);
    public static final NColor PALE_GREEN_3 = _reg("PaleGreen3", "PaleGreen", 135, 255, 175);
    public static final NColor AQUAMARINE_3 = _reg("Aquamarine3", "Aquamarine", 135, 255, 215);
    public static final NColor DARK_SLATE_GRAY_3 = _reg("DarkSlateGray3", "SlateGray", 135, 255, 255);
    public static final NColor RED_2 = _reg("Red2", "Red", 175, 0, 0);
    public static final NColor DEEP_PINK_3 = _reg("DeepPink3", "DeepPink", 175, 0, 95);
    public static final NColor MEDIUM_VIOLET_RED = _reg("MediumVioletRed", "VioletRed", 175, 0, 135);
    public static final NColor LIGHT_VIOLET = _reg("LightViolet", "Violet", 207, 159, 255);
    public static final NColor MAGENTA = _reg("Magenta", "Magenta", 175, 0, 175);
    public static final NColor DARK_VIOLET_2 = _reg("DarkViolet2", "Violet", 175, 0, 215);
    public static final NColor PURPLE_6 = _reg("Purple6", "Purple", 175, 0, 255);
    public static final NColor LIGHT_PURPLE = _reg("LightPurple", "Purple", 203, 195, 227);
    public static final NColor DARK_PURPLE = _reg("DarkPurple", "Purple", 152, 29, 151);
    public static final NColor DARK_ORANGE = _reg("DarkOrange", "Orange", 175, 95, 0);
    public static final NColor INDIAN_RED = _reg("IndianRed", "IndianRed", 175, 95, 95);
    public static final NColor HOT_PINK = _reg("HotPink", "HotPink", 175, 95, 135);
    public static final NColor LIGHT_ORCHID = _reg("LightOrchid", "Orchid", 230, 168, 215);
    public static final NColor DARK_ORCHID = _reg("DarkOrchid", "Orchid", 153, 50, 204);
    public static final NColor MEDIUM_ORCHID = _reg("MediumOrchid", "Orchid", 175, 95, 175);
    public static final NColor MEDIUM_ORCHID_2 = _reg("MediumOrchid2", "Orchid", 175, 95, 215);
    public static final NColor MEDIUM_PURPLE_5 = _reg("MediumPurple5", "Purple", 175, 95, 255);
    public static final NColor GOLDENROD = _reg("Goldenrod", "Goldenrod", 218, 165, 32);
    public static final NColor DARK_GOLDENROD = _reg("DarkGoldenrod", "Goldenrod", 175, 135, 0);
    public static final NColor LIGHT_SALMON = _reg("LightSalmon", "Salmon", 175, 135, 95);
    public static final NColor LIGHT_BROWN = _reg("LightBrown", "Brown", 196, 164, 132);
    public static final NColor BROWN = _reg("Brown", "Brown", 150, 75, 0);
    public static final NColor DARK_BROWN = _reg("DarkBrown", "Brown", 101, 67, 33);
    public static final NColor ROSY_BROWN = _reg("RosyBrown", "RosyBrown", 175, 135, 135);
    public static final NColor MEDIUM_PURPLE_6 = _reg("MediumPurple6", "Purple", 175, 135, 215);
    public static final NColor MEDIUM_PURPLE_7 = _reg("MediumPurple7", "Purple", 175, 135, 255);
    public static final NColor GOLD = _reg("Gold", "Gold", 175, 175, 0);
    public static final NColor LIGHT_KHAKI = _reg("LightKhaki", "Khaki", 240, 230, 140);
    public static final NColor DARK_KHAKI = _reg("DarkKhaki", "Khaki", 175, 175, 95);
    public static final NColor NAVAJO_WHITE = _reg("NavajoWhite", "NavajoWhite", 175, 175, 135);
    public static final NColor LIGHT_STEEL_BLUE = _reg("LightSteelBlue", "SteelBlue", 175, 175, 215);
    public static final NColor LIGHT_STEEL_BLUE_2 = _reg("LightSteelBlue2", "SteelBlue", 175, 175, 255);
    public static final NColor YELLOW_4 = _reg("Yellow4", "Yellow", 175, 215, 0);
    public static final NColor DARK_OLIVE_GREEN_3 = _reg("DarkOliveGreen3", "OliveGreen", 175, 215, 95);
    public static final NColor DARK_SEA_GREEN_5 = _reg("DarkSeaGreen5", "SeaGreen", 175, 215, 135);
    public static final NColor DARK_SEA_GREEN_6 = _reg("DarkSeaGreen6", "SeaGreen", 175, 215, 175);
    public static final NColor LIGHT_CYAN = _reg("LightCyan", "Cyan", 175, 215, 215);
    public static final NColor LIGHT_SKY_BLUE_3 = _reg("LightSkyBlue3", "SkyBlue", 175, 215, 255);
    public static final NColor GREEN_YELLOW = _reg("GreenYellow", "GreenYellow", 175, 255, 0);
    public static final NColor DARK_OLIVE_GREEN_4 = _reg("DarkOliveGreen4", "OliveGreen", 175, 255, 95);
    public static final NColor PALE_GREEN_4 = _reg("PaleGreen4", "PaleGreen", 175, 255, 135);
    public static final NColor DARK_SEA_GREEN_7 = _reg("DarkSeaGreen7", "SeaGreen", 175, 255, 175);
    public static final NColor DARK_SEA_GREEN_8 = _reg("DarkSeaGreen8", "SeaGreen", 175, 255, 215);
    public static final NColor PALE_TURQUOISE_2 = _reg("PaleTurquoise2", "PaleTurquoise", 175, 255, 255);
    public static final NColor RED_3 = _reg("Red3", "Red", 215, 0, 0);
    public static final NColor DEEP_PINK_4 = _reg("DeepPink4", "DeepPink", 215, 0, 95);
    public static final NColor DEEP_PINK_5 = _reg("DeepPink5", "DeepPink", 215, 0, 135);
    public static final NColor MAGENTA_2 = _reg("Magenta2", "Magenta", 215, 0, 175);
    public static final NColor MAGENTA_3 = _reg("Magenta3", "Magenta", 215, 0, 215);
    public static final NColor MAGENTA_4 = _reg("Magenta4", "Magenta", 215, 0, 255);
    public static final NColor DARK_ORANGE_2 = _reg("DarkOrange2", "Orange", 215, 95, 0);
    public static final NColor INDIAN_RED_2 = _reg("IndianRed2", "IndianRed", 215, 95, 95);
    public static final NColor HOT_PINK_2 = _reg("HotPink2", "HotPink", 215, 95, 135);
    public static final NColor HOT_PINK_3 = _reg("HotPink3", "HotPink", 215, 95, 175);
    public static final NColor ORCHID = _reg("Orchid", "Orchid", 215, 95, 215);
    public static final NColor MEDIUM_ORCHID_3 = _reg("MediumOrchid3", "Orchid", 215, 95, 255);
    public static final NColor ORANGE_3 = _reg("Orange3", "Orange", 215, 135, 0);
    public static final NColor DARK_SALMON = _reg("DarkSalmon", "Salmon", 233, 150, 122);
    public static final NColor LIGHT_SALMON_2 = _reg("LightSalmon2", "Salmon", 215, 135, 95);
    public static final NColor LIGHT_PINK_2 = _reg("LightPink2", "Pink", 215, 135, 135);
    public static final NColor DARK_PINK = _reg("DarkPink", "Pink", 231, 84, 128);
    public static final NColor PINK = _reg("Pink", "Pink", 215, 135, 175);
    public static final NColor PLUM_2 = _reg("Plum2", "Plum", 215, 135, 215);
    public static final NColor VIOLET = _reg("Violet", "Violet", 215, 135, 255);
    public static final NColor GOLD_2 = _reg("Gold2", "Gold", 215, 175, 0);
    public static final NColor LIGHT_GOLDENROD = _reg("LightGoldenrod", "Goldenrod", 215, 175, 95);
    public static final NColor TAN = _reg("Tan", "Tan", 215, 175, 135);
    public static final NColor LIGHT_TAN = _reg("LightTan", "Tan", 236, 222, 201);
    public static final NColor DARK_TAN = _reg("DarkTan", "Tan", 145, 129, 81);
    public static final NColor TUSCAN_TAN = _reg("TuscanTan", "TuscanTan", 166, 123, 91);
    public static final NColor ALMOND = _reg("Almond", "Almond", 239, 222, 205);
    public static final NColor BONE = _reg("Bone", "Bone", 227, 218, 201);
    public static final NColor BISCUIT = _reg("Biscuit", "Biscuit", 239, 204, 162);
    public static final NColor BRANDY = _reg("Brandy", "Brandy", 218, 188, 148);
    public static final NColor CALICO = _reg("Calico", "Calico", 224, 141, 91);
    public static final NColor CAMEL = _reg("Camel", "Camel", 193, 154, 107);
    public static final NColor CAMEO = _reg("Cameo", "Cameo", 238, 215, 185);
    public static final NColor CARAMEL = _reg("Caramel", "Caramel", 255, 213, 154);
    public static final NColor CASHMERE = _reg("Cashmere", "Cashmere", 230, 200, 160);
    public static final NColor CREAM = _reg("Cream", "Cream", 255, 253, 208);
    public static final NColor CHALKY = _reg("Chalky", "Chalky", 239, 201, 144);
    public static final NColor DEER = _reg("Deer", "Deer", 186, 135, 89);
    public static final NColor DESERT = _reg("Desert", "Desert", 250, 213, 165);
    public static final NColor DIRT = _reg("Dirt", "Dirt", 155, 118, 83);
    public static final NColor EQUATOR = _reg("Equator", "Equator", 227, 197, 101);
    public static final NColor MISTY_ROSE = _reg("MistyRose", "Rose", 215, 175, 175);
    public static final NColor THISTLE = _reg("Thistle", "Thistle", 215, 175, 215);
    public static final NColor PLUM_3 = _reg("Plum3", "Plum", 215, 175, 255);
    public static final NColor YELLOW_5 = _reg("Yellow5", "Yellow", 215, 215, 0);
    public static final NColor KHAKI = _reg("Khaki", "Khaki", 215, 215, 95);
    public static final NColor LIGHT_GOLDENROD_2 = _reg("LightGoldenrod2", "Goldenrod", 215, 215, 135);
    public static final NColor LIGHT_YELLOW = _reg("LightYellow", "Yellow", 215, 215, 175);
    public static final NColor LIGHT_STEEL_BLUE_3 = _reg("LightSteelBlue3", "SteelBlue", 215, 215, 255);
    public static final NColor YELLOW_6 = _reg("Yellow6", "Yellow", 215, 255, 0);
    public static final NColor DARK_OLIVE_GREEN_5 = _reg("DarkOliveGreen5", "OliveGreen", 215, 255, 95);
    public static final NColor DARK_OLIVE_GREEN_6 = _reg("DarkOliveGreen6", "OliveGreen", 215, 255, 135);
    public static final NColor DARK_SEA_GREEN_9 = _reg("DarkSeaGreen9", "SeaGreen", 215, 255, 175);
    public static final NColor HONEY = _reg("Honey", "Honey", 224, 172, 105);
    public static final NColor HONEYDEW = _reg("Honeydew", "Honeydew", 215, 255, 215);
    public static final NColor HUSK = _reg("Husk", "Husk", 189, 165, 93);
    public static final NColor IVORY = _reg("Ivory", "Ivory", 255, 255, 240);
    public static final NColor LIGHT_CYAN_2 = _reg("LightCyan2", "Cyan", 215, 255, 255);
    public static final NColor RED_4 = _reg("Red4", "Red", 255, 0, 0);
    public static final NColor DEEP_PINK_6 = _reg("DeepPink6", "DeepPink", 255, 0, 95);
    public static final NColor DEEP_PINK_7 = _reg("DeepPink7", "DeepPink", 255, 0, 135);
    public static final NColor DEEP_PINK_8 = _reg("DeepPink8", "DeepPink", 255, 0, 175);
    public static final NColor MAGENTA_5 = _reg("Magenta5", "Magenta", 255, 0, 215);
    public static final NColor MAGENTA_6 = _reg("Magenta6", "Magenta", 255, 0, 255);
    public static final NColor ORANGE_RED = _reg("OrangeRed", "OrangeRed", 255, 95, 0);
    public static final NColor INDIAN_RED_3 = _reg("IndianRed3", "IndianRed", 255, 95, 95);
    public static final NColor INDIAN_RED_4 = _reg("IndianRed4", "IndianRed", 255, 95, 135);
    public static final NColor HOT_PINK_4 = _reg("HotPink4", "HotPink", 255, 95, 175);
    public static final NColor HOT_PINK_5 = _reg("HotPink5", "HotPink", 255, 95, 215);
    public static final NColor MEDIUM_ORCHID_4 = _reg("MediumOrchid4", "Orchid", 255, 95, 255);
    public static final NColor DARK_ORANGE_3 = _reg("DarkOrange3", "Orange", 255, 135, 0);
    public static final NColor SALMON = _reg("Salmon", "Salmon", 255, 135, 95);
    public static final NColor LIGHT_CORAL = _reg("LightCoral", "Coral", 255, 135, 135);
    public static final NColor PALE_VIOLET_RED = _reg("PaleVioletRed", "VioletRed", 255, 135, 175);
    public static final NColor ORCHID_2 = _reg("Orchid2", "Orchid", 255, 135, 215);
    public static final NColor ORCHID_3 = _reg("Orchid3", "Orchid", 255, 135, 255);
    public static final NColor ORANGE_4 = _reg("Orange4", "Orange", 255, 175, 0);
    public static final NColor SAND = _reg("Sand", "Sand", 194, 178, 128);
    public static final NColor SANDY_BROWN = _reg("SandyBrown", "SandyBrown", 255, 175, 95);
    public static final NColor LIGHT_SALMON_3 = _reg("LightSalmon3", "Salmon", 255, 175, 135);
    public static final NColor LIGHT_PINK_3 = _reg("LightPink3", "Pink", 255, 175, 175);
    public static final NColor PINK_2 = _reg("Pink2", "Pink", 255, 175, 215);
    public static final NColor PLUM_4 = _reg("Plum4", "Plum", 255, 175, 255);
    public static final NColor GOLD_3 = _reg("Gold3", "Gold", 255, 215, 0);
    public static final NColor LIGHT_GOLDENROD_3 = _reg("LightGoldenrod3", "Goldenrod", 255, 215, 95);
    public static final NColor LIGHT_GOLDENROD_4 = _reg("LightGoldenrod4", "Goldenrod", 255, 215, 135);
    public static final NColor NAVAJO_WHITE_2 = _reg("NavajoWhite2", "NavajoWhite", 255, 215, 175);
    public static final NColor MISTY_ROSE_2 = _reg("MistyRose2", "MistyRose", 255, 215, 215);
    public static final NColor THISTLE_2 = _reg("Thistle2", "Thistle", 255, 215, 255);
    public static final NColor YELLOW_7 = _reg("Yellow7", "Yellow", 255, 255, 0);
    public static final NColor LIGHT_GOLDENROD_5 = _reg("LightGoldenrod5", "Goldenrod", 255, 255, 95);
    public static final NColor KHAKI_2 = _reg("Khaki2", "Khaki", 255, 255, 135);
    public static final NColor WHEAT_2 = _reg("Wheat2", "Wheat", 255, 255, 175);
    public static final NColor CORNSILK = _reg("Cornsilk", "Cornsilk", 255, 255, 215);
    public static final NColor TUMBLEWEED = _reg("Tumbleweed", "Tumbleweed", 220, 173, 141);
    public static final NColor TACHA = _reg("Tacha", "Tacha", 214, 183, 90);
    public static final NColor SHADOW = _reg("Shadow", "Shadow", 138, 121, 93);
    public static final NColor MOCASSIN = _reg("Moccasin", "Moccasin", 255, 228, 181);

    public static final NColor GRAY = _reg("Gray", "Gray", 128, 128, 128);
    public static final NColor GRAY_0 = _regGray(0);
    public static final NColor GRAY_1 = _regGray(1);
    public static final NColor GRAY_2 = _regGray(2);
    public static final NColor GRAY_3 = _reg("Gray3", "Gray", 8, 8, 8);
    public static final NColor GRAY_4 = _regGray(4);
    public static final NColor GRAY_5 = _regGray(5);
    public static final NColor GRAY_6 = _regGray(6);
    public static final NColor GRAY_7 = _reg("Gray7", "Gray", 18, 18, 18);
    public static final NColor GRAY_8 = _regGray(8);
    public static final NColor GRAY_9 = _regGray(9);
    public static final NColor GRAY_10 = _regGray(10);
    public static final NColor GRAY_11 = _reg("Gray11", "Gray", 28, 28, 28);
    public static final NColor GRAY_12 = _regGray(12);
    public static final NColor GRAY_13 = _regGray(13);
    public static final NColor GRAY_14 = _regGray(14);
    public static final NColor GRAY_15 = _reg("Gray15", "Gray", 38, 38, 38);
    public static final NColor GRAY_16 = _regGray(16);
    public static final NColor GRAY_17 = _regGray(17);
    public static final NColor GRAY_18 = _regGray(18);
    public static final NColor GRAY_19 = _reg("Gray19", "Gray", 48, 48, 48);
    public static final NColor GRAY_20 = _regGray(20);
    public static final NColor GRAY_21 = _regGray(21);
    public static final NColor GRAY_22 = _regGray(22);
    public static final NColor GRAY_23 = _reg("Gray23", "Gray", 58, 58, 58);
    public static final NColor GRAY_24 = _regGray(24);
    public static final NColor GRAY_25 = _regGray(25);
    public static final NColor GRAY_26 = _regGray(26);
    public static final NColor GRAY_27 = _reg("Gray27", "Gray", 68, 68, 68);
    public static final NColor GRAY_28 = _regGray(28);
    public static final NColor GRAY_29 = _regGray(29);
    public static final NColor GRAY_30 = _reg("Gray30", "Gray", 78, 78, 78);
    public static final NColor GRAY_31 = _regGray(31);
    public static final NColor GRAY_32 = _regGray(32);
    public static final NColor GRAY_33 = _regGray(33);
    public static final NColor GRAY_34 = _regGray(34);
    public static final NColor GRAY_35 = _reg("Gray35", "Gray", 88, 88, 88);
    public static final NColor GRAY_36 = _regGray(36);
    public static final NColor GRAY_37 = _reg("Gray37", "Gray", 95, 95, 95);
    public static final NColor GRAY_38 = _regGray(38);
    public static final NColor GRAY_39 = _reg("Gray39", "Gray", 98, 98, 98);
    public static final NColor GRAY_40 = _regGray(40);
    public static final NColor GRAY_41 = _regGray(41);
    public static final NColor GRAY_42 = _reg("Gray42", "Gray", 108, 108, 108);
    public static final NColor GRAY_43 = _regGray(43);
    public static final NColor GRAY_44 = _regGray(44);
    public static final NColor GRAY_45 = _regGray(45);
    public static final NColor GRAY_46 = _reg("Gray46", "Gray", 118, 118, 118);
    public static final NColor GRAY_47 = _regGray(47);
    public static final NColor GRAY_48 = _regGray(48);
    public static final NColor GRAY_49 = _regGray(49);
    public static final NColor GRAY_50 = _reg("Gray50", "Gray", 128, 128, 128);
    public static final NColor GRAY_51 = _regGray(51);
    public static final NColor GRAY_52 = _regGray(52);
    public static final NColor GRAY_53 = _reg("Gray53", "Gray", 135, 135, 135);
    public static final NColor GRAY_54 = _reg("Gray54", "Gray", 138, 138, 138);
    public static final NColor GRAY_55 = _regGray(55);
    public static final NColor GRAY_56 = _regGray(56);
    public static final NColor GRAY_57 = _regGray(57);
    public static final NColor GRAY_58 = _reg("Gray58", "Gray", 148, 148, 148);
    public static final NColor GRAY_59 = _regGray(59);
    public static final NColor GRAY_60 = _regGray(60);
    public static final NColor GRAY_61 = _regGray(61);
    public static final NColor GRAY_62 = _reg("Gray62", "Gray", 158, 158, 158);
    public static final NColor GRAY_63 = _reg("Gray63", "Gray", 175, 135, 175);
    public static final NColor GRAY_64 = _regGray(64);
    public static final NColor GRAY_65 = _regGray(65);
    public static final NColor GRAY_66 = _reg("Gray66", "Gray", 168, 168, 168);
    public static final NColor GRAY_67 = _regGray(67);
    public static final NColor GRAY_68 = _regGray(68);
    public static final NColor GRAY_69 = _reg("Gray69", "Gray", 175, 175, 175);
    public static final NColor GRAY_70 = _reg("Gray70", "Gray", 178, 178, 178);
    public static final NColor GRAY_71 = _regGray(71);
    public static final NColor GRAY_72 = _regGray(72);
    public static final NColor GRAY_73 = _regGray(73);
    public static final NColor GRAY_74 = _reg("Gray74", "Gray", 188, 188, 188);
    public static final NColor GRAY_75 = _regGray(75);
    public static final NColor GRAY_76 = _regGray(76);
    public static final NColor GRAY_77 = _regGray(77);
    public static final NColor GRAY_78 = _reg("Gray78", "Gray", 198, 198, 198);
    public static final NColor GRAY_79 = _regGray(79);
    public static final NColor GRAY_80 = _regGray(80);
    public static final NColor GRAY_81 = _regGray(81);
    public static final NColor GRAY_82 = _reg("Gray82", "Gray", 208, 208, 208);
    public static final NColor GRAY_83 = _regGray(83);
    public static final NColor GRAY_84 = _reg("Gray84", "Gray", 215, 215, 215);
    public static final NColor GRAY_85 = _reg("Gray85", "Gray", 218, 218, 218);
    public static final NColor GRAY_86 = _regGray(86);
    public static final NColor GRAY_87 = _regGray(87);
    public static final NColor GRAY_88 = _regGray(88);
    public static final NColor GRAY_89 = _reg("Gray89", "Gray", 228, 228, 228);
    public static final NColor GRAY_90 = _regGray(90);
    public static final NColor GRAY_91 = _regGray(91);
    public static final NColor GRAY_92 = _regGray(92);
    public static final NColor GRAY_93 = _reg("Gray93", "Gray", 238, 238, 238);
    public static final NColor GRAY_94 = _regGray(94);
    public static final NColor GRAY_95 = _regGray(95);
    public static final NColor GRAY_96 = _regGray(96);
    public static final NColor GRAY_97 = _regGray(97);
    public static final NColor GRAY_98 = _regGray(98);
    public static final NColor GRAY_99 = _regGray(99);
    public static final NColor GRAY_100 = _reg("Gray100", "Gray", 255, 255, 255);
    public static final NColor LIGHT_GRAY = _reg("LightGray", "Gray", 192, 192, 192);
    public static final NColor DARK_GRAY = _reg("DarkGray", "Gray", 64, 64, 64);
    public static final List<NColor> ALL = Collections.unmodifiableList(_ALL_REGISTERED);
    public static final List<NColor> ALL_CANONICAL = Collections.unmodifiableList(_ALL_CANONICAL);
    public static final Map<String, NColor> BY_NAME = Collections.unmodifiableMap(_ALL_BY_NAME);

    /**
     * ANSI COLORS (4 bits) as 32bits
     */
    public static java.util.List<NColor> ANSI_COLORS_16 = Collections.unmodifiableList(Arrays.asList(
            NColor.of32(0, 0, 0),         // 0: Black
            NColor.of32(128, 0, 0),       // 1: Red
            NColor.of32(0, 128, 0),       // 2: Green
            NColor.of32(128, 128, 0),     // 3: Yellow
            NColor.of32(0, 0, 128),       // 4: Blue
            NColor.of32(128, 0, 128),     // 5: Magenta
            NColor.of32(0, 128, 128),     // 6: Cyan
            NColor.of32(192, 192, 192),   // 7: White (light gray)
            NColor.of32(128, 128, 128),   // 8: Bright Black (dark gray)
            NColor.of32(255, 0, 0),       // 9: Bright Red
            NColor.of32(0, 255, 0),       //10: Bright Green
            NColor.of32(255, 255, 0),     //11: Bright Yellow
            NColor.of32(0, 0, 255),       //12: Bright Blue
            NColor.of32(255, 0, 255),     //13: Bright Magenta
            NColor.of32(0, 255, 255),     //14: Bright Cyan
            NColor.of32(255, 255, 255)    //15: Bright White
    ));
    /**
     * ANSI COLORS (8 bits) as 32bits
     */
    public static final List<NColor> ANSI_COLORS_256;

    static {
        List<NColor> ansiColors = new ArrayList<>(256);
        ansiColors.addAll(ANSI_COLORS_16);
        // 16–231: 6×6×6 RGB cube
        int[] levels = {0, 95, 135, 175, 215, 255};
        int index = 16;
        for (int r = 0; r < 6; r++) {
            for (int g = 0; g < 6; g++) {
                for (int b = 0; b < 6; b++) {
                    ansiColors.add(NColor.of32(levels[r], levels[g], levels[b]));
                }
            }
        }
        // 232–255: Grayscale from 8 to 238 in steps of 10
        for (int i = 0; i < 24; i++) {
            int gray = 8 + i * 10;
            ansiColors.add(NColor.of32(gray, gray, gray));
        }
        ANSI_COLORS_256 = Collections.unmodifiableList(ansiColors);
    }
//    static {
//        for (NColor nNamedColor : _ALL_CANONICAL) {
//            List<NColor> li = _ALL_BY_CANONICAL_NAME.get(nNamedColor.getName());
//            System.out.println(nNamedColor.getName()+" :: "+li.stream().map(x->x.getName()).sorted().collect(Collectors.joining(",")));
//            boolean light=li.stream().anyMatch(x->x.getName().contains("Light"));
//            boolean dark=li.stream().anyMatch(x->x.getName().contains("Dark"));
//            boolean medium=li.stream().anyMatch(x->x.getName().contains("Medium"));
//            if(light&&!dark){
//                System.out.println(">>>>>>>> missing Dark"+nNamedColor.getName());
//            }
//            if(!light&&dark){
//                System.out.println(">>>>>>>> missing Light"+nNamedColor.getName());
//            }
//            if(medium&&!light){
//                System.out.println(">>>>>>>> missing Light"+nNamedColor.getName());
//            }
//            if(medium&&!dark){
//                System.out.println(">>>>>>>> missing Dark"+nNamedColor.getName());
//            }
//        }
//    }

    /**
     * Deterministic mapping from int → NColor.
     * <p>
     * Two-step mapping ensures colors are evenly chosen across canonical colors,
     * so grays (which are more numerous) don’t dominate the distribution.
     */
    public static NColor pickColor(int hashCode) {
        int a = Math.abs(hashCode);
        NColor c = _ALL_CANONICAL.get(a % _ALL_CANONICAL.size());
        List<NColor> li = _ALL_BY_CANONICAL_NAME.get(c.getName());
        return li.get(a % li.size());
    }


    private static NColor _reg2(String canonicalName, NColor color) {
        NColor namedColor = color;
        _ALL_REGISTERED.add(namedColor);
        _ALL_BY_NAME.put(namedColor.getName(), namedColor);
        _ALL_BY_CANONICAL_NAME.computeIfAbsent(canonicalName, v -> new ArrayList<>()).add(namedColor);
        if (canonicalName.equals(namedColor.getName())) {
            _ALL_CANONICAL.add(namedColor);
        }
        return namedColor;
    }

//    private static NColor _reg(String name, int r, int g, int b) {
//        return _reg(name, new Color(r, g, b));
//    }

    private static NColor _reg(String name, String canonicalName, int r, int g, int b) {
        return _reg2(canonicalName, NColor.of32(r, g, b, name));
    }

    private static NColor _regGray(int percent) {
        int v = Math.round(percent * 255 / 100f);
        return _reg2("Gray", NColor.of32(v, v, v, "Gray" + percent));
    }


    public static NOptional<NColor> ofName(String name) {
        return NOptional.ofNamed(BY_NAME.get(name == null ? null : NNameFormat.CLASS_NAME.format(name.trim()).toLowerCase()), "color " + name);
    }

    public static NOptional<List<NColor>> ofCanonicalName(String name) {
        return NOptional.ofNamed(_ALL_BY_CANONICAL_NAME.get(name == null ? null : NNameFormat.CLASS_NAME.format(name.trim()).toLowerCase()), "color " + name);
    }

    public static String toHtmlHex(NColor cl) {
        return String.format("#%02X%02X%02X", cl.getRed(), cl.getGreen(), cl.getBlue());
    }

    public static NColor ansiToColor(int index) {
        if (index < 0 || index > 255) {
            throw new IllegalArgumentException("ANSI color index must be between 0 and 255");
        }

        if (index < 16) {
            // Basic colors
            int[] basicColors = {
                    0x000000, 0x800000, 0x008000, 0x808000,
                    0x000080, 0x800080, 0x008080, 0xC0C0C0,
                    0x808080, 0xFF0000, 0x00FF00, 0xFFFF00,
                    0x0000FF, 0xFF00FF, 0x00FFFF, 0xFFFFFF
            };
            return NColor.of32(basicColors[index]);
        } else if (index < 232) {
            // 6x6x6 color cube
            int level = index - 16;
            int r = (level / 36) % 6 * 51;
            int g = (level / 6) % 6 * 51;
            int b = level % 6 * 51;
            return NColor.of32(r, g, b);
        } else {
            // Grayscale colors
            int gray = (index - 232) * 10 + 8;
            return NColor.of32(gray, gray, gray);
        }
    }

    // end static
    private final Bits bits;
    private final long color;
    private final String name;

    public enum Bits implements NEnum {
        BITS_4(4),
        BITS_8(8),
        BITS_16(16),
        BITS_24(24),
        BITS_32(32),
        BITS_64(64);
        private final int bits;
        private final String id;

        Bits(int bits) {
            this.bits = bits;
            this.id = NNameFormat.ID_NAME.format(name());
        }

        public int bits() {
            return bits;
        }

        @Override
        public String id() {
            return id;
        }

        public static NOptional<Bits> parse(String value) {
            return NEnumUtils.parseEnum(value, Bits.class, enumValue -> {
                switch (enumValue.getNormalizedValue()) {
                    case "STANDARD":
                        return NOptional.of(Bits.BITS_32);
                    case "BITS4":
                        return NOptional.of(Bits.BITS_4);
                    case "BITS8":
                        return NOptional.of(Bits.BITS_8);
                    case "BITS16":
                        return NOptional.of(Bits.BITS_16);
                    case "BITS24":
                        return NOptional.of(Bits.BITS_24);
                    case "BITS32":
                        return NOptional.of(Bits.BITS_32);
                    case "BITS64":
                        return NOptional.of(Bits.BITS_64);
                }
                return NOptional.ofNamedEmpty(value);
            });
        }
    }

    public NColor(Bits bits, long color, String name) {
        this.bits = bits;
        this.name = name;
        switch (bits){
            case BITS_4:{
                this.color = color & 0xF;
                break;
            }
            case BITS_8:{
                this.color = color & 0xFF;
                break;
            }
            case BITS_16:{
                this.color = color & 0xFFFF;
                break;
            }
            case BITS_24:{
                this.color = color & 0xFFFFFF;
                break;
            }
            case BITS_32:{
                this.color = (int)color;
                break;
            }
            default:{
                this.color = color;
            }
        }
    }

    public String getName() {
        return name;
    }

    public static NColor of4(int color) {
        return new NColor(Bits.BITS_4, color, null);
    }

    public static NColor of8(int color) {
        return new NColor(Bits.BITS_8, color, null);
    }

    public static NColor of24(int color) {
        return new NColor(Bits.BITS_24, color, null);
    }

    public static NColor of32(int r, int g, int b, String name) {
        return new NColor(Bits.BITS_32, (r & 255) << 16 | (g & 255) << 8 | (b & 255) << 0, name);
    }

    public static NColor of32(int r, int g, int b) {
        return new NColor(Bits.BITS_32, (r & 255) << 16 | (g & 255) << 8 | (b & 255) << 0, null);
    }

    public static NColor of32(int r, int g, int b, int a) {
        return new NColor(Bits.BITS_32, (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255) << 0, null);
    }

    public static NColor of32(int r, int g, int b, int a, String name) {
        return new NColor(Bits.BITS_32, (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255) << 0, name);
    }

    public static NColor of32(int color) {
        return new NColor(Bits.BITS_32, color, null);
    }

    public static NColor of64(long color) {
        return new NColor(Bits.BITS_64, color, null);
    }

    public NColor withName(String name) {
        return new NColor(bits, color, name);
    }

    public Bits getBits() {
        return bits;
    }

    public NColor toColor32() {
        switch (bits) {
            case BITS_4: {
                int c = (int) color;
                if (c >= 0 && c < 16) {
                    return ANSI_COLORS_16.get(c);
                }
                return ANSI_COLORS_16.get(0);
            }
            case BITS_16:
            case BITS_24: {
                int c = (int) color;
                if (c >= 0 && c < 255) {
                    return ANSI_COLORS_256.get(c);
                }
                return ANSI_COLORS_256.get(0);
            }
            case BITS_32:
                return this;
            case BITS_64:
                return NColor.of32((int) color);
        }
        return NColor.of32((int) color);
    }

    public int getIntColor() {
        return (int) color;
    }

    public long getLongColor() {
        return color;
    }

    public int getRGB() {
        if (bits == Bits.BITS_32) {
            return (int) color;
        }
        return toColor32().getRGB();
    }

    public int getRed() {
        return this.getRGB() >> 16 & 255;
    }

    public int getGreen() {
        return this.getRGB() >> 8 & 255;
    }

    public int getBlue() {
        return this.getRGB() >> 0 & 255;
    }

    public int getAlpha() {
        return this.getRGB() >> 24 & 255;
    }


}
