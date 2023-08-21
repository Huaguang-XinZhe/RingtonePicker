package com.huaguang.ringtonepicker;

import android.Manifest;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.huaguang.ringtonepicker.databinding.FragmentRingtoneDialogBinding;
import com.permissionx.guolindev.PermissionX;

import java.util.Objects;

public class RingtoneDialogFragment extends BottomSheetDialogFragment {

    static final String RINGTONE_REQUEST_KEY = "ringtoneRequestKey";
    private SPHelper spHelper;
    private Song currentRingtone;
    private FragmentRingtoneDialogBinding binding;

    public RingtoneDialogFragment() {
        // Required empty public constructor
    }

    public static RingtoneDialogFragment newInstance() {
        return new RingtoneDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置 spHelper。不能放在属性中直接赋值，那时候 Fragment 可能还没加在宿主 Activity 上，Context 无效
        spHelper = SPHelper.Companion.getInstance(requireContext());
        // 从 sp 中取值，设置铃声信息
        currentRingtone = getCurrentRingtone(); // 要用到 spHelper，所以应该放在后边

        if (!spHelper.getFlag("from_back_selected") && currentRingtone != null) { // 只要不来自于 ”无“ 的点击，就不会为 null
            // 不来自从列表返回后的重建，且不来自 ”无“ 条目的点击，就初始化 Player
            RingtoneControl.INSTANCE.initializePlayer(
                    requireContext(),
                    Objects.requireNonNull(currentRingtone.getSongUri())
            );
            Log.i("铃声选择", "onCreate: 初始化播放器");
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRingtoneDialogBinding.inflate(getLayoutInflater(), container, false);
        // 显示标题
        binding.tvCurrentRingtone.setText(getTitleDisplay());
        // 当前铃声为 null，不显示播放、暂停图标
        if (currentRingtone == null) {
            binding.ivDisplay.setVisibility(View.GONE);
        } else {
            binding.ivDisplay.setVisibility(View.VISIBLE);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("铃声选择", "onViewCreated: 执行");

        // 当前铃声项的点击监听，通用
        binding.layoutCurrentRingtone.setOnClickListener(v -> {
            Log.i("铃声选择", "onViewCreated: 当前铃声块点击");
            RingtoneControl.INSTANCE.playOrPause();
        });

        // 观察铃声播放状态的变化
        // 这里不要使用 requireActivity()，会出问题，Fragment 销毁时观察者不会被释放
        RingtoneControl.INSTANCE.getStatus().observe(getViewLifecycleOwner(), status -> {
            if (status == Status.PLAYING) {
                binding.ivDisplay.setImageResource(R.drawable.play);
            } else {
                binding.ivDisplay.setImageResource(R.drawable.pause);
            }
        });

        // 获取 RingtoneListFragment 设置的结果并更新 UI（当前铃声项）
        getParentFragmentManager().setFragmentResultListener(
                RINGTONE_REQUEST_KEY,
                this,
                (requestKey, result) -> { // 下面的逻辑有结果传回来才会执行，没有结果传回来就不会执行！
                    Song song = result.getParcelable("songKey");
                    // 更新当前铃声的 Uri 和 title，以备 sp 持久化存储
                    currentRingtone = song;
                    Log.i("铃声选择", "返回结果处理 current = " + currentRingtone);
                    /*---------- 更新当前铃声的 UI（标题和图标）----------------------*/
                    binding.tvCurrentRingtone.setText(song.getSongTitle());
                    // 这段逻辑会在 Fragment 创建类型生命周期回调结束后再执行，所以有必要再设置
                    binding.ivDisplay.setVisibility(View.VISIBLE);
                }
        );

        // 你的其他代码，例如启动 RingtoneListFragment
        binding.tvSystemRingtone.setOnClickListener(v -> {
            /*-------------------------------进入系统铃声列表-----------------------------------*/
            // 停止对话框内当前铃声的播放
            RingtoneControl.INSTANCE.stopRingtone();
            // 关闭对话框
            dismiss();
            // 打开铃声列表页（标记为系统）
            RingtoneListFragment fragment = RingtoneListFragment.newInstance("system");
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment) // 假设你的容器ID是container
                    .addToBackStack(null)
                    .commit();
            // 设置 from_back_selected 的值，让 DialogFragment 重建时不执行 Player 的初始化
        });

        binding.tvLocalRingtone.setOnClickListener(v -> {
            //请求存储空间权限（READ_EXTERNAL_STORAGE）
            requestPermission();

        });

        // “无” 条目的点击监听
        binding.tvNoRingtone.setOnClickListener(v -> {
            currentRingtone = null;
            dismiss();
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("铃声选择", "onDestroy: DialogFragment 销毁了");
        RingtoneControl.INSTANCE.stopRingtone();
        spHelper.saveRingtoneInfo(currentRingtone);
        binding = null;
    }

    private void requestPermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 注意，这里的 Manifest 是 android 包里边的，不是自己应用的 Manifest 类，不要搞错了！
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        PermissionX.init(this)
                .permissions(permission)
                .explainReasonBeforeRequest()
                .onExplainRequestReason((scope, deniedList) -> {
                    scope.showRequestReasonDialog(deniedList, "即将申请的权限是获取本地铃声所必需的条件", "我已明白");
                })
                .onForwardToSettings((scope, deniedList) -> {
                    scope.showForwardToSettingsDialog(deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白");
                })
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        // 权限获取成功提示，只执行一次
                        spHelper.doOnce(() -> {
                            Toast.makeText(getContext(), "权限获取成功！", Toast.LENGTH_SHORT).show();
                            return null;
                        });

                        openLocalRingtoneList();
                    } else {
                        Toast.makeText(getContext(), "您已拒绝此权限，无法获取本地铃声！", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 进入本地铃声列表
     */
    private void openLocalRingtoneList() {
        // 停止对话框内当前铃声的播放
        RingtoneControl.INSTANCE.stopRingtone();
        Toast.makeText(getContext(), "本铃声戴耳机也会外放，请关注音量", Toast.LENGTH_SHORT).show();
        // 关闭对话框
        dismiss();
        // 打开铃声列表（标记为本地）在主活动容器中
        RingtoneListFragment fragment = RingtoneListFragment.newInstance("local");
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment) // 假设你的容器ID是container
                .addToBackStack(null)
                .commit();
        // 更新 sp 中 from_back_selected 的值，既然打开，就一定会返回。
        // 又因为仅有当前 Fragment 持有 SPHelper 的引用，所以在这里设置。

    }

    /**
     * 从 sp 中获取存储的铃声信息，没有就返回系统的。
     */
    private Song getCurrentRingtone() {
        // uri = content://media/external/audio/media/69，用 MediaPlayer 能够播放
        String uriStrFromSP = spHelper.getUri();
        Uri uri;
        String title;

        if (uriStrFromSP.equals("NULL")) return null;

        if (uriStrFromSP.isEmpty()) {
            // songUri=content://settings/system/alarm_alert，用 MediaPlayer 能够播放！
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            title = "系统默认（闹铃）";
        }  else {
            uri = Uri.parse(uriStrFromSP);
            title = spHelper.getTitle();
        }

        assert title != null;
        return new Song(title, uri, "");
    }

    private String getTitleDisplay() {
        String title;
        if (currentRingtone == null) {
            title = "无";
        } else {
            title = currentRingtone.getSongTitle();
        }
        return title;
    }

}
