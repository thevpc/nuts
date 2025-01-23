package net.thevpc.nuts.installer;

import com.formdev.flatlaf.FlatLightLaf;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.panels.*;
import net.thevpc.nuts.installer.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NutsInstaller implements InstallerContext {
    private final static String VERSION = "0.8.5.0";
    private final Map<String, Object> vars = new HashMap<>();
    private final java.util.List<AbstractInstallPanel> panels = new ArrayList<>();
    private final LoadingPanel loading = new LoadingPanel();
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel centerPanel;
    private int currentIndex;
    private JButton nextButton;
    private JButton previousButton;
    private JProgressBar progressBar;
    private JButton cancelButton;
    private JLabel leftComponent;
    private JButton exitButton;
    private boolean installFailed;

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI(args));
    }

    private static void createAndShowGUI(String[] args) {
        FlatLightLaf.setup();
//        FlatDarkLaf.setup();
        NutsInstaller mi = new NutsInstaller();
        InstallData.of(mi).setCmdline(args);
        doParseCommandLine(args, InstallData.of(mi));
        final JPanel panel = mi.createMainPanel();
        final JFrame frame = new JFrame("Nuts Package Manager Installer - " + VERSION);
        mi.frame = frame;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setResizable(false);
        frame.setIconImage(new ImageIcon(NutsInstaller.class.getResource("nuts-icon.png")).getImage());
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.add(createLeft(), BorderLayout.LINE_START);
        main.add(createCenter(), BorderLayout.CENTER);
        main.add(createBottom(), BorderLayout.PAGE_END);
        main.setPreferredSize(new Dimension(800, 500));
        applyPlaf();
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(main);
        });
        showPage(1);
        return main;
    }


    @Override
    public JPanel createBottom() {
        JPanel p = new JPanel(new BorderLayout());
        Box line = Box.createHorizontalBox();
        line.add(new JSeparator(JSeparator.HORIZONTAL));
        p.add(line, BorderLayout.PAGE_START);
        Box hb = Box.createHorizontalBox();
        p.add(hb, BorderLayout.CENTER);
        hb.add(Box.createHorizontalGlue());
        hb.add(Box.createRigidArea(new Dimension(10, 40)));
        previousButton = new JButton("Previous");
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onPreviousButton();
            }
        });
        hb.add(previousButton);
        hb.add(Box.createRigidArea(new Dimension(10, 0)));
        nextButton = new JButton("Next");
        hb.add(nextButton);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onNextButton();
            }
        });
        hb.add(Box.createRigidArea(new Dimension(10, 0)));
        cancelButton = new JButton("Cancel");
        hb.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancelButton();
            }
        });
        hb.add(Box.createRigidArea(new Dimension(10, 0)));
        exitButton = new JButton("Finish");
        hb.add(exitButton);
        hb.add(Box.createRigidArea(new Dimension(10, 0)));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onExitButton();
            }
        });
        return p;
    }

    @Override
    public void onNextButton() {
        if (hasNext(currentIndex)) {
            panels.get(currentIndex - 1).onNext();
            showPage(currentIndex + 1);
        }
    }

    @Override
    public void onPreviousButton() {
        if (hasPrevious(currentIndex)) {
            panels.get(currentIndex - 1).onPrevious();
            showPage(currentIndex - 1);
        }
    }

    @Override
    public void onCancelButton() {
        panels.get(currentIndex - 1).onCancel();
        frame.dispose();
    }

    @Override
    public void onExitButton() {
        frame.dispose();
    }

    @Override
    public JFrame getFrame() {
        return frame;
    }

    @Override
    public JButton getNextButton() {
        return nextButton;
    }

    @Override
    public JButton getPreviousButton() {
        return previousButton;
    }

    @Override
    public JButton getCancelButton() {
        return cancelButton;
    }

    @Override
    public JButton getExitButton() {
        return exitButton;
    }

    @Override
    public int getPagesCount() {
        return panels.size();
    }

    @Override
    public boolean isInstallFailed() {
        return installFailed;
    }

    @Override
    public NutsInstaller setInstallFailed(boolean installFailed) {
        this.installFailed = installFailed;
        return this;
    }

    @Override
    public boolean hasNext(int pageIndex) {
        return pageIndex < getPagesCount();
    }

    @Override
    public boolean hasPrevious(int pageIndex) {
        return pageIndex > 1;
    }

    @Override
    public Map<String, Object> getVars() {
        return vars;
    }

    public void startLoading() {
        cardLayout.show(centerPanel, "loading");
        loading.onShow();
    }

    public void stopLoading(int index) {
        loading.onHide();
        cardLayout.show(centerPanel, String.valueOf(index));
    }

    private JPanel createCenter() {
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.add(loading, "loading");
        loading.onAdd(this, 0);
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
        JPanel pp = new JPanel(new BorderLayout());
        pp.add(centerPanel, BorderLayout.CENTER);

        progressBar = new JProgressBar();
        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        pp.add(progressBar, BorderLayout.PAGE_END);
        return pp;
    }


    private void addPanel(AbstractInstallPanel p) {
        int index = getPagesCount() + 1;
        centerPanel.add(p, String.valueOf(index));
        panels.add(p);
        p.onAdd(this, index);
    }

    private void showPage(int panelIndex) {
        AbstractInstallPanel p = panels.get(panelIndex - 1);
        currentIndex = panelIndex;
        cardLayout.show(centerPanel, String.valueOf(panelIndex));
        getPreviousButton().setEnabled(panelIndex > 1);
        getNextButton().setEnabled(hasNext(panelIndex));
        setProgressByPageIndex();
        p.onShow();
    }

    private JComponent createLeft() {
        ImageIcon icon = new ImageIcon(getClass().getResource("nuts-logo.png"));
        int w = 220;
        Image img2 = UIHelper.getFixedSizeImage(icon.getImage(), w, -1, true);
        icon = new ImageIcon(img2);
        JLabel label = new JLabel(icon);
        label.setOpaque(true);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill=GridBagConstraints.BOTH;
        g.insets=new Insets(5,10,5,10);
        g.gridx=0;
        g.gridy=0;
        g.weightx=10;
        g.weighty=10;
        p.setOpaque(true);
        p.add(label, g);
        p.setBorder(BorderFactory.createEtchedBorder());
        this.leftComponent=label;
        setDarkMode(InstallData.of(this).darkMode);
        return p;
    }

    public void setDarkMode(boolean darkMode) {
        Color cc=darkMode?new Color(0x3b3e40):new Color(0xf2f2f2);
        leftComponent.setBackground(cc);
        for (Component component : leftComponent.getComponents()) {
            component.setBackground(cc);
        }
    }

    public void setProgressByPageIndex() {
        progressBar.setIndeterminate(false);
        int n = (int) (currentIndex * 100.0 / (getPagesCount()));
        progressBar.setValue(n);
    }

    @Override
    public void applyPlaf() {
        JFrame f = getFrame();
        if (f != null) {
            SwingUtilities.invokeLater(() -> {
                SwingUtilities.updateComponentTreeUI(f);
            });
        }
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(loading);
        });
    }
}
