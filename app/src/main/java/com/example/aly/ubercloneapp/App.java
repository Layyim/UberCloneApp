package com.example.aly.ubercloneapp;

import android.app.Application;

import com.parse.Parse;

public class App extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("S2bTojt0T3eN7XeMD2ZAj4h9V7bvGbU2QYXyIAj9")
                // if defined
                .clientKey("HoT3KSJJ6sKiFNVux5YLhRActZwtBD0hJyyh3iRi")
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}
