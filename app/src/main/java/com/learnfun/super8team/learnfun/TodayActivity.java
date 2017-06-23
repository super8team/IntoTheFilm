package com.learnfun.super8team.learnfun;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

import static android.R.attr.bitmap;

public class TodayActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private StudentHelper helper;
    private static int MY_LOCATION_REQUEST_CODE = 2000;
    public static final int REQUEST_CODE_WRITE = 1001;
    private LocationManager locationManager;
    View marker_root_view;
    MarkerOptions myMarker=null;
    private Socket socket=null;
    boolean isPageOpen = false;
    String placeNum="";
    Animation translateLeftAnim;
    Animation translateRightAnim;

    LinearLayout slidingPage01;
    ScrollView scrollPage;

    LatLng SEOUL = new LatLng(35.896687, 128.620512);

    JSONObject sendData,planGPS;
    UserPreferences userPreferences;
    NetworkAsync requestNetwork;
    //ArrayList<LatLng> classOne = new ArrayList();
    ArrayList<Marker> classOneMarker = new ArrayList();
    //ArrayList<LatLng> classTwo = new ArrayList();
    ArrayList<Marker> classTwoMarker = new ArrayList();
    //ArrayList<LatLng> classThree = new ArrayList();
    ArrayList<Marker> classThreeMarker = new ArrayList();

    private Button slidingPageClose,writeHistory,logBtn;
    ArrayAdapter<String> adapter;
    Spinner spinner;
    Switch histroySwitch;
    boolean mSwc = true;    // 스위치 상태를 기억할 변수
    boolean emitSwitch = false;
    TextView contentView;
    String logContent = "";
    ImageView historyImage;

    //히스토리에 이미지를 뿌려주기 위한 변수들
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<MyData> myDataset;
    public static final String baseShoppingURL = "http://163.44.166.91/LEARnFUN/storage/app/historyImgs/1-1.png";
    Bitmap bitmap;
    public ImageView mImageView;
    public TextView mTextView;

    //dialog
    AppCompatDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("tag", "온크리에이트 실행");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        helper = new StudentHelper(this,"NameDB",null,7);
        slidingPageClose = (Button)findViewById(R.id.slidingPageClose);
        writeHistory = (Button)findViewById(R.id.writeHistory);
        logBtn = (Button)findViewById(R.id.logBtn);
        logBtn.setOnClickListener(logListener);

        slidingPage01 = (LinearLayout) findViewById(R.id.slidingPage01);
        scrollPage = (ScrollView) findViewById(R.id.scrollPage);

        //historyImage =(ImageView)this.findViewById(R.id.pictureImage);


        contentView = (TextView)this.findViewById(R.id.contentView);


        translateLeftAnim = AnimationUtils.loadAnimation(this,R.anim.translate_left);
        translateRightAnim = AnimationUtils.loadAnimation(this,R.anim.translate_right);

        SlidingPageAnimationListener animListener = new SlidingPageAnimationListener();
        translateLeftAnim.setAnimationListener(animListener);
        translateRightAnim.setAnimationListener(animListener);

        spinner = (Spinner)findViewById(R.id.spinner);

        histroySwitch = (Switch)findViewById(R.id.historySwitch);
        histroySwitch.setOnCheckedChangeListener(SWITCH);




        userPreferences = UserPreferences.getUserPreferences(this);

        try{

            socket = IO.socket("http://163.44.166.91:8000");
            //socket.on(Socket.EVENT_CONNECT, listenStartPerson);
            //.on("getclass1", listen_start_person)
        } catch(Exception e){
            Log.i("ERROR", "ERROR : Socket connection failed");
        }

        socket.connect();

        if(socket.connected()){
            Log.i("SocketCheck", "Socket connection successful");
        }
        else{
            Log.i("SocketCheck", "Socket connection failed");
        }

        socket.on("getclass",listenGetMessagePerson);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.class_Name,android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);



        chkGpsService(); //GPS sensor on / off check






        slidingPageClose.setOnClickListener(new View.OnClickListener() {//발자취창 눌렀을때 슬라이딩이벤트
            @Override
            public void onClick(View v) {
                slidingPage01.startAnimation(translateRightAnim);
                scrollPage.startAnimation(translateRightAnim);
            }
        });
        writeHistory.setOnClickListener(new View.OnClickListener() { //글쓰기 버튼 클릭시
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteHistoryActivity.class);
                intent.putExtra("placeNum", placeNum);
                startActivityForResult(intent,REQUEST_CODE_WRITE);
            }
        });
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){


                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                   // Toast.makeText(parent.getContext(),"선택한것은"+parent.getItemAtPosition(position),Toast.LENGTH_SHORT).show();
                    mMap.clear();

                    if(parent.getItemAtPosition(position).equals("1반")){
                        Log.e("planResult", "1반 받았나!@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                        classOneMarker.clear();
                        if(emitSwitch) socket.emit("class1", "I want class1's GPS");
                        //addStudentMarker(classOne, "#21ff3f", "1반");
                        if(myMarker!=null) mMap.addMarker(myMarker);

                    }else if(parent.getItemAtPosition(position).equals("2반")){
                        classTwoMarker.clear();
                        socket.emit("class2", "I want class2's GPS");
                        //addStudentMarker(classTwo, "#24d8b4","2반");
                        if(myMarker!=null) mMap.addMarker(myMarker);

                    }else{
                        classThreeMarker.clear();
                        socket.emit("class3", "I want class3's GPS");
                        //addStudentMarker(classThree,"#ebf224","3반");
                        if(myMarker!=null) mMap.addMarker(myMarker);

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
    }//oncreate function end

    //로그를 보여주기 위한 dialog
    private View.OnClickListener logListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TodayActivity.this); // 빌더 얻기

            // 제목 설정
            alertDialogBuilder.setTitle("로그");

            // 다이얼로그 메세지 생성 setMessage에 서버에서 로그 기록을 가지고 와서 뿌려줘야함
            alertDialogBuilder
                    .setMessage("11:30 - 영진전문대학에 진입했습니다.")
                    .setCancelable(false)
                    .setNegativeButton("취소", //Negative 버튼 기능 작성
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel(); // 다이얼로그 취소
                                }
                            });

            // 다이럴로그 객체 얻어오기
            AlertDialog alertDialog = alertDialogBuilder.create();

            // 다이얼로그 보여주기
            alertDialog.show();

        }
    };
    //GPS 설정 체크
    private boolean chkGpsService() {

        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        Log.d(gps, "aaaa");

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
            return false;

        } else {
            return true;
        }
    }
    //스위치 눌렀을때 작동 on/off
    public Switch.OnCheckedChangeListener SWITCH = new Switch.OnCheckedChangeListener()

    {

        public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {

            mSwc = isChecked;
            if(isChecked == true){ //처음 화면은 학생들의 위치를 나타내주는 화면
                histroySwitch.setText("발자취");
                mMap.clear();
                socket.emit("history", "don't send any gps");
                spinner.setVisibility(View.INVISIBLE); // 화면에 안보임
                classOneMarker.clear();
                classTwoMarker.clear();
                classThreeMarker.clear();
                if(myMarker!=null) mMap.addMarker(myMarker);
                getPlanGPS();


                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() { //발자취 눌렀을때 동작함수
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if(!marker.getTitle().equals("현재위치")) {
                            placeNum = marker.getTitle();
                            if (isPageOpen) {
                                //slidingPage01.startAnimation(translateRightAnim);
                            } else {
                                startProgress();//다이얼로그 실행 함수
                                slidingPage01.setVisibility(View.VISIBLE);
                                slidingPage01.startAnimation(translateLeftAnim);
                                scrollPage.setVisibility(View.VISIBLE);
                                scrollPage.startAnimation(translateLeftAnim);


                                //디비에서 히스토리 정보를 가져와서 슬라이드창에 글을 뿌려준다
                                sendData = new JSONObject();

                                try {
                                    //recentDate.put("date",getDate());
                                    sendData.put("userId",userPreferences.getUserId());
                                    sendData.put("placeNum",placeNum);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                requestNetwork = new NetworkAsync(TodayActivity.this, "getHistoryContent",  NetworkAsync.POST, sendData);

                                try {
                                    // 네트워크 통신 후 json 획득
                                    String returnString = requestNetwork.execute().get();
                                    Log.e("planResult", "result is "+returnString);
                                    JSONObject place = new JSONObject(returnString);

                                    //JSONArray planGPSArray = new JSONArray(planGPS.getString("gps"));
                                    JSONObject contentList = new JSONObject(place.getString("place"));
                                    String sumContent = "";

                                    //cardView를 만들기위한 코드
                                    mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
                                    // use this setting to improve performance if you know that changes
                                    // in content do not change the layout size of the RecyclerView
                                    mRecyclerView.setHasFixedSize(true);

                                    // use a linear layout manager
                                    mLayoutManager = new LinearLayoutManager(TodayActivity.this);
                                    mRecyclerView.setLayoutManager(mLayoutManager);

                                    // specify an adapter (see also next example)
                                    myDataset = new ArrayList<>();
                                    mAdapter = new MyAdapter(myDataset);
                                    mRecyclerView.setAdapter(mAdapter);

                                    for(int i = 0 ; i < contentList.length();i++){

                                        //제이슨배열을 만든것을 하나씩 제이슨 객체로 만듬
                                        String contentNum = "content" + (i+1);
                                        JSONObject dataJsonObject = contentList.getJSONObject(contentNum);
                                        //JSONObject placeData = new JSONObject(dataJsonObject);sumContent

                                        //sumContent += "content : " + dataJsonObject.getString("content") + "\nweather : " +dataJsonObject.getString("weather")+ "\n";
                                        //contentView.setText("content : " + dataJsonObject.getString("content") + "\nweather : " +dataJsonObject.getString("weather"));
                                        //getImageFromURL(dataJsonObject.getString("url")),dataJsonObject.getString("content")

//                                        myDataset.add(new MyData(dataJsonObject.getString("content"),getImageFromURL("http://163.44.166.91/LEARnFUN/storage/app/historyImgs/1-1.png")));
                                        String imageUrl="";
                                        imageUrl = dataJsonObject.getString("url");

                                        final String finalImageUrl = imageUrl;
                                        Thread mThread = new Thread(){
                                            @Override
                                            public void run() {
                                                try{//baseShoppingURL

                                                    URL url = new URL(finalImageUrl); //URL주소를 이용해서 URL객체를 생성
                                                    //아래 코드는 웹에서 이미지를 가져온뒤
                                                    //이미지 뷰에 지정할 Bitmap을 생성하는 과정
                                                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                                                    conn.setDoInput(true);
                                                    conn.connect();

                                                    InputStream is = conn.getInputStream();
                                                    bitmap = BitmapFactory.decodeStream(is);

                                                }catch (IOException ex){

                                                }
                                            }
                                        };

                                        mThread.start();

                                        try{
                                            mThread.join();

                                            myDataset.add(new MyData(dataJsonObject.getString("content"),bitmap));


                                        }catch (InterruptedException e){

                                        }


                                    }
                                    //historyImage.setImageBitmap(bitmap);
                                    //contentView.setText(sumContent);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                        }
                        return false;
                    }
                });


            } else{// 히스토리를 나타내는 화면
                histroySwitch.setText("학생위치");
                mMap.clear();
                spinner.setVisibility(View.VISIBLE); // 화면에보임
                socket.emit("class1", "I want to class1's GPS");
                //addStudentMarker(classOne, "#21ff3f", "1반");
                if(myMarker!=null) mMap.addMarker(myMarker);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        return false;
                    }
                });
            }


        }

    };



    @Override
    protected void onDestroy() {
//        socket.emit("disconnection");
        socket.on(Socket.EVENT_DISCONNECT, listenDisconnectPerson);
        super.onDestroy();
    }

    private Emitter.Listener listenStartPerson = new Emitter.Listener() {

        public void call(Object... args) {

            //socket.emit("Connection", "connected on server");
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
            Log.d("loglog", "class 받았다@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            final JSONObject obj = (JSONObject)args[0];

            //서버에서 보낸 JSON객체를 사용할 수 있습니다.

            runOnUiThread(new Runnable() { //발생가능 문제점 : 1반이 선택된 상태에서 1반에 대한 객체가 들어올경우 마커가 중첩되어 찍힐수있음
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


    public void getPlanGPS(){ // 히스토리부분

        //String userid = userPreferences.getUserId();

        sendData = new JSONObject();

        try {

            //recentDate.put("date",getDate());
            sendData.put("userID","Illum");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestNetwork = new NetworkAsync(this, "getPlan",  NetworkAsync.POST, sendData);

        try {
            // 네트워크 통신 후 json 획득
            String returnString = requestNetwork.execute().get();
            Log.e("planResult", "result is "+returnString);
            planGPS = new JSONObject(returnString);

            //JSONArray planGPSArray = new JSONArray(planGPS.getString("gps"));
            JSONObject placeGPS = new JSONObject(planGPS.getString("gps"));
            PolygonOptions rectOptions = new PolygonOptions();
            Log.e("planResult", "result is "+ placeGPS);

            for(int i = 0 ; i < placeGPS.length();i++){

                //제이슨배열을 만든것을 하나씩 제이슨 객체로 만듬
                String placeNum = "place" + (i+1);
                JSONObject dataJsonObject = placeGPS.getJSONObject(placeNum);
                //JSONObject placeData = new JSONObject(dataJsonObject);
                Log.d("TAG", dataJsonObject.getString("name"));
                LatLng planGPS = new LatLng(dataJsonObject.getDouble("lat"), dataJsonObject.getDouble("lng"));

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(planGPS);
                //markerOptions.icon(R.drawable.placeMarker); // change the color of marker
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker));
                markerOptions.title(dataJsonObject.getString("no"));

                Marker planMarker = mMap.addMarker(markerOptions);

                //planMarker.setOnClickListener(onButton1Clicked); //마커에 클릭이벤트를 달아 오른쪽에 창이 나오도록 해야함
                rectOptions.add(planGPS);

            }

            // Get back the mutable Polygon
            Polygon polygon = mMap.addPolygon(rectOptions);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }



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
                socket.emit("class1", "I want class1's GPS");
                emitSwitch = true;
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
//        marker_root_view = LayoutInflater.from(this).inflate(R.layout.activity_today, null);
//        //tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);
//        }


    private android.location.LocationListener locationListener = new android.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            //setCustomMarkerView();
            Log.d("TAG", "onLocationChanged에 들어왔다");

            LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            //Toast.makeText(TodayActivity.this, (int) location.getLatitude()+" 좌표변경",Toast.LENGTH_SHORT).show();
            if(myMarker==null){
                addMyMarker(myLatLng);

            }
            else{
                myMarker.position(myLatLng);

            }

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

    // View를 Bitmap으로 변환
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
        myMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.mymarkerd)); // change the color of marker(red)
        myMarker.title("현재위치");
        mMap.addMarker(myMarker);

        //지도 상에서 보여주는 영역 이동
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //mGoogleApiClient.connect();
    }

    private void addStudentMarker(JSONArray arrayList,String markerColor,String className) throws JSONException {
        Log.d("TAG", "학생마커 생성");
        ArrayList<Student> student = helper.selectAll();

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
            for(int j =0; j < student.size();j++){
                if(dataJsonObject.getString("name").equals(student.get(j).name)){
                    markerOptions.icon(getMarkerIcon(student.get(j).color)); // change the color of marker
                    Log.d("dbColor=", student.get(j).color);
                    Log.d("TAG", "아이콘색변경했다");
                }
            }
            //markerOptions.icon(getMarkerIcon("red")); // change the color of marker
            markerOptions.title(dataJsonObject.getString("name"));
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

        //#ebf224 노랑   #0732f2 파랑  #ef1c09 빨강
        switch (color){
            case "red" :
                color = "#ef1c09";
                break;
            case "yellow" :
                color = "#ebf224";
                break;
            case "blue" :
                color = "#0732f2";
                break;
        }
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
////        mSydney = mMap.addMarker(new MarkerOptions()
////                .position(SYDNEY)
////                .title("Sydney");
////        mSydney.setTag(0);
//
//    }








    @Override
    public boolean onMarkerClick(Marker marker) {
        //Toast.makeText(this,"마커가 선택 되었습니다.",Toast.LENGTH_SHORT).show();
        return false;
    }

    private class SlidingPageAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if(isPageOpen){
                slidingPage01.setVisibility(View.GONE);
                scrollPage.setVisibility(View.GONE);
                isPageOpen = false;

            }else{

                isPageOpen=true;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //색상을 클릭하고 다시 넘어올경우 색상을 저장해야함
        if(requestCode == REQUEST_CODE_WRITE){
            if(resultCode == RESULT_OK){
                String name = intent.getExtras().getString("name");
//                Toast toast = Toast.makeText(getBaseContext(),
//                        "응답으로 전달된 name: )" + name, Toast.LENGTH_LONG);
//                toast.show();

            }
        }
        slidingPage01.setVisibility(View.GONE);
        scrollPage.setVisibility(View.GONE);
        isPageOpen = false;
    }

    public void progressON(Activity activity, String message) {

        if (activity == null || activity.isFinishing()) {
            return;
        }


        if (progressDialog != null && progressDialog.isShowing()) {
            progressSET(message);
        } else {

            progressDialog = new AppCompatDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.progress_loading);
            progressDialog.show();

        }


        final ImageView img_loading_frame = (ImageView) progressDialog.findViewById(R.id.iv_frame_loading);
        final AnimationDrawable frameAnimation = (AnimationDrawable) img_loading_frame.getBackground();
        img_loading_frame.post(new Runnable() {
            @Override
            public void run() {
                frameAnimation.start();
            }
        });

        TextView tv_progress_message = (TextView) progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }


    }
    public void progressSET(String message) {

        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }


        TextView tv_progress_message = (TextView) progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }

    }

    public void progressOFF() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    private void startProgress() {

        progressON(TodayActivity.this,"Loading...");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressOFF();
            }
        }, 11500);

    }

}
