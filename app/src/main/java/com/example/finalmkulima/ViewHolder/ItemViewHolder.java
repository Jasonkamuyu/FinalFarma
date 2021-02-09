package com.example.finalmkulima.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalmkulima.Interface.ItemClickListener;
import com.example.finalmkulima.R;

public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtProductName, txtProductDescription, txtProductPrice,textProductStatus;
    public ImageView imageView;
    public ItemClickListener listner;


    public ItemViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.product_farmer_image);
        txtProductName = (TextView) itemView.findViewById(R.id.product_farmer_name);
        txtProductDescription = (TextView) itemView.findViewById(R.id.product_farmer_description);
        txtProductPrice = (TextView) itemView.findViewById(R.id.product_farmer_price);
        textProductStatus = (TextView) itemView.findViewById(R.id.product_farmer_state);
    }

    public void setItemClickListner(ItemClickListener listner)
    {
        this.listner = listner;
    }

    @Override
    public void onClick(View v) {

        listner.onClick(v, getAdapterPosition(), false);

    }
}

