package com.jivesoftware.intellij.tech;

import com.jivesoftware.intellij.tech.commands.JCommandType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Command {
    String name;
    String commandStr;
    String id;
    JCommandType type;

    public static String getString(Command command) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", command.name);
        jsonObject.put("commandStr", command.commandStr);
        jsonObject.put("id", command.id);
        jsonObject.put("type", command.type.getName());

        return jsonObject.toString();
    }

    public static Command getCommand(String commandStr) throws JSONException {
        final JSONObject jsonObject = new JSONObject(commandStr);

        return new Command(jsonObject.getString("name"), jsonObject.getString("commandStr"), jsonObject.getString("id"), JCommandType.getByName(jsonObject.getString("type")));
    }

    public Command(String name, String commandStr, String id, JCommandType type) {
        this.name = name;
        this.commandStr = commandStr;
        this.id = id;
        this.type = type;
    }

    public JCommandType getType() {
        return type;
    }

    public void setType(JCommandType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommandStr() {
        return commandStr;
    }

    public void setCommandStr(String commandStr) {
        this.commandStr = commandStr;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        return "<html>" + name + "</html>";
    }
}
