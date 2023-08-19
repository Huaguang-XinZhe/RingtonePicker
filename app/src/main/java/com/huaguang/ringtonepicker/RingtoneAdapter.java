package com.huaguang.ringtonepicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huaguang.ringtonepicker.databinding.FragmentRingtoneListItemBinding;

import java.util.List;

public class RingtoneAdapter extends RecyclerView.Adapter<RingtoneAdapter.MyViewHolder> {

    private final List<Song> songs;
    private OnItemClickListener listener;
    private int selectedPosition = -1; // 初始值为-1，表示没有选中项 // select_only_one


    public RingtoneAdapter(List<Song> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentRingtoneListItemBinding binding = FragmentRingtoneListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.itemBinding.tvSongTitle.setText(song.getSongTitle());
        holder.itemBinding.tvArtist.setText(song.getArtist());
        holder.itemBinding.rbSelected.setChecked(position == selectedPosition); // select_only_one
        holder.itemView.setOnClickListener(v -> {
            /*-------------------------- 保证只选一个（select_only_one） -----------------------------*/
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && selectedPosition != adapterPosition) {
                notifyItemChanged(selectedPosition); // 更新前一个选中项
                selectedPosition = adapterPosition; // 更新选中位置
                notifyItemChanged(selectedPosition); // 更新新选中项
            }
            /*---- 播放、停止音乐，返回结果等与视图控显无关的其他操作 ----*/
            if (listener != null) {
                listener.onItemClicked(holder, adapterPosition);
            }
        });
    }



    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        final FragmentRingtoneListItemBinding itemBinding;

        MyViewHolder(FragmentRingtoneListItemBinding binding) {
            super(binding.getRoot());
            this.itemBinding = binding;
        }
    }

    interface OnItemClickListener {
        void onItemClicked(MyViewHolder holder, int position);
    }
}
