package com.jivesoftware.intellij.tech.commands;

import javax.swing.*;

public interface JCommandInstance {
    JCommandType getType();

    String getTitle();

    ProcessBuilder getCommand();

    JPanel getPanel();
}
