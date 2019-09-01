package com.bug32.librivoxaudiobooks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class productAdapter extends RecyclerView.Adapter<productAdapter.productViewHolder> {

    private Context mContext;
    private ArrayList<Product> mproductItems;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public productAdapter (Context context , ArrayList<Product> productItems){
        mContext = context;
        mproductItems = productItems;
    }


    @NonNull
    @Override
    public productViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.product, parent, false);
        return new productViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull productViewHolder holder, int position) {

        Product currentItem = mproductItems.get(position);

        String title = currentItem.getMtitle();
        String imgUrl = currentItem.getmImgUrl();
        String author = currentItem.getmAuthor();

        holder.titleText.setText(title);
        holder.authName.setText(author);
        Picasso.get().load(imgUrl).fit().into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mproductItems.size();
    }

    public class productViewHolder extends RecyclerView.ViewHolder {

        public TextView titleText;
        public ImageView imageView;
        public TextView authName;

        public productViewHolder(View itemView) {
            super(itemView);

            titleText = itemView.findViewById(R.id.titleText);
            imageView = itemView.findViewById(R.id.imageView);
            authName = itemView.findViewById(R.id.authorName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public void setFilter(ArrayList<Product> newList){

        mproductItems = new ArrayList<>();
        mproductItems.addAll(newList);
        notifyDataSetChanged();
    }
}
