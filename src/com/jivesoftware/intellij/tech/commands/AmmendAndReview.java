package com.jivesoftware.intellij.tech.commands;

import com.google.common.collect.Lists;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lev.waisberg
 * Date: 11/11/13
 * Time: 10:14 AM
 */
public class AmmendAndReview implements JCommandInstance {
    private JPanel panel1;

    public JCommandType getType() {
        return  JCommandType.AMEND_REVIEW;
    }

    public List<ProcessBuilder> getCommands() {
        return Lists.newArrayList(
                new ProcessBuilder("git", "commit", "--amend", "-q").directory(new File(System.getenv("J_BIN"))).redirectErrorStream(true),
                new ProcessBuilder("git", "review").directory(new File(System.getenv("J_BIN"))).redirectErrorStream(true)
        );
    }

    public JPanel getPanel() {
        return panel1;
    }

    public String getCommandStr() {
        return "";
    }

    public void LoadCommand(String commandStr) {
    }
}
