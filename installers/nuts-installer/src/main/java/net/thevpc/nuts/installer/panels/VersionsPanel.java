package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.InstallerContext;
import net.thevpc.nuts.installer.model.ButtonInfo;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.connector.RequestQuery;
import net.thevpc.nuts.installer.connector.RequestQueryInfo;
import net.thevpc.nuts.installer.connector.SimpleRecommendationConnector;
import net.thevpc.nuts.installer.util.UIHelper;
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

public class VersionsPanel extends AbstractInstallPanel {
    ButtonGroup bg;
    JPanel panel;
    JEditorPane jep;
    JToggleButton stableButton;
    JToggleButton previewButton;

    public VersionsPanel() {
        super(new BorderLayout());
    }

    @Override
    public void onAdd(InstallerContext installerContext, int pageIndex) {
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
        stableButton = add2(new ButtonInfo("Stable", "Stable", new Color(161, 205, 161), Color.GREEN));
        previewButton = add2(new ButtonInfo("Preview", "Preview", new Color(255, 224, 101), Color.ORANGE));

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
        jep.setText("<html><body>select the <strong>stable</strong> version for production</body></html>");    }

    private JToggleButton add2(ButtonInfo s) {

        InstallData id = InstallData.of(getInstallerContext());
        JToggleButton a = new JToggleButton(s.text);
        a.setForeground(Color.BLACK);
        a.putClientProperty("ButtonInfo", s);
        a.setBackground(s.bg);
//        a.setPreferredSize(new Dimension(60,60));
        panel.add(a);
        bg.add(a);
        a.setForeground(Color.BLACK);
        a.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                ButtonInfo ii = (ButtonInfo) ((JToggleButton) e.getSource()).getClientProperty("ButtonInfo");
                jep.setText(ii.html);
                a.setForeground(Color.BLACK);
            }
        });
        a.setIcon(new ImageIcon(UIHelper.getCheckedImage(false)));
        a.setSelectedIcon(new ImageIcon(UIHelper.getCheckedImage(true)));
        return a;
    }

    @Override
    public void onNext() {
        InstallData id = InstallData.of(getInstallerContext());
        if(stableButton.isSelected() || !previewButton.isEnabled()) {
            ButtonInfo jj = (ButtonInfo) (stableButton.getClientProperty("ButtonInfo"));
            id.installVersion=jj.verInfo;
        }else{
            ButtonInfo jj = (ButtonInfo) (previewButton.getClientProperty("ButtonInfo"));
            id.installVersion=jj.verInfo;
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
            ButtonInfo ii = (ButtonInfo) (stableButton.getClientProperty("ButtonInfo"));
            ii.html=("<html><body>Select the <strong>stable</strong> version <strong>"+info.stable.runtime+"</strong> for production</body></html>");
//            stableButton.setText("<html><body><center>Stable<br>version<br><strong>" + info.stable.runtime+"</strong></center></body></html>");
            stableButton.setText("Stable");
            ii.verInfo = info.stable;
            ButtonInfo jj = (ButtonInfo) (previewButton.getClientProperty("ButtonInfo"));
            jj.html=("<html><body>Select the <strong>preview</strong> version <strong>"+info.preview.runtime+"</strong> to test new features and get latest updates and bug fixes</body></html>");
            jj.verInfo = info.preview;
            stableButton.setSelected(true);
            jep.setText(ii.html);
            if (Objects.equals(info.stable.runtime, info.preview.runtime)) {
                previewButton.setEnabled(false);
                previewButton.setText("Preview");
            } else {
//                previewButton.setText("<html><body><center>Preview<br>version<br><strong>" + info.preview.runtime+"</strong></center></body></html>");
                previewButton.setText("Preview");
                previewButton.setEnabled(true);
            }
            stableButton.setForeground(Color.BLACK);
            previewButton.setForeground(Color.BLACK);
            getInstallerContext().getNextButton().setEnabled(ii.verInfo.valid);
            getInstallerContext().getPreviousButton().setEnabled(true);
            panel.invalidate();
            panel.revalidate();
        });
    }

    private Info loadInfo(){
        SwingUtilities.invokeLater(() -> {
            getInstallerContext().startLoading();
        });
        RequestQueryInfo ri = new RequestQueryInfo();
        RequestQuery q = new RequestQuery();
        q.setId("net.thevpc.nuts:nuts#RELEASE");
        ri.setQ(q);
        Map info = null;
        Properties metadata  = new Properties();
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
        Info ii=new Info();
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
        ii.stable.api =metadata.getProperty("stableApiVersion");
        ii.stable.runtime =metadata.getProperty("stableRuntimeVersion");
        if(ii.stable.runtime==null){
            ii.stable.runtime =metadata.getProperty("stableImplVersion");
        }
        ii.stable.location=metadata.getProperty("stableJarLocation");
        ii.stable.valid=ii.stable.api!=null;
        ii.preview.api =metadata.getProperty("latestApiVersion");
        ii.preview.runtime =metadata.getProperty("latestRuntimeVersion");
        if(ii.preview.runtime==null){
            ii.preview.runtime =metadata.getProperty("latestImplVersion");
        }
        ii.preview.location=metadata.getProperty("latestJarLocation");
        ii.preview.valid=ii.preview.api!=null;
        return ii;
    }

    public static class Info {
        VerInfo stable = new VerInfo(true);
        VerInfo preview = new VerInfo(false);
    }

}
