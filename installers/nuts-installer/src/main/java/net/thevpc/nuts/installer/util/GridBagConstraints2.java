package net.thevpc.nuts.installer.util;

import java.awt.*;

public class GridBagConstraints2 extends GridBagConstraints {


    public int getGridx() {
        return gridx;
    }

    public GridBagConstraints2 setGrid(int gridx,int gridy) {
        this.gridx=gridx;
        this.gridy=gridy;
        return this;
    }
    public GridBagConstraints2 setGridx(int gridx) {
        this.gridx = gridx;
        return this;
    }

    public int getGridy() {
        return gridy;
    }

    public GridBagConstraints2 setGridy(int gridy) {
        this.gridy = gridy;
        return this;
    }

    public int getGridwidth() {
        return gridwidth;
    }

    public GridBagConstraints2 setGridwidth(int gridwidth) {
        this.gridwidth = gridwidth;
        return this;
    }

    public int getGridheight() {
        return gridheight;
    }

    public GridBagConstraints2 setGridheight(int gridheight) {
        this.gridheight = gridheight;
        return this;
    }

    public double getWeightx() {
        return weightx;
    }

    public GridBagConstraints2 setWeightx(double weightx) {
        this.weightx = weightx;
        return this;
    }

    public double getWeighty() {
        return weighty;
    }

    public GridBagConstraints2 setWeighty(double weighty) {
        this.weighty = weighty;
        return this;
    }

    public int getAnchor() {
        return anchor;
    }

    public GridBagConstraints2 setAnchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    public int getFill() {
        return fill;
    }

    public GridBagConstraints2 setFill(int fill) {
        this.fill = fill;
        return this;
    }

    public Insets getInsets() {
        return insets;
    }

    public GridBagConstraints2 setInsets(Insets insets) {
        this.insets = insets;
        return this;
    }

    public int getIpadx() {
        return ipadx;
    }

    public GridBagConstraints2 setIpadx(int ipadx) {
        this.ipadx = ipadx;
        return this;
    }

    public int getIpady() {
        return ipady;
    }

    public GridBagConstraints2 setIpady(int ipady) {
        this.ipady = ipady;
        return this;
    }
}
