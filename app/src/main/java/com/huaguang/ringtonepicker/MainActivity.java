package com.huaguang.ringtonepicker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.huaguang.ringtonepicker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            showRingtoneDialog();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 放在 Activity 的 onDestroy 回调内有时不会得到完整执行，所以放在这里
        SPHelper.Companion.getInstance(this).setFlag("from_back_selected", false); // 恢复初始值
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    public void showRingtoneDialog() {
        RingtoneDialogFragment ringtoneDialogFragment = RingtoneDialogFragment.newInstance();
        ringtoneDialogFragment.show(getSupportFragmentManager(), ringtoneDialogFragment.getTag());
    }
}
