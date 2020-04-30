package com.example.androideatitv2_serverapp.Model.EventBus;

import com.example.androideatitv2_serverapp.Model.CategoryModel;

public class categoryClick {
    private boolean success;
    private CategoryModel categoryModel;

    public categoryClick(boolean success, CategoryModel categoryModel) {
        this.success = success;
        this.categoryModel = categoryModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public CategoryModel getCategoryModel() {
        return categoryModel;
    }

    public void setCategoryModel(CategoryModel categoryModel) {
        this.categoryModel = categoryModel;
    }
}
