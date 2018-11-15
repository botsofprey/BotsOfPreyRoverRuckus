package Autonomous;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;

import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by robotics on 12/12/17.
 */

/*
    A class to help us use vuforia more easily
    Keep in mind to call the loadCipherAssets function if you wish to use this for crypto-key image detection
    The
 */
public class VuforiaHelper {

    VuforiaLocalizer vuLoc;
    VuforiaTrackables relicTrackables;
    VuforiaTrackable relicTemplate;
    private final float UPRIGHT_POST_ROTATE_IN_DEG = 270;
    private final float HORIZONTAL_WITH_CAMERA_TO_LEFT_POST_ROTATE_IN_DEG = 180;

    public VuforiaHelper(){
        try {
            VuforiaLocalizer.Parameters params = new VuforiaLocalizer.Parameters(R.id.cameraMonitorViewId);
            params.vuforiaLicenseKey = "Afh+Mi//////AAAAGT/WCUCZNUhEt3/AvBZOSpKBjwlgufihL3d3H5uiMfbq/1tDOM6w+dgMIdKUvVFEjNNy9zSaruPDbwX0HwjI6BEvxuWbw+UcZFcfF7i4g7peD4zSCEyZBCi59q5H/a2aTsnJVaG0WO0pPawHDuuScrMsA/QPKQGV/pZOT6rK8cW2C3bEkZpZ1qqkSM5zNeKs2OQtr8Bvl2nQiVK6mQ3ZT4fxWGb7P/iTZ4k1nEhkxI56sr5HlxmSd0WOx9i8hYDTJCASU6wwtOeUHZYigZmdRYuARS+reLJRXUylirmoU8kVvMK1p2Kf8dajEWsTuPwBec/BSaygmpqD0WkAc2B1Vmaa/1zTRfYNR3spIfjHQCYu";
            params.cameraDirection = VuforiaLocalizer.CameraDirection.FRONT;
            vuLoc = ClassFactory.createVuforiaLocalizer(params);
            Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true); //enables RGB565 format for the image
            vuLoc.setFrameQueueCapacity(1); //tells VuforiaLocalizer to only store one frame at a time

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public  void kill(){
        //Vuforia.deinit();
        //vuLoc.
    }

    /*
        loadCipherAssets
        tells vuforia to begin tracking cryptokeys
     */

    public void loadCipherAssets(){
        relicTrackables = vuLoc.loadTrackablesFromAsset("RelicVuMark");
        relicTemplate = relicTrackables.get(0);
        relicTrackables.activate();
    }


    /*
        getMark()
        returns a RelicRecoveryVuMark based on what the camera sees
     */
    public RelicRecoveryVuMark getMark(){
        RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
        return vuMark;
    }


    /*
        getImage
        wantedWidth -- of the image to return
        wantedHeight -- of the image to return
        returns a Bitmap of what the camera sees
     */
    public Bitmap getImage(int wantedWidth, int wantedHeight) {
        Image i = null;
        long timeStart = System.currentTimeMillis();
        try{
            i = takeImage();

        }catch (Exception e){
            throw new RuntimeException(e);
        }
        //Log.d("VH IMG TAKE TIME", "" + (System.currentTimeMillis() - timeStart));

        if(i != null) {
            long conversionStart = System.currentTimeMillis();
            Bitmap bmp = convertImageToBmp(i);
            //Log.d("VH IMG Convert", "" + (System.currentTimeMillis() - conversionStart));
            long copyStart = System.currentTimeMillis();
            Bitmap orig = bmp.copy(Bitmap.Config.ARGB_8888,true);
            //Log.d("VH IMG ORIG","Height: " + orig.getHeight() + " Width: " + orig.getWidth());
            //Log.d("VH IMG CPY", "" + (System.currentTimeMillis() - copyStart));
            long scaleStart = System.currentTimeMillis();
            Matrix matrix = new Matrix();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(orig,wantedWidth,wantedHeight,true);
            matrix.postRotate(UPRIGHT_POST_ROTATE_IN_DEG);
            //Log.d("VH IMG Scale", "" + (System.currentTimeMillis() - scaleStart));
            long rotationStart = System.currentTimeMillis();
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
            rotatedBitmap = Bitmap.createScaledBitmap(rotatedBitmap,wantedWidth,wantedHeight,true);
           //Log.d("VH IMG Rotation", "" + (System.currentTimeMillis() - rotationStart));
            return rotatedBitmap;
        }
        return null;
    }

    /*
        convertImageToBmp
        requires and Image of what to convert
        returns the Bitmap of the Image
     */
    private Bitmap convertImageToBmp(Image img){
        Bitmap bmp = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.RGB_565);
        bmp.copyPixelsFromBuffer(img.getPixels());
        return bmp;
    }


    /*
        takeImage()
        this function gets vuforia to return an Image of what it sees
     */
    private Image takeImage() throws InterruptedException{
        Image img = null;
        VuforiaLocalizer.CloseableFrame frame = vuLoc.getFrameQueue().take(); //takes the frame at the head of the queue
        long numImages = frame.getNumImages();
        for (int i = 0; i < numImages; i++) {
            //Log.d("Format","" + frame.getImage(i).getFormat());
            if (frame.getImage(i).getFormat() == PIXEL_FORMAT.RGB565) {
                img = frame.getImage(i);
                break;
            }
        }
        return img;
    }


    /*
        saveBMP
        requires a Bitmap of what to save to the local storage

        saves a BMP to the phone, you can find it in the root directory
     */
    public void saveBMP(Bitmap bmp){

        FileOutputStream out = null;
        try {

            File yourFile = new File(Environment.getExternalStorageDirectory().toString() + "/robot" + System.currentTimeMillis() + ".png");
            yourFile.createNewFile(); // if file already exists will do nothing
            out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/robot" + System.currentTimeMillis() + ".png",false);
            //Log.d("Saving",out.toString());
            bmp.compress(Bitmap.CompressFormat.PNG, 10, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
