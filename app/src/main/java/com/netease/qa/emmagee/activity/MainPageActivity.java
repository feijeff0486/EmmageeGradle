/*
 * Copyright (c) 2012-2013 NetEase, Inc. and other contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.netease.qa.emmagee.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.qa.emmagee.R;
import com.netease.qa.emmagee.common.adapter.ProgrameAdapter;
import com.netease.qa.emmagee.event.ServiceStateEvent;
import com.netease.qa.emmagee.service.EmmageeService;
import com.netease.qa.emmagee.utils.Constants;
import com.netease.qa.emmagee.utils.ProcessInfo;
import com.netease.qa.emmagee.utils.Settings;
import com.netease.qa.emmagee.utils.ShellUtils;
import com.netease.qa.emmagee.utils.bus.RxBus;

import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Main Page of Emmagee
 *
 * @author andrewleo
 */
public class MainPageActivity extends Activity {

    private static final String LOG_TAG = Constants.LOG_PREFIX + MainPageActivity.class.getSimpleName();

    private static final int TIMEOUT = 20000;

    private ProcessInfo processInfo;
    private Intent monitorService;
    private RecyclerView lstViProgramme;
    private Button btnTest;
    private int pid, uid;

    private TextView nbTitle;
    private ImageView ivGoBack;
    private ImageView ivBtnSet;
    private LinearLayout layBtnSet;
    private Long mExitTime = (long) 0;
    private ProgrameAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "MainActivity::onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mainpage);

        initTitleLayout();
        loadSettings();
        processInfo = new ProcessInfo();
        btnTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest();
            }
        });

        mAdapter = new ProgrameAdapter(processInfo.getAllPackages(getBaseContext()));
        lstViProgramme.setAdapter(mAdapter);

        nbTitle.setText(getString(R.string.app_name));
        ivGoBack.setImageResource(R.drawable.refresh);
        ivBtnSet.setImageResource(R.drawable.settings_button);
        layBtnSet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSettingsActivity();
            }
        });

        ivGoBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Toast.makeText(MainPageActivity.this, R.string.update_list, Toast.LENGTH_SHORT).show();
                mAdapter.swapItems(processInfo.getAllPackages(getBaseContext()));
            }
        });
    }

    private void initTitleLayout() {
        ivGoBack = (ImageView) findViewById(R.id.go_back);
        nbTitle = (TextView) findViewById(R.id.nb_title);
        ivBtnSet = (ImageView) findViewById(R.id.btn_set);
        lstViProgramme = (RecyclerView) findViewById(R.id.processList);
        btnTest = (Button) findViewById(R.id.btn_test);
        layBtnSet = (LinearLayout) findViewById(R.id.lay_btn_set);
    }

    private void loadSettings() {
        SharedPreferences preferences = Settings.getDefaultSharedPreferences(this);
        boolean wakeLock = preferences.getBoolean(Settings.KEY_WACK_LOCK, false);
        if (wakeLock) {
            Toast.makeText(this, R.string.wake_lock_on_toast, Toast.LENGTH_LONG).show();
            Settings.getDefaultWakeLock(this).acquireFullWakeLock();
        }
    }

    private void startTest() {
        if (Build.VERSION.SDK_INT >= 24) {
            Toast.makeText(MainPageActivity.this, getString(R.string.nougat_warning), Toast.LENGTH_LONG).show();
            return;
        }
        monitorService = new Intent();
        monitorService.setClass(MainPageActivity.this, EmmageeService.class);
        if (getString(R.string.start_test).equals(btnTest.getText().toString())) {
            if (mAdapter.checkedProg != null) {
                Log.i(LOG_TAG, "onClick: " + mAdapter.checkedProg);
                String packageName = mAdapter.checkedProg.getPackageName();
                String processName = mAdapter.checkedProg.getProcessName();
                Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                String startActivity = "";
                Log.d(LOG_TAG, packageName);
                // clear logcat
                try {
                    Runtime.getRuntime().exec("logcat -c");
                } catch (IOException e) {
                    Log.d(LOG_TAG, e.getMessage());
                }

                pid = getPidByPackageName(packageName);

                if (pid == 0) {
                    Log.e(LOG_TAG, packageName + " not running!");
                    Toast.makeText(MainPageActivity.this, packageName + " not running!", Toast.LENGTH_LONG).show();

                    try {
                        startActivity = intent.resolveActivity(getPackageManager()).getShortClassName();
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainPageActivity.this, getString(R.string.can_not_start_app_toast), Toast.LENGTH_LONG).show();
                        return;
                    }
                    waitForAppStart(packageName);
                }

                Log.i(LOG_TAG, String.format("ready watching [%s]-[%s]", packageName, pid));
                monitorService.putExtra("processName", processName);
                monitorService.putExtra("pid", pid);
                monitorService.putExtra("uid", uid);
                monitorService.putExtra("packageName", packageName);
                monitorService.putExtra("startActivity", startActivity);
                startService(monitorService);
                btnTest.setText(getString(R.string.stop_test));
            } else {
                Toast.makeText(MainPageActivity.this, getString(R.string.choose_app_toast), Toast.LENGTH_LONG).show();
            }
        } else {
            btnTest.setText(getString(R.string.start_test));
            Toast.makeText(MainPageActivity.this, getString(R.string.test_result_file_toast) + EmmageeService.resultFilePath,
                    Toast.LENGTH_LONG).show();
            stopService(monitorService);
        }
    }

    private Disposable serviceStateDisposable;

    @Override
    public void onResume() {
        super.onResume();
        RxBus.getDefault().toObservableSticky(ServiceStateEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ServiceStateEvent>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        serviceStateDisposable = d;
                    }

                    @Override
                    public void onNext(ServiceStateEvent event) {
                        if (!event.running) {
                            btnTest.setText(getString(R.string.start_test));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onPause() {
        if (serviceStateDisposable != null && !serviceStateDisposable.isDisposed()) {
            serviceStateDisposable.dispose();
        }
        super.onPause();
    }

    /**
     * wait for test application started.
     *
     * @param packageName package name of test application
     */
    private void waitForAppStart(String packageName) {
        Log.d(LOG_TAG, "wait for app start");
        boolean isProcessStarted = false;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + TIMEOUT) {
            pid = processInfo.getPidByPackageName(getBaseContext(), packageName);
            if (pid != 0) {
                isProcessStarted = true;
                Log.i(LOG_TAG, String.format("waitForAppStart: running... %s-pid:%s", packageName, pid));
                break;
            }
            if (isProcessStarted) {
                Log.i(LOG_TAG, String.format("waitForAppStart: running... %s-pid:%s", packageName, pid));
                break;
            }
        }
    }

    private int getPidByPackageName(@NonNull String packageName) {
        Log.i(LOG_TAG, "getPidByPackageName: packageName= " + packageName);
        int pid = 0;
        ShellUtils.CommandResult result = ShellUtils.execCmd("ps | grep " + packageName, false);
        Log.i(LOG_TAG, "getPidByPackageName: " + result);
        if (result.result == 0) {
            String list_regex = "\\n+";
            String item_regex = "\\s+";
            String[] list = result.successMsg.split(list_regex);
            for (String split : list) {
                String[] item = split.split(item_regex);

                for (String s : item) {
                    Log.i(LOG_TAG, "getPidByPackageName: " + s);
                }
                if (item[8].equals(packageName)) {
                    pid = Integer.parseInt(item[1]);
                    if ("system".equals(item[0])) {
                        uid = 1000;
                    } else if (item[0].startsWith("u0_a")) {
                        uid = Integer.parseInt(item[0].replace("u0_a", "100"));
                    } else {
                        uid = 0;
                    }
                    break;
                }
                Log.i(LOG_TAG, "---------------------- ");
            }
        }
        return pid;
    }

    /**
     * show a dialog when click return key.
     *
     * @return Return true to prevent this event from being propagated further,
     * or false to indicate that you have not handled this event and it
     * should continue to be propagated.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);

//            if ((System.currentTimeMillis() - mExitTime) > 2000) {
//                Toast.makeText(this, R.string.quite_alert, Toast.LENGTH_SHORT).show();
//                mExitTime = System.currentTimeMillis();
//            } else {
//                if (monitorService != null) {
//                    Log.d(LOG_TAG, "stop service");
//                    stopService(monitorService);
//                }
//                System.exit(0);
//            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            goToSettingsActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void goToSettingsActivity() {
        Intent intent = new Intent();
        intent.setClass(MainPageActivity.this, SettingsActivity.class);
        startActivityForResult(intent, Activity.RESULT_FIRST_USER);
    }

}
