package com.hyeok.point1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/*** RecyclerView control 클래스  ***/
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.RVViewHolderClass> {

    public ArrayList<ModelClass> objectModelClassList;


    private OnItemClickListener mListener = null;

    private int data;

    public RVAdapter(ArrayList<ModelClass> objectModelClassList) {
        this.objectModelClassList = objectModelClassList;
        System.out.println("new RVAdapter created !!!");
    }

    public interface OnItemClickListener{
        void onItemClick(View v, int position);
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }

    @NonNull
    @Override
    public RVViewHolderClass onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new RVViewHolderClass(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.single_row,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RVViewHolderClass rvViewHolderClass, int i) {
        ModelClass objectModelClass = objectModelClassList.get(i);
        rvViewHolderClass.imageNameTV.setText(objectModelClass.getImageName());

        rvViewHolderClass.objectImageView.setImageBitmap(objectModelClass.getImage());
    }

    @Override
    public int getItemCount() {
        if (objectModelClassList != null) {
            System.out.println("getItemCount:"+objectModelClassList.size());
            return objectModelClassList.size();
        }
        else{
            return 0;
        }
    }

    public void setPosition(int pos){  data = pos;  }

    public int  getPosition(){
        return data;
    }

    public class RVViewHolderClass extends RecyclerView.ViewHolder {

        TextView imageNameTV;
        ImageView objectImageView;

        public RVViewHolderClass(@NonNull View itemView) {
            super(itemView);
            imageNameTV = itemView.findViewById(R.id.sr_imageDetailsTV);
            objectImageView = itemView.findViewById(R.id.sr_imageIV);
            // RecycleView 의 해당 item 클릭시, item 의 position을 반환 합니다.
            imageNameTV.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        System.out.println("position : "+position);
                        setPosition(position);
                        if(mListener != null){
                            mListener.onItemClick(v,position);
                        }
                    }
                }
            });
        }
    }
}