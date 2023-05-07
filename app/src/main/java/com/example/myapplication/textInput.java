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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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



// CSV 파일을 읽어서 값을 저장할 리스트

        // 버튼 활성화
        //주소이름이 textInput 클래스에서 MapsActivity 클래스로 이동
        search_button.setClickable(true);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String location = editText_address.getText().toString();
                String clientId = "f0mtotdif9";
                String clientSecret = "v9rgXsx7Rw6OaXCvf1k2JSLXAApjPNmUWMMhOFZc";

               /*
                String clientId = "{클라이언트 아이디 입력}";
                String clientSecret = "{시크릿 키 입력}";
                */
                if(location.isEmpty()) {

                    Toast toast = Toast.makeText(getApplicationContext(), "도착 역을 입력해주세요", Toast.LENGTH_LONG);
                    toast.show();
                }else {




                    // CSV 파일 읽기
                    try {
                        String targetName = editText_address.getText().toString();
                        List<String> names = new ArrayList<>();
                        List<String> xlocation = new ArrayList<>();
                        List<String> ylocation = new ArrayList<>();

                        InputStream inputStream = getAssets().open("locationSub.csv");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // CSV 파일의 각 라인을 쉼표(,)로 분리하여 데이터 추출
                            String[] parts = line.split(",");
                            if (parts.length >= 7) {
                                // 역사명, 노선번호, 노선명, 역위도, 역경도, 환승역구분, 데이터기준일자 값을 각각 저장
                                names.add(parts[0].trim());
                                xlocation.add(parts[3].trim());
                                ylocation.add(parts[4].trim());

                            }
                        }

                        // 사용자 입력과 CSV 파일의 이름 비교하여 일치하는 값 찾기
                        int index = -1; // 일치하는 값이 발견되는 인덱스 초기화
                        for (int i = 0; i < names.size(); i++) {
                            if (names.get(i).equalsIgnoreCase(targetName)) {
                                index = i; // 일치하는 값이 발견되면 인덱스 저장 후 반복문 종료
                                break;
                            }
                        }

                        // 결과 출력
                        if (index != -1) {
                            String foundName = names.get(index);

                            String xfound = xlocation.get(index);
                            String yfound = ylocation.get(index);

                            System.out.println("Name: " + foundName);
                            System.out.println("X Location: " + xfound);
                            System.out.println("Y Location: " + yfound);


                        // xfound x좌표, yfound y좌표 다음 액티비티로 값 전달
                            Intent intent = new Intent(textInput.this, MapsActivity.class);
                            intent.putExtra("location_name", location);
                            intent.putExtra("xlocation", xfound);
                            intent.putExtra("ylocation", yfound);

                            startActivity(intent);

                            // TODO: 필요한 작업 수행 (예: 데이터 저장, 화면에 출력 등)
                        } else {
                            System.out.println("Name not found.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                /*
                   new Thread(new Runnable() {
                       @Override
                       public void run() {
                           try {
                               String query = URLEncoder.encode(location, "UTF-8");
                               String apiURL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + query;
                               URL url = new URL(apiURL);
                               HttpURLConnection con = (HttpURLConnection)url.openConnection();
                               con.setRequestMethod("GET");
                               con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
                               con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
                               int responseCode = con.getResponseCode();
                               BufferedReader br;
                               if(responseCode==200) { // 정상 호출
                                   br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                               } else {  // 에러 발생
                                   br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                               }
                               String inputLine;
                               StringBuffer response = new StringBuffer();
                               while ((inputLine = br.readLine()) != null) {
                                   response.append(inputLine);
                               }
                               br.close();
                               System.out.println(response.toString());
                               JSONObject jsonObj = new JSONObject(response.toString());
                               JSONArray addresses = jsonObj.getJSONArray("addresses");
                               JSONObject address = addresses.getJSONObject(0);
                               String x = address.getString("y");
                               String y = address.getString("x");

                               System.out.println(x);
                               System.out.println(y);


                           } catch (Exception e) {
                               System.out.println(e);
                           }
                       }
                   }).start();



  */

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
