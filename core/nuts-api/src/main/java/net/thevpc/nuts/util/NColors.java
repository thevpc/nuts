package net.thevpc.nuts.util;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class NColors {
    private static final List<NNamedColor> _ALL_REGISTERED = new ArrayList<>();
    private static final List<NNamedColor> _ALL_CANONICAL = new ArrayList<>();
    private static final Map<String, NNamedColor> _ALL_BY_NAME = new LinkedHashMap<>();
    private static final Map<String, List<NNamedColor>> _ALL_BY_CANONICAL_NAME = new LinkedHashMap<>();

    public static final NNamedColor BLACK = _reg("Black", "Black", 0, 0, 0);
    public static final NNamedColor MAROON = _reg("Maroon", "Maroon", 128, 0, 0);
    public static final NNamedColor GREEN = _reg("Green", "Green", 0, 128, 0);
    public static final NNamedColor OLIVE = _reg("Olive", "Olive", 128, 128, 0);
    public static final NNamedColor NAVY = _reg("Navy", "Navy", 0, 0, 128);
    public static final NNamedColor PURPLE = _reg("Purple","Purple", 128, 0, 128);
    public static final NNamedColor TEAL = _reg("Teal", "Teal", 0, 128, 128);
    public static final NNamedColor SILVER = _reg("Silver","Silver", 192, 192, 192);
    public static final NNamedColor RED = _reg("Red", "Red", 255, 0, 0);
    public static final NNamedColor LIME = _reg("Lime", "Lime", 0, 255, 0);
    public static final NNamedColor YELLOW = _reg("Yellow", "Yellow", 255, 255, 0);
    public static final NNamedColor DARK_YELLOW = _reg("DarkYellow","Yellow", 186, 142, 35);
    public static final NNamedColor BLUE = _reg("Blue","Blue", 0, 0, 255);
    public static final NNamedColor FUCHSIA = _reg("Fuchsia", "Fuchsia", 255, 0, 255);
    public static final NNamedColor AQUA = _reg("Aqua", "Aqua", 0, 255, 255);
    public static final NNamedColor WHITE = _reg("White","White", 255, 255, 255);
    public static final NNamedColor NAVY_BLUE = _reg("NavyBlue","NavyBlue",0, 0, 95);
    public static final NNamedColor DARK_BLUE = _reg("DarkBlue","Blue", 0, 0, 135);
    public static final NNamedColor LIGHT_BLUE = _reg("LightBlue","Blue", 173, 216, 230);
    public static final NNamedColor BLUE_2 = _reg("Blue2","Blue", 0, 0, 175);
    public static final NNamedColor BLUE_3 = _reg("Blue3","Blue", 0, 0, 215);
    public static final NNamedColor BLUE_4 = _reg("Blue4","Blue", 0, 0, 255);
    public static final NNamedColor DARK_GREEN = _reg("DarkGreen","Green", 0, 95, 0);
    public static final NNamedColor DEEP_SKY_BLUE = _reg("DeepSkyBlue","DeepSkyBlue", 0, 95, 95);
    public static final NNamedColor DEEP_SKY_BLUE_2 = _reg("DeepSkyBlue2","DeepSkyBlue", 0, 95, 135);
    public static final NNamedColor DEEP_SKY_BLUE_3 = _reg("DeepSkyBlue3","DeepSkyBlue", 0, 95, 175);
    public static final NNamedColor DODGER_BLUE = _reg("DodgerBlue","DodgerBlue", 0, 95, 215);
    public static final NNamedColor DODGER_BLUE_2 = _reg("DodgerBlue2","DodgerBlue", 0, 95, 255);
    public static final NNamedColor GREEN_2 = _reg("Green2","Green", 0, 135, 0);
    public static final NNamedColor SPRING_GREEN = _reg("SpringGreen","SpringGreen", 0, 135, 95);
    public static final NNamedColor LIGHT_SPRING_GREEN = _reg("LightSpringGreen","SpringGreen", 139, 231, 185);
    public static final NNamedColor DARK_SPRING_GREEN = _reg("DarkSpringGreen","SpringGreen", 23, 114, 69);
    public static final NNamedColor TURQUOISE = _reg("Turquoise","Turquoise", 0, 135, 135);
    public static final NNamedColor DEEP_SKY_BLUE_4 = _reg("DeepSkyBlue4","DeepSkyBlue", 0, 135, 175);
    public static final NNamedColor DEEP_SKY_BLUE_5 = _reg("DeepSkyBlue5","DeepSkyBlue", 0, 135, 215);
    public static final NNamedColor DODGER_BLUE_3 = _reg("DodgerBlue3","DodgerBlue", 0, 135, 255);
    public static final NNamedColor GREEN_3 = _reg("Green3","Green", 0, 175, 0);
    public static final NNamedColor SPRING_GREEN_2 = _reg("SpringGreen2","SpringGreen", 0, 175, 95);
    public static final NNamedColor DARK_CYAN = _reg("DarkCyan","Cyan", 0, 175, 135);
    public static final NNamedColor LIGHT_SEA_GREEN = _reg("LightSeaGreen","SeaGreen", 0, 175, 175);
    public static final NNamedColor DEEP_SKY_BLUE_6 = _reg("DeepSkyBlue6","DeepSkyBlue", 0, 175, 215);
    public static final NNamedColor DEEP_SKY_BLUE_7 = _reg("DeepSkyBlue7","DeepSkyBlue", 0, 175, 255);
    public static final NNamedColor GREEN_4 = _reg("Green4","Green", 0, 215, 0);
    public static final NNamedColor SPRING_GREEN_3 = _reg("SpringGreen3","SpringGreen", 0, 215, 95);
    public static final NNamedColor SPRING_GREEN_4 = _reg("SpringGreen4","SpringGreen", 0, 215, 135);
    public static final NNamedColor CYAN = _reg("Cyan","Cyan", 0, 215, 175);
    public static final NNamedColor DARK_TURQUOISE = _reg("DarkTurquoise","Turquoise", 0, 215, 215);
    public static final NNamedColor LIGHT_TURQUOISE = _reg("LightTurquoise","Turquoise", 175, 228, 222);
    public static final NNamedColor TURQUOISE_2 = _reg("Turquoise2","Turquoise", 0, 215, 255);
    public static final NNamedColor GREEN_5 = _reg("Green5","Green", 0, 255, 0);
    public static final NNamedColor SPRING_GREEN_5 = _reg("SpringGreen5","SpringGreen", 0, 255, 95);
    public static final NNamedColor SPRING_GREEN_6 = _reg("SpringGreen6","SpringGreen", 0, 255, 135);
    public static final NNamedColor MEDIUM_SPRING_GREEN = _reg("MediumSpringGreen","SpringGreen", 0, 255, 175);
    public static final NNamedColor CYAN_2 = _reg("Cyan2","Cyan", 0, 255, 215);
    public static final NNamedColor CYAN_3 = _reg("Cyan3", "Cyan", 0, 255, 255);
    public static final NNamedColor DARK_RED = _reg("DarkRed","Red", 95, 0, 0);
    public static final NNamedColor LIGHT_RED = _reg("LightRed","Red", 255,114,118);
    public static final NNamedColor CERISE = _reg("Cerise","Cerise", 223,70,97);
    public static final NNamedColor STRAWBERRY = _reg("Strawberry","Strawberry", 197,70,68);
    public static final NNamedColor MANGO = _reg("Mango","Mango", 183,94,74);
    public static final NNamedColor SCARLET = _reg("Scarlet","Scarlet", 255, 36, 0);
    public static final NNamedColor BEIGE = _reg("Beige","Beige", 245, 245, 220);
    public static final NNamedColor BRICK = _reg("Brick","Brick", 188, 74, 60);
    public static final NNamedColor DEEP_PINK = _reg("DeepPink","DeepPink", 95, 0, 95);
    public static final NNamedColor PURPLE_2 = _reg("Purple2","Purple", 95, 0, 135);
    public static final NNamedColor PURPLE_3 = _reg("Purple3","Purple", 95, 0, 175);
    public static final NNamedColor PURPLE_4 = _reg("Purple4","Purple", 95, 0, 215);
    public static final NNamedColor BLUE_VIOLET = _reg("BlueViolet","BlueViolet", 95, 0, 255);
    public static final NNamedColor ORANGE = _reg("Orange","Orange", 95, 95, 0);
    public static final NNamedColor LIGHT_ORANGE = _reg("LightOrange","Orange", 252,210,153);
    public static final NNamedColor MEDIUM_PURPLE = _reg("MediumPurple","Purple", 95, 95, 135);
    public static final NNamedColor DARK_SLATE_BLUE = _reg("DarkSlateBlue","SlateBlue", 72,61,139);
    public static final NNamedColor SLATE_BLUE = _reg("SlateBlue","SlateBlue", 95, 95, 175);
    public static final NNamedColor SLATE_BLUE_2 = _reg("SlateBlue2","SlateBlue", 95, 95, 215);
    public static final NNamedColor ROYAL_BLUE = _reg("RoyalBlue","RoyalBlue", 95, 95, 255);
    public static final NNamedColor CHARTREUSE = _reg("Chartreuse","Chartreuse", 95, 135, 0);
    public static final NNamedColor DARK_SEA_GREEN = _reg("DarkSeaGreen","SeaGreen", 95, 135, 95);
    public static final NNamedColor PALE_TURQUOISE = _reg("PaleTurquoise","PaleTurquoise", 95, 135, 135);
    public static final NNamedColor DARK_STEEL_BLUE = _reg("DarkSteelBlue","SteelBlue", 41, 93, 138);
    public static final NNamedColor STEEL_BLUE = _reg("SteelBlue","SteelBlue", 95, 135, 175);
    public static final NNamedColor STEEL_BLUE_2 = _reg("SteelBlue2","SteelBlue", 95, 135, 215);
    public static final NNamedColor CORNFLOWER_BLUE = _reg("CornflowerBlue","CornflowerBlue", 95, 135, 255);
    public static final NNamedColor CHARTREUSE_2 = _reg("Chartreuse2","Chartreuse", 95, 175, 0);
    public static final NNamedColor DARK_SEA_GREEN_2 = _reg("DarkSeaGreen2","SeaGreen", 95, 175, 95);
    public static final NNamedColor CADET_BLUE = _reg("CadetBlue","CadetBlue", 95, 175, 135);
    public static final NNamedColor CADET_BLUE_2 = _reg("CadetBlue2","CadetBlue", 95, 175, 175);
    public static final NNamedColor SKY_BLUE = _reg("SkyBlue","Blue", 95, 175, 215);
    public static final NNamedColor STEEL_BLUE_3 = _reg("SteelBlue3","SteelBlue", 95, 175, 255);
    public static final NNamedColor CHARTREUSE_3 = _reg("Chartreuse3","Chartreuse", 95, 215, 0);
    public static final NNamedColor PALE_GREEN = _reg("PaleGreen","PaleGreen", 95, 215, 95);
    public static final NNamedColor SEA_GREEN = _reg("SeaGreen","SeaGreen", 95, 215, 135);
    public static final NNamedColor AQUAMARINE = _reg("Aquamarine","Aquamarine", 95, 215, 175);
    public static final NNamedColor MEDIUM_TURQUOISE = _reg("MediumTurquoise","Turquoise", 95, 215, 215);
    public static final NNamedColor STEEL_BLUE_4 = _reg("SteelBlue4","SteelBlue", 95, 215, 255);
    public static final NNamedColor CHARTREUSE_4 = _reg("Chartreuse4","Chartreuse", 95, 255, 0);
    public static final NNamedColor SEA_GREEN_2 = _reg("SeaGreen2","SeaGreen", 95, 255, 95);
    public static final NNamedColor SEA_GREEN_3 = _reg("SeaGreen3","SeaGreen", 95, 255, 135);
    public static final NNamedColor SEA_GREEN_4 = _reg("SeaGreen4","SeaGreen", 95, 255, 175);
    public static final NNamedColor AQUAMARINE_2 = _reg("Aquamarine2","Aquamarine", 95, 255, 215);
    public static final NNamedColor DARK_SLATE_GRAY = _reg("DarkSlateGray","SlateGray", 95, 255, 255);
    public static final NNamedColor DARK_RED_2 = _reg("DarkRed2","Red", 135, 0, 0);
    public static final NNamedColor DEEP_PINK_2 = _reg("DeepPink2","DeepPink", 135, 0, 95);
    public static final NNamedColor LIGHT_MAGENTA = _reg("LightMagenta","Magenta", 255, 119, 255);
    public static final NNamedColor DARK_MAGENTA = _reg("DarkMagenta","Magenta", 135, 0, 135);
    public static final NNamedColor DARK_MAGENTA_2 = _reg("DarkMagenta2","Magenta", 135, 0, 175);
    public static final NNamedColor DARK_VIOLET = _reg("DarkViolet","Violet", 135, 0, 215);
    public static final NNamedColor PURPLE_5 = _reg("Purple5","Purple", 135, 0, 255);
    public static final NNamedColor ORANGE_2 = _reg("Orange2", "Orange", 135, 95, 0);
    public static final NNamedColor LIGHT_PINK = _reg("LightPink","Pink", 135, 95, 95);
    public static final NNamedColor PLUM = _reg("Plum","Plum", 135, 95, 135);
    public static final NNamedColor MEDIUM_PURPLE_2 = _reg("MediumPurple2","Purple", 135, 95, 175);
    public static final NNamedColor MEDIUM_PURPLE_3 = _reg("MediumPurple3","Purple", 135, 95, 215);
    public static final NNamedColor SLATE_BLUE_3 = _reg("SlateBlue3","SlateBlue", 135, 95, 255);
    public static final NNamedColor YELLOW_2 = _reg("Yellow2","Yellow", 135, 135, 0);
    public static final NNamedColor WHEAT = _reg("Wheat","Wheat", 135, 135, 95);
    public static final NNamedColor LIGHT_SLATE_GRAY = _reg("LightSlateGray","SlateGray", 135, 135, 175);
    public static final NNamedColor MEDIUM_PURPLE_4 = _reg("MediumPurple4","Purple", 135, 135, 215);
    public static final NNamedColor LIGHT_SLATE_BLUE = _reg("LightSlateBlue","SlateBlue", 135, 135, 255);
    public static final NNamedColor YELLOW_3 = _reg("Yellow3","Yellow", 135, 175, 0);
    public static final NNamedColor DARK_OLIVE_GREEN = _reg("DarkOliveGreen","OliveGreen", 135, 175, 95);
    public static final NNamedColor DARK_SEA_GREEN_3 = _reg("DarkSeaGreen3","SeaGreen", 135, 175, 135);
    public static final NNamedColor LIGHT_SKY_BLUE = _reg("LightSkyBlue","SkyBlue", 135, 175, 175);
    public static final NNamedColor LIGHT_SKY_BLUE_2 = _reg("LightSkyBlue2","SkyBlue", 135, 175, 215);
    public static final NNamedColor SKY_BLUE_2 = _reg("SkyBlue2","SkyBlue", 135, 175, 255);
    public static final NNamedColor CHARTREUSE_5 = _reg("Chartreuse5","Chartreuse", 135, 215, 0);
    public static final NNamedColor DARK_OLIVE_GREEN_2 = _reg("DarkOliveGreen2","OliveGreen", 135, 215, 95);
    public static final NNamedColor PALE_GREEN_2 = _reg("PaleGreen2","PaleGreen", 135, 215, 135);
    public static final NNamedColor DARK_SEA_GREEN_4 = _reg("DarkSeaGreen4","SeaGreen", 135, 215, 175);
    public static final NNamedColor DARK_SLATE_GRAY_2 = _reg("DarkSlateGray2","SlateGray", 135, 215, 215);
    public static final NNamedColor SKY_BLUE_3 = _reg("SkyBlue3","SkyBlue", 135, 215, 255);
    public static final NNamedColor CHARTREUSE_6 = _reg("Chartreuse6","Chartreuse", 135, 255, 0);
    public static final NNamedColor LIGHT_GREEN = _reg("LightGreen","Green", 135, 255, 95);
    public static final NNamedColor LIGHT_GREEN_2 = _reg("LightGreen2","Green", 135, 255, 135);
    public static final NNamedColor PALE_GREEN_3 = _reg("PaleGreen3","PaleGreen", 135, 255, 175);
    public static final NNamedColor AQUAMARINE_3 = _reg("Aquamarine3","Aquamarine", 135, 255, 215);
    public static final NNamedColor DARK_SLATE_GRAY_3 = _reg("DarkSlateGray3","SlateGray", 135, 255, 255);
    public static final NNamedColor RED_2 = _reg("Red2","Red", 175, 0, 0);
    public static final NNamedColor DEEP_PINK_3 = _reg("DeepPink3","DeepPink", 175, 0, 95);
    public static final NNamedColor MEDIUM_VIOLET_RED = _reg("MediumVioletRed","VioletRed", 175, 0, 135);
    public static final NNamedColor LIGHT_VIOLET = _reg("LightViolet","Violet", 207, 159, 255);
    public static final NNamedColor MAGENTA = _reg("Magenta","Magenta", 175, 0, 175);
    public static final NNamedColor DARK_VIOLET_2 = _reg("DarkViolet2","Violet", 175, 0, 215);
    public static final NNamedColor PURPLE_6 = _reg("Purple6","Purple", 175, 0, 255);
    public static final NNamedColor LIGHT_PURPLE = _reg("LightPurple","Purple", 203, 195, 227);
    public static final NNamedColor DARK_PURPLE = _reg("DarkPurple","Purple", 152,29,151);
    public static final NNamedColor DARK_ORANGE = _reg("DarkOrange","Orange", 175, 95, 0);
    public static final NNamedColor INDIAN_RED = _reg("IndianRed","IndianRed", 175, 95, 95);
    public static final NNamedColor HOT_PINK = _reg("HotPink","HotPink", 175, 95, 135);
    public static final NNamedColor LIGHT_ORCHID = _reg("LightOrchid","Orchid", 230,168,215);
    public static final NNamedColor DARK_ORCHID = _reg("DarkOrchid","Orchid", 153, 50, 204);
    public static final NNamedColor MEDIUM_ORCHID = _reg("MediumOrchid","Orchid", 175, 95, 175);
    public static final NNamedColor MEDIUM_ORCHID_2 = _reg("MediumOrchid2","Orchid", 175, 95, 215);
    public static final NNamedColor MEDIUM_PURPLE_5 = _reg("MediumPurple5","Purple", 175, 95, 255);
    public static final NNamedColor GOLDENROD = _reg("Goldenrod","Goldenrod", 218,165,32);
    public static final NNamedColor DARK_GOLDENROD = _reg("DarkGoldenrod","Goldenrod", 175, 135, 0);
    public static final NNamedColor LIGHT_SALMON = _reg("LightSalmon","Salmon", 175, 135, 95);
    public static final NNamedColor LIGHT_BROWN = _reg("LightBrown","Brown", 196, 164, 132);
    public static final NNamedColor BROWN = _reg("Brown","Brown", 150, 75, 0);
    public static final NNamedColor DARK_BROWN = _reg("DarkBrown","Brown", 101, 67, 33);
    public static final NNamedColor ROSY_BROWN = _reg("RosyBrown","RosyBrown", 175, 135, 135);
    public static final NNamedColor MEDIUM_PURPLE_6 = _reg("MediumPurple6","Purple", 175, 135, 215);
    public static final NNamedColor MEDIUM_PURPLE_7 = _reg("MediumPurple7","Purple", 175, 135, 255);
    public static final NNamedColor GOLD = _reg("Gold", "Gold",175, 175, 0);
    public static final NNamedColor LIGHT_KHAKI = _reg("LightKhaki","Khaki", 240, 230, 140);
    public static final NNamedColor DARK_KHAKI = _reg("DarkKhaki","Khaki", 175, 175, 95);
    public static final NNamedColor NAVAJO_WHITE = _reg("NavajoWhite","NavajoWhite", 175, 175, 135);
    public static final NNamedColor LIGHT_STEEL_BLUE = _reg("LightSteelBlue","SteelBlue", 175, 175, 215);
    public static final NNamedColor LIGHT_STEEL_BLUE_2 = _reg("LightSteelBlue2","SteelBlue", 175, 175, 255);
    public static final NNamedColor YELLOW_4 = _reg("Yellow4","Yellow", 175, 215, 0);
    public static final NNamedColor DARK_OLIVE_GREEN_3 = _reg("DarkOliveGreen3","OliveGreen", 175, 215, 95);
    public static final NNamedColor DARK_SEA_GREEN_5 = _reg("DarkSeaGreen5", "SeaGreen", 175, 215, 135);
    public static final NNamedColor DARK_SEA_GREEN_6 = _reg("DarkSeaGreen6", "SeaGreen", 175, 215, 175);
    public static final NNamedColor LIGHT_CYAN = _reg("LightCyan","Cyan", 175, 215, 215);
    public static final NNamedColor LIGHT_SKY_BLUE_3 = _reg("LightSkyBlue3", "SkyBlue", 175, 215, 255);
    public static final NNamedColor GREEN_YELLOW = _reg("GreenYellow","GreenYellow", 175, 255, 0);
    public static final NNamedColor DARK_OLIVE_GREEN_4 = _reg("DarkOliveGreen4", "OliveGreen",175, 255, 95);
    public static final NNamedColor PALE_GREEN_4 = _reg("PaleGreen4", "PaleGreen", 175, 255, 135);
    public static final NNamedColor DARK_SEA_GREEN_7 = _reg("DarkSeaGreen7","SeaGreen", 175, 255, 175);
    public static final NNamedColor DARK_SEA_GREEN_8 = _reg("DarkSeaGreen8","SeaGreen", 175, 255, 215);
    public static final NNamedColor PALE_TURQUOISE_2 = _reg("PaleTurquoise2","PaleTurquoise", 175, 255, 255);
    public static final NNamedColor RED_3 = _reg("Red3","Red", 215, 0, 0);
    public static final NNamedColor DEEP_PINK_4 = _reg("DeepPink4","DeepPink", 215, 0, 95);
    public static final NNamedColor DEEP_PINK_5 = _reg("DeepPink5","DeepPink", 215, 0, 135);
    public static final NNamedColor MAGENTA_2 = _reg("Magenta2","Magenta", 215, 0, 175);
    public static final NNamedColor MAGENTA_3 = _reg("Magenta3","Magenta", 215, 0, 215);
    public static final NNamedColor MAGENTA_4 = _reg("Magenta4","Magenta", 215, 0, 255);
    public static final NNamedColor DARK_ORANGE_2 = _reg("DarkOrange2","Orange", 215, 95, 0);
    public static final NNamedColor INDIAN_RED_2 = _reg("IndianRed2","IndianRed", 215, 95, 95);
    public static final NNamedColor HOT_PINK_2 = _reg("HotPink2","HotPink", 215, 95, 135);
    public static final NNamedColor HOT_PINK_3 = _reg("HotPink3","HotPink", 215, 95, 175);
    public static final NNamedColor ORCHID = _reg("Orchid", "Orchid", 215, 95, 215);
    public static final NNamedColor MEDIUM_ORCHID_3 = _reg("MediumOrchid3","Orchid", 215, 95, 255);
    public static final NNamedColor ORANGE_3 = _reg("Orange3", "Orange", 215, 135, 0);
    public static final NNamedColor DARK_SALMON = _reg("DarkSalmon", "Salmon", 233, 150, 122);
    public static final NNamedColor LIGHT_SALMON_2 = _reg("LightSalmon2", "Salmon", 215, 135, 95);
    public static final NNamedColor LIGHT_PINK_2 = _reg("LightPink2", "Pink", 215, 135, 135);
    public static final NNamedColor DARK_PINK = _reg("DarkPink", "Pink", 231, 84, 128);
    public static final NNamedColor PINK = _reg("Pink", "Pink", 215, 135, 175);
    public static final NNamedColor PLUM_2 = _reg("Plum2", "Plum", 215, 135, 215);
    public static final NNamedColor VIOLET = _reg("Violet", "Violet",215, 135, 255);
    public static final NNamedColor GOLD_2 = _reg("Gold2", "Gold", 215, 175, 0);
    public static final NNamedColor LIGHT_GOLDENROD = _reg("LightGoldenrod","Goldenrod", 215, 175, 95);
    public static final NNamedColor TAN = _reg("Tan","Tan", 215, 175, 135);
    public static final NNamedColor LIGHT_TAN = _reg("LightTan","Tan", 236, 222, 201);
    public static final NNamedColor DARK_TAN = _reg("DarkTan","Tan", 145, 129, 81);
    public static final NNamedColor TUSCAN_TAN = _reg("TuscanTan","TuscanTan", 166, 123, 91);
    public static final NNamedColor ALMOND = _reg("Almond","Almond", 239, 222, 205);
    public static final NNamedColor BONE = _reg("Bone","Bone", 227, 218, 201);
    public static final NNamedColor BISCUIT = _reg("Biscuit","Biscuit", 239, 204, 162);
    public static final NNamedColor BRANDY = _reg("Brandy","Brandy", 218, 188, 148);
    public static final NNamedColor CALICO = _reg("Calico","Calico", 224, 141, 91);
    public static final NNamedColor CAMEL = _reg("Camel","Camel", 193, 154, 107);
    public static final NNamedColor CAMEO = _reg("Cameo","Cameo", 238, 215, 185);
    public static final NNamedColor CARAMEL = _reg("Caramel","Caramel", 255, 213, 154);
    public static final NNamedColor CASHMERE = _reg("Cashmere","Cashmere", 230, 200, 160);
    public static final NNamedColor CREAM = _reg("Cream","Cream", 255, 253, 208);
    public static final NNamedColor CHALKY = _reg("Chalky","Chalky", 239, 201, 144);
    public static final NNamedColor DEER = _reg("Deer","Deer", 186, 135, 89);
    public static final NNamedColor DESERT = _reg("Desert","Desert", 250, 213, 165);
    public static final NNamedColor DIRT = _reg("Dirt","Dirt", 155, 118, 83);
    public static final NNamedColor EQUATOR = _reg("Equator","Equator", 227, 197, 101);
    public static final NNamedColor MISTY_ROSE = _reg("MistyRose","Rose", 215, 175, 175);
    public static final NNamedColor THISTLE = _reg("Thistle", "Thistle", 215, 175, 215);
    public static final NNamedColor PLUM_3 = _reg("Plum3", "Plum", 215, 175, 255);
    public static final NNamedColor YELLOW_5 = _reg("Yellow5", "Yellow", 215, 215, 0);
    public static final NNamedColor KHAKI = _reg("Khaki", "Khaki", 215, 215, 95);
    public static final NNamedColor LIGHT_GOLDENROD_2 = _reg("LightGoldenrod2", "Goldenrod", 215, 215, 135);
    public static final NNamedColor LIGHT_YELLOW = _reg("LightYellow", "Yellow", 215, 215, 175);
    public static final NNamedColor LIGHT_STEEL_BLUE_3 = _reg("LightSteelBlue3", "SteelBlue", 215, 215, 255);
    public static final NNamedColor YELLOW_6 = _reg("Yellow6", "Yellow", 215, 255, 0);
    public static final NNamedColor DARK_OLIVE_GREEN_5 = _reg("DarkOliveGreen5","OliveGreen", 215, 255, 95);
    public static final NNamedColor DARK_OLIVE_GREEN_6 = _reg("DarkOliveGreen6","OliveGreen", 215, 255, 135);
    public static final NNamedColor DARK_SEA_GREEN_9 = _reg("DarkSeaGreen9","SeaGreen", 215, 255, 175);
    public static final NNamedColor HONEY = _reg("Honey", "Honey", 224, 172, 105);
    public static final NNamedColor HONEYDEW = _reg("Honeydew", "Honeydew", 215, 255, 215);
    public static final NNamedColor HUSK = _reg("Husk", "Husk", 189, 165, 93);
    public static final NNamedColor IVORY = _reg("Ivory", "Ivory", 255, 255, 240);
    public static final NNamedColor LIGHT_CYAN_2 = _reg("LightCyan2", "Cyan", 215, 255, 255);
    public static final NNamedColor RED_4 = _reg("Red4", "Red", 255, 0, 0);
    public static final NNamedColor DEEP_PINK_6 = _reg("DeepPink6", "DeepPink", 255, 0, 95);
    public static final NNamedColor DEEP_PINK_7 = _reg("DeepPink7", "DeepPink", 255, 0, 135);
    public static final NNamedColor DEEP_PINK_8 = _reg("DeepPink8", "DeepPink", 255, 0, 175);
    public static final NNamedColor MAGENTA_5 = _reg("Magenta5", "Magenta", 255, 0, 215);
    public static final NNamedColor MAGENTA_6 = _reg("Magenta6", "Magenta", 255, 0, 255);
    public static final NNamedColor ORANGE_RED = _reg("OrangeRed", "OrangeRed",255, 95, 0);
    public static final NNamedColor INDIAN_RED_3 = _reg("IndianRed3","IndianRed", 255, 95, 95);
    public static final NNamedColor INDIAN_RED_4 = _reg("IndianRed4","IndianRed", 255, 95, 135);
    public static final NNamedColor HOT_PINK_4 = _reg("HotPink4", "HotPink", 255, 95, 175);
    public static final NNamedColor HOT_PINK_5 = _reg("HotPink5", "HotPink", 255, 95, 215);
    public static final NNamedColor MEDIUM_ORCHID_4 = _reg("MediumOrchid4","Orchid", 255, 95, 255);
    public static final NNamedColor DARK_ORANGE_3 = _reg("DarkOrange3","Orange", 255, 135, 0);
    public static final NNamedColor SALMON = _reg("Salmon", "Salmon", 255, 135, 95);
    public static final NNamedColor LIGHT_CORAL = _reg("LightCoral", "Coral", 255, 135, 135);
    public static final NNamedColor PALE_VIOLET_RED = _reg("PaleVioletRed", "VioletRed",255, 135, 175);
    public static final NNamedColor ORCHID_2 = _reg("Orchid2","Orchid", 255, 135, 215);
    public static final NNamedColor ORCHID_3 = _reg("Orchid3","Orchid", 255, 135, 255);
    public static final NNamedColor ORANGE_4 = _reg("Orange4","Orange", 255, 175, 0);
    public static final NNamedColor SAND = _reg("Sand","Sand", 194, 178, 128);
    public static final NNamedColor SANDY_BROWN = _reg("SandyBrown","SandyBrown", 255, 175, 95);
    public static final NNamedColor LIGHT_SALMON_3 = _reg("LightSalmon3", "Salmon",255, 175, 135);
    public static final NNamedColor LIGHT_PINK_3 = _reg("LightPink3","Pink", 255, 175, 175);
    public static final NNamedColor PINK_2 = _reg("Pink2","Pink", 255, 175, 215);
    public static final NNamedColor PLUM_4 = _reg("Plum4","Plum", 255, 175, 255);
    public static final NNamedColor GOLD_3 = _reg("Gold3","Gold", 255, 215, 0);
    public static final NNamedColor LIGHT_GOLDENROD_3 = _reg("LightGoldenrod3","Goldenrod", 255, 215, 95);
    public static final NNamedColor LIGHT_GOLDENROD_4 = _reg("LightGoldenrod4","Goldenrod", 255, 215, 135);
    public static final NNamedColor NAVAJO_WHITE_2 = _reg("NavajoWhite2","NavajoWhite", 255, 215, 175);
    public static final NNamedColor MISTY_ROSE_2 = _reg("MistyRose2","MistyRose", 255, 215, 215);
    public static final NNamedColor THISTLE_2 = _reg("Thistle2","Thistle", 255, 215, 255);
    public static final NNamedColor YELLOW_7 = _reg("Yellow7","Yellow", 255, 255, 0);
    public static final NNamedColor LIGHT_GOLDENROD_5 = _reg("LightGoldenrod5","Goldenrod", 255, 255, 95);
    public static final NNamedColor KHAKI_2 = _reg("Khaki2","Khaki", 255, 255, 135);
    public static final NNamedColor WHEAT_2 = _reg("Wheat2","Wheat", 255, 255, 175);
    public static final NNamedColor CORNSILK = _reg("Cornsilk","Cornsilk", 255, 255, 215);
    public static final NNamedColor TUMBLEWEED = _reg("Tumbleweed","Tumbleweed", 220, 173, 141);
    public static final NNamedColor TACHA = _reg("Tacha","Tacha", 214, 183, 90);
    public static final NNamedColor SHADOW = _reg("Shadow","Shadow", 138, 121, 93);
    public static final NNamedColor MOCASSIN = _reg("Moccasin","Moccasin", 255, 228, 181);

    public static final NNamedColor GRAY = _reg("Gray", "Gray", Color.GRAY);
    public static final NNamedColor GRAY_0 = _regGray(0);
    public static final NNamedColor GRAY_1 = _regGray(1);
    public static final NNamedColor GRAY_2 = _regGray(2);
    public static final NNamedColor GRAY_3 = _reg("Gray3","Gray", 8, 8, 8);
    public static final NNamedColor GRAY_4 = _regGray(4);
    public static final NNamedColor GRAY_5 = _regGray(5);
    public static final NNamedColor GRAY_6 = _regGray(6);
    public static final NNamedColor GRAY_7 = _reg("Gray7","Gray", 18, 18, 18);
    public static final NNamedColor GRAY_8 = _regGray(8);
    public static final NNamedColor GRAY_9 = _regGray(9);
    public static final NNamedColor GRAY_10 = _regGray(10);
    public static final NNamedColor GRAY_11 = _reg("Gray11","Gray", 28, 28, 28);
    public static final NNamedColor GRAY_12 = _regGray(12);
    public static final NNamedColor GRAY_13 = _regGray(13);
    public static final NNamedColor GRAY_14 = _regGray(14);
    public static final NNamedColor GRAY_15 = _reg("Gray15","Gray", 38, 38, 38);
    public static final NNamedColor GRAY_16 = _regGray(16);
    public static final NNamedColor GRAY_17 = _regGray(17);
    public static final NNamedColor GRAY_18 = _regGray(18);
    public static final NNamedColor GRAY_19 = _reg("Gray19","Gray", 48, 48, 48);
    public static final NNamedColor GRAY_20 = _regGray(20);
    public static final NNamedColor GRAY_21 = _regGray(21);
    public static final NNamedColor GRAY_22 = _regGray(22);
    public static final NNamedColor GRAY_23 = _reg("Gray23","Gray", 58, 58, 58);
    public static final NNamedColor GRAY_24 = _regGray(24);
    public static final NNamedColor GRAY_25 = _regGray(25);
    public static final NNamedColor GRAY_26 = _regGray(26);
    public static final NNamedColor GRAY_27 = _reg("Gray27","Gray", 68, 68, 68);
    public static final NNamedColor GRAY_28 = _regGray(28);
    public static final NNamedColor GRAY_29 = _regGray(29);
    public static final NNamedColor GRAY_30 = _reg("Gray30","Gray", 78, 78, 78);
    public static final NNamedColor GRAY_31 = _regGray(31);
    public static final NNamedColor GRAY_32 = _regGray(32);
    public static final NNamedColor GRAY_33 = _regGray(33);
    public static final NNamedColor GRAY_34 = _regGray(34);
    public static final NNamedColor GRAY_35 = _reg("Gray35","Gray", 88, 88, 88);
    public static final NNamedColor GRAY_36 = _regGray(36);
    public static final NNamedColor GRAY_37 = _reg("Grey37","Gray", 95, 95, 95);
    public static final NNamedColor GRAY_38 = _regGray(38);
    public static final NNamedColor GRAY_39 = _reg("Gray39","Gray", 98, 98, 98);
    public static final NNamedColor GRAY_40 = _regGray(40);
    public static final NNamedColor GRAY_41 = _regGray(41);
    public static final NNamedColor GRAY_42 = _reg("Gray42","Gray", 108, 108, 108);
    public static final NNamedColor GRAY_43 = _regGray(43);
    public static final NNamedColor GRAY_44 = _regGray(44);
    public static final NNamedColor GRAY_45 = _regGray(45);
    public static final NNamedColor GRAY_46 = _reg("Gray46","Gray", 118, 118, 118);
    public static final NNamedColor GRAY_47 = _regGray(47);
    public static final NNamedColor GRAY_48 = _regGray(48);
    public static final NNamedColor GRAY_49 = _regGray(49);
    public static final NNamedColor GRAY_50 = _reg("Gray50","Gray", 128, 128, 128);
    public static final NNamedColor GRAY_51 = _regGray(51);
    public static final NNamedColor GRAY_52 = _regGray(52);
    public static final NNamedColor GRAY_53 = _reg("Grey53","Gray", 135, 135, 135);
    public static final NNamedColor GRAY_54 = _reg("Gray54","Gray", 138, 138, 138);
    public static final NNamedColor GRAY_55 = _regGray(55);
    public static final NNamedColor GRAY_56 = _regGray(56);
    public static final NNamedColor GRAY_57 = _regGray(57);
    public static final NNamedColor GRAY_58 = _reg("Gray58","Gray", 148, 148, 148);
    public static final NNamedColor GRAY_59 = _regGray(59);
    public static final NNamedColor GRAY_60 = _regGray(60);
    public static final NNamedColor GRAY_61 = _regGray(61);
    public static final NNamedColor GRAY_62 = _reg("Gray62","Gray", 158, 158, 158);
    public static final NNamedColor GRAY_63 = _reg("Grey63","Gray", 175, 135, 175);
    public static final NNamedColor GRAY_64 = _regGray(64);
    public static final NNamedColor GRAY_65 = _regGray(65);
    public static final NNamedColor GRAY_66 = _reg("Gray66","Gray", 168, 168, 168);
    public static final NNamedColor GRAY_67 = _regGray(67);
    public static final NNamedColor GRAY_68 = _regGray(68);
    public static final NNamedColor GRAY_69 = _reg("Grey69","Gray", 175, 175, 175);
    public static final NNamedColor GRAY_70 = _reg("Gray70","Gray", 178, 178, 178);
    public static final NNamedColor GRAY_71 = _regGray(71);
    public static final NNamedColor GRAY_72 = _regGray(72);
    public static final NNamedColor GRAY_73 = _regGray(73);
    public static final NNamedColor GRAY_74 = _reg("Gray74","Gray", 188, 188, 188);
    public static final NNamedColor GRAY_75 = _regGray(75);
    public static final NNamedColor GRAY_76 = _regGray(76);
    public static final NNamedColor GRAY_77 = _regGray(77);
    public static final NNamedColor GRAY_78 = _reg("Gray78","Gray", 198, 198, 198);
    public static final NNamedColor GRAY_79 = _regGray(79);
    public static final NNamedColor GRAY_80 = _regGray(80);
    public static final NNamedColor GRAY_81 = _regGray(81);
    public static final NNamedColor GRAY_82 = _reg("Gray82","Gray", 208, 208, 208);
    public static final NNamedColor GRAY_83 = _regGray(83);
    public static final NNamedColor GRAY_84 = _reg("Grey84","Gray", 215, 215, 215);
    public static final NNamedColor GRAY_85 = _reg("Gray85","Gray", 218, 218, 218);
    public static final NNamedColor GRAY_86 = _regGray(86);
    public static final NNamedColor GRAY_87 = _regGray(87);
    public static final NNamedColor GRAY_88 = _regGray(88);
    public static final NNamedColor GRAY_89 = _reg("Gray89","Gray", 228, 228, 228);
    public static final NNamedColor GRAY_90 = _regGray(90);
    public static final NNamedColor GRAY_91 = _regGray(91);
    public static final NNamedColor GRAY_92 = _regGray(92);
    public static final NNamedColor GRAY_93 = _reg("Gray93","Gray", 238, 238, 238);
    public static final NNamedColor GRAY_94 = _regGray(94);
    public static final NNamedColor GRAY_95 = _regGray(95);
    public static final NNamedColor GRAY_96 = _regGray(96);
    public static final NNamedColor GRAY_97 = _regGray(97);
    public static final NNamedColor GRAY_98 = _regGray(98);
    public static final NNamedColor GRAY_99 = _regGray(99);
    public static final NNamedColor GRAY_100 = _reg("Gray100","Gray", 255, 255, 255);
    public static final NNamedColor LIGHT_GRAY = _reg("LightGray", "Gray", Color.LIGHT_GRAY);
    public static final NNamedColor DARK_GRAY = _reg("DarkGray", "Gray", Color.DARK_GRAY);

//    static {
//        for (NNamedColor nNamedColor : _ALL_CANONICAL) {
//            List<NNamedColor> li = _ALL_BY_CANONICAL_NAME.get(nNamedColor.getName());
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
     * Deterministic mapping from int → NNamedColor.
     * <p>
     * Two-step mapping ensures colors are evenly chosen across canonical colors,
     * so grays (which are more numerous) don’t dominate the distribution.
     */
    public static NNamedColor pickName(int hashCode) {
        int a = Math.abs(hashCode);
        NNamedColor c = _ALL_CANONICAL.get(a % _ALL_CANONICAL.size());
        List<NNamedColor> li = _ALL_BY_CANONICAL_NAME.get(c.getName());
        return li.get(a % li.size());
    }

    private static NNamedColor _reg(String name, Color color) {
        int x = name.length();
        while (x > 0 && name.charAt(x-1) >= '0' && name.charAt(x-1) <= '9') {
            x--;
        }
        String canonicalName = name.substring(0, x);
        return _reg(canonicalName, canonicalName, color);
    }

    private static NNamedColor _reg(String name, String canonicalName, Color color) {
        NNamedColor namedColor = new NNamedColor(name, color);
        _ALL_REGISTERED.add(namedColor);
        _ALL_BY_NAME.put(name, namedColor);
        _ALL_BY_CANONICAL_NAME.computeIfAbsent(canonicalName, v -> new ArrayList<>()).add(namedColor);
        if (canonicalName.equals(name)) {
            _ALL_CANONICAL.add(namedColor);
        }
        return namedColor;
    }

    private static NNamedColor _reg(String name, int r, int g, int b) {
        return _reg(name, new Color(r, g, b));
    }

    private static NNamedColor _reg(String name, String canonicalName, int r, int g, int b) {
        return _reg(name, canonicalName, new Color(r, g, b));
    }

    private static NNamedColor _regGray(int percent) {
        int v = Math.round(percent * 255 / 100f);
        return _reg("Gray" + percent, "Gray", new Color(v, v, v));
    }

    public static final List<NNamedColor> ALL = Collections.unmodifiableList(_ALL_REGISTERED);
    public static final List<NNamedColor> ALL_CANONICAL = Collections.unmodifiableList(_ALL_CANONICAL);

    public static final Map<String, NNamedColor> BY_NAME = Collections.unmodifiableMap(_ALL_BY_NAME);

    public static NOptional<NNamedColor> ofName(String name) {
        return NOptional.ofNamed(BY_NAME.get(name == null ? null : NNameFormat.CLASS_NAME.format(name.trim()).toLowerCase()), "color " + name);
    }

    public static NOptional<List<NNamedColor>> ofCanonicalName(String name) {
        return NOptional.ofNamed(_ALL_BY_CANONICAL_NAME.get(name == null ? null : NNameFormat.CLASS_NAME.format(name.trim()).toLowerCase()), "color " + name);
    }

    public static String toHtmlHex(Color cl) {
        return String.format("#%02X%02X%02X", cl.getRed(), cl.getGreen(), cl.getBlue());
    }

    public static Color ansiToColor(int index) {
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
            return new Color(basicColors[index]);
        } else if (index < 232) {
            // 6x6x6 color cube
            int level = index - 16;
            int r = (level / 36) % 6 * 51;
            int g = (level / 6) % 6 * 51;
            int b = level % 6 * 51;
            return new Color(r, g, b);
        } else {
            // Grayscale colors
            int gray = (index - 232) * 10 + 8;
            return new Color(gray, gray, gray);
        }
    }

}
