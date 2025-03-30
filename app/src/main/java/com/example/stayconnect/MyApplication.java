package com.example.stayconnect;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        HashMap<String, Object> config = new HashMap<>();
        config.put("cloud_name", "dbd6eqanh"); // Replace with your Cloudinary cloud name
        config.put("api_key", "275353222815773");      // Replace with your Cloudinary API key
        config.put("api_secret", "xlHCmE7iUeki2by7eKk-8qFEXRA"); // Replace with your Cloudinary API secret

        MediaManager.init(this, config);
        //Alternatively
        //MediaManager.init(this, "cloud_name", "your_cloud_name", "api_key", "your_api_key", "api_secret", "your_api_secret");
    }
}
