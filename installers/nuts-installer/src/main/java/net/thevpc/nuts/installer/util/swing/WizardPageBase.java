package net.thevpc.nuts.installer.util.swing;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import net.thevpc.nuts.installer.model.ButtonInfo;
import net.thevpc.nuts.installer.model.InstallData;

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

    public void onAdd(Wizard installerContext, int pageIndex) {
        this.installerContext = installerContext;
        this.pageIndex = pageIndex;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public Wizard getInstallerContext() {
        return installerContext;
    }

    public void onPrevious() {

    }

    public void onNext() {

    }

    public void onShow() {

    }

    public void onHide() {

    }

    public void onCancel() {

    }

    public void sendAction(String[] action) {

    }

    public void applyPlaf() {

    }

//    public void applyPlaf() {
//        InstallData id = InstallData.of(getInstallerContext());
////        if (id.darkMode) {
////            FlatDarkLaf.setup();
////        } else {
////            FlatLightLaf.setup();
////        }
//        SwingUtilities.invokeLater(() -> {
//            SwingUtilities.updateComponentTreeUI(this);
//        });
//        getInstallerContext().applyPlaf();
//        getInstallerContext().setDarkMode(id.darkMode);
//    }
}
