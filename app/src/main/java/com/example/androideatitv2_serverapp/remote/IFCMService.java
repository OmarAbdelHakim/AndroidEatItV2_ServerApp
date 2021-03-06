package com.example.androideatitv2_serverapp.remote;

import com.example.androideatitv2_serverapp.Model.FCMSendData;
import com.example.androideatitv2_serverapp.Model.FCMresponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAYGZ3acw:APA91bFB1nocKbptOcVgjchdvK4AqzYdpag9RTa6C3wYGe3szUWJGOBmixNlhozxK6ZANidOnhKs9DbAq9V2FMsTiJCSMukMA7ACYnsLBk0OTbqN57dONYeD-GFFEoOzHRcfLLjHICxa"
    })
    @POST("fcm/send")
    Observable<FCMresponse> sendNotification(@Body FCMSendData body);
}
