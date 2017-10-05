package com.example.claire.assignment2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This is the screen for requesting
 * items that are expiring in set-up
 * remaining days.
 */

public class RequestItemActivity extends AppCompatActivity {

    private RequestTask requestTh;
    private DeleteTask delTh;

    private ListView listView;
    private TextView textView;
    private View progressView;

    private String email;
    private int remDays;
    private int itemPos = 0;
    private ArrayList<Item> items = new ArrayList<>();
    private ItemAdapter adapter;

    private Handler requestHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_item);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        listView = (ListView) findViewById(R.id.requestItemLV);
        textView = (TextView) findViewById(R.id.noRequestItemTV);
        progressView = (View) findViewById(R.id.requestProgress);

        final AlertDialog.Builder deleteItemDialog = new AlertDialog.Builder(this);

        Intent it = getIntent();
        email = it.getStringExtra("email");
        remDays = it.getIntExtra("remDays", 3);

        getRequestItem();

        requestHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if(items.size() == 0)
                            textView.setText("There is no item expiring in " + remDays + "!");
                        else
                            textView.setText("");
                        adapter = new ItemAdapter(RequestItemActivity.this, R.layout.item_layout, items);
                        listView.setAdapter(adapter);
                        break;
                    case 2:
                        Toast.makeText(RequestItemActivity.this, "Connection Time out!", Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        items.remove(items.get(itemPos));
                        adapter.notifyDataSetChanged();
                        if(items.size() == 0)
                            textView.setText("There is no item expiring in " + remDays + "!");
                        Toast.makeText(RequestItemActivity.this, "Successfully Deleted!", Toast.LENGTH_LONG).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.putExtra("itemId", items.get(i).getItemId());
                intent.setClass(RequestItemActivity.this, ShowItemInfoActivity.class);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                itemPos = i;
                setAlert(deleteItemDialog, view, i);
                System.out.println("position is " + i);
                return true;
            }
        });
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

    //delete alert
    public void setAlert(AlertDialog.Builder deleteItemcDialog, final View view, final int position) {

        deleteItemcDialog.setTitle("Deletion Alert!");
        deleteItemcDialog.setMessage("Are you sure to delete this item?");
        deleteItemcDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete(items.get(position).getItemId());
            }
        });
        deleteItemcDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        deleteItemcDialog.create().show();
    }

    //get list of requested items from server
    public void getRequestItem() {
        if(requestTh!=null) {
            return;
        }

        showProgress(true);

        requestTh = new RequestTask();
        requestTh.execute((Void) null);
    }

    public class RequestTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost("http://moibie-assignment2.australiasoutheast.cloudapp.azure.com:8080/azureTest2/FindAsRemainingDays");

                JSONObject listData = new JSONObject();
                listData.put("userName", email);
                listData.put("remainingDays", remDays);

                StringEntity se = new StringEntity(listData.toString());
                se.setContentType("text/json");
                post.setEntity(se);

                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
                HttpClient httpClient = new DefaultHttpClient(httpParameters);

                HttpResponse httpResponse = httpClient.execute(post);

                if(httpResponse.getStatusLine().getStatusCode()==200){
                    String re = EntityUtils.toString(httpResponse.getEntity());
                    JSONObject result = new JSONObject(re);
                    JSONArray res = result.getJSONArray("array");
                    int length = res.length();

                    for(int i=length-1; i>=0; i--) {
                        JSONObject itemInfo = res.getJSONObject(i);
                        int itemId = itemInfo.getInt("itemId");
                        String title = itemInfo.getString("title");
                        String proDate = itemInfo.getString("proDate");
                        int shelfLife = itemInfo.getInt("shelfLife");
                        String expDate = itemInfo.getString("expDate");
                        int remainingDays = itemInfo.getInt("remainingDays");
                        String image = itemInfo.getString("image");
                        String type = itemInfo.getString("type");
                        String subType = itemInfo.getString("subType");

                        String rDays;

                        if(remainingDays<=0)
                            rDays = "Expired!";
                        else
                            rDays = Integer.toString(remainingDays) + " days left";

                        items.add(new Item(itemId, title, proDate, Integer.toString(shelfLife), expDate, rDays, type, subType, image));
                    }
                    return true;
                }
                else
                    return false;
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            showProgress(false);

            if (success) {
                Message message = new Message();
                message.what = 1;
                requestHandler.sendMessage(message);
            }
            else {
                Message message = new Message();
                message.what = 2;
                requestHandler.sendMessage(message);
            }
            requestTh.cancel(true);
            requestTh.onCancelled();
        }

        @Override
        protected void onCancelled() {
            requestTh = null;
        }
    }

    //send delete request to server
    public void delete(int itemId) {
        if(delTh!=null) {
            return;
        }

        showProgress(true);

        delTh = new DeleteTask(itemId);
        delTh.execute((Void) null);
    }

    public class DeleteTask extends AsyncTask<Void, Void, Boolean> {

        private int itemId;

        public DeleteTask(int itemId) {
            this.itemId = itemId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost("http://moibie-assignment2.australiasoutheast.cloudapp.azure.com:8080/azureTest2/deleteItem");

                JSONObject topicData = new JSONObject();
                topicData.put("itemId", itemId);

                StringEntity se = new StringEntity(topicData.toString());
                se.setContentType("text/json");
                post.setEntity(se);

                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
                HttpClient httpClient = new DefaultHttpClient(httpParameters);

                System.out.println("before lecture delete execute");
                HttpResponse httpResponse = httpClient.execute(post);
                System.out.println("after lecture delete execute");

                if(httpResponse.getStatusLine().getStatusCode()==200){
                    System.out.println("success");
                    String re = EntityUtils.toString(httpResponse.getEntity());
                    JSONObject result = new JSONObject(re);

                    String flag = result.get("flag").toString();
                    return (flag.equals("true"));
                }
                else
                    return false;
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            showProgress(false);

            if (success) {
                Message message = new Message();
                message.what = 3;
                requestHandler.sendMessage(message);

            }
            else {
                Message message = new Message();
                message.what = 2;
                requestHandler.sendMessage(message);
            }
            delTh.cancel(true);
            delTh.onCancelled();
        }

        @Override
        protected void onCancelled() {
            delTh = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mPinSetFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
