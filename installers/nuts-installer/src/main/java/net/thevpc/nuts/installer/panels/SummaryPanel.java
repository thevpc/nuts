package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.InstallData;
import net.thevpc.nuts.installer.util.UIHelper;
import net.thevpc.nuts.installer.util.Utils;

import javax.swing.*;
import java.awt.*;

public class SummaryPanel extends AbstractInstallPanel {
    JEditorPane jep;

    public SummaryPanel() {
        super(new BorderLayout());
        add(UIHelper.titleLabel("Thank you for you patience, this is the installation summary"), BorderLayout.PAGE_START);
        jep = new JEditorPane();
        jep.setContentType("text/html");
        jep.setEditable(false);
        add(new JScrollPane(jep));
    }

    @Override
    public void onShow() {
        if (!getInstallerContext().isInstallFailed()) {
            jep.setText(Utils.loadString("summary-success.html", Utils.toMSS(getInstallerContext().getVars())));
        } else {
            jep.setText(Utils.loadString("summary-error.html", Utils.toMSS(getInstallerContext().getVars())));
        }
        getInstallerContext().getExitButton().setEnabled(true);
        getInstallerContext().getCancelButton().setEnabled(false);
    }

}
