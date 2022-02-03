package net.thevpc.nuts.installer.util;

import java.awt.*;

public class GBC extends GridBagConstraints {


    public int getGridx() {
        return gridx;
    }

    public GBC nextLine() {
        this.gridx=0;
        this.gridy++;
        return this;
    }

    public GBC setGrid(int gridx, int gridy) {
        this.gridx=gridx;
        this.gridy=gridy;
        return this;
    }
    public GBC setGridx(int gridx) {
        this.gridx = gridx;
        return this;
    }

    public int getGridy() {
        return gridy;
    }

    public GBC setGridy(int gridy) {
        this.gridy = gridy;
        return this;
    }

    public int getGridwidth() {
        return gridwidth;
    }

    public GBC setGridwidth(int gridwidth) {
        this.gridwidth = gridwidth;
        return this;
    }

    public int getGridheight() {
        return gridheight;
    }

    public GBC setGridheight(int gridheight) {
        this.gridheight = gridheight;
        return this;
    }

    public double getWeightx() {
        return weightx;
    }

    public GBC setWeight(double weightx, double weighty) {
        this.weightx = weightx;
        this.weighty = weighty;
        return this;
    }

    public GBC setWeightx(double weightx) {
        this.weightx = weightx;
        return this;
    }

    public double getWeighty() {
        return weighty;
    }

    public GBC setWeighty(double weighty) {
        this.weighty = weighty;
        return this;
    }

    public int getAnchor() {
        return anchor;
    }

    public GBC setAnchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    public int getFill() {
        return fill;
    }

    public GBC setFill(int fill) {
        this.fill = fill;
        return this;
    }

    public Insets getInsets() {
        return insets;
    }

    public GBC setInsets(int top, int left, int bottom, int right) {
        return setInsets(new Insets(top, left, bottom, right));
    }

    public GBC setInsets(Insets insets) {
        this.insets = insets;
        return this;
    }

    public int getIpadx() {
        return ipadx;
    }

    public GBC setIpadx(int ipadx) {
        this.ipadx = ipadx;
        return this;
    }

    public int getIpady() {
        return ipady;
    }

    public GBC setIpady(int ipady) {
        this.ipady = ipady;
        return this;
    }
}
