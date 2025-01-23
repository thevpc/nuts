package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.boot.swing.UIHelper;
import net.thevpc.nuts.installer.util.Utils;
import net.thevpc.nuts.boot.swing.WizardPageBase;

import javax.swing.*;
import java.awt.*;

public class IntroductionPanel extends WizardPageBase {
    JEditorPane jep;
    public IntroductionPanel() {
        super(new BorderLayout());
        add(UIHelper.titleLabel("You are about to install nuts Package Manager"),BorderLayout.PAGE_START);
        jep = new JEditorPane();
        jep.setContentType("text/html");
        jep.setEditable(false);
        add(UIHelper.margins(new JScrollPane(jep),10),BorderLayout.CENTER);
    }
    @Override
    public void onShow() {
        jep.setText(Utils.loadString("introduction.html",Utils.getVarsConverter(getInstallerContext())));
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
    }

}
