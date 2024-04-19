package net.thevpc.nuts.installer.panels.test;

import net.thevpc.nuts.installer.model.App;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.panels.AbstractInstallPanel;
import net.thevpc.nuts.installer.util.AnsiTermPane;
import net.thevpc.nuts.installer.util.StreamGobbler;
import net.thevpc.nuts.installer.util.UIHelper;
import net.thevpc.nuts.installer.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TestTermPanel extends AbstractInstallPanel {
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
