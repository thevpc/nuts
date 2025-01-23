package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.boot.swing.UIHelper;
import net.thevpc.nuts.boot.swing.WizardPageBase;
import net.thevpc.nuts.boot.swing.Wizard;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.util.*;
import net.thevpc.nuts.boot.swing.GBC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.function.Consumer;

public class JavaPanel extends WizardPageBase {
    JTextField javaPathField = new JTextField();
    JButton javaPathButton = new JButton("...");
    JTextArea javaResultLabel = new JTextArea();
    JScrollPane javaResultLabelScroll;
    String lastEvalPath;

    public JavaPanel() {
        super(new BorderLayout());

        javaPathField.setToolTipText("Specify Java command here");
        javaPathButton.setToolTipText("Specify Java command here");
        javaResultLabel.setToolTipText("");
        javaPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                int r = jfc.showOpenDialog(JavaPanel.this);
                if (r == JFileChooser.APPROVE_OPTION) {
                    File f = jfc.getSelectedFile();
                    if (f != null) {
                        javaPathField.setText(f.getPath());
                        evalJavaVersion();
                    }
                }
            }
        });
        add(UIHelper.titleLabel("Please select JRE Location to run nuts"), BorderLayout.PAGE_START);
        JPanel gbox = new JPanel(new GridBagLayout());
        GBC gc = GBC
                .of().anchorNorthWest()
                .fillHorizontal()
                .weight(1,0)
                .insets(5);
        gbox.add(new JLabel("Java Executable Location"), gc.at(0, 0));
        gbox.add(jPanel(), gc.nextLine());
        javaResultLabel.setEditable(false);
        gbox.add(javaResultLabelScroll=new JScrollPane(javaResultLabel), gc.nextLine().weight(1,2).fillBoth());
        add(UIHelper.margins(gbox, 10));
        resetDefaults();
        javaPathField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                evalJavaVersion();
            }
        });
    }

    private JPanel jPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(javaPathField, BorderLayout.CENTER);
        p.add(javaPathButton, BorderLayout.LINE_END);
        return p;
    }

    private void resetDefaults() {
    }

    @Override
    public void onAdd(Wizard installerContext, int pageIndex) {
        super.onAdd(installerContext, pageIndex);
        javaPathField.setText(InstallData.of(getInstallerContext()).getDefaultJavaHome());
    }

    @Override
    public void onNext() {
        InstallData id = InstallData.of(getInstallerContext());
        id.java = Utils.trim(javaPathField.getText());
        super.onNext();
    }

    @Override
    public void onShow() {
        resetDefaults();
        evalJavaVersion();
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
    }

    public void evalJavaVersion() {
        String javaPath = javaPathField.getText().trim();
        if (Utils.isBlank(javaPath)) {
            javaPath = "java";
        }
        if(javaPath.equals(lastEvalPath)){
            return;
        }
        String javaPathToEval = javaPath;
        new Thread(() -> {
            lastEvalPath= javaPathToEval;
            String e = evalJavaVersionSync(javaPathToEval);
            SwingUtilities.invokeLater(() -> {
                if (e.length() > 0) {
                    javaResultLabel.setText("Detected java version :\n" + e);
                    javaResultLabelScroll.setBorder(BorderFactory.createLineBorder(Color.GREEN.darker(),3));
                    getInstallerContext().getNextButton().setEnabled(true);
                } else {
                    javaResultLabel.setText("Invalid java location");
                    javaResultLabelScroll.setBorder(BorderFactory.createLineBorder(Color.RED.darker(),3));
                    getInstallerContext().getNextButton().setEnabled(false);
                }
            });
        }).start();
    }

    public String evalJavaVersionSync(String javaPath) {
        ProcessBuilder sb = new ProcessBuilder();
        sb.command(javaPath, "-version");
        Process p = null;
        try {
            p = sb.start();
            StringBuilder sout = new StringBuilder();
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), new Consumer<String>() {
                @Override
                public void accept(String s) {
                    sout.append(s);
                }
            });
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), new Consumer<String>() {
                @Override
                public void accept(String s) {
                    sout.append(s);
                }
            });
            errorGobbler.start();
            outputGobbler.start();
            int e = p.waitFor();
            if (e != 0) {
                return "";
            }
            return sout.toString().trim();
//            BufferedReader br=new BufferedReader(new StringReader(sout.toString()));
//            String line = br.readLine();
//            if(line==null){
//                line="";
//            }
//            return line.trim();
        } catch (Exception e) {
            return "";
        }
    }
}
