package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.connector.RequestQuery;
import net.thevpc.nuts.installer.connector.RequestQueryInfo;
import net.thevpc.nuts.installer.connector.SimpleRecommendationConnector;
import net.thevpc.nuts.installer.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.Objects;

public class VersionsPanel extends AbstractInstallPanel {
    ButtonGroup bg;
    JPanel panel;
    JEditorPane jep;
    JToggleButton stableButton;
    JToggleButton previewButton;

    public VersionsPanel() {
        super(new BorderLayout());
        add(UIHelper.titleLabel("Please select the version you want to install locally"),BorderLayout.PAGE_START);
        GridLayout gg = new GridLayout(1, 2);
        gg.setVgap(10);
        gg.setHgap(10);
        panel = new JPanel(gg);
        int w=160;
        panel.setPreferredSize(new Dimension(2*w,w));
        panel.setMaximumSize(new Dimension(2*w,w));
        bg = new ButtonGroup();
        stableButton=add2(new ButtonInfo("Stable","Stable",new Color(161,205,161), Color.GREEN));
        previewButton=add2(new ButtonInfo("Preview","Preview",new Color(255,224,101),Color.ORANGE));

        JPanel jp=new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        jp.add(panel, c);
        add(jp,BorderLayout.CENTER);
        jep = new JEditorPane();
        jep.setContentType("text/html");
        jep.setEditable(false);

        JScrollPane jsp = new JScrollPane(jep);
        jsp.setMinimumSize(new Dimension(100,100));
        jsp.setMaximumSize(new Dimension(1000,100));
        jsp.setPreferredSize(new Dimension(100,100));
        add(UIHelper.margins(jsp,10),BorderLayout.PAGE_END);
        jep.setText("<html><body>selection the <strong>stable</strong> version for production</body></html>");
    }

    private static class ButtonInfo{
        String text;
        String html;
        Color bg;
        Color bg2;

        public ButtonInfo(String text, String html, Color bg, Color bg2) {
            this.text = text;
            this.html = html;
            this.bg = bg;
            this.bg2 = bg2;
        }
        private void apply(JToggleButton jtb){
            JToggleButton a = new JToggleButton(text);
            a.setBackground(bg);
        }
    }
    private JToggleButton add2(ButtonInfo s) {

        JToggleButton a = new JToggleButton(s.text);
        a.putClientProperty("ButtonInfo",s);
        a.setBackground(s.bg);
//        a.setPreferredSize(new Dimension(60,60));
        panel.add(a);
        bg.add(a);
        a.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                ButtonInfo ii=(ButtonInfo) ((JToggleButton)e.getSource()).getClientProperty("ButtonInfo");
                jep.setText(ii.html);
            }
        });
        return a;
    }

    @Override
    public void onShow() {
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
        new Thread(this::updateButtons).start();

    }

    protected void updateButtons() {
        SwingUtilities.invokeLater(() -> {
            getInstallerContext().startLoading();
        });
        RequestQueryInfo ri = new RequestQueryInfo();
        ri.url="http://localhost:8080";
        RequestQuery q = new RequestQuery();
        q.setId("net.thevpc.nuts:nuts#RELEASE");
        ri.setQ(q);
        Map d = new SimpleRecommendationConnector().askDescriptor(ri);
        Map info=(Map) d.get("info");
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
        String stableVersion=(String) info.get("stableVersion");
        String previewVersion=(String) info.get("previewVersion");
        SwingUtilities.invokeLater(() -> {
            getInstallerContext().stopLoading(getPageIndex());
            ButtonInfo ii=(ButtonInfo) (stableButton.getClientProperty("ButtonInfo"));
            ii.html="version "+stableVersion;
            ButtonInfo jj=(ButtonInfo) (previewButton.getClientProperty("ButtonInfo"));
            jj.html="version "+previewVersion;
            if(Objects.equals(stableVersion, previewVersion)){
                previewButton.setEnabled(false);
                stableButton.setSelected(true);
                jep.setText(ii.html);
            }else{
                previewButton.setEnabled(true);
            }
            panel.invalidate();
            panel.revalidate();
        });
    }


}
