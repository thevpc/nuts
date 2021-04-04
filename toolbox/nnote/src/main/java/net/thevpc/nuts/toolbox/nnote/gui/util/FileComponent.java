/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import java.awt.Component;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import net.thevpc.common.swing.GridBagLayoutSupport;
import net.thevpc.echo.Application;

/**
 *
 * @author vpc
 */
public class FileComponent extends JPanel {

    private JTextField textField = new JTextField();
    private JButton show;
    private Application app;
    private boolean acceptAllFileFilterUsed;
    private List<FileFilter> fileFilters = new ArrayList<>();

    public FileComponent() {
        show = new JButton("...");
        show.addActionListener((e)
                -> {
            onShowDialog();
        }
        );
        new GridBagLayoutSupport("[pwd-===][check] ; insets(2)")
                .bind("pwd", textField)
                .bind("check", show)
                .apply(this);
    }

    public JTextField getTextField() {
        return textField;
    }
    
    public boolean  isEditable(){
        return show.isEnabled() && textField.isEditable();
    }
    public void setEditable(boolean b){
        show.setEnabled(b);
        textField.setEditable(b);
    }
    

    private void onShowDialog() throws HeadlessException {
        JFileChooser c = new JFileChooser();
        for (FileFilter filter : fileFilters) {
            c.addChoosableFileFilter(filter);
        }
        c.setAcceptAllFileFilterUsed(acceptAllFileFilterUsed);
        int v = c.showOpenDialog(
                app==null?null:(Component) app.mainWindow().get().component()
        );
        if (v == JFileChooser.APPROVE_OPTION) {
            textField.setText(c.getSelectedFile().getPath());
        }
    }

    public boolean isAcceptAllFileFilterUsed() {
        return acceptAllFileFilterUsed;
    }

    public FileComponent setAcceptAllFileFilterUsed(boolean acceptAllFileFilterUsed) {
        this.acceptAllFileFilterUsed = acceptAllFileFilterUsed;
        return this;
    }
    

    public String getContentString() {
        return textField.getText();
    }

    public FileComponent setValue(String s) {
        textField.setText(s);
        return this;
    }

    public List<FileFilter> getFileFilters() {
        return fileFilters;
    }

    public FileComponent setFileFilters(List<FileFilter> fileFilters) {
        this.fileFilters = fileFilters;
        return this;
    }

    public void uninstall() {
    }

    public void install(Application app) {
    }

}
