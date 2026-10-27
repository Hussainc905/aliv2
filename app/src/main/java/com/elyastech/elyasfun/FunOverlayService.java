package com.elyastech.elyasfun;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;

public final class FunOverlayService extends Service {
    public static final String ACTION_SHOW="com.elyastech.elyasfun.SHOW";
    public static final String ACTION_STOP="com.elyastech.elyasfun.STOP";
    private static final String CHANNEL="elyas_fun_channel_v3";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private WindowManager windowManager;
    private CartoonSceneView scene;
    private BroadcastReceiver unlockReceiver;
    private Runnable removeTask;

    @Override public void onCreate() {
        super.onCreate();
        windowManager=(WindowManager)getSystemService(WINDOW_SERVICE);
        NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel ch=new NotificationChannel(CHANNEL,"ELYAS FUN • التنشيط",NotificationManager.IMPORTANCE_LOW);
        ch.setSound(null,null);ch.enableVibration(false);ch.enableLights(false);
        manager.createNotificationChannel(ch);
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notif=new Notification.Builder(this,CHANNEL)
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentTitle("ELYAS FUN V3")
                .setContentText("جاهز للظهور عند فتح القفل")
                .setContentIntent(open)
                .setOngoing(true)
                .build();
        startForeground(202,notif);
        unlockReceiver=new BroadcastReceiver() {
            @Override public void onReceive(Context context,Intent intent){
                if(Intent.ACTION_USER_PRESENT.equals(intent.getAction())) show();
            }
        };
        IntentFilter filter=new IntentFilter(Intent.ACTION_USER_PRESENT);
        if(Build.VERSION.SDK_INT>=33) registerReceiver(unlockReceiver,filter,Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(unlockReceiver,filter);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        if(intent!=null && ACTION_STOP.equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
        if(!getSharedPreferences("elyas_fun",MODE_PRIVATE).getBoolean("enabled",false)){
            stopSelf();return START_NOT_STICKY;
        }
        if(intent!=null && ACTION_SHOW.equals(intent.getAction())) show();
        return START_STICKY;
    }
    private void show(){
        if(!Settings.canDrawOverlays(this))return;
        removeOverlay();
        CartoonSceneView next=new CartoonSceneView(this,false);
        WindowManager.LayoutParams params=new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START;
        // <= 0.8 for Android 12+ pass-through touch security threshold.
        params.alpha=.78f;
        try{
            windowManager.addView(next,params);
            scene=next;
            next.play();
            SoundFX.door(this);
            removeTask=this::removeOverlay;
            handler.postDelayed(removeTask,CartoonSceneView.DURATION_MS+250);
        }catch(RuntimeException ignored){scene=null;}
    }
    private void removeOverlay(){
        if(removeTask!=null)handler.removeCallbacks(removeTask);
        removeTask=null;
        if(scene!=null){
            scene.stop();
            try{windowManager.removeViewImmediate(scene);}catch(RuntimeException ignored){}
            scene=null;
        }
    }
    @Override public void onDestroy(){
        removeOverlay();
        if(unlockReceiver!=null){try{unregisterReceiver(unlockReceiver);}catch(RuntimeException ignored){}}
        unlockReceiver=null;
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent){return null;}
}
