package com.jivesoftware.intellij.tech;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.jivesoftware.intellij.tech.commands.JCommandInstance;
import com.jivesoftware.intellij.tech.commands.JCommandType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: lev.waisberg
 * Date: 11/10/13
 * Time: 11:46 AM
 */
public class JToolWindow implements ToolWindowFactory {

    public static final String NOTIFICATION_TITLE = "IntelliJ-Tech";
    public static final String NOTIFICATION_LOG_GROUP = NOTIFICATION_TITLE;
    public static final String NOTIFICATION_BALLOON_GROUP = "IntelliJ-Tech-Balloon";

    public class ProcessWatcher implements Runnable {

        private Process p;
        private volatile boolean finished = false;

        public ProcessWatcher(Process p) {
            this.p = p;
            new Thread(this).start();
        }

        public boolean isFinished() {
            return finished;
        }

        public void run() {
            try {
                p.waitFor();
            } catch (Exception e) {}
            finished = true;
        }

    }

    private JPanel panel1;
    private JComboBox comboBox1;
    private JPanel commandPanel;
    private JButton saveButton;
    private JButton runButton;
    private JButton addToFavsButton;
    private JButton addCommandBtn;
    private JPanel treePanel;
    private JTextField commandTitleTxt;
    private Project project;

    public JCommandInstance currentInstance;

    public JToolWindow() {
        addCommandBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final JCommandType selectedItem = (JCommandType)comboBox1.getSelectedItem();
                currentInstance = selectedItem.getInstance();

                commandPanel.removeAll();
                commandPanel.add(currentInstance.getPanel(), BorderLayout.CENTER);
                commandPanel.updateUI();

//                commandTitleTxt.setText(currentInstance.getTitle());
//                commandTitleTxt.updateUI();
//
            }
        });
        runButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    public void run() {
                        runJCommand();
                    }
                }).start();

                EventLog.getEventLog(project).show(null);
            }
        });
    }

    private void runJCommand() {
        final Notifications notifications = project.getMessageBus().syncPublisher(Notifications.TOPIC);

        boolean error = false;
        for (ProcessBuilder command : currentInstance.getCommands()) {
            if (!runCommand(notifications, command)) {
                error = true;
            }
        }

        if (error) {
            notifications.notify(new Notification(NOTIFICATION_BALLOON_GROUP, NOTIFICATION_TITLE, commandTitleTxt.getText() + " command finished with errors!",
                    NotificationType.ERROR));
        }
        else {
            notifications.notify(new Notification(NOTIFICATION_BALLOON_GROUP, NOTIFICATION_TITLE, commandTitleTxt.getText() + " command finished!",
                    NotificationType.INFORMATION));
        }

    }

    private boolean runCommand(Notifications notifications, ProcessBuilder command) {
        boolean error = false;
        notifications.notify(new Notification(NOTIFICATION_LOG_GROUP, NOTIFICATION_TITLE, "*** Running command: " +
                StringUtil.join(command.command(), " "),
                NotificationType.INFORMATION));
        Process process = null;
        try {
            process = command.start();
        }
        catch (IOException e) {
            notifications.notify(new Notification(NOTIFICATION_LOG_GROUP, NOTIFICATION_TITLE, "failed to run: " + e.getMessage(),
                    NotificationType.ERROR));
        }

        if (process != null) {
            InputStream output = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(output);
            BufferedReader br = new BufferedReader(isr);


            String line;
            boolean exit = false;
            try {
                while ((line = br.readLine()) != null || !exit) {
                    if (line != null && !line.isEmpty()) {
                        notifications.notify(new Notification(NOTIFICATION_LOG_GROUP, NOTIFICATION_TITLE, line,
                                NotificationType.INFORMATION));
                    }
                    try {
                        if (0 != process.exitValue()) {
                            error = true;
                        }
                        exit = true;
                    } catch (IllegalThreadStateException t) {
                        // The process has not yet finished.
                        // Should we stop it?
                    }
                }
            }
            catch (IOException e) {
                notifications.notify(new Notification(NOTIFICATION_LOG_GROUP, NOTIFICATION_TITLE, "failed to run: " + e.getMessage(),
                        NotificationType.ERROR));
            }
        }
        return !error;
    }

    private void initNotifications() {
        NotificationsConfiguration.getNotificationsConfiguration().changeSettings(NOTIFICATION_LOG_GROUP,
                NotificationDisplayType.NONE, true);
        NotificationsConfiguration.getNotificationsConfiguration().changeSettings(NOTIFICATION_BALLOON_GROUP,
                NotificationDisplayType.BALLOON, false);
    }

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        this.project = project;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel1, "", false);
        toolWindow.getContentManager().addContent(content);


        initNotifications();
        initComboBox(comboBox1);
        initTree(treePanel);

        final PropertiesComponent instance = PropertiesComponent.getInstance(project);


    }

    private void initTree(JPanel treePanel) {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("J-Tech Commands");
        root.add(new DefaultMutableTreeNode("Favorites"));
        JTree tree = new Tree(root);

        treePanel.add(tree, BorderLayout.CENTER);
        treePanel.updateUI();
    }

    private void initComboBox(JComboBox comboBox) {
        for (JCommandType jCommandType : JCommandType.values()) {
            comboBox.addItem(jCommandType);
        }
    }

    public static void main(String args[]) {
        int x =1;
    }
}
