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

    NColor BLACK = NColorBase._reg("Black", "Black", 0, 0, 0);
    NColor MAROON = NColorBase._reg("Maroon", "Maroon", 128, 0, 0);
    NColor GREEN = NColorBase._reg("Green", "Green", 0, 128, 0);
    NColor OLIVE = NColorBase._reg("Olive", "Olive", 128, 128, 0);
    NColor NAVY = NColorBase._reg("Navy", "Navy", 0, 0, 128);
    NColor PURPLE = NColorBase._reg("Purple", "Purple", 128, 0, 128);
    NColor TEAL = NColorBase._reg("Teal", "Teal", 0, 128, 128);
    NColor SILVER = NColorBase._reg("Silver", "Silver", 192, 192, 192);
    NColor RED = NColorBase._reg("Red", "Red", 255, 0, 0);
    NColor LIME = NColorBase._reg("Lime", "Lime", 0, 255, 0);
    NColor YELLOW = NColorBase._reg("Yellow", "Yellow", 255, 255, 0);
    NColor DARK_YELLOW = NColorBase._reg("DarkYellow", "Yellow", 186, 142, 35);
    NColor BLUE = NColorBase._reg("Blue", "Blue", 0, 0, 255);
    NColor FUCHSIA = NColorBase._reg("Fuchsia", "Fuchsia", 255, 0, 255);
    NColor AQUA = NColorBase._reg("Aqua", "Aqua", 0, 255, 255);
    NColor WHITE = NColorBase._reg("White", "White", 255, 255, 255);
    NColor NAVY_BLUE = NColorBase._reg("NavyBlue", "NavyBlue", 0, 0, 95);
    NColor DARK_BLUE = NColorBase._reg("DarkBlue", "Blue", 0, 0, 135);
    NColor LIGHT_BLUE = NColorBase._reg("LightBlue", "Blue", 173, 216, 230);
    NColor BLUE_2 = NColorBase._reg("Blue2", "Blue", 0, 0, 175);
    NColor BLUE_3 = NColorBase._reg("Blue3", "Blue", 0, 0, 215);
    NColor BLUE_4 = NColorBase._reg("Blue4", "Blue", 0, 0, 255);
    NColor DARK_GREEN = NColorBase._reg("DarkGreen", "Green", 0, 95, 0);
    NColor DEEP_SKY_BLUE = NColorBase._reg("DeepSkyBlue", "DeepSkyBlue", 0, 95, 95);
    NColor DEEP_SKY_BLUE_2 = NColorBase._reg("DeepSkyBlue2", "DeepSkyBlue", 0, 95, 135);
    NColor DEEP_SKY_BLUE_3 = NColorBase._reg("DeepSkyBlue3", "DeepSkyBlue", 0, 95, 175);
    NColor DODGER_BLUE = NColorBase._reg("DodgerBlue", "DodgerBlue", 0, 95, 215);
    NColor DODGER_BLUE_2 = NColorBase._reg("DodgerBlue2", "DodgerBlue", 0, 95, 255);
    NColor GREEN_2 = NColorBase._reg("Green2", "Green", 0, 135, 0);
    NColor SPRING_GREEN = NColorBase._reg("SpringGreen", "SpringGreen", 0, 135, 95);
    NColor LIGHT_SPRING_GREEN = NColorBase._reg("LightSpringGreen", "SpringGreen", 139, 231, 185);
    NColor DARK_SPRING_GREEN = NColorBase._reg("DarkSpringGreen", "SpringGreen", 23, 114, 69);
    NColor TURQUOISE = NColorBase._reg("Turquoise", "Turquoise", 0, 135, 135);
    NColor DEEP_SKY_BLUE_4 = NColorBase._reg("DeepSkyBlue4", "DeepSkyBlue", 0, 135, 175);
    NColor DEEP_SKY_BLUE_5 = NColorBase._reg("DeepSkyBlue5", "DeepSkyBlue", 0, 135, 215);
    NColor DODGER_BLUE_3 = NColorBase._reg("DodgerBlue3", "DodgerBlue", 0, 135, 255);
    NColor GREEN_3 = NColorBase._reg("Green3", "Green", 0, 175, 0);
    NColor SPRING_GREEN_2 = NColorBase._reg("SpringGreen2", "SpringGreen", 0, 175, 95);
    NColor DARK_CYAN = NColorBase._reg("DarkCyan", "Cyan", 0, 175, 135);
    NColor LIGHT_SEA_GREEN = NColorBase._reg("LightSeaGreen", "SeaGreen", 0, 175, 175);
    NColor DEEP_SKY_BLUE_6 = NColorBase._reg("DeepSkyBlue6", "DeepSkyBlue", 0, 175, 215);
    NColor DEEP_SKY_BLUE_7 = NColorBase._reg("DeepSkyBlue7", "DeepSkyBlue", 0, 175, 255);
    NColor GREEN_4 = NColorBase._reg("Green4", "Green", 0, 215, 0);
    NColor SPRING_GREEN_3 = NColorBase._reg("SpringGreen3", "SpringGreen", 0, 215, 95);
    NColor SPRING_GREEN_4 = NColorBase._reg("SpringGreen4", "SpringGreen", 0, 215, 135);
    NColor CYAN = NColorBase._reg("Cyan", "Cyan", 0, 215, 175);
    NColor DARK_TURQUOISE = NColorBase._reg("DarkTurquoise", "Turquoise", 0, 215, 215);
    NColor LIGHT_TURQUOISE = NColorBase._reg("LightTurquoise", "Turquoise", 175, 228, 222);
    NColor TURQUOISE_2 = NColorBase._reg("Turquoise2", "Turquoise", 0, 215, 255);
    NColor GREEN_5 = NColorBase._reg("Green5", "Green", 0, 255, 0);
    NColor SPRING_GREEN_5 = NColorBase._reg("SpringGreen5", "SpringGreen", 0, 255, 95);
    NColor SPRING_GREEN_6 = NColorBase._reg("SpringGreen6", "SpringGreen", 0, 255, 135);
    NColor MEDIUM_SPRING_GREEN = NColorBase._reg("MediumSpringGreen", "SpringGreen", 0, 255, 175);
    NColor CYAN_2 = NColorBase._reg("Cyan2", "Cyan", 0, 255, 215);
    NColor CYAN_3 = NColorBase._reg("Cyan3", "Cyan", 0, 255, 255);
    NColor DARK_RED = NColorBase._reg("DarkRed", "Red", 95, 0, 0);
    NColor LIGHT_RED = NColorBase._reg("LightRed", "Red", 255, 114, 118);
    NColor CERISE = NColorBase._reg("Cerise", "Cerise", 223, 70, 97);
    NColor STRAWBERRY = NColorBase._reg("Strawberry", "Strawberry", 197, 70, 68);
    NColor MANGO = NColorBase._reg("Mango", "Mango", 183, 94, 74);
    NColor SCARLET = NColorBase._reg("Scarlet", "Scarlet", 255, 36, 0);
    NColor BEIGE = NColorBase._reg("Beige", "Beige", 245, 245, 220);
    NColor BRICK = NColorBase._reg("Brick", "Brick", 188, 74, 60);
    NColor DEEP_PINK = NColorBase._reg("DeepPink", "DeepPink", 95, 0, 95);
    NColor PURPLE_2 = NColorBase._reg("Purple2", "Purple", 95, 0, 135);
    NColor PURPLE_3 = NColorBase._reg("Purple3", "Purple", 95, 0, 175);
    NColor PURPLE_4 = NColorBase._reg("Purple4", "Purple", 95, 0, 215);
    NColor BLUE_VIOLET = NColorBase._reg("BlueViolet", "BlueViolet", 95, 0, 255);
    NColor ORANGE = NColorBase._reg("Orange", "Orange", 255, 165, 0);
    NColor LIGHT_ORANGE = NColorBase._reg("LightOrange", "Orange", 250, 181, 127);
    NColor MEDIUM_PURPLE = NColorBase._reg("MediumPurple", "Purple", 95, 95, 135);
    NColor DARK_SLATE_BLUE = NColorBase._reg("DarkSlateBlue", "SlateBlue", 72, 61, 139);
    NColor SLATE_BLUE = NColorBase._reg("SlateBlue", "SlateBlue", 95, 95, 175);
    NColor SLATE_BLUE_2 = NColorBase._reg("SlateBlue2", "SlateBlue", 95, 95, 215);
    NColor ROYAL_BLUE = NColorBase._reg("RoyalBlue", "RoyalBlue", 95, 95, 255);
    NColor CHARTREUSE = NColorBase._reg("Chartreuse", "Chartreuse", 95, 135, 0);
    NColor DARK_SEA_GREEN = NColorBase._reg("DarkSeaGreen", "SeaGreen", 95, 135, 95);
    NColor PALE_TURQUOISE = NColorBase._reg("PaleTurquoise", "PaleTurquoise", 95, 135, 135);
    NColor DARK_STEEL_BLUE = NColorBase._reg("DarkSteelBlue", "SteelBlue", 41, 93, 138);
    NColor STEEL_BLUE = NColorBase._reg("SteelBlue", "SteelBlue", 95, 135, 175);
    NColor STEEL_BLUE_2 = NColorBase._reg("SteelBlue2", "SteelBlue", 95, 135, 215);
    NColor CORNFLOWER_BLUE = NColorBase._reg("CornflowerBlue", "CornflowerBlue", 95, 135, 255);
    NColor CHARTREUSE_2 = NColorBase._reg("Chartreuse2", "Chartreuse", 95, 175, 0);
    NColor DARK_SEA_GREEN_2 = NColorBase._reg("DarkSeaGreen2", "SeaGreen", 95, 175, 95);
    NColor CADET_BLUE = NColorBase._reg("CadetBlue", "CadetBlue", 95, 175, 135);
    NColor CADET_BLUE_2 = NColorBase._reg("CadetBlue2", "CadetBlue", 95, 175, 175);
    NColor SKY_BLUE = NColorBase._reg("SkyBlue", "Blue", 95, 175, 215);
    NColor STEEL_BLUE_3 = NColorBase._reg("SteelBlue3", "SteelBlue", 95, 175, 255);
    NColor CHARTREUSE_3 = NColorBase._reg("Chartreuse3", "Chartreuse", 95, 215, 0);
    NColor PALE_GREEN = NColorBase._reg("PaleGreen", "PaleGreen", 95, 215, 95);
    NColor SEA_GREEN = NColorBase._reg("SeaGreen", "SeaGreen", 95, 215, 135);
    NColor AQUAMARINE = NColorBase._reg("Aquamarine", "Aquamarine", 95, 215, 175);
    NColor MEDIUM_TURQUOISE = NColorBase._reg("MediumTurquoise", "Turquoise", 95, 215, 215);
    NColor STEEL_BLUE_4 = NColorBase._reg("SteelBlue4", "SteelBlue", 95, 215, 255);
    NColor CHARTREUSE_4 = NColorBase._reg("Chartreuse4", "Chartreuse", 95, 255, 0);
    NColor SEA_GREEN_2 = NColorBase._reg("SeaGreen2", "SeaGreen", 95, 255, 95);
    NColor SEA_GREEN_3 = NColorBase._reg("SeaGreen3", "SeaGreen", 95, 255, 135);
    NColor SEA_GREEN_4 = NColorBase._reg("SeaGreen4", "SeaGreen", 95, 255, 175);
    NColor AQUAMARINE_2 = NColorBase._reg("Aquamarine2", "Aquamarine", 95, 255, 215);
    NColor DARK_SLATE_GRAY = NColorBase._reg("DarkSlateGray", "SlateGray", 95, 255, 255);
    NColor DARK_RED_2 = NColorBase._reg("DarkRed2", "Red", 135, 0, 0);
    NColor DEEP_PINK_2 = NColorBase._reg("DeepPink2", "DeepPink", 135, 0, 95);
    NColor LIGHT_MAGENTA = NColorBase._reg("LightMagenta", "Magenta", 255, 119, 255);
    NColor DARK_MAGENTA = NColorBase._reg("DarkMagenta", "Magenta", 135, 0, 135);
    NColor DARK_MAGENTA_2 = NColorBase._reg("DarkMagenta2", "Magenta", 135, 0, 175);
    NColor DARK_VIOLET = NColorBase._reg("DarkViolet", "Violet", 135, 0, 215);
    NColor PURPLE_5 = NColorBase._reg("Purple5", "Purple", 135, 0, 255);
    NColor ORANGE_2 = NColorBase._reg("Orange2", "Orange", 135, 95, 0);
    NColor LIGHT_PINK = NColorBase._reg("LightPink", "Pink", 135, 95, 95);
    NColor PLUM = NColorBase._reg("Plum", "Plum", 135, 95, 135);
    NColor MEDIUM_PURPLE_2 = NColorBase._reg("MediumPurple2", "Purple", 135, 95, 175);
    NColor MEDIUM_PURPLE_3 = NColorBase._reg("MediumPurple3", "Purple", 135, 95, 215);
    NColor SLATE_BLUE_3 = NColorBase._reg("SlateBlue3", "SlateBlue", 135, 95, 255);
    NColor YELLOW_2 = NColorBase._reg("Yellow2", "Yellow", 135, 135, 0);
    NColor WHEAT = NColorBase._reg("Wheat", "Wheat", 135, 135, 95);
    NColor LIGHT_SLATE_GRAY = NColorBase._reg("LightSlateGray", "SlateGray", 135, 135, 175);
    NColor MEDIUM_PURPLE_4 = NColorBase._reg("MediumPurple4", "Purple", 135, 135, 215);
    NColor LIGHT_SLATE_BLUE = NColorBase._reg("LightSlateBlue", "SlateBlue", 135, 135, 255);
    NColor YELLOW_3 = NColorBase._reg("Yellow3", "Yellow", 135, 175, 0);
    NColor DARK_OLIVE_GREEN = NColorBase._reg("DarkOliveGreen", "OliveGreen", 135, 175, 95);
    NColor DARK_SEA_GREEN_3 = NColorBase._reg("DarkSeaGreen3", "SeaGreen", 135, 175, 135);
    NColor LIGHT_SKY_BLUE = NColorBase._reg("LightSkyBlue", "SkyBlue", 135, 175, 175);
    NColor LIGHT_SKY_BLUE_2 = NColorBase._reg("LightSkyBlue2", "SkyBlue", 135, 175, 215);
    NColor SKY_BLUE_2 = NColorBase._reg("SkyBlue2", "SkyBlue", 135, 175, 255);
    NColor CHARTREUSE_5 = NColorBase._reg("Chartreuse5", "Chartreuse", 135, 215, 0);
    NColor DARK_OLIVE_GREEN_2 = NColorBase._reg("DarkOliveGreen2", "OliveGreen", 135, 215, 95);
    NColor PALE_GREEN_2 = NColorBase._reg("PaleGreen2", "PaleGreen", 135, 215, 135);
    NColor DARK_SEA_GREEN_4 = NColorBase._reg("DarkSeaGreen4", "SeaGreen", 135, 215, 175);
    NColor DARK_SLATE_GRAY_2 = NColorBase._reg("DarkSlateGray2", "SlateGray", 135, 215, 215);
    NColor SKY_BLUE_3 = NColorBase._reg("SkyBlue3", "SkyBlue", 135, 215, 255);
    NColor CHARTREUSE_6 = NColorBase._reg("Chartreuse6", "Chartreuse", 135, 255, 0);
    NColor LIGHT_GREEN = NColorBase._reg("LightGreen", "Green", 135, 255, 95);
    NColor LIGHT_GREEN_2 = NColorBase._reg("LightGreen2", "Green", 135, 255, 135);
    NColor PALE_GREEN_3 = NColorBase._reg("PaleGreen3", "PaleGreen", 135, 255, 175);
    NColor AQUAMARINE_3 = NColorBase._reg("Aquamarine3", "Aquamarine", 135, 255, 215);
    NColor DARK_SLATE_GRAY_3 = NColorBase._reg("DarkSlateGray3", "SlateGray", 135, 255, 255);
    NColor RED_2 = NColorBase._reg("Red2", "Red", 175, 0, 0);
    NColor DEEP_PINK_3 = NColorBase._reg("DeepPink3", "DeepPink", 175, 0, 95);
    NColor MEDIUM_VIOLET_RED = NColorBase._reg("MediumVioletRed", "VioletRed", 175, 0, 135);
    NColor LIGHT_VIOLET = NColorBase._reg("LightViolet", "Violet", 207, 159, 255);
    NColor MAGENTA = NColorBase._reg("Magenta", "Magenta", 175, 0, 175);
    NColor DARK_VIOLET_2 = NColorBase._reg("DarkViolet2", "Violet", 175, 0, 215);
    NColor PURPLE_6 = NColorBase._reg("Purple6", "Purple", 175, 0, 255);
    NColor LIGHT_PURPLE = NColorBase._reg("LightPurple", "Purple", 203, 195, 227);
    NColor DARK_PURPLE = NColorBase._reg("DarkPurple", "Purple", 152, 29, 151);
    NColor DARK_ORANGE = NColorBase._reg("DarkOrange", "Orange", 175, 95, 0);
    NColor INDIAN_RED = NColorBase._reg("IndianRed", "IndianRed", 175, 95, 95);
    NColor HOT_PINK = NColorBase._reg("HotPink", "HotPink", 175, 95, 135);
    NColor LIGHT_ORCHID = NColorBase._reg("LightOrchid", "Orchid", 230, 168, 215);
    NColor DARK_ORCHID = NColorBase._reg("DarkOrchid", "Orchid", 153, 50, 204);
    NColor MEDIUM_ORCHID = NColorBase._reg("MediumOrchid", "Orchid", 175, 95, 175);
    NColor MEDIUM_ORCHID_2 = NColorBase._reg("MediumOrchid2", "Orchid", 175, 95, 215);
    NColor MEDIUM_PURPLE_5 = NColorBase._reg("MediumPurple5", "Purple", 175, 95, 255);
    NColor GOLDENROD = NColorBase._reg("Goldenrod", "Goldenrod", 218, 165, 32);
    NColor DARK_GOLDENROD = NColorBase._reg("DarkGoldenrod", "Goldenrod", 175, 135, 0);
    NColor LIGHT_SALMON = NColorBase._reg("LightSalmon", "Salmon", 175, 135, 95);
    NColor LIGHT_BROWN = NColorBase._reg("LightBrown", "Brown", 196, 164, 132);
    NColor BROWN = NColorBase._reg("Brown", "Brown", 150, 75, 0);
    NColor DARK_BROWN = NColorBase._reg("DarkBrown", "Brown", 101, 67, 33);
    NColor ROSY_BROWN = NColorBase._reg("RosyBrown", "RosyBrown", 175, 135, 135);
    NColor MEDIUM_PURPLE_6 = NColorBase._reg("MediumPurple6", "Purple", 175, 135, 215);
    NColor MEDIUM_PURPLE_7 = NColorBase._reg("MediumPurple7", "Purple", 175, 135, 255);
    NColor GOLD = NColorBase._reg("Gold", "Gold", 175, 175, 0);
    NColor LIGHT_KHAKI = NColorBase._reg("LightKhaki", "Khaki", 240, 230, 140);
    NColor DARK_KHAKI = NColorBase._reg("DarkKhaki", "Khaki", 175, 175, 95);
    NColor NAVAJO_WHITE = NColorBase._reg("NavajoWhite", "NavajoWhite", 175, 175, 135);
    NColor LIGHT_STEEL_BLUE = NColorBase._reg("LightSteelBlue", "SteelBlue", 175, 175, 215);
    NColor LIGHT_STEEL_BLUE_2 = NColorBase._reg("LightSteelBlue2", "SteelBlue", 175, 175, 255);
    NColor YELLOW_4 = NColorBase._reg("Yellow4", "Yellow", 175, 215, 0);
    NColor DARK_OLIVE_GREEN_3 = NColorBase._reg("DarkOliveGreen3", "OliveGreen", 175, 215, 95);
    NColor DARK_SEA_GREEN_5 = NColorBase._reg("DarkSeaGreen5", "SeaGreen", 175, 215, 135);
    NColor DARK_SEA_GREEN_6 = NColorBase._reg("DarkSeaGreen6", "SeaGreen", 175, 215, 175);
    NColor LIGHT_CYAN = NColorBase._reg("LightCyan", "Cyan", 175, 215, 215);
    NColor LIGHT_SKY_BLUE_3 = NColorBase._reg("LightSkyBlue3", "SkyBlue", 175, 215, 255);
    NColor GREEN_YELLOW = NColorBase._reg("GreenYellow", "GreenYellow", 175, 255, 0);
    NColor DARK_OLIVE_GREEN_4 = NColorBase._reg("DarkOliveGreen4", "OliveGreen", 175, 255, 95);
    NColor PALE_GREEN_4 = NColorBase._reg("PaleGreen4", "PaleGreen", 175, 255, 135);
    NColor DARK_SEA_GREEN_7 = NColorBase._reg("DarkSeaGreen7", "SeaGreen", 175, 255, 175);
    NColor DARK_SEA_GREEN_8 = NColorBase._reg("DarkSeaGreen8", "SeaGreen", 175, 255, 215);
    NColor PALE_TURQUOISE_2 = NColorBase._reg("PaleTurquoise2", "PaleTurquoise", 175, 255, 255);
    NColor RED_3 = NColorBase._reg("Red3", "Red", 215, 0, 0);
    NColor DEEP_PINK_4 = NColorBase._reg("DeepPink4", "DeepPink", 215, 0, 95);
    NColor DEEP_PINK_5 = NColorBase._reg("DeepPink5", "DeepPink", 215, 0, 135);
    NColor MAGENTA_2 = NColorBase._reg("Magenta2", "Magenta", 215, 0, 175);
    NColor MAGENTA_3 = NColorBase._reg("Magenta3", "Magenta", 215, 0, 215);
    NColor MAGENTA_4 = NColorBase._reg("Magenta4", "Magenta", 215, 0, 255);
    NColor DARK_ORANGE_2 = NColorBase._reg("DarkOrange2", "Orange", 215, 95, 0);
    NColor INDIAN_RED_2 = NColorBase._reg("IndianRed2", "IndianRed", 215, 95, 95);
    NColor HOT_PINK_2 = NColorBase._reg("HotPink2", "HotPink", 215, 95, 135);
    NColor HOT_PINK_3 = NColorBase._reg("HotPink3", "HotPink", 215, 95, 175);
    NColor ORCHID = NColorBase._reg("Orchid", "Orchid", 215, 95, 215);
    NColor MEDIUM_ORCHID_3 = NColorBase._reg("MediumOrchid3", "Orchid", 215, 95, 255);
    NColor ORANGE_3 = NColorBase._reg("Orange3", "Orange", 215, 135, 0);
    NColor DARK_SALMON = NColorBase._reg("DarkSalmon", "Salmon", 233, 150, 122);
    NColor LIGHT_SALMON_2 = NColorBase._reg("LightSalmon2", "Salmon", 215, 135, 95);
    NColor LIGHT_PINK_2 = NColorBase._reg("LightPink2", "Pink", 215, 135, 135);
    NColor DARK_PINK = NColorBase._reg("DarkPink", "Pink", 231, 84, 128);
    NColor PINK = NColorBase._reg("Pink", "Pink", 215, 135, 175);
    NColor PLUM_2 = NColorBase._reg("Plum2", "Plum", 215, 135, 215);
    NColor VIOLET = NColorBase._reg("Violet", "Violet", 215, 135, 255);
    NColor GOLD_2 = NColorBase._reg("Gold2", "Gold", 215, 175, 0);
    NColor LIGHT_GOLDENROD = NColorBase._reg("LightGoldenrod", "Goldenrod", 215, 175, 95);
    NColor TAN = NColorBase._reg("Tan", "Tan", 215, 175, 135);
    NColor LIGHT_TAN = NColorBase._reg("LightTan", "Tan", 236, 222, 201);
    NColor DARK_TAN = NColorBase._reg("DarkTan", "Tan", 145, 129, 81);
    NColor TUSCAN_TAN = NColorBase._reg("TuscanTan", "TuscanTan", 166, 123, 91);
    NColor ALMOND = NColorBase._reg("Almond", "Almond", 239, 222, 205);
    NColor BONE = NColorBase._reg("Bone", "Bone", 227, 218, 201);
    NColor BISCUIT = NColorBase._reg("Biscuit", "Biscuit", 239, 204, 162);
    NColor BRANDY = NColorBase._reg("Brandy", "Brandy", 218, 188, 148);
    NColor CALICO = NColorBase._reg("Calico", "Calico", 224, 141, 91);
    NColor CAMEL = NColorBase._reg("Camel", "Camel", 193, 154, 107);
    NColor CAMEO = NColorBase._reg("Cameo", "Cameo", 238, 215, 185);
    NColor CARAMEL = NColorBase._reg("Caramel", "Caramel", 255, 213, 154);
    NColor CASHMERE = NColorBase._reg("Cashmere", "Cashmere", 230, 200, 160);
    NColor CREAM = NColorBase._reg("Cream", "Cream", 255, 253, 208);
    NColor CHALKY = NColorBase._reg("Chalky", "Chalky", 239, 201, 144);
    NColor DEER = NColorBase._reg("Deer", "Deer", 186, 135, 89);
    NColor DESERT = NColorBase._reg("Desert", "Desert", 250, 213, 165);
    NColor DIRT = NColorBase._reg("Dirt", "Dirt", 155, 118, 83);
    NColor EQUATOR = NColorBase._reg("Equator", "Equator", 227, 197, 101);
    NColor MISTY_ROSE = NColorBase._reg("MistyRose", "Rose", 215, 175, 175);
    NColor THISTLE = NColorBase._reg("Thistle", "Thistle", 215, 175, 215);
    NColor PLUM_3 = NColorBase._reg("Plum3", "Plum", 215, 175, 255);
    NColor YELLOW_5 = NColorBase._reg("Yellow5", "Yellow", 215, 215, 0);
    NColor KHAKI = NColorBase._reg("Khaki", "Khaki", 215, 215, 95);
    NColor LIGHT_GOLDENROD_2 = NColorBase._reg("LightGoldenrod2", "Goldenrod", 215, 215, 135);
    NColor LIGHT_YELLOW = NColorBase._reg("LightYellow", "Yellow", 215, 215, 175);
    NColor LIGHT_STEEL_BLUE_3 = NColorBase._reg("LightSteelBlue3", "SteelBlue", 215, 215, 255);
    NColor YELLOW_6 = NColorBase._reg("Yellow6", "Yellow", 215, 255, 0);
    NColor DARK_OLIVE_GREEN_5 = NColorBase._reg("DarkOliveGreen5", "OliveGreen", 215, 255, 95);
    NColor DARK_OLIVE_GREEN_6 = NColorBase._reg("DarkOliveGreen6", "OliveGreen", 215, 255, 135);
    NColor DARK_SEA_GREEN_9 = NColorBase._reg("DarkSeaGreen9", "SeaGreen", 215, 255, 175);
    NColor HONEY = NColorBase._reg("Honey", "Honey", 224, 172, 105);
    NColor HONEYDEW = NColorBase._reg("Honeydew", "Honeydew", 215, 255, 215);
    NColor HUSK = NColorBase._reg("Husk", "Husk", 189, 165, 93);
    NColor IVORY = NColorBase._reg("Ivory", "Ivory", 255, 255, 240);
    NColor LIGHT_CYAN_2 = NColorBase._reg("LightCyan2", "Cyan", 215, 255, 255);
    NColor RED_4 = NColorBase._reg("Red4", "Red", 255, 0, 0);
    NColor DEEP_PINK_6 = NColorBase._reg("DeepPink6", "DeepPink", 255, 0, 95);
    NColor DEEP_PINK_7 = NColorBase._reg("DeepPink7", "DeepPink", 255, 0, 135);
    NColor DEEP_PINK_8 = NColorBase._reg("DeepPink8", "DeepPink", 255, 0, 175);
    NColor MAGENTA_5 = NColorBase._reg("Magenta5", "Magenta", 255, 0, 215);
    NColor MAGENTA_6 = NColorBase._reg("Magenta6", "Magenta", 255, 0, 255);
    NColor ORANGE_RED = NColorBase._reg("OrangeRed", "OrangeRed", 255, 95, 0);
    NColor INDIAN_RED_3 = NColorBase._reg("IndianRed3", "IndianRed", 255, 95, 95);
    NColor INDIAN_RED_4 = NColorBase._reg("IndianRed4", "IndianRed", 255, 95, 135);
    NColor HOT_PINK_4 = NColorBase._reg("HotPink4", "HotPink", 255, 95, 175);
    NColor HOT_PINK_5 = NColorBase._reg("HotPink5", "HotPink", 255, 95, 215);
    NColor MEDIUM_ORCHID_4 = NColorBase._reg("MediumOrchid4", "Orchid", 255, 95, 255);
    NColor DARK_ORANGE_3 = NColorBase._reg("DarkOrange3", "Orange", 255, 135, 0);
    NColor SALMON = NColorBase._reg("Salmon", "Salmon", 255, 135, 95);
    NColor LIGHT_CORAL = NColorBase._reg("LightCoral", "Coral", 255, 135, 135);
    NColor PALE_VIOLET_RED = NColorBase._reg("PaleVioletRed", "VioletRed", 255, 135, 175);
    NColor ORCHID_2 = NColorBase._reg("Orchid2", "Orchid", 255, 135, 215);
    NColor ORCHID_3 = NColorBase._reg("Orchid3", "Orchid", 255, 135, 255);
    NColor ORANGE_4 = NColorBase._reg("Orange4", "Orange", 255, 175, 0);
    NColor SAND = NColorBase._reg("Sand", "Sand", 194, 178, 128);
    NColor SANDY_BROWN = NColorBase._reg("SandyBrown", "SandyBrown", 255, 175, 95);
    NColor LIGHT_SALMON_3 = NColorBase._reg("LightSalmon3", "Salmon", 255, 175, 135);
    NColor LIGHT_PINK_3 = NColorBase._reg("LightPink3", "Pink", 255, 175, 175);
    NColor PINK_2 = NColorBase._reg("Pink2", "Pink", 255, 175, 215);
    NColor PLUM_4 = NColorBase._reg("Plum4", "Plum", 255, 175, 255);
    NColor GOLD_3 = NColorBase._reg("Gold3", "Gold", 255, 215, 0);
    NColor LIGHT_GOLDENROD_3 = NColorBase._reg("LightGoldenrod3", "Goldenrod", 255, 215, 95);
    NColor LIGHT_GOLDENROD_4 = NColorBase._reg("LightGoldenrod4", "Goldenrod", 255, 215, 135);
    NColor NAVAJO_WHITE_2 = NColorBase._reg("NavajoWhite2", "NavajoWhite", 255, 215, 175);
    NColor MISTY_ROSE_2 = NColorBase._reg("MistyRose2", "MistyRose", 255, 215, 215);
    NColor THISTLE_2 = NColorBase._reg("Thistle2", "Thistle", 255, 215, 255);
    NColor YELLOW_7 = NColorBase._reg("Yellow7", "Yellow", 255, 255, 0);
    NColor LIGHT_GOLDENROD_5 = NColorBase._reg("LightGoldenrod5", "Goldenrod", 255, 255, 95);
    NColor KHAKI_2 = NColorBase._reg("Khaki2", "Khaki", 255, 255, 135);
    NColor WHEAT_2 = NColorBase._reg("Wheat2", "Wheat", 255, 255, 175);
    NColor CORNSILK = NColorBase._reg("Cornsilk", "Cornsilk", 255, 255, 215);
    NColor TUMBLEWEED = NColorBase._reg("Tumbleweed", "Tumbleweed", 220, 173, 141);
    NColor TACHA = NColorBase._reg("Tacha", "Tacha", 214, 183, 90);
    NColor SHADOW = NColorBase._reg("Shadow", "Shadow", 138, 121, 93);
    NColor MOCASSIN = NColorBase._reg("Moccasin", "Moccasin", 255, 228, 181);
    NColor COPPER = NColorBase._reg("Copper", "Copper", 0xb87333);
    NColor RAW_COPPER = NColorBase._reg("RawCopper", "Copper", 0xc46b51);
    NColor VINTAGE_COPPER = NColorBase._reg("VintageCopper", "Copper", 0x9d5f46);
    NColor BRIGHT_COPPER = NColorBase._reg("BrightCopper", "Copper", 0xc7561e);
    NColor DARK_COPPER = NColorBase._reg("DarkCopper", "Copper", 0x77422c);
    NColor BURNT_COPPER = NColorBase._reg("BurntCopper", "Copper", 0x982e01);

    NColor GRAY = NColorBase._reg("Gray", "Gray", 128, 128, 128);
    NColor GRAY_0 = NColorBase._regGray(0);
    NColor GRAY_1 = NColorBase._regGray(1);
    NColor GRAY_2 = NColorBase._regGray(2);
    NColor GRAY_3 = NColorBase._reg("Gray3", "Gray", 8, 8, 8);
    NColor GRAY_4 = NColorBase._regGray(4);
    NColor GRAY_5 = NColorBase._regGray(5);
    NColor GRAY_6 = NColorBase._regGray(6);
    NColor GRAY_7 = NColorBase._reg("Gray7", "Gray", 18, 18, 18);
    NColor GRAY_8 = NColorBase._regGray(8);
    NColor GRAY_9 = NColorBase._regGray(9);
    NColor GRAY_10 = NColorBase._regGray(10);
    NColor GRAY_11 = NColorBase._reg("Gray11", "Gray", 28, 28, 28);
    NColor GRAY_12 = NColorBase._regGray(12);
    NColor GRAY_13 = NColorBase._regGray(13);
    NColor GRAY_14 = NColorBase._regGray(14);
    NColor GRAY_15 = NColorBase._reg("Gray15", "Gray", 38, 38, 38);
    NColor GRAY_16 = NColorBase._regGray(16);
    NColor GRAY_17 = NColorBase._regGray(17);
    NColor GRAY_18 = NColorBase._regGray(18);
    NColor GRAY_19 = NColorBase._reg("Gray19", "Gray", 48, 48, 48);
    NColor GRAY_20 = NColorBase._regGray(20);
    NColor GRAY_21 = NColorBase._regGray(21);
    NColor GRAY_22 = NColorBase._regGray(22);
    NColor GRAY_23 = NColorBase._reg("Gray23", "Gray", 58, 58, 58);
    NColor GRAY_24 = NColorBase._regGray(24);
    NColor GRAY_25 = NColorBase._regGray(25);
    NColor GRAY_26 = NColorBase._regGray(26);
    NColor GRAY_27 = NColorBase._reg("Gray27", "Gray", 68, 68, 68);
    NColor GRAY_28 = NColorBase._regGray(28);
    NColor GRAY_29 = NColorBase._regGray(29);
    NColor GRAY_30 = NColorBase._reg("Gray30", "Gray", 78, 78, 78);
    NColor GRAY_31 = NColorBase._regGray(31);
    NColor GRAY_32 = NColorBase._regGray(32);
    NColor GRAY_33 = NColorBase._regGray(33);
    NColor GRAY_34 = NColorBase._regGray(34);
    NColor GRAY_35 = NColorBase._reg("Gray35", "Gray", 88, 88, 88);
    NColor GRAY_36 = NColorBase._regGray(36);
    NColor GRAY_37 = NColorBase._reg("Gray37", "Gray", 95, 95, 95);
    NColor GRAY_38 = NColorBase._regGray(38);
    NColor GRAY_39 = NColorBase._reg("Gray39", "Gray", 98, 98, 98);
    NColor GRAY_40 = NColorBase._regGray(40);
    NColor GRAY_41 = NColorBase._regGray(41);
    NColor GRAY_42 = NColorBase._reg("Gray42", "Gray", 108, 108, 108);
    NColor GRAY_43 = NColorBase._regGray(43);
    NColor GRAY_44 = NColorBase._regGray(44);
    NColor GRAY_45 = NColorBase._regGray(45);
    NColor GRAY_46 = NColorBase._reg("Gray46", "Gray", 118, 118, 118);
    NColor GRAY_47 = NColorBase._regGray(47);
    NColor GRAY_48 = NColorBase._regGray(48);
    NColor GRAY_49 = NColorBase._regGray(49);
    NColor GRAY_50 = NColorBase._reg("Gray50", "Gray", 128, 128, 128);
    NColor GRAY_51 = NColorBase._regGray(51);
    NColor GRAY_52 = NColorBase._regGray(52);
    NColor GRAY_53 = NColorBase._reg("Gray53", "Gray", 135, 135, 135);
    NColor GRAY_54 = NColorBase._reg("Gray54", "Gray", 138, 138, 138);
    NColor GRAY_55 = NColorBase._regGray(55);
    NColor GRAY_56 = NColorBase._regGray(56);
    NColor GRAY_57 = NColorBase._regGray(57);
    NColor GRAY_58 = NColorBase._reg("Gray58", "Gray", 148, 148, 148);
    NColor GRAY_59 = NColorBase._regGray(59);
    NColor GRAY_60 = NColorBase._regGray(60);
    NColor GRAY_61 = NColorBase._regGray(61);
    NColor GRAY_62 = NColorBase._reg("Gray62", "Gray", 158, 158, 158);
    NColor GRAY_63 = NColorBase._reg("Gray63", "Gray", 175, 135, 175);
    NColor GRAY_64 = NColorBase._regGray(64);
    NColor GRAY_65 = NColorBase._regGray(65);
    NColor GRAY_66 = NColorBase._reg("Gray66", "Gray", 168, 168, 168);
    NColor GRAY_67 = NColorBase._regGray(67);
    NColor GRAY_68 = NColorBase._regGray(68);
    NColor GRAY_69 = NColorBase._reg("Gray69", "Gray", 175, 175, 175);
    NColor GRAY_70 = NColorBase._reg("Gray70", "Gray", 178, 178, 178);
    NColor GRAY_71 = NColorBase._regGray(71);
    NColor GRAY_72 = NColorBase._regGray(72);
    NColor GRAY_73 = NColorBase._regGray(73);
    NColor GRAY_74 = NColorBase._reg("Gray74", "Gray", 188, 188, 188);
    NColor GRAY_75 = NColorBase._regGray(75);
    NColor GRAY_76 = NColorBase._regGray(76);
    NColor GRAY_77 = NColorBase._regGray(77);
    NColor GRAY_78 = NColorBase._reg("Gray78", "Gray", 198, 198, 198);
    NColor GRAY_79 = NColorBase._regGray(79);
    NColor GRAY_80 = NColorBase._regGray(80);
    NColor GRAY_81 = NColorBase._regGray(81);
    NColor GRAY_82 = NColorBase._reg("Gray82", "Gray", 208, 208, 208);
    NColor GRAY_83 = NColorBase._regGray(83);
    NColor GRAY_84 = NColorBase._reg("Gray84", "Gray", 215, 215, 215);
    NColor GRAY_85 = NColorBase._reg("Gray85", "Gray", 218, 218, 218);
    NColor GRAY_86 = NColorBase._regGray(86);
    NColor GRAY_87 = NColorBase._regGray(87);
    NColor GRAY_88 = NColorBase._regGray(88);
    NColor GRAY_89 = NColorBase._reg("Gray89", "Gray", 228, 228, 228);
    NColor GRAY_90 = NColorBase._regGray(90);
    NColor GRAY_91 = NColorBase._regGray(91);
    NColor GRAY_92 = NColorBase._regGray(92);
    NColor GRAY_93 = NColorBase._reg("Gray93", "Gray", 238, 238, 238);
    NColor GRAY_94 = NColorBase._regGray(94);
    NColor GRAY_95 = NColorBase._regGray(95);
    NColor GRAY_96 = NColorBase._regGray(96);
    NColor GRAY_97 = NColorBase._regGray(97);
    NColor GRAY_98 = NColorBase._regGray(98);
    NColor GRAY_99 = NColorBase._regGray(99);
    NColor GRAY_100 = NColorBase._reg("Gray100", "Gray", 255, 255, 255);
    NColor LIGHT_GRAY = NColorBase._reg("LightGray", "Gray", 192, 192, 192);
    NColor DARK_GRAY = NColorBase._reg("DarkGray", "Gray", 64, 64, 64);
    List<NColor> ALL = NColorBase.ALL;
    List<NColor> ALL_CANONICAL = NColorBase.ALL_CANONICAL;
    Map<String, NColor> BY_NAME = NColorBase.BY_NAME;

    /**
     * ANSI COLORS (4 bits) as 32bits
     */
    java.util.List<NColor> ANSI_COLORS_16 = NColorBase.ANSI_COLORS_16;
    /**
     * ANSI COLORS (8 bits) as 32bits
     */
    List<NColor> ANSI_COLORS_256= NColorBase.ANSI_COLORS_16;



    /**
     * Deterministic mapping from int → NColor.
     * <p>
     * Two-step mapping ensures colors are evenly chosen across canonical colors,
     * so grays (which are more numerous) don’t dominate the distribution.
     */
    static NColor pickColor(int hashCode) {
        return NColorBase.pickColor(hashCode);
    }


    /**
     * Creates a new instance of of name.
     *
     * @param name name
     * @return of name result
     */
    static NOptional<NColor> ofName(String name) {
        return NColorBase.ofName(name);
    }

    /**
     * Creates a new instance of of canonical name.
     *
     * @param name name
     * @return of canonical name result
     */
    static NOptional<List<NColor>> ofCanonicalName(String name) {
        return NColorBase.ofCanonicalName(name);
    }

    /**
     * Converts to html hex.
     *
     * @param cl cl
     * @return to html hex result
     */
    static String toHtmlHex(NColor cl) {
        return NColorBase.toHtmlHex(cl);
    }

    /**
     * Ansi to color.
     *
     * @param index index
     * @return ansi to color result
     */
    static NColor ansiToColor(int index) {
        return NColorBase.ansiToColor(index);
    }

    /**
     * Creates a new instance of of4.
     *
     * @param color color
     * @return of4 result
     */
    static NColor of4(int color) {
        return NColorBase.of4(color);
    }

    /**
     * Creates a new instance of of4.
     *
     * @param color color
     * @param name name
     * @return of4 result
     */
    static NColor of4(int color, String name) {
        return NColorBase.of4(color,name);
    }

    /**
     * Creates a new instance of of8.
     *
     * @param color color
     * @return of8 result
     */
    static NColor of8(int color) {
        return NColorBase.of8(color);
    }

    /**
     * Creates a new instance of of8.
     *
     * @param color color
     * @param name name
     * @return of8 result
     */
    static NColor of8(int color, String name) {
        return NColorBase.of8(color,name);
    }

    /**
     * Creates a new instance of of16.
     *
     * @param color color
     * @return of16 result
     */
    static NColor of16(int color) {
        return NColorBase.of16(color);
    }

    /**
     * Creates a new instance of of16.
     *
     * @param color color
     * @param name name
     * @return of16 result
     */
    static NColor of16(int color, String name) {
        return NColorBase.of16(color,name);
    }

    /**
     * Creates a new instance of of24.
     *
     * @param color color
     * @return of24 result
     */
    static NColor of24(int color) {
        return NColorBase.of24(color);
    }
    /**
     * Creates a new instance of of24.
     *
     * @param color color
     * @param name name
     * @return of24 result
     */
    static NColor of24(int color, String name) {
        return NColorBase.of24(color,name);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param r r
     * @param g g
     * @param b b
     * @return of32 result
     */
    static NColor of32(int r, int g, int b) {
        return NColorBase.of32(r,g,b);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param r r
     * @param g g
     * @param b b
     * @param name name
     * @return of32 result
     */
    static NColor of32(int r, int g, int b, String name) {
        return NColorBase.of32(r,g,b,name);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param r r
     * @param g g
     * @param b b
     * @param a a
     * @return of32 result
     */
    static NColor of32(int r, int g, int b, int a) {
        return NColorBase.of32(r,g,b,a);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param r r
     * @param g g
     * @param b b
     * @param a a
     * @param name name
     * @return of32 result
     */
    static NColor of32(int r, int g, int b, int a, String name) {
        return NColorBase.of32(r,g,b,a,name);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param color color
     * @return of32 result
     */
    static NColor of32(int color) {
        return NColorBase.of32(color);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param color color
     * @param name name
     * @return of32 result
     */
    static NColor of32(int color, String name) {
        return NColorBase.of32(color,name);
    }

    /**
     * Creates a new instance of of64.
     *
     * @param color color
     * @return of64 result
     */
    static NColor of64(long color) {
        return NColorBase.of64(color);
    }

    /**
     * Creates a new instance of of64.
     *
     * @param color color
     * @param name name
     * @return of64 result
     */
    static NColor of64(long color, String name) {
        return NColorBase.of64(color,name);
    }

    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * Bits.
     *
     * @return bits result
     */
    NColorBits bits();

    /**
     * With name.
     *
     * @param name name
     * @return with name result
     */
    NColor withName(String name);

    /**
     * Int color.
     *
     * @return int color result
     */
    int intColor();

    /**
     * Long color.
     *
     * @return long color result
     */
    long longColor();

    /**
     * Converts to color32.
     *
     * @return to color32 result
     */
    NColor toColor32();

    /**
     * Rgb.
     *
     * @return rgb result
     */
    int rgb();

    /**
     * Red.
     *
     * @return red result
     */
    int red();

    /**
     * Green.
     *
     * @return green result
     */
    int green();

    /**
     * Blue.
     *
     * @return blue result
     */
    int blue();

    /**
     * Alpha.
     *
     * @return alpha result
     */
    int alpha();

}
