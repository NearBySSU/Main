package com.example.nearby;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Locale;

public class Utils {
    //위치 권한 확인 함수
    public static boolean checkLocationPermission(Activity activity, int requestCode) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(activity, "위치 권환 획득 실패! 앱 설정에서 위치 권환을 허용해 주세요", Toast.LENGTH_LONG).show();
            requestLocationPermission(activity, requestCode);
            return false;
        }
        return true;
    }

    //위치 권한 요청 함수
    public static void requestLocationPermission(Activity activity, int requestCode) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    requestCode);
        }
    }

    //알림 권한 요청 함수
    public static boolean checkNotifiPermission(Activity activity, int requestCode) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(activity, "알림 권환 획득 실패! 앱 설정에서 위치 권환을 허용해 주세요", Toast.LENGTH_LONG).show();
            requestNotifiPermission(activity, requestCode);
            return false;
        }
        return true;
    }

    //알림 권한 요청 함수
    public static void requestNotifiPermission(Activity activity, int requestCode) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    requestCode);
        }
    }

    //지역의 이름을 구하는 함수
    public static String[] getLocationName(Context context, Location location) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.KOREAN);
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                String adminArea = addresses.get(0).getAdminArea(); // '서울특별시'와 같은 정보를 가져옵니다.
                String locality = addresses.get(0).getSubLocality(); // '성북구'와 같은 정보를 가져옵니다.
                return new String[]{adminArea, locality};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


