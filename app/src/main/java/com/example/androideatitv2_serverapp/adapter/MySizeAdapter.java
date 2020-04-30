package com.example.androideatitv2_serverapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.androideatitv2_serverapp.Model.EventBus.SelectSizeModel;
import com.example.androideatitv2_serverapp.Model.SizeModel;
import com.example.androideatitv2_serverapp.Model.EventBus.UpdateSizeModel;
import com.example.androideatitv2_serverapp.R;
import com.example.androideatitv2_serverapp.callback.IRecycelerViewClickListner;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MySizeAdapter extends RecyclerView.Adapter<MySizeAdapter.MyViewHolder> {

    Context context;
    List<SizeModel> sizeModelList;
    UpdateSizeModel updateSizeModel;
    int editPos ;

    public MySizeAdapter(Context context, List<SizeModel> sizeModelList) {
        this.context = context;
        this.sizeModelList = sizeModelList;
        editPos = -1;
        updateSizeModel = new UpdateSizeModel();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_size_addon_display , parent , false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.txt_name.setText(sizeModelList.get(position).getName());
            holder.txt_price.setText(String.valueOf(sizeModelList.get(position).getPrice()));

            //Event

        holder.img_delete.setOnClickListener(v -> {
            //Delete Item
            sizeModelList.remove(position);
            notifyItemRemoved(position);
            updateSizeModel.setSizeModelList(sizeModelList); // set for Event
            EventBus.getDefault().postSticky(updateSizeModel); // Send Event
        });
        // should handle the all item here to prevent Crash
        holder.setListener(new IRecycelerViewClickListner() {
            @Override
            public void onItemclickListner(View view, int pos) {
                editPos = position;
                EventBus.getDefault().postSticky(new SelectSizeModel(sizeModelList.get(pos)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return sizeModelList.size();
    }

    public void addNewSize(SizeModel sizeModel) {
        sizeModelList.add(sizeModel);
        notifyItemInserted(sizeModelList.size()-1);
        updateSizeModel.setSizeModelList(sizeModelList);
        EventBus.getDefault().postSticky(updateSizeModel);
    }

    public void editSize(SizeModel sizeModel) {
        if(editPos != -1)
        {
            sizeModelList.set(editPos ,sizeModel);
            notifyItemChanged(editPos);
            editPos =-1; // reset variables after success

            //Send update
            updateSizeModel.setSizeModelList(sizeModelList);
            EventBus.getDefault().postSticky(updateSizeModel);

        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_name)
        TextView txt_name;

        @BindView(R.id.txt_price)
        TextView txt_price;

        @BindView(R.id.img_delete)
        ImageView img_delete;

        Unbinder unbinder;

        IRecycelerViewClickListner listener;
        public void setListener(IRecycelerViewClickListner listener) {
            this.listener = listener;
        }


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder = ButterKnife.bind(this , itemView);

            itemView.setOnClickListener(view -> listener.onItemclickListner(view,getAdapterPosition()));
        }
    }
}
