package com.example.myapplication;

import android.annotation.SuppressLint;
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
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class textInput extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    EditText start_address;
    EditText arrive_address;
    Button search_button;
    private String location,location_st;
    private static final String SERVICE_KEY = "input your service key";
    private static final String API_URL = "http://data.humetro.busan.kr/voc/api/open_api_distance.tnn";

    private List<Integer> time_count = new ArrayList<>();

    private List<Integer> time_count2 = new ArrayList<>();




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_text);


        start_address = findViewById(R.id.start_address);
        arrive_address = findViewById(R.id.arrive_address);
        search_button = findViewById(R.id.search_button);


// CSV 파일을 읽어서 값을 저장할 리스트

        // 버튼 활성화
        //주소이름이 textInput 클래스에서 MapsActivity 클래스로 이동
        search_button.setClickable(true);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                location_st = start_address.getText().toString().trim();
                location = arrive_address.getText().toString().trim();

                if(location.isEmpty()||location_st.isEmpty()) {

                    Toast toast = Toast.makeText(getApplicationContext(), "도착 역을 입력해주세요", Toast.LENGTH_LONG);
                    toast.show();

                }else {

                    // CSV 파일 읽기
                    try {

                        String arriveName = arrive_address.getText().toString().trim();
                        String startName = start_address.getText().toString().trim();
                        List<String> sbnum = new ArrayList<>();
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
                               //역 번호_0, 이름_1 , x좌표_3, y좌표_4 값 을 읽어드림
                                sbnum.add(parts[0].trim());
                                names.add(parts[1].trim());
                                xlocation.add(parts[3].trim());
                                ylocation.add(parts[4].trim());

                            }
                        }

                        // 사용자 입력과 CSV 파일의 이름 비교하여 일치하는 값 찾기
                        int index = -1; // 일치하는 값이 발견되는 인덱스 초기화
                        int index_sub = -1;
                        for (int i = 0; i < names.size(); i++) {
                            if (names.get(i).equalsIgnoreCase(arriveName)) {
                                index = i; // 일치하는 값이 발견되면 인덱스 저장 후 반복문 종료
                                break;
                            }
                        }
                        //시작역 데이터 찾기
                        for(int i = 0; i < names.size();i++){
                            if(names.get(i).equalsIgnoreCase(startName)){
                                index_sub = i;
                                break;
                            }
                        }

                        // 결과 출력
                        if (index != -1) {

                            String startnum = sbnum.get(index_sub);
                            String stopnum = sbnum.get(index);
                            System.out.println(startnum);
                            System.out.println(stopnum);
                            find_direction(startnum,stopnum);

                            // String arriveloc = names.get(index);
                            String arr_stop_x = xlocation.get(index);
                            String arr_stop_y = ylocation.get(index);
                            String arr_start_x = xlocation.get(index_sub);
                            String arr_start_y = ylocation.get(index_sub);
                            int temp1 = Integer.parseInt(startnum);
                            int temp2 = Integer.parseInt(stopnum);
                            int timeSum = 0;


                            if(find_direction(startnum,stopnum)>0){
                                   for(int i = temp1;i > temp2;i--){
                                        call_travelTime(i,1); // 다대포 방면 계산

                                    }

                                for(int i = 0;i<time_count.size();i +=2){

                                    timeSum+=time_count.get(i);
                                    timeSum+=time_count2.get(i);

                                    System.out.println("다대포방명 : "+timeSum);

                                }

                            }else if(find_direction(startnum,stopnum) < 0){
                                    for(int i = temp1;i < temp2;i++){
                                        call_travelTime(i,-1); // 노포 방면 계산

                                    }

                                for(int i = 1;i<time_count.size();i +=2){

                                    timeSum+=time_count.get(i);
                                    timeSum+=time_count2.get(i);

                                    System.out.println("노포 방면 : "+timeSum);

                                }

                            }



                        // xfound x좌표, yfound y좌표 다음 액티비티로 값 전달
                            Intent intent = new Intent(textInput.this, MapsActivity.class);

                            intent.putExtra("start_x",arr_start_x);
                            intent.putExtra("start_y",arr_start_y);
                            intent.putExtra("stop_x", arr_stop_x);
                            intent.putExtra("stop_y", arr_stop_y);
                            intent.putExtra("time_left",timeSum);



                        //Maps액티비티 실행.
                            startActivity(intent);



                        } else {
                            System.out.println("Name not found.");
                            Toast.makeText(getApplicationContext(),"입력한 정거장을 찾지 못했습니다.\n 다시 확인해주세요.",Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });






    }
    public int find_direction(String num1, String num2){
        int startNum = Integer.parseInt(num1);
        int stopNum = Integer.parseInt(num2);
        int temp = startNum - stopNum;




        return temp;
       }

    public void call_travelTime(int call_id,int way)throws IOException{
        System.out.println(call_id);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    StringBuilder urlBuilder = new StringBuilder(API_URL);
                    urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY);
                    urlBuilder.append("&" + URLEncoder.encode("act", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("scode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(call_id), "UTF-8"));

                    URL url = new URL(urlBuilder.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                            System.out.println(sb.toString());
                        }

                        parseJSON(sb.toString());

                        reader.close();
                        inputStream.close();
                        conn.disconnect();


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        });

        thread.start();

        try {
            thread.join(); // 생성된 스레드가 종료될 때까지 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void parseJSON(String jsonString) {
        try {


            // JSON 데이터를 JSONObject로 변환
            JSONObject jsonObject = new JSONObject(jsonString);

            // 원하는 부분을 추출하여 파싱
            JSONObject responseObj = jsonObject.getJSONObject("response");
            JSONObject bodyObj = responseObj.getJSONObject("body");
            JSONArray itemArray = bodyObj.getJSONArray("item");
            // 예시: "key"라는 키의 값 추출

            // 파싱된 값 사용
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject getTime = itemArray.getJSONObject(i);
                String time = getTime.getString("time");
                int time1 = Integer.parseInt(time);
                String stopTime = getTime.getString("stoppingTime");
                int time2 = Integer.parseInt(stopTime);

                time_count.add(time1);
                time_count2.add(time2);



               System.out.println("time: " + time+", stoppingTime : "+stopTime);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
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
