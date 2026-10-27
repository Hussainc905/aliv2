package com.elyastech.elyasfun;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private CartoonSceneView preview;
    private TextView status;
    private int dp(float n){return Math.round(getResources().getDisplayMetrics().density*n);}
    private GradientDrawable gradient(int a,int b,int radius){
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});
        d.setCornerRadius(dp(radius));return d;
    }
    private GradientDrawable plain(int color,int radius,int border){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));
        if(border!=0)d.setStroke(dp(1),border);return d;
    }
    private TextView text(String value,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);
        t.setGravity(Gravity.CENTER);t.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
        if(bold)t.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        return t;
    }
    private void button(LinearLayout root,String label,boolean primary,View.OnClickListener click){
        TextView b=text(label,16,primary?Color.WHITE:0xFFDDE8FF,true);
        b.setBackground(primary?gradient(0xFF486CE2,0xFF7057B9,17):plain(0xFF222D49,17,0xFF3C4D70));
        b.setOnClickListener(click);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(56));p.bottomMargin=dp(10);root.addView(b,p);
    }
    private TextView label(LinearLayout l,String s,int size,int col,boolean bold,int bottom){
        TextView t=text(s,size,col,bold);t.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(bottom);l.addView(t,p);
        return t;
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setStatusBarColor(0xFF0B1020);
        getWindow().setNavigationBarColor(0xFF0B1020);
        ScrollView scroll=new ScrollView(this);
        scroll.setBackgroundColor(0xFF0B1020);scroll.setFillViewport(true);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(18),dp(22),dp(18),dp(28));
        scroll.addView(page);setContentView(scroll);
        TextView badge=label(page,"ELYAS  •  FUN STUDIO",12,0xFF90B4FE,true,5);
        badge.setLetterSpacing(.18f);
        label(page,"مغامرة طفلك السينمائية",26,0xFFF5F6FF,true,4);
        label(page,"V3.0  •  حركتان للتنظيف  •  2.5D",13,0xFFA8B8D4,false,14);
        FrameLayout frame=new FrameLayout(this);
        frame.setBackground(gradient(0xFF30436A,0xFF0E1830,29));
        frame.setPadding(dp(5),dp(5),dp(5),dp(5));
        frame.setClipToOutline(true);
        LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(dp(278),dp(508));fp.bottomMargin=dp(15);page.addView(frame,fp);
        preview=new CartoonSceneView(this,true);
        frame.addView(preview,new FrameLayout.LayoutParams(-1,-1));
        status=label(page,"",14,0xFFBFD4FF,true,13);
        button(page,"▶  شاهد المغامرة مع صوت الباب",true,v->replay());
        button(page,"✦  إظهار فوق شاشة الهاتف",false,v->showOverlay());
        button(page,"●  تشغيل عند فتح قفل الهاتف",false,v->enableService());
        button(page,"■  إيقاف التشغيل التلقائي",false,v->stopService());
        LinearLayout soundRow=new LinearLayout(this);soundRow.setOrientation(LinearLayout.HORIZONTAL);
        soundRow.setGravity(Gravity.CENTER_VERTICAL);soundRow.setBackground(plain(0xFF19253C,16,0xFF33435E));
        soundRow.setPadding(dp(13),dp(6),dp(10),dp(6));
        TextView soundLabel=text("صوت فتح الباب فقط",15,0xFFE6EFFF,true);
        soundLabel.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        soundRow.addView(soundLabel,new LinearLayout.LayoutParams(0,dp(44),1));
        Switch s=new Switch(this);
        s.setChecked(getSharedPreferences("elyas_fun",MODE_PRIVATE).getBoolean("door_sound",true));
        s.setThumbTintList(ColorStateList.valueOf(0xFFE9F2FF));
        s.setOnCheckedChangeListener((b,checked)->getSharedPreferences("elyas_fun",MODE_PRIVATE)
                .edit().putBoolean("door_sound",checked).apply());
        soundRow.addView(s);
        LinearLayout.LayoutParams soundParams=new LinearLayout.LayoutParams(-1,dp(57));soundParams.bottomMargin=dp(17);
        page.addView(soundRow,soundParams);
        label(page,"تسلسل الحركة: يفتح الباب وهو متضايق شويّة ← يحمل سلّم قابل للطي ← يصعد وينظف المكان الأول ← ينزل ويحرك السلّم ← يصعد وينظف المكان الثاني ← ينظر إليك ويلوح باي باي ← يرجع للباب",13,0xFFB2C1DA,false,10).setLineSpacing(dp(4),1f);
        label(page,"الشخصية خيالية للمتعة فقط ولا تنظّف الشاشة فعليًا. يتطلّب الظهور فوق التطبيقات إذن العرض وخدمة إشعار دائمة. بعد إعادة تشغيل الهاتف قد يلزم فتح التطبيق مجددًا حسب قيود أندرويد.",12,0xFF8091B2,false,5).setLineSpacing(dp(3),1f);
        updateStatus();preview.play();
    }
    private void replay(){preview.play();SoundFX.door(this);}
    private boolean overlayOkay(){return Build.VERSION.SDK_INT<23||Settings.canDrawOverlays(this);}
    private boolean requestOverlay(){
        if(overlayOkay())return true;
        Toast.makeText(this,"اسمح للتطبيق بالظهور فوق التطبيقات ثم ارجع",Toast.LENGTH_LONG).show();
        startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));
        return false;
    }
    private void notifPermission(){
        if(Build.VERSION.SDK_INT>=33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},101);
    }
    private void showOverlay(){if(!requestOverlay())return;startFun(true);}
    private void enableService(){if(!requestOverlay())return;startFun(false);}
    private void startFun(boolean show){
        getSharedPreferences("elyas_fun",MODE_PRIVATE).edit().putBoolean("enabled",true).apply();
        notifPermission();
        Intent service=new Intent(this,FunOverlayService.class);
        if(show)service.setAction(FunOverlayService.ACTION_SHOW);
        try{
            if(Build.VERSION.SDK_INT>=26)startForegroundService(service);
            else startService(service);
        }catch(RuntimeException exc){
            getSharedPreferences("elyas_fun",MODE_PRIVATE).edit().putBoolean("enabled",false).apply();
            Toast.makeText(this,"فشل التشغيل: "+exc.getMessage(),Toast.LENGTH_LONG).show();
        }
        updateStatus();
    }
    private void stopService(){
        getSharedPreferences("elyas_fun",MODE_PRIVATE).edit().putBoolean("enabled",false).apply();
        stopService(new Intent(this,FunOverlayService.class));updateStatus();
    }
    private void updateStatus(){
        if(status==null)return;
        boolean on=getSharedPreferences("elyas_fun",MODE_PRIVATE).getBoolean("enabled",false);
        status.setText(on?"● التشغيل التلقائي: مفعّل":"○ التشغيل التلقائي: غير مفعّل");
        status.setTextColor(on?0xFF75E9B5:0xFFDDE6F9);
    }
    @Override protected void onResume(){super.onResume();updateStatus();if(preview!=null)preview.play();}
    @Override protected void onPause(){if(preview!=null)preview.stop();super.onPause();}
}
