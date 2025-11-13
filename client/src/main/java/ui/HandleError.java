package ui;

import com.google.gson.Gson;

import java.util.HashMap;

public class HandleError {
    private static String message;

    public HandleError(String message) {
        this.message = message;
    }

    public String sendMessage(String message) {
        try {
            var map = new Gson().fromJson(message, HashMap.class);
            return map.get("message").toString();
        } catch (Exception e) {
            return message;
        }
    }
}
