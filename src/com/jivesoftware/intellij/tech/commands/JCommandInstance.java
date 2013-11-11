package com.jivesoftware.intellij.tech.commands;

import javax.swing.*;
import java.util.List;

public interface JCommandInstance {
    JCommandType getType();

    List<ProcessBuilder> getCommands();

    JPanel getPanel();
}
