package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.nswing.UIHelper;
import net.thevpc.nuts.installer.util.Utils;
import net.thevpc.nuts.nswing.WizardPageBase;

import javax.swing.*;
import java.awt.*;

public class SummaryPanel extends WizardPageBase {
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
        if (!InstallData.of(getInstallerContext()).isInstallFailed()) {
            jep.setText(Utils.loadString("summary-success.html", Utils.getVarsConverter(getInstallerContext())));
        } else {
            jep.setText(Utils.loadString("summary-error.html", Utils.getVarsConverter(getInstallerContext())));
        }
        getInstallerContext().getExitButton().setEnabled(true);
        getInstallerContext().getCancelButton().setEnabled(false);
    }

}
