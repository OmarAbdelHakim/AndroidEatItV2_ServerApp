package com.example.androideatitv2_serverapp.callback;

import com.example.androideatitv2_serverapp.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallBackListner {
    void onCategoryLoadingSuccess(List<CategoryModel> categoryModels);
    void onCategoryLoadingFailed(String message);
}
