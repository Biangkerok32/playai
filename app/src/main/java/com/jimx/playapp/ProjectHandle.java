package com.jimx.playapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jimx on 17-4-21.
 */
public class ProjectHandle implements Serializable {
    private long id;
    private String name;

    private Date dateCreated;
    private Date dateModified;

    public static ProjectHandle fromJsonObject(JSONObject obj) throws JSONException {
        ProjectHandle projectHandle = new ProjectHandle();

        projectHandle.id = obj.getLong("id");
        projectHandle.name = obj.getString("name");
        projectHandle.dateCreated = new Date(obj.getLong("dateCreated"));
        projectHandle.dateModified = new Date(obj.getLong("dateModified"));

        return projectHandle;
    }

    public static List<ProjectHandle> fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        List<ProjectHandle> projectHandleList = new ArrayList<ProjectHandle>();

        JSONArray projects = jsonObject.getJSONArray("projects");
        for (int i = 0; i < projects.length(); i++) {
            projectHandleList.add(fromJsonObject(projects.getJSONObject(i)));
        }

        return projectHandleList;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
