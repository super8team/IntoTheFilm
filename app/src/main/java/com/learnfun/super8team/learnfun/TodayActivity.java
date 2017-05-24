package com.learnfun.super8team.learnfun;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class TodayActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener,AdapterView.OnItemSelectedListener {

    private GoogleMap mMap;
    private static int MY_LOCATION_REQUEST_CODE = 2000;
    private LocationManager locationManager;
    View marker_root_view;
    MarkerOptions myMarker=null;
    private Socket socket=null;

    LatLng SEOUL = new LatLng(37.56, 126.97);
    JSONObject classOne;
    JSONObject classTwo;
    JSONObject classThree;

    //ArrayList<LatLng> classOne = new ArrayList();
    ArrayList<Marker> classOneMarker = new ArrayList();
    //ArrayList<LatLng> classTwo = new ArrayList();
    ArrayList<Marker> classTwoMarker = new ArrayList();
    //ArrayList<LatLng> classThree = new ArrayList();
    ArrayList<Marker> classThreeMarker = new ArrayList();


    ArrayAdapter<String> adapter;
    Spinner spinner;

//    void addOneClass(){
//
//        LatLng latLng1 = new LatLng(35.897532, 128.622696);
//        LatLng latLng2 = new LatLng(35.896664, 128.619985);
//
//
//        classOne.add(latLng1); classOne.add(latLng2);
//
//    }
//
//    void addTwoClass(){
//
//
//        LatLng latLng3 = new LatLng(35.891765, 128.614243);
//        LatLng latLng4 = new LatLng(35.897884, 128.608298);
//
//
//        classTwo.add(latLng3);classTwo.add(latLng4);
//
//    }
//
//    void addThreeClass(){
//
//
//        LatLng latLng5 = new LatLng(35.895238, 128.622741);
//        LatLng latLng6 = new LatLng(35.893233, 128.624808);
//
//
//        classThree.add(latLng5);classThree.add(latLng6);
//
//    }

//    void removeClassMarker(String className){ //각 마커별 이름을 구별해야함, 디비에 각 학생들의 gps를 저장해야함, 자신의 gps를 저장하는 db
//        Log.d("tag", "리무브마커함수 실행");
//        if(className.equals("1반")){
//            if(!classOneMarker.isEmpty()) {
//                Log.d("tag", "1반지우기");
//                for (int i = 0; i < classOneMarker.size(); i++) {
//                    classOneMarker.get(i).remove();
//                    classOneMarker.clear();
//
//                }
//            }
//        }else if(className.equals("2반")){
//            if(!classTwoMarker.isEmpty()) {
//                Log.d("tag", "2반지우기");
//                for (int i = 0; i < classTwoMarker.size(); i++) {
//                    classTwoMarker.get(i).remove();
//                    classTwoMarker.clear();
//                }
//            }
//        }else{
//            if(!classThreeMarker.isEmpty()) {
//                Log.d("tag", "3반지우기");
//                for (int i = 0; i < classThreeMarker.size(); i++) {
//                    classThreeMarker.get(i).remove();
//                    classThreeMarker.clear();
//                }
//            }
//        }
//
//    }
//    void repeatRemoveMarker(String className){
//        for(int i=0; i < classOneMarker.size();i++){
//            classTwoMarker.get(i).remove();
//            classThreeMarker.get(i).remove();
//
//            classTwoMarker.clear();
//            classThreeMarker.clear();
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("tag", "온크리에이트 실행");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        addOneClass();
//        addTwoClass();
//        addThreeClass();

        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.class_Name,android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(parent.getContext(),"선택한것은"+parent.getItemAtPosition(position),Toast.LENGTH_SHORT).show();
                mMap.clear();

                if(parent.getItemAtPosition(position).equals("1반")){
                    classOneMarker.clear();
                    socket.emit("class1", "I want to class1's GPS");
                    //addStudentMarker(classOne, "#21ff3f", "1반");
                    if(myMarker!=null) mMap.addMarker(myMarker);

                }else if(parent.getItemAtPosition(position).equals("2반")){
                    classTwoMarker.clear();
                    socket.emit("class2", "I want to class2's GPS");
                    //addStudentMarker(classTwo, "#24d8b4","2반");
                    if(myMarker!=null) mMap.addMarker(myMarker);

                }else{
                    classThreeMarker.clear();
                    socket.emit("class3", "I want to class3's GPS");
                    //addStudentMarker(classThree,"#ebf224","3반");
                    if(myMarker!=null) mMap.addMarker(myMarker);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //it can go to the ManagementPage for student
        //Button manageStudnetBtn = (Button)findViewById(R.id.manageStudent);
        //manageStudnetBtn.setOnClickListener(new View.OnClickListener(){

//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), StudentListActivity.class);
//                startActivity(intent);
//            }
  //      });

            try{
                socket = IO.socket("http://172.19.1.166:8000");


                socket.on(Socket.EVENT_CONNECT, listenStartPerson);
                //.on("getclass1", listen_start_person)
                socket.on("getclass", listenGetMessagePerson);

                socket.connect();


            }
            catch(Exception e){

            }



    }//oncreate function end

    @Override
    protected void onDestroy() {
        socket.on(Socket.EVENT_DISCONNECT, listenDisconnectPerson);
        super.onDestroy();
    }

    private Emitter.Listener listenStartPerson = new Emitter.Listener() {

        public void call(Object... args) {

            socket.emit("Connection", "i can connect to you");
            //서버에서 보낸 JSON객체를 사용할 수 있습니다.

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //이곳에 ui 관련 작업을 할 수 있습니다.


                }
            });
        }
    };
    private Emitter.Listener listenGetMessagePerson = new Emitter.Listener() {

        public void call(Object... args) {
            Log.d("loglog", "class1 받았다!!!!!!!!!!!");
            final JSONObject obj = (JSONObject)args[0];


            //서버에서 보낸 JSON객체를 사용할 수 있습니다.

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //위에서 오브젝트를 받은것을 다시 제이슨배열로 해체
                        JSONArray jsonArray = new JSONArray(obj.getString("class"));
                        //제이슨배열을 만든것을 하나씩 제이슨 객체로 만듬
                        for(int i =0; i < jsonArray.length(); i++){

                            JSONObject dataJsonObject = jsonArray.getJSONObject(i);// 0에 cho 객체가 있음
                            //제이슨 객체안의 데이터를 빼온다
                            Log.d("studentname", dataJsonObject.getString("class"));

                            if(dataJsonObject.getString("class").equals("1")){
                                addStudentMarker(jsonArray, "#21ff3f", "1반");
                            }else if(dataJsonObject.getString("class").equals("2")){
                                addStudentMarker(jsonArray,"#ebf224","2반");
                            }else{
                                addStudentMarker(jsonArray,"#24d8b4","3반");

                            }
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //이곳에 ui 관련 작업을 할 수 있습니다.


                }
            });
        }
    };
    private Emitter.Listener listenDisconnectPerson = new Emitter.Listener() {

        public void call(Object... args) {
            Log.d("loglog", "연결 끊는다");
            socket.disconnect();


            //서버에서 보낸 JSON객체를 사용할 수 있습니다.

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //이곳에 ui 관련 작업을 할 수 있습니다.


                }
            });
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                Log.d("TAG", "onMapLoaded 체크퍼미션 실행했다");

                checkLocationPermission();
                //  addImageMarker(); // 새로운 이미지 마커를 박음
            }


        });

    }//onMapReady function end



//
//    protected void onStart() {
//        if(mGoogleApiClient==null){
//            buildGoogleApiClient();
//
//        }
//
//        super.onStart();
//    }

    protected void onStop() {
        //mGoogleApiClient.disconnect();
        super.onStop();
    }






    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            //퍼미션 요청을 위해 UI를 보여줘야 하는지 검사

            Log.d("TAG", "checkLocationPermission" + "이미 퍼미션 획득한 경우");
            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);


        } else {
            Log.d("TAG", "checkLocationPermission" + "퍼미션 없는경우 권한신청");
            // Show rationale and request permission.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_LOCATION_REQUEST_CODE);



        }
        return true;
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("TAG", "onRequestPermissionsResult실행");
//        Log.d("permissions", permissions[0]);
//        Log.d("permissions.length", Integer.toString(permissions.length));
//        Log.d("grantResults", Integer.toString(grantResults[0]));
//        Log.d("ACCESS_FINE_LOCATION", Manifest.permission.ACCESS_FINE_LOCATION);
//        Log.d("PERMISSION_GRANTED", Integer.toString(PackageManager.PERMISSION_GRANTED));


        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            Log.d("TAG", "requestCode if문 안에 들어왔다");
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)  &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {



                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "퍼미션 획득시 마커를 추가 해준다");


                    locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                    locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);

                    //mMap.setMyLocationEnabled(true);
                    Log.d("TAG", "connect 실행했다");

                }
            } else {
                // Permission was denied. Display an error message.
                Log.d("TAG", "퍼미션취소");

            }
        }
    }//onRequestPermissionsResult end

//    private void setCustomMarkerView() {
//        marker_root_view = LayoutInflater.from(this).inflate(R.layout.activity_maps, null);
//        //tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);
//        }


    private android.location.LocationListener locationListener = new android.location.LocationListener() {


        @Override
        public void onLocationChanged(Location location) {
            //setCustomMarkerView();
            Log.d("TAG", "onLocationChanged에 들어왔다");

            LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            if(myMarker==null) addMyMarker(myLatLng);
            else myMarker.position(myLatLng);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

//    // View를 Bitmap으로 변환
//    private Bitmap createDrawableFromView(Context context, View view) {
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
//        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
//        view.buildDrawingCache();
//        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
//
//        Canvas canvas = new Canvas(bitmap);
//        view.draw(canvas);
//
//        return bitmap;
//    }

    private void addMyMarker(LatLng latLng){
        Log.d("TAG", "마커생성!!!!!!!!!!!!!!!!!");
        //현재 위치에 마커 생성

        myMarker = new MarkerOptions();
        myMarker.position(latLng);
        myMarker.icon(getMarkerIcon("#e21d24")); // change the color of marker(red)
        myMarker.title("현재위치");
        mMap.addMarker(myMarker);

        //지도 상에서 보여주는 영역 이동
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //mGoogleApiClient.connect();
    }

    private void addStudentMarker(JSONArray arrayList,String markerColor,String className) throws JSONException {
        Log.d("TAG", "학생마커 생성");

        for(int i = 0 ; i < arrayList.length();i++){
            Log.d("TAG", "포문안에 들어왔다");
            //제이슨배열을 만든것을 하나씩 제이슨 객체로 만듬
            JSONObject dataJsonObject = arrayList.getJSONObject(i);
            Log.d("TAG", "제이슨 오브젝트로 만들었다");
            Log.d("TAG", String.valueOf(dataJsonObject.getDouble("lat")));
            LatLng studentGPS = new LatLng(dataJsonObject.getDouble("lat"), dataJsonObject.getDouble("lng"));
            Log.d("TAG", "좌표객체만들었다");
            MarkerOptions markerOptions = new MarkerOptions();
            Log.d("TAG", "마커옵션 만들었다");
            markerOptions.position(studentGPS);
            Log.d("TAG", "마커에 좌표넣었다");
            markerOptions.icon(getMarkerIcon(markerColor)); // change the color of marker
            Log.d("TAG", "아이콘색변경했다");
            markerOptions.title(dataJsonObject.getString("name")+className);
            Log.d("TAG", "마커이름 바꿨다");
            Marker studentMarker = mMap.addMarker(markerOptions);
            Log.d("TAG", "마커 생성하고 맵에 추가했다");

            if(className.equals("1반")){
                Log.d("tag", "1반 마커배열에 넣음");
                classOneMarker.add(studentMarker);
            }else if(className.equals("2반")){
                Log.d("tag", "2반 마커배열에 넣음");
                classTwoMarker.add(studentMarker);
            }else{
                Log.d("tag", "3반 마커배열에 넣음");
                classThreeMarker.add(studentMarker);
            }
        }


    }

    // method definition //change the color of marker
    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }
//    private void addImageMarker(){
//
////        View marker = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.onbin_marker, null);
////        //현재 위치에 마커 생성
//        ArrayList<LatLng> a = new ArrayList();
//        LatLng latLng1 = new LatLng(35.897532, 128.622696);
//        LatLng latLng2 = new LatLng(35.896664, 128.619985);
//        LatLng latLng3 = new LatLng(35.891765, 128.614243);
//        LatLng latLng4 = new LatLng(35.897884, 128.608298);
//        LatLng latLng5 = new LatLng(35.895238, 128.622741);
//        LatLng latLng6 = new LatLng(35.893233, 128.624808);
//
//        a.add(latLng1); a.add(latLng2); a.add(latLng3);a.add(latLng4);a.add(latLng5);a.add(latLng6);
//
//
////
////        MarkerOptions markerOptions = new MarkerOptions();
////        markerOptions.position(latLng);
////        markerOptions.title("현재위치");
////        // markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));
////        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker)));
////        markerOptions.flat(true);
////        mMap.addMarker(markerOptions);
//        for(int i = 0 ; i < a.size();i++){
//            GroundOverlayOptions newarkMap = new GroundOverlayOptions()
//                    .image(BitmapDescriptorFactory.fromResource(R.drawable.onebin))
//                    .position(a.get(i), 86f, 65f);
//            mMap.addGroundOverlay(newarkMap);
//        }
//        mSydney = mMap.addMarker(new MarkerOptions()
//                .position(SYDNEY)
//                .title("Sydney");
//        mSydney.setTag(0);
//
//    }
@Override
public void onLocationChanged(Location location) {

}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
