package com.example.androideatitv2_serverapp.Model.EventBus;

import com.example.androideatitv2_serverapp.Model.AddonModel;

import java.util.List;

public class UpdateAddonModel {

    private List<AddonModel> addonModels;

    public UpdateAddonModel() {

    }

    public List<AddonModel> getAddonModels() {
        return addonModels;
    }

    public void setAddonModels(List<AddonModel> addonModels) {
        this.addonModels = addonModels;
    }
}
