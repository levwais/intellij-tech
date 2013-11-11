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
 * User: varkel
 * Date: 11/11/13
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class JStop implements JCommandInstance {
        private JPanel panel;
        private JComboBox module;
        private JTextField custom;

        String title = "";
        Map<JCheckBox, String> checkBoxToArgument;

        public JStop() {
            checkBoxToArgument = new HashMap<JCheckBox, String>();
        }

    public JCommandType getType() {
        return JCommandType.J_STOP;
    }

    public String getTitle() {
        return title;
    }

    public List<ProcessBuilder> getCommands() {
        List<String> processNameAndArguments = new ArrayList<String>();
        processNameAndArguments.add("j-vm j-stop");

        for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
            if (entry.getKey().isSelected())
                processNameAndArguments.add(entry.getValue());

        processNameAndArguments.add(getCustomAttributes());

        String moduleName = getModuleName();
        processNameAndArguments.add(moduleName);

        return Lists.newArrayList(
                new ProcessBuilder(processNameAndArguments).directory(new File(System.getenv("J_BIN")))
                        .redirectErrorStream(true)
        );
    }

    private String getCustomAttributes() {
        return custom.getText() == null ? "" : custom.getText();
    }

    private String getModuleName() {
        return module.getSelectedItem() == null ? "" : module.getSelectedItem().toString();
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getCommandStr() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("module", getModuleName());
            jsonObject.put("custom", getCustomAttributes());

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
            module.setSelectedItem(jsonObject.getString("module"));
            custom.setText(jsonObject.getString("custom"));

            for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
                entry.getKey().setSelected(jsonObject.getBoolean(entry.getValue()));
        }
        catch (JSONException e) {
            return;
        }
    }
}
