package com.jimx.playapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private TextView textError;
    private EditText edtEmail;
    private EditText edtPassword;
    private AppInvClient aiClient;

    private boolean busy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        aiClient = new AppInvClient();
        busy = false;

        textError = (TextView) findViewById(R.id.tvError);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        /*
        aiClient.login("test",
                "admin",
                new AppInvClient.LoginCallback() {
                    @Override
                    public void onSuccess(AppInvClient client) {
                        Intent intent = new Intent();
                        intent.putExtra("client", client);

                        intent.setClass(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(String reason) {
                        textError.setVisibility(View.VISIBLE);
                        textError.setText(reason);
                        busy = false;
                    }
                }); */

        String scm = "{\"authURL\":[\"appinventor-164806.appspot.com\"],\"YaVersion\":\"159\",\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\",\"$Type\":\"Form\",\"$Version\":\"20\",\"AppName\":\"test1\",\"Title\":\"Screen1\",\"Uuid\":\"0\",\"$Components\":[{\"$Name\":\"TextBox1\",\"$Type\":\"TextBox\",\"$Version\":\"5\",\"Hint\":\"Hint for TextBox1\",\"Uuid\":\"-1554897281\"},{\"$Name\":\"Button1\",\"$Type\":\"Button\",\"$Version\":\"6\",\"Text\":\"Text for Button1\",\"Uuid\":\"866681793\"}]}}";
        JSONObject scmObj = null;
        try {
            scmObj = new JSONObject(scm);
            JSONObject propObj = scmObj.getJSONObject("Properties");
            Log.i("LOGIN", YailGenerator.getComponentLines("Screen1", propObj, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (busy) {
                    return;
                }
                busy = true;

                textError.setVisibility(View.GONE);
                aiClient.login(edtEmail.getText().toString(),
                        edtPassword.getText().toString(),
                        new AppInvClient.LoginCallback() {
                            @Override
                            public void onSuccess(AppInvClient client) {
                                Intent intent = new Intent();
                                intent.putExtra("client", client);

                                intent.setClass(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void onFailure(String reason) {
                                textError.setVisibility(View.VISIBLE);
                                textError.setText(reason);
                                busy = false;
                            }
                        });
            }
        });
    }
}
