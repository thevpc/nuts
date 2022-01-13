package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.InstallData;
import net.thevpc.nuts.installer.util.UIHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ConfigurePanel extends AbstractInstallPanel{
    JTextField wsField;
    public ConfigurePanel() {
        super(new BorderLayout());
        add(UIHelper.titleLabel("Please select installation options"),BorderLayout.PAGE_START);
        Box vbox = Box.createVerticalBox();
        vbox.add(Box.createRigidArea(new Dimension(1,30)));
        JCheckBox optionZReset = new JCheckBox("Reset Workspace (-Z)");
        JCheckBox optionSStandalone = new JCheckBox("Standalone Mode (-S)");
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
        vbox.add(optionZReset);
        vbox.add(Box.createRigidArea(new Dimension(1,30)));
        vbox.add(optionSStandalone);
        vbox.add(Box.createRigidArea(new Dimension(1,30)));
        vbox.add(new JLabel("Custom Workspace"));
        vbox.add(Box.createRigidArea(new Dimension(1,10)));
        wsField = new JTextField();
        vbox.add(wsField);
        wsField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                InstallData.of(getInstallerContext()).workspace=wsField.getText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                InstallData.of(getInstallerContext()).workspace=wsField.getText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                InstallData.of(getInstallerContext()).workspace=wsField.getText();
            }
        });
        vbox.add(new JLabel("Other Options"));
        vbox.add(Box.createRigidArea(new Dimension(1,10)));
        vbox.add(new JScrollPane(new JTextArea("")));
        vbox.add(Box.createRigidArea(new Dimension(1,30)));
        add(UIHelper.margins(vbox,10));
    }
    @Override
    public void onShow() {
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
    }

}
