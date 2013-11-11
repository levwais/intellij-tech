package com.jivesoftware.intellij.tech.commands;

import com.google.common.collect.Lists;
import com.sun.tools.javac.util.Pair;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: varkel
 * Date: 11/11/13
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class Jvm implements JCommandInstance {
    private JCheckBox b;
    private JCheckBox B;
    private JTextField BText;
    private JCheckBox c;
    private JCheckBox x;
    private JTextField xText;
    private JCheckBox f;
    private JCheckBox d;
    private JCheckBox s;
    private JCheckBox u;
    private JCheckBox n;
    private JCheckBox t;
    private JCheckBox p;
    private JCheckBox P;
    private JCheckBox R;
    private JCheckBox U;
    private JCheckBox l;
    private JCheckBox r;
    private JCheckBox D;
    private JCheckBox a;
    private JTextField aText;
    private JPanel panel;
    private JPanel panel2;

    private String title = "";
    Map<JCheckBox, String> checkBoxToArgument;
    Map<JCheckBox, Pair<String, JTextField>> checkBoxToTextAndArgument;

    public Jvm() {
        checkBoxToArgument = new HashMap<JCheckBox, String>();
        checkBoxToArgument.put(b, "-b");
        checkBoxToArgument.put(c, "-c");

        checkBoxToArgument.put(f, "-f");
        checkBoxToArgument.put(d, "-d");
        checkBoxToArgument.put(s, "-s");
        checkBoxToArgument.put(u, "-u");
        checkBoxToArgument.put(n, "-n");
        checkBoxToArgument.put(t, "-t");
        checkBoxToArgument.put(p, "-p");
        checkBoxToArgument.put(P, "-P");
        checkBoxToArgument.put(R, "-R");
        checkBoxToArgument.put(U, "-U");
        checkBoxToArgument.put(l, "-l");
        checkBoxToArgument.put(r, "-r");
        checkBoxToArgument.put(D, "-D");

        checkBoxToTextAndArgument = new HashMap<JCheckBox, Pair<String, JTextField>>();
        checkBoxToTextAndArgument.put(B, Pair.of("-B", BText));
        checkBoxToTextAndArgument.put(x, Pair.of("-x", xText));
        checkBoxToTextAndArgument.put(a, Pair.of("-x", aText));

        ChangeListener checkBoxChangeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                JCheckBox checkBox = (JCheckBox) changeEvent.getSource();
                Pair<String, JTextField> pair = checkBoxToTextAndArgument.get(checkBox);
                pair.snd.setEditable(checkBox.isSelected());
            }
        };

        for (Map.Entry<JCheckBox, Pair<String, JTextField>> entry : checkBoxToTextAndArgument.entrySet()) {
            entry.getKey().addChangeListener(checkBoxChangeListener);
        }

    }

    public JCommandType getType() {
        return JCommandType.J_VM;
    }

    public String getTitle() {
        return title;
    }

    public List<ProcessBuilder> getCommands() {
        List<String> processNameAndArguments = new ArrayList<String>();
        processNameAndArguments.add("j-vm");

        for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
            if (entry.getKey().isSelected())
                processNameAndArguments.add(entry.getValue());

        for (Map.Entry<JCheckBox, Pair<String, JTextField>> entry : checkBoxToTextAndArgument.entrySet())
            if (entry.getKey().isSelected())
                processNameAndArguments.add(entry.getValue().fst + " " + entry.getValue().snd.getText());

        return Lists.newArrayList(
                new ProcessBuilder(processNameAndArguments).directory(new File(System.getenv("J_BIN")))
                        .redirectErrorStream(true)
        );
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getCommandStr() {
        try {
            JSONObject jsonObject = new JSONObject();

            for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
                jsonObject.put(entry.getValue(), entry.getKey().isSelected());

            for (Map.Entry<JCheckBox, Pair<String, JTextField>> entry : checkBoxToTextAndArgument.entrySet())
                jsonObject.put(entry.getValue().fst, entry.getKey().isSelected() ? entry.getValue().snd.getText() : null);


            return jsonObject.toString();
        }
        catch (JSONException e) {
            return null;
        }
    }

    public void LoadCommand(String commandStr) {
        try {
            JSONObject jsonObject = new JSONObject(commandStr);

            for (Map.Entry<JCheckBox, String> entry : checkBoxToArgument.entrySet())
                entry.getKey().setSelected(jsonObject.getBoolean(entry.getValue()));

            for (Map.Entry<JCheckBox, Pair<String, JTextField>> entry : checkBoxToTextAndArgument.entrySet()) {
                String parameter = jsonObject.optString(entry.getValue().fst, null);
                if (parameter == null) {
                    entry.getKey().setSelected(false);
                }
                else {
                    entry.getKey().setSelected(true);
                    entry.getValue().snd.setText(parameter);
                }
            }
        }
        catch (JSONException e) {
            return;
        }
    }
}
