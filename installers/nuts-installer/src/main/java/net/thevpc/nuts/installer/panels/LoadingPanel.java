package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.nswing.UIHelper;
import net.thevpc.nuts.nswing.WizardPageBase;

import javax.swing.*;
import java.awt.*;

public class LoadingPanel extends WizardPageBase {
    JProgressBar progressBar;
    public LoadingPanel() {
        super(new BorderLayout());
        add(UIHelper.titleLabel("Please wait, we are loading..."),BorderLayout.PAGE_START);
        Box b = Box.createVerticalBox();
        progressBar = new JProgressBar();
        b.add(progressBar);
        add(b,BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        progressBar.setIndeterminate(true);
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
    }

    @Override
    public void onHide() {
        progressBar.setIndeterminate(false);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }
}
