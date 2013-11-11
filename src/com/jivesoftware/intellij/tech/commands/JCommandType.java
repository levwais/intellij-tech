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
    J_VM("j-vm {command}", JDeployable.class);
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

