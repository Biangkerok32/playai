package com.jimx.playapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AppInvClient aiClient;

    private ListView lvProjects;
    private Map<Long, CachedProject> projectCache;

    private TextView tvError;

    private EditText edtReplKey;
    private Button btnConnectRepl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvError = (TextView) findViewById(R.id.tvError);
        lvProjects = (ListView) findViewById(R.id.lvProjects);
        edtReplKey = (EditText) findViewById(R.id.edtReplKey);
        btnConnectRepl = (Button) findViewById(R.id.btnConnectRepl);
        projectCache = new HashMap<>();

        Intent intent = getIntent();
        aiClient = (AppInvClient) intent.getSerializableExtra("client");

        lvProjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProjectHandle handle = aiClient.getProjectHandleList().get(position);
                showProject(handle);
            }
        });

        btnConnectRepl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("isRepl", true);
                intent.putExtra("replKey", edtReplKey.getText().toString());
                intent.setClass(MainActivity.this, AppContainer.class);
                startActivity(intent);
            }
        });

        //updateProjectList();
    }

    private void updateProjectList() {
        List<ProjectHandle> projs = aiClient.getProjectHandleList();
        String[] strs = new String[projs.size()];
        for (int i = 0; i < strs.length; i++) {
            strs[i] = projs.get(i).getName();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, strs);
        lvProjects.setAdapter(arrayAdapter);
    }

    private void showProject(ProjectHandle handle) {
        CachedProject cp = projectCache.get(handle.getId());
        if (cp != null) {
            showCachedProject(cp);
        }

        aiClient.loadProject(handle, new AppInvClient.LoadProjectCallback() {
            @Override
            public void onSuccess(CachedProject project) {
                projectCache.put(project.getHandle().getId(), project);
                showCachedProject(project);
            }

            @Override
            public void onFailure() {
                tvError.setText("Failed to load project, please try again");
            }
        });
    }

    private void showCachedProject(CachedProject cp) {
        try {
            String yail = cp.buildYailForScreen("Screen1");

            Intent intent = new Intent();
            intent.putExtra("isRepl", false);
            intent.putExtra("yailCode", yail);
            intent.setClass(MainActivity.this, AppContainer.class);
            startActivity(intent);
        } catch (JSONException e) {
            tvError.setText("Server internal error");
            e.printStackTrace();
        }
    }
}
