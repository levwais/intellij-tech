package com.jivesoftware.intellij.tech.commands;

import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: lev.waisberg
 * Date: 11/11/13
 * Time: 3:09 PM
 */
public class MvnCommand implements JCommandInstance {
    private JPanel panel1;
    private JTextField textField1;
    private JButton button1;
    private JCheckBox cleanCheckBox;
    private JCheckBox installCheckBox;
    private JCheckBox skipTestsCheckBox;
    private JCheckBox runCheckStyleCheckBox;
    private JCheckBox buildDeployablesCheckBox;
    private JTextField customFlagsTxt;
    Map<JCheckBox, String> checkBoxToArgument;

    public MvnCommand() {
        textField1.setText(System.getenv("J_CODE"));

        checkBoxToArgument = new HashMap<JCheckBox, String>();
        checkBoxToArgument.put(cleanCheckBox, "clean");
        checkBoxToArgument.put(installCheckBox, "install");
        checkBoxToArgument.put(skipTestsCheckBox, "-DskipTests");
        checkBoxToArgument.put(runCheckStyleCheckBox, "-Pchecks");
        checkBoxToArgument.put(buildDeployablesCheckBox, "-Pmake-deployable");

        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser folderChooser = new JFileChooser(textField1.getText());
                folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (folderChooser.showOpenDialog(button1) == JFileChooser.APPROVE_OPTION) {
                    textField1.setText(folderChooser.getSelectedFile().getPath());
                }
            }
        });
    }

    public JCommandType getType() {
        return JCommandType.MVN;
    }

    public List<ProcessBuilder> getCommands() {
        List<String> processNameAndArguments = new ArrayList<String>();
        processNameAndArguments.add("mvn");

        for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
            if (entry.getKey().isSelected())
                processNameAndArguments.add(entry.getValue());
        if (!customFlagsTxt.getText().isEmpty()) {
            processNameAndArguments.add(customFlagsTxt.getText().trim());
        }

        return Lists.newArrayList(new ProcessBuilder(processNameAndArguments).directory(new File(textField1.getText())));
    }

    public JPanel getPanel() {
        return panel1;
    }

    public String getCommandStr() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("folder", textField1.getText());
            jsonObject.put("customFlags", customFlagsTxt.getText());

            for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
                jsonObject.put(entry.getValue(), entry.getKey().isSelected());

            return jsonObject.toString();
        }
        catch (JSONException e) {
            return null;
        }

    }

    public void LoadCommand(String commandStr) {
        try {
            JSONObject jsonObject = new JSONObject(commandStr);
            textField1.setText(jsonObject.getString("folder"));
            customFlagsTxt.setText(jsonObject.getString("customFlags"));

            for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
                entry.getKey().setSelected(jsonObject.getBoolean(entry.getValue()));
        }
        catch (JSONException e) {
            return;
        }
    }
}
