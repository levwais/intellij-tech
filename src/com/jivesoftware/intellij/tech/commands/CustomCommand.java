package com.jivesoftware.intellij.tech.commands;

import com.google.common.collect.Lists;
import com.intellij.openapi.ui.Messages;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lev.waisberg
 * Date: 11/11/13
 * Time: 3:39 PM
 */
public class CustomCommand implements JCommandInstance {
    private JPanel panel1;
    private JButton button1;
    private JTextField runFromTxt;
    private JTextField commandTxt;
    private JList list1;
    private JButton removeFromListButton;
    private JButton addToListButton;
    private JButton downBtn;
    private JButton upBtn;

    public class Command {
        String command;
        String directory;

        public Command(String command, String directory) {
            this.command = command;
            this.directory = directory;
        }

        public String toString() {
            return "<html>"+command+"</html>";
        }
    }


    public CustomCommand() {
        list1.setModel(new DefaultListModel());
        addToListButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (runFromTxt.getText().isEmpty()) {
                    Messages.showErrorDialog("Please fill the folder to run the command from.", "Folder Missing");
                    return;
                }
                if (commandTxt.getText().isEmpty()) {
                    Messages.showErrorDialog("Please fill the command to run.", "Command Missing");
                    return;
                }

                ((DefaultListModel)list1.getModel()).addElement(new Command(commandTxt.getText().trim(), runFromTxt.getText().trim()));
                list1.updateUI();
            }
        });
        removeFromListButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list1.getSelectedValue() == null) {
                    Messages.showErrorDialog("Please select a command to remove it.", "Command not selected");
                    return;
                }

                commandTxt.setText(((Command)list1.getSelectedValue()).command);
                runFromTxt.setText(((Command) list1.getSelectedValue()).directory);
                ((DefaultListModel)list1.getModel()).remove(list1.getSelectedIndex());
                list1.updateUI();
            }
        });
        upBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list1.getSelectedValue() == null) {
                    Messages.showErrorDialog("Please select a command to move it.", "Command not selected");
                    return;
                }
                int indexOfSelected = list1.getSelectedIndex();
                if (indexOfSelected == 0) {
                    return; // already up
                }
                swapElements(indexOfSelected, indexOfSelected - 1);
                indexOfSelected = indexOfSelected - 1;
                list1.setSelectedIndex(indexOfSelected);
                list1.updateUI();
            }
        });
        downBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list1.getSelectedValue() == null) {
                    Messages.showErrorDialog("Please select a command to move it.", "Command not selected");
                    return;
                }
                int indexOfSelected = list1.getSelectedIndex();
                if (indexOfSelected == ((DefaultListModel) list1.getModel()).size() - 1) {
                    return; // already down
                }
                swapElements(indexOfSelected, indexOfSelected + 1);
                indexOfSelected = indexOfSelected + 1;
                list1.setSelectedIndex(indexOfSelected);
                list1.updateUI();
            }
        });

        runFromTxt.setText(System.getenv("J_CODE"));
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser folderChooser = new JFileChooser(runFromTxt.getText());
                folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (folderChooser.showOpenDialog(button1) == JFileChooser.APPROVE_OPTION) {
                    runFromTxt.setText(folderChooser.getSelectedFile().getPath());
                }
            }
        });
    }

    private void swapElements(int pos1, int pos2) {
        final DefaultListModel listModel = (DefaultListModel) list1.getModel();
        Command tmp = (Command) listModel.get(pos1);
        listModel.set(pos1, listModel.get(pos2));
        listModel.set(pos2, tmp);
    }

    public JCommandType getType() {
        return JCommandType.CUSTOM;
    }

    public List<ProcessBuilder> getCommands() {
        List<ProcessBuilder> result = Lists.newArrayList();
        final DefaultListModel model = (DefaultListModel) list1.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            final Command command = (Command) model.get(i);
            result.add(new ProcessBuilder(command.command.split(" ")).directory(new File(command.directory)));
        }
        return result;
    }

    public JPanel getPanel() {
        return panel1;
    }

    public String getCommandStr() {
        try {
            JSONObject json = new JSONObject();
            final JSONArray array = new JSONArray();
            final DefaultListModel model = (DefaultListModel) list1.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                final Command command = (Command) model.get(i);
                final JSONObject commandJson = new JSONObject();
                commandJson.put("command", command.command);
                commandJson.put("dir", command.directory);
                array.put(commandJson);

            }
            json.put("commands", array);
            return json.toString();
        }
        catch (JSONException e) {
            return null;
        }
    }

    public void LoadCommand(String commandStr) {
        try {
            JSONObject json = new JSONObject(commandStr);
            final JSONArray commands = json.getJSONArray("commands");
            final DefaultListModel model = (DefaultListModel) list1.getModel();

            for (int i = 0; i < commands.length(); i++) {
                JSONObject commandJson = commands.getJSONObject(i);
                model.addElement(new Command(commandJson.getString("command"), commandJson.getString("dir")));
            }
        }
        catch (JSONException e) {
            return;
        }
        list1.updateUI();
    }
}
