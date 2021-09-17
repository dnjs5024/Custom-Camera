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
    private  Menu mMenu;
    public static  Context mContext;
    private String albumName;
    private boolean isDestroy = false;
    private String imageName; // 카메라로 찍은 사진 이름
    private Uri imageUri; // 카메라로 찍은 사진 Uri
    private ImageView imageView;
    private Uri locationUri; //갤러리에서 가져온 사진 Uri



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


        //권한 체크

        String[] permissionChecked = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        int cameraPermission = ContextCompat.checkSelfPermission(MainActivity.this, permissionChecked[0]);
        int readPermission = ContextCompat.checkSelfPermission(MainActivity.this, permissionChecked[1]);

        if(cameraPermission== PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(),"환영합니다",Toast.LENGTH_SHORT).show();

        }else{
            //둘다 권한 허용이 안된경우
            if(cameraPermission == readPermission){
                Toast.makeText(getApplicationContext(),"카메라를 사용하기 위해서는 권환이 필요합니다.",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,permissionChecked,REQUEST_PERMISSION );
            }else {
                if (cameraPermission == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        //한번 거부한적 있는경우
                        Toast.makeText(getApplicationContext(), "사진촬영을 위해서는 권환이 필요합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        //처음 권한 체크
                        Toast.makeText(getApplicationContext(), "카메라 처음", Toast.LENGTH_SHORT).show();
                    }
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION);
                }
                if (readPermission == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Toast.makeText(getApplicationContext(), "폴더를 생성하기 위해서는 권환이 필요합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "파일 처음", Toast.LENGTH_SHORT).show();
                    }
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                }
            }
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
                        setDirEmpty(dirName);
                        list_data.remove(num);
                        mainAdapter.notifyDataSetChanged();

                    }

                }
                    checkedCnt = 0;
                    toolbar.setTitle("저장위치 선택");
                    bottomNavigationView.setVisibility(View.INVISIBLE);
                    showOptionMenu( true);
                    ifVisible = false;

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
        MainData firstData = new MainData(R.mipmap.ic_launcher,"기본경로");
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
                    String value = map.get(key).toString();
                    MainData mainData = new MainData(R.mipmap.ic_launcher,value);
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
            ad.setMessage("사진을 저장할 경로를 적어주세요");
            EditText editText = new EditText(this);
            ad.setView(editText);
            ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String result = editText.getText().toString();
                    String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + result + File.separator;
                    Log.e("location==>",strFolderName);
                    File file = new File(strFolderName);
                    if( !file.exists() ) {
                        Log.e("File==>", "파일 생성");
                        file.mkdirs();
                    }else{
                        Log.e("File==>", "이미 존재함");

                    }

                    MainData mainData = new MainData(R.mipmap.ic_launcher, result);
                    list_data.add(mainData);
                    Log.e("dialog data",list_data.toString());
                    mainAdapter.notifyDataSetChanged();
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
        }

        return super.onOptionsItemSelected(item);
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
                Toast.makeText(getApplicationContext(), "권한 허용 ", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "권한 거부 ", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setIcon(R.mipmap.ic_launcher_round);
                ad.setTitle("안내");
                ad.setMessage("권한을 허용하실려면 설정->개인정보 보호->권한에서 허용해주세요");
                ad.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                ad.setNegativeButton("설정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                        dialogInterface.dismiss();
                    }
                });
                ad.show();
            }

        }

    }

    //dir 하위 파일,폴더까지 삭제하는 코드
    public void childFileDelete(String dirName){

        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI ,
                MediaStore.Images.Media.RELATIVE_PATH +"='Pictures/"+dirName+"/'",null);

    }
    public void setDirEmpty(String dirName){
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + dirName + File.separator;
        path.trim();
        Log.e("path",path);
        File file = new File(path);
        childFileDelete(dirName);
        file.delete();

    }
    //카메라 실행
    public void onCamera(String dirName){
        isDestroy = true;
        albumName = dirName;
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        imageName = "SAMPLE_"+timeStamp+".jpg";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = createImageUri(dirName,imageName, "image/jpeg");
//        intent.setClipData(ClipData.newRawUri("",imageUri));
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activityResultLauncher.launch(intent);
    }
    private Uri createImageUri(String dirName,String fileName, String mimeType) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName); // 확장자가 붙어있는 파일명 ex) sample.jpg
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType); // ex) image/jpeg
        if(!dirName.equals("기본경로")){
            values.put( MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + dirName );
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
                        }else{

                            Log.e("result","오류");
                            getContentResolver().delete(imageUri, null, null);
                        }


                }
            });
    //갤러리에서 사진을 가져오는 코드
    public Uri getImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncherAlbum.launch(intent);

        return locationUri;
    }
    public Uri returnUri(Uri uri){
        return uri;
    }

    ActivityResultLauncher<Intent> activityResultLauncherAlbum = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(RESULT_OK==result.getResultCode()){
                Intent intent = result.getData();
                Log.e("2","2");
                locationUri = intent.getData();
                returnUri(locationUri);

            }else{
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
        Log.e("last data size", String.valueOf(list_data.size()));
        map.clear();
        if (list_data.size()>0) {
            for (int i = 1; i < list_data.size(); i++) {
                map.put("list_data" + i, list_data.get(i).getTv_title());
            }
        }
        Log.e("last data",list_data.toString());
        JSONObject jsonObject = new JSONObject(map);
        SharedPreferences sharedPreferences = getSharedPreferences("folderName",0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("jMap",jsonObject.toString());
        editor.commit();
        Log.e("jMap",sharedPreferences.getString("jMap",""));
    }




}