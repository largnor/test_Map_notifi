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

public class MapsActivity extends Activity implements OnMapReadyCallback,LocationSource.OnLocationChangedListener{
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private MapView mapView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private LocationSource.OnLocationChangedListener onLocationChangedListener;
    private NaverMap naverMap;
    private int GEOFENCE_RADIUS = 200;
    private int aram_call = 0;
    private Location mCurrentLocation;
    private LatLng mOverlayLocation = new LatLng(35.267207,129.232717); // 임의의 오버레이 영역 좌표
    private String location_name;

    private String xlocation;
    private String ylocation;


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
        location_name = bundle.getString("location_name");
        xlocation = bundle.getString("xlocation");
        ylocation = bundle.getString("ylocation");


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

        //xy 좌표값에 마커 생성
            double locationx = Double.parseDouble(xlocation);
            double locationy = Double.parseDouble(ylocation);
            LatLng latLng = new LatLng(locationx,locationy);

            Marker marker = new Marker();
            marker.setPosition(latLng);
            marker.setMap(naverMap);

            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(latLng);
            cameraUpdate.animate(CameraAnimation.Easing);
            cameraUpdate.zoomTo(newZoomLevel);


            naverMap.moveCamera(cameraUpdate);



            locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
            naverMap.setLocationSource(locationSource);


            UiSettings uiSettings = naverMap.getUiSettings();
            uiSettings.setLocationButtonEnabled(true);

/*
            //원 생성
            CircleOverlay circleOverlay = new CircleOverlay();
            circleOverlay.setCenter(mOverlayLocation);
            circleOverlay.setRadius(GEOFENCE_RADIUS);
            circleOverlay.setColor(Color.TRANSPARENT);
            circleOverlay.setOutlineColor(Color.RED);
            circleOverlay.setOutlineWidth(3);



         */
             //circleOverlay.setMap(naverMap);
          //  naverMap.setLocationTrackingMode(LocationTrackingMode.Follow); // 위치추적 follow 모드 활성 조작시 nofollow 모드로 바뀜
          //  naverMap.moveCamera(cameraUpdate);


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


    /** 알림 채널 생성 및 호출*/
    public void createNotification(){
        show();
    }
    /** 알림호출시 내용 설정*/
    private void show(){
        int notificationId = 1;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "alarm_test");
        builder.setContentTitle("도착했습니다");
        builder.setContentText("도착했습니다");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel channel = new NotificationChannel("alarm_test", "테스트채널", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), attributes);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        builder.setLargeIcon(largeIcon);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        long[] pattern = {0, 500, 1000};
        builder.setVibrate(pattern);

        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(ringtoneUri);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
   }

    public void removeNotification(){
        hide();
    }
    public void hide(){
        NotificationManagerCompat.from(this).cancel(1);
    }


    /** 뒤로가기*/
    @Override
    public void onBackPressed() {
       super.onBackPressed();
       locationSource = null;
       removeNotification();

       finish();
    }
}


