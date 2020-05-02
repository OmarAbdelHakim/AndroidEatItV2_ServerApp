package com.example.androideatitv2_serverapp.ui.order;

import com.example.androideatitv2_serverapp.Model.OrderModel;
import com.example.androideatitv2_serverapp.callback.IOrderCallBackListener;
import com.example.androideatitv2_serverapp.common.common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OrderViewModel extends ViewModel implements IOrderCallBackListener {

    private MutableLiveData<List<OrderModel>> orderModelMutableLiveData;
    private MutableLiveData<String> messageError;

    private IOrderCallBackListener listener;

    public OrderViewModel() {
      orderModelMutableLiveData = new MutableLiveData<>();
      messageError = new MutableLiveData<>();
      listener =this;
    }

    public MutableLiveData<List<OrderModel>> getOrderModelMutableLiveData() {
        loadOrderByStatus(0);
        return orderModelMutableLiveData;
    }

    public void loadOrderByStatus(int status) {

        List<OrderModel> tempList = new ArrayList<>();
        Query orderRef = FirebaseDatabase.getInstance().getReference(common.ORDER_REF)
                .orderByChild("orderStatus")
                .equalTo(status);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapshot : dataSnapshot.getChildren())
                {
                    OrderModel orderModel = itemSnapshot.getValue(OrderModel.class);
                    orderModel.setKey(itemSnapshot.getKey());
                    tempList.add(orderModel);
                }
                listener.onOrderLoadingSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onOrderLoadingFailed(databaseError.getMessage());
            }
        });

    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onOrderLoadingSuccess(List<OrderModel> orderModelList) {
            if(orderModelList.size() > 0)
            {
                Collections.sort(orderModelList,(orderModel,t1)->{
                    if(orderModel.getCreateDate() < t1.getCreateDate())
                        return -1;
                    return orderModel.getCreateDate() == t1.getCreateDate() ? 0:1;
                });
            }
            orderModelMutableLiveData.setValue(orderModelList);
    }

    @Override
    public void onOrderLoadingFailed(String message) {
        messageError.setValue(message);
    }
}