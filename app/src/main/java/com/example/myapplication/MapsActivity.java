package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import android.widget.Toast;
import com.naver.maps.geometry.LatLng;
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
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private MapView mapView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private LocationSource.OnLocationChangedListener onLocationChangedListener;
    private NaverMap naverMap;

    private int time_left;
    private String start_name;
    private String arrive_name;

    private String startnum, stopnum;



    private String start_X,start_y,stop_X,stop_Y;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        locationSource = new FusedLocationSource(this,LOCATION_PERMISSION_REQUEST_CODE);

        locationSource.activate((LocationSource.OnLocationChangedListener) this);
        mapView.getMapAsync(this);


        Bundle bundle = intent.getExtras();

        start_X = bundle.getString("start_x");
        start_y = bundle.getString("start_y");
        stop_X = bundle.getString("stop_x");
        stop_Y = bundle.getString("stop_y");
        time_left = bundle.getInt("time_left");


        CountDownTimer countDownTimer = new CountDownTimer(time_left * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                System.out.println("Seconds remaining: " + seconds);

            }

            @Override
            public void onFinish() {
                System.out.println("Countdown finished!");
            }
        };

        countDownTimer.start();

        // 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            if (checkSelfPermission(android.Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY}, NOTIFICATION_PERMISSION_CODE);
            }
        }


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

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "알림 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "알림 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }



    }

    // 맵을 구현하고 좌표에 맞는 영역 구현
    public void onMapReady(@NonNull NaverMap naverMap) {
            this.naverMap = naverMap;
            float newZoomLevel = 15.0f;

            //시작역 좌표값 마커생성
            double startlocx = Double.parseDouble(start_X);
            double startlocy = Double.parseDouble(start_y);
            LatLng startlatlng = new LatLng(startlocx,startlocy);

            //도착역 xy 좌표값에 마커 생성
            double stoplocx = Double.parseDouble(stop_X);
            double stoplocy = Double.parseDouble(stop_Y);
            LatLng stoplatLng = new LatLng(stoplocx,stoplocy);

            Marker stop_marker = new Marker();
            Marker start_marker = new Marker();

            start_marker.setPosition(startlatlng);
            start_marker.setMap(naverMap);
            stop_marker.setPosition(stoplatLng);
            stop_marker.setMap(naverMap);

            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(stoplatLng);
            cameraUpdate.animate(CameraAnimation.Easing);
            cameraUpdate.zoomTo(newZoomLevel);


            naverMap.moveCamera(cameraUpdate);



            locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
            naverMap.setLocationSource(locationSource);



            //현재 위치 카메라 조정 ui
            UiSettings uiSettings = naverMap.getUiSettings();
            uiSettings.setLocationButtonEnabled(true);




        }


        /// 위치값이 변경되는걸 감지하고 알림을 호출하는 부분
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


       finish();
    }
}


