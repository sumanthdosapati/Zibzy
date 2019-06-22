package com.ds.zibzy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button reg,log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reg=(Button)findViewById(R.id.reg);
        log=findViewById(R.id.log);

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reac=new Intent(getApplicationContext(),Register.class);
                startActivity(reac);
            }
        });
        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loac=new Intent(getApplicationContext(),Login.class);
                startActivity(loac);
            }
        });

    }
}
