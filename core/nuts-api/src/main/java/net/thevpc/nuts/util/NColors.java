package net.thevpc.nuts.util;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NColors {
    public static final NNamedColor BLACK = new NNamedColor("Black", new Color(0, 0, 0));
    public static final NNamedColor MAROON = new NNamedColor("Maroon", new Color(128, 0, 0));
    public static final NNamedColor GREEN = new NNamedColor("Green", new Color(0, 128, 0));
    public static final NNamedColor OLIVE = new NNamedColor("Olive", new Color(128, 128, 0));
    public static final NNamedColor NAVY = new NNamedColor("Navy", new Color(0, 0, 128));
    public static final NNamedColor PURPLE = new NNamedColor("Purple", new Color(128, 0, 128));
    public static final NNamedColor TEAL = new NNamedColor("Teal", new Color(0, 128, 128));
    public static final NNamedColor SILVER = new NNamedColor("Silver", new Color(192, 192, 192));
    public static final NNamedColor GREY = new NNamedColor("Grey", new Color(128, 128, 128));
    public static final NNamedColor RED = new NNamedColor("Red", new Color(255, 0, 0));
    public static final NNamedColor LIME = new NNamedColor("Lime", new Color(0, 255, 0));
    public static final NNamedColor YELLOW = new NNamedColor("Yellow", new Color(255, 255, 0));
    public static final NNamedColor BLUE = new NNamedColor("Blue", new Color(0, 0, 255));
    public static final NNamedColor FUCHSIA = new NNamedColor("Fuchsia", new Color(255, 0, 255));
    public static final NNamedColor AQUA = new NNamedColor("Aqua", new Color(0, 255, 255));
    public static final NNamedColor WHITE = new NNamedColor("White", new Color(255, 255, 255));
    public static final NNamedColor GREY_0 = new NNamedColor("Grey0", new Color(0, 0, 0));
    public static final NNamedColor NAVY_BLUE = new NNamedColor("NavyBlue", new Color(0, 0, 95));
    public static final NNamedColor DARK_BLUE = new NNamedColor("DarkBlue", new Color(0, 0, 135));
    public static final NNamedColor BLUE_2 = new NNamedColor("Blue2", new Color(0, 0, 175));
    public static final NNamedColor BLUE_3 = new NNamedColor("Blue3", new Color(0, 0, 215));
    public static final NNamedColor BLUE_4 = new NNamedColor("Blue4", new Color(0, 0, 255));
    public static final NNamedColor DARK_GREEN = new NNamedColor("DarkGreen", new Color(0, 95, 0));
    public static final NNamedColor DEEP_SKY_BLUE = new NNamedColor("DeepSkyBlue", new Color(0, 95, 95));
    public static final NNamedColor DEEP_SKY_BLUE_2 = new NNamedColor("DeepSkyBlue2", new Color(0, 95, 135));
    public static final NNamedColor DEEP_SKY_BLUE_3 = new NNamedColor("DeepSkyBlue3", new Color(0, 95, 175));
    public static final NNamedColor DODGER_BLUE = new NNamedColor("DodgerBlue", new Color(0, 95, 215));
    public static final NNamedColor DODGER_BLUE_2 = new NNamedColor("DodgerBlue2", new Color(0, 95, 255));
    public static final NNamedColor GREEN_2 = new NNamedColor("Green2", new Color(0, 135, 0));
    public static final NNamedColor SPRING_GREEN = new NNamedColor("SpringGreen", new Color(0, 135, 95));
    public static final NNamedColor TURQUOISE = new NNamedColor("Turquoise", new Color(0, 135, 135));
    public static final NNamedColor DEEP_SKY_BLUE_4 = new NNamedColor("DeepSkyBlue4", new Color(0, 135, 175));
    public static final NNamedColor DEEP_SKY_BLUE_5 = new NNamedColor("DeepSkyBlue5", new Color(0, 135, 215));
    public static final NNamedColor DODGER_BLUE_3 = new NNamedColor("DodgerBlue3", new Color(0, 135, 255));
    public static final NNamedColor GREEN_3 = new NNamedColor("Green3", new Color(0, 175, 0));
    public static final NNamedColor SPRING_GREEN_2 = new NNamedColor("SpringGreen2", new Color(0, 175, 95));
    public static final NNamedColor DARK_CYAN = new NNamedColor("DarkCyan", new Color(0, 175, 135));
    public static final NNamedColor LIGHT_SEA_GREEN = new NNamedColor("LightSeaGreen", new Color(0, 175, 175));
    public static final NNamedColor DEEP_SKY_BLUE_6 = new NNamedColor("DeepSkyBlue6", new Color(0, 175, 215));
    public static final NNamedColor DEEP_SKY_BLUE_7 = new NNamedColor("DeepSkyBlue7", new Color(0, 175, 255));
    public static final NNamedColor GREEN_4 = new NNamedColor("Green4", new Color(0, 215, 0));
    public static final NNamedColor SPRING_GREEN_3 = new NNamedColor("SpringGreen3", new Color(0, 215, 95));
    public static final NNamedColor SPRING_GREEN_4 = new NNamedColor("SpringGreen4", new Color(0, 215, 135));
    public static final NNamedColor CYAN = new NNamedColor("Cyan", new Color(0, 215, 175));
    public static final NNamedColor DARK_TURQUOISE = new NNamedColor("DarkTurquoise", new Color(0, 215, 215));
    public static final NNamedColor TURQUOISE_2 = new NNamedColor("Turquoise2", new Color(0, 215, 255));
    public static final NNamedColor GREEN_5 = new NNamedColor("Green5", new Color(0, 255, 0));
    public static final NNamedColor SPRING_GREEN_5 = new NNamedColor("SpringGreen5", new Color(0, 255, 95));
    public static final NNamedColor SPRING_GREEN_6 = new NNamedColor("SpringGreen6", new Color(0, 255, 135));
    public static final NNamedColor MEDIUM_SPRING_GREEN = new NNamedColor("MediumSpringGreen", new Color(0, 255, 175));
    public static final NNamedColor CYAN_2 = new NNamedColor("Cyan2", new Color(0, 255, 215));
    public static final NNamedColor CYAN_3 = new NNamedColor("Cyan3", new Color(0, 255, 255));
    public static final NNamedColor DARK_RED = new NNamedColor("DarkRed", new Color(95, 0, 0));
    public static final NNamedColor DEEP_PINK = new NNamedColor("DeepPink", new Color(95, 0, 95));
    public static final NNamedColor PURPLE_2 = new NNamedColor("Purple2", new Color(95, 0, 135));
    public static final NNamedColor PURPLE_3 = new NNamedColor("Purple3", new Color(95, 0, 175));
    public static final NNamedColor PURPLE_4 = new NNamedColor("Purple4", new Color(95, 0, 215));
    public static final NNamedColor BLUE_VIOLET = new NNamedColor("BlueViolet", new Color(95, 0, 255));
    public static final NNamedColor ORANGE = new NNamedColor("Orange", new Color(95, 95, 0));
    public static final NNamedColor GREY_37 = new NNamedColor("Grey37", new Color(95, 95, 95));
    public static final NNamedColor MEDIUM_PURPLE = new NNamedColor("MediumPurple", new Color(95, 95, 135));
    public static final NNamedColor SLATE_BLUE = new NNamedColor("SlateBlue", new Color(95, 95, 175));
    public static final NNamedColor SLATE_BLUE_2 = new NNamedColor("SlateBlue2", new Color(95, 95, 215));
    public static final NNamedColor ROYAL_BLUE = new NNamedColor("RoyalBlue", new Color(95, 95, 255));
    public static final NNamedColor CHARTREUSE = new NNamedColor("Chartreuse", new Color(95, 135, 0));
    public static final NNamedColor DARK_SEA_GREEN = new NNamedColor("DarkSeaGreen", new Color(95, 135, 95));
    public static final NNamedColor PALE_TURQUOISE = new NNamedColor("PaleTurquoise", new Color(95, 135, 135));
    public static final NNamedColor STEEL_BLUE = new NNamedColor("SteelBlue", new Color(95, 135, 175));
    public static final NNamedColor STEEL_BLUE_2 = new NNamedColor("SteelBlue2", new Color(95, 135, 215));
    public static final NNamedColor CORNFLOWER_BLUE = new NNamedColor("CornflowerBlue", new Color(95, 135, 255));
    public static final NNamedColor CHARTREUSE_2 = new NNamedColor("Chartreuse2", new Color(95, 175, 0));
    public static final NNamedColor DARK_SEA_GREEN_2 = new NNamedColor("DarkSeaGreen2", new Color(95, 175, 95));
    public static final NNamedColor CADET_BLUE = new NNamedColor("CadetBlue", new Color(95, 175, 135));
    public static final NNamedColor CADET_BLUE_2 = new NNamedColor("CadetBlue2", new Color(95, 175, 175));
    public static final NNamedColor SKY_BLUE = new NNamedColor("SkyBlue", new Color(95, 175, 215));
    public static final NNamedColor STEEL_BLUE_3 = new NNamedColor("SteelBlue3", new Color(95, 175, 255));
    public static final NNamedColor CHARTREUSE_3 = new NNamedColor("Chartreuse3", new Color(95, 215, 0));
    public static final NNamedColor PALE_GREEN = new NNamedColor("PaleGreen", new Color(95, 215, 95));
    public static final NNamedColor SEA_GREEN = new NNamedColor("SeaGreen", new Color(95, 215, 135));
    public static final NNamedColor AQUAMARINE = new NNamedColor("Aquamarine", new Color(95, 215, 175));
    public static final NNamedColor MEDIUM_TURQUOISE = new NNamedColor("MediumTurquoise", new Color(95, 215, 215));
    public static final NNamedColor STEEL_BLUE_4 = new NNamedColor("SteelBlue4", new Color(95, 215, 255));
    public static final NNamedColor CHARTREUSE_4 = new NNamedColor("Chartreuse4", new Color(95, 255, 0));
    public static final NNamedColor SEA_GREEN_2 = new NNamedColor("SeaGreen2", new Color(95, 255, 95));
    public static final NNamedColor SEA_GREEN_3 = new NNamedColor("SeaGreen3", new Color(95, 255, 135));
    public static final NNamedColor SEA_GREEN_4 = new NNamedColor("SeaGreen4", new Color(95, 255, 175));
    public static final NNamedColor AQUAMARINE_2 = new NNamedColor("Aquamarine2", new Color(95, 255, 215));
    public static final NNamedColor DARK_SLATE_GRAY = new NNamedColor("DarkSlateGray", new Color(95, 255, 255));
    public static final NNamedColor DARK_RED_2 = new NNamedColor("DarkRed2", new Color(135, 0, 0));
    public static final NNamedColor DEEP_PINK_2 = new NNamedColor("DeepPink2", new Color(135, 0, 95));
    public static final NNamedColor DARK_MAGENTA = new NNamedColor("DarkMagenta", new Color(135, 0, 135));
    public static final NNamedColor DARK_MAGENTA_2 = new NNamedColor("DarkMagenta2", new Color(135, 0, 175));
    public static final NNamedColor DARK_VIOLET = new NNamedColor("DarkViolet", new Color(135, 0, 215));
    public static final NNamedColor PURPLE_5 = new NNamedColor("Purple5", new Color(135, 0, 255));
    public static final NNamedColor ORANGE_2 = new NNamedColor("Orange2", new Color(135, 95, 0));
    public static final NNamedColor LIGHT_PINK = new NNamedColor("LightPink", new Color(135, 95, 95));
    public static final NNamedColor PLUM = new NNamedColor("Plum", new Color(135, 95, 135));
    public static final NNamedColor MEDIUM_PURPLE_2 = new NNamedColor("MediumPurple2", new Color(135, 95, 175));
    public static final NNamedColor MEDIUM_PURPLE_3 = new NNamedColor("MediumPurple3", new Color(135, 95, 215));
    public static final NNamedColor SLATE_BLUE_3 = new NNamedColor("SlateBlue3", new Color(135, 95, 255));
    public static final NNamedColor YELLOW_2 = new NNamedColor("Yellow2", new Color(135, 135, 0));
    public static final NNamedColor WHEAT = new NNamedColor("Wheat", new Color(135, 135, 95));
    public static final NNamedColor GREY_53 = new NNamedColor("Grey53", new Color(135, 135, 135));
    public static final NNamedColor LIGHT_SLATE_GREY = new NNamedColor("LightSlateGrey", new Color(135, 135, 175));
    public static final NNamedColor MEDIUM_PURPLE_4 = new NNamedColor("MediumPurple4", new Color(135, 135, 215));
    public static final NNamedColor LIGHT_SLATE_BLUE = new NNamedColor("LightSlateBlue", new Color(135, 135, 255));
    public static final NNamedColor YELLOW_3 = new NNamedColor("Yellow3", new Color(135, 175, 0));
    public static final NNamedColor DARK_OLIVE_GREEN = new NNamedColor("DarkOliveGreen", new Color(135, 175, 95));
    public static final NNamedColor DARK_SEA_GREEN_3 = new NNamedColor("DarkSeaGreen3", new Color(135, 175, 135));
    public static final NNamedColor LIGHT_SKY_BLUE = new NNamedColor("LightSkyBlue", new Color(135, 175, 175));
    public static final NNamedColor LIGHT_SKY_BLUE_2 = new NNamedColor("LightSkyBlue2", new Color(135, 175, 215));
    public static final NNamedColor SKY_BLUE_2 = new NNamedColor("SkyBlue2", new Color(135, 175, 255));
    public static final NNamedColor CHARTREUSE_5 = new NNamedColor("Chartreuse5", new Color(135, 215, 0));
    public static final NNamedColor DARK_OLIVE_GREEN_2 = new NNamedColor("DarkOliveGreen2", new Color(135, 215, 95));
    public static final NNamedColor PALE_GREEN_2 = new NNamedColor("PaleGreen2", new Color(135, 215, 135));
    public static final NNamedColor DARK_SEA_GREEN_4 = new NNamedColor("DarkSeaGreen4", new Color(135, 215, 175));
    public static final NNamedColor DARK_SLATE_GRAY_2 = new NNamedColor("DarkSlateGray2", new Color(135, 215, 215));
    public static final NNamedColor SKY_BLUE_3 = new NNamedColor("SkyBlue3", new Color(135, 215, 255));
    public static final NNamedColor CHARTREUSE_6 = new NNamedColor("Chartreuse6", new Color(135, 255, 0));
    public static final NNamedColor LIGHT_GREEN = new NNamedColor("LightGreen", new Color(135, 255, 95));
    public static final NNamedColor LIGHT_GREEN_2 = new NNamedColor("LightGreen2", new Color(135, 255, 135));
    public static final NNamedColor PALE_GREEN_3 = new NNamedColor("PaleGreen3", new Color(135, 255, 175));
    public static final NNamedColor AQUAMARINE_3 = new NNamedColor("Aquamarine3", new Color(135, 255, 215));
    public static final NNamedColor DARK_SLATE_GRAY_3 = new NNamedColor("DarkSlateGray3", new Color(135, 255, 255));
    public static final NNamedColor RED_2 = new NNamedColor("Red2", new Color(175, 0, 0));
    public static final NNamedColor DEEP_PINK_3 = new NNamedColor("DeepPink3", new Color(175, 0, 95));
    public static final NNamedColor MEDIUM_VIOLET_RED = new NNamedColor("MediumVioletRed", new Color(175, 0, 135));
    public static final NNamedColor MAGENTA = new NNamedColor("Magenta", new Color(175, 0, 175));
    public static final NNamedColor DARK_VIOLET_2 = new NNamedColor("DarkViolet2", new Color(175, 0, 215));
    public static final NNamedColor PURPLE_6 = new NNamedColor("Purple6", new Color(175, 0, 255));
    public static final NNamedColor DARK_ORANGE = new NNamedColor("DarkOrange", new Color(175, 95, 0));
    public static final NNamedColor INDIAN_RED = new NNamedColor("IndianRed", new Color(175, 95, 95));
    public static final NNamedColor HOT_PINK = new NNamedColor("HotPink", new Color(175, 95, 135));
    public static final NNamedColor MEDIUM_ORCHID = new NNamedColor("MediumOrchid", new Color(175, 95, 175));
    public static final NNamedColor MEDIUM_ORCHID_2 = new NNamedColor("MediumOrchid2", new Color(175, 95, 215));
    public static final NNamedColor MEDIUM_PURPLE_5 = new NNamedColor("MediumPurple5", new Color(175, 95, 255));
    public static final NNamedColor DARK_GOLDENROD = new NNamedColor("DarkGoldenrod", new Color(175, 135, 0));
    public static final NNamedColor LIGHT_SALMON = new NNamedColor("LightSalmon", new Color(175, 135, 95));
    public static final NNamedColor ROSY_BROWN = new NNamedColor("RosyBrown", new Color(175, 135, 135));
    public static final NNamedColor GREY_63 = new NNamedColor("Grey63", new Color(175, 135, 175));
    public static final NNamedColor MEDIUM_PURPLE_6 = new NNamedColor("MediumPurple6", new Color(175, 135, 215));
    public static final NNamedColor MEDIUM_PURPLE_7 = new NNamedColor("MediumPurple7", new Color(175, 135, 255));
    public static final NNamedColor GOLD = new NNamedColor("Gold", new Color(175, 175, 0));
    public static final NNamedColor DARK_KHAKI = new NNamedColor("DarkKhaki", new Color(175, 175, 95));
    public static final NNamedColor NAVAJO_WHITE = new NNamedColor("NavajoWhite", new Color(175, 175, 135));
    public static final NNamedColor GREY_69 = new NNamedColor("Grey69", new Color(175, 175, 175));
    public static final NNamedColor LIGHT_STEEL_BLUE = new NNamedColor("LightSteelBlue", new Color(175, 175, 215));
    public static final NNamedColor LIGHT_STEEL_BLUE_2 = new NNamedColor("LightSteelBlue2", new Color(175, 175, 255));
    public static final NNamedColor YELLOW_4 = new NNamedColor("Yellow4", new Color(175, 215, 0));
    public static final NNamedColor DARK_OLIVE_GREEN_3 = new NNamedColor("DarkOliveGreen3", new Color(175, 215, 95));
    public static final NNamedColor DARK_SEA_GREEN_5 = new NNamedColor("DarkSeaGreen5", new Color(175, 215, 135));
    public static final NNamedColor DARK_SEA_GREEN_6 = new NNamedColor("DarkSeaGreen6", new Color(175, 215, 175));
    public static final NNamedColor LIGHT_CYAN = new NNamedColor("LightCyan", new Color(175, 215, 215));
    public static final NNamedColor LIGHT_SKY_BLUE_3 = new NNamedColor("LightSkyBlue3", new Color(175, 215, 255));
    public static final NNamedColor GREEN_YELLOW = new NNamedColor("GreenYellow", new Color(175, 255, 0));
    public static final NNamedColor DARK_OLIVE_GREEN_4 = new NNamedColor("DarkOliveGreen4", new Color(175, 255, 95));
    public static final NNamedColor PALE_GREEN_4 = new NNamedColor("PaleGreen4", new Color(175, 255, 135));
    public static final NNamedColor DARK_SEA_GREEN_7 = new NNamedColor("DarkSeaGreen7", new Color(175, 255, 175));
    public static final NNamedColor DARK_SEA_GREEN_8 = new NNamedColor("DarkSeaGreen8", new Color(175, 255, 215));
    public static final NNamedColor PALE_TURQUOISE_2 = new NNamedColor("PaleTurquoise2", new Color(175, 255, 255));
    public static final NNamedColor RED_3 = new NNamedColor("Red3", new Color(215, 0, 0));
    public static final NNamedColor DEEP_PINK_4 = new NNamedColor("DeepPink4", new Color(215, 0, 95));
    public static final NNamedColor DEEP_PINK_5 = new NNamedColor("DeepPink5", new Color(215, 0, 135));
    public static final NNamedColor MAGENTA_2 = new NNamedColor("Magenta2", new Color(215, 0, 175));
    public static final NNamedColor MAGENTA_3 = new NNamedColor("Magenta3", new Color(215, 0, 215));
    public static final NNamedColor MAGENTA_4 = new NNamedColor("Magenta4", new Color(215, 0, 255));
    public static final NNamedColor DARK_ORANGE_2 = new NNamedColor("DarkOrange2", new Color(215, 95, 0));
    public static final NNamedColor INDIAN_RED_2 = new NNamedColor("IndianRed2", new Color(215, 95, 95));
    public static final NNamedColor HOT_PINK_2 = new NNamedColor("HotPink2", new Color(215, 95, 135));
    public static final NNamedColor HOT_PINK_3 = new NNamedColor("HotPink3", new Color(215, 95, 175));
    public static final NNamedColor ORCHID = new NNamedColor("Orchid", new Color(215, 95, 215));
    public static final NNamedColor MEDIUM_ORCHID_3 = new NNamedColor("MediumOrchid3", new Color(215, 95, 255));
    public static final NNamedColor ORANGE_3 = new NNamedColor("Orange3", new Color(215, 135, 0));
    public static final NNamedColor LIGHT_SALMON_2 = new NNamedColor("LightSalmon2", new Color(215, 135, 95));
    public static final NNamedColor LIGHT_PINK_2 = new NNamedColor("LightPink2", new Color(215, 135, 135));
    public static final NNamedColor PINK = new NNamedColor("Pink", new Color(215, 135, 175));
    public static final NNamedColor PLUM_2 = new NNamedColor("Plum2", new Color(215, 135, 215));
    public static final NNamedColor VIOLET = new NNamedColor("Violet", new Color(215, 135, 255));
    public static final NNamedColor GOLD_2 = new NNamedColor("Gold2", new Color(215, 175, 0));
    public static final NNamedColor LIGHT_GOLDENROD = new NNamedColor("LightGoldenrod", new Color(215, 175, 95));
    public static final NNamedColor TAN = new NNamedColor("Tan", new Color(215, 175, 135));
    public static final NNamedColor MISTY_ROSE = new NNamedColor("MistyRose", new Color(215, 175, 175));
    public static final NNamedColor THISTLE = new NNamedColor("Thistle", new Color(215, 175, 215));
    public static final NNamedColor PLUM_3 = new NNamedColor("Plum3", new Color(215, 175, 255));
    public static final NNamedColor YELLOW_5 = new NNamedColor("Yellow5", new Color(215, 215, 0));
    public static final NNamedColor KHAKI = new NNamedColor("Khaki", new Color(215, 215, 95));
    public static final NNamedColor LIGHT_GOLDENROD_2 = new NNamedColor("LightGoldenrod2", new Color(215, 215, 135));
    public static final NNamedColor LIGHT_YELLOW = new NNamedColor("LightYellow", new Color(215, 215, 175));
    public static final NNamedColor GREY_84 = new NNamedColor("Grey84", new Color(215, 215, 215));
    public static final NNamedColor LIGHT_STEEL_BLUE_3 = new NNamedColor("LightSteelBlue3", new Color(215, 215, 255));
    public static final NNamedColor YELLOW_6 = new NNamedColor("Yellow6", new Color(215, 255, 0));
    public static final NNamedColor DARK_OLIVE_GREEN_5 = new NNamedColor("DarkOliveGreen5", new Color(215, 255, 95));
    public static final NNamedColor DARK_OLIVE_GREEN_6 = new NNamedColor("DarkOliveGreen6", new Color(215, 255, 135));
    public static final NNamedColor DARK_SEA_GREEN_9 = new NNamedColor("DarkSeaGreen9", new Color(215, 255, 175));
    public static final NNamedColor HONEYDEW = new NNamedColor("Honeydew", new Color(215, 255, 215));
    public static final NNamedColor LIGHT_CYAN_2 = new NNamedColor("LightCyan2", new Color(215, 255, 255));
    public static final NNamedColor RED_4 = new NNamedColor("Red4", new Color(255, 0, 0));
    public static final NNamedColor DEEP_PINK_6 = new NNamedColor("DeepPink6", new Color(255, 0, 95));
    public static final NNamedColor DEEP_PINK_7 = new NNamedColor("DeepPink7", new Color(255, 0, 135));
    public static final NNamedColor DEEP_PINK_8 = new NNamedColor("DeepPink8", new Color(255, 0, 175));
    public static final NNamedColor MAGENTA_5 = new NNamedColor("Magenta5", new Color(255, 0, 215));
    public static final NNamedColor MAGENTA_6 = new NNamedColor("Magenta6", new Color(255, 0, 255));
    public static final NNamedColor ORANGE_RED = new NNamedColor("OrangeRed", new Color(255, 95, 0));
    public static final NNamedColor INDIAN_RED_3 = new NNamedColor("IndianRed3", new Color(255, 95, 95));
    public static final NNamedColor INDIAN_RED_4 = new NNamedColor("IndianRed4", new Color(255, 95, 135));
    public static final NNamedColor HOT_PINK_4 = new NNamedColor("HotPink4", new Color(255, 95, 175));
    public static final NNamedColor HOT_PINK_5 = new NNamedColor("HotPink5", new Color(255, 95, 215));
    public static final NNamedColor MEDIUM_ORCHID_4 = new NNamedColor("MediumOrchid4", new Color(255, 95, 255));
    public static final NNamedColor DARK_ORANGE_3 = new NNamedColor("DarkOrange3", new Color(255, 135, 0));
    public static final NNamedColor SALMON = new NNamedColor("Salmon", new Color(255, 135, 95));
    public static final NNamedColor LIGHT_CORAL = new NNamedColor("LightCoral", new Color(255, 135, 135));
    public static final NNamedColor PALE_VIOLET_RED = new NNamedColor("PaleVioletRed", new Color(255, 135, 175));
    public static final NNamedColor ORCHID_2 = new NNamedColor("Orchid2", new Color(255, 135, 215));
    public static final NNamedColor ORCHID_3 = new NNamedColor("Orchid3", new Color(255, 135, 255));
    public static final NNamedColor ORANGE_4 = new NNamedColor("Orange4", new Color(255, 175, 0));
    public static final NNamedColor SANDY_BROWN = new NNamedColor("SandyBrown", new Color(255, 175, 95));
    public static final NNamedColor LIGHT_SALMON_3 = new NNamedColor("LightSalmon3", new Color(255, 175, 135));
    public static final NNamedColor LIGHT_PINK_3 = new NNamedColor("LightPink3", new Color(255, 175, 175));
    public static final NNamedColor PINK_2 = new NNamedColor("Pink2", new Color(255, 175, 215));
    public static final NNamedColor PLUM_4 = new NNamedColor("Plum4", new Color(255, 175, 255));
    public static final NNamedColor GOLD_3 = new NNamedColor("Gold3", new Color(255, 215, 0));
    public static final NNamedColor LIGHT_GOLDENROD_3 = new NNamedColor("LightGoldenrod3", new Color(255, 215, 95));
    public static final NNamedColor LIGHT_GOLDENROD_4 = new NNamedColor("LightGoldenrod4", new Color(255, 215, 135));
    public static final NNamedColor NAVAJO_WHITE_2 = new NNamedColor("NavajoWhite2", new Color(255, 215, 175));
    public static final NNamedColor MISTY_ROSE_2 = new NNamedColor("MistyRose2", new Color(255, 215, 215));
    public static final NNamedColor THISTLE_2 = new NNamedColor("Thistle2", new Color(255, 215, 255));
    public static final NNamedColor YELLOW_7 = new NNamedColor("Yellow7", new Color(255, 255, 0));
    public static final NNamedColor LIGHT_GOLDENROD_5 = new NNamedColor("LightGoldenrod5", new Color(255, 255, 95));
    public static final NNamedColor KHAKI_2 = new NNamedColor("Khaki2", new Color(255, 255, 135));
    public static final NNamedColor WHEAT_2 = new NNamedColor("Wheat2", new Color(255, 255, 175));
    public static final NNamedColor CORNSILK = new NNamedColor("Cornsilk", new Color(255, 255, 215));
    public static final NNamedColor GREY_100 = new NNamedColor("Grey100", new Color(255, 255, 255));
    public static final NNamedColor GREY_3 = new NNamedColor("Grey3", new Color(8, 8, 8));
    public static final NNamedColor GREY_7 = new NNamedColor("Grey7", new Color(18, 18, 18));
    public static final NNamedColor GREY_11 = new NNamedColor("Grey11", new Color(28, 28, 28));
    public static final NNamedColor GREY_15 = new NNamedColor("Grey15", new Color(38, 38, 38));
    public static final NNamedColor GREY_19 = new NNamedColor("Grey19", new Color(48, 48, 48));
    public static final NNamedColor GREY_23 = new NNamedColor("Grey23", new Color(58, 58, 58));
    public static final NNamedColor GREY_27 = new NNamedColor("Grey27", new Color(68, 68, 68));
    public static final NNamedColor GREY_30 = new NNamedColor("Grey30", new Color(78, 78, 78));
    public static final NNamedColor GREY_35 = new NNamedColor("Grey35", new Color(88, 88, 88));
    public static final NNamedColor GREY_39 = new NNamedColor("Grey39", new Color(98, 98, 98));
    public static final NNamedColor GREY_42 = new NNamedColor("Grey42", new Color(108, 108, 108));
    public static final NNamedColor GREY_46 = new NNamedColor("Grey46", new Color(118, 118, 118));
    public static final NNamedColor GREY_50 = new NNamedColor("Grey50", new Color(128, 128, 128));
    public static final NNamedColor GREY_54 = new NNamedColor("Grey54", new Color(138, 138, 138));
    public static final NNamedColor GREY_58 = new NNamedColor("Grey58", new Color(148, 148, 148));
    public static final NNamedColor GREY_62 = new NNamedColor("Grey62", new Color(158, 158, 158));
    public static final NNamedColor GREY_66 = new NNamedColor("Grey66", new Color(168, 168, 168));
    public static final NNamedColor GREY_70 = new NNamedColor("Grey70", new Color(178, 178, 178));
    public static final NNamedColor GREY_74 = new NNamedColor("Grey74", new Color(188, 188, 188));
    public static final NNamedColor GREY_78 = new NNamedColor("Grey78", new Color(198, 198, 198));
    public static final NNamedColor GREY_82 = new NNamedColor("Grey82", new Color(208, 208, 208));
    public static final NNamedColor GREY_85 = new NNamedColor("Grey85", new Color(218, 218, 218));
    public static final NNamedColor GREY_89 = new NNamedColor("Grey89", new Color(228, 228, 228));
    public static final NNamedColor GREY_93 = new NNamedColor("Grey93", new Color(238, 238, 238));

    public static final List<NNamedColor> ALL = Arrays.asList(new NNamedColor[]{
            BLACK,
            MAROON,
            GREEN,
            OLIVE,
            NAVY,
            PURPLE,
            TEAL,
            SILVER,
            GREY,
            RED,
            LIME,
            YELLOW,
            BLUE,
            FUCHSIA,
            AQUA,
            WHITE,
            GREY_0,
            NAVY_BLUE,
            DARK_BLUE,
            BLUE_2,
            BLUE_3,
            BLUE_4,
            DARK_GREEN,
            DEEP_SKY_BLUE,
            DEEP_SKY_BLUE_2,
            DEEP_SKY_BLUE_3,
            DODGER_BLUE,
            DODGER_BLUE_2,
            GREEN_2,
            SPRING_GREEN,
            TURQUOISE,
            DEEP_SKY_BLUE_4,
            DEEP_SKY_BLUE_5,
            DODGER_BLUE_3,
            GREEN_3,
            SPRING_GREEN_2,
            DARK_CYAN,
            LIGHT_SEA_GREEN,
            DEEP_SKY_BLUE_6,
            DEEP_SKY_BLUE_7,
            GREEN_4,
            SPRING_GREEN_3,
            SPRING_GREEN_4,
            CYAN,
            DARK_TURQUOISE,
            TURQUOISE_2,
            GREEN_5,
            SPRING_GREEN_5,
            SPRING_GREEN_6,
            MEDIUM_SPRING_GREEN,
            CYAN_2,
            CYAN_3,
            DARK_RED,
            DEEP_PINK,
            PURPLE_2,
            PURPLE_3,
            PURPLE_4,
            BLUE_VIOLET,
            ORANGE,
            GREY_37,
            MEDIUM_PURPLE,
            SLATE_BLUE,
            SLATE_BLUE_2,
            ROYAL_BLUE,
            CHARTREUSE,
            DARK_SEA_GREEN,
            PALE_TURQUOISE,
            STEEL_BLUE,
            STEEL_BLUE_2,
            CORNFLOWER_BLUE,
            CHARTREUSE_2,
            DARK_SEA_GREEN_2,
            CADET_BLUE,
            CADET_BLUE_2,
            SKY_BLUE,
            STEEL_BLUE_3,
            CHARTREUSE_3,
            PALE_GREEN,
            SEA_GREEN,
            AQUAMARINE,
            MEDIUM_TURQUOISE,
            STEEL_BLUE_4,
            CHARTREUSE_4,
            SEA_GREEN_2,
            SEA_GREEN_3,
            SEA_GREEN_4,
            AQUAMARINE_2,
            DARK_SLATE_GRAY,
            DARK_RED_2,
            DEEP_PINK_2,
            DARK_MAGENTA,
            DARK_MAGENTA_2,
            DARK_VIOLET,
            PURPLE_5,
            ORANGE_2,
            LIGHT_PINK,
            PLUM,
            MEDIUM_PURPLE_2,
            MEDIUM_PURPLE_3,
            SLATE_BLUE_3,
            YELLOW_2,
            WHEAT,
            GREY_53,
            LIGHT_SLATE_GREY,
            MEDIUM_PURPLE_4,
            LIGHT_SLATE_BLUE,
            YELLOW_3,
            DARK_OLIVE_GREEN,
            DARK_SEA_GREEN_3,
            LIGHT_SKY_BLUE,
            LIGHT_SKY_BLUE_2,
            SKY_BLUE_2,
            CHARTREUSE_5,
            DARK_OLIVE_GREEN_2,
            PALE_GREEN_2,
            DARK_SEA_GREEN_4,
            DARK_SLATE_GRAY_2,
            SKY_BLUE_3,
            CHARTREUSE_6,
            LIGHT_GREEN,
            LIGHT_GREEN_2,
            PALE_GREEN_3,
            AQUAMARINE_3,
            DARK_SLATE_GRAY_3,
            RED_2,
            DEEP_PINK_3,
            MEDIUM_VIOLET_RED,
            MAGENTA,
            DARK_VIOLET_2,
            PURPLE_6,
            DARK_ORANGE,
            INDIAN_RED,
            HOT_PINK,
            MEDIUM_ORCHID,
            MEDIUM_ORCHID_2,
            MEDIUM_PURPLE_5,
            DARK_GOLDENROD,
            LIGHT_SALMON,
            ROSY_BROWN,
            GREY_63,
            MEDIUM_PURPLE_6,
            MEDIUM_PURPLE_7,
            GOLD,
            DARK_KHAKI,
            NAVAJO_WHITE,
            GREY_69,
            LIGHT_STEEL_BLUE,
            LIGHT_STEEL_BLUE_2,
            YELLOW_4,
            DARK_OLIVE_GREEN_3,
            DARK_SEA_GREEN_5,
            DARK_SEA_GREEN_6,
            LIGHT_CYAN,
            LIGHT_SKY_BLUE_3,
            GREEN_YELLOW,
            DARK_OLIVE_GREEN_4,
            PALE_GREEN_4,
            DARK_SEA_GREEN_7,
            DARK_SEA_GREEN_8,
            PALE_TURQUOISE_2,
            RED_3,
            DEEP_PINK_4,
            DEEP_PINK_5,
            MAGENTA_2,
            MAGENTA_3,
            MAGENTA_4,
            DARK_ORANGE_2,
            INDIAN_RED_2,
            HOT_PINK_2,
            HOT_PINK_3,
            ORCHID,
            MEDIUM_ORCHID_3,
            ORANGE_3,
            LIGHT_SALMON_2,
            LIGHT_PINK_2,
            PINK,
            PLUM_2,
            VIOLET,
            GOLD_2,
            LIGHT_GOLDENROD,
            TAN,
            MISTY_ROSE,
            THISTLE,
            PLUM_3,
            YELLOW_5,
            KHAKI,
            LIGHT_GOLDENROD_2,
            LIGHT_YELLOW,
            GREY_84,
            LIGHT_STEEL_BLUE_3,
            YELLOW_6,
            DARK_OLIVE_GREEN_5,
            DARK_OLIVE_GREEN_6,
            DARK_SEA_GREEN_9,
            HONEYDEW,
            LIGHT_CYAN_2,
            RED_4,
            DEEP_PINK_6,
            DEEP_PINK_7,
            DEEP_PINK_8,
            MAGENTA_5,
            MAGENTA_6,
            ORANGE_RED,
            INDIAN_RED_3,
            INDIAN_RED_4,
            HOT_PINK_4,
            HOT_PINK_5,
            MEDIUM_ORCHID_4,
            DARK_ORANGE_3,
            SALMON,
            LIGHT_CORAL,
            PALE_VIOLET_RED,
            ORCHID_2,
            ORCHID_3,
            ORANGE_4,
            SANDY_BROWN,
            LIGHT_SALMON_3,
            LIGHT_PINK_3,
            PINK_2,
            PLUM_4,
            GOLD_3,
            LIGHT_GOLDENROD_3,
            LIGHT_GOLDENROD_4,
            NAVAJO_WHITE_2,
            MISTY_ROSE_2,
            THISTLE_2,
            YELLOW_7,
            LIGHT_GOLDENROD_5,
            KHAKI_2,
            WHEAT_2,
            CORNSILK,
            GREY_100,
            GREY_3,
            GREY_7,
            GREY_11,
            GREY_15,
            GREY_19,
            GREY_23,
            GREY_27,
            GREY_30,
            GREY_35,
            GREY_39,
            GREY_42,
            GREY_46,
            GREY_50,
            GREY_54,
            GREY_58,
            GREY_62,
            GREY_66,
            GREY_70,
            GREY_74,
            GREY_78,
            GREY_82,
            GREY_85,
            GREY_89,
            GREY_93
    });
    public static final Map<String, NNamedColor> BY_NAME = Collections.unmodifiableMap(
            ALL.stream()
                    .collect(Collectors.toMap(c->c.getName().toLowerCase(), c -> c))
    );

    public static NOptional<NNamedColor> ofName(String name) {
        return NOptional.ofNamed(BY_NAME.get(name == null ? null : NNameFormat.CLASS_NAME.format(name.trim()).toLowerCase()), "color " + name);
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

//    //OLD
//    public static Color[] COLOR255_VALUES = new Color[]{
//            new Color(0, 0, 0),
//            new Color(128, 0, 0),
//            new Color(0, 128, 0),
//            new Color(128, 128, 0),
//            new Color(0, 0, 128),
//            new Color(128, 0, 128),
//            new Color(0, 128, 128),
//            new Color(192, 192, 192),
//            new Color(128, 128, 128),
//            new Color(255, 0, 0),
//            new Color(0, 255, 0),
//            new Color(255, 255, 0),
//            new Color(0, 0, 255),
//            new Color(255, 0, 255),
//            new Color(0, 255, 255),
//            new Color(255, 255, 255),
//            new Color(0, 0, 0),
//            new Color(0, 0, 95),
//            new Color(0, 0, 135),
//            new Color(0, 0, 175),
//            new Color(0, 0, 215),
//            new Color(0, 0, 255),
//            new Color(0, 95, 0),
//            new Color(0, 95, 95),
//            new Color(0, 95, 135),
//            new Color(0, 95, 175),
//            new Color(0, 95, 215),
//            new Color(0, 95, 255),
//            new Color(0, 135, 0),
//            new Color(0, 135, 95),
//            new Color(0, 135, 135),
//            new Color(0, 135, 175),
//            new Color(0, 135, 215),
//            new Color(0, 135, 255),
//            new Color(0, 175, 0),
//            new Color(0, 175, 95),
//            new Color(0, 175, 135),
//            new Color(0, 175, 175),
//            new Color(0, 175, 215),
//            new Color(0, 175, 255),
//            new Color(0, 215, 0),
//            new Color(0, 215, 95),
//            new Color(0, 215, 135),
//            new Color(0, 215, 175),
//            new Color(0, 215, 215),
//            new Color(0, 215, 255),
//            new Color(0, 255, 0),
//            new Color(0, 255, 95),
//            new Color(0, 255, 135),
//            new Color(0, 255, 175),
//            new Color(0, 255, 215),
//            new Color(0, 255, 255),
//            new Color(95, 0, 0),
//            new Color(95, 0, 95),
//            new Color(95, 0, 135),
//            new Color(95, 0, 175),
//            new Color(95, 0, 215),
//            new Color(95, 0, 255),
//            new Color(95, 95, 0),
//            new Color(95, 95, 95),
//            new Color(95, 95, 135),
//            new Color(95, 95, 175),
//            new Color(95, 95, 215),
//            new Color(95, 95, 255),
//            new Color(95, 135, 0),
//            new Color(95, 135, 95),
//            new Color(95, 135, 135),
//            new Color(95, 135, 175),
//            new Color(95, 135, 215),
//            new Color(95, 135, 255),
//            new Color(95, 175, 0),
//            new Color(95, 175, 95),
//            new Color(95, 175, 135),
//            new Color(95, 175, 175),
//            new Color(95, 175, 215),
//            new Color(95, 175, 255),
//            new Color(95, 215, 0),
//            new Color(95, 215, 95),
//            new Color(95, 215, 135),
//            new Color(95, 215, 175),
//            new Color(95, 215, 215),
//            new Color(95, 215, 255),
//            new Color(95, 255, 0),
//            new Color(95, 255, 95),
//            new Color(95, 255, 135),
//            new Color(95, 255, 175),
//            new Color(95, 255, 215),
//            new Color(95, 255, 255),
//            new Color(135, 0, 0),
//            new Color(135, 0, 95),
//            new Color(135, 0, 135),
//            new Color(135, 0, 175),
//            new Color(135, 0, 215),
//            new Color(135, 0, 255),
//            new Color(135, 95, 0),
//            new Color(135, 95, 95),
//            new Color(135, 95, 135),
//            new Color(135, 95, 175),
//            new Color(135, 95, 215),
//            new Color(135, 95, 255),
//            new Color(135, 135, 0),
//            new Color(135, 135, 95),
//            new Color(135, 135, 135),
//            new Color(135, 135, 175),
//            new Color(135, 135, 215),
//            new Color(135, 135, 255),
//            new Color(135, 175, 0),
//            new Color(135, 175, 95),
//            new Color(135, 175, 135),
//            new Color(135, 175, 175),
//            new Color(135, 175, 215),
//            new Color(135, 175, 255),
//            new Color(135, 215, 0),
//            new Color(135, 215, 95),
//            new Color(135, 215, 135),
//            new Color(135, 215, 175),
//            new Color(135, 215, 215),
//            new Color(135, 215, 255),
//            new Color(135, 255, 0),
//            new Color(135, 255, 95),
//            new Color(135, 255, 135),
//            new Color(135, 255, 175),
//            new Color(135, 255, 215),
//            new Color(135, 255, 255),
//            new Color(175, 0, 0),
//            new Color(175, 0, 95),
//            new Color(175, 0, 135),
//            new Color(175, 0, 175),
//            new Color(175, 0, 215),
//            new Color(175, 0, 255),
//            new Color(175, 95, 0),
//            new Color(175, 95, 95),
//            new Color(175, 95, 135),
//            new Color(175, 95, 175),
//            new Color(175, 95, 215),
//            new Color(175, 95, 255),
//            new Color(175, 135, 0),
//            new Color(175, 135, 95),
//            new Color(175, 135, 135),
//            new Color(175, 135, 175),
//            new Color(175, 135, 215),
//            new Color(175, 135, 255),
//            new Color(175, 175, 0),
//            new Color(175, 175, 95),
//            new Color(175, 175, 135),
//            new Color(175, 175, 175),
//            new Color(175, 175, 215),
//            new Color(175, 175, 255),
//            new Color(175, 215, 0),
//            new Color(175, 215, 95),
//            new Color(175, 215, 135),
//            new Color(175, 215, 175),
//            new Color(175, 215, 215),
//            new Color(175, 215, 255),
//            new Color(175, 255, 0),
//            new Color(175, 255, 95),
//            new Color(175, 255, 135),
//            new Color(175, 255, 175),
//            new Color(175, 255, 215),
//            new Color(175, 255, 255),
//            new Color(215, 0, 0),
//            new Color(215, 0, 95),
//            new Color(215, 0, 135),
//            new Color(215, 0, 175),
//            new Color(215, 0, 215),
//            new Color(215, 0, 255),
//            new Color(215, 95, 0),
//            new Color(215, 95, 95),
//            new Color(215, 95, 135),
//            new Color(215, 95, 175),
//            new Color(215, 95, 215),
//            new Color(215, 95, 255),
//            new Color(215, 135, 0),
//            new Color(215, 135, 95),
//            new Color(215, 135, 135),
//            new Color(215, 135, 175),
//            new Color(215, 135, 215),
//            new Color(215, 135, 255),
//            new Color(215, 175, 0),
//            new Color(215, 175, 95),
//            new Color(215, 175, 135),
//            new Color(215, 175, 175),
//            new Color(215, 175, 215),
//            new Color(215, 175, 255),
//            new Color(215, 215, 0),
//            new Color(215, 215, 95),
//            new Color(215, 215, 135),
//            new Color(215, 215, 175),
//            new Color(215, 215, 215),
//            new Color(215, 215, 255),
//            new Color(215, 255, 0),
//            new Color(215, 255, 95),
//            new Color(215, 255, 135),
//            new Color(215, 255, 175),
//            new Color(215, 255, 215),
//            new Color(215, 255, 255),
//            new Color(255, 0, 0),
//            new Color(255, 0, 95),
//            new Color(255, 0, 135),
//            new Color(255, 0, 175),
//            new Color(255, 0, 215),
//            new Color(255, 0, 255),
//            new Color(255, 95, 0),
//            new Color(255, 95, 95),
//            new Color(255, 95, 135),
//            new Color(255, 95, 175),
//            new Color(255, 95, 215),
//            new Color(255, 95, 255),
//            new Color(255, 135, 0),
//            new Color(255, 135, 95),
//            new Color(255, 135, 135),
//            new Color(255, 135, 175),
//            new Color(255, 135, 215),
//            new Color(255, 135, 255),
//            new Color(255, 175, 0),
//            new Color(255, 175, 95),
//            new Color(255, 175, 135),
//            new Color(255, 175, 175),
//            new Color(255, 175, 215),
//            new Color(255, 175, 255),
//            new Color(255, 215, 0),
//            new Color(255, 215, 95),
//            new Color(255, 215, 135),
//            new Color(255, 215, 175),
//            new Color(255, 215, 215),
//            new Color(255, 215, 255),
//            new Color(255, 255, 0),
//            new Color(255, 255, 95),
//            new Color(255, 255, 135),
//            new Color(255, 255, 175),
//            new Color(255, 255, 215),
//            new Color(255, 255, 255),
//            new Color(8, 8, 8),
//            new Color(18, 18, 18),
//            new Color(28, 28, 28),
//            new Color(38, 38, 38),
//            new Color(48, 48, 48),
//            new Color(58, 58, 58),
//            new Color(68, 68, 68),
//            new Color(78, 78, 78),
//            new Color(88, 88, 88),
//            new Color(98, 98, 98),
//            new Color(108, 108, 108),
//            new Color(118, 118, 118),
//            new Color(128, 128, 128),
//            new Color(138, 138, 138),
//            new Color(148, 148, 148),
//            new Color(158, 158, 158),
//            new Color(168, 168, 168),
//            new Color(178, 178, 178),
//            new Color(188, 188, 188),
//            new Color(198, 198, 198),
//            new Color(208, 208, 208),
//            new Color(218, 218, 218),
//            new Color(228, 228, 228),
//            new Color(238, 238, 238)
//    };
//
//    public static String[] COLOR255_NAMES = new String[]{
//            "Black",
//            "Maroon",
//            "Green",
//            "Olive",
//            "Navy",
//            "Purple",
//            "Teal",
//            "Silver",
//            "Grey",
//            "Red",
//            "Lime",
//            "Yellow",
//            "Blue",
//            "Fuchsia",
//            "Aqua",
//            "White",
//            "Grey0",
//            "NavyBlue",
//            "DarkBlue",
//            "Blue3",
//            "Blue4",
//            "Blue1",
//            "DarkGreen",
//            "DeepSkyBlue4",
//            "DeepSkyBlue5",
//            "DeepSkyBlue6",
//            "DodgerBlue3",
//            "DodgerBlue2",
//            "Green4",
//            "SpringGreen4",
//            "Turquoise4",
//            "DeepSkyBlue3",
//            "DeepSkyBlue7",
//            "DodgerBlue1",
//            "Green3",
//            "SpringGreen3",
//            "DarkCyan",
//            "LightSeaGreen",
//            "DeepSkyBlue2",
//            "DeepSkyBlue1",
//            "Green5",
//            "SpringGreen5",
//            "SpringGreen2",
//            "Cyan3",
//            "DarkTurquoise",
//            "Turquoise2",
//            "Green1",
//            "SpringGreen6",
//            "SpringGreen1",
//            "MediumSpringGreen",
//            "Cyan2",
//            "Cyan1",
//            "DarkRed",
//            "DeepPink4",
//            "Purple4",
//            "Purple5",
//            "Purple3",
//            "BlueViolet",
//            "Orange4",
//            "Grey37",
//            "MediumPurple4",
//            "SlateBlue3",
//            "SlateBlue4",
//            "RoyalBlue1",
//            "Chartreuse4",
//            "DarkSeaGreen4",
//            "PaleTurquoise4",
//            "SteelBlue",
//            "SteelBlue3",
//            "CornflowerBlue",
//            "Chartreuse3",
//            "DarkSeaGreen5",
//            "CadetBlue",
//            "CadetBlue2",
//            "SkyBlue3",
//            "SteelBlue1",
//            "Chartreuse4",
//            "PaleGreen3",
//            "SeaGreen3",
//            "Aquamarine3",
//            "MediumTurquoise",
//            "SteelBlue2",
//            "Chartreuse2",
//            "SeaGreen2",
//            "SeaGreen1",
//            "SeaGreen4",
//            "Aquamarine1",
//            "DarkSlateGray2",
//            "DarkRed2",
//            "DeepPink5",
//            "DarkMagenta",
//            "DarkMagenta2",
//            "DarkViolet",
//            "Purple2",
//            "Orange4",
//            "LightPink4",
//            "Plum4",
//            "MediumPurple3",
//            "MediumPurple3",
//            "SlateBlue1",
//            "Yellow4",
//            "Wheat4",
//            "Grey53",
//            "LightSlateGrey",
//            "MediumPurple",
//            "LightSlateBlue",
//            "Yellow4",
//            "DarkOliveGreen3",
//            "DarkSeaGreen",
//            "LightSkyBlue3",
//            "LightSkyBlue3",
//            "SkyBlue2",
//            "Chartreuse2",
//            "DarkOliveGreen3",
//            "PaleGreen3",
//            "DarkSeaGreen3",
//            "DarkSlateGray3",
//            "SkyBlue1",
//            "Chartreuse1",
//            "LightGreen",
//            "LightGreen",
//            "PaleGreen1",
//            "Aquamarine1",
//            "DarkSlateGray1",
//            "Red3",
//            "DeepPink4",
//            "MediumVioletRed",
//            "Magenta3",
//            "DarkViolet",
//            "Purple3",
//            "DarkOrange3",
//            "IndianRed",
//            "HotPink3",
//            "MediumOrchid3",
//            "MediumOrchid",
//            "MediumPurple2",
//            "DarkGoldenrod",
//            "LightSalmon3",
//            "RosyBrown",
//            "Grey63",
//            "MediumPurple2",
//            "MediumPurple1",
//            "Gold3",
//            "DarkKhaki",
//            "NavajoWhite3",
//            "Grey69",
//            "LightSteelBlue3",
//            "LightSteelBlue",
//            "Yellow3",
//            "DarkOliveGreen3",
//            "DarkSeaGreen3",
//            "DarkSeaGreen2",
//            "LightCyan3",
//            "LightSkyBlue1",
//            "GreenYellow",
//            "DarkOliveGreen2",
//            "PaleGreen1",
//            "DarkSeaGreen2",
//            "DarkSeaGreen1",
//            "PaleTurquoise1",
//            "Red3",
//            "DeepPink3",
//            "DeepPink3",
//            "Magenta3",
//            "Magenta3",
//            "Magenta2",
//            "DarkOrange3",
//            "IndianRed",
//            "HotPink3",
//            "HotPink2",
//            "Orchid",
//            "MediumOrchid1",
//            "Orange3",
//            "LightSalmon3",
//            "LightPink3",
//            "Pink3",
//            "Plum3",
//            "Violet",
//            "Gold3",
//            "LightGoldenrod3",
//            "Tan",
//            "MistyRose3",
//            "Thistle3",
//            "Plum2",
//            "Yellow3",
//            "Khaki3",
//            "LightGoldenrod2",
//            "LightYellow3",
//            "Grey84",
//            "LightSteelBlue1",
//            "Yellow2",
//            "DarkOliveGreen1",
//            "DarkOliveGreen1",
//            "DarkSeaGreen1",
//            "Honeydew2",
//            "LightCyan1",
//            "Red1",
//            "DeepPink2",
//            "DeepPink1",
//            "DeepPink1",
//            "Magenta2",
//            "Magenta1",
//            "OrangeRed1",
//            "IndianRed1",
//            "IndianRed1",
//            "HotPink",
//            "HotPink",
//            "MediumOrchid1",
//            "DarkOrange",
//            "Salmon1",
//            "LightCoral",
//            "PaleVioletRed1",
//            "Orchid2",
//            "Orchid1",
//            "Orange1",
//            "SandyBrown",
//            "LightSalmon1",
//            "LightPink1",
//            "Pink1",
//            "Plum1",
//            "Gold1",
//            "LightGoldenrod2",
//            "LightGoldenrod2",
//            "NavajoWhite1",
//            "MistyRose1",
//            "Thistle1",
//            "Yellow1",
//            "LightGoldenrod1",
//            "Khaki1",
//            "Wheat1",
//            "Cornsilk1",
//            "Grey100",
//            "Grey3",
//            "Grey7",
//            "Grey11",
//            "Grey15",
//            "Grey19",
//            "Grey23",
//            "Grey27",
//            "Grey30",
//            "Grey35",
//            "Grey39",
//            "Grey42",
//            "Grey46",
//            "Grey50",
//            "Grey54",
//            "Grey58",
//            "Grey62",
//            "Grey66",
//            "Grey70",
//            "Grey74",
//            "Grey78",
//            "Grey82",
//            "Grey85",
//            "Grey89",
//            "Grey93"
//    };
//
//    public static String extractName(String a) {
//        if (a.startsWith("Grey")) {
//            return a;
//        }
//        if (Character.isDigit(a.charAt(a.length() - 1))) {
//            int x = a.length();
//            while (x > 0 && Character.isDigit(a.charAt(x - 1))) {
//                x--;
//            }
//            return a.substring(0, x);
//        }
//        return a;
//    }
//
//    public static void main(String[] args) {
//        Map<String, Integer> visited = new HashMap<>();
//        String[] newNames = new String[COLOR255_NAMES.length];
//        for (int i = 0; i < 256; i++) {
//            String name0 = COLOR255_NAMES[i];
//            String c = extractName(name0);
//            if (visited.containsKey(c)) {
//                Integer n = visited.get(c);
//                n++;
//                visited.put(c, n);
//                newNames[i] = c + n;
//            } else {
//                visited.put(c, 1);
//                newNames[i] = c;
//            }
//        }
//        for (int i = 0; i < 256; i++) {
//            Color c = COLOR255_VALUES[i];
//            System.out.println(
//                    "public static final NamedColor " + toConstName(newNames[i]) + "=new NamedColor(\"" + newNames[i] + "\",new Color(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "));"
//            );
//        }
//        System.out.println("public static final List<NamedColor> ALL=Arrays.asList(new NamedColor[]{");
//        for (int i = 0; i < 256; i++) {
//            Color c = COLOR255_VALUES[i];
//            System.out.println(
//                    "" + toConstName(newNames[i]) + (i == 255 ? "" : ",")
//            );
//        }
//        System.out.println("}");
//    }
//
//    public static String toConstName(String s) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < s.length(); i++) {
//            if (i == 0) {
//                sb.append(Character.toUpperCase(s.charAt(i)));
//            } else {
//                if (Character.isDigit(s.charAt(i)) && !Character.isDigit(s.charAt(i - 1))) {
//                    sb.append('_');
//                    sb.append(s.charAt(i));
//                } else if (Character.isUpperCase(s.charAt(i)) && !Character.isUpperCase(s.charAt(i - 1))) {
//                    sb.append('_');
//                    sb.append(s.charAt(i));
//                } else {
//                    sb.append(Character.toUpperCase(s.charAt(i)));
//                }
//            }
//        }
//        return sb.toString();
//    }
}
