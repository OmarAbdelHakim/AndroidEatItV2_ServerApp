package com.example.androideatitv2_serverapp.ui.food_list;

import com.example.androideatitv2_serverapp.Model.FoodModel;
import com.example.androideatitv2_serverapp.common.common;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FoodListViewModel extends ViewModel {

    private MutableLiveData<List<FoodModel>> mutableLiveDataFoodList;

    public FoodListViewModel() {

    }

    public MutableLiveData<List<FoodModel>> getMutableLiveDataFoodList() {

        if(mutableLiveDataFoodList == null)

            mutableLiveDataFoodList = new MutableLiveData<>();
        mutableLiveDataFoodList.setValue(common.categorySelected.getFoods());



        return mutableLiveDataFoodList;
    }
}