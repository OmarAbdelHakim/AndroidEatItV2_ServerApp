package com.example.androideatitv2_serverapp.ui.order;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.TokenWatcher;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.operators.observable.ObservableBuffer;
import io.reactivex.schedulers.Schedulers;

import com.example.androideatitv2_serverapp.Model.EventBus.AddonSizeEditEvent;
import com.example.androideatitv2_serverapp.Model.EventBus.ChangeMenuClick;
import com.example.androideatitv2_serverapp.Model.EventBus.LoadOrderEvent;
import com.example.androideatitv2_serverapp.Model.EventBus.ToastEvent;
import com.example.androideatitv2_serverapp.Model.FCMSendData;
import com.example.androideatitv2_serverapp.Model.FCMresponse;
import com.example.androideatitv2_serverapp.Model.FoodModel;
import com.example.androideatitv2_serverapp.Model.OrderModel;
import com.example.androideatitv2_serverapp.Model.TokenModel;
import com.example.androideatitv2_serverapp.R;
import com.example.androideatitv2_serverapp.SizeAddonEditActivity;
import com.example.androideatitv2_serverapp.adapter.MyOrderAdapter;
import com.example.androideatitv2_serverapp.common.BottomSheetOrderFragment;
import com.example.androideatitv2_serverapp.common.MySwipeHelper;
import com.example.androideatitv2_serverapp.common.common;
import com.example.androideatitv2_serverapp.remote.IFCMService;
import com.example.androideatitv2_serverapp.remote.RetrofitFCMClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderFragment extends Fragment {

    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;

    @BindView(R.id.txt_order_filter)
    TextView txt_order_filter;


    Unbinder unbinder;

    LayoutAnimationController layoutAnimationController;
    MyOrderAdapter adapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;


    private OrderViewModel orderViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        orderViewModel =
                ViewModelProviders.of(this).get(OrderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_order, container, false);

        unbinder = ButterKnife.bind(this , root);
        initView();
        orderViewModel.getMessageError().observe(getViewLifecycleOwner(), s ->
                Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show());

        orderViewModel.getOrderModelMutableLiveData().observe(getViewLifecycleOwner(), new Observer<List<OrderModel>>() {
            @Override
            public void onChanged(List<OrderModel> orderModels) {
                if(orderModels != null)
                {
                    adapter = new MyOrderAdapter(getContext() , orderModels);
                    recycler_order.setAdapter(adapter);
                    recycler_order.setLayoutAnimation(layoutAnimationController);

                    updateTextCounter();

                }
            }
        });
        return root;
    }

    private void initView() {

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);


        setHasOptionsMenu(true);


        recycler_order.setHasFixedSize(true);
        recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));


        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext() , R.anim.layout_items_from_left);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;


        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext() , recycler_order,width/6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

                buf.add(new MyButton(getContext(),"Directions",30 , 0, Color.parseColor("#9b0000"),
                        pos -> {



                        }));
                buf.add(new MyButton(getContext(),"Call",30 , 0, Color.parseColor("#560027"),
                        pos -> {

                            Dexter.withActivity(getActivity())
                                    .withPermission(Manifest.permission.CALL_PHONE)
                                    .withListener(new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted(PermissionGrantedResponse response) {

                                            OrderModel orderModel = adapter.getItemAtPosition(pos);
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_DIAL);
                                            intent.setData(Uri.parse(new StringBuilder("tel: ")
                                            .append(orderModel.getUserPhone()).toString()));
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onPermissionDenied(PermissionDeniedResponse response) {
                                            Toast.makeText(getContext(), "You must accept "+response.getPermissionName(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                                        }
                                    }).check();



                        }));

                buf.add(new MyButton(getContext(),"Remove",30 , 0, Color.parseColor("#12005e"),
                        pos -> {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                            .setTitle("Delete")
                            .setMessage("Do you really want to delete this order?")
                            .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss()).
                                    setPositiveButton("DELETE", (dialogInterface, i) -> {

                                        OrderModel orderModel = adapter.getItemAtPosition(pos);
                                        FirebaseDatabase.getInstance()
                                                .getReference(common.ORDER_REF)
                                                .child(orderModel.getKey())
                                                .removeValue()
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                adapter.removeItem(pos);
                                                adapter.notifyItemRemoved(pos);
                                                updateTextCounter();
                                                dialogInterface.dismiss();
                                                Toast.makeText(getContext(), "Order has been delete!", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    });

                    //Create Dialog
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                            negativeButton.setTextColor(Color.GRAY);

                            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            positiveButton.setTextColor(Color.RED);


                        }));

                buf.add(new MyButton(getContext(),"Edit",30 , 0, Color.parseColor("#336699"),
                        pos -> {

                                    showEditDialog(adapter.getItemAtPosition(pos) , pos);


                        }));

            }
        };
    }

    private void showEditDialog(OrderModel orderModel, int pos) {
        View layout_dialog;
        AlertDialog.Builder builder;
        if(orderModel.getOrderStatus() == 0)
        {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_shipping,null);
            builder = new AlertDialog.Builder(getContext() , android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
            .setView(layout_dialog);

        }
        else if(orderModel.getOrderStatus() == -1) // Cancelled
        {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_canclled,null);
            builder = new AlertDialog.Builder(getContext() )
            .setView(layout_dialog);
        }
        else // Shipped
        {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_shipped,null);
            builder = new AlertDialog.Builder(getContext() )
            .setView(layout_dialog);
        }

        // View
        Button btn_ok = (Button)layout_dialog.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button)layout_dialog.findViewById(R.id.btn_cancel);

        RadioButton rdi_shipping = (RadioButton)layout_dialog.findViewById(R.id.rdi_shipping);
        RadioButton rdi_shipped = (RadioButton)layout_dialog.findViewById(R.id.rdi_shipped);
        RadioButton rdi_cancelled = (RadioButton)layout_dialog.findViewById(R.id.rdi_cancelled);
        RadioButton rdi_delete = (RadioButton)layout_dialog.findViewById(R.id.rdi_delete);
        RadioButton rdi_restore_placed = (RadioButton)layout_dialog.findViewById(R.id.rdi_restore_placed);

        TextView txt_status = (TextView)layout_dialog.findViewById(R.id.txt_status);

        //set Data

        txt_status.setText(new StringBuilder("Order Status (")
        .append(common.convertStatusToString(orderModel.getOrderStatus()))
        .append(")"));

        //Create Dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        //custom dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(rdi_cancelled != null && rdi_cancelled.isChecked())
                    updateOrder(pos,orderModel,-1);

                else if(rdi_shipping != null && rdi_shipping.isChecked())
                    updateOrder(pos,orderModel, 1);

                else if(rdi_shipped != null && rdi_shipped.isChecked())
                    updateOrder(pos,orderModel, 2);

                else if(rdi_restore_placed != null && rdi_restore_placed.isChecked())
                    updateOrder(pos,orderModel, 0);

                else if(rdi_delete != null && rdi_delete.isChecked())
                    deleteOrder(pos,orderModel);
            }
        });




    }

    private void deleteOrder(int pos, OrderModel orderModel) {
        if(!TextUtils.isEmpty(orderModel.getKey()))
        {


            FirebaseDatabase.getInstance()
                    .getReference(common.ORDER_REF)
                    .child(orderModel.getKey())
                    .removeValue()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    adapter.removeItem(pos);
                    adapter.notifyItemRemoved(pos);
                    updateTextCounter();
                    Toast.makeText(getContext(), " Delete order success! ", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "Order number must not be null or empty!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOrder(int pos ,OrderModel orderModel , int status)
    {
        if(!TextUtils.isEmpty(orderModel.getKey()))
        {
            Map<String,Object> updateData = new HashMap<>();
            updateData.put("orderStatus",status);

            FirebaseDatabase.getInstance()
                    .getReference(common.ORDER_REF)
                    .child(orderModel.getKey())
                    .updateChildren(updateData)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {


                    //Show Dilaog
                    android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
                    dialog.show();

                    //First get token from user
                    FirebaseDatabase.getInstance()
                            .getReference(common.TOKEN_REF)
                            .child(orderModel.getUserId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.exists())
                                    {
                                        TokenModel tokenModel = dataSnapshot.getValue(TokenModel.class);
                                        Map<String,String> notiData = new HashMap<>();
                                        notiData.put(common.NOTI_TITLE , "Your Order was update");
                                        notiData.put(common.NOTI_CONTENT , new StringBuilder("Your order")
                                        .append(orderModel.getKey())
                                        .append("was update to")
                                        .append(common.convertStatusToString(status)).toString());

                                        FCMSendData sendData = new FCMSendData(tokenModel.getToken(),notiData);

                                        compositeDisposable.add(ifcmService.sendNotification(sendData)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(fcMresponse -> {
                                                dialog.dismiss();
                                            if(fcMresponse.getSuccess() == 1)
                                            {
                                                Toast.makeText(getContext(), " Update order success! ", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Toast.makeText(getContext(), " Update order success but failed to send notification! ", Toast.LENGTH_SHORT).show();
                                            }
                                                
                                        }, throwable -> {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }));

                                    }
                                    else
                                    {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "Token not found", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });





                    adapter.removeItem(pos);
                    adapter.notifyItemRemoved(pos);
                    updateTextCounter();

                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "Order number must not be null or empty!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTextCounter() {
        txt_order_filter.setText(new StringBuilder("Orders (")
                .append(adapter.getItemCount())
                .append(")"));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.action_filter)
        {
            BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
            bottomSheetOrderFragment.show(getActivity().getSupportFragmentManager(),"OrderFilter");
            return true;
        }

        else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if(EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent.class))
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent.class);
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }

    @Subscribe(sticky = true , threadMode = ThreadMode.MAIN)
    public void onLoadOrderEvent(LoadOrderEvent event)
    {
        orderViewModel.loadOrderByStatus(event.getStatus());
    }

}
