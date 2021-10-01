package com.example.camera_exam;

import static android.os.Environment.DIRECTORY_PICTURES;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 10;
    private static final int REQUEST_PERMISSION2 = 122;
    private RecyclerView recyclerView;
    private  List<MainData> list_data;
    private  MainAdapter mainAdapter;
    private GridLayoutManager gridLayoutManager;
    private Map<String,Object> map ;
    private long backBtnTime = 0;
    private CheckBox checkBox ;
    private BottomNavigationView bottomNavigationView;
    private boolean ifVisible = false;
    private  Toolbar toolbar;
    private int checkedCnt = 0;
    public  boolean doTouch = false;//수정 삭제시 아이템 클릭시 카메라 안나오게
    private  Menu mMenu;
    public static  Context mContext;
    private String albumName = "";
    private boolean isDestroy = false;
    private boolean isRun = true;
    private String imageName = ""; // 카메라로 찍은 사진 이름
    private Uri imageUri; // 카메라로 찍은 사진 Uri
    private ImageView imageView;
    private Uri locationUri; //갤러리에서 가져온 사진 Uri
    private  int position = 0; //사진 수정했을때 몇번째 데이터인지 알기위해
    private  int fixNum = 0; //사진 수정했을때 몇번째 데이터인지 알기위해



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //앱 강제종료
        startService(new Intent(this, ForecdTerminationService.class));

        setContentView(R.layout.activity_main);
        //툴바생성
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mContext = this;

        if(Build.VERSION.SDK_INT < 23){
            return;
        }else{
                requestUserPermission();
        }

        //바텀내비 생성 //저장공간 삭제
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                List<Integer> list = new ArrayList<>();
                boolean delete = false;

                if(R.id.action_delete==item.getItemId()){
                    int cnt = 10000;
                    int noCheck = 0;
                    for(int i = 1 ; i < list_data.size() ; i++){
                            checkBox  = (CheckBox)findViewById(cnt + i);

                            if(checkBox.isChecked()){
                                int deleteNum = checkBox.getId() - cnt;
                                Log.e("deleteNum", String.valueOf(deleteNum));
                                list.add(deleteNum);
                                Log.e("list", list.toString());
                                checkBox.setChecked(false);
                                delete =true;
                            }else{
                                noCheck++;

                            }
                    }
                    if(noCheck==list_data.size()-1){
                        Toast.makeText(getApplicationContext(),"삭제할 항목을 선택해주세요",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if(delete){
                    int num = 0;
                    for (int i = list.size()-1 ; 0 <= i ; i--){
                        num = list.get(i);
                        Log.e("num", String.valueOf(num));
                        String dirName = list_data.get(num).getTv_title();
                        childFileDelete(dirName);
                        list_data.remove(num);
                        mainAdapter.notifyDataSetChanged();

                    }

                }
                    checkedCnt = 0;
                    toolbar.setTitle("저장위치 선택");
                    bottomNavigationView.setVisibility(View.INVISIBLE);
                    showOptionMenu( true);
                    ifVisible = false;
                    doTouch = false;

                }
                return false;
            }
        });


        recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        gridLayoutManager = new GridLayoutManager(this,5);
        recyclerView.setLayoutManager(gridLayoutManager);
        list_data = new ArrayList<MainData>();
        mainAdapter = new MainAdapter(list_data,getApplicationContext());
        recyclerView.setAdapter(mainAdapter);

        //기본 경로 만들기
        MainData firstData = new MainData(R.mipmap.ic_launcher,"기본경로",null);
        list_data.add(firstData);
        Log.e("firstData",list_data.toString());
        mainAdapter.notifyDataSetChanged();

        //String ==> json ==> Map으로 변환
        SharedPreferences sharedPreferences = getSharedPreferences("folderName",0);
        String jMap = sharedPreferences.getString("jMap","");
        Log.e("jMap",jMap);

        //앱이 처음 설치되고 실행됬을떄
        map = new HashMap<>();

        if(!jMap.equals("{}")) {
            try {
                JSONObject jsonObject = new JSONObject(jMap);
                ObjectMapper objectMapper = new ObjectMapper();
                map = objectMapper.readValue(jMap, Map.class);
                Iterator<String>iterator = map.keySet().iterator();
                while(iterator.hasNext()){
                    String key = iterator.next();
                    Map<String,Object> valueMap = (Map<String, Object>) map.get(key);
                    String title = valueMap.get("title").toString();
                    Uri uri = null;
                    if(valueMap.containsKey("uri")){
                        String strUri =  valueMap.get("uri").toString();
                        uri = Uri.parse(strUri);
                        Log.e("title",title);
                        Log.e("uri", String.valueOf(uri));
                    }

                    MainData mainData = new MainData(R.mipmap.ic_launcher,title,uri);
                    list_data.add(mainData);
                    mainAdapter.notifyDataSetChanged();
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

        }else{
            map = new HashMap<>();
            Log.e("map","비어있음");
        }



    }

    //권한 체크
    private void requestUserPermission() {
        String[] permissionChecked = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int cameraPermission = ContextCompat.checkSelfPermission(MainActivity.this, permissionChecked[0]);
        int readPermission = ContextCompat.checkSelfPermission(MainActivity.this, permissionChecked[1]);
        int writerPerMission = ContextCompat.checkSelfPermission(MainActivity.this,permissionChecked[2]);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
            if(cameraPermission== PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),"환영합니다",Toast.LENGTH_SHORT).show();

            }else{
                //둘다 권한 허용이 안된경우
                if(cameraPermission == readPermission){
                    Toast.makeText(getApplicationContext(),"카메라를 사용하기 위해서는 권환이 필요합니다.",Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_PERMISSION );
                }else {
                    if (cameraPermission == PackageManager.PERMISSION_DENIED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                            //한번 거부한적 있는경우
                            Toast.makeText(getApplicationContext(), "앱을 사용하기 위해서는 권환이 필요합니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            //처음 권한 체크
                            Toast.makeText(getApplicationContext(), "사진촬영을 위해서는 권환이 필요합니다.", Toast.LENGTH_SHORT).show();
                        }
                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION);
                    }
                    if (readPermission == PackageManager.PERMISSION_DENIED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            Toast.makeText(getApplicationContext(), "앱을 사용하기 위해서는 권환이 필요합니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "사진촬영을 위해서는 권환이 필요합니다.", Toast.LENGTH_SHORT).show();
                        }
                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                    }
                }
            }

        }else{
            if(cameraPermission== PackageManager.PERMISSION_GRANTED && writerPerMission == PackageManager.PERMISSION_GRANTED && readPermission== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),"환영합니다",Toast.LENGTH_SHORT).show();

            }else{
                int cnt = 0;
                String[] temp = new String[0];
                if (cameraPermission == PackageManager.PERMISSION_DENIED) {
                        cnt++;
                        temp = new String[cnt];
                        for( int i = 0 ; i < cnt ; i++ ){
                            temp[i] = permissionChecked[i];
                        }
                    }
                    if (readPermission == PackageManager.PERMISSION_DENIED) {
                        cnt++;
                        temp = new String[cnt];
                        for( int i = 0 ; i < cnt ; i++ ){
                            temp[i] = permissionChecked[i];
                        }
                    }
                    if (writerPerMission == PackageManager.PERMISSION_DENIED) {
                        cnt++;
                        temp = new String[cnt];
                        for( int i = 0 ; i < cnt ; i++ ){
                            temp[i] = permissionChecked[i];
                        }
                    }
                    Toast.makeText(getApplicationContext(), "사진촬영을 위해서는 권환이 필요합니다.", Toast.LENGTH_SHORT).show();

                ActivityCompat.requestPermissions(this,temp, REQUEST_PERMISSION2);

            }


        }



    }


    //툴바 메뉴 처음 클릭했을때만 실행
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mMenu = menu;
        getMenuInflater().inflate(R.menu.menu,menu);

        return true;
    }
    public void showOptionMenu(boolean isShow) {
        if(mMenu == null) {
            return;
        }
        mMenu.setGroupVisible(R.id.menu_group, isShow); }



    //툴바 아이템 선택 이벤트 구현
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();


        if(id == android.R.id.home){
            finish();
            return true;
        }
        //저장위치를 생성하는 코드
        if(id == R.id.menu_1){
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setIcon(R.mipmap.ic_launcher_round);
            ad.setTitle("앨범 생성");
            ad.setMessage("사진을 저장할 경로를 적어주세요.");
            EditText editText = new EditText(this);
            editText.setHint("이름은 여섯글자 이하로 작성해주세요.");
            ad.setView(editText);
            ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String result = editText.getText().toString();
                    if(result.trim().equals("")){
                        Toast.makeText(getApplicationContext(),"이름은 한글자 이상으로 작성해주세요",Toast.LENGTH_SHORT).show();
                    }else if(result.length()>6){
                        Toast.makeText(getApplicationContext(),"이름은 여섯글자이하로 해주세요ㅠ",Toast.LENGTH_SHORT).show();
                    }else {
                        MainData mainData = new MainData(R.mipmap.ic_launcher, result, null);
                        list_data.add(mainData);
                        mainAdapter.notifyDataSetChanged();
                    }
                    dialog.dismiss();
                }
            });
            ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();

                }
            });
            ad.show();
            return true;
        }else if(id == R.id.menu_2){
            if(list_data.size()==1){
                Toast.makeText(getApplicationContext(),"삭제할 데이터가 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                return true;
            }
            doTouch = true;
            toolbar.setTitle("0");
            int cnt = 10000;
            for(int i = 1 ;i < list_data.size();i++){
                checkBox  = (CheckBox)findViewById(cnt + i);
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            checkedCnt = 0;
                        for(int i = 1 ; i < list_data.size() ; i++) {
                            checkBox = (CheckBox) findViewById(cnt + i);
                            if(checkBox.isChecked()){
                                checkedCnt++;
                            }
                        }
                        String title = String.valueOf(checkedCnt);
                        toolbar.setTitle(title);
                    }
                });
            }
            showOptionMenu( false);
            bottomNavigationView.setVisibility(View.VISIBLE);
            ifVisible = true;
            Log.e("for문", String.valueOf(ifVisible));
            return true;
        }else if(id == R.id.menu_3){
            if(list_data.size()==1){
                Toast.makeText(getApplicationContext(),"수정할 데이터가 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                return true;
            }
            doTouch = true;
            toolbar.setTitle("수정할 항목을 선택해주세요");
            int cnt = 10000;
            for(int i = 1 ;i < list_data.size();i++){
                checkBox  = (CheckBox)findViewById(cnt + i);
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        for(int i = 1 ; i < list_data.size() ; i++) {
                            checkBox = (CheckBox) findViewById(cnt + i);
                            if(checkBox.isChecked()){
                                int num = checkBox.getId();
                                fixNum = num - 10000;
                                Log.e("position", String.valueOf(position));
                                checkBox.setChecked(false);
                            }
                        }
                        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                        ad.setIcon(R.mipmap.ic_launcher_round);
                        ad.setTitle("앨범이름 수정");
                        ad.setMessage("변경할 이름을 적어주세요");
                        EditText editText = new EditText(MainActivity.this);
                        editText.setHint("이름은 여섯글자 이하로 작성해주세요.");
                        ad.setView(editText);
                        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String result = editText.getText().toString();
                                if(result.trim().equals("")){
                                    Toast.makeText(getApplicationContext(),"변경할 이름을 한글자 이상으로 적어주세요",Toast.LENGTH_SHORT).show();
                                }else if(result.length()>6){
                                    Toast.makeText(getApplicationContext(),"수정할 이름은 여섯글자이하로 해주세요ㅠ",Toast.LENGTH_SHORT).show();
                                }else{
                                    MainData changeData = new MainData(R.mipmap.ic_launcher,result,list_data.get(fixNum).getIv_setProfile());
                                    list_data.set(fixNum,changeData);
                                    Log.e("changeData",list_data.toString());
                                    mainAdapter.notifyDataSetChanged();

                                }

                                hideCheckbox();
                            }
                        });
                        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                hideCheckbox();

                            }
                        });
                        ad.show();
                    }
                });
            }
            fixNum = 0;
            ifVisible = true;
            showOptionMenu( false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
//checkbox 안보이게
    public void hideCheckbox(){
            doTouch = false;
            fixNum = 0;
            int cnt = 10000;
            for(int i = 0 ;i < list_data.size();i++){
                checkBox  = (CheckBox)findViewById(cnt + i);
                checkBox.setVisibility(View.INVISIBLE);
                checkBox.setChecked(false);
            }
            bottomNavigationView.setVisibility(View.INVISIBLE);
            showOptionMenu( true);
            checkedCnt = 0;
            toolbar.setTitle("저장위치 선택");
    }
    //back버튼 눌렀을때 이벤트
    @Override
    public void onBackPressed() {
        if(list_data.size()==1){
            ifVisible = false;
            bottomNavigationView.setVisibility(View.INVISIBLE);
            toolbar.setTitle("저장위치 선택");
        }

        if(ifVisible){
            int cnt = 10000;
            for(int i = 0 ;i < list_data.size();i++){
                checkBox  = (CheckBox)findViewById(cnt + i);
                checkBox.setVisibility(View.INVISIBLE);
                checkBox.setChecked(false);
            }
            bottomNavigationView.setVisibility(View.INVISIBLE);
            showOptionMenu( true);
            checkedCnt = 0;
            toolbar.setTitle("저장위치 선택");
            ifVisible = false;
            doTouch = false;
        }

        //두번누르면 종료
        long currentTime = System.currentTimeMillis();
        long gapTime = currentTime - backBtnTime;

        if(0 <= gapTime && 2000>= gapTime ){
            super.onBackPressed();
        }else{
            backBtnTime = currentTime;
            Toast.makeText(getApplicationContext(),"한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
        }

    }
    //권한 체크 result값

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult( requestCode, permissions, grantResults);
        Log.e("onRequest","onRequestPermissionsResult");
        if (requestCode == REQUEST_PERMISSION) {
            int sum = 0;
            for(int result : grantResults){
                Log.e("Result", String.valueOf(result));
                 sum += result;
                Log.e("sum", String.valueOf(sum));
            }
            if (sum == 0) {//PackageManager.PERMISSION_GRANTED
                Toast.makeText(getApplicationContext(), "환영합니다.", Toast.LENGTH_SHORT).show();

            } else {
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setIcon(R.mipmap.ic_launcher_round);
                ad.setTitle("안내");
                ad.setMessage("권한을 허용하실려면 설정->개인정보 보호->권한에서 허용해주세요");
                ad.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                ad.setNegativeButton("설정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                        finish();
                    }
                });
                ad.show();
            }

        }
        if(requestCode == REQUEST_PERMISSION2){
            int sum = 0;
            for(int result : grantResults){
                Log.e("Result", String.valueOf(result));
                sum += result;
                Log.e("sum", String.valueOf(sum));
            }
            if (sum == 0) {//PackageManager.PERMISSION_GRANTED
                Toast.makeText(getApplicationContext(), "환영합니다.", Toast.LENGTH_SHORT).show();

            } else {
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setIcon(R.mipmap.ic_launcher_round);
                ad.setTitle("안내");
                ad.setMessage("권한을 허용하실려면 설정->개인정보 보호->권한에서 허용해주세요");
                ad.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                ad.setNegativeButton("설정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                        finish();
                    }
                });
                ad.show();
            }


        }

    }

    //dir 하위 파일,폴더까지 삭제하는 코드

    public void childFileDelete(String dirName){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
            getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI ,
                    MediaStore.Images.Media.RELATIVE_PATH +"='Pictures/"+dirName+"/'",null);

        }else{
            String str = "/storage/emulated/0/Pictures/"+dirName+File.separator;
            Cursor cursor =   getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,null,null,null);
                while (cursor.moveToNext()){
                    int titleNum = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    int dataNum = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    String title = cursor.getString(titleNum);
                    String data = cursor.getString(dataNum);
                    int cnt = dirName.length();
                    if(data.substring(0,30+cnt).equals(str)){
                        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI ,
                  MediaStore.Images.Media.DATA +"='/storage/emulated/0/Pictures/"+ dirName +"/"+title+"'",null);
                    }
                }
                File file = new File(str);
            file.delete();
        }


    }
    //카메라 실행
    public void onCamera(String dirName){
        isDestroy = true;
        albumName = dirName;
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        imageName = "STORAGE_CAMERA_"+timeStamp+".jpg";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = createImageUri(dirName,imageName, "image/jpeg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activityResultLauncher.launch(intent);
    }
    private Uri createImageUri(String dirName,String fileName, String mimeType) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName); // 확장자가 붙어있는 파일명 ex) sample.jpg
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType); // ex) image/jpeg

        //버전이 p이상 일경우
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            if(!dirName.equals("기본경로")){
                values.put( MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + dirName );
            }
        }else{
            if(!dirName.equals("기본경로")){
                String filePath = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + dirName;
                File file = new File(filePath);
                if(!file.exists()){
                    file.mkdir();
                }else{
                    Log.e("mkdir","이미 존재");
                }
                String imagePath = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + dirName + File.separator + fileName;
                values.put( MediaStore.Images.Media.DATA, imagePath );

            }
        }
        Log.e("values",String.valueOf(values));
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Log.e("uri",String.valueOf(uri));
        return uri;

    }
    ActivityResultLauncher<Intent> activityResultLauncher =  registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                         isDestroy = false;

                        if(result.getResultCode()==RESULT_OK){

                            onCamera(albumName);
                            Log.e("result","굳");
                        }else {

                            Log.e("result", "오류");
                            Log.e("result", String.valueOf(imageUri));
                            if (imageUri != null) {
                                getContentResolver().delete(imageUri, null, null);
                            }
                        }


                }
            });
    //갤러리에서 사진을 가져오는 코드
    public void getImage(int position){
        this.position = position;
        locationUri = null;
        Log.e("1","1");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncherAlbum.launch(intent);

    }

    ActivityResultLauncher<Intent> activityResultLauncherAlbum = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(RESULT_OK==result.getResultCode()){
                Intent intent = result.getData();
                Log.e("2","2");
                locationUri = intent.getData();
                Log.e("locationUri", String.valueOf(locationUri));
                MainData changeData = new MainData(R.mipmap.ic_launcher,list_data.get(position).getTv_title(),locationUri);
                list_data.set(position,changeData);
                Log.e("changeData",list_data.toString());
                mainAdapter.notifyDataSetChanged();
                locationUri = null;
                position = 0;
            }else{
                locationUri = null;
                Log.e("str","제대로 해라");
            }

        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isDestroy){
            getContentResolver().delete(imageUri, null, null);

        }
        map.clear();
        if (list_data.size()>0) {
            for (int i = 1; i < list_data.size(); i++) {
                //map.put("list_data" + i, list_data.get(i).getTv_title());
                Map<String,Object> rMap = new HashMap<>();
                rMap.put("title",list_data.get(i).getTv_title());
                if(list_data.get(i).getIv_setProfile() != null){
                    rMap.put("uri",list_data.get(i).getIv_setProfile().toString());
                }
                map.put("list_data" + i,rMap);
            }
        }
        JSONObject jsonObject = new JSONObject(map);
        SharedPreferences sharedPreferences = getSharedPreferences("folderName",0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("jMap",jsonObject.toString());
        editor.commit();
        Log.e("jMap",sharedPreferences.getString("jMap",""));
    }



}