package com.example.camera_exam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private  List<MainData> list_data;
    private  MainAdapter mainAdapter;
    private Button button;
    private GridLayoutManager gridLayoutManager;
    private int last_num = 0;
    private Map<String,Object> map ;
    private long backBtnTime = 0;
    private CheckBox checkBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //툴바생성
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        mainAdapter.notifyDataSetChanged();
        
        //String ==> json ==> Map으로 변환
        SharedPreferences sharedPreferences = getSharedPreferences("folderName",0);
        String jMap = sharedPreferences.getString("jMap","");
        Log.e("jMap",jMap);
        try {
            if(!jMap.equals("")){
                JSONObject jsonObject = new JSONObject(jMap);
                ObjectMapper objectMapper = new ObjectMapper();
                map = objectMapper.readValue(jMap,Map.class);
                Log.e("map",map.toString());
                Iterator<String>iterator = map.keySet().iterator();
                while (iterator.hasNext()){
                    Object key = iterator.next();
                    String value = map.get(key).toString();
                    MainData mainData = new MainData(R.mipmap.ic_launcher,value);
                    list_data.add(mainData);
                    mainAdapter.notifyDataSetChanged();

                }
            }else{
                map = new HashMap<>();
                Log.e("map","비어있음");
            }
        } catch (JSONException e) {
            Log.e("String==>Json",e.toString());
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        button = (Button) findViewById(R.id.btn_add);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
            return true;
        }

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
                    MainData mainData = new MainData(R.mipmap.ic_launcher, result);
                    map.put(result,result);
                    list_data.add(mainData);
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
            //체크박스
            checkBox = (CheckBox)findViewById(R.id.cb_delete);
            Toast.makeText(getApplicationContext(),"삭제",Toast.LENGTH_SHORT);
            checkBox.setVisibility(View.VISIBLE);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //두번누르면 종료
    @Override
    public void onBackPressed() {

        long currentTime = System.currentTimeMillis();
        long gapTime = currentTime - backBtnTime;

        if(0 <= gapTime && 2000>= gapTime ){
            super.onBackPressed();
        }else{
            backBtnTime = currentTime;
            Toast.makeText(getApplicationContext(),"한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        JSONObject jsonObject = new JSONObject(map);
        SharedPreferences sharedPreferences = getSharedPreferences("folderName",0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("jMap",jsonObject.toString());
        editor.commit();
        Log.e("jMap",sharedPreferences.getString("jMap",""));
    }
}