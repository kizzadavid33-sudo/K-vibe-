package com.kvibe.app;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.View;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.webkit.*;
import java.io.InputStream;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
 private WebView web; private ValueCallback<Uri[]> chooser;
 private static final int PERMS=41, FILES=42;

 @Override public void onCreate(Bundle b){
  super.onCreate(b);
  getWindow().setStatusBarColor(0xff000000);

  FrameLayout root=new FrameLayout(this);
  root.setBackgroundColor(0xff000000);

  web=new WebView(this);
  web.setBackgroundColor(0xff000000);
  WebSettings s=web.getSettings();
  s.setJavaScriptEnabled(true);
  s.setDomStorageEnabled(true);
  s.setMediaPlaybackRequiresUserGesture(false);
  s.setAllowFileAccess(false);
  s.setAllowContentAccess(true);
  s.setSupportZoom(false);
  s.setBuiltInZoomControls(false);

  WebViewAssetLoader loader=new WebViewAssetLoader.Builder()
    .addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();

  web.setWebChromeClient(new WebChromeClient(){
   @Override public void onPermissionRequest(PermissionRequest r){
    runOnUiThread(()->{
     if(Build.VERSION.SDK_INT>=23 &&
       (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)){
      requestPermissions();
     } else r.grant(r.getResources());
    });
   }
   @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> cb,FileChooserParams p){
    chooser=cb;
    Intent i=p.createIntent();
    try{startActivityForResult(i,FILES);}
    catch(Exception e){chooser=null; return false;}
    return true;
   }
  });

  root.addView(web,new FrameLayout.LayoutParams(-1,-1));

  ImageView splash=new ImageView(this);
  splash.setBackgroundColor(0xff000000);
  splash.setScaleType(ImageView.ScaleType.FIT_CENTER);
  try{
   InputStream in=getAssets().open("kvibe-logo.b64");
   java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();
   byte[] buf=new byte[8192]; int n;
   while((n=in.read(buf))!=-1) out.write(buf,0,n);
   in.close();
   byte[] bytes=Base64.decode(out.toString("UTF-8"),Base64.DEFAULT);
   Bitmap logo=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
   splash.setImageBitmap(logo);
  }catch(Exception ignored){}

  FrameLayout.LayoutParams splashParams=new FrameLayout.LayoutParams(-1,-1);
  splashParams.gravity=Gravity.CENTER;
  root.addView(splash,splashParams);
  setContentView(root);

  web.setVisibility(View.INVISIBLE);
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){
    return loader.shouldInterceptRequest(r.getUrl());
   }
   @Override public void onPageFinished(WebView v,String url){
    v.setVisibility(View.VISIBLE);
    splash.animate().alpha(0f).setDuration(350).withEndAction(()->root.removeView(splash)).start();
   }
  });

  requestPermissions();
  web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
 }

 private void requestPermissions(){
  if(Build.VERSION.SDK_INT>=23) ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},PERMS);
 }
 @Override protected void onActivityResult(int r,int c,Intent d){
  super.onActivityResult(r,c,d);
  if(r==FILES&&chooser!=null){
   chooser.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(c,d));
   chooser=null;
  }
 }
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);}
 @Override public void onBackPressed(){if(web.canGoBack()) web.goBack(); else super.onBackPressed();}
}
