package Autonomous.OpModes;

import android.graphics.Bitmap;
import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.util.ArrayList;

import Autonomous.ImageProcessing.CryptoBoxColumnImageProcessor;
import Autonomous.VuforiaHelper;

/**
 * Created by root on 11/20/17.
 */

/*
    An opmode to test saving images using vuforia
 */
@Autonomous(name="Save Image Test", group="Linear Opmode")  // @Autonomous(...) is the other common choice
@Disabled
public class VuforiaImageCaptureTest extends LinearOpMode {

    CryptoBoxColumnImageProcessor cryptoFinder;
    @Override
    public void runOpMode() throws InterruptedException {
        int imageTaken = 0;
        CryptoBoxColumnImageProcessor.CRYPTOBOX_COLOR color = CryptoBoxColumnImageProcessor.CRYPTOBOX_COLOR.BLUE;
        /*To access the image: you need to iterate through the images of the frame object:*/
        VuforiaHelper vuforia = new VuforiaHelper(hardwareMap);
        //wait for the op mode to start, this is the time to change teams
        while (!opModeIsActive()) {
            if (gamepad1.start) {
                if(color == CryptoBoxColumnImageProcessor.CRYPTOBOX_COLOR.BLUE) color = CryptoBoxColumnImageProcessor.CRYPTOBOX_COLOR.RED;
                else color = CryptoBoxColumnImageProcessor.CRYPTOBOX_COLOR.BLUE;
                while (gamepad1.start) ;
            }
            telemetry.addData("Color", color);
            telemetry.update();
        }
        //initialize the image processor 
        cryptoFinder = new CryptoBoxColumnImageProcessor(CryptoBoxColumnImageProcessor.DESIRED_HEIGHT, CryptoBoxColumnImageProcessor.DESIRED_WIDTH,.1,1, color);
        telemetry.addData("Status","Initialized");

        waitForStart();
        //storage variables 
        Bitmap bmp;
        ArrayList<Integer> columnLocations = new ArrayList<Integer>();
        while(opModeIsActive()){
            long timeStart = System.currentTimeMillis();
            //get an image 
            bmp = vuforia.getImage(CryptoBoxColumnImageProcessor.DESIRED_WIDTH, CryptoBoxColumnImageProcessor.DESIRED_HEIGHT);
            if(bmp != null){
                long algorithmStart = System.currentTimeMillis();
                //find the columns 
                columnLocations = cryptoFinder.findColumns(bmp,true);
                telemetry.addData("Algorithm Time", "" + (System.currentTimeMillis() - algorithmStart));
                //for every column seen, print its location 
                if(columnLocations != null){
                    for(int i = 0; i < columnLocations.size(); i ++){
                        telemetry.addData("Column " + i, " " + columnLocations.get(i).intValue());
                    }
                }
                //save every tenth image 
                if(imageTaken == 50) {
                    vuforia.saveBMP(bmp); // save edited image
                    imageTaken = 0; 
                }
                imageTaken++;
                telemetry.addData("Loop Time", "" + (System.currentTimeMillis() - timeStart));
                telemetry.update();
            }
            else{
                Log.d("BMP","NULL!");
            }
        }

    }







}
