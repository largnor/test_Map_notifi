package com.example.myapplication;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class textInput extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    EditText editText_address;
    Button search_button;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_text);


        editText_address = findViewById(R.id.editText_address);
        search_button = findViewById(R.id.search_button);



        // 버튼 활성화
        //주소이름이 textInput 클래스에서 MapsActivity 클래스로 이동
        search_button.setClickable(true);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String location = editText_address.getText().toString();


                if(location.isEmpty()) {

                    Toast toast = Toast.makeText(getApplicationContext(), "도착 역을 입력해주세요", Toast.LENGTH_LONG);
                    toast.show();
                }else {
                    Intent intent = new Intent(textInput.this, MapsActivity.class);
                    intent.putExtra("location_name", location);
                    startActivity(intent);
                }
            }
        });






    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("종료");
        builder.setMessage("종료하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 종료 버튼 클릭시 처리할 로직
                finishAffinity(); // 앱 종료
            }
        });
        builder.setNegativeButton("아니오", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
