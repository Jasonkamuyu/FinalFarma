package com.example.finalmkulima.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalmkulima.Interface.ItemClickListener;
import com.example.finalmkulima.R;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtProductName, txtProductDescription, txtProductPrice,txtBuyerLocation,txtFarmerName,txtFarmerPhone;
    public ImageView imageView;
    public ItemClickListener listner;


    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.product_image);
        txtProductName = (TextView) itemView.findViewById(R.id.product_name);
        txtProductDescription = (TextView) itemView.findViewById(R.id.product_description);
        txtProductPrice = (TextView) itemView.findViewById(R.id.product_price);
        txtBuyerLocation=(TextView) itemView.findViewById(R.id.Buyer_Location);
        txtFarmerName=(TextView) itemView.findViewById(R.id.farmer_product_name);
        txtFarmerPhone=(TextView) itemView.findViewById(R.id.farmer_product_phone);
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
