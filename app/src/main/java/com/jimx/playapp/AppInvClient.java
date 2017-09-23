package com.jimx.playapp;

/**
 * Created by jimx on 17-4-15.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/* The HTTP client that communicates with the AI server */
public class AppInvClient implements Serializable {

    public static final String AI_SERVER = "http://appinventor-164806.appspot.com";
    public static final String LOGIN_URL = AI_SERVER + "/login";
    public static final String USER_PROFILE_URL = AI_SERVER + "/ode/user_profile";
    public static final String PROJECT_LIST_URL = AI_SERVER + "/ode/project_list";
    public static final String PROJECT_INFO_URL = AI_SERVER + "/ode/project_info";
    public static final String PROJECT_LOAD_URL = AI_SERVER + "/ode/project_load";
    public static final String REPL_SESSION_URL = AI_SERVER + "/rendezvous2/session";

    private static final String COOKIES_HEADER = "Set-Cookie";
    private static final String TAG = "AppInvClient";

    private String cookies;

    private UserProfile userProfile;
    private List<ProjectHandle> projectHandleList;

    public AppInvClient() {
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public List<ProjectHandle> getProjectHandleList() {
        return projectHandleList;
    }

    private String urlGet(URL url, boolean withCookies) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        if (withCookies) attachCookies(urlConnection);
        urlConnection.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                urlConnection.getInputStream()));
        String lines;
        StringBuffer buffer = new StringBuffer();
        while ((lines = reader.readLine()) != null) {
            buffer.append(lines);
        }
        reader.close();
        urlConnection.disconnect();

        return buffer.toString();
    }

    private String urlGet(URL url) throws IOException {
        return urlGet(url, true);
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        private LoginCallback callback;

        public LoginTask(LoginCallback cb) {
            callback = cb;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                /* Log in to the server */
                URL url = new URL(LOGIN_URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setUseCaches(false);
                urlConnection.connect();

                DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
                String content = "email=" + params[0] + "&password=" + params[1];
                out.writeBytes(content);
                out.flush();
                out.close();

                Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
                if (cookiesHeader == null) {
                    return "Wrong email or password";
                }

                for (String cookie : cookiesHeader) {
                    cookies = cookie;
                }

                /* Fetch user profile */
                String userJson = urlGet(new URL(USER_PROFILE_URL));
                userProfile = UserProfile.fromJson(userJson);

                /* Fetch project list */
                String projectsJson = urlGet(new URL(PROJECT_LIST_URL));
                projectHandleList = ProjectHandle.fromJson(projectsJson);

                return "OK";
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                return null;
            } catch (JSONException e) {
                return "Server internal error";
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                callback.onFailure("Network error while logging in, please check your connection");
            } else if (!s.equals("OK")) {
                callback.onFailure(s);
            } else {
                callback.onSuccess(AppInvClient.this);
            }
        }
    }

    public interface LoginCallback {
        void onSuccess(AppInvClient client);
        void onFailure(String reason);
    }

    public void login(String email, String password, LoginCallback cb) {
        new LoginTask(cb).execute(email, password);
    }

    public void attachCookies(HttpURLConnection conn) {
        if (cookies != null) {
            conn.setRequestProperty("Cookie", cookies);
        }
    }

    public interface LoadProjectCallback {
        void onSuccess(CachedProject project);
        void onFailure();
    }

    private class LoadProjectTask extends AsyncTask<ProjectHandle, Void, CachedProject> {
        private LoadProjectCallback callback;

        public LoadProjectTask(LoadProjectCallback cb) {
            callback = cb;
        }

        @Override
        protected CachedProject doInBackground(ProjectHandle... params) {
            ProjectHandle handle = params[0];
            try {
                URL url = new URL(PROJECT_INFO_URL + "/" + handle.getId());
                String projectInfoJson = urlGet(url);
                Log.i(TAG, projectInfoJson);
                JSONObject projectInfoObj = new JSONObject(projectInfoJson);
                if (!projectInfoObj.getBoolean("ok")) return null;

                JSONObject rootNode = projectInfoObj.getJSONObject("rootNode");
                String projectType = rootNode.getString("type");
                if (!projectType.equals(CachedProject.YOUNG_ANDROID_TYPE)) return null;

                CachedProject cp = new CachedProject(handle);
                JSONArray sources = rootNode.getJSONArray("sources");
                for (int i = 0; i < sources.length(); i++) {
                    JSONObject source = sources.getJSONObject(i);
                    String name = source.getString("name");
                    String fileId = source.getString("fileId");

                    URL loadUrl = new URL(PROJECT_LOAD_URL + "/" + handle.getId() + "/" + fileId);
                    String contentJson = urlGet(loadUrl);
                    JSONObject contentObj = new JSONObject(contentJson);

                    if (!contentObj.getBoolean("ok")) return null;
                    String content = contentObj.getString("content");

                    cp.addSource(name, fileId, content);
                }
                return cp;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(CachedProject cp) {
            super.onPostExecute(cp);
            if (cp == null) {
                callback.onFailure();
            } else {
                callback.onSuccess(cp);
            }
        }
    }

    public void loadProject(ProjectHandle handle, LoadProjectCallback cb) {
        new LoadProjectTask(cb).execute(handle);
    }
}

