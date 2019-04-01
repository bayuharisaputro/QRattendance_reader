package com.example.bayuharisaputro.qrattendance_reader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.bayuharisaputro.qrattendance_reader.service.AppController;
import com.example.bayuharisaputro.qrattendance_reader.service.Server;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Map;
public class LoginActivity extends AppCompatActivity{
    public static final String TAG = AppController.class.getSimpleName();
    private AutoCompleteTextView mNim;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    ScrollView login_form;
    private static final String TAG_SUCCESS = "success";
    int success=1;
    Intent mServiceIntent;
    Context ctx;
    private String nim, password,kelas;

    public Context getCtx() {
        return ctx;
    }
    String tag_json_obj = "json_obj_req";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mNim = (AutoCompleteTextView) findViewById(R.id.nim);
        mPasswordView = (EditText) findViewById(R.id.password);
        Button login = (Button) findViewById(R.id.login);
        login_form = findViewById(R.id.login_form);
        login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                check();


            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void check() {
        StringRequest strReq = new StringRequest(Request.Method.POST, Server.URL +"checkUser.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        Log.d("get edit data", jObj.toString());
                        nim=(jObj.getString("nim"));
                        password=(jObj.getString("password"));
                        kelas=(jObj.getString("kelas"));
                        SharedPreferences preferences = getSharedPreferences("user",MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("nim", mNim.getText().toString());
                        editor.putString("nama", jObj.getString("nama_mahasiswa"));
                        editor.putString("password", mPasswordView.getText().toString());
                        editor.putString("kelas",kelas);
                        editor.commit();
//                        Snackbar.make(login_form, jObj.getString("message"),Snackbar.LENGTH_LONG).show();
                        Snackbar.make(login_form,jObj.getString("nama_mahasiswa") ,Snackbar.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        nim="";
                        password="";
                        Snackbar.make(login_form, jObj.getString("message"),Snackbar.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(login_form,"ada kesalahan",Snackbar.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("nim", mNim.getText().toString() );
                params.put("pass",mPasswordView.getText().toString());
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }


}

