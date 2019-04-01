package com.example.bayuharisaputro.qrattendance_reader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.bayuharisaputro.qrattendance_reader.service.AppController;
import com.example.bayuharisaputro.qrattendance_reader.service.Server;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = AppController.class.getSimpleName();
    String tag_json_obj = "json_obj_req";
    private AutoCompleteTextView mNim;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String d_key, n_key;
    private static final String TAG_SUCCESS = "success";
    int success=1;
    private Button buttonScan;
    private TextView textStatus;
    private String challenge,hash,sess, nama,kelas ;
    String plain;
    private String checkSess;
    private IntentIntegrator intentIntegrator;
    RelativeLayout Rlayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonScan = findViewById(R.id.buttonScan);
        textStatus = findViewById(R.id.textViewStatus);
        Rlayout = findViewById(R.id.activity_main);
        buttonScan.setOnClickListener(this);
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        nama = prefs.getString("nama", null);
        kelas = prefs.getString("kelas", null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            if (result.getContents() == null){
                Toast.makeText(this, "Hasil tidak ditemukan", Toast.LENGTH_SHORT).show();
            }else{
                try{
                    JSONObject object = new JSONObject(result.getContents());
                    this.challenge = object.getString("0");
                    this.hash = object.getString("1");
                    decrypt(challenge);

                    if(hash.equals(MD5(plain))) {

                        textStatus.setText("HALO " + nama + " :)");

                    }
                    else {
                        textStatus.setText("KAMU BUKAN KELAS INI ATAU SESSION KAMU HABIS :(( ");
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onClick(View v) {
         getKey(kelas);
        intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setPrompt("Scan QR untuk melakukan absensi");
        intentIntegrator.setCaptureActivity(PortraitActivity.class);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.initiateScan();
    }

    private void decrypt(String cipher) {

        String[] cipherToChar = cipher.split(" ");
        BigInteger dKey, nKey;
        SharedPreferences prefs = getSharedPreferences("user_key", MODE_PRIVATE);
        nKey = new BigInteger(prefs.getString("n_key", null));
        dKey = new BigInteger(prefs.getString("d_key", null));
        BigInteger[] cipherS = new BigInteger[cipherToChar.length];
        BigInteger[] plainS = new BigInteger[cipherToChar.length];
        for (int counter = 0; counter<cipherToChar.length; counter++) {
                cipherS[counter] = new BigInteger(String.valueOf(cipherToChar[counter]));
                plainS[counter] = cipherS[counter].modPow(dKey,nKey);

        }
        plain = TextUtils.join(" ", plainS);

    }
    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {

        }
        catch(UnsupportedEncodingException ex){

        }
        return null;
    }

    private void getKey(final String kelasParam) {
        StringRequest strReq = new StringRequest(Request.Method.POST, Server.URL +"getKey.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        Log.d("get edit data", jObj.toString());
                        n_key=(jObj.getString("n_key"));
                        d_key=(jObj.getString("d_key"));
                        SharedPreferences preferences = getSharedPreferences("user_key",MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("n_key", n_key);
                        editor.putString("d_key", d_key);
                        editor.commit();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(Rlayout,"ada kesalahan",Snackbar.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("kelas", kelasParam);
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }
}
