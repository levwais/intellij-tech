package com.jivesoftware.intellij.tech.commands;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: lev.waisberg
 * Date: 11/10/13
 * Time: 4:40 PM
 */
public class JDeployable implements JCommandInstance {

    private JTextField textField1;
    private JCheckBox checkBox1;
    private JCheckBox checkBox2;
    private JCheckBox checkBox3;
    private JCheckBox checkBox4;
    private JPanel panel;

    String title = "";


    public JCommandType getType() {
        return JCommandType.J_DEPLOYABLE;
    }

    public String getTitle() {
        return title;
    }

    public ProcessBuilder getCommand() {
        return new ProcessBuilder("j", textField1.getText()).directory(new File(System.getenv("J_BIN"))).redirectErrorStream(true);
    }

    public JPanel getPanel() {
        return panel;
    }
}
