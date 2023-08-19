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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class RingtoneListFragment extends Fragment implements RingtoneAdapter.OnItemClickListener {

    private String listType;
    private FragmentRingtoneListBinding binding;
    private List<Song> songs;

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
        songs = getSongs(listType);
        RingtoneAdapter adapter = new RingtoneAdapter(songs);
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RingtoneControl.INSTANCE.stopAndPrepare();
        binding = null;
    }

    @Override
    public void onItemClicked(RingtoneAdapter.MyViewHolder holder, int position) {
        Log.i("铃声选择", "onItemClicked: 铃声选中！");
        Song song = songs.get(position);
        // 点击项的播放控制（播放、暂停、停止）
        RingtoneControl.INSTANCE.ringtonePlayControl(requireContext(), song.getSongUri(), position);
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
        Cursor cursor = null;
        ContentResolver resolver = requireContext().getContentResolver();
        try {
            if (category.equals("local")) {
                // 使用投影来提高查询效率
                String[] projection = {
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media._ID
                };

                // 本地歌曲
                cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection, "artist!=?", new String[]{"<unknown>"}, null);

                if (cursor != null) {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                    int artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                    int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);

                    while (cursor.moveToNext()) {
                        String title = cursor.getString(titleIndex);
                        String artist = cursor.getString(artistIndex);
                        long id = cursor.getLong(idIndex);
                        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                        songsList.add(new Song(title, artist, songUri));
                    }
                }
            } else if (category.equals("system")) {
                // 系统铃声
                RingtoneManager manager = new RingtoneManager(getContext());
                cursor = manager.getCursor();

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int position = cursor.getPosition();
                        // content://media/external/audio/media/76502?title=Daylight%20Dreaming&canonical=1
                        Uri songUri = manager.getRingtoneUri(position);
                        String title = getTitleFromUri(songUri);
                        assert title != null;
                        songsList.add(new Song(title, "", songUri));
                    }
                }
            }
        } catch (Exception e) {
            // 这里可以根据你的需要来处理异常，例如日志记录或者用户提示
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return songsList;
    }

    public static String getTitleFromUri(Uri uri) {
        try {
            String query = uri.getQuery();
            if (query != null) {
                String[] parameters = query.split("&");
                for (String parameter : parameters) {
                    if (parameter.startsWith("title=")) {
                        String title = parameter.substring(6); // 跳过 "title=" 这6个字符
                        return URLDecoder.decode(title, "UTF-8"); // 转换 %20 等字符
                    }
                }
            }
        } catch (Exception e) {
            // 你可以根据需要处理此异常，例如打印日志或返回默认标题
            return "default title";
        }

        return "default title"; // 如果找不到 title，则返回 null 或其他默认值
    }


}
