package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.boot.swing.WizardPageBase;
import net.thevpc.nuts.boot.swing.Wizard;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.boot.swing.UIHelper;
import net.thevpc.nuts.installer.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class LicensePanel extends WizardPageBase {
    JRadioButton accept;
    JRadioButton doNotAccept;

    public LicensePanel() {
        super(new BorderLayout());
        JTextArea a = new JTextArea();
        a.setEditable(false);
        add(UIHelper.titleLabel("Please read the following license agreement carefully"), BorderLayout.PAGE_START);
        add(UIHelper.margins(new JScrollPane(a), 10), BorderLayout.CENTER);
        Box b = Box.createVerticalBox();
        accept = new JRadioButton("I accept the terms of this license agreement.");
        doNotAccept = new JRadioButton("I do not accept the terms of this license agreement.");
        doNotAccept.setSelected(true);
        ButtonGroup bg = new ButtonGroup();
        bg.add(accept);
        bg.add(doNotAccept);
        b.add(Box.createRigidArea(new Dimension(0, 5)));
        b.add(accept);
        b.add(Box.createRigidArea(new Dimension(0, 5)));
        b.add(doNotAccept);
        b.add(Box.createRigidArea(new Dimension(0, 5)));
        add(UIHelper.margins(b, 10), BorderLayout.PAGE_END);
        ItemListener il = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateStatus();
            }
        };
        accept.addItemListener(il);
        doNotAccept.addItemListener(il);
        a.setText(Utils.loadString("license.txt", null));
    }

    private void updateStatus() {
        if(getInstallerContext().getNextButton()==null){
            return;
        }
        getInstallerContext().getNextButton().setEnabled(accept.isSelected() && getInstallerContext().hasNext(getPageIndex()));
    }

    @Override
    public void onAdd(Wizard installerContext, int pageIndex) {
        super.onAdd(installerContext, pageIndex);
        accept.setSelected(InstallData.of(installerContext).isDefaultAcceptTerms());
    }

    @Override
    public void onShow() {
        updateStatus();
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
    }

}
