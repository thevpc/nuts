package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.InstallData;
import net.thevpc.nuts.installer.util.GridBagConstraints2;
import net.thevpc.nuts.installer.util.ProcessUtils;
import net.thevpc.nuts.installer.util.UIHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigurePanel extends AbstractInstallPanel{
    JTextField optionWorkspace;
    JCheckBox optionKskip;
    JCheckBox optionZReset;
    JCheckBox optionSStandalone;
    JLabel customWorkspaceLabel;
    JLabel otherOptionsLabel;
    JTextArea otherOptions;
    public ConfigurePanel() {
        super(new BorderLayout());

        optionWorkspace = new JTextField();
        optionKskip = new JCheckBox("Skip Companions (-k)");
        optionZReset = new JCheckBox("Reset Workspace (-Z)");
        optionSStandalone = new JCheckBox("Standalone Mode (-S)");
        customWorkspaceLabel = new JLabel("Custom Workspace");
        otherOptionsLabel = new JLabel("Other Options");
        otherOptions = new JTextArea("");

        optionKskip.setToolTipText("Skip Companions");
        optionZReset.setToolTipText("Reset Workspace");
        optionSStandalone.setToolTipText("Standalone Mode");
        customWorkspaceLabel.setToolTipText("Workspace Name or location");
        optionWorkspace.setToolTipText("Workspace Name or location");
        otherOptionsLabel.setToolTipText("Other options");
        otherOptions.setToolTipText("Other options");

        optionZReset.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JCheckBox c=(JCheckBox) e.getSource();
                InstallData.of(getInstallerContext()).optionZ=c.isSelected();
            }
        });
        optionSStandalone.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JCheckBox c=(JCheckBox) e.getSource();
                InstallData.of(getInstallerContext()).optionS=c.isSelected();
            }
        });
        optionWorkspace.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                InstallData.of(getInstallerContext()).workspace= optionWorkspace.getText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                InstallData.of(getInstallerContext()).workspace= optionWorkspace.getText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                InstallData.of(getInstallerContext()).workspace= optionWorkspace.getText();
            }
        });

        add(UIHelper.titleLabel("Please select installation options"),BorderLayout.PAGE_START);
        JPanel gbox=new JPanel(new GridBagLayout());
        GridBagConstraints2 gc=new GridBagConstraints2().setAnchor(GridBagConstraints.NORTHWEST)
                .setFill(GridBagConstraints.HORIZONTAL)
                .setInsets(new Insets(5,5,5,5))
                ;
        gbox.add(optionZReset,gc.setGrid(0,0));
        gbox.add(optionKskip,gc.setGrid(0,1));
        gbox.add(optionSStandalone,gc.setGrid(0,2));
        gbox.add(customWorkspaceLabel,gc.setGrid(0,3));
        gbox.add(optionWorkspace,gc.setGrid(0,4));
        gbox.add(otherOptionsLabel,gc.setGrid(0,5));
        gbox.add(new JScrollPane(otherOptions),gc.setGrid(0,6).setFill(GridBagConstraints.BOTH).setWeightx(1).setWeighty(1));
        add(UIHelper.margins(gbox,10));
    }

    @Override
    public void onShow() {
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
    }

    @Override
    public void onNext() {
        InstallData id = InstallData.of(getInstallerContext());
        id.optionk=optionKskip.isSelected();
        id.optionZ=optionKskip.isSelected();
        id.optionS=optionSStandalone.isSelected();
        if(optionWorkspace.getText().trim().length()>0){
            id.workspace=optionWorkspace.getText().trim();
        }
        id.otherOptions.addAll(Arrays.asList(ProcessUtils.parseCommandLine(otherOptions.getText())));
        super.onNext();
    }
}
