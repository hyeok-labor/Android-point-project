package com.hyeok.point1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, PlacesListener {

    private GoogleMap mMap;
    private Marker currentMarker = null;

    private static final String TAG = "MapActivity:";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;         // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500;  // 0.5초

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    Location mCurrentLocatiion;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
                           // (참고로 Toast에서는 Context가 필요했습니다.)
    List<Marker> previous_marker = null;

    public static int MAX_ARRAY;        // 검색된 전체 매장 수
    public static String NameOfStore;   // 가까운 거리에 있는 매장 저장변수

    public static int numType = 3;

    public static double[] finalDistanceArray = new double[30];    // 매장 표시 개수
    public static String[] finalNameArray = new String[100];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_map);

        previous_marker = new ArrayList<Marker>();

        Button button = (Button) findViewById(R.id.button_map);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPlaceInformation(currentPosition);  // 주변 매장 마커 표시

            }
        });

        mLayout = findViewById(R.id.layout_map);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mMap = googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            startLocationUpdates(); // 3. 위치 업데이트 시작

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(MapActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d(TAG, "onMapClick :");
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                location = locationList.get(0);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());


                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);


                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);

                mCurrentLocatiion = location;
            }
        }

    };

    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap != null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
    }

    public void setDefaultLocation() {
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 여부 확인하세요";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메서드
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                } else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    // 여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");

                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }

    @Override
    public void onPlacesFailure(PlacesException e) {
        numType--;
        Log.d(TAG,"PlacesException  & numType : "+numType);
    }

    @Override
    public void onPlacesStart() {

    }

    /** Place API를 통해 검색된 맵정보를 표시하는 마커가 표시됩니다.**/
    public int numThread = 0;       // 스레드 횟수 저장변수
    @Override
    public void onPlacesSuccess(final List<Place> places) {
        Log.d(TAG, "onPlacesSuccess!!! ");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = 0;      // for문이 돌면서 i값 증가시킴 == 마커 개수

                double[] distanceArray = new double[30];    // 매장 표시 개수
                String[] nameArray = new String[100];

                // 검색이 가능할 때까지 반복

                for (noman.googleplaces.Place place : places) {
                    LatLng latLng
                            = new LatLng(place.getLatitude()
                            , place.getLongitude());

                    String marketSnippet = getCurrentAddress(latLng);

                    // 생성된 마커에 해당 정보 표시
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(marketSnippet);
                    Marker item = mMap.addMarker(markerOptions);
                    previous_marker.add(item);

                    // Log를 통해 위도, 경도 및 매장명을 확인
                    Log.d(TAG, "Result : " + "위도:" + place.getLatitude()
                            + " 경도:" + place.getLongitude() + " 매장명:" + place.getName());

                    // 계산된 거리, 지명을 배열에 저장
                    distanceArray[i] = distance(place.getLatitude(), place.getLongitude(),
                            location.getLatitude(), location.getLongitude());
                    nameArray[i] = place.getName();

                    Log.d(TAG, "distanceArray[" + i + "]:" + distanceArray[i]);
                    Log.d(TAG, "nameArray[" + i + "]:" + nameArray[i]);
                    i++;
                }   // end for

                MAX_ARRAY = i;  // 전역 변수로 선언된 MAX_ARRAY
                Log.d(TAG, "MAX_ARRAY :" + i);

                // 최단거리 마커 찾기
                int nearestIndex = nearest(distanceArray, MAX_ARRAY);
                Log.d(TAG, "index[" + nearestIndex + "] ==> " + nameArray[nearestIndex]);
                NameOfStore = searchText(nameArray[nearestIndex]);    // 작업이 끝나고 최단거리 매장이름값

                finalDistanceArray[numThread] = distanceArray[nearestIndex];    // run 실행 후 얻은 값을 배열에 저장
                finalNameArray[numThread++] = NameOfStore;                      // 스레드횟수를 증가 시킨다.

                Log.d(TAG, "numThread : "+ numThread);
                Log.d(TAG, "========================================================" );

                if(numThread==numType){     // 모든 run() 이 종료 되었을때, 각 run()에서 얻어진 최단거리 값에서 가장 작은 수를 구함.
                    nearestIndex = nearest(finalDistanceArray, numType);
                    Log.d(TAG, "FinalIndex[" + nearestIndex + "] ==> " + finalNameArray[nearestIndex]);
                    NameOfStore = searchText(finalNameArray[nearestIndex]);
                    // 팝업 다이어로그
                    createDialog();
                    numThread = 0;      // 재검색을 위해 0으로 초기화
                    Log.d(TAG, "========================================================" );
                }

                // 중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);
            }
        });


    }

    @Override
    public void onPlacesFinished() {
        // run 한바퀴 돌면 finished.
    }

    public void showPlaceInformation(LatLng location) {
        mMap.clear();                     //지도 클리어

        if (previous_marker != null)
            previous_marker.clear();      //지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(MapActivity.this)
                .key("AIzaSyDTyDS9Uz_DRdJrm0wDcJhYKkf9l3P-T0Q") // Place API KEY
                .latlng(location.latitude, location.longitude)  // 현재 위치
                .radius(100)
                .type(PlaceType.BAKERY)
                .build()
                .execute();

        new NRPlaces.Builder()
                .listener(MapActivity.this)
                .key("AIzaSyDTyDS9Uz_DRdJrm0wDcJhYKkf9l3P-T0Q") // Place API KEY
                .latlng(location.latitude, location.longitude)  // 현재 위치
                .radius(100)
                .type(PlaceType.CAFE)
                .build()
                .execute();

        new NRPlaces.Builder()
                .listener(MapActivity.this)
                .key("AIzaSyDTyDS9Uz_DRdJrm0wDcJhYKkf9l3P-T0Q") // Place API KEY
                .latlng(location.latitude, location.longitude)  // 현재 위치
                .radius(100)
                .type(PlaceType.CONVENIENCE_STORE)
                .build()
                .execute();

    }

    // 두 지점간의 거리를 계산합니다.
    private static double distance(double lat1, double lon1, double lat2, double lon2) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;  // mile 단위
        dist = dist * 1609.344;     // mile --> meter 로 단위 변환

        return (dist);
    }

    // decimal degrees 를 radians 값으로 convert
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // radians 값을 decimal degrees 값으로 convert
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    // 가장 가까운 거리의 인덱스를 return
    public static int nearest(double[] array1, int max_array) {

        int[] data = new int[max_array];
        int index = 0;

        // double -> int type casting -> store to data array
        for (int i = 0; i < max_array; i++) {
            data[i] = (int) array1[i];
        }

        int target = 0;     // target과 가장 가까운 수를 찾는다. ==> 0과 가까움 == 현재 위치와 가장 근접
        int near = 0;       // 가장 가까운수 저장변수
        int min = 100;      // 차이값의 최소값을 저장할 변수. 초기값은 100(반경설정값)

        // 처리
        for (int i = 0; i < data.length; i++) {
            int a = data[i] - target;
            if (min > a) {
                min = a;
                near = data[i];
            }
        }

        // 출력
        Log.d(TAG, "가장가까운값(near):" + near);

        // 변수 near의 인덱스찾기
        for (index = 0; index < data.length; index++) {
            if (data[index] == near)
                break;
        }

        Log.d(TAG, "index:" + index);

        return index;
    }

    // 매장에 맞는 포인트 문자열 반환
    public static String searchText(String str) {

        // Todo : 매장과 상응하는 해당 포인트 또는 멤버십을 업데이트 해야함
        // 해당 문자를 포함할 경우
        if (str.contains("투썸") || str.contains("twosome") || str.contains("TWOSOME") ) {
            str = "TWOSOMEPLACE";
        }
        else if (str.contains("파리") || str.contains("paris") || str.contains("PARIS") ) {
            str = "HAPPYPOINT";
        }
        else if (str.contains("뚜레") || str.contains("tous") || str.contains("TOUS") ) {
            str = "CJONE";
        }
        else if (str.contains("쥐에스") || str.contains("gs") || str.contains("GS") ) {
            str = "GSPOINT";
        }
        else if (str.contains("씨유") || str.contains("cu") || str.contains("CU") ) {
            str = "CUPOINT";
        }
        else if (str.contains("스타벅") || str.contains("starb") || str.contains("STARB") ) {
            str = "STARBUCKS";
        }
        // 어떤 문자도 포함하지 않는 경우 그대로 리턴
        return str;
    }

    // 팝업 다이어로그 메서드
    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("<"+NameOfStore+"> 이 맞나요?");
        builder.setMessage("확인을 누르면 포인트로 이동.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // "확인" 버튼 클릭시 실행할 메서드
                Toast.makeText(getBaseContext(), NameOfStore + "불러오는중...", Toast.LENGTH_SHORT).show();

                // Intent를 통해 EditActivity로 전환
                Intent i = new Intent(MapActivity.this,EditActivity.class);

                i.putExtra("key",NameOfStore);
                startActivity(i);
            }
        });
        builder.setNegativeButton("재검색", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // "재검색" 버튼 클릭시 실행할 메서드
                Toast.makeText(getBaseContext(), "재검색하세요...", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNeutralButton("취소", null);
        builder.create().show();
    }
}