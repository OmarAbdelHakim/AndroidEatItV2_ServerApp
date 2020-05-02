package com.example.androideatitv2_serverapp.callback;

import com.example.androideatitv2_serverapp.Model.OrderModel;

import java.util.List;

public interface IOrderCallBackListener {
    void onOrderLoadingSuccess(List<OrderModel> orderModelList);
    void onOrderLoadingFailed(String message);
}
