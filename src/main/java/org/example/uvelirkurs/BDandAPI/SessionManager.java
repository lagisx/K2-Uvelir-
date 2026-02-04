package org.example.uvelirkurs.BDandAPI;

import org.json.JSONObject;

public class SessionManager {

    private static JSONObject currentUser;

    public static void login(JSONObject user) {
        currentUser = user;
    }

    public static JSONObject getUser() {
        return currentUser;
    }
    public static void setUser(JSONObject user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
