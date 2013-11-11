package com.jivesoftware.intellij.tech.commands;

import com.google.common.collect.Lists;
import com.intellij.ide.util.PropertiesComponent;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: lev.waisberg
 * Date: 11/11/13
 * Time: 5:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelUpgrade implements JCommandInstance {
    private JPanel panel1;
    private JCheckBox environmentCheckBox;
    private JComboBox envTxt;
    private JCheckBox tenantCheckBox;
    private JCheckBox serverCheckBox;
    private JComboBox tenantTxt;
    private JTextField serverTxt;
    private JCheckBox portCheckBox;
    private JFormattedTextField portTxt;
    private JCheckBox skipBuildBCheckBox;
    private JTextField customTxt;

    Map<JCheckBox, String> checkBoxToArgument;

    public ModelUpgrade() {
        portTxt.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter()));

        checkBoxToArgument = new HashMap<JCheckBox, String>();
        checkBoxToArgument.put(skipBuildBCheckBox, "-b");

        environmentCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    envTxt.setEnabled(true);
                }
                else {
                    envTxt.setEnabled(false);
                }
            }
        });
        tenantCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    tenantTxt.setEnabled(true);
                }
                else {
                    tenantTxt.setEnabled(false);
                }
            }
        });
        portCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    portTxt.setEnabled(true);
                }
                else {
                    portTxt.setEnabled(false);
                }
            }
        });
        serverCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    serverTxt.setEnabled(true);
                }
                else {
                    serverTxt.setEnabled(false);
                }
            }
        });

        final PropertiesComponent instance = PropertiesComponent.getInstance();
        final String[] tenants = instance.getValues("tenants");
        if (tenants != null) {
            tenantTxt.setModel(new DefaultComboBoxModel(tenants));
        }
    }

    public JCommandType getType() {
        return JCommandType.MODEL_UPGRADE;
    }

    public List<ProcessBuilder> getCommands() {
        List<String> processNameAndArguments = new ArrayList<String>();
        processNameAndArguments.add("j-model-upgrade");

        for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
            if (entry.getKey().isSelected())
                processNameAndArguments.add(entry.getValue());

        if (environmentCheckBox.isSelected()) {
            processNameAndArguments.add("-e");
            processNameAndArguments.add(envTxt.getSelectedItem().toString());
        }

        if (tenantCheckBox.isSelected()) {
            processNameAndArguments.add("-t");
            processNameAndArguments.add(tenantTxt.getSelectedItem().toString());
        }

        if (serverCheckBox.isSelected()) {
            processNameAndArguments.add("-s");
            processNameAndArguments.add(serverTxt.getText());
        }

        if (portCheckBox.isSelected()) {
            processNameAndArguments.add("-p");
            processNameAndArguments.add(portTxt.getText());
        }

        if (!customTxt.getText().trim().isEmpty()) {
            processNameAndArguments.addAll(Lists.newArrayList(customTxt.getText().trim().split(" ")));
        }


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
            jsonObject.put("environment", envTxt.getSelectedItem().toString());
            jsonObject.put("environmentChecked", environmentCheckBox.isSelected());
            jsonObject.put("tenant", tenantTxt.getSelectedItem().toString());
            updateTenants(tenantTxt.getSelectedItem().toString());
            jsonObject.put("tenantChecked", tenantCheckBox.isSelected());
            jsonObject.put("server", serverTxt.getText());
            jsonObject.put("serverChecked", serverCheckBox.isSelected());
            jsonObject.put("port", portTxt.getText());
            jsonObject.put("portChecked", portCheckBox.isSelected());
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
            envTxt.setSelectedItem(jsonObject.getString("environment"));
            environmentCheckBox.setSelected(jsonObject.getBoolean("environmentChecked"));
            tenantTxt.setSelectedItem(jsonObject.getString("tenant"));
            tenantCheckBox.setSelected(jsonObject.getBoolean("tenantChecked"));
            serverTxt.setText(jsonObject.getString("server"));
            serverCheckBox.setSelected(jsonObject.getBoolean("serverChecked"));
            portTxt.setText(jsonObject.getString("port"));
            portCheckBox.setSelected(jsonObject.getBoolean("portChecked"));
            customTxt.setText(jsonObject.getString("custom"));

            for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
                entry.getKey().setSelected(jsonObject.getBoolean(entry.getValue()));
        }
        catch (JSONException e) {
            return;
        }

    }

    private void updateTenants(String tenant) {
        final PropertiesComponent instance = PropertiesComponent.getInstance();
        final String[] tenants = instance.getValues("tenants");
        if (tenants != null) {
            for (String existingT : tenants) {
                if (existingT.equalsIgnoreCase(tenant)) {
                    return;
                }
            }
            instance.setValues("tenants", (String[]) Lists.asList(tenant, tenants).toArray());
        }
        else {
            instance.setValues("tenants", new String[]{tenant});
        }

    }


}
