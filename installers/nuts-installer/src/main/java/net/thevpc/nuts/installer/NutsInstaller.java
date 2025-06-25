package net.thevpc.nuts.installer;

import com.formdev.flatlaf.FlatLightLaf;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.panels.*;
import net.thevpc.nuts.nswing.UIHelper;
import net.thevpc.nuts.nswing.WizardBase;
import net.thevpc.nuts.nswing.WizardPageBase;

import javax.swing.*;
import java.awt.*;

public class NutsInstaller extends WizardBase {
    private final static String VERSION = "0.8.6.0";
    private final LoadingPanel loading = new LoadingPanel();

    public NutsInstaller() {
        setFrameTitle("Nuts Package Manager Installer - " + VERSION);
        setFrameIconImage(new ImageIcon(NutsInstaller.class.getResource("nuts-icon.png")).getImage());
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI(args));
    }

    private static void createAndShowGUI(String[] args) {
        FlatLightLaf.setup();
//        FlatDarkLaf.setup();
        NutsInstaller mi = new NutsInstaller();

        InstallData.of(mi).setCmdline(args);
        doParseCommandLine(args, InstallData.of(mi));
        mi.showFrame();
//        frame.setResizable(false);
    }

    protected JComponent createLeftImpl() {
        ImageIcon icon = new ImageIcon(getClass().getResource("nuts-logo.png"));
        int w = 220;
        Image img2 = UIHelper.getFixedSizeImage(icon.getImage(), w, -1, true);
        icon = new ImageIcon(img2);
        JLabel label = new JLabel(icon);
        label.setOpaque(true);
        return label;
    }

    private static void doParseCommandLine(String[] args, InstallData installData) {
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("-")) {
                String optionKey = null;
                String optionValue = null;
                int x = a.indexOf("=");
                if (x < 0) {
                    optionKey = a;
                } else {
                    optionKey = a.substring(0, x);
                    optionValue = a.substring(x + 1);
                }
                if (optionKey.startsWith("--")) {
                    processOption(optionKey, optionValue, installData);
                } else if (optionKey.equals("-version")) {
                    //just ignore
                } else {
                    char[] u = optionKey.substring(1).toCharArray();
                    for (int j = 0; j < u.length; j++) {
                        char c = u[j];
                        if (j == u.length - 1) {
                            processOption("-" + c, optionValue, installData);
                        } else {
                            processOption("-" + c, null, installData);
                        }
                    }
                }
            }
        }
    }

    private static void processOption(String optionKey, String optionValue, InstallData installData) {
        switch (optionKey) {
            case "--workspace":
            case "-w": {
                installData.setDefaultWorkspace(optionValue);
                break;
            }
            case "--verbose": {
                installData.setDefaultVerbose(!"false".equals(optionValue));
                break;
            }
            case "--log-file-verbose": {
                installData.setDefaultVerboseFile(!"false".equals(optionValue));
                break;
            }
            case "-S":
            case "--standalone": {
                installData.setDefaultStandalone(!"false".equals(optionValue));
                break;
            }
            case "--switch": {
                installData.setDefaultSwitch(!"false".equals(optionValue));
                break;
            }
            case "-Z": {
                installData.setDefaultReset(!"false".equals(optionValue));
                break;
            }
            case "--nuts-options": {
                installData.setDefaultNutsOptions(optionValue);
                break;
            }
            case "--dark-mode": {
                installData.setDarkMode(!"false".equals(optionValue));
                break;
            }
            case "--accept-terms": {
                installData.setDefaultAcceptTerms(!"false".equals(optionValue));
                break;
            }
            case "--java-home": {
                installData.setDefaultJavaHome(optionValue);
                break;
            }
        }
    }

    @Override
    protected WizardPageBase loadingPage() {
        return loading;
    }

    @Override
    protected void createCenterImpl() {
        addPanel(new DarkModePanel());
//        addPanel(new TestTermPanel());
        addPanel(new IntroductionPanel());
        addPanel(new LicensePanel());
        addPanel(new VersionsPanel());
        addPanel(new PackagesPanel());
        addPanel(new ConfigurePanel());
        addPanel(new JavaPanel());
        addPanel(new ProcessPanel());
        addPanel(new SummaryPanel());
    }

}
