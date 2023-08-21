package com.huaguang.ringtonepicker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huaguang.ringtonepicker.databinding.FragmentRingtoneListItemBinding;

import java.util.List;

public class RingtoneAdapter extends RecyclerView.Adapter<RingtoneAdapter.MyViewHolder> {

    private final List<Song> songs;
    private final Context context;
    private int selectedPosition = -1; // select_only_one
    private OnItemClickListener listener;

    /**
     * @param songs 适配器所需的源数据
     * @param context 为了在适配器关联到 RecyclerView 后创建并设置布局管理器所需，本质上还是为了实现页面定位。
     * @param spHelper 要求传入 SPHelper，为了获取 title 和（新）列表比较，以找出铃声的位置
     */
    public RingtoneAdapter(List<Song> songs, Context context, SPHelper spHelper) {
        this.songs = songs;
        this.context = context;

        // 找到匹配的位置（先找到位置，再去定位，以免出现不在可见范围内无法定位的问题）
        // 因为可见范围内的条目绑定完后就会回调到 onAttachedToRecyclerView 方法。
        String title = spHelper.getTitle();
        for (int i = 0; i < songs.size(); i++) {
            if (title != null && title.equals(songs.get(i).getSongTitle())) {
                selectedPosition = i;
                break;
            }
        }
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
            // 直接使用 position 会警告（赋值、判断的时候），使用 adapterPosition 不会
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && selectedPosition != adapterPosition) {
                notifyItemChanged(selectedPosition); // 更新前一个选中项
                selectedPosition = adapterPosition; // 更新选中位置
                notifyItemChanged(selectedPosition); // 更新新选中项
            }
            /*---- 播放、停止音乐，返回结果等与视图控显无关的其他操作 ----*/
            if (listener != null) {
                listener.onItemClicked(holder, song, adapterPosition);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        Log.i("铃声选择", "onAttachedToRecyclerView: 设置布局管理器");
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        // 比 Handler 延迟好用一些
        recyclerView.post(() -> layoutManager.scrollToPositionWithOffset(selectedPosition, 500));

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
        void onItemClicked(MyViewHolder holder, Song song, int position);
    }
}
