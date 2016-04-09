package com.sandstormweb.droneone.helpers.camerahelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.*;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class CameraHelper
{
    private Camera camera;
    private byte[] data;

//    interfaces
    public interface OnPictureCaptured{
        public void onPictureCaptured(byte[] data);
    }
    public interface OnCameraInitialized{
        public void OnCameraInitialized(CameraHelper cameraHelper);
    }

    public CameraHelper(final SurfaceView surfaceView, final OnCameraInitialized onCameraInitialized)
    {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(SurfaceHolder holder) {
                            try {
                                camera = Camera.open();

                                camera.setPreviewDisplay(holder);
                                camera.startPreview();
                                setPreviewImageSizeToLowest();

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        onCameraInitialized.OnCameraInitialized(CameraHelper.this);
                                    }
                                }, 1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                            try {
                                refreshCamera(holder);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void surfaceDestroyed(SurfaceHolder holder) {
                            try {
                                camera.stopPreview();
                                camera.release();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public byte[] getPreview()
    {
        try{
            return this.data;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param quality quality is an int between 1 to 100 where 1 is lowest quality and 100 is the highest quality
     */
    public void setQuality(int quality)
    {
        try{
            if(quality > 100 || quality < 1) throw new Exception("quality is not in 1 to 100 range");

            Camera.Parameters parameters = this.camera.getParameters();

            parameters.setJpegQuality(quality);

            this.camera.setParameters(parameters);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setPreviewImageSizeToLowest()
    {
        try{
            Camera.Parameters parameters = this.camera.getParameters();

            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            for(int i = 0; i < sizes.size(); i++)
            {
                System.out.println("size ==> "+sizes.get(i).width+" "+sizes.get(i).height);
            }
            parameters.setPreviewSize(sizes.get(sizes.size()-4).width, sizes.get(sizes.size()-4).height);

            parameters.setJpegQuality(100);
            parameters.setPreviewFormat(ImageFormat.JPEG);

            this.camera.setParameters(parameters);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setImageSizeToLow()
    {
        try{
            Camera.Parameters parameters = this.camera.getParameters();

            List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
            for(int i = 0; i < sizes.size(); i++)
            {
                System.out.println("size ==> "+sizes.get(i).width+" "+sizes.get(i).height);
            }
            parameters.setPictureSize(sizes.get(sizes.size()-1).width, sizes.get(sizes.size()-1).height);

            parameters.setJpegQuality(20);
            parameters.setPictureFormat(ImageFormat.JPEG);

            this.camera.setParameters(parameters);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Bitmap getBitmapFromJpegByteArray(byte[] data)
    {
        try{
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void refreshCamera(SurfaceHolder surfaceHolder) {
        try {
            if (surfaceHolder.getSurface() == null) return;

            camera.stopPreview();
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Parameters parameters = camera.getParameters();
                    int width = parameters.getPreviewSize().width;
                    int height = parameters.getPreviewSize().height;
                    ByteArrayOutputStream outstr = new ByteArrayOutputStream();
                    Rect rect = new Rect(0, 0, width, height);
                    YuvImage yuvimage=new YuvImage(data,ImageFormat.NV21,width,height,null);
                    yuvimage.compressToJpeg(rect, 10, outstr);

                    CameraHelper.this.data = outstr.toByteArray();

//                    System.out.println("preview callback" + CameraHelper.this.data.length+" 2");
                }
            });
            camera.startPreview();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void close()
    {
        try {
            camera.release();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
