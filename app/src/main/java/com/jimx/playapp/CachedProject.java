package com.jimx.playapp;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;

/**
 * Created by jimx on 17-4-21.
 */
public class CachedProject implements Serializable {
    private ProjectHandle handle;

    public static final String TAG = "CachedProject";

    public static final String YOUNG_ANDROID_TYPE = "YoungAndroid";
    public enum ProjectType {
        YOUNG_ANDROID_PROJECT,
        UNKNOWN_PROJECT,
    }
    private ProjectType type;

    public class SourceFile {
        private String name;
        private String fileId;
        private String content;

        public SourceFile(String name, String fileId, String content) {
            this.name = name;
            this.fileId = fileId;
            this.content = content;
        }

        public String getName() {
            return name;
        }

        public String getFileId() {
            return fileId;
        }

        public String getContent() {
            return content;
        }
    }
    List<SourceFile> sources;

    public CachedProject(ProjectHandle handle) {
        this.handle = handle;
        sources = new ArrayList<>();
    }

    public ProjectHandle getHandle() {
        return handle;
    }

    public void setHandle(ProjectHandle handle) {
        this.handle = handle;
    }

    public ProjectType getType() {
        return type;
    }

    public void addSource(String name, String fileId, String content) {
        sources.add(new SourceFile(name, fileId, content));
    }

    public String buildYailForScreen(String screenName) throws JSONException {
        SourceFile scmFile = null;
        for (SourceFile source: sources) {
            if (source.getName().equals(screenName + ".scm")) scmFile = source;
        }

        if (scmFile == null) {
            return "";
        }

        String scmJson = scmFile.getContent();
        /* Remove the #|JSON ... |# part */
        scmJson = scmJson.substring(9);
        scmJson = scmJson.substring(0, scmJson.length() - 2);
        JSONObject scmObj = new JSONObject(scmJson);
        JSONObject propObj = scmObj.getJSONObject("Properties");

        String yailCode = YailGenerator.getComponentLines("Screen1", propObj, "");
        return yailCode;
    }
}
