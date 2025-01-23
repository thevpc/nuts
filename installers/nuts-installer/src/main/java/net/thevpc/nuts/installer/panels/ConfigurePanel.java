package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.InstallerContext;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.util.GBC;
import net.thevpc.nuts.installer.util.ProcessUtils;
import net.thevpc.nuts.installer.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ConfigurePanel extends AbstractInstallPanel {
    JTextField optionWorkspace;
    JCheckBox optionVerbose;
    JCheckBox optionSwitch;
    JCheckBox optionLogFileVerbose;
    JCheckBox optionZReset;
    JCheckBox optionSStandalone;
    JLabel customWorkspaceLabel;
    JLabel otherOptionsLabel;
    JTextArea otherOptions;

    public ConfigurePanel() {
        super(new BorderLayout());

        optionWorkspace = new JTextField();
        optionVerbose = new JCheckBox("Verbose (--verbose)");
        optionLogFileVerbose = new JCheckBox("Log File (--log-file-verbose)");
        optionZReset = new JCheckBox("Reset Workspace (-Z)");
        optionSStandalone = new JCheckBox("Standalone Mode (-S)");
        optionSwitch = new JCheckBox("Switch Mode (--switch)");
        customWorkspaceLabel = new JLabel("Custom Workspace");
        otherOptionsLabel = new JLabel("Other Options");
        otherOptions = new JTextArea("");

        optionVerbose.setToolTipText("Verbose Mode enable extra trace/logging messages that could be helpful when something does not run as expected.");
        optionLogFileVerbose.setToolTipText("Messages are stored to log file");
        optionZReset.setToolTipText("Reset Workspace will delete any existing installation files.");
        optionSStandalone.setToolTipText("Standalone Mode will force all files to be stored in a single root folder.");
        optionSwitch.setToolTipText("Switch to, as a default workspace.");
        customWorkspaceLabel.setToolTipText("Workspace Name or location");
        optionWorkspace.setToolTipText("Workspace Name or location");
        otherOptionsLabel.setToolTipText("Other options");
        otherOptions.setToolTipText("You can specify here other options that are supported by nuts commandline parser. See documentation for more details.");
        add(UIHelper.titleLabel("Please select installation options"), BorderLayout.PAGE_START);
        JPanel gbox = new JPanel(new GridBagLayout());
        GBC gc = new GBC().setAnchor(GridBagConstraints.NORTHWEST)
                .setFill(GridBagConstraints.HORIZONTAL)
                .setInsets(5, 5, 5, 5);
        gbox.add(optionZReset, gc.setGrid(0, 0));
        gbox.add(optionSStandalone, gc.nextLine());
        gbox.add(optionSwitch, gc.nextLine());
        gbox.add(optionVerbose, gc.nextLine());
        gbox.add(optionLogFileVerbose, gc.nextLine());
        gbox.add(customWorkspaceLabel, gc.nextLine());
        gbox.add(optionWorkspace, gc.nextLine());
        gbox.add(otherOptionsLabel, gc.nextLine());
        gbox.add(new JScrollPane(otherOptions), gc.nextLine().setFill(GridBagConstraints.BOTH).setWeight(1, 1));
        add(UIHelper.margins(gbox, 10));
    }

    @Override
    public void onAdd(InstallerContext installerContext, int pageIndex) {
        super.onAdd(installerContext, pageIndex);
        resetDefaults();
    }

    private void resetDefaults() {
        InstallData id = InstallData.of(getInstallerContext());
        optionSwitch.setSelected(id.isDefaultSwitch());
        optionVerbose.setSelected(id.isDefaultVerbose());
        optionLogFileVerbose.setSelected(id.isDefaultVerboseFile());
        optionZReset.setSelected(id.isDefaultReset());
        optionSStandalone.setSelected(id.isDefaultStandalone());
        optionWorkspace.setText(id.getDefaultWorkspace()==null?"":id.getDefaultWorkspace());
        otherOptions.setText(id.getDefaultNutsOptions());
    }

    @Override
    public void onNext() {
        InstallData id = InstallData.of(getInstallerContext());
        id.optionZ = optionZReset.isSelected();
        id.optionS = optionSStandalone.isSelected();
        id.optionVerbose = optionVerbose.isSelected();
        id.optionVerboseFile = optionLogFileVerbose.isSelected();
        id.optionSwitch = optionSwitch.isSelected();
        if (optionWorkspace.getText().trim().length() > 0) {
            id.workspace = optionWorkspace.getText().trim();
        }
        id.otherOptions.addAll(Arrays.asList(ProcessUtils.parseCmdLine(otherOptions.getText())));
        super.onNext();
    }

    @Override
    public void onShow() {
        resetDefaults();
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
    }
}
