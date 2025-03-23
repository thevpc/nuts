package net.thevpc.nuts.installer.panels.test;

import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.nswing.WizardPageBase;
import net.thevpc.nuts.nswing.UIHelper;
import net.thevpc.nuts.nswing.AnsiTermPane;

import javax.swing.*;
import java.awt.*;

public class TestTermPanel extends WizardPageBase {
    AnsiTermPane ansiTermPane;

    public TestTermPanel() {
        super(new BorderLayout());
        add(UIHelper.titleLabel("Please wait until the installation is complete"), BorderLayout.PAGE_START);
        ansiTermPane = new AnsiTermPane(false);
        add(new JScrollPane(ansiTermPane), BorderLayout.CENTER);
    }


    @Override
    public void onShow() {
        ansiTermPane.setDarkMode(InstallData.of(getInstallerContext()).darkMode);
        ansiTermPane.clearScreen();
        for (int i = 0; i < 20; i++) {
            ansiTermPane.append(i,"COLOR "+i+"\n");
        }
    }
}
