package com.jivesoftware.intellij.tech;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.jivesoftware.intellij.tech.commands.JCommandInstance;
import com.jivesoftware.intellij.tech.commands.JCommandType;
import org.codehaus.jettison.json.JSONException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;

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
    private DefaultMutableTreeNode favNode;
    private DefaultMutableTreeNode savedNode;
    private Tree tree;
    private DefaultMutableTreeNode currentNode;
    private Map<String, Command> id2CommandMap = Maps.newHashMap();
    private Process currentProcess = null;

    private JPanel panel1;
    private JComboBox comboBox1;
    private JPanel commandPanel;
    private JButton saveButton;
    private JButton runButton;
    private JButton addToFavsButton;
    private JButton addCommandBtn;
    private JPanel treePanel;
    private JTextField commandTitleTxt;
    private JButton duplicateButton;
    private JButton removeBtn;
    private Project project;
    private boolean running = false;

    public JCommandInstance currentInstance;

    public JToolWindow() {
        addCommandBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                currentNode = null;
                commandTitleTxt.setText("");
                final JCommandType selectedItem = (JCommandType)comboBox1.getSelectedItem();
                final JCommandInstance instance = selectedItem.getInstance();
                loadInstance(instance);
            }
        });
        runButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (running) {
                    currentProcess.destroy();

                    try {
                        runButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResource("/resources/play.png"))));
                    }
                    catch (IOException e1) {
                        // ignore
                    }
                    runButton.setToolTipText("Run");
                    running = false;
                }
                else {
                    new Thread(new Runnable() {
                        public void run() {
                            runCurrentCommand();
                        }
                    }).start();

                    EventLog.getEventLog(project).show(null);


                    try {
                        runButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResource("/resources/stop.png"))));
                    }
                    catch (IOException e1) {
                        // ignore
                    }
                    runButton.setToolTipText("Stop");
                    running = true;
                }
            }
        });

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveInstance();
            }
        });

        removeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (tree.getLastSelectedPathComponent() != null) {
                    if (tree.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode) {
                        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        final Object userObject = selectedNode.getUserObject();
                        if (userObject != null && userObject instanceof Command) {
                            if (selectedNode.getParent() == savedNode) {
                                savedNode.remove(selectedNode);
                                storeSavedNodes();
                                id2CommandMap.remove(((Command)userObject).getId());

                                loadFavCommands();
                                storeFavNodes();
                            }
                            else if (selectedNode.getParent() == favNode) {
                                favNode.remove(selectedNode);
                                storeFavNodes();
                            }

                            tree.updateUI();
                            return;
                        }
                    }
                }

                Messages.showErrorDialog("You must select a command before doing this action.", "No Selected Command");
            }
        });

        duplicateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (tree.getLastSelectedPathComponent() != null) {
                    if (tree.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode) {
                        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        final Object userObject = selectedNode.getUserObject();
                        if (userObject != null && userObject instanceof Command) {
                            final Command currentCommand = (Command) userObject;
                            Command newCommand = new Command(currentCommand.getName() + "#2", currentCommand.getCommandStr(), UUID.randomUUID().toString(), currentCommand.getType());
                            loadInstance(currentCommand.getType().getInstance());

                            addCommandToSavedNode(newCommand);

                            return;
                        }
                    }
                }

                Messages.showErrorDialog("You must select a command before doing this action.", "No Selected Command");
            }
        });

        addToFavsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (tree.getLastSelectedPathComponent() != null) {
                    if (tree.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode) {
                        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        final Object userObject = selectedNode.getUserObject();
                        if (userObject != null && userObject instanceof Command) {
                            if (selectedNode.getParent() == savedNode) {
                                favNode.add(new DefaultMutableTreeNode(userObject));
                                storeFavNodes();

                                tree.updateUI();
                                return;
                            }
                        }
                    }
                }

                Messages.showErrorDialog("You must select a command before doing this action.", "No Selected Command");
            }
        });
    }



    private void saveInstance() {
        final String title = commandTitleTxt.getText().trim();
        if (title.isEmpty()) {
            Messages.showErrorDialog("Please add a title for you command before saving.", "No Command Title");
            return;
        }

        if (currentNode == null) { // no current node
            final String id = UUID.randomUUID().toString();
            final Command command = new Command(title, currentInstance.getCommandStr(), id, currentInstance.getType());
            currentNode = addCommandToSavedNode(command);
        }
        else {
            final Command command = (Command) currentNode.getUserObject();
            command.setCommandStr(currentInstance.getCommandStr());
            command.setName(title);
            currentNode.setUserObject(command);
        }

        tree.expandPath(new TreePath(currentNode));
        tree.updateUI();

        storeSavedNodes();
    }

    private DefaultMutableTreeNode addCommandToSavedNode(Command command) {
        id2CommandMap.put(command.getId(), command);
        final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(command);
        savedNode.add(newNode);

        tree.updateUI();

        return newNode;
    }

    private void storeSavedNodes() {
        final PropertiesComponent instance = PropertiesComponent.getInstance(project);
        java.util.List<String> commands = Lists.newArrayList();
        for (int i = 0; i < savedNode.getChildCount(); i++) {
            final TreeNode childNode = savedNode.getChildAt(i);
            if (childNode instanceof DefaultMutableTreeNode) {
                final Object userObject = ((DefaultMutableTreeNode) childNode).getUserObject();
                if (userObject instanceof Command) {
                    try {
                        commands.add(Command.getString(((Command)userObject)));
                    }
                    catch (JSONException e) {
                        // ignore
                    }
                }
            }
        }
        instance.setValues("savedCommands", commands.toArray(new String[commands.size()]));
    }

    private void loadSavedCommands() {
        savedNode.removeAllChildren();
        for (String commandStr : PropertiesComponent.getInstance(project).getValues("savedCommands")) {
            try {
                addCommandToSavedNode(Command.getCommand(commandStr));
            }
            catch (JSONException e) {
                // ignore
            }
        }
        tree.expandPath(new TreePath(savedNode.getPath()));
        tree.updateUI();
    }
    private void storeFavNodes() {
        final PropertiesComponent instance = PropertiesComponent.getInstance(project);
        java.util.List<String> commandIds = Lists.newArrayList();
        for (int i = 0; i < favNode.getChildCount(); i++) {
            final TreeNode childNode = favNode.getChildAt(i);
            if (childNode instanceof DefaultMutableTreeNode) {
                final Object userObject = ((DefaultMutableTreeNode) childNode).getUserObject();
                if (userObject instanceof Command) {
                    commandIds.add(((Command) userObject).getId());
                }
            }
        }
        instance.setValues("favCommandIds", commandIds.toArray(new String[commandIds.size()]));
    }

    private void loadFavCommands() {
        favNode.removeAllChildren();
        for (String commandId : PropertiesComponent.getInstance(project).getValues("favCommandIds")) {
            final Command command = id2CommandMap.get(commandId);
            if (command != null) {
                favNode.add(new DefaultMutableTreeNode(command));
            }
        }
        tree.expandPath(new TreePath(favNode.getPath()));
        tree.updateUI();
    }



    private void loadInstance(JCommandInstance instance) {
        currentInstance = instance;

        commandPanel.removeAll();
        commandPanel.add(currentInstance.getPanel(), BorderLayout.CENTER);
        commandPanel.updateUI();

        addToFavsButton.setEnabled(true);
        runButton.setEnabled(true);
        saveButton.setEnabled(true);
    }



    private void runCurrentCommand() {
        final Notifications notifications = project.getMessageBus().syncPublisher(Notifications.TOPIC);

        boolean error = false;
        for (ProcessBuilder command : currentInstance.getCommands()) {
            if (!runCommand(notifications, command)) {
                error = true;
                break;
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
        currentProcess = null;
        boolean error = false;
        notifications.notify(new Notification(NOTIFICATION_LOG_GROUP, NOTIFICATION_TITLE, "*** Running command: " +
                StringUtil.join(command.command(), " "),
                NotificationType.INFORMATION));
        try {
            currentProcess = command.start();
        }
        catch (IOException e) {
            notifications.notify(new Notification(NOTIFICATION_LOG_GROUP, NOTIFICATION_TITLE, "failed to run: " + e.getMessage(),
                    NotificationType.ERROR));
            error = true;
        }

        if (currentProcess != null) {
            InputStream output = currentProcess.getInputStream();
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
                        if (0 != currentProcess.exitValue()) {
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
                error = true;
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

        loadSavedCommands();
        loadFavCommands();
    }

    private void initTree(JPanel treePanel) {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("J-Tech Commands");
        favNode = new DefaultMutableTreeNode("<html>Favorites</html>");
        savedNode = new DefaultMutableTreeNode("<html>Saved</html>");
        root.add(favNode);
        root.add(savedNode);
        tree = new Tree(root);
        tree.setShowsRootHandles(false);
        tree.setCellRenderer(new TreeRenderer(favNode, savedNode));
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                treeNodeMouseClicked(e);
            }

        });
        tree.setRootVisible(false);

        treePanel.add(tree, BorderLayout.CENTER);
        treePanel.updateUI();

    }

    private void treeNodeMouseClicked(MouseEvent me) {
        if (me.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        TreePath tp = tree.getPathForLocation(me.getX(), me.getY());
        if (tp == null) {
            return;
        }
        final Object node = tp.getLastPathComponent();
        if (node instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode)node).getUserObject() instanceof Command) {
            currentNode = (DefaultMutableTreeNode) node;

            final Command command = (Command) currentNode.getUserObject();
            final JCommandInstance instance = command.getType().getInstance();
            instance.LoadCommand(command.getCommandStr());
            loadInstance(instance);

            commandTitleTxt.setText(command.getName());

            if (me.getClickCount() >= 2) {
                runButton.doClick();
            }
        }
    }

    class TreeRenderer extends DefaultTreeCellRenderer {
        private ImageIcon saveIcon;
        private ImageIcon favIcon;
        Object favoritesNode;
        private DefaultMutableTreeNode savedNode;

        TreeRenderer(Object favoritesNode, DefaultMutableTreeNode savedNode) {
            this.favoritesNode = favoritesNode;
            this.savedNode = savedNode;
            try {
                favIcon = new ImageIcon(ImageIO.read(getClass().getResource("/resources/icon_star.gif")));
                saveIcon = new ImageIcon(ImageIO.read(getClass().getResource("/resources/save_icon.png")));
            }
            catch (IOException e) {
            }
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                      boolean leaf,
                                                      int row, boolean hasFocus)
        {
            final Component component = super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);
            if (favoritesNode == value) {
                setIcon(favIcon);
            }
            else if (savedNode == value) {
                setIcon(saveIcon);
            }

            return component;
        }
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
