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
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //툴바생성
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //권한 체크

        int permissionChecked = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);

        if(permissionChecked == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(),"환영합니다",Toast.LENGTH_SHORT).show();

        }else{

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){
                //한번 거부한적 있는경우
                Toast.makeText(getApplicationContext(),"카메라를 사용하기 위해서는 권환이 필요합니다.",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION );
            }else{
                //처음 권한 체크
                Toast.makeText(getApplicationContext(),"처음",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION );
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
                        String result = list_data.get(num).getTv_title();
                        String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + result + File.separator;
                        File file = new File(strFolderName);
                        file.delete();
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
        mainAdapter = new MainAdapter(list_data,this);
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
        Log.e("onBackPressed", String.valueOf(map.size()));
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
            Toast.makeText(getApplicationContext(),"뒤로가기 버튼 : "+cnt,Toast.LENGTH_SHORT);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            Toast.makeText(getApplicationContext(), "ㅇ" + grantResults[0], Toast.LENGTH_SHORT).show();
            if (grantResults.length > 0 && grantResults[0] == 0) {//PackageManager.PERMISSION_GRANTED
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
    //카메라 리턴값 받는곳
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==RESULT_OK){
                        Intent intent = result.getData();
                        String str = intent.getStringExtra("text");
                        Toast.makeText(getApplicationContext(),"test",Toast.LENGTH_SHORT).show();
                    }
                }
            });



    @Override
    protected void onDestroy() {
        super.onDestroy();
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