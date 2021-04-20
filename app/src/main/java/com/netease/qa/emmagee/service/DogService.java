package com.netease.qa.emmagee.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.netease.qa.emmagee.utils.Constants;

/**
 * @author Jeff
 * @describe
 * @date 2020/4/7.
 */
public class DogService extends Service {
    private static final String TAG = Constants.LOG_PREFIX + "DogService";
    private MyBinder mBinder;

    private String processName, packageName, startActivity;
    private int pid, uid;

    public DogService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        if (intent != null && intent.getExtras() != null) {
            pid = intent.getExtras().getInt("pid");
//		uid = intent.getExtras().getInt("uid");
            processName = intent.getExtras().getString("processName");
            packageName = intent.getExtras().getString("packageName");
            startActivity = intent.getExtras().getString("startActivity");
        }
        Toast.makeText(this, "DogService 启动", Toast.LENGTH_LONG).show();
        bindService(new Intent(this, EmmageeService.class), connection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "connected with EmmageeService.");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "DogService onServiceDisconnected: ");
            if (EmmageeService.shutdownByUser) {
                return;
            }
//            Toast.makeText(DogService.this,"链接断开，重新启动 EmmageeService",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DogService.this, EmmageeService.class);
            intent.putExtra("processName", processName);
            intent.putExtra("pid", pid);
            intent.putExtra("packageName", packageName);
            intent.putExtra("startActivity", startActivity);
            intent.putExtra("watch_flag", true);
            startService(new Intent(DogService.this, EmmageeService.class));
            bindService(new Intent(DogService.this, EmmageeService.class), connection, Context.BIND_IMPORTANT);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        mBinder = new MyBinder();
        return mBinder;
    }

    private class MyBinder extends Binder {

    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        unbindService(connection);
        super.onDestroy();
    }

}
