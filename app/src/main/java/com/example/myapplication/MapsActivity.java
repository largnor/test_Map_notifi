package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationSource;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends Activity implements OnMapReadyCallback,LocationSource.OnLocationChangedListener{

    private MapView mapView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private FusedLocationSource locationSource;
    private LocationSource.OnLocationChangedListener onLocationChangedListener;
    private NaverMap naverMap;
    private CountDownTimer countDownTimer;


    private static final String CHANNEL_ID = "my_channel_id";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;


    private int time_left;


    private String start_X,start_y,stop_X,stop_Y;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);


        Button startButton = findViewById(R.id.myButton);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.VIBRATE)
                        == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 이미 부여되었으므로 진행
                    // 알림 관련 코드 실행
                        setCountDownTimer();
                } else {
                    // 권한이 부여되지 않았으므로 권한 요청

                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.VIBRATE}, PERMISSION_REQUEST_CODE);
                }


                startButton.setVisibility(View.GONE);
                Toast.makeText(MapsActivity.this, "안내를 시작합니다", Toast.LENGTH_SHORT).show();


            }
        });



        locationSource = new FusedLocationSource(this,LOCATION_PERMISSION_REQUEST_CODE);

        locationSource.activate((LocationSource.OnLocationChangedListener) this);
        mapView.getMapAsync(this);


        Bundle bundle = intent.getExtras();

        start_X = bundle.getString("start_x");
        start_y = bundle.getString("start_y");
        stop_X = bundle.getString("stop_x");
        stop_Y = bundle.getString("stop_y");
        time_left = bundle.getInt("time_left");


    }

    private void setCountDownTimer(){

        countDownTimer = new CountDownTimer(time_left * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                showNotification(millisUntilFinished);
                if(millisUntilFinished<=30000){
                    showNotification(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                cancelNotification();
                System.out.println("Countdown finished!");
            }
        };
        countDownTimer.start();
    }



    // request code와 권한획득 여부 확인
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (locationSource.onRequestPermissionsResult( requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
                return;
            }
        }

    }

    public void onMapReady(@NonNull NaverMap naverMap) {
            this.naverMap = naverMap;


            //시작역 좌표값 마커생성
            double startlocx = Double.parseDouble(start_X);
            double startlocy = Double.parseDouble(start_y);
            LatLng startlatlng = new LatLng(startlocx,startlocy);

            //도착역 xy 좌표값에 마커 생성
            double stoplocx = Double.parseDouble(stop_X);
            double stoplocy = Double.parseDouble(stop_Y);
            LatLng stoplatLng = new LatLng(stoplocx,stoplocy);


            double center_x = (startlocx+stoplocx)/2;
            double center_y = (startlocy+startlocy)/2;
            LatLng target = new LatLng(center_x,center_y);


            LatLngBounds bounds = new LatLngBounds(startlatlng,stoplatLng);
            int padding = 200;
            Marker stop_marker = new Marker();
            Marker start_marker = new Marker();

            start_marker.setPosition(startlatlng);
            start_marker.setMap(naverMap);
            stop_marker.setPosition(stoplatLng);
            stop_marker.setMap(naverMap);

            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(target)
                                                    .animate(CameraAnimation.Easing)
                                                    .fitBounds(bounds, padding);



            naverMap.moveCamera(cameraUpdate);



            locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
            naverMap.setLocationSource(locationSource);



            //현재 위치 카메라 조정 ui
            UiSettings uiSettings = naverMap.getUiSettings();
            uiSettings.setLocationButtonEnabled(true);




        }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "My Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void showNotification(long millisUntilFinished) {

        long seconds = millisUntilFinished / 1000;
        long re_minute = seconds / 60;
        long re_second = seconds % 60;
        String context = re_minute+ " 분 " + re_second+ " 초 ";

/*
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("예상시간입니다")
                    .setContentText(context+" 남았습니다")
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setVibrate(new long[]{0, 500});
        }
*/


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("예상시간입니다")
                .setContentText(context + " 남았습니다")
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setVibrate(new long[]{0, 1000});


        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void cancelNotification() {
         notificationManager.cancel(NOTIFICATION_ID);
    }


        // 위치값이 변경되는걸 감지하고 알림을 호출하는 부분
    @Override
    public void onLocationChanged(@NonNull Location location) {
        /*
            int count=0;
            mCurrentLocation = location;
             Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

            if (onLocationChangedListener != null) {
                onLocationChangedListener.onLocationChanged(location);
            }

            // 현재 위치와 오버레이 좌표간의 거리 계산
            float[] distance = new float[2];
            Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                    mOverlayLocation.latitude, mOverlayLocation.longitude, distance);
            float distanceInMeters = distance[0];

            // 거리가 설정한 반경보다 작을 경우 알림 출력
            if (distanceInMeters <= GEOFENCE_RADIUS) {
                if (aram_call == 0) {
                    createNotification();
                    aram_call++;
                }
            }

         */
    }


    public void activate(LocationSource.OnLocationChangedListener listener) {
        onLocationChangedListener = listener;
    }

    public void deactivate() {
        onLocationChangedListener = null;
    }



      /** 뒤로가기*/
    @Override
    public void onBackPressed() {
       super.onBackPressed();
       locationSource = null;

       if(countDownTimer != null){
           countDownTimer.cancel();
           System.out.println("Count Cancel");
       }
        cancelNotification();

       finish();
    }
}


