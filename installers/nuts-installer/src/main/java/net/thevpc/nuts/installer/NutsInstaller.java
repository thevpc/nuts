package net.thevpc.nuts.installer;

import com.formdev.flatlaf.FlatLightLaf;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.panels.*;
import net.thevpc.nuts.installer.util.NutsInstallerProfiler;
import net.thevpc.nuts.installer.util.swing.*;

import javax.swing.*;
import java.awt.*;

public class NutsInstaller extends WizardBase {
    private final static String VERSION = "0.8.9.0";
    public static final Color TAGLINE_COLOR_LIGHT_MODE = new Color(0x2A, 0xB5, 0x9A);
    public static final Color TAGLINE_COLOR_DARK_MODE = new Color(0x2A, 0xB5, 0x9A).darker().darker();
    public static final Color URL_COLOR_LIGHT_MODE = new Color(0x2A, 0xB5, 0x9A);
    public static final Color URL_COLOR_DARK_MODE = new Color(0x2A, 0xB5, 0x9A).darker().darker();
    private final LoadingPanel loading = new LoadingPanel();
    Color HINT_LABEL_COLOR_DARK_MODE = new Color(0x302A2A);
    Color HINT_LABEL_COLOR_LIGHT_MODE = new Color(26, 34, 33, 95).darker();
    private JLabel hintLabel;
    private JLabel taglineLabel;
    private JLabel urlLabel;

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
        InstallData id = InstallData.of(mi);
        id.setCmdline(args);
        doParseCommandLine(args, id);
        mi.showFrame();
        if(id.isBuildNativeProfiling()){
            new NutsInstallerProfiler(mi).run();
        }
//        frame.setResizable(false);
    }

    protected JComponent createLeftImpl() {
        ImageIcon icon = new ImageIcon(getClass().getResource("nuts-logo.png"));
        int w = 220;
        Image img2 = UIHelper.getFixedSizeImage(icon.getImage(), w, -1, true);
        icon = new ImageIcon(img2);
        boolean dark = InstallData.of(NutsInstaller.this).isDarkMode();

        JLabel logoLabel = new JLabel(icon);
        logoLabel.setOpaque(false);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        taglineLabel = new JLabel("Managing Network Updatable Things");
        taglineLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        taglineLabel.setForeground(dark?TAGLINE_COLOR_DARK_MODE:TAGLINE_COLOR_LIGHT_MODE); // teal brand color
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        urlLabel = new JLabel("https://thevpc.github.io/nuts");
        urlLabel.setFont(new Font("SansSerif", Font.PLAIN, 17));
        urlLabel.setForeground(dark?URL_COLOR_DARK_MODE:URL_COLOR_LIGHT_MODE);
        urlLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        content.add(logoLabel);
        content.add(Box.createVerticalStrut(28)); // Increased strut for better breathing room
        content.add(taglineLabel);
        content.add(Box.createVerticalStrut(14));
        content.add(urlLabel);

        // --- NEW FOOTER SECTION ---
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 10));

        hintLabel = new JLabel("Try: nuts search <package>");
        // Monospaced font makes it feel like a terminal command
        hintLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        hintLabel.setForeground(new Color(26, 34, 33, 95).darker());
        hintLabel.setForeground(dark? HINT_LABEL_COLOR_LIGHT_MODE: HINT_LABEL_COLOR_DARK_MODE.darker());
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        hintLabel.setToolTipText("Run this command in your terminal after installation");
        footer.add(hintLabel);
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                boolean dark = InstallData.of(NutsInstaller.this).isDarkMode();
                Color left  = dark ? new Color(0x91989A) : new Color(0xE8, 0xF4, 0xF2);
                Color right = dark ? new Color(0x91989A) : new Color(0xF0, 0xF0, 0xF0);
                GradientPaint gp = new GradientPaint(0, 0, left, getWidth(), 0, right);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(content, BorderLayout.NORTH);
        wrapper.add(footer, BorderLayout.SOUTH); // Anchors the hint to the bottom
        wrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0x2A, 0xB5, 0x9A, 80)));
        return wrapper;
    }

    @Override
    protected void applyPlafLeftSidebar() {
        boolean dark = InstallData.of(NutsInstaller.this).isDarkMode();
        hintLabel.setForeground(dark? HINT_LABEL_COLOR_DARK_MODE:HINT_LABEL_COLOR_LIGHT_MODE);
        taglineLabel.setForeground(dark?TAGLINE_COLOR_DARK_MODE:TAGLINE_COLOR_LIGHT_MODE); // teal brand color
        urlLabel.setForeground(dark?URL_COLOR_DARK_MODE:URL_COLOR_LIGHT_MODE);
        super.applyPlafLeftSidebar();
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
            case "-l":
            case "--verbose":
            {
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
            case "--build-native-profiling": {
                installData.setBuildNativeProfiling(!"false".equals(optionValue));
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
