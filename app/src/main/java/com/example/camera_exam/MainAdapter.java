package com.example.camera_exam;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.CustomViewHolder> {

    private List<MainData> arrayList;
    private Context context;
    private  int cb_id = 10000;

    public MainAdapter(List<MainData> arrayList,Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }



    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list,parent,false);
        CustomViewHolder customViewHolderlder = new CustomViewHolder(view);


        return customViewHolderlder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.checkBox.setId(10000+position);
        holder.checkBox.setVisibility(View.INVISIBLE);
        holder.textView.setText(arrayList.get(position).getTv_title());
        //사진 변경했을 경우
        if(arrayList.get(position).getIv_setProfile() == null){
            Glide.with(holder.imageView).load(arrayList.get(position).getIv_profile()).into(holder.imageView);
        }else{
            Glide.with(holder.imageView).load(arrayList.get(position).getIv_setProfile()).into(holder.imageView);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //MainActivity 메소드를 실행
                ((MainActivity)MainActivity.mContext).onCamera(arrayList.get(position).getTv_title());

            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(context.getApplicationContext(),"아파!",Toast.LENGTH_SHORT).show();
                //MainActivity 메소드를 실행
                ((MainActivity)MainActivity.mContext).getImage(position);

                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        Log.e("getItemCount", String.valueOf(arrayList.size()));
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{

        private TextView textView;
        private ImageView imageView;
        private CheckBox checkBox;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.tv_title);
            imageView = (ImageView) itemView.findViewById(R.id.iv_profile);
            checkBox = (CheckBox) itemView.findViewById(R.id.cb_delete);

        }
    }
    public class CamaraThread extends Thread{

        CamaraThread(){

        }

        @Override
        public void run() {
            while(true){
                Log.e("CameraThread","사진 가져오는 중");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
          //  Log.e("CameraThread","thread 종료");
        }
    }


}
