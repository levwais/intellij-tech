package com.jivesoftware.intellij.tech;

import com.jivesoftware.intellij.tech.commands.JCommandType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: lev.waisberg
 * Date: 11/11/13
 * Time: 11:39 AM
 */
public class CommandSerializer {
    public String getString(Command command) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", command.name);
        jsonObject.put("commandStr", command.commandStr);
        jsonObject.put("id", command.id);
        jsonObject.put("type", command.type.getName());

        return jsonObject.toString();
    }

    public Command getCommand(String commandStr) throws JSONException {
        final JSONObject jsonObject = new JSONObject(commandStr);

        return new Command(jsonObject.getString("name"), jsonObject.getString("commandStr"), jsonObject.getString("id"), JCommandType.getByName(jsonObject.getString("type")));
    }
}

