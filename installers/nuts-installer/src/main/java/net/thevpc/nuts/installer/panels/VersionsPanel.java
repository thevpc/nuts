package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.util.swing.*;
import net.thevpc.nuts.installer.model.ButtonInfo;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.connector.RequestQuery;
import net.thevpc.nuts.installer.connector.RequestQueryInfo;
import net.thevpc.nuts.installer.connector.SimpleRecommendationConnector;
import net.thevpc.nuts.installer.util.UiHelper2;
import net.thevpc.nuts.installer.util.Utils;
import net.thevpc.nuts.installer.model.VerInfo;

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
    Color greenFg = Color.GREEN;
    Color orangeFg = Color.ORANGE;
    Color redFg = new Color(255, 100, 101);
    Color greenBg = new Color(161, 205, 161);
    Color orangeBg = new Color(255, 224, 101);
    Color redBg = new Color(255, 100, 101);
    ButtonInfo ltsButtonInfo = new ButtonInfo("LTS", "LTS", greenBg, greenFg, UiHelper2.getCheckedImageIcon(false), UiHelper2.getCheckedImageIcon(true));
    ButtonInfo standardButtonInfo = new ButtonInfo("Standard", "Standard", orangeBg, orangeFg, UiHelper2.getCheckedImageIcon(false), UiHelper2.getCheckedImageIcon(true));
    ButtonInfo errorButtonInfo = new ButtonInfo("Not Available", "<html><body>Unable to resolve stable version. Please Check your internet connexion</body></html>", redBg, redFg, UiHelper2.getStopImageIcon(false), UiHelper2.getStopImageIcon(true));

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
        standardButton = add2(standardButtonInfo);
        ltsButton = add2(ltsButtonInfo);

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

    private JToggleButton add2(ButtonInfo s) {

        InstallData id = InstallData.of(getInstallerContext());
        JToggleButton a = new JToggleButton(s.text);
        s.bind(a);
//        a.setPreferredSize(new Dimension(60,60));
        panel.add(a);
        bg.add(a);
        a.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JToggleButton src = (JToggleButton) e.getSource();
                ButtonInfo ii = ButtonInfo.of(src);
                updateObservations(ii);
                ii.applyButtonInfo(src);
            }
        });
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
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
        new Thread(this::updateButtons).start();

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
                ltsButton.setEnabled(true);
                ii.html = ("<html><body>Select the <strong>LTS</strong> version <strong>" + info.stable.runtime + "</strong> for maximum stability and long-term support. It receives critical security patches only</body></html>");
                ltsButtonInfo.applyButtonInfo(ltsButton);
                updateObservations(ltsButtonInfo);
            } else {
                ltsButton.setEnabled(false);
                errorButtonInfo.applyButtonInfo(ltsButton);
                updateObservations(errorButtonInfo);
            }
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
            if (ii.verInfo.valid) {
                standardButton.setVisible(true);
            }else{
                standardButton.setVisible(false);
            }
            standardButton.setSelected(true);

            getInstallerContext().getNextButton().setEnabled(ii.verInfo.valid);
            getInstallerContext().getPreviousButton().setEnabled(true);
            panel.invalidate();
            panel.revalidate();
        });
    }
    private void updateObservations(ButtonInfo e){
        if(e==null){
            jep.setText("<html><body>select the <strong>stable</strong> version for production</body></html>");
        }else {
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
