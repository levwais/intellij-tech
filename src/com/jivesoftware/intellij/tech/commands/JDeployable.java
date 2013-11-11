package com.jivesoftware.intellij.tech.commands;

import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: lev.waisberg
 * Date: 11/10/13
 * Time: 4:40 PM
 */
public class JDeployable implements JCommandInstance {

    private JPanel panel;
    private JCheckBox B;
    private JCheckBox d;
    private JCheckBox t;
    private JCheckBox c;
    private JCheckBox f;
    private JCheckBox r;
    private JCheckBox x;
    private JCheckBox X;
    private JCheckBox M;
    private JCheckBox l;
    private JComboBox module;

    String title = "";
    Map<JCheckBox, String> checkBoxToArgument;

    public JDeployable() {
        checkBoxToArgument = new HashMap<JCheckBox, String>();
        checkBoxToArgument.put(B, "-B");
        checkBoxToArgument.put(d, "-d");
        checkBoxToArgument.put(t, "-t");
        checkBoxToArgument.put(c, "-c");
        checkBoxToArgument.put(f, "-f");
        checkBoxToArgument.put(r, "-r");
        checkBoxToArgument.put(x, "-x");
        checkBoxToArgument.put(X, "-X");
        checkBoxToArgument.put(M, "-M");
        checkBoxToArgument.put(l, "-l");
    }

    public JCommandType getType() {
        return JCommandType.J_DEPLOYABLE;
    }

    public String getTitle() {
        return title;
    }

    public List<ProcessBuilder> getCommands() {
        List<String> processNameAndArguments = new ArrayList<String>();
        processNameAndArguments.add("j");
        String moduleName = getModuleName();
        processNameAndArguments.add(moduleName);

        for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
            if (entry.getKey().isSelected())
                processNameAndArguments.add(entry.getValue());

        return Lists.newArrayList(
                new ProcessBuilder(processNameAndArguments).directory(new File(System.getenv("J_BIN"))).redirectErrorStream(true)
        );
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

            for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
                entry.getKey().setSelected(jsonObject.getBoolean(entry.getValue()));
        }
        catch (JSONException e) {
            return;
        }
    }
}
