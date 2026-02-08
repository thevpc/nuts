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
public interface NColor {
    // start static

    NColor BLACK = AbstractNColor._reg("Black", "Black", 0, 0, 0);
    NColor MAROON = AbstractNColor._reg("Maroon", "Maroon", 128, 0, 0);
    NColor GREEN = AbstractNColor._reg("Green", "Green", 0, 128, 0);
    NColor OLIVE = AbstractNColor._reg("Olive", "Olive", 128, 128, 0);
    NColor NAVY = AbstractNColor._reg("Navy", "Navy", 0, 0, 128);
    NColor PURPLE = AbstractNColor._reg("Purple", "Purple", 128, 0, 128);
    NColor TEAL = AbstractNColor._reg("Teal", "Teal", 0, 128, 128);
    NColor SILVER = AbstractNColor._reg("Silver", "Silver", 192, 192, 192);
    NColor RED = AbstractNColor._reg("Red", "Red", 255, 0, 0);
    NColor LIME = AbstractNColor._reg("Lime", "Lime", 0, 255, 0);
    NColor YELLOW = AbstractNColor._reg("Yellow", "Yellow", 255, 255, 0);
    NColor DARK_YELLOW = AbstractNColor._reg("DarkYellow", "Yellow", 186, 142, 35);
    NColor BLUE = AbstractNColor._reg("Blue", "Blue", 0, 0, 255);
    NColor FUCHSIA = AbstractNColor._reg("Fuchsia", "Fuchsia", 255, 0, 255);
    NColor AQUA = AbstractNColor._reg("Aqua", "Aqua", 0, 255, 255);
    NColor WHITE = AbstractNColor._reg("White", "White", 255, 255, 255);
    NColor NAVY_BLUE = AbstractNColor._reg("NavyBlue", "NavyBlue", 0, 0, 95);
    NColor DARK_BLUE = AbstractNColor._reg("DarkBlue", "Blue", 0, 0, 135);
    NColor LIGHT_BLUE = AbstractNColor._reg("LightBlue", "Blue", 173, 216, 230);
    NColor BLUE_2 = AbstractNColor._reg("Blue2", "Blue", 0, 0, 175);
    NColor BLUE_3 = AbstractNColor._reg("Blue3", "Blue", 0, 0, 215);
    NColor BLUE_4 = AbstractNColor._reg("Blue4", "Blue", 0, 0, 255);
    NColor DARK_GREEN = AbstractNColor._reg("DarkGreen", "Green", 0, 95, 0);
    NColor DEEP_SKY_BLUE = AbstractNColor._reg("DeepSkyBlue", "DeepSkyBlue", 0, 95, 95);
    NColor DEEP_SKY_BLUE_2 = AbstractNColor._reg("DeepSkyBlue2", "DeepSkyBlue", 0, 95, 135);
    NColor DEEP_SKY_BLUE_3 = AbstractNColor._reg("DeepSkyBlue3", "DeepSkyBlue", 0, 95, 175);
    NColor DODGER_BLUE = AbstractNColor._reg("DodgerBlue", "DodgerBlue", 0, 95, 215);
    NColor DODGER_BLUE_2 = AbstractNColor._reg("DodgerBlue2", "DodgerBlue", 0, 95, 255);
    NColor GREEN_2 = AbstractNColor._reg("Green2", "Green", 0, 135, 0);
    NColor SPRING_GREEN = AbstractNColor._reg("SpringGreen", "SpringGreen", 0, 135, 95);
    NColor LIGHT_SPRING_GREEN = AbstractNColor._reg("LightSpringGreen", "SpringGreen", 139, 231, 185);
    NColor DARK_SPRING_GREEN = AbstractNColor._reg("DarkSpringGreen", "SpringGreen", 23, 114, 69);
    NColor TURQUOISE = AbstractNColor._reg("Turquoise", "Turquoise", 0, 135, 135);
    NColor DEEP_SKY_BLUE_4 = AbstractNColor._reg("DeepSkyBlue4", "DeepSkyBlue", 0, 135, 175);
    NColor DEEP_SKY_BLUE_5 = AbstractNColor._reg("DeepSkyBlue5", "DeepSkyBlue", 0, 135, 215);
    NColor DODGER_BLUE_3 = AbstractNColor._reg("DodgerBlue3", "DodgerBlue", 0, 135, 255);
    NColor GREEN_3 = AbstractNColor._reg("Green3", "Green", 0, 175, 0);
    NColor SPRING_GREEN_2 = AbstractNColor._reg("SpringGreen2", "SpringGreen", 0, 175, 95);
    NColor DARK_CYAN = AbstractNColor._reg("DarkCyan", "Cyan", 0, 175, 135);
    NColor LIGHT_SEA_GREEN = AbstractNColor._reg("LightSeaGreen", "SeaGreen", 0, 175, 175);
    NColor DEEP_SKY_BLUE_6 = AbstractNColor._reg("DeepSkyBlue6", "DeepSkyBlue", 0, 175, 215);
    NColor DEEP_SKY_BLUE_7 = AbstractNColor._reg("DeepSkyBlue7", "DeepSkyBlue", 0, 175, 255);
    NColor GREEN_4 = AbstractNColor._reg("Green4", "Green", 0, 215, 0);
    NColor SPRING_GREEN_3 = AbstractNColor._reg("SpringGreen3", "SpringGreen", 0, 215, 95);
    NColor SPRING_GREEN_4 = AbstractNColor._reg("SpringGreen4", "SpringGreen", 0, 215, 135);
    NColor CYAN = AbstractNColor._reg("Cyan", "Cyan", 0, 215, 175);
    NColor DARK_TURQUOISE = AbstractNColor._reg("DarkTurquoise", "Turquoise", 0, 215, 215);
    NColor LIGHT_TURQUOISE = AbstractNColor._reg("LightTurquoise", "Turquoise", 175, 228, 222);
    NColor TURQUOISE_2 = AbstractNColor._reg("Turquoise2", "Turquoise", 0, 215, 255);
    NColor GREEN_5 = AbstractNColor._reg("Green5", "Green", 0, 255, 0);
    NColor SPRING_GREEN_5 = AbstractNColor._reg("SpringGreen5", "SpringGreen", 0, 255, 95);
    NColor SPRING_GREEN_6 = AbstractNColor._reg("SpringGreen6", "SpringGreen", 0, 255, 135);
    NColor MEDIUM_SPRING_GREEN = AbstractNColor._reg("MediumSpringGreen", "SpringGreen", 0, 255, 175);
    NColor CYAN_2 = AbstractNColor._reg("Cyan2", "Cyan", 0, 255, 215);
    NColor CYAN_3 = AbstractNColor._reg("Cyan3", "Cyan", 0, 255, 255);
    NColor DARK_RED = AbstractNColor._reg("DarkRed", "Red", 95, 0, 0);
    NColor LIGHT_RED = AbstractNColor._reg("LightRed", "Red", 255, 114, 118);
    NColor CERISE = AbstractNColor._reg("Cerise", "Cerise", 223, 70, 97);
    NColor STRAWBERRY = AbstractNColor._reg("Strawberry", "Strawberry", 197, 70, 68);
    NColor MANGO = AbstractNColor._reg("Mango", "Mango", 183, 94, 74);
    NColor SCARLET = AbstractNColor._reg("Scarlet", "Scarlet", 255, 36, 0);
    NColor BEIGE = AbstractNColor._reg("Beige", "Beige", 245, 245, 220);
    NColor BRICK = AbstractNColor._reg("Brick", "Brick", 188, 74, 60);
    NColor DEEP_PINK = AbstractNColor._reg("DeepPink", "DeepPink", 95, 0, 95);
    NColor PURPLE_2 = AbstractNColor._reg("Purple2", "Purple", 95, 0, 135);
    NColor PURPLE_3 = AbstractNColor._reg("Purple3", "Purple", 95, 0, 175);
    NColor PURPLE_4 = AbstractNColor._reg("Purple4", "Purple", 95, 0, 215);
    NColor BLUE_VIOLET = AbstractNColor._reg("BlueViolet", "BlueViolet", 95, 0, 255);
    NColor ORANGE = AbstractNColor._reg("Orange", "Orange", 255, 165, 0);
    NColor LIGHT_ORANGE = AbstractNColor._reg("LightOrange", "Orange", 250, 181, 127);
    NColor MEDIUM_PURPLE = AbstractNColor._reg("MediumPurple", "Purple", 95, 95, 135);
    NColor DARK_SLATE_BLUE = AbstractNColor._reg("DarkSlateBlue", "SlateBlue", 72, 61, 139);
    NColor SLATE_BLUE = AbstractNColor._reg("SlateBlue", "SlateBlue", 95, 95, 175);
    NColor SLATE_BLUE_2 = AbstractNColor._reg("SlateBlue2", "SlateBlue", 95, 95, 215);
    NColor ROYAL_BLUE = AbstractNColor._reg("RoyalBlue", "RoyalBlue", 95, 95, 255);
    NColor CHARTREUSE = AbstractNColor._reg("Chartreuse", "Chartreuse", 95, 135, 0);
    NColor DARK_SEA_GREEN = AbstractNColor._reg("DarkSeaGreen", "SeaGreen", 95, 135, 95);
    NColor PALE_TURQUOISE = AbstractNColor._reg("PaleTurquoise", "PaleTurquoise", 95, 135, 135);
    NColor DARK_STEEL_BLUE = AbstractNColor._reg("DarkSteelBlue", "SteelBlue", 41, 93, 138);
    NColor STEEL_BLUE = AbstractNColor._reg("SteelBlue", "SteelBlue", 95, 135, 175);
    NColor STEEL_BLUE_2 = AbstractNColor._reg("SteelBlue2", "SteelBlue", 95, 135, 215);
    NColor CORNFLOWER_BLUE = AbstractNColor._reg("CornflowerBlue", "CornflowerBlue", 95, 135, 255);
    NColor CHARTREUSE_2 = AbstractNColor._reg("Chartreuse2", "Chartreuse", 95, 175, 0);
    NColor DARK_SEA_GREEN_2 = AbstractNColor._reg("DarkSeaGreen2", "SeaGreen", 95, 175, 95);
    NColor CADET_BLUE = AbstractNColor._reg("CadetBlue", "CadetBlue", 95, 175, 135);
    NColor CADET_BLUE_2 = AbstractNColor._reg("CadetBlue2", "CadetBlue", 95, 175, 175);
    NColor SKY_BLUE = AbstractNColor._reg("SkyBlue", "Blue", 95, 175, 215);
    NColor STEEL_BLUE_3 = AbstractNColor._reg("SteelBlue3", "SteelBlue", 95, 175, 255);
    NColor CHARTREUSE_3 = AbstractNColor._reg("Chartreuse3", "Chartreuse", 95, 215, 0);
    NColor PALE_GREEN = AbstractNColor._reg("PaleGreen", "PaleGreen", 95, 215, 95);
    NColor SEA_GREEN = AbstractNColor._reg("SeaGreen", "SeaGreen", 95, 215, 135);
    NColor AQUAMARINE = AbstractNColor._reg("Aquamarine", "Aquamarine", 95, 215, 175);
    NColor MEDIUM_TURQUOISE = AbstractNColor._reg("MediumTurquoise", "Turquoise", 95, 215, 215);
    NColor STEEL_BLUE_4 = AbstractNColor._reg("SteelBlue4", "SteelBlue", 95, 215, 255);
    NColor CHARTREUSE_4 = AbstractNColor._reg("Chartreuse4", "Chartreuse", 95, 255, 0);
    NColor SEA_GREEN_2 = AbstractNColor._reg("SeaGreen2", "SeaGreen", 95, 255, 95);
    NColor SEA_GREEN_3 = AbstractNColor._reg("SeaGreen3", "SeaGreen", 95, 255, 135);
    NColor SEA_GREEN_4 = AbstractNColor._reg("SeaGreen4", "SeaGreen", 95, 255, 175);
    NColor AQUAMARINE_2 = AbstractNColor._reg("Aquamarine2", "Aquamarine", 95, 255, 215);
    NColor DARK_SLATE_GRAY = AbstractNColor._reg("DarkSlateGray", "SlateGray", 95, 255, 255);
    NColor DARK_RED_2 = AbstractNColor._reg("DarkRed2", "Red", 135, 0, 0);
    NColor DEEP_PINK_2 = AbstractNColor._reg("DeepPink2", "DeepPink", 135, 0, 95);
    NColor LIGHT_MAGENTA = AbstractNColor._reg("LightMagenta", "Magenta", 255, 119, 255);
    NColor DARK_MAGENTA = AbstractNColor._reg("DarkMagenta", "Magenta", 135, 0, 135);
    NColor DARK_MAGENTA_2 = AbstractNColor._reg("DarkMagenta2", "Magenta", 135, 0, 175);
    NColor DARK_VIOLET = AbstractNColor._reg("DarkViolet", "Violet", 135, 0, 215);
    NColor PURPLE_5 = AbstractNColor._reg("Purple5", "Purple", 135, 0, 255);
    NColor ORANGE_2 = AbstractNColor._reg("Orange2", "Orange", 135, 95, 0);
    NColor LIGHT_PINK = AbstractNColor._reg("LightPink", "Pink", 135, 95, 95);
    NColor PLUM = AbstractNColor._reg("Plum", "Plum", 135, 95, 135);
    NColor MEDIUM_PURPLE_2 = AbstractNColor._reg("MediumPurple2", "Purple", 135, 95, 175);
    NColor MEDIUM_PURPLE_3 = AbstractNColor._reg("MediumPurple3", "Purple", 135, 95, 215);
    NColor SLATE_BLUE_3 = AbstractNColor._reg("SlateBlue3", "SlateBlue", 135, 95, 255);
    NColor YELLOW_2 = AbstractNColor._reg("Yellow2", "Yellow", 135, 135, 0);
    NColor WHEAT = AbstractNColor._reg("Wheat", "Wheat", 135, 135, 95);
    NColor LIGHT_SLATE_GRAY = AbstractNColor._reg("LightSlateGray", "SlateGray", 135, 135, 175);
    NColor MEDIUM_PURPLE_4 = AbstractNColor._reg("MediumPurple4", "Purple", 135, 135, 215);
    NColor LIGHT_SLATE_BLUE = AbstractNColor._reg("LightSlateBlue", "SlateBlue", 135, 135, 255);
    NColor YELLOW_3 = AbstractNColor._reg("Yellow3", "Yellow", 135, 175, 0);
    NColor DARK_OLIVE_GREEN = AbstractNColor._reg("DarkOliveGreen", "OliveGreen", 135, 175, 95);
    NColor DARK_SEA_GREEN_3 = AbstractNColor._reg("DarkSeaGreen3", "SeaGreen", 135, 175, 135);
    NColor LIGHT_SKY_BLUE = AbstractNColor._reg("LightSkyBlue", "SkyBlue", 135, 175, 175);
    NColor LIGHT_SKY_BLUE_2 = AbstractNColor._reg("LightSkyBlue2", "SkyBlue", 135, 175, 215);
    NColor SKY_BLUE_2 = AbstractNColor._reg("SkyBlue2", "SkyBlue", 135, 175, 255);
    NColor CHARTREUSE_5 = AbstractNColor._reg("Chartreuse5", "Chartreuse", 135, 215, 0);
    NColor DARK_OLIVE_GREEN_2 = AbstractNColor._reg("DarkOliveGreen2", "OliveGreen", 135, 215, 95);
    NColor PALE_GREEN_2 = AbstractNColor._reg("PaleGreen2", "PaleGreen", 135, 215, 135);
    NColor DARK_SEA_GREEN_4 = AbstractNColor._reg("DarkSeaGreen4", "SeaGreen", 135, 215, 175);
    NColor DARK_SLATE_GRAY_2 = AbstractNColor._reg("DarkSlateGray2", "SlateGray", 135, 215, 215);
    NColor SKY_BLUE_3 = AbstractNColor._reg("SkyBlue3", "SkyBlue", 135, 215, 255);
    NColor CHARTREUSE_6 = AbstractNColor._reg("Chartreuse6", "Chartreuse", 135, 255, 0);
    NColor LIGHT_GREEN = AbstractNColor._reg("LightGreen", "Green", 135, 255, 95);
    NColor LIGHT_GREEN_2 = AbstractNColor._reg("LightGreen2", "Green", 135, 255, 135);
    NColor PALE_GREEN_3 = AbstractNColor._reg("PaleGreen3", "PaleGreen", 135, 255, 175);
    NColor AQUAMARINE_3 = AbstractNColor._reg("Aquamarine3", "Aquamarine", 135, 255, 215);
    NColor DARK_SLATE_GRAY_3 = AbstractNColor._reg("DarkSlateGray3", "SlateGray", 135, 255, 255);
    NColor RED_2 = AbstractNColor._reg("Red2", "Red", 175, 0, 0);
    NColor DEEP_PINK_3 = AbstractNColor._reg("DeepPink3", "DeepPink", 175, 0, 95);
    NColor MEDIUM_VIOLET_RED = AbstractNColor._reg("MediumVioletRed", "VioletRed", 175, 0, 135);
    NColor LIGHT_VIOLET = AbstractNColor._reg("LightViolet", "Violet", 207, 159, 255);
    NColor MAGENTA = AbstractNColor._reg("Magenta", "Magenta", 175, 0, 175);
    NColor DARK_VIOLET_2 = AbstractNColor._reg("DarkViolet2", "Violet", 175, 0, 215);
    NColor PURPLE_6 = AbstractNColor._reg("Purple6", "Purple", 175, 0, 255);
    NColor LIGHT_PURPLE = AbstractNColor._reg("LightPurple", "Purple", 203, 195, 227);
    NColor DARK_PURPLE = AbstractNColor._reg("DarkPurple", "Purple", 152, 29, 151);
    NColor DARK_ORANGE = AbstractNColor._reg("DarkOrange", "Orange", 175, 95, 0);
    NColor INDIAN_RED = AbstractNColor._reg("IndianRed", "IndianRed", 175, 95, 95);
    NColor HOT_PINK = AbstractNColor._reg("HotPink", "HotPink", 175, 95, 135);
    NColor LIGHT_ORCHID = AbstractNColor._reg("LightOrchid", "Orchid", 230, 168, 215);
    NColor DARK_ORCHID = AbstractNColor._reg("DarkOrchid", "Orchid", 153, 50, 204);
    NColor MEDIUM_ORCHID = AbstractNColor._reg("MediumOrchid", "Orchid", 175, 95, 175);
    NColor MEDIUM_ORCHID_2 = AbstractNColor._reg("MediumOrchid2", "Orchid", 175, 95, 215);
    NColor MEDIUM_PURPLE_5 = AbstractNColor._reg("MediumPurple5", "Purple", 175, 95, 255);
    NColor GOLDENROD = AbstractNColor._reg("Goldenrod", "Goldenrod", 218, 165, 32);
    NColor DARK_GOLDENROD = AbstractNColor._reg("DarkGoldenrod", "Goldenrod", 175, 135, 0);
    NColor LIGHT_SALMON = AbstractNColor._reg("LightSalmon", "Salmon", 175, 135, 95);
    NColor LIGHT_BROWN = AbstractNColor._reg("LightBrown", "Brown", 196, 164, 132);
    NColor BROWN = AbstractNColor._reg("Brown", "Brown", 150, 75, 0);
    NColor DARK_BROWN = AbstractNColor._reg("DarkBrown", "Brown", 101, 67, 33);
    NColor ROSY_BROWN = AbstractNColor._reg("RosyBrown", "RosyBrown", 175, 135, 135);
    NColor MEDIUM_PURPLE_6 = AbstractNColor._reg("MediumPurple6", "Purple", 175, 135, 215);
    NColor MEDIUM_PURPLE_7 = AbstractNColor._reg("MediumPurple7", "Purple", 175, 135, 255);
    NColor GOLD = AbstractNColor._reg("Gold", "Gold", 175, 175, 0);
    NColor LIGHT_KHAKI = AbstractNColor._reg("LightKhaki", "Khaki", 240, 230, 140);
    NColor DARK_KHAKI = AbstractNColor._reg("DarkKhaki", "Khaki", 175, 175, 95);
    NColor NAVAJO_WHITE = AbstractNColor._reg("NavajoWhite", "NavajoWhite", 175, 175, 135);
    NColor LIGHT_STEEL_BLUE = AbstractNColor._reg("LightSteelBlue", "SteelBlue", 175, 175, 215);
    NColor LIGHT_STEEL_BLUE_2 = AbstractNColor._reg("LightSteelBlue2", "SteelBlue", 175, 175, 255);
    NColor YELLOW_4 = AbstractNColor._reg("Yellow4", "Yellow", 175, 215, 0);
    NColor DARK_OLIVE_GREEN_3 = AbstractNColor._reg("DarkOliveGreen3", "OliveGreen", 175, 215, 95);
    NColor DARK_SEA_GREEN_5 = AbstractNColor._reg("DarkSeaGreen5", "SeaGreen", 175, 215, 135);
    NColor DARK_SEA_GREEN_6 = AbstractNColor._reg("DarkSeaGreen6", "SeaGreen", 175, 215, 175);
    NColor LIGHT_CYAN = AbstractNColor._reg("LightCyan", "Cyan", 175, 215, 215);
    NColor LIGHT_SKY_BLUE_3 = AbstractNColor._reg("LightSkyBlue3", "SkyBlue", 175, 215, 255);
    NColor GREEN_YELLOW = AbstractNColor._reg("GreenYellow", "GreenYellow", 175, 255, 0);
    NColor DARK_OLIVE_GREEN_4 = AbstractNColor._reg("DarkOliveGreen4", "OliveGreen", 175, 255, 95);
    NColor PALE_GREEN_4 = AbstractNColor._reg("PaleGreen4", "PaleGreen", 175, 255, 135);
    NColor DARK_SEA_GREEN_7 = AbstractNColor._reg("DarkSeaGreen7", "SeaGreen", 175, 255, 175);
    NColor DARK_SEA_GREEN_8 = AbstractNColor._reg("DarkSeaGreen8", "SeaGreen", 175, 255, 215);
    NColor PALE_TURQUOISE_2 = AbstractNColor._reg("PaleTurquoise2", "PaleTurquoise", 175, 255, 255);
    NColor RED_3 = AbstractNColor._reg("Red3", "Red", 215, 0, 0);
    NColor DEEP_PINK_4 = AbstractNColor._reg("DeepPink4", "DeepPink", 215, 0, 95);
    NColor DEEP_PINK_5 = AbstractNColor._reg("DeepPink5", "DeepPink", 215, 0, 135);
    NColor MAGENTA_2 = AbstractNColor._reg("Magenta2", "Magenta", 215, 0, 175);
    NColor MAGENTA_3 = AbstractNColor._reg("Magenta3", "Magenta", 215, 0, 215);
    NColor MAGENTA_4 = AbstractNColor._reg("Magenta4", "Magenta", 215, 0, 255);
    NColor DARK_ORANGE_2 = AbstractNColor._reg("DarkOrange2", "Orange", 215, 95, 0);
    NColor INDIAN_RED_2 = AbstractNColor._reg("IndianRed2", "IndianRed", 215, 95, 95);
    NColor HOT_PINK_2 = AbstractNColor._reg("HotPink2", "HotPink", 215, 95, 135);
    NColor HOT_PINK_3 = AbstractNColor._reg("HotPink3", "HotPink", 215, 95, 175);
    NColor ORCHID = AbstractNColor._reg("Orchid", "Orchid", 215, 95, 215);
    NColor MEDIUM_ORCHID_3 = AbstractNColor._reg("MediumOrchid3", "Orchid", 215, 95, 255);
    NColor ORANGE_3 = AbstractNColor._reg("Orange3", "Orange", 215, 135, 0);
    NColor DARK_SALMON = AbstractNColor._reg("DarkSalmon", "Salmon", 233, 150, 122);
    NColor LIGHT_SALMON_2 = AbstractNColor._reg("LightSalmon2", "Salmon", 215, 135, 95);
    NColor LIGHT_PINK_2 = AbstractNColor._reg("LightPink2", "Pink", 215, 135, 135);
    NColor DARK_PINK = AbstractNColor._reg("DarkPink", "Pink", 231, 84, 128);
    NColor PINK = AbstractNColor._reg("Pink", "Pink", 215, 135, 175);
    NColor PLUM_2 = AbstractNColor._reg("Plum2", "Plum", 215, 135, 215);
    NColor VIOLET = AbstractNColor._reg("Violet", "Violet", 215, 135, 255);
    NColor GOLD_2 = AbstractNColor._reg("Gold2", "Gold", 215, 175, 0);
    NColor LIGHT_GOLDENROD = AbstractNColor._reg("LightGoldenrod", "Goldenrod", 215, 175, 95);
    NColor TAN = AbstractNColor._reg("Tan", "Tan", 215, 175, 135);
    NColor LIGHT_TAN = AbstractNColor._reg("LightTan", "Tan", 236, 222, 201);
    NColor DARK_TAN = AbstractNColor._reg("DarkTan", "Tan", 145, 129, 81);
    NColor TUSCAN_TAN = AbstractNColor._reg("TuscanTan", "TuscanTan", 166, 123, 91);
    NColor ALMOND = AbstractNColor._reg("Almond", "Almond", 239, 222, 205);
    NColor BONE = AbstractNColor._reg("Bone", "Bone", 227, 218, 201);
    NColor BISCUIT = AbstractNColor._reg("Biscuit", "Biscuit", 239, 204, 162);
    NColor BRANDY = AbstractNColor._reg("Brandy", "Brandy", 218, 188, 148);
    NColor CALICO = AbstractNColor._reg("Calico", "Calico", 224, 141, 91);
    NColor CAMEL = AbstractNColor._reg("Camel", "Camel", 193, 154, 107);
    NColor CAMEO = AbstractNColor._reg("Cameo", "Cameo", 238, 215, 185);
    NColor CARAMEL = AbstractNColor._reg("Caramel", "Caramel", 255, 213, 154);
    NColor CASHMERE = AbstractNColor._reg("Cashmere", "Cashmere", 230, 200, 160);
    NColor CREAM = AbstractNColor._reg("Cream", "Cream", 255, 253, 208);
    NColor CHALKY = AbstractNColor._reg("Chalky", "Chalky", 239, 201, 144);
    NColor DEER = AbstractNColor._reg("Deer", "Deer", 186, 135, 89);
    NColor DESERT = AbstractNColor._reg("Desert", "Desert", 250, 213, 165);
    NColor DIRT = AbstractNColor._reg("Dirt", "Dirt", 155, 118, 83);
    NColor EQUATOR = AbstractNColor._reg("Equator", "Equator", 227, 197, 101);
    NColor MISTY_ROSE = AbstractNColor._reg("MistyRose", "Rose", 215, 175, 175);
    NColor THISTLE = AbstractNColor._reg("Thistle", "Thistle", 215, 175, 215);
    NColor PLUM_3 = AbstractNColor._reg("Plum3", "Plum", 215, 175, 255);
    NColor YELLOW_5 = AbstractNColor._reg("Yellow5", "Yellow", 215, 215, 0);
    NColor KHAKI = AbstractNColor._reg("Khaki", "Khaki", 215, 215, 95);
    NColor LIGHT_GOLDENROD_2 = AbstractNColor._reg("LightGoldenrod2", "Goldenrod", 215, 215, 135);
    NColor LIGHT_YELLOW = AbstractNColor._reg("LightYellow", "Yellow", 215, 215, 175);
    NColor LIGHT_STEEL_BLUE_3 = AbstractNColor._reg("LightSteelBlue3", "SteelBlue", 215, 215, 255);
    NColor YELLOW_6 = AbstractNColor._reg("Yellow6", "Yellow", 215, 255, 0);
    NColor DARK_OLIVE_GREEN_5 = AbstractNColor._reg("DarkOliveGreen5", "OliveGreen", 215, 255, 95);
    NColor DARK_OLIVE_GREEN_6 = AbstractNColor._reg("DarkOliveGreen6", "OliveGreen", 215, 255, 135);
    NColor DARK_SEA_GREEN_9 = AbstractNColor._reg("DarkSeaGreen9", "SeaGreen", 215, 255, 175);
    NColor HONEY = AbstractNColor._reg("Honey", "Honey", 224, 172, 105);
    NColor HONEYDEW = AbstractNColor._reg("Honeydew", "Honeydew", 215, 255, 215);
    NColor HUSK = AbstractNColor._reg("Husk", "Husk", 189, 165, 93);
    NColor IVORY = AbstractNColor._reg("Ivory", "Ivory", 255, 255, 240);
    NColor LIGHT_CYAN_2 = AbstractNColor._reg("LightCyan2", "Cyan", 215, 255, 255);
    NColor RED_4 = AbstractNColor._reg("Red4", "Red", 255, 0, 0);
    NColor DEEP_PINK_6 = AbstractNColor._reg("DeepPink6", "DeepPink", 255, 0, 95);
    NColor DEEP_PINK_7 = AbstractNColor._reg("DeepPink7", "DeepPink", 255, 0, 135);
    NColor DEEP_PINK_8 = AbstractNColor._reg("DeepPink8", "DeepPink", 255, 0, 175);
    NColor MAGENTA_5 = AbstractNColor._reg("Magenta5", "Magenta", 255, 0, 215);
    NColor MAGENTA_6 = AbstractNColor._reg("Magenta6", "Magenta", 255, 0, 255);
    NColor ORANGE_RED = AbstractNColor._reg("OrangeRed", "OrangeRed", 255, 95, 0);
    NColor INDIAN_RED_3 = AbstractNColor._reg("IndianRed3", "IndianRed", 255, 95, 95);
    NColor INDIAN_RED_4 = AbstractNColor._reg("IndianRed4", "IndianRed", 255, 95, 135);
    NColor HOT_PINK_4 = AbstractNColor._reg("HotPink4", "HotPink", 255, 95, 175);
    NColor HOT_PINK_5 = AbstractNColor._reg("HotPink5", "HotPink", 255, 95, 215);
    NColor MEDIUM_ORCHID_4 = AbstractNColor._reg("MediumOrchid4", "Orchid", 255, 95, 255);
    NColor DARK_ORANGE_3 = AbstractNColor._reg("DarkOrange3", "Orange", 255, 135, 0);
    NColor SALMON = AbstractNColor._reg("Salmon", "Salmon", 255, 135, 95);
    NColor LIGHT_CORAL = AbstractNColor._reg("LightCoral", "Coral", 255, 135, 135);
    NColor PALE_VIOLET_RED = AbstractNColor._reg("PaleVioletRed", "VioletRed", 255, 135, 175);
    NColor ORCHID_2 = AbstractNColor._reg("Orchid2", "Orchid", 255, 135, 215);
    NColor ORCHID_3 = AbstractNColor._reg("Orchid3", "Orchid", 255, 135, 255);
    NColor ORANGE_4 = AbstractNColor._reg("Orange4", "Orange", 255, 175, 0);
    NColor SAND = AbstractNColor._reg("Sand", "Sand", 194, 178, 128);
    NColor SANDY_BROWN = AbstractNColor._reg("SandyBrown", "SandyBrown", 255, 175, 95);
    NColor LIGHT_SALMON_3 = AbstractNColor._reg("LightSalmon3", "Salmon", 255, 175, 135);
    NColor LIGHT_PINK_3 = AbstractNColor._reg("LightPink3", "Pink", 255, 175, 175);
    NColor PINK_2 = AbstractNColor._reg("Pink2", "Pink", 255, 175, 215);
    NColor PLUM_4 = AbstractNColor._reg("Plum4", "Plum", 255, 175, 255);
    NColor GOLD_3 = AbstractNColor._reg("Gold3", "Gold", 255, 215, 0);
    NColor LIGHT_GOLDENROD_3 = AbstractNColor._reg("LightGoldenrod3", "Goldenrod", 255, 215, 95);
    NColor LIGHT_GOLDENROD_4 = AbstractNColor._reg("LightGoldenrod4", "Goldenrod", 255, 215, 135);
    NColor NAVAJO_WHITE_2 = AbstractNColor._reg("NavajoWhite2", "NavajoWhite", 255, 215, 175);
    NColor MISTY_ROSE_2 = AbstractNColor._reg("MistyRose2", "MistyRose", 255, 215, 215);
    NColor THISTLE_2 = AbstractNColor._reg("Thistle2", "Thistle", 255, 215, 255);
    NColor YELLOW_7 = AbstractNColor._reg("Yellow7", "Yellow", 255, 255, 0);
    NColor LIGHT_GOLDENROD_5 = AbstractNColor._reg("LightGoldenrod5", "Goldenrod", 255, 255, 95);
    NColor KHAKI_2 = AbstractNColor._reg("Khaki2", "Khaki", 255, 255, 135);
    NColor WHEAT_2 = AbstractNColor._reg("Wheat2", "Wheat", 255, 255, 175);
    NColor CORNSILK = AbstractNColor._reg("Cornsilk", "Cornsilk", 255, 255, 215);
    NColor TUMBLEWEED = AbstractNColor._reg("Tumbleweed", "Tumbleweed", 220, 173, 141);
    NColor TACHA = AbstractNColor._reg("Tacha", "Tacha", 214, 183, 90);
    NColor SHADOW = AbstractNColor._reg("Shadow", "Shadow", 138, 121, 93);
    NColor MOCASSIN = AbstractNColor._reg("Moccasin", "Moccasin", 255, 228, 181);

    NColor GRAY = AbstractNColor._reg("Gray", "Gray", 128, 128, 128);
    NColor GRAY_0 = AbstractNColor._regGray(0);
    NColor GRAY_1 = AbstractNColor._regGray(1);
    NColor GRAY_2 = AbstractNColor._regGray(2);
    NColor GRAY_3 = AbstractNColor._reg("Gray3", "Gray", 8, 8, 8);
    NColor GRAY_4 = AbstractNColor._regGray(4);
    NColor GRAY_5 = AbstractNColor._regGray(5);
    NColor GRAY_6 = AbstractNColor._regGray(6);
    NColor GRAY_7 = AbstractNColor._reg("Gray7", "Gray", 18, 18, 18);
    NColor GRAY_8 = AbstractNColor._regGray(8);
    NColor GRAY_9 = AbstractNColor._regGray(9);
    NColor GRAY_10 = AbstractNColor._regGray(10);
    NColor GRAY_11 = AbstractNColor._reg("Gray11", "Gray", 28, 28, 28);
    NColor GRAY_12 = AbstractNColor._regGray(12);
    NColor GRAY_13 = AbstractNColor._regGray(13);
    NColor GRAY_14 = AbstractNColor._regGray(14);
    NColor GRAY_15 = AbstractNColor._reg("Gray15", "Gray", 38, 38, 38);
    NColor GRAY_16 = AbstractNColor._regGray(16);
    NColor GRAY_17 = AbstractNColor._regGray(17);
    NColor GRAY_18 = AbstractNColor._regGray(18);
    NColor GRAY_19 = AbstractNColor._reg("Gray19", "Gray", 48, 48, 48);
    NColor GRAY_20 = AbstractNColor._regGray(20);
    NColor GRAY_21 = AbstractNColor._regGray(21);
    NColor GRAY_22 = AbstractNColor._regGray(22);
    NColor GRAY_23 = AbstractNColor._reg("Gray23", "Gray", 58, 58, 58);
    NColor GRAY_24 = AbstractNColor._regGray(24);
    NColor GRAY_25 = AbstractNColor._regGray(25);
    NColor GRAY_26 = AbstractNColor._regGray(26);
    NColor GRAY_27 = AbstractNColor._reg("Gray27", "Gray", 68, 68, 68);
    NColor GRAY_28 = AbstractNColor._regGray(28);
    NColor GRAY_29 = AbstractNColor._regGray(29);
    NColor GRAY_30 = AbstractNColor._reg("Gray30", "Gray", 78, 78, 78);
    NColor GRAY_31 = AbstractNColor._regGray(31);
    NColor GRAY_32 = AbstractNColor._regGray(32);
    NColor GRAY_33 = AbstractNColor._regGray(33);
    NColor GRAY_34 = AbstractNColor._regGray(34);
    NColor GRAY_35 = AbstractNColor._reg("Gray35", "Gray", 88, 88, 88);
    NColor GRAY_36 = AbstractNColor._regGray(36);
    NColor GRAY_37 = AbstractNColor._reg("Gray37", "Gray", 95, 95, 95);
    NColor GRAY_38 = AbstractNColor._regGray(38);
    NColor GRAY_39 = AbstractNColor._reg("Gray39", "Gray", 98, 98, 98);
    NColor GRAY_40 = AbstractNColor._regGray(40);
    NColor GRAY_41 = AbstractNColor._regGray(41);
    NColor GRAY_42 = AbstractNColor._reg("Gray42", "Gray", 108, 108, 108);
    NColor GRAY_43 = AbstractNColor._regGray(43);
    NColor GRAY_44 = AbstractNColor._regGray(44);
    NColor GRAY_45 = AbstractNColor._regGray(45);
    NColor GRAY_46 = AbstractNColor._reg("Gray46", "Gray", 118, 118, 118);
    NColor GRAY_47 = AbstractNColor._regGray(47);
    NColor GRAY_48 = AbstractNColor._regGray(48);
    NColor GRAY_49 = AbstractNColor._regGray(49);
    NColor GRAY_50 = AbstractNColor._reg("Gray50", "Gray", 128, 128, 128);
    NColor GRAY_51 = AbstractNColor._regGray(51);
    NColor GRAY_52 = AbstractNColor._regGray(52);
    NColor GRAY_53 = AbstractNColor._reg("Gray53", "Gray", 135, 135, 135);
    NColor GRAY_54 = AbstractNColor._reg("Gray54", "Gray", 138, 138, 138);
    NColor GRAY_55 = AbstractNColor._regGray(55);
    NColor GRAY_56 = AbstractNColor._regGray(56);
    NColor GRAY_57 = AbstractNColor._regGray(57);
    NColor GRAY_58 = AbstractNColor._reg("Gray58", "Gray", 148, 148, 148);
    NColor GRAY_59 = AbstractNColor._regGray(59);
    NColor GRAY_60 = AbstractNColor._regGray(60);
    NColor GRAY_61 = AbstractNColor._regGray(61);
    NColor GRAY_62 = AbstractNColor._reg("Gray62", "Gray", 158, 158, 158);
    NColor GRAY_63 = AbstractNColor._reg("Gray63", "Gray", 175, 135, 175);
    NColor GRAY_64 = AbstractNColor._regGray(64);
    NColor GRAY_65 = AbstractNColor._regGray(65);
    NColor GRAY_66 = AbstractNColor._reg("Gray66", "Gray", 168, 168, 168);
    NColor GRAY_67 = AbstractNColor._regGray(67);
    NColor GRAY_68 = AbstractNColor._regGray(68);
    NColor GRAY_69 = AbstractNColor._reg("Gray69", "Gray", 175, 175, 175);
    NColor GRAY_70 = AbstractNColor._reg("Gray70", "Gray", 178, 178, 178);
    NColor GRAY_71 = AbstractNColor._regGray(71);
    NColor GRAY_72 = AbstractNColor._regGray(72);
    NColor GRAY_73 = AbstractNColor._regGray(73);
    NColor GRAY_74 = AbstractNColor._reg("Gray74", "Gray", 188, 188, 188);
    NColor GRAY_75 = AbstractNColor._regGray(75);
    NColor GRAY_76 = AbstractNColor._regGray(76);
    NColor GRAY_77 = AbstractNColor._regGray(77);
    NColor GRAY_78 = AbstractNColor._reg("Gray78", "Gray", 198, 198, 198);
    NColor GRAY_79 = AbstractNColor._regGray(79);
    NColor GRAY_80 = AbstractNColor._regGray(80);
    NColor GRAY_81 = AbstractNColor._regGray(81);
    NColor GRAY_82 = AbstractNColor._reg("Gray82", "Gray", 208, 208, 208);
    NColor GRAY_83 = AbstractNColor._regGray(83);
    NColor GRAY_84 = AbstractNColor._reg("Gray84", "Gray", 215, 215, 215);
    NColor GRAY_85 = AbstractNColor._reg("Gray85", "Gray", 218, 218, 218);
    NColor GRAY_86 = AbstractNColor._regGray(86);
    NColor GRAY_87 = AbstractNColor._regGray(87);
    NColor GRAY_88 = AbstractNColor._regGray(88);
    NColor GRAY_89 = AbstractNColor._reg("Gray89", "Gray", 228, 228, 228);
    NColor GRAY_90 = AbstractNColor._regGray(90);
    NColor GRAY_91 = AbstractNColor._regGray(91);
    NColor GRAY_92 = AbstractNColor._regGray(92);
    NColor GRAY_93 = AbstractNColor._reg("Gray93", "Gray", 238, 238, 238);
    NColor GRAY_94 = AbstractNColor._regGray(94);
    NColor GRAY_95 = AbstractNColor._regGray(95);
    NColor GRAY_96 = AbstractNColor._regGray(96);
    NColor GRAY_97 = AbstractNColor._regGray(97);
    NColor GRAY_98 = AbstractNColor._regGray(98);
    NColor GRAY_99 = AbstractNColor._regGray(99);
    NColor GRAY_100 = AbstractNColor._reg("Gray100", "Gray", 255, 255, 255);
    NColor LIGHT_GRAY = AbstractNColor._reg("LightGray", "Gray", 192, 192, 192);
    NColor DARK_GRAY = AbstractNColor._reg("DarkGray", "Gray", 64, 64, 64);
    List<NColor> ALL = AbstractNColor.ALL;
    List<NColor> ALL_CANONICAL = AbstractNColor.ALL_CANONICAL;
    Map<String, NColor> BY_NAME = AbstractNColor.BY_NAME;

    /**
     * ANSI COLORS (4 bits) as 32bits
     */
    java.util.List<NColor> ANSI_COLORS_16 = AbstractNColor.ANSI_COLORS_16;
    /**
     * ANSI COLORS (8 bits) as 32bits
     */
    List<NColor> ANSI_COLORS_256=AbstractNColor.ANSI_COLORS_16;



    /**
     * Deterministic mapping from int → NColor.
     * <p>
     * Two-step mapping ensures colors are evenly chosen across canonical colors,
     * so grays (which are more numerous) don’t dominate the distribution.
     */
    static NColor pickColor(int hashCode) {
        return AbstractNColor.pickColor(hashCode);
    }


    static NOptional<NColor> ofName(String name) {
        return AbstractNColor.ofName(name);
    }

    static NOptional<List<NColor>> ofCanonicalName(String name) {
        return AbstractNColor.ofCanonicalName(name);
    }

    static String toHtmlHex(NColor cl) {
        return AbstractNColor.toHtmlHex(cl);
    }

    static NColor ansiToColor(int index) {
        return AbstractNColor.ansiToColor(index);
    }

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

    static NColor of4(int color) {
        return AbstractNColor.of4(color);
    }

    static NColor of4(int color, String name) {
        return AbstractNColor.of4(color,name);
    }

    static NColor of8(int color) {
        return AbstractNColor.of8(color);
    }

    static NColor of8(int color, String name) {
        return AbstractNColor.of8(color,name);
    }

    static NColor of16(int color) {
        return AbstractNColor.of16(color);
    }

    static NColor of16(int color, String name) {
        return AbstractNColor.of16(color,name);
    }

    static NColor of24(int color) {
        return AbstractNColor.of24(color);
    }
    static NColor of24(int color, String name) {
        return AbstractNColor.of24(color,name);
    }

    static NColor of32(int r, int g, int b) {
        return AbstractNColor.of32(r,g,b);
    }

    static NColor of32(int r, int g, int b, String name) {
        return AbstractNColor.of32(r,g,b,name);
    }

    static NColor of32(int r, int g, int b, int a) {
        return AbstractNColor.of32(r,g,b,a);
    }

    static NColor of32(int r, int g, int b, int a, String name) {
        return AbstractNColor.of32(r,g,b,a,name);
    }

    static NColor of32(int color) {
        return AbstractNColor.of32(color);
    }

    static NColor of32(int color, String name) {
        return AbstractNColor.of32(color,name);
    }

    static NColor of64(long color) {
        return AbstractNColor.of64(color);
    }

    static NColor of64(long color, String name) {
        return AbstractNColor.of64(color,name);
    }

    String getName();

    Bits getBits();

    NColor withName(String name);

    int getIntColor();

    long getLongColor();

    NColor toColor32();

    int getRGB();

    int getRed();

    int getGreen();

    int getBlue();

    int getAlpha();

}
