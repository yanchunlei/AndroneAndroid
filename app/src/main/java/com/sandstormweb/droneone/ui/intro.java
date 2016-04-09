package com.sandstormweb.droneone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sandstormweb.droneone.R;

public class intro extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            setContentView(R.layout.intro);

            startWait();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startWait()
    {
        try{
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    finish();
                                    startActivity(new Intent(getApplicationContext(), Controller.class));
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {}
}
