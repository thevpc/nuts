package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.InstallerContext;

import javax.swing.*;
import java.awt.*;

public class AbstractInstallPanel extends JPanel {
    private InstallerContext installerContext;
    private int pageIndex;
    public AbstractInstallPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public AbstractInstallPanel(LayoutManager layout) {
        super(layout);
    }

    public AbstractInstallPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public AbstractInstallPanel() {
    }

    public void onAdd(InstallerContext installerContext,int pageIndex){
        this.installerContext=installerContext;
        this.pageIndex=pageIndex;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public InstallerContext getInstallerContext() {
        return installerContext;
    }

    public void onPrevious(){

    }
    public void onNext(){

    }
    public void onShow(){

    }

    public void onHide(){

    }

    public void onCancel() {

    }
}
