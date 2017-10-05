package com.example.claire.assignment2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
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
 * This activity is　the screen
 * for get list of items under specific
 * type and display it.
 */

public class ItemActivity extends AppCompatActivity {

    private ItemListTask itemListTh;
    private DeleteItemTask delItemTh;

    private ListView itemList;
    private TextView textView;
    private View progressView;

    private Handler itemHandler;
    private ArrayList<Item> items = new ArrayList<Item>();
    private ItemAdapter adapter;
    private String email;
    private String type;
    private String subType;
    private int itemPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        itemList = (ListView) findViewById(R.id.itemListView);
        textView = (TextView) findViewById(R.id.noItemTV);
        progressView = (View) findViewById(R.id.itemProgress);

        //delete alert
        final AlertDialog.Builder deleteItemDialog = new AlertDialog.Builder(this);

        Intent it = getIntent();
        email = it.getStringExtra("email");
        type = it.getStringExtra("type");
        subType = it.getStringExtra("subType");

        //get items from server
        getItems();

        itemHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if(items.size() == 0)
                            textView.setText("There is no item in this type!");
                        else
                            textView.setText("");
                        showProgress(false);
                        adapter = new ItemAdapter(ItemActivity.this, R.layout.item_layout, items);
                        System.out.println("request first time");
                        itemList.setAdapter(adapter);
                        break;
                    case 2:
                        Toast.makeText(ItemActivity.this, "Connection Time out!", Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        items.remove(items.get(itemPos));
                        adapter.notifyDataSetChanged();
                        if(items.size() == 0)
                            textView.setText("There is no item in this type!");
                        Toast.makeText(ItemActivity.this, "Successfully Deleted!", Toast.LENGTH_LONG).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.putExtra("itemId", items.get(i).getItemId());
                intent.setClass(ItemActivity.this, ShowItemInfoActivity.class);
                startActivity(intent);

            }
        });

        //for deleting items
        itemList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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

    public void setAlert(AlertDialog.Builder deleteItemcDialog, final View view, final int position) {

        deleteItemcDialog.setTitle("Deletion Alert!");
        deleteItemcDialog.setMessage("Are you sure to delete this item?");
        deleteItemcDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem(items.get(position).getItemId());
            }
        });
        deleteItemcDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        deleteItemcDialog.create().show();
    }

    //transfer string to bitmap
    public static Bitmap String2Bitmap(String st) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }


    public void getItems() {
        if(itemListTh!=null) {
            return;
        }

        showProgress(true);

        itemListTh = new ItemListTask();
        itemListTh.execute((Void) null);
    }

    //AsyncTask to send request and get list of items from server
    public class ItemListTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost("http://moibie-assignment2.australiasoutheast.cloudapp.azure.com:8080/azureTest2/FindAddedItems");

                //data transmitted to server
                JSONObject listData = new JSONObject();
                listData.put("userName", email);
                listData.put("subType", subType);

                StringEntity se = new StringEntity(listData.toString());
                se.setContentType("text/json");
                post.setEntity(se);

                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
                HttpClient httpClient = new DefaultHttpClient(httpParameters);

                HttpResponse httpResponse = httpClient.execute(post);

                if(httpResponse.getStatusLine().getStatusCode()==200){
                    System.out.println("success");
                    String re = EntityUtils.toString(httpResponse.getEntity());
                    JSONObject result = new JSONObject(re);
                    System.out.println("there is data");
                    JSONArray res = result.getJSONArray("array");
                    int length = res.length();

                    //add create instance for all items int JSONArray and add into arraylist
                    for(int i=length-1; i>=0; i--) {
                        JSONObject itemInfo = res.getJSONObject(i);
                        int itemId = itemInfo.getInt("itemId");
                        String title = itemInfo.getString("title");
                        String proDate = itemInfo.getString("proDate");
                        int shelfLife = itemInfo.getInt("shelfLife");
                        String expDate = itemInfo.getString("expDate");
                        int remainingDays = itemInfo.getInt("remainingDays");
                        String image = itemInfo.getString("image");

                        System.out.println(title);
                        System.out.println(proDate);
                        System.out.println(shelfLife);
                        System.out.println(expDate);

                        String rDay;
                        if(remainingDays>=0)
                            rDay = "Expired!";
                        else
                            rDay = Integer.toString(-remainingDays) + " days left";

                        items.add(new Item(itemId, title, proDate, Integer.toString(shelfLife), expDate, rDay, type, subType, image));
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

            if (success) {
                Message message = new Message();
                message.what = 1;
                itemHandler.sendMessage(message);
            }
            else {
                Message message = new Message();
                message.what = 2;
                itemHandler.sendMessage(message);
            }
            itemListTh.cancel(true);
            itemListTh.onCancelled();
        }

        @Override
        protected void onCancelled() {
            itemListTh = null;
        }
    }

    //send delete request to server
    public void deleteItem(int itemId) {
        if(delItemTh!=null) {
            return;
        }

        showProgress(true);

        delItemTh = new DeleteItemTask(itemId);
        delItemTh.execute((Void) null);
    }

    public class DeleteItemTask extends AsyncTask<Void, Void, Boolean> {

        private int itemId;

        public DeleteItemTask(int itemId) {
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
                    if(flag.equals("true"))
                        return true;
                    else
                        return false;
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
                //finish();
                Message message = new Message();
                message.what = 3;
                itemHandler.sendMessage(message);

            }
            else {
                Message message = new Message();
                message.what = 2;
                itemHandler.sendMessage(message);
            }
            delItemTh.cancel(true);
            delItemTh.onCancelled();
        }

        @Override
        protected void onCancelled() {
            delItemTh = null;
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
