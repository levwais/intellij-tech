package com.jivesoftware.intellij.tech.commands;

import sun.reflect.Reflection;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: lev.waisberg
 * Date: 11/10/13
 * Time: 5:37 PM
 */
public enum JCommandType {
    J_DEPLOYABLE("j {deployable}", JDeployable.class),
    AMEND_REVIEW("commit amend + review", AmmendAndReview.class),
    GIT_REVIEW("git review", GitReview.class),
    MVN("mvn command", MvnCommand.class),
    J_VM("j-vm {command}", Jvm.class),
    CUSTOM("custom command(s)", CustomCommand.class);

    public static JCommandType getByName(String name) {
        for (JCommandType jCommandType : values()) {
            if (jCommandType.name.equalsIgnoreCase(name)) {
                return jCommandType;
            }
        }
        return null;
    }

    private String name;
    private Class instanceClass;

    private JCommandType(String name, Class instanceClass) {
        this.name = name;
        this.instanceClass = instanceClass;
    }

    public String getName() {
        return name;
    }

    public JCommandInstance getInstance() {
        try {
            return (JCommandInstance)instanceClass.newInstance();
        }
        catch (InstantiationException e) {
            return null;
        }
        catch (IllegalAccessException e) {
            return null;
        }
    }

    public String toString() {
        return name;
    }
}

