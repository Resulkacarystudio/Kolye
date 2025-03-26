package com.example.greenlove;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

    private List<String> imageList;  // String türüne dönüştürülmeli
    private Context context;

    public ImagePagerAdapter(Context context, List<String> imageList) {  // String olarak güncellendi
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pager_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageList.get(position);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("ImagePagerAdapter", "Loading image: " + imageUrl);
            Glide.with(context)
                    .load(imageUrl)
                    .fitCenter()  // Görüntüyü merkezde ve orantılı sığacak şekilde gösterir
                    .into(holder.imageView);
        } else {
            Log.e("ImagePagerAdapter", "Image URL is null or empty at position: " + position);
            holder.imageView.setImageResource(R.drawable.kadi_asset); // Placeholder resim
        }
    }



    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
