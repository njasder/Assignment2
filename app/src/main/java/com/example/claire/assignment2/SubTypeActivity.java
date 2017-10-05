package com.example.claire.assignment2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the screen for load sub-types
 * under specific main type.
 */

public class SubTypeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SetPicTask setTh;

    private ListView listView;
    private View progressView;

    private List<SubItem> subItems = new ArrayList<SubItem>();
    private SubItemAdapter adapter;
    private String email;
    private String type = "";
    private Handler subHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtype);

        SysApplication.getInstance().addActivity(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.subTypeListView);
        progressView = (View) findViewById(R.id.subType_progress);

        final AlertDialog.Builder getItemDialog = new AlertDialog.Builder(this);

        Intent it = getIntent();
        email = it.getStringExtra("email");
        type = it.getStringExtra("type");
        System.out.println("type is " + type);

        setPic();

        //setup navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.sub_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent it = new Intent();
                it.putExtra("email", email);
                it.putExtra("type", type);
                it.putExtra("subType", subItems.get(i).getSubType());
                it.setClass(SubTypeActivity.this, ItemActivity.class);
                startActivity(it);
            }
        });

        subHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        listView.setAdapter(adapter);
                        showProgress(false);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.sub_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    //alert dialog for entering remaining days
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
                    Toast.makeText(SubTypeActivity.this, "Remaining days must be set!", Toast.LENGTH_LONG).show();
                }
                else if(!remDays.matches("[0-9]{1,}")) {
                    Toast.makeText(SubTypeActivity.this, "Remaining days must be an integer!", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent intent = new Intent();
                    intent.putExtra("email", email);
                    intent.putExtra("remDays", Integer.parseInt(remDays));
                    intent.setClass(SubTypeActivity.this, RequestItemActivity.class);
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
            Toast.makeText(SubTypeActivity.this, "Item Added Successfully", Toast.LENGTH_LONG).show();
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
            Intent it =new Intent();
            it.putExtra("email", email);
            it.setClass(SubTypeActivity.this, ItemInfoActivity.class);
            startActivityForResult(it,1);
        } else if (id == R.id.nav_logout) {
            Intent it =new Intent();
            it.setClass(SubTypeActivity.this, LoginActivity.class);
            startActivity(it);
            finish();
        } else if(id == R.id.nav_search) {
            Message message = new Message();
            message.what = 2;
            subHandler.sendMessage(message);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer!=null)
            drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //update sub type list in case of main type
    public void updateList() {
        switch (type) {
            case "Cosmetic":
                subItems.add(new SubItem(type, "Lip", R.mipmap.lip));
                subItems.add(new SubItem(type, "Eye", R.mipmap.eye));
                subItems.add(new SubItem(type, "Foundation", R.mipmap.foundation));
                subItems.add(new SubItem(type, "Skin Care", R.mipmap.skin_care));
                subItems.add(new SubItem(type, "Mask", R.mipmap.mask));
                break;
            case "Commodity":
                subItems.add(new SubItem(type, "Shampoo", R.mipmap.shampoo));
                subItems.add(new SubItem(type, "Conditioner", R.mipmap.conditioner));
                subItems.add(new SubItem(type, "Body Wash", R.mipmap.body_wash));
                subItems.add(new SubItem(type, "Lotion", R.mipmap.lotion));
                break;
            case "Food":
                subItems.add(new SubItem(type, "Chilled Food", R.mipmap.chilled_food));
                subItems.add(new SubItem(type, "Snack", R.mipmap.snack));
                subItems.add(new SubItem(type, "Dairy", R.mipmap.dairy));
                break;
            case "Drink":
                subItems.add(new SubItem(type, "Soft Drink", R.mipmap.soft_drink));
                subItems.add(new SubItem(type, "Juice", R.mipmap.juice));
                subItems.add(new SubItem(type, "Alcohol", R.mipmap.alcohol));
                break;
            case "Medical":
                subItems.add(new SubItem(type, "Pills", R.mipmap.pills));
                subItems.add(new SubItem(type, "Granules", R.mipmap.granules));
                subItems.add(new SubItem(type, "Oral Liquid", R.mipmap.oral_liquid));
                subItems.add(new SubItem(type, "Contact Lens", R.mipmap.contact_lens));
                break;
            default:
                break;
        }
    }

    //asyncTask for loading all sub-type instances
    public void setPic() {
        if(setTh!=null) {
            return;
        }

        showProgress(true);

        setTh = new SetPicTask();
        setTh.execute((Void) null);
    }

    public class SetPicTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                updateList();
                adapter = new SubItemAdapter(SubTypeActivity.this, R.layout.makeup_item_layout, subItems, SubTypeActivity.this.getResources());
                return true;
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            showProgress(false);

            Message message  = new Message();
            message.what = 1;
            subHandler.sendMessage(message);

            setTh.cancel(true);
            setTh.onCancelled();
        }

        @Override
        protected void onCancelled() {
            setTh = null;
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
