package net.thevpc.nuts.installer.util;

import net.thevpc.nuts.installer.model.ButtonColorSet;
import net.thevpc.nuts.installer.model.StatusButtonColorSet;

import java.awt.*;

public class InstallPalette {
    public static final String THEME_DARK_CONTAINER_COLOR = "#3b3e40";
    public static final String THEME_LIGHT_CONTAINER_COLOR = "#f0f0f0";
    public static StatusButtonColorSet standardLight = new StatusButtonColorSet(
            new ButtonColorSet(new Color(0xE8, 0xE8, 0xE8), new Color(0x55, 0x55, 0x55)), // normal
            new ButtonColorSet(new Color(0xF7, 0xC9, 0x7A), new Color(0x6B, 0x3F, 0x00)), // selected ★
            new ButtonColorSet(new Color(0xF0, 0xF0, 0xF0), new Color(0xAA, 0xAA, 0xAA)), // disabled
            new ButtonColorSet(new Color(0xF5, 0xE0, 0xB0), new Color(0x8B, 0x5E, 0x2C))  // hover
    );

    public static StatusButtonColorSet standardDark = new StatusButtonColorSet(
            new ButtonColorSet(new Color(0x55, 0x5A, 0x5D), new Color(0xAA, 0xAA, 0xAA)), // normal
            new ButtonColorSet(new Color(0xE8, 0x94, 0x0A), Color.WHITE),                  // selected ★ — option A
            new ButtonColorSet(new Color(0x42, 0x45, 0x47), new Color(0x77, 0x77, 0x77)), // disabled
            new ButtonColorSet(new Color(0xD4, 0x69, 0x1E), Color.WHITE)                   // hover — option C selected
    );


    public static StatusButtonColorSet ltsLight = new StatusButtonColorSet(
            new ButtonColorSet(new Color(0xE8, 0xE8, 0xE8), new Color(0x55, 0x55, 0x55)), // normal
            new ButtonColorSet(new Color(0x7A, 0xBF, 0x96), new Color(0x0D, 0x3B, 0x26)), // selected ★
            new ButtonColorSet(new Color(0xF0, 0xF0, 0xF0), new Color(0xAA, 0xAA, 0xAA)), // disabled
            new ButtonColorSet(new Color(0xC0, 0xE0, 0xCC), new Color(0x1E, 0x5A, 0x3E))  // hover
    );

    public static StatusButtonColorSet ltsDark = new StatusButtonColorSet(
            new ButtonColorSet(new Color(0x55, 0x5A, 0x5D), new Color(0xAA, 0xAA, 0xAA)), // normal
            new ButtonColorSet(new Color(0x2E, 0x7A, 0x54), Color.WHITE),                  // selected ★
            new ButtonColorSet(new Color(0x42, 0x45, 0x47), new Color(0x77, 0x77, 0x77)), // disabled
            new ButtonColorSet(new Color(0x3A, 0x6A, 0x4E), new Color(0xC0, 0xE8, 0xC0))  // hover
    );

    public static StatusButtonColorSet errorLight = new StatusButtonColorSet(
            new ButtonColorSet(new Color(0xE8, 0xE8, 0xE8), new Color(0x55, 0x55, 0x55)), // normal
            new ButtonColorSet(new Color(0xE0, 0x70, 0x70), Color.WHITE),                  // selected ★
            new ButtonColorSet(new Color(0xF0, 0xF0, 0xF0), new Color(0xAA, 0xAA, 0xAA)), // disabled
            new ButtonColorSet(new Color(0xF5, 0xC8, 0xC8), new Color(0xB3, 0x3A, 0x3A))  // hover
    );

    public static StatusButtonColorSet errorDark = new StatusButtonColorSet(
            new ButtonColorSet(new Color(0x55, 0x5A, 0x5D), new Color(0xAA, 0xAA, 0xAA)), // normal
            new ButtonColorSet(new Color(0x8B, 0x40, 0x40), Color.WHITE),                  // selected ★
            new ButtonColorSet(new Color(0x42, 0x45, 0x47), new Color(0x77, 0x77, 0x77)), // disabled
            new ButtonColorSet(new Color(0x6E, 0x3A, 0x3A), new Color(0xF5, 0xB8, 0xB8))  // hover
    );

    // Light button on light parent — selected is PRIMARY
    public static final StatusButtonColorSet themeLightButtonLightTheme = new StatusButtonColorSet(
            new ButtonColorSet(new Color(0xE8, 0xE8, 0xE8), new Color(0x55, 0x55, 0x55)), // normal
            new ButtonColorSet(new Color(0xA8, 0xCB, 0xF0), new Color(0x0D, 0x3D, 0x7A)), // selected ★
            new ButtonColorSet(new Color(0xF0, 0xF0, 0xF0), new Color(0xAA, 0xAA, 0xAA)), // disabled
            new ButtonColorSet(new Color(0xC8, 0xDE, 0xF5), new Color(0x1A, 0x56, 0xA0))  // hover — light blue hint
    );

    // Light button on dark parent — selected is PHANTOM
    public static final StatusButtonColorSet themeLightButtonDarkTheme = new StatusButtonColorSet(
            new ButtonColorSet(new Color(0x55, 0x5A, 0x5D), new Color(0xAA, 0xAA, 0xAA)), // normal
            new ButtonColorSet(new Color(0x1A, 0x72, 0xC0), Color.WHITE),                  // selected (phantom)
            new ButtonColorSet(new Color(0x42, 0x45, 0x47), new Color(0x77, 0x77, 0x77)), // disabled
            new ButtonColorSet(new Color(0x2E, 0x4A, 0x68), new Color(0xA8, 0xC4, 0xE0))  // hover — dark blue hint
    );

    // Dark button on light parent — selected is PHANTOM
    public static final StatusButtonColorSet themeDarkButtonLightTheme = new StatusButtonColorSet(
            new ButtonColorSet(new Color(0xE8, 0xE8, 0xE8), new Color(0x55, 0x55, 0x55)), // normal
            new ButtonColorSet(new Color(0x1A, 0x4A, 0x80), new Color(0xC0, 0xD8, 0xF0)), // selected (phantom)
            new ButtonColorSet(new Color(0xF0, 0xF0, 0xF0), new Color(0xAA, 0xAA, 0xAA)), // disabled
            new ButtonColorSet(new Color(0xC8, 0xDE, 0xF5), new Color(0x1A, 0x56, 0xA0))  // hover — light blue hint
    );

    // Dark button on dark parent — selected is PRIMARY
    public static final StatusButtonColorSet themeDarkButtonDarkTheme = new StatusButtonColorSet(
            new ButtonColorSet(new Color(0x55, 0x5A, 0x5D), new Color(0xAA, 0xAA, 0xAA)), // normal
            new ButtonColorSet(new Color(0x1A, 0x4A, 0x80), new Color(0xC0, 0xD8, 0xF0)), // selected ★
            new ButtonColorSet(new Color(0x42, 0x45, 0x47), new Color(0x77, 0x77, 0x77)), // disabled
            new ButtonColorSet(new Color(0x2E, 0x4A, 0x68), new Color(0xA8, 0xC4, 0xE0))  // hover — dark blue hint
    );
}
