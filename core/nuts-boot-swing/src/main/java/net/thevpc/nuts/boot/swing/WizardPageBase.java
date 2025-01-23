package net.thevpc.nuts.boot.swing;

import javax.swing.*;
import java.awt.*;

public class WizardPageBase extends JPanel {
    private Wizard installerContext;
    private int pageIndex;
    public WizardPageBase(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public WizardPageBase(LayoutManager layout) {
        super(layout);
    }

    public WizardPageBase(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public WizardPageBase() {
    }

    public void onAdd(Wizard installerContext, int pageIndex){
        this.installerContext=installerContext;
        this.pageIndex=pageIndex;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public Wizard getInstallerContext() {
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
