package com.example.androideatitv2_serverapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androideatitv2_serverapp.Model.EventBus.ChangeMenuClick;
import com.example.androideatitv2_serverapp.Model.EventBus.ToastEvent;
import com.example.androideatitv2_serverapp.Model.EventBus.categoryClick;
import com.example.androideatitv2_serverapp.Model.MenuItemBack;
import com.example.androideatitv2_serverapp.common.common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private   DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private int menuClick = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        subscribeToTopic(common.CreateTopicOrder());

        drawer = findViewById(R.id.drawer_layout);
        navigationView= findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_order)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                //it's possible to do more actions on several items, if there is a large amount of items I prefer switch(){case} instead of if()

              /* if (id==R.id.nav_sign_out){
                  signOut();
               }*/
                switch (menuItem.getItemId())
                {
                    case R.id.nav_category:
                        if(menuItem.getItemId() != menuClick)
                        {
                           // navController.popBackStack(); // Remove all back stack
                            navController.navigate(R.id.nav_category);
                        }

                        break;

                    case R.id.nav_order:
                       if(menuItem.getItemId() != menuClick)
                       {
                          // navController.popBackStack();
                           navController.navigate(R.id.nav_order);
                       }
                        break;
                    case R.id.nav_sign_out:
                            signOut();
                        break;
                    default:
                            menuClick = -1;
                            break;
                }

                NavigationUI.onNavDestinationSelected(menuItem,navController);
                //This is for closing the drawer after acting on it
                drawer.closeDrawer(GravityCompat.START);

                menuClick = menuItem.getItemId();

                return true;
            }
        });
        navigationView.bringToFront();
            // heta dy bta3t al name al byzaher fe  navigationbar
        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView)headerView.findViewById(R.id.txt_user);
        common.setSpanString("Hey " , common.currentServerUser.getName(),txt_user);


        menuClick = R.id.nav_category; // Default

    }

    private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful())
                            Toast.makeText(HomeActivity.this, "Failed: "+task.isSuccessful(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign out")
                .setMessage("Do you really want sign out ?")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();

                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                common.selectedFood = null;
                common.categorySelected = null;
                common.currentServerUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActivity.this , MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCategoryClick(categoryClick event)
    {
        if(event.isSuccess())
        {
            if(menuClick != R.id.nav_food_list)
            {
                navController.navigate(R.id.nav_food_list);
                menuClick = R.id.nav_food_list;
            }
        }
    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onToastEvent (ToastEvent event)
    {
        if(event.isUpdate())
        {
            Toast.makeText(this, " Update Success! ", Toast.LENGTH_SHORT).show();


        }
        else
        {
            Toast.makeText(this, " Delete Success! ", Toast.LENGTH_SHORT).show();

        }

        //Refresh
            EventBus.getDefault().postSticky(new ChangeMenuClick( event.isFromFoodList()));
    }


    /*@Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event)
    {
       if(event.isFromFoodList())
       {
           //Clear
           navController.popBackStack(R.id.nav_category , true);
           navController.navigate(R.id.nav_category);
       }
       else
       {
           //Clear
           navController.popBackStack(R.id.nav_food_list , true);
           navController.navigate(R.id.nav_food_list);
       }
       menuClick= -1;
    }*/

    @Subscribe (sticky = true , threadMode = ThreadMode.MAIN )
    public void onMenuItemCallBack(MenuItemBack event )
    {
        menuClick = -1;
        if(getSupportFragmentManager().getBackStackEntryCount() > 0 )
            getSupportFragmentManager().popBackStack();

    }
}
