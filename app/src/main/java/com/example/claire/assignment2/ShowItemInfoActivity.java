package com.example.claire.assignment2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
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
 *
 */

public class ShowItemInfoActivity extends AppCompatActivity {

    private GetItemTask getItemTh;

    private ImageView photo;
    private TextView title;
    private TextView proDate;
    private TextView shelfLife;
    private TextView expDate;
    private TextView remainingDays;
    private TextView type;
    private TextView subType;
    private View progressView;

    private String sPhoto;
    private String sTitle;
    private String sProDate;
    private String sShelfLife;
    private String sExpDate;
    private String sRemainingDays;
    private String sType;
    private String sSubType;

    private Handler itemInfoHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_item_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        photo = (ImageView) findViewById(R.id.infoPhotoIV);
        title = (TextView) findViewById(R.id.infoTitleTV);
        proDate = (TextView) findViewById(R.id.infoProDateTV);
        shelfLife = (TextView) findViewById(R.id.infoShelfLifeTV);
        expDate = (TextView) findViewById(R.id.infoExpDateTV);
        remainingDays = (TextView) findViewById(R.id.infoRemainingDaysTV);
        type = (TextView) findViewById(R.id.infoTypeTV);
        subType = (TextView) findViewById(R.id.infoSubtypeTV);
        progressView = (View) findViewById(R.id.showItemInfoProgress);

        Intent it = getIntent();
        getItem(it.getIntExtra("itemId", 1));

        itemInfoHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        String iTitle = "Item Title: " + sTitle;
                        String iProDate = "Production Date: " + sProDate;
                        String iShelfLife = "ShelfLife: " + sShelfLife;
                        String iExpDate = "Expiry Date: " + sExpDate;
                        String iRemainingDays = "Remaining Days: " + sRemainingDays;
                        String iType = "Type: " + sType;
                        String iSubType = sSubType;

                        photo.setImageBitmap(String2Bitmap(sPhoto));
                        title.setText(iTitle);
                        proDate.setText(iProDate);
                        shelfLife.setText(iShelfLife);
                        expDate.setText(iExpDate);
                        remainingDays.setText(iRemainingDays);
                        type.setText(iType);
                        subType.setText(iSubType);
                        break;
                    case 2:
                        Toast.makeText(ShowItemInfoActivity.this, "Connection Time out!", Toast.LENGTH_LONG).show();
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

    public void getItem(int itemId) {
        if(getItemTh!=null) {
            return;
        }

        showProgress(true);

        getItemTh = new GetItemTask(itemId);
        getItemTh.execute((Void) null);
    }

    public class GetItemTask extends AsyncTask<Void, Void, Boolean> {

        private int itemId;

        public GetItemTask(int itemId) {
            this.itemId = itemId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost("http://moibie-assignment2.australiasoutheast.cloudapp.azure.com:8080/azureTest2/FindItem");

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
                    String re = EntityUtils.toString(httpResponse.getEntity());
                    JSONObject result = new JSONObject(re);

                    sTitle = result.getString("title");
                    sProDate = result.getString("proDate");
                    sShelfLife = Integer.toString(result.getInt("shelfLife"));
                    sExpDate = result.getString("expDate");
                    int rd = result.getInt("remainingDays");
                    sPhoto = result.getString("image");
                    sType = result.getString("type");
                    sSubType = result.getString("subType");

                    String rDay;
                    if(rd>=0)
                        sRemainingDays = "Expired!";
                    else
                        sRemainingDays = Integer.toString(-rd);
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
                itemInfoHandler.sendMessage(message);

            }
            else {
                Message message = new Message();
                message.what = 2;
                itemInfoHandler.sendMessage(message);
            }
            getItemTh.cancel(true);
            getItemTh.onCancelled();
        }

        @Override
        protected void onCancelled() {
            getItemTh = null;
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
