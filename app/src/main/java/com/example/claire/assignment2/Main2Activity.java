package com.example.claire.assignment2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * This is then screen after login with
 * five main types and a navigation drawer
 */

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private AddItemTask addItemTh;

    private ListView listView;
    private TextView headerText;
    private View progressView;

    private List<MainItem> mainItems = new ArrayList<MainItem>();
    private MainItemAdapter adapter;
    private String email;

    private Handler mainItemHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        SysApplication.getInstance().addActivity(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.mainItemListView);
        headerText = (TextView) LayoutInflater.from(Main2Activity.this).inflate(R.layout.nav_header_main2, null).findViewById(R.id.textView);
        progressView = (View) findViewById(R.id.main2Progress);

        final AlertDialog.Builder getItemDialog = new AlertDialog.Builder(this);

        Intent it = getIntent();
        email = it.getStringExtra("email");
        System.out.println(email);
        System.out.println(headerText.getText().toString());

        //setup navigation drawer and set listener
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerText.setText(email);
        addItem();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showProgress(true);
                Intent intent = new Intent();
                intent.putExtra("email", email);
                intent.putExtra("type", mainItems.get(i).getType());
                intent.setClass(Main2Activity.this, SubTypeActivity.class);
                startActivity(intent);
                showProgress(false);
            }
        });

        mainItemHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        adapter = new MainItemAdapter(Main2Activity.this, R.layout.makeup_item_layout, mainItems);
                        showProgress(false);
                        listView.setAdapter(adapter);
                        break;
                    case 2:
                        setAlert(getItemDialog);
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //setup alert dialog
    public void setAlert(AlertDialog.Builder getItemDialog) {

        final EditText expiryDays = new EditText(this);
        getItemDialog.setTitle("Requesting items near expiry date!");
        getItemDialog.setMessage("Set remaining days for item requested:");
        getItemDialog.setView(expiryDays);
        getItemDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            String remDays = expiryDays.getText().toString();
            if(remDays.equals("")) {
                Toast.makeText(Main2Activity.this, "Remaining days must be set!", Toast.LENGTH_LONG).show();
            }
            else if(!remDays.matches("[0-9]{1,}")) {
                Toast.makeText(Main2Activity.this, "Remaining days must be an integer!", Toast.LENGTH_LONG).show();
            }
            else {
                Intent intent = new Intent();
                intent.putExtra("email", email);
                intent.putExtra("remDays", Integer.parseInt(remDays));
                intent.setClass(Main2Activity.this, RequestItemActivity.class);
                startActivity(intent);
            }
        }
    });
    getItemDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
    });
    getItemDialog.create().show();
}


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 2){
            Toast.makeText(Main2Activity.this, "Item Added Successfully", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_addItem) {
            Intent it = new Intent();
            it.putExtra("email", email);
            it.setClass(Main2Activity.this, ItemInfoActivity.class);
            startActivityForResult(it,1);
        } else if (id == R.id.nav_logout) {
            Intent it = new Intent();
            it.setClass(Main2Activity.this, LoginActivity.class);
            startActivity(it);
            finish();
        } else if(id == R.id.nav_search) {
            Message message = new Message();
            message.what = 2;
            mainItemHandler.sendMessage(message);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        }
    }

    //load five main types into listview with asynctask
    public void addItem() {
        if(addItemTh!=null) {
            return;
        }

        showProgress(true);

        addItemTh = new AddItemTask();
        addItemTh.execute((Void) null);
    }

    public class AddItemTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mainItems.add(new MainItem("Cosmetic", R.drawable.cosmetic));
                mainItems.add(new MainItem("Commodity", R.drawable.commodity));
                mainItems.add(new MainItem("Food", R.drawable.food));
                mainItems.add(new MainItem("Drink", R.drawable.drink));
                mainItems.add(new MainItem("Medical", R.drawable.medical));
                return true;
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            Message message  = new Message();
            message.what = 1;
            mainItemHandler.sendMessage(message);

            addItemTh.cancel(true);
            addItemTh.onCancelled();
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            addItemTh = null;
        }
    }
}
