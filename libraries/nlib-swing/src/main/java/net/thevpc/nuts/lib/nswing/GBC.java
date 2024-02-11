package net.thevpc.nuts.lib.nswing;

import java.awt.*;

public class GBC {
    GridBagConstraints b = new GridBagConstraints();

    public static GBC of(int x, int y) {
        return of().at(x, y);
    }

    public static GBC ofAt(int x, int y) {
        return of().at(x, y);
    }

    public GBC ipad(int x, int y) {
        b.ipadx = x;
        b.ipady = y;
        return this;
    }

    public GBC weightx(int x) {
        b.weightx = x;
        return this;
    }

    public GBC weighty(int y) {
        b.weighty = y;
        return this;
    }

    public GBC weight(int x) {
        return weight(x,x);
    }

    public GBC weight(int x, int y) {
        b.weightx = x;
        b.weighty = y;
        return this;
    }

    public GBC at(int x, int y) {
        b.gridx = x;
        b.gridy = y;
        return this;
    }

    public static GBC of() {
        return new GBC();
    }

    public GBC fillNone() {
        b.fill = GridBagConstraints.NONE;
        return this;
    }

    public GBC fillVertical() {
        b.fill = GridBagConstraints.VERTICAL;
        return this;
    }

    public GBC fillHorizontal() {
        b.fill = GridBagConstraints.HORIZONTAL;
        return this;
    }

    public GBC fillBoth() {
        b.fill = GridBagConstraints.BOTH;
        return this;
    }

    public GridBagConstraints build() {
        return b;
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

    public GBC anchor(int a) {
        this.b.anchor = a;
        return this;
    }

    public GBC colspanReminder() {
        b.gridwidth = GridBagConstraints.REMAINDER;
        return this;
    }

    public GBC colspanRelative() {
        b.gridwidth = GridBagConstraints.RELATIVE;
        return this;
    }

    public GBC colspan(int c) {
        b.gridwidth = c;
        return this;
    }

    public GBC rowspan(int c) {
        b.gridheight = c;
        return this;
    }

    public GBC rowspanReminder() {
        b.gridheight = GridBagConstraints.REMAINDER;
        return this;
    }

    public GBC rowspanRelative() {
        b.gridheight = GridBagConstraints.RELATIVE;
        return this;
    }

    public GBC insets(int v, int h) {
        return insets(new Insets(v, h, v, h));
    }

    public GBC insets(int top,int left,int bottom,int right) {
        return insets(new Insets(top, left, bottom, right));
    }

    public GBC insets(int i) {
        return insets(new Insets(i, i, i, i));
    }

    public GBC insets(Insets i) {
        b.insets = i;
        return this;
    }

}
