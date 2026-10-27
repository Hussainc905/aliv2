package com.elyastech.elyasfun;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/** Frame-synchronized transparent animated 2.5D stage, no video or internet required.
 * All props move in device coordinates. Actor's limbs deform in a 16 x 28 bitmap mesh.
 */
public final class CartoonSceneView extends View {
    public static final long DURATION_MS = 51000L;
    private static final float TOTAL = DURATION_MS / 1000f;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap frontal, walking, wiping;
    private final boolean preview;
    private final int meshColumns = 16, meshRows = 28;
    private final float[] meshWalk = new float[(meshColumns+1)*(meshRows+1)*2];
    private final float[] meshWipe = new float[(meshColumns+1)*(meshRows+1)*2];
    private long started = -1L;
    private boolean running = false;
    private float h, ground;
    private final Random stars = new Random(2209);

    public CartoonSceneView(Context context, boolean preview) {
        super(context);
        this.preview=preview;
        frontal=asset(context,"boy_front.png");
        walking=asset(context,"boy_walk.png");
        wiping=asset(context,"boy_wipe.png");
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }
    private Bitmap asset(Context ctx,String name) {
        try(InputStream in=ctx.getAssets().open(name)) {
            return BitmapFactory.decodeStream(in);
        } catch(IOException ex) { return null; }
    }
    public void play() { started=SystemClock.uptimeMillis();running=true;postInvalidateOnAnimation(); }
    public void stop() { running=false; }
    private static float cap(float n){return Math.max(0f,Math.min(1f,n));}
    private static float lerp(float a,float b,float n){return a+(b-a)*cap(n);}
    private static float ease(float x){x=cap(x);return x*x*(3f-2f*x);}
    private void color(int col){p.reset();p.setAntiAlias(true);p.setFilterBitmap(true);p.setColor(col);p.setStyle(Paint.Style.FILL);}
    private void stroke(int col,float width){color(col);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
    private void rounded(Canvas c,float x,float y,float w,float ht,float radius,int col){color(col);c.drawRoundRect(x,y,x+w,y+ht,radius,radius,p);}
    private void line(Canvas c,float x1,float y1,float x2,float y2,float width,int col){stroke(col,width);c.drawLine(x1,y1,x2,y2,p);}

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth()<1 || getHeight()<1) return;
        float factor=getWidth()/420f;
        canvas.save(); canvas.scale(factor,factor);
        h=getHeight()/factor;
        ground=h-45;
        float t=started<0?0:(SystemClock.uptimeMillis()-started)/1000f;
        if (preview) wallpaper(canvas,t);
        if(t<=TOTAL) scene(canvas,t);
        canvas.restore();
        if(running){
            if(preview && t>TOTAL+0.6f){started=SystemClock.uptimeMillis();}
            if(preview || t<TOTAL) postInvalidateOnAnimation();
            else running=false;
        }
    }

    private void wallpaper(Canvas c,float time) {
        color(0xFF0F1D3A);
        p.setShader(new LinearGradient(0,0,0,h,
                new int[]{0xFF081526,0xFF173560,0xFF466C91,0xFF18223A},
                new float[]{0,.33f,.73f,1},Shader.TileMode.CLAMP));
        c.drawRect(0,0,420,h,p);p.setShader(null);
        stars.setSeed(2209);
        for(int i=0;i<70;i++){
            float x=stars.nextFloat()*420,y=stars.nextFloat()*h*.64f;
            float a=.3f+.7f*(float)Math.sin(time*.8f+i*.67f)*.35f+.3f;
            color(0xFFEDF6FF);p.setAlpha((int)(170*cap(a)));c.drawCircle(x,y,i%7==0?1.6f:.7f,p);
        }
        color(0xFFECF2FD);p.setShader(new RadialGradient(325,h*.18f,56,
                new int[]{0x99DAE8FF,0x44DAE8FF,0x00DAE8FF},null,Shader.TileMode.CLAMP));
        c.drawCircle(325,h*.18f,58,p);p.setShader(null);
        color(0xFFF0F5FF);c.drawCircle(325,h*.18f,18,p);
        hills(c,h*.61f,0xFF405875,57, .41f);
        hills(c,h*.68f,0xFF2A4264,33,.72f);
        hills(c,h*.78f,0xFF192E4D,22,1.1f);
        color(0x991B304E);c.drawRect(0,h*.77f,420,h,p);
        for(int i=0;i<13;i++){
            float yy=h*.79f+i*(h*.20f/13f);
            line(c,0,yy,420,yy,.6f,0x225DB6D2);
        }
        // These preview-only texts never obscure the actual phone screen in overlay mode.
        color(0xCCFFFFFF);p.setTypeface(Typeface.create("sans-serif-light",Typeface.NORMAL));p.setTextSize(38);
        c.drawText("ELYAS FUN",23,57,p);
        color(0xFFBBDDF5);p.setTextSize(13);
        c.drawText("ANIMATED SCREEN ADVENTURE",25,80,p);
    }
    private void hills(Canvas c,float y,int fill,float amplitude,float wave){
        Path path=new Path();path.moveTo(0,h);
        path.lineTo(0,y);
        for(int i=0;i<=42;i++){
            float x=i*10;
            float peak=(float)(Math.sin(i*.48f*wave+1.3)*amplitude+Math.sin(i*.18f+.6)*amplitude*.75f);
            path.lineTo(x,y-peak);
        }
        path.lineTo(420,h);path.close();color(fill);c.drawPath(path,p);
    }
    // V3: two independently cleaned areas with a genuinely portable foldable step ladder.
    // Timing uses one global playhead so replay and unlock overlays stay deterministic.
    private void scene(Canvas c, float t) {
        final float ladderOneX = 205f;
        final float ladderTwoX = 308f;
        float doorOpenness = t < 1.8f ? ease(t / 1.8f)
                : (t > 48.5f ? 1f - ease((t - 48.5f) / 2f) : 1f);
        drawGroundShadow(c);
        door(c, doorOpenness);

        // The two smudges are two different target locations, not a replay of the same wipe.
        float cleaningFirst = cap((t - 11.5f) / 5.6f);
        float cleaningSecond = cap((t - 32.4f) / 5.4f);
        dirtMark(c, 205, ground - 277, cleaningFirst, t);
        dirtMark(c, 332, ground - 312, cleaningSecond, t);

        boolean ladderVisible = t > 2.1f && t < 48.2f;
        float ladderX = ladderOneX;
        float deployed = 0f;
        if (t <= 6.5f) {
            ladderX = lerp(75f, ladderOneX, ease((t - 2.1f) / 4.4f));
        } else if (t < 22.4f) {
            ladderX = ladderOneX;
            deployed = ease((t - 6.5f) / 1.8f)
                    * (1f - ease((t - 19.8f) / 2.6f));
        } else if (t < 26.9f) {
            ladderX = lerp(ladderOneX, ladderTwoX, ease((t - 22.4f) / 4.5f));
        } else if (t < 43.0f) {
            ladderX = ladderTwoX;
            deployed = ease((t - 26.9f) / 1.7f)
                    * (1f - ease((t - 40.5f) / 2.5f));
        } else {
            ladderX = lerp(ladderTwoX, 70f, ease((t - 43f) / 5.1f));
        }
        if (ladderVisible) ladder(c, ladderX, ground, deployed, t);

        if (t > 1.4f && t < 49.0f) {
            if (t < 6.5f) {
                float n = ease((t - 1.4f) / 5.1f);
                float bx = lerp(78, ladderOneX - 22, n);
                actor(c, walking, bx, ground, 143, 1f, t, true, false);
                // A mildly grumpy arrival: knitted eyebrows and a tiny sigh cloud.
                grumpy(c, bx, ground, t, n);
            } else if (t < 8.3f) {
                actor(c, frontal, ladderOneX - 23, ground, 142, 1f, t, false, false);
                armAction(c, ladderOneX - 23, ground, 142, ease((t-6.5f)/1.8f));
            } else if (t < 11.5f) {
                float n=ease((t-8.3f)/3.2f);
                actor(c, walking, lerp(ladderOneX-23,ladderOneX+9,n),
                        ground-136*n, 146, 1f, t, true, false);
            } else if (t < 17.1f) {
                float q=t-11.5f;
                float sway=(float)Math.sin(q*3.6f)*10f;
                actor(c, wiping, ladderOneX+10+sway*.15f,ground-136,160,1f,t,false,true);
                wipingCloth(c,215+sway,ground-278+(float)Math.cos(q*3.6f)*17f,q);
                sparkle(c,215,ground-279,t);
            } else if (t < 19.8f) {
                float n=ease((t-17.1f)/2.7f);
                actor(c,walking,lerp(ladderOneX+9,ladderOneX-23,n),
                        ground-136*(1-n),145,-1f,t,true,false);
            } else if (t < 22.4f) {
                actor(c,frontal,ladderOneX-25,ground,142,1f,t,false,false);
                armAction(c,ladderOneX-25,ground,142,ease((t-19.8f)/2.6f));
            } else if (t < 26.9f) {
                float n=ease((t-22.4f)/4.5f);
                actor(c,walking,lerp(ladderOneX-25,ladderTwoX-25,n),
                        ground,143,1f,t,true,false);
            } else if (t < 28.6f) {
                actor(c,frontal,ladderTwoX-24,ground,142,1f,t,false,false);
                armAction(c,ladderTwoX-24,ground,142,ease((t-26.9f)/1.7f));
            } else if (t < 32.4f) {
                float n=ease((t-28.6f)/3.8f);
                actor(c,walking,lerp(ladderTwoX-24,ladderTwoX+4,n),
                        ground-135*n,146,1f,t,true,false);
            } else if (t < 37.8f) {
                float q=t-32.4f;
                float sway=(float)Math.sin(q*3.4f)*12f;
                actor(c,wiping,ladderTwoX+4+sway*.10f,ground-135,163,1f,t,false,true);
                wipingCloth(c,332+sway,ground-311+(float)Math.cos(q*3.4f)*19f,q);
                sparkle(c,331,ground-315,t);
            } else if (t < 40.5f) {
                float n=ease((t-37.8f)/2.7f);
                actor(c,walking,lerp(ladderTwoX+4,ladderTwoX-25,n),
                        ground-135*(1-n),144,-1,t,true,false);
            } else if (t < 43.0f) {
                // Explicit look to the viewer and several distinct hand waves at the end.
                actor(c,frontal,ladderTwoX-27,ground,149,1f,t,false,false);
                wave(c,ladderTwoX-4,ground-111,t);
                smileSpark(c,ladderTwoX-27,ground-140,t);
            } else {
                float n=ease((t-43f)/5.1f);
                actor(c,walking,lerp(ladderTwoX-27,76,n),ground,142,-1f,t,true,false);
            }
        }

        // The front rail passes over the character's shoes only while the stepladder is open.
        if(ladderVisible && deployed > .15f) ladderFront(c,ladderX,ground,deployed);
        if(t > 37.0f) sparkle(c,332,ground-314,t);
        doorFrame(c,doorOpenness);
    }
    private void grumpy(Canvas c,float x,float floor,float t,float walked) {
        // These small expressive eyebrows sit over the head of the walking sprite.
        float y = floor - 115f - (float)Math.abs(Math.sin(t*9))*4f;
        line(c,x-14,y-6,x-5,y-2,2.5f,0xFF432C25);
        line(c,x+3,y-2,x+13,y-6,2.5f,0xFF432C25);
        if(walked < .75f) {
            color(0x55D7EFFF);
            float drift=(t*15f)%23f;
            c.drawCircle(x+24+drift*.7f,y-30-drift,3.4f,p);
        }
    }
    private void dirtMark(Canvas c,float x,float y,float progress,float t){
        // The first and second fingerprints fade out independently as each wipe completes.
        float a=1f-ease(progress);
        if(a<=.01f)return;
        color(0xFFBDD0DF);p.setAlpha((int)(58*a));
        c.drawOval(x-51,y-38,x+52,y+42,p);
        for(int i=0;i<5;i++){
            color(0xFFD7E3ED);p.setAlpha((int)(78*a));
            float xx=x-39+i*19,yy=y-22+(i%2)*18;
            c.drawOval(xx-9,yy-12,xx+12,yy+14,p);
        }
        for(int i=0;i<3;i++) {
            stroke(0x66ECF2FA,2f);
            p.setAlpha((int)(100*a));
            c.drawArc(new RectF(x-32+i*8,y-27+i*6,x+32-i*7,y+30-i*4),
                    20,200,false,p);
        }
    }
    private void wipingCloth(Canvas c,float x,float y,float q){
        float r=(float)Math.sin(q*7f)*3.5f;
        rounded(c,x-18+r,y-12,35,23,8,0xFF297DBB);
        rounded(c,x-15+r,y-10,30,17,6,0xFF83CAEE);
        line(c,x-9+r,y-8,x+11+r,y-8,2,0xFFD9F5FF);
        for(int i=0;i<3;i++){
            float yy=y+5+i*4;
            line(c,x-23,yy,x-36,yy-5,1.8f,0x75CDEBFF);
        }
    }
    private void smileSpark(Canvas c,float x,float y,float t){
        float z=(float)(Math.sin(t*5f)*1.4f);
        sparkle(c,x+20+z,y-22,t);
    }
    private void drawGroundShadow(Canvas c){
        color(0x66000000);c.drawOval(14,ground-13,406,ground+24,p);
        color(0x2FDFE9FF);c.drawOval(20,ground-9,402,ground+3,p);
    }
    private void door(Canvas c,float amount){
        float x=22, yy=ground-187;
        color(0xFF6B402A);c.drawRoundRect(x-7,yy-8,x+113,ground+2,25,25,p);
        color(0xFF2D1D28);c.drawRoundRect(x,yy,x+100,ground,19,19,p);
        if(amount>.01f){
            color(0xFFFFC96E);p.setShader(new RadialGradient(x+50,yy+95,95,
                    new int[]{(int)(0xE0FFC978L),(int)(0x60FFC978L),0x00FFC978},null,Shader.TileMode.CLAMP));
            c.drawRoundRect(x+4,yy+3,x+96,ground-4,14,14,p);p.setShader(null);
            color(0x55FFE9C2);p.setAlpha((int)(150*amount));
            c.drawOval(x-15,ground-14,x+170,ground+14,p);
        }
        c.save();c.translate(x+5,yy+3);c.scale(1-amount*.78f,1,0,0);
        rounded(c,0,0,92,178,12,0xFF874825);
        rounded(c,7,5,80,165,9,0xFFB8793A);
        stroke(0xFFDBA268,3);c.drawRoundRect(17,15,70,87,8,8,p);c.drawRoundRect(17,100,70,155,7,7,p);
        rounded(c,21,19,45,64,4,0xFF8C4B24);
        rounded(c,21,104,45,47,4,0xFF95552B);
        color(0xFFFFE2A5);c.drawCircle(75,96,6,p);
        c.restore();
    }
    private void doorFrame(Canvas c,float amount){
        float yy=ground-187;
        stroke(0xFFC4884B,6);c.drawRoundRect(21,yy-2,123,ground+1,18,18,p);
        stroke(0xFF4D2B20,2);c.drawRoundRect(17,yy-7,127,ground+2,21,21,p);
        color(0xFFF0D2A5);c.drawOval(12,ground-6,138,ground+4,p);
    }
    // Hinged, PORTABLE four-step stepladder. Folds flat, is carried, opens into an A-frame.
    private void ladder(Canvas c,float x,float floor,float open,float t){
        c.save();c.translate(x,floor);
        float sway=(open<.08f?(float)Math.sin(t*9)*3f:0);
        c.rotate(sway,-2,-84);
        float spread=lerp(9,70,open);
        // rear rail behind front frame
        line(c,8,-167,spread+1,-6,11,0xFF533320);
        line(c,8,-167,spread+1,-6,6,0xFFC18D52);
        line(c,6,-133,spread*.75f,-49,3,0xFFE0BA79);
        line(c,-30,-6,-6,-167,12,0xFF412615);
        line(c,-30,-6,-6,-167,8,0xFFBD894F);
        line(c,30,-6,6,-167,12,0xFF4B2F1D);
        line(c,30,-6,6,-167,8,0xFFD4A36C);
        for(int j=0;j<4;j++){
            float yy=-29-j*31;
            float width=lerp(50,25,j/3f);
            line(c,-width/2,yy,width/2,yy,11,0xFF583723);
            line(c,-width/2,yy-3,width/2,yy-3,8,0xFFD7AB70);
            line(c,-width/2+5,yy-5,width/2-5,yy-5,2,0xFFEAD4A5);
        }
        rounded(c,-13,-168,26,12,5,0xFF383F49);
        color(0xFFD3D8DB);c.drawCircle(1,-162,3,p);
        color(0xFF333B43);c.drawOval(-36,-11,-18,0,p);c.drawOval(23,-11,39,0,p);
        c.restore();
    }
    private void ladderFront(Canvas c,float x,float floor,float open){
        // Separate front highlight: the character is visually ON the ladder, not behind a static staircase.
        color(0x55FFE4AD);
        c.drawRoundRect(x+12,floor-135,x+17,floor-65,3,3,p);
        color(0xAA332D2C);c.drawCircle(x+6,floor-162,3,p);
    }

    private void actor(Canvas c,Bitmap bitmap,float x,float feet,float height,float direction,float t,boolean moving,boolean cleaning){
        if(bitmap==null)return;
        float phase=t*9f;
        float vertical=moving?(float)Math.abs(Math.sin(phase)) * 4f: cleaning?(float)Math.sin(t*3f)*1.5f:0;
        float incline=moving?(float)Math.sin(phase*.5f)*2.3f:0;
        c.save();c.translate(x,feet-vertical);c.scale(direction,1);
        c.rotate(incline,0,-height*.65f);
        float scale=height/bitmap.getHeight();
        float width=bitmap.getWidth()*scale;
        c.scale(scale,scale);c.translate(-bitmap.getWidth()/2f,-bitmap.getHeight());
        if(moving || cleaning){
            float[] mesh=cleaning?meshWipe:meshWalk;
            populateMesh(bitmap,mesh,phase,moving,cleaning);
            color(Color.WHITE);
            c.drawBitmapMesh(bitmap,meshColumns,meshRows,mesh,0,null,0,p);
        } else {
            color(Color.WHITE);c.drawBitmap(bitmap,0,0,p);
        }
        c.restore();
    }
    private void populateMesh(Bitmap bit,float[] vertices,float phase,boolean moving,boolean cleaning){
        float w=bit.getWidth(),he=bit.getHeight();
        for(int j=0;j<=meshRows;j++){
            for(int i=0;i<=meshColumns;i++){
                float x=w*i/meshColumns,y=he*j/meshRows;
                float nx=x/w,ny=y/he;
                float a=(float)Math.sin(phase),b=(float)Math.sin(phase+Math.PI);
                // Upper limbs swing opposite each other; lower limbs swing out of phase.
                float leftArm=blob(nx,.10f,.24f)*blob(ny,.61f,.17f);
                float rightArm=blob(nx,.86f,.22f)*blob(ny,.55f,.17f);
                float legLeft=blob(nx,.33f,.18f)*blob(ny,.84f,.15f);
                float legRight=blob(nx,.67f,.18f)*blob(ny,.85f,.15f);
                float dx=0,dy=0;
                if(moving){
                    dx+=(-leftArm+rightArm)*a*w*.072f;
                    dy+=(leftArm-rightArm)*a*he*.027f;
                    dx+=(legLeft-legRight)*a*w*.082f;
                    dy+= (legLeft*b + legRight*a)*he*.037f;
                    dx+=blob(ny,.16f,.18f)*a*w*.009f;
                }
                if(cleaning){
                    // Top right (raised wiping hand) articulates independently of the head.
                    float hand=blob(nx,.83f,.21f)*blob(ny,.20f,.23f);
                    dx+=hand*(float)Math.sin(phase*.36f)*w*.11f;
                    dy+=hand*(float)Math.cos(phase*.45f)*he*.026f;
                }
                int off=(j*(meshColumns+1)+i)*2;
                vertices[off]=x+dx;vertices[off+1]=y+dy;
            }
        }
    }
    private static float blob(float x,float mid,float width){float z=(x-mid)/width;return (float)Math.exp(-z*z*1.5);}
    private void armAction(Canvas c,float x,float floor,float size,float t){
        float xx=x+24, yy=floor-size*.63f;
        line(c,xx,yy,xx+16+8*t,yy+11*t,7,0xFFFFD3AE);
    }
    private void wipe(Canvas c,float x,float y,float n){
        // Wiping actually changes the graphic layer: fog disappears and glint traces reveal it.
        float fade=(float)Math.max(0,1-n/8f);
        color(0x99D8E7FF);p.setAlpha((int)(72*fade));
        c.drawRoundRect(x-60,y-48,x+55,y+50,22,22,p);
        for(int i=0;i<7;i++){
            float xx=x+((i*37)%85)-40;
            float yy=y+((i*29)%65)-30;
            color(0xFFAAD7FF);p.setAlpha((int)(110*fade));
            c.drawCircle(xx,yy,2+i%4,p);
        }
        for(int k=0;k<3;k++){
            float yy=y-20+k*13;
            line(c,x-35,yy,x+20,yy-8,2.1f,0x55D8FAFF);
        }
        sparkle(c,x+26,y-14,n);
    }
    private void wave(Canvas c,float x,float y,float t){
        c.save();c.rotate((float)Math.sin(t*13)*22,x,y);
        line(c,x-10,y+38,x+8,y+5,9,0xFFFFCFAD);
        color(0xFFFFDBB7);c.drawOval(x,y-8,x+19,y+12,p);
        c.restore();
    }
    private void sparkle(Canvas c,float x,float y,float t){
        for(int i=0;i<7;i++){
            float phase=(t*1.6f+i*.71f)%1.4f;
            float xx=x+(float)Math.sin(i*2.4f)*58,yy=y+(float)Math.cos(i*1.4f)*43-25*phase;
            float r=(1.1f+(i%3))*(1-cap(phase/1.6f));
            if(r<.3f)continue;
            stroke(0x99E4FCFF,1.25f);
            c.drawLine(xx-r*2,yy,xx+r*2,yy,p);c.drawLine(xx,yy-r*2,xx,yy+r*2,p);
        }
    }
}
