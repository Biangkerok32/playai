package com.jimx.playapp;

/**
 * Created by jimx on 17-4-21.
 */

import org.json.*;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private boolean tosAccepted;
    private String userId;

    public static UserProfile fromJson(String json) throws JSONException {
        JSONObject rt = new JSONObject(json);

        UserProfile user = new UserProfile();
        user.tosAccepted = rt.getBoolean("tosAccepted");
        user.userId = rt.getString("userId");

        return user;
    }

    public String getUserId() {
        return userId;
    }
}

