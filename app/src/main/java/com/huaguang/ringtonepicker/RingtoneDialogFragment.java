package com.huaguang.ringtonepicker;

import android.Manifest;
import android.media.Ringtone;
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

public class RingtoneDialogFragment extends BottomSheetDialogFragment {

    static final String RINGTONE_REQUEST_KEY = "ringtoneRequestKey";
    private FragmentRingtoneDialogBinding binding;
    private Ringtone currentRingtone;

    public RingtoneDialogFragment() {
        // Required empty public constructor
    }

    public static RingtoneDialogFragment newInstance() {
        return new RingtoneDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRingtoneDialogBinding.inflate(getLayoutInflater(), container, false);
        // TODO: 2023/8/19 从 sp 中取值，设置 currentRingtone
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().setFragmentResultListener(
                RINGTONE_REQUEST_KEY,
                this,
                (requestKey, result) -> {
                    // 获取并处理结果
                    Song song = (Song) result.getParcelable("songKey");
                    currentRingtone = RingtoneManager.getRingtone(requireContext(), song.getSongUri());
                    /*---------------------------------更新当前铃声的 UI，并设置点击监听---------------------------------*/
                    binding.tvCurrentRingtone.setText(song.getSongTitle());
                    binding.layoutCurrentRingtone.setOnClickListener(v -> {
                        if (currentRingtone.isPlaying()) {
                            currentRingtone.stop();
                            binding.ivDisplay.setImageResource(R.drawable.play);
                        } else {
                            // play 方法调用一次就会重新播放一次，原播放不会受到影响，多铃声同时进行。
                            // 播放会在后台进行，只有当应用程序的进程结束时播放才会停止，Activity 销毁不受影响。
                            // 仅会完整的播放一次。
                            currentRingtone.play();
                            binding.ivDisplay.setImageResource(R.drawable.pause);
                        }
                    });
                }
        );

        // 你的其他代码，例如启动 RingtoneListFragment
        binding.tvSystemRingtone.setOnClickListener(v -> {
            /*----------------------------------进入系统铃声列表-----------------------------------------*/
            // 停止对话框内当前铃声的播放
            stopCurrentRingtone();
            Toast.makeText(getContext(), "此处厂商似乎做了限制，打开略有延迟", Toast.LENGTH_SHORT).show();
            // 打开铃声列表页（标记为系统）
            RingtoneListFragment fragment = RingtoneListFragment.newInstance("system");
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment) // 假设你的容器ID是container
                    .addToBackStack(null)
                    .commit();
        });

        binding.tvLocalRingtone.setOnClickListener(v -> {
            //请求存储空间权限（READ_EXTERNAL_STORAGE）
            requestPermission();

        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                        //只会执行一次！
                        //                        if (sp.getBoolean("isRequested", true)) {
                        Toast.makeText(getContext(), "权限获取成功！", Toast.LENGTH_SHORT).show();

                        //                            sp.edit().putBoolean("isRequested", false).apply();
                        //                        }
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
        stopCurrentRingtone();
        Toast.makeText(getContext(), "本铃声戴耳机也会外放，请关注音量", Toast.LENGTH_SHORT).show();
        // 打开铃声列表（标记为本地）
        RingtoneListFragment fragment = RingtoneListFragment.newInstance("local");
        getParentFragmentManager().beginTransaction()
                .replace(R.id.container, fragment) // 假设你的容器ID是container
                .addToBackStack(null)
                .commit();
    }

    private void stopCurrentRingtone() {
        // 停止对话框内当前铃声的播放
        if (currentRingtone != null && currentRingtone.isPlaying()) {
            currentRingtone.stop();
        }
    }

}
