package com.jivesoftware.intellij.tech.commands;

import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: lev.waisberg
 * Date: 11/11/13
 * Time: 5:06 PM
 */
public class EnvUpdate implements JCommandInstance {
    private JPanel panel1;
    private JCheckBox buildEverythingBCheckBox;
    private JCheckBox forcePushFCheckBox;
    private JCheckBox dryRunNCheckBox;
    private JCheckBox donTRunPuppetCheckBox;
    private JCheckBox useJiveVagrantCredentialsCheckBox;
    private JTextField customTxt;
    private JComboBox environmentTxt;
    private JComboBox versionTxt;

    Map<JCheckBox, String> checkBoxToArgument;

    public EnvUpdate() {
        checkBoxToArgument = new HashMap<JCheckBox, String>();
        checkBoxToArgument.put(buildEverythingBCheckBox, "-b");
        checkBoxToArgument.put(forcePushFCheckBox, "-f");
        checkBoxToArgument.put(dryRunNCheckBox, "-n");
        checkBoxToArgument.put(donTRunPuppetCheckBox, "-P");
        checkBoxToArgument.put(useJiveVagrantCredentialsCheckBox, "-v");
    }

    public JCommandType getType() {
        return JCommandType.J_ENV_UPDATE;
    }

    public List<ProcessBuilder> getCommands() {
        List<String> processNameAndArguments = new ArrayList<String>();
        processNameAndArguments.add("j-env-update");

        for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
            if (entry.getKey().isSelected())
                processNameAndArguments.add(entry.getValue());

        processNameAndArguments.add("-e");
        processNameAndArguments.add(environmentTxt.getSelectedItem().toString());

        if (!customTxt.getText().trim().isEmpty()) {
            processNameAndArguments.addAll(Lists.newArrayList(customTxt.getText().trim().split(" ")));
        }

        processNameAndArguments.add(versionTxt.getSelectedItem().toString());


        return Lists.newArrayList(
                new ProcessBuilder(processNameAndArguments).directory(new File(System.getenv("J_BIN")))
                        .redirectErrorStream(true)
        );

    }

    public JPanel getPanel() {
        return panel1;
    }

    public String getCommandStr() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("environment", environmentTxt.getSelectedItem().toString());
            jsonObject.put("version", versionTxt.getSelectedItem().toString());
            jsonObject.put("custom", customTxt.getText().trim());

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
            environmentTxt.setSelectedItem(jsonObject.getString("environment"));
            versionTxt.setSelectedItem(jsonObject.getString("version"));
            customTxt.setText(jsonObject.getString("custom"));

            for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
                entry.getKey().setSelected(jsonObject.getBoolean(entry.getValue()));
        }
        catch (JSONException e) {
            return;
        }
    }
}
