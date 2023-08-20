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
    protected void onDestroy() {
        super.onDestroy();
        SPHelper.Companion.getInstance(this).setFlag("from_back", false); // 恢复初始值
        binding = null;
    }

    public void showRingtoneDialog() {
        RingtoneDialogFragment ringtoneDialogFragment = RingtoneDialogFragment.newInstance();
        ringtoneDialogFragment.show(getSupportFragmentManager(), ringtoneDialogFragment.getTag());
    }
}
