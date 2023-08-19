package com.huaguang.ringtonepicker;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.huaguang.ringtonepicker.databinding.FragmentRingtoneListBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RingtoneListFragment extends Fragment implements RingtoneAdapter.OnItemClickListener {

    private String listType;
    private FragmentRingtoneListBinding binding;

    public RingtoneListFragment() {
        // Required empty public constructor
    }

    public static RingtoneListFragment newInstance(String listType) {
        RingtoneListFragment fragment = new RingtoneListFragment();
        Bundle args = new Bundle();
        args.putString("listType", listType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listType = getArguments().getString("listType");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRingtoneListBinding.inflate(inflater, container, false);

        // 配置 RecyclerView 的布局和适配器（包括数据和监听器）
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Song> songs = getSongs(listType);
        RingtoneAdapter adapter = new RingtoneAdapter(songs);
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        RingtoneControl.INSTANCE.stopRingtone();
        binding = null;
    }

    @Override
    public void onItemClicked(RingtoneAdapter.MyViewHolder holder, Song song) {
        Log.i("铃声选择", "onItemClicked: 铃声选中！");
        // 停止前边的音乐，播放新音乐
        RingtoneControl.INSTANCE.playRingtone(requireContext(), song.getSongUri());
        // 设置结果
        setResult(song);
    }

    /**
     * 设置结果，对于特定的请求键，多次设置的结果将被最终结果覆盖。
     */
    public void setResult(Song song) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("songKey", song);
        getParentFragmentManager().setFragmentResult(RingtoneDialogFragment.RINGTONE_REQUEST_KEY, bundle);
    }

    /**
     * 根据传入的类名获取歌曲列表（Song 对象列表）
     */
    private List<Song> getSongs(String category) {
        List<Song> songsList = new ArrayList<>();
        Cursor cursor;
        ContentResolver resolver = requireContext().getContentResolver();

        if (category.equals("local")) {
            // 本地歌曲
            cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, "artist!=?", new String[]{"<unknown>"}, null);
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                songsList.add(new Song(title, artist, songUri));
            }
        } else { // category.equals("system")
            // 系统铃声
            RingtoneManager manager = new RingtoneManager(getContext());
            cursor = manager.getCursor();
            while (cursor.moveToNext()) {
                int position = cursor.getPosition();
                String title = manager.getRingtone(position).getTitle(getContext());
                Uri songUri = manager.getRingtoneUri(position);
                songsList.add(new Song(title, "", songUri));
            }
        }
        cursor.close();

        return songsList;
    }

}
