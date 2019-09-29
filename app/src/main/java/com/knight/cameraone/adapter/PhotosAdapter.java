package com.knight.cameraone.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.knight.cameraone.R;

import java.util.List;

/**
 * @author created by luguian
 * @organize
 * @Date 2019/9/29 17:13
 * @descript:照片
 */

public class PhotosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<String> photoLists;


    public PhotosAdapter(List<String> photoLists){
        this.photoLists = photoLists;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //设置Image图片
        Glide.with(holder.itemView.getContext()).load(photoLists.get(position))
                .apply(RequestOptions.bitmapTransform(new CircleCrop())
                        .override(((ViewHolder)holder).mImageView.getWidth(), ((ViewHolder)holder).mImageView.getHeight())
                        .error(R.drawable.default_person_icon))
                .into(((ViewHolder)holder).mImageView);
    }

    @Override
    public int getItemCount() {
        return photoLists.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.iv_photo);
        }
    }
}
