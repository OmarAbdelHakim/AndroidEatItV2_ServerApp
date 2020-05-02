package com.example.androideatitv2_serverapp.ui.order;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.example.androideatitv2_serverapp.Model.EventBus.AddonSizeEditEvent;
import com.example.androideatitv2_serverapp.Model.EventBus.ChangeMenuClick;
import com.example.androideatitv2_serverapp.Model.EventBus.LoadOrderEvent;
import com.example.androideatitv2_serverapp.Model.FoodModel;
import com.example.androideatitv2_serverapp.Model.OrderModel;
import com.example.androideatitv2_serverapp.R;
import com.example.androideatitv2_serverapp.SizeAddonEditActivity;
import com.example.androideatitv2_serverapp.adapter.MyOrderAdapter;
import com.example.androideatitv2_serverapp.common.BottomSheetOrderFragment;
import com.example.androideatitv2_serverapp.common.MySwipeHelper;
import com.example.androideatitv2_serverapp.common.common;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class OrderFragment extends Fragment {

    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;

    @BindView(R.id.txt_order_filter)
    TextView txt_order_filter;


    Unbinder unbinder;

    LayoutAnimationController layoutAnimationController;
    MyOrderAdapter adapter;


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

                    txt_order_filter.setText(new StringBuilder("Orders (")
                    .append(orderModels.size())
                    .append(")"));

                }
            }
        });
        return root;
    }

    private void initView() {

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
                                                txt_order_filter.setText(new StringBuilder("Orders (")
                                                .append(adapter.getItemCount())
                                                .append(")"));
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

                            // create a new activity to display them size and addon


                        }));

            }
        };
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_filter:
                BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
                bottomSheetOrderFragment.show(getActivity().getSupportFragmentManager(),"OrderFilter");
                break;
        }

        return true;
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
