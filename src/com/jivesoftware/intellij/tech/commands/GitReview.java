package com.jivesoftware.intellij.tech.commands;

import com.google.common.collect.Lists;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lev.waisberg
 * Date: 11/11/13
 * Time: 10:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class GitReview implements JCommandInstance {
    private JPanel panel1;

    public JCommandType getType() {
        return JCommandType.AMEND_REVIEW;
    }

    public List<ProcessBuilder> getCommands() {
        return Lists.newArrayList(
                new ProcessBuilder("git", "review").directory(new File(System.getenv("J_BIN"))).redirectErrorStream(true)
        );

    }

    public JPanel getPanel() {
        return panel1;
    }
}
