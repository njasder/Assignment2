package com.example.claire.assignment2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * This is the screen for entering personal information
 * and sign up in system.
 */

public class RegisterActivity extends AppCompatActivity {

    private RegisterTask registerTh;

    private EditText emailET;
    private EditText passwordET;
    private EditText conPasswordET;
    private Button confirm;
    private Spinner genderSpinner;
    private ArrayAdapter adapter;
    private Handler registerHandler;

    private String gender;
    private String flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        emailET = (EditText) findViewById(R.id.fillEmailET);
        passwordET = (EditText) findViewById(R.id.fillPasswordET);
        conPasswordET = (EditText) findViewById(R.id.confirmPasswordET);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        confirm = (Button) findViewById(R.id.confirmRegister);

        //set
        final String[] genderOptions = {"Secrete", "Female", "Male"};
        adapter = new ArrayAdapter<String>(RegisterActivity.this, R.layout.spinner_item, genderOptions);
        adapter.setDropDownViewResource(R.layout.dropdown_style);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                gender = adapter.getItem(arg2).toString();
                arg0.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                gender = "";
                arg0.setVisibility(View.VISIBLE);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        registerHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Intent intent = new Intent();
                        intent.putExtra("email", emailET.getText().toString());
                        setResult(2, intent);
                        finish();
                        break;
                    case 2:
                        Toast.makeText(RegisterActivity.this, "Email already exists!", Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        Toast.makeText(RegisterActivity.this, "Connection Timeout!", Toast.LENGTH_LONG).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
// TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation== Configuration.ORIENTATION_LANDSCAPE) {
        }
        else{
        }
    }

    public static boolean validate(String email){

        boolean flag=true;

        if(!email.contains("@")||!email.contains(".")){

            flag=false;
        }
        if(flag){

            if(email.indexOf("@")>email.indexOf("."))
                flag=false;
        }
        return flag;
    }

    public void attemptRegister() {
        emailET.setError(null);
        passwordET.setError(null);
        conPasswordET.setError(null);

        boolean cancel = false;
        View focusView = null;

        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String secondPassword = conPasswordET.getText().toString();
        if(email.equals("")) {
            emailET.setError("Email cannot be empty!");
            focusView = emailET;
            cancel = true;
        }
        else if(password.equals("")) {
            passwordET.setError("Password cannot be empty!");
            focusView = passwordET;
            cancel = true;
        }
        else if(secondPassword.equals("")) {
            conPasswordET.setError("Password must be confirmed!");
            focusView = conPasswordET;
            cancel = true;
        }
        else if(gender.equals("")) {
            Message message = new Message();
            message.what = 4;
            registerHandler.sendMessage(message);
        }
        else if(!validate(email)) {
            emailET.setError("This is not a valid email!");
            focusView = emailET;
            cancel = true;
        }
        else if(!password.equals(secondPassword)) {
            conPasswordET.setError("Different from the first typing");
            focusView = conPasswordET;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true);
            register(email, password);
        }
    }

    public void register(String userName, String password) {
        if(registerTh!=null) {
            return;
        }

//        showProgress(true);

        registerTh = new RegisterTask(userName, password);
        registerTh.execute((Void) null);
    }

    public class RegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String userName;
        private final String password;

        RegisterTask(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost("http://moibie-assignment2.australiasoutheast.cloudapp.azure.com:8080/azureTest2/Registration");

                JSONObject registerData = new JSONObject();
                registerData.put("username", userName);
                registerData.put("password", password);
                registerData.put("gender", gender);

                StringEntity se = new StringEntity(registerData.toString());
                se.setContentType("text/json");
                post.setEntity(se);

                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
                HttpClient httpClient = new DefaultHttpClient(httpParameters);

                System.out.println("before execute");
                HttpResponse httpResponse = httpClient.execute(post);
                System.out.println("after execute");

                if(httpResponse.getStatusLine().getStatusCode()==200){
                    System.out.println("success");
                    String re = EntityUtils.toString(httpResponse.getEntity());
                    JSONObject result = new JSONObject(re);
                    flag = result.get("flag").toString();

                    System.out.println("flag is " + "\"" + flag + "\"");

                    if(flag.equals("success"))
                        return true;
                    else
                        return false;
                }
                else
                    return false;
            } catch(Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
//            newTopic = null;
//            showProgress(false);

            if (success) {
                //finish();
                Message message = new Message();
                message.what = 1;
                registerHandler.sendMessage(message);
                registerTh.cancel(true);
                registerTh.onCancelled();
            }
            else {
                if(flag.equals("duplicated")){
                    Message message = new Message();
                    message.what = 2;
                    registerHandler.sendMessage(message);
                    registerTh.cancel(true);
                    registerTh.onCancelled();
                }
                else {
                    Message message = new Message();
                    message.what = 3;
                    registerHandler.sendMessage(message);
                    registerTh.cancel(true);
                    registerTh.onCancelled();
                }
            }
        }

        @Override
        protected void onCancelled() {
            registerTh = null;
        }
    }
}
