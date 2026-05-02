package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.model.*;
import net.thevpc.nuts.installer.util.InstallPalette;
import net.thevpc.nuts.installer.util.swing.*;
import net.thevpc.nuts.installer.connector.RequestQuery;
import net.thevpc.nuts.installer.connector.RequestQueryInfo;
import net.thevpc.nuts.installer.connector.SimpleRecommendationConnector;
import net.thevpc.nuts.installer.util.UiHelper2;
import net.thevpc.nuts.installer.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.StringReader;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class VersionsPanel extends WizardPageBase {


    ButtonGroup bg;
    JPanel panel;
    JEditorPane jep;
    JToggleButton ltsButton;
    JToggleButton standardButton;
    JToggleButton errorButton;
    boolean errorMode;
//    Color greenFg = Color.GREEN;
//    Color orangeFg = Color.ORANGE;
//    Color redFg = new Color(255, 100, 101);
//    Color greenBg = new Color(161, 205, 161);
//    Color orangeBg = new Color(255, 224, 101);
//    Color redBg = new Color(255, 100, 101);
    ButtonInfo ltsButtonInfo = new ButtonInfo("LTS", "LTS", InstallPalette.ltsLight, InstallPalette.ltsDark, UiHelper2.getCheckedImageIcon(false), UiHelper2.getCheckedImageIcon(true));
    ButtonInfo stdButtonInfo = new ButtonInfo("Standard", "Standard", InstallPalette.standardLight, InstallPalette.standardDark, UiHelper2.getCheckedImageIcon(false), UiHelper2.getCheckedImageIcon(true));
    ButtonInfo errButtonInfo = new ButtonInfo("Not Available", "<html><body>Unable to resolve stable version. Please Check your internet connection</body></html>", InstallPalette.errorLight, InstallPalette.errorDark, UiHelper2.getStopImageIcon(false), UiHelper2.getStopImageIcon(true));
    ItemListener defaultButtonListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            JToggleButton src = (JToggleButton) e.getSource();
            ButtonInfo ii = ButtonInfo.of(src);
            updateObservations(ii);
            ii.applyButtonInfo(src);
            // Inside your item listener or when setting selected

        }
    };

    public VersionsPanel() {
        super(new BorderLayout());
    }

    @Override
    public void onAdd(Wizard installerContext, int pageIndex) {
        super.onAdd(installerContext, pageIndex);
        add(UIHelper.titleLabel("Please select the version you want to install locally"), BorderLayout.PAGE_START);
        GridLayout gg = new GridLayout(1, 2);
        gg.setVgap(10);
        gg.setHgap(10);
        panel = new JPanel(gg);
        int w = 160;
        panel.setPreferredSize(new Dimension(2 * w, w));
        panel.setMaximumSize(new Dimension(2 * w, w));
        bg = new ButtonGroup();
        switchMode(false);
        JPanel jp = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        jp.add(panel, c);
        add(jp, BorderLayout.CENTER);
        jep = new JEditorPane();
        jep.setContentType("text/html");
        jep.setEditable(false);

        JScrollPane jsp = new JScrollPane(jep);
        jsp.setMinimumSize(new Dimension(100, 100));
        jsp.setMaximumSize(new Dimension(1000, 100));
        jsp.setPreferredSize(new Dimension(100, 100));
        add(UIHelper.margins(jsp, 10), BorderLayout.PAGE_END);
        updateObservations(null);
    }

    @Override
    public void sendAction(String[] action) {
        switch (action[0]) {
            case "lts": {
                SwingUtilities.invokeLater(() -> {
                    ltsButton.setSelected(true);
                });
                break;
            }
            case "standard": {
                SwingUtilities.invokeLater(() -> {
                    standardButton.setSelected(true);
                });
                break;
            }
            case "wait-loading": {
                getInstallerContext().waitLoading();
                break;
            }
        }
    }

    private void switchMode(boolean error) {
        this.errorMode=error;
        if (error) {
            GridLayout gg = new GridLayout(1, 1);
            gg.setVgap(10);
            gg.setHgap(10);
            panel.removeAll();
            panel.setLayout(gg);
            int w = 160;
            panel.setPreferredSize(new Dimension(2 * w, w));
            panel.setMaximumSize(new Dimension(2 * w, w));
            if (errorButton == null) {
                errorButton = errButtonInfo.createAndBind();
                errorButton.setEnabled(false);
            }
            errButtonInfo.applyButtonInfo(errorButton);
            panel.add(errorButton);
            updateObservations(errButtonInfo);
        } else {
            GridLayout gg = new GridLayout(1, 2);
            gg.setVgap(10);
            gg.setHgap(10);
            panel.removeAll();
            if (standardButton != null) {
                bg.remove(standardButton);
            }
            if (ltsButton != null) {
                bg.remove(ltsButton);
            }
            panel.setLayout(gg);
            int w = 160;
            panel.setPreferredSize(new Dimension(2 * w, w));
            panel.setMaximumSize(new Dimension(2 * w, w));

            if (standardButton == null) {
                standardButton = add2(stdButtonInfo);
            } else {
                panel.add(standardButton);
                bg.add(standardButton);
            }
            if (ltsButton == null) {
                ltsButton = add2(ltsButtonInfo);
            } else {
                panel.add(ltsButton);
                bg.add(ltsButton);
            }
        }
    }

    private JToggleButton add2(ButtonInfo s) {
        JToggleButton a = s.createAndBind();
        panel.add(a);
        bg.add(a);
        a.addItemListener(defaultButtonListener);
        return a;
    }

    @Override
    public void onNext() {
        InstallData id = InstallData.of(getInstallerContext());
        if (ltsButton.isSelected() || !standardButton.isEnabled()) {
            ButtonInfo jj = ButtonInfo.of(ltsButton);
            id.setInstallVersion(jj.verInfo);
        } else {
            ButtonInfo jj = ButtonInfo.of(standardButton);
            id.setInstallVersion(jj.verInfo);
        }
        super.onNext();
    }

    @Override
    public void onShow() {
        boolean darkMode = InstallData.of(getInstallerContext()).darkMode;
        ltsButtonInfo.setDarkMode(darkMode);
        stdButtonInfo.setDarkMode(darkMode);
        errButtonInfo.setDarkMode(darkMode);
        switchMode(false);
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
        new Thread(this::updateButtons).start();
    }

    @Override
    public void applyPlaf() {
        boolean darkMode = InstallData.of(getInstallerContext()).darkMode;
        ltsButtonInfo.setDarkMode(darkMode);
        stdButtonInfo.setDarkMode(darkMode);
        errButtonInfo.setDarkMode(darkMode);
        switchMode(errorMode);
        super.applyPlaf();
    }

    protected void updateButtons() {
        getInstallerContext().getNextButton().setEnabled(false);
        getInstallerContext().getPreviousButton().setEnabled(false);
        Info info = loadInfo();
        SwingUtilities.invokeLater(() -> {
            getInstallerContext().stopLoading(getPageIndex());
            ButtonInfo ii = ButtonInfo.of(ltsButton);
            ii.verInfo = info.stable;
            if (ii.verInfo.valid) {
                switchMode(false);
                ltsButton.setEnabled(true);
                ii.html = ("<html><body>Select the <strong>LTS</strong> version <strong>" + info.stable.runtime + "</strong> for maximum stability and long-term support. It receives critical security patches only</body></html>");
                ltsButtonInfo.applyButtonInfo(ltsButton);
                updateObservations(ltsButtonInfo);
                ButtonInfo jj = ButtonInfo.of(standardButton);
                jj.html = ("<html><body>Select the <strong>Standard</strong> version <strong>" + info.preview.runtime + "</strong> to get the latest features, updates, and bug fixes. If you are not sure what to choose, choose Standard</body></html>");
                jj.verInfo = info.preview;
                if (Objects.equals(info.stable.runtime, info.preview.runtime)) {
                    standardButton.setEnabled(false);
                    standardButton.setText("Standard");
                } else {
//                previewButton.setText("<html><body><center>Standard<br>version<br><strong>" + info.preview.runtime+"</strong></center></body></html>");
                    standardButton.setText("Standard");
                    standardButton.setEnabled(true);
                }
                standardButton.setVisible(ii.verInfo.valid);
                standardButton.setSelected(true);
            } else {
                switchMode(true);
            }

            getInstallerContext().getNextButton().setEnabled(ii.verInfo.valid);
            getInstallerContext().getPreviousButton().setEnabled(true);
            panel.invalidate();
            panel.revalidate();
        });
    }

    private void updateObservations(ButtonInfo e) {
        if (e == null) {
            jep.setText("<html><body>select the <strong>stable</strong> version for production</body></html>");
        } else {
            jep.setText(e.html);
        }
    }

    private Info loadInfo() {
        SwingUtilities.invokeLater(() -> {
            getInstallerContext().startLoading();
        });
        RequestQueryInfo ri = new RequestQueryInfo();
        RequestQuery q = new RequestQuery();
        q.setId("net.thevpc.nuts:nuts-app#RELEASE");
        ri.setQ(q);
        Map info = null;
        Properties metadata = new Properties();
        try {
            try {
                Map d = new SimpleRecommendationConnector().getRecommendations(ri);
                info = (Map) d.get("info");
            } catch (Exception ex) {
                //ignore
            }
            try {
                String md = Utils.downloadFile("https://raw.githubusercontent.com/thevpc/nuts/master/METADATA");
                metadata.load(new StringReader(md));
            } catch (Exception ex) {
                //ignore
            }
        } finally {
            SwingUtilities.invokeLater(() -> {
                getInstallerContext().stopLoading(getPageIndex());
            });
        }
        /**
         * public class PackageDescriptorInfo {
         *     private List<String> securityIssues;
         *     private List<String> alternatives;
         *     private List<String> recommendations;
         *     private String recommendedVersion;
         *     private String stableVersion;
         *     private String previewVersion;
         *     private String smallIcon;
         *     private String longDescriptionHtml;
         *     private Instant lastUpdate;
         * }
         */
        Info ii = new Info();
//        if(info!=null) {
//            if(info.get("stableVersion") instanceof String) {
//                ii.stableVersionRuntime = (String) info.get("stableVersion");
//            }
//            if(info.get("previewVersion") instanceof String) {
//                ii.previewVersionApi = (String) info.get("previewVersion");
//            }
//        }
//        stableApiVersion=0.8.3
//        stableRuntimeVersion=0.8.3.0
//        stableJarLocation=https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/0.8.3/nuts-0.8.3.jar
//        latestApiVersion=0.8.3
//        latestRuntimeVersion=0.8.3.1-alpha1
//        latestJarLocation=https://raw.githubusercontent.com/thevpc/nuts-preview/master/net/thevpc/nuts/nuts/0.8.3/nuts-0.8.3.jar
//        apiVersion=0.8.3
//        implVersion=0.8.3.1-alpha1
//        jarLocation=https://raw.githubusercontent.com/thevpc/nuts-preview/master/net/thevpc/nuts/nuts/0.8.3/nuts-0.8.3.jar
//        buildTime=Sun Jan 23 03:59:50 PM +0000 2022
        ii.stable.api = metadata.getProperty("stableApiVersion");
        ii.stable.runtime = metadata.getProperty("stableRuntimeVersion");
        if (ii.stable.runtime == null) {
            ii.stable.runtime = metadata.getProperty("stableImplVersion");
        }
        ii.stable.location = metadata.getProperty("stableJarLocation");
        ii.stable.valid = ii.stable.api != null;
        ii.preview.api = metadata.getProperty("latestApiVersion");
        ii.preview.runtime = metadata.getProperty("latestRuntimeVersion");
        if (ii.preview.runtime == null) {
            ii.preview.runtime = metadata.getProperty("latestImplVersion");
        }
        ii.preview.location = metadata.getProperty("latestJarLocation");
        ii.preview.valid = ii.preview.api != null;
        return ii;
    }

    public static class Info {
        VerInfo stable = new VerInfo(true);
        VerInfo preview = new VerInfo(false);
    }

}
