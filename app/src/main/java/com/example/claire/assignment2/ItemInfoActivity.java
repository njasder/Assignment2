package com.example.claire.assignment2;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This is the screen for entering
 * detailed information about a
 * to be added item.
 */

public class ItemInfoActivity extends AppCompatActivity {
    private SubmitTask submitTh;
    private PicTask picTh;

    public static final int GET_PIC_FROM_CAMERA = 0x123;
    public static final int GET_PIC_FROM_GALLERY = 0X124;
    public static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 0;
    public static final int DATE_DIALOG = 2;

    private EditText titleET;
    private EditText proDateET;
    private EditText expDateET;
    private EditText shelfLifeET;
    private ImageView itemPic;
    private Spinner typeSpinner;
    private Spinner subTypeSpinner;
    private Button subButton;
    private View progressView;

    private File mFile;
    private ArrayAdapter adapter;

    private ArrayAdapter subTypeAdapter;

    private  Bitmap bm;

    private String title = "";
    private String type = "";
    private String subType = "";
    private String proDate = "";
    private String expDate = "";
    private String bitmapString = "";
    private int shelfLife = 0;
    private boolean imageSet = false;
    private String userName;

    private int mYear;
    private int mMonth;
    private int mDay;

    private boolean proPressed = true;

    Handler itemInfoHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        titleET = (EditText) findViewById(R.id.itemTitleET);
        proDateET = (EditText) findViewById(R.id.productionDateET);
        expDateET = (EditText) findViewById(R.id.expiryDateET);
        shelfLifeET = (EditText) findViewById(R.id.shelfLifeET);
        itemPic = (ImageView) findViewById(R.id.setPhotoIV);
        typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        subTypeSpinner = (Spinner) findViewById(R.id.subTypeSpinner);
        subButton = (Button) findViewById(R.id.submitButton);
        progressView = (View) findViewById(R.id.itemInfo_progress);

        //String arrays for types and sub-types
        final String[] types = {"Cosmetic", "Commodity", "Food", "Drink", "Medical"};
        final String[] cosmetic = {"Lip", "Eye", "Foundation", "Skin Care", "Mask"};
        final String[] commodity ={"Shampoo", "Conditioner", "Body Wash", "Lotion"};
        final String[] food = {"Chilled Food", "Snack", "Dairy"};
        final String[] drink = {"Soft Drink", "Juice", "Alcohol"};
        final String[] medical = {"Pills", "Granules", "Oral Liquid", "Contact Lens"};

        //spinner adapter
        adapter = new ArrayAdapter<String>(ItemInfoActivity.this, R.layout.spinner_item, types);

        //get user id from the previous activity
        Intent emailIntent = getIntent();
        userName = emailIntent.getStringExtra("email");

        if(type.equals("")) {
            subTypeSpinner.setEnabled(false);
        }

        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                type = adapter.getItem(arg2).toString();
                subTypeSpinner.setEnabled(true);
                //set sub-types spinner when a type is chosen
                switch (type) {
                    case "Cosmetic":
                        subTypeAdapter = new ArrayAdapter<String>(ItemInfoActivity.this, R.layout.spinner_item, cosmetic);
                        subTypeSpinner.setAdapter(subTypeAdapter);
                        break;
                    case "Commodity":
                        subTypeAdapter = new ArrayAdapter<String>(ItemInfoActivity.this, R.layout.spinner_item, commodity);
                        subTypeSpinner.setAdapter(subTypeAdapter);
                        break;
                    case "Food":
                        subTypeAdapter = new ArrayAdapter<String>(ItemInfoActivity.this, R.layout.spinner_item, food);
                        subTypeSpinner.setAdapter(subTypeAdapter);
                        break;
                    case "Drink":
                        subTypeAdapter = new ArrayAdapter<String>(ItemInfoActivity.this, R.layout.spinner_item, drink);
                        subTypeSpinner.setAdapter(subTypeAdapter);
                        break;
                    case "Medical":
                        subTypeAdapter = new ArrayAdapter<String>(ItemInfoActivity.this, R.layout.spinner_item, medical);
                        subTypeSpinner.setAdapter(subTypeAdapter);
                        break;
                }
                arg0.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                type = "";
                arg0.setVisibility(View.VISIBLE);
            }
        });

        subTypeSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                subType = subTypeAdapter.getItem(arg2).toString();
                arg0.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                subType = "";
                arg0.setVisibility(View.VISIBLE);
            }
        });

        //set up date picker
        initCalendar();
        initViewAndEvents();

        itemPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ItemInfoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //ask permission for RITE_EXTERNAL_STORAGE
                    ActivityCompat.requestPermissions(ItemInfoActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                }
                else
                    showPopueWindow();
            }
        });

        //listener for submission
        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                titleET.setError(null);
                proDateET.setError(null);
                expDateET.setError(null);
                shelfLifeET.setError(null);

                boolean cancel = false;
                View focusView = null;
                Date pDate = null;
                Date eDate = null;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                title = titleET.getText().toString();
                proDate = proDateET.getText().toString();
                expDate = expDateET.getText().toString();
                String sf = shelfLifeET.getText().toString();

                //calculate date with shelf life
                if(!sf.equals("") && expDate.equals("") && !proDate.equals("")) {
                    Date prDate = null;
                    try {
                        System.out.println("try block");
                        prDate = formatter.parse(proDate);
                        Calendar calP = Calendar.getInstance();
                        calP.setTime(prDate);
                        calP.add(Calendar.DATE, Integer.parseInt(sf));
                        String exDate = "";
                        exDate = formatter.format(calP.getTime());
                        expDate = exDate;
                        expDateET.setText(exDate);
                    } catch (Exception e) {
                    }
                }

                if(!sf.equals("") && !expDate.equals("") && proDate.equals("")) {
                    Date exDate = null;
                    try {
                        System.out.println("try block");
                        exDate = formatter.parse(expDate);
                        Calendar calP = Calendar.getInstance();
                        calP.setTime(exDate);
                        calP.add(Calendar.DATE, -Integer.parseInt(sf));
                        String prDate = "";
                        prDate = formatter.format(calP.getTime());
                        proDate = prDate;
                        proDateET.setText(prDate);
                    } catch (Exception e) {
                    }
                }

                //check if all fields are filled with appropriate value
                if(!imageSet) {
                    Toast.makeText(ItemInfoActivity.this, "The image is not set!", Toast.LENGTH_LONG).show();
                }
                else if(title.equals("")) {
                    titleET.setError("Email cannot be empty!");
                    focusView = titleET;
                    cancel = true;
                }
                else if(proDate.equals("")) {
                    proDateET.setError("Production cannot be empty!");
                    focusView = proDateET;
                    cancel = true;
                }
                else if(expDate.equals("")) {
                    expDateET.setError("Expiry Date cannot be empty!");
                    focusView = expDateET;
                    cancel = true;
                }
                else if(sf.equals("")) {
                    shelfLifeET.setError("Shelf Life cannot be empty!");
                    focusView = shelfLifeET;
                    cancel = true;
                }
                else if(!sf.matches("[0-9]{1,}")) {
                    shelfLifeET.setError("Shelf Life must be number!");
                    focusView = shelfLifeET;
                    cancel = true;
                }
                else {
                    try {
                        pDate = formatter.parse(proDate);
                        eDate = formatter.parse(expDate);
                        if(dateDiff(pDate, eDate)>0) {
                            proDateET.setError("Production date must be later than expiry date!");
                            focusView = proDateET;
                            cancel = true;
                        }
                        else if(Math.abs(Integer.parseInt(sf) - dateDiff(eDate, pDate)) > 2) {
                            shelfLifeET.setError("Shelf Life does not match the period between production date and expiry date");
                            focusView = shelfLifeET;
                            cancel = true;
                        }
                        else if(dateDiff(new Date(), eDate) > 0) {
                            expDateET.setError("Expiry date should not be earlier than present date!");
                            focusView = expDateET;
                            cancel = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //if not filled with appropriate value, set error field.
                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
//            showProgress(true);
                    shelfLife = Integer.parseInt(sf);
                    //invoke the AsynckTask
                    submit();
                }
            }
        });

        itemInfoHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Intent intent = new Intent();
                        setResult(2, intent);//返回值调用函数，其中2为resultCode，返回值的标志
                        finish();
                        break;
                    case 2:
                        showProgress(false);
                        Toast.makeText(ItemInfoActivity.this, "Connection Timeout", Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        itemPic.setImageBitmap(bm);
                        break;
                    case 4:
                        shelfLifeET.setText(msg.getData().getString("sl"));
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode,grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                showPopueWindow();
            } else {
                // Permission Denied
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageSet = true;
        //handler images from different resources
        switch (requestCode) {
            case GET_PIC_FROM_CAMERA:
                if (resultCode == RESULT_OK) {
                    //compress the image
                    ImageZip.zipImage(mFile.getAbsolutePath());
                    try {
                        bitmapString = Bitmap2String(MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(mFile)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    itemPic.setImageURI(Uri.fromFile(mFile));
                }
                break;
            case GET_PIC_FROM_GALLERY:
                if(resultCode==RESULT_OK) {
                    getPic(data);
                }
                break;
        }
    }

    //popup window for setting images of item
    private void showPopueWindow(){
        View popView = View.inflate(this,R.layout.activity_popup,null);
        Button bt_album = (Button) popView.findViewById(R.id.btn_pick_photo);
        Button bt_camera = (Button) popView.findViewById(R.id.btn_take_photo);
        Button bt_cancle = (Button) popView.findViewById(R.id.btn_cancel);
        //get width and height of screen
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels*1/3;

        final PopupWindow popupWindow = new PopupWindow(popView,weight,height);
        popupWindow.setFocusable(true);
        //popup window disappears when outside is touched
        popupWindow.setOutsideTouchable(true);

        bt_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, GET_PIC_FROM_GALLERY);
                } catch (ActivityNotFoundException e) {

                }

                popupWindow.dismiss();

            }
        });
        bt_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPictureFromCamera();
                popupWindow.dismiss();

            }
        });
        bt_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM,0,50);

    }

    private void getPictureFromCamera() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        //confirm the directory of images taken by camera
        mFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        try {
            mFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //load uri-typed directory
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
        //send intent to onActivityResult，requestCode is GET_PIC_FROM_CAMERA
        startActivityForResult(intent, GET_PIC_FROM_CAMERA);
    }

    private void initViewAndEvents() {
        setDialogOnClickListener(R.id.productionDateET, DATE_DIALOG);
        setDialogOnClickListener(R.id.expiryDateET, DATE_DIALOG);
    }

    private void setDialogOnClickListener(final int editTextId, final int dialogId) {
        EditText b = (EditText) findViewById(editTextId);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(dialogId);
                if(editTextId == R.id.productionDateET)
                    proPressed = true;
                else
                    proPressed = false;
            }
        });
    }

    private void initCalendar() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG:
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
                        mDay);
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG:
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
                break;
        }
    }

    private void updateDisplay() {
        StringBuffer sb = new StringBuffer();
        sb.append(mYear).append("-");
        if(mMonth<9)
            sb.append("0").append(mMonth + 1).append("-");
        else
            sb.append(mMonth + 1).append("-");

        if(mDay<10)
            sb.append("0").append(mDay);
        else
            sb.append(mDay);

        //judge which date picker is clicked and do calculation
        if(proPressed) {
            proDateET.setText(sb.toString());
            String sl = shelfLifeET.getText().toString();
            if(!sl.equals("")) {
                System.out.println("if block");
                Date pDate = null;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    System.out.println("try block");
                    pDate = formatter.parse(sb.toString());
                    Calendar calP = Calendar.getInstance();
                    calP.setTime(pDate);
                    calP.add(Calendar.DATE, Integer.parseInt(sl));
                    String eDate = "";
                    eDate = formatter.format(calP.getTime());
                    expDateET.setText(eDate);
                } catch (Exception e) {
                }
            }
            else if(sl.equals("") && !expDateET.getText().toString().equals("")) {
                Date pDate = null;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    pDate = formatter.parse(sb.toString());
                    int slife = 0;
                    slife = dateDiff(formatter.parse(expDateET.getText().toString()), pDate);
                    if(slife>=0) {
                        Bundle b = new Bundle();
                        b.putString("sl", Integer.toString(slife));
                        Message msg = new Message();
                        msg.what = 4;
                        msg.setData(b);
                        itemInfoHandler.sendMessage(msg);
                    }
                } catch (Exception e) {

                }
            }
        }

        else {
            expDateET.setText(sb.toString());
            String sl = shelfLifeET.getText().toString();
            if(sl.equals("") && !proDateET.getText().toString().equals("")) {
                Date eDate = null;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    eDate = formatter.parse(sb.toString());
                    int slife = 0;
                    slife = dateDiff(eDate, formatter.parse(proDateET.getText().toString()));
                    System.out.println("shelf life is " + slife);
                    if(slife>=0) {
                        Bundle b = new Bundle();
                        b.putString("sl", Integer.toString(slife));
                        Message msg = new Message();
                        msg.what = 4;
                        msg.setData(b);
                        itemInfoHandler.sendMessage(msg);
                    }
                } catch (Exception e) {

                }
            }
            else if(!sl.equals("")) {
                Date eDate = null;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    System.out.println("try block");
                    eDate = formatter.parse(sb.toString());
                    Calendar calP = Calendar.getInstance();
                    calP.setTime(eDate);
                    calP.add(Calendar.DATE, -Integer.parseInt(sl));
                    String pDate = "";
                    pDate = formatter.format(calP.getTime());
                    proDateET.setText(pDate);
                } catch (Exception e) {

                }
            }
        }

    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay();
        }
    };

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    //transform bitmap to string
    public static String Bitmap2String(Bitmap bitmap)
    {
        return Base64.encodeToString(Bitmap2Bytes(bitmap), Base64.DEFAULT);
    }

    //transform string to bitmap
    public static Bitmap String2Bitmap(String st)
    {
        Bitmap bitmap = null;
        try
        {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            return bitmap;
        }
        catch (Exception e)
        {
            return null;
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

    //to invoke AsynkTask to send submit request to server and transmit all info
    public void submit() {
        if(submitTh!=null) {
            return;
        }

        showProgress(true);

        submitTh = new SubmitTask();
        submitTh.execute((Void) null);
    }

    public class SubmitTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost("http://moibie-assignment2.australiasoutheast.cloudapp.azure.com:8080/azureTest2/AddItem");

                JSONObject submitData = new JSONObject();
                submitData.put("title", title);
                submitData.put("proDate", proDate);
                submitData.put("expDate", expDate);
                submitData.put("shelfLife", shelfLife);
                submitData.put("type", type);
                submitData.put("subType", subType);
                submitData.put("image", bitmapString);
                submitData.put("userName", userName);

                StringEntity se = new StringEntity(submitData.toString());
                se.setContentType("text/json");
                post.setEntity(se);

                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
                HttpClient httpClient = new DefaultHttpClient(httpParameters);

                System.out.println("before submit execute");
                HttpResponse httpResponse = httpClient.execute(post);
                System.out.println("after submit execute");

                if(httpResponse.getStatusLine().getStatusCode()==200){
                    System.out.println("success");
                    String re = EntityUtils.toString(httpResponse.getEntity());
                    System.out.println(re);
                    JSONObject result = new JSONObject(re);
                    String flag = result.get("flag").toString();
                    System.out.println("before printing flag");
                    System.out.println("flag is " + "\"" + flag + "\"");

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
            submitTh.cancel(true);
            submitTh.onCancelled();
        }

        @Override
        protected void onCancelled() {
            submitTh = null;
        }
    }

    //to get images
    public void getPic(Intent data) {
        if(picTh!=null) {
            return;
        }

        showProgress(true);

        picTh = new PicTask(data);
        picTh.execute((Void) null);
    }

    public class PicTask extends AsyncTask<Void, Void, Boolean> {

        private Intent data;

        public PicTask(Intent data) {
            this.data = data;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Bitmap bitmap;
                if (data != null) {
                    if (data.getExtras() != null) {
                        bitmap = (Bitmap) data.getExtras().get("data");
                    }
                    if (data.getData() != null) {
                        data.getData();

                        Uri mImageCaptureUri = data.getData();
                        if (mImageCaptureUri != null) {
                            Bitmap image;
                            try {
                                image = MediaStore.Images.Media.getBitmap(ItemInfoActivity.this.getContentResolver(), mImageCaptureUri);
                                if (image != null) {
                                    Matrix matrix = new Matrix();
                                    matrix.setScale(0.5f, 0.5f);
                                    bm = Bitmap.createBitmap(image, 0, 0, image.getWidth(),
                                            image.getHeight(), matrix, true);
                                    bitmapString = Bitmap2String(image);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                return true;
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            Message message  = new Message();
            message.what = 3;
            itemInfoHandler.sendMessage(message);

            picTh.cancel(true);
            picTh.onCancelled();

        }

        @Override
        protected void onCancelled() {
            picTh = null;
            showProgress(false);
        }
    }

    //calculate date difference
    public static int dateDiff(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        long ldate1 = date1.getTime() + cal1.get(Calendar.ZONE_OFFSET) + cal1.get(Calendar.DST_OFFSET);
        long ldate2 = date2.getTime() + cal2.get(Calendar.ZONE_OFFSET) + cal2.get(Calendar.DST_OFFSET);
        // Use integer calculation, truncate the decimals
        int hr1 = (int) (ldate1 / 3600000); // 60*60*1000
        int hr2 = (int) (ldate2 / 3600000);

        int days1 = hr1 / 24;
        int days2 = hr2 / 24;

        int dateDiff = days1 - days2;
        return dateDiff;
    }
}
