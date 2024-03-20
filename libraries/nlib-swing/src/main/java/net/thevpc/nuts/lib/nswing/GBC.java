package net.thevpc.nuts.lib.nswing;

import java.awt.*;

public class GBC {
    private int gridx;
    private int gridy;
    private int gridwidth;
    private int gridheight;

    private double weightx;
    private double weighty;

    private int anchor;
    private int fill;

    private Insets insets;

    private int ipadx;

    private int ipady;

    public GBC() {
        this.reset();
    }

    public static GBC of(int x, int y) {
        return of().at(x, y);
    }

    public static GBC ofAt(int x, int y) {
        return of().at(x, y);
    }

    public GBC ipad(int x, int y) {
        this.ipadx = x;
        this.ipady = y;
        return this;
    }

    public GBC weightx(int x) {
        this.weightx = x;
        return this;
    }

    public GBC weighty(int y) {
        this.weighty = y;
        return this;
    }

    public GBC weight(int x) {
        return weight(x, x);
    }

    public GBC weight(int x, int y) {
        this.weightx = x;
        this.weighty = y;
        return this;
    }

    public GBC at(int x, int y) {
        this.gridx = x;
        this.gridy = y;
        return this;
    }

    public static GBC of() {
        return new GBC();
    }

    public GBC fillNone() {
        this.fill = GridBagConstraints.NONE;
        return this;
    }

    public GBC fillVertical() {
        this.fill = GridBagConstraints.VERTICAL;
        return this;
    }

    public GBC fillHorizontal() {
        this.fill = GridBagConstraints.HORIZONTAL;
        return this;
    }

    public GBC fillBoth() {
        this.fill = GridBagConstraints.BOTH;
        return this;
    }


    public GBC anchorWest() {
        return anchor(GridBagConstraints.WEST);
    }

    public GBC anchorEast() {
        return anchor(GridBagConstraints.EAST);
    }

    public GBC anchorSouth() {
        return anchor(GridBagConstraints.SOUTH);
    }

    public GBC anchorCenter() {
        return anchor(GridBagConstraints.CENTER);
    }

    public GBC anchorNorthWest() {
        return anchor(GridBagConstraints.NORTHWEST);
    }

    public GBC anchorNorthEast() {
        return anchor(GridBagConstraints.NORTHEAST);
    }

    public GBC anchorNorth() {
        return anchor(GridBagConstraints.NORTH);
    }

    public GridBagConstraints moveNextColumn() {
        GridBagConstraints g = build();
        this.nextColumn();
        return g;
    }

    public GridBagConstraints moveNextLine() {
        GridBagConstraints g = build();
        this.nextLine();
        return g;
    }

    public GBC nextColumn() {
        this.gridx += this.gridwidth;
        return this;
    }

    public GBC nextLine() {
        this.gridx = 0;
        this.gridy += this.gridheight;
        return this;
    }

    public GBC anchor(int a) {
        this.anchor = a;
        return this;
    }

    public GBC colspanReminder() {
        this.gridwidth = GridBagConstraints.REMAINDER;
        return this;
    }

    public GBC colspanRelative() {
        this.gridwidth = GridBagConstraints.RELATIVE;
        return this;
    }

    public GBC colspan(int c) {
        this.gridwidth = c;
        return this;
    }

    public GBC rowspan(int c) {
        this.gridheight = c;
        return this;
    }

    public GBC rowspanReminder() {
        this.gridheight = GridBagConstraints.REMAINDER;
        return this;
    }

    public GBC rowspanRelative() {
        this.gridheight = GridBagConstraints.RELATIVE;
        return this;
    }

    public GBC insets(int v, int h) {
        return insets(new Insets(v, h, v, h));
    }

    public GBC insets(int top, int left, int bottom, int right) {
        return insets(new Insets(top, left, bottom, right));
    }

    public GBC insets(int i) {
        return insets(new Insets(i, i, i, i));
    }

    public GBC insets(Insets i) {
        this.insets = i;
        return this;
    }

    ////////////////////////////////////////


    public GridBagConstraints build() {
        GridBagConstraints b = new GridBagConstraints();
        b.gridx = this.gridx;
        b.gridy = this.gridy;
        b.gridwidth = this.gridwidth;
        b.gridheight = this.gridheight;
        b.weightx = this.weightx;
        b.weighty = this.weighty;
        b.anchor = this.anchor;
        b.fill = this.fill;
        b.insets = copyInsets(this.insets);
        b.ipadx = this.ipadx;
        b.ipady = this.ipady;
        return b;
    }

    public GBC reset() {
        this.gridx = 0;
        this.gridy = 0;
//        this.gridx = GridBagConstraints.RELATIVE;
//        this.gridy = GridBagConstraints.RELATIVE;
        this.gridwidth = 1;
        this.gridheight = 1;
        this.weightx = 0;
        this.weighty = 0;
        this.anchor = GridBagConstraints.CENTER;
        this.fill = 0;
        this.insets = new Insets(0, 0, 0, 0);
        this.ipadx = 0;
        this.ipady = 0;
        return this;
    }

    public GBC copy() {
        GBC c2 = new GBC();
        c2.gridx = this.gridx;
        c2.gridy = this.gridy;
        c2.gridwidth = this.gridwidth;
        c2.gridheight = this.gridheight;
        c2.weightx = this.weightx;
        c2.weighty = this.weighty;
        c2.anchor = this.anchor;
        c2.fill = this.fill;
        c2.insets = copyInsets(this.insets);
        c2.ipadx = this.ipadx;
        c2.ipady = this.ipady;
        return c2;
    }

    public GBC set(GridBagConstraints b) {
        if (b != null) {
            this.gridx = b.gridx;
            this.gridy = b.gridy;
            this.gridwidth = b.gridwidth;
            this.gridheight = b.gridheight;
            this.weightx = b.weightx;
            this.weighty = b.weighty;
            this.anchor = b.anchor;
            this.fill = b.fill;
            this.insets = copyInsets(b.insets);
            this.ipadx = b.ipadx;
            this.ipady = b.ipady;
        }
        return this;
    }

    private Insets copyInsets(Insets o) {
        if (o == null) {
            return new Insets(0, 0, 0, 0);
        }
        return (Insets) o.clone();
    }
}
