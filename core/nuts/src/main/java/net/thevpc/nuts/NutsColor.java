package net.thevpc.nuts;

public class NutsColor {
    private int type;
    private int color;

    public NutsColor(int type, int color) {
        this.type = type;
        this.color = color;
    }

    public static NutsColor of4(int color) {
        return new NutsColor(4, color);
    }

    public static NutsColor of8(int color) {
        return new NutsColor(8, color);
    }

    public static NutsColor of24(int color) {
        return new NutsColor(24, color);
    }

    public int getType() {
        return type;
    }

    public int getColor() {
        return color;
    }
}
