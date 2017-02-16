package com.aleckeller.deliverit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by aleckeller on 2/16/17.
 */
public class CheckoutActivity extends AppCompatActivity {
    private Button backLocationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout);

        backLocationBtn = (Button) findViewById(R.id.locButton);
        backLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locIntent = new Intent(CheckoutActivity.this, LocationActivity.class);
                startActivity(locIntent);
                finish();
            }
        });
    }
}
