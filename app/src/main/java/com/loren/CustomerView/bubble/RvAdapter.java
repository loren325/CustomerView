package com.loren.CustomerView.bubble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.loren.CustomerView.R;

import java.util.ArrayList;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {

    private ArrayList<MessageEntity> msgList;

    public RvAdapter(ArrayList<MessageEntity> msgList){
        this.msgList = msgList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageEntity messageEntity = msgList.get(position);
        if(Integer.parseInt(messageEntity.msgCount) > 0){
            if(Integer.parseInt(messageEntity.msgCount) > 99){
                holder.dragTv.setText("99+");
            }else {
                holder.dragTv.setText(messageEntity.msgCount);
            }
            holder.dragTv.setVisibility(View.VISIBLE);
        }else {
            holder.dragTv.setVisibility(View.GONE);
        }

        holder.dragTv.setOnDragListener(new DragMsgView.OnDragListener() {

            @Override
            public void onRestore() {

            }

            @Override
            public void onDismiss() {
                holder.dragTv.setVisibility(View.GONE);
                messageEntity.msgCount = "0";
            }
        });
        holder.titleTv.setText(messageEntity.title);
        holder.contentTv.setText(messageEntity.content);
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        DragMsgView dragTv;
        TextView titleTv, contentTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dragTv = itemView.findViewById(R.id.drag_tv);
            titleTv = itemView.findViewById(R.id.title_tv);
            contentTv = itemView.findViewById(R.id.content_tv);
        }
    }
}
