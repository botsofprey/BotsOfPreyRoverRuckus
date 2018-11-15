package Autonomous.ImageProcessing;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by robotics on 12/12/17.
 */

/*
    A class to process images and mainly find the columns of the cryptobox

 */
public class CryptoBoxColumnImageProcessor {
    public int imageWidth;
    public int imageHeight;
    private double percentRequiredInColumnToCheck;
    private int minimumColumnWidth;
    public static final int DESIRED_HEIGHT = 40;
    public static final int DESIRED_WIDTH = 40;
    public static final double CLOSE_UP_MIN_PERCENT_COLUMN_CHECK = 0.3;
    public static final double FAR_AWAY_MIN_PERCENT_COLUMN_CHECK = 0.1;
    public static final int CLOSE_UP_MIN_COLUMN_WIDTH = 3;
    public static final int FAR_AWAY_MIN_COLUMN_WIDTH = 1;
    public enum CRYPTOBOX_COLOR {BLUE,RED};
    CRYPTOBOX_COLOR colorToFind = CRYPTOBOX_COLOR.BLUE;


    /**
     * constructor for this class, you should not use this as you can not set the team's color to look for
     *
     * @param desiredHeight         this is an int of the image that will be processed. It is suggested that this is a rather
     *                              small number to reduce the required processing time
     * @param desiredWidth          like suggested above, keep this small to reduce processing time.
     * @param percentColumnCheck    this is the minimum required percentage of one pixel column that must be the team's color to consider
     * @param minColumnWidth        this is the minimum required number of columns to be of the required color next to each other before the algorithm concludes the columns to be a cryptobox column
     */

    public CryptoBoxColumnImageProcessor(int desiredHeight, int desiredWidth, double percentColumnCheck, int minColumnWidth){
        imageHeight = desiredHeight;
        imageWidth = desiredWidth;
        percentRequiredInColumnToCheck = percentColumnCheck;
        minimumColumnWidth = minColumnWidth;
    }

    /**
     *
     * @param desiredHeight         this is an int of the image that will be processed. It is suggested that this is a rather
     *                              small number to reduce the required processing time
     * @param desiredWidth          like suggested above, keep this small to reduce processing time.
     * @param percentColumnCheck    this is the minimum required percentage of one pixel column that must be the team's color to consider
     * @param minColumnWidth        this is the minimum required number of columns to be of the required color next to each other before the algorithm concludes the columns to be a cryptobox column
     * @param color                 This is the team's color, which is also the color cryptobox the algorithm will be looking for
     */
    public CryptoBoxColumnImageProcessor(int desiredHeight, int desiredWidth, double percentColumnCheck, int minColumnWidth, CRYPTOBOX_COLOR color){
        colorToFind = color;
        imageHeight = desiredHeight;
        imageWidth = desiredWidth;
        percentRequiredInColumnToCheck = percentColumnCheck;
        minimumColumnWidth = minColumnWidth;
    }

    /**
     * this function sets the required minimum columns that need to be adjacent and of the same color before the algorithm concludes
     * the adjacent columns are part of a cryptobox column
     * @param percentOfWidthOfImage the minimum number of adjacent columns of the team's color before being called a cryptobox column
     */

    public void setRequiredMinimumColumnWidth(double percentOfWidthOfImage){
        int widthInPixels = (int)(percentOfWidthOfImage*imageWidth + .5);
        if(widthInPixels <= 0) widthInPixels = 1;
        else if(widthInPixels >1){
            throw new RuntimeException("Wanted Column Width Greater Than Image Width!");
        }
        minimumColumnWidth = widthInPixels;
    }

    /**
     * this function sets the minimum percentage of a column of the camera's image to be the team's color before deciding the column is of
     * that color.
     * @param percentOfImageHeight  the minimum percentage of a column to be the team's color before calling the column a cryptobox column
     */

    public void setRequiredPercentOfImageColumnBlue(double percentOfImageHeight){
        if(percentOfImageHeight > 1){
            throw new RuntimeException("Wanted Column Height Percent Greater Than 1.0!");
        }
        percentRequiredInColumnToCheck = percentOfImageHeight;
    }

    /**
     * this function scales a Bitmap to the correct size
     * @param bmp the Bitmap to scale
     * @return a Bitmap at the right size
     */

    public Bitmap scaleBmp(Bitmap bmp){
        bmp = Bitmap.createScaledBitmap(bmp,imageWidth,imageHeight,true);
        Bitmap b = bmp.copy( Bitmap.Config.ARGB_8888 , true);
        return b;
    }

    private ArrayList<Integer> getColumnsWithRequiredPercentBlue(double [] blueFrequencyByColumn){
        ArrayList<Integer> interestingColumns = new ArrayList<Integer>();
        //look for columns that have the minimum required % of a color
        for(int i = 0; i < blueFrequencyByColumn.length; i++){
            if(blueFrequencyByColumn[i] >= percentRequiredInColumnToCheck){
                interestingColumns.add(Integer.valueOf(i));
            }
        }
        return interestingColumns;
    }

    private ArrayList<Integer> getColumnsWithRequiredBlueCount(int [] blueFrequencyByColumn){
        ArrayList<Integer> interestingColumns = new ArrayList<Integer>();
        //look for columns that have the minimum required % of a color
        int minimumPixelCount = (int)(percentRequiredInColumnToCheck * imageHeight + .5);
        for(int i = 0; i < blueFrequencyByColumn.length; i++){
            if(blueFrequencyByColumn[i] >= minimumPixelCount){
                interestingColumns.add(Integer.valueOf(i));
            }
        }
        return interestingColumns;
    }

    /**
     * this function determines the bounds of the cryptobox in a image
     * @param columnsWithRequiredBluePercent the indexes of columns determined to meet the minimum percentage of the team's color
     * @return an ArrayList of the start and end of the columns of the image that correspond to the start and end of the cryptobox colored columns
     */

    private ArrayList<Integer> getColumnBounds(ArrayList<Integer> columnsWithRequiredBluePercent){
        ArrayList<Integer> columnBounds = new ArrayList<Integer>();
        if(columnsWithRequiredBluePercent.size() > 0) {
            columnBounds.add(columnsWithRequiredBluePercent.get(0));
            for (int i = 1; i < columnsWithRequiredBluePercent.size(); i++) {
                if (i != 1) columnBounds.add(columnsWithRequiredBluePercent.get(i));
                while (i < columnsWithRequiredBluePercent.size() && columnsWithRequiredBluePercent.get(i).intValue() == columnsWithRequiredBluePercent.get(i - 1).intValue() + 1) {
                    i++;
                }
                columnBounds.add(columnsWithRequiredBluePercent.get(i - 1));
            }
            //look for columns to be at least ___ columns wide
            for (int i = 0; i < columnBounds.size() - 1; i += 2) {
                if (columnBounds.get(i + 1).intValue() - columnBounds.get(i).intValue() >= minimumColumnWidth) {
                } else {
                    columnBounds.remove(i);
                    columnBounds.remove(i);
                    i -= 2;
                }
            }
        }
        return columnBounds;
    }

    /**
     * this function determines the centers of the colored cryptobox columns. This can only be used if two columns are in the view of the camera.
     * as this is typically not the case, it is suggested that you use the findColumnCenters function
     * @param columnBounds the processed and determined bounds of cryptoboxes.
     * @return an ArrayList of the centers of the cryptobox, where you would score a glyph
     */

    private ArrayList<Integer> getColumnCenters(ArrayList<Integer> columnBounds) {
        ArrayList<Integer> columnCenters = new ArrayList<Integer>();
        for (int i = 0; i < columnBounds.size() / 2; i++) {
            columnCenters.add((int) (columnBounds.get(i * 2).intValue() + (columnBounds.get(i * 2 + 1).intValue() - columnBounds.get(i * 2).intValue()) / 2.0 + .5));
        }
        return columnCenters;
    }

    /**
     * This function returns the location of the colored cryptobox columns
     * @param bmp the Bitmap from the camera
     * @param shouldModifyImage whether the algorithm should modify the image as it processes it. Set this to true if you intend on saving the image
     * @return an ArrayList of the columns, relative to the width of the Bitmap, of the location of all seen cryptobox columns
     */

    public ArrayList<Integer> findColumnCenters(Bitmap bmp, boolean shouldModifyImage){
        ArrayList<Integer> columns = findColumns(bmp,shouldModifyImage);
        ArrayList<Integer> centers = new ArrayList<Integer>();
        for(int i = 0; i < columns.size() -1; i ++){
            centers.add(Integer.valueOf((int)((columns.get(i) + columns.get(i + 1))/2.0 + .5)));
        }
        return centers;
    }

    /**
     * This is the basis of the entire cryptobox processing image algorithm. This algorithm works by first determining all
     * columns that are of the team's color. This is determined through comparing the percentage of the coloumn that are blue or red to the minimum required percentage.
     * Then those columns that are blue or red are compared to columns adjacent to them to determine if there is a colored column or if it is noise.
     * @param bmp
     * @param shouldModifyImage
     * @return
     */

    public ArrayList<Integer> findColumns(Bitmap bmp, boolean shouldModifyImage){
        //Log.d("CF IMG ","Height: " + bmp.getHeight() + " Width: " + bmp.getWidth());
        if(bmp.getHeight() > imageHeight && bmp.getWidth() > imageWidth){
            bmp = scaleBmp(bmp);
        }
        ArrayList<Integer> columnCenters = new ArrayList<Integer>();
        int width = bmp.getWidth(), height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        long collapseStart = System.currentTimeMillis();
        int [] frequencyByColumn = null;
        if(colorToFind == CRYPTOBOX_COLOR.BLUE) frequencyByColumn = collapseVerticallyByBlueCount(pixels,width,height);
        else if(colorToFind == CRYPTOBOX_COLOR.RED) frequencyByColumn = collapseVerticallyByRedCount(pixels,width,height);
        //Log.d("CF IMG PROC", "Collapse Time: " + (System.currentTimeMillis() - collapseStart));
        ArrayList<Integer> interestingColumns = getColumnsWithRequiredBlueCount(frequencyByColumn);
        ArrayList<Integer> columnBounds = getColumnBounds(interestingColumns);
//        for (int i = 0; i < columnBounds.size(); i++) {
//            Log.d("Bound", columnBounds.get(i).toString());
//        }
        //get average column locations
        columnCenters = getColumnCenters(columnBounds);
//        for (int i = 0; i < columnCenters.size(); i++) {
//            Log.d("Centers", columnCenters.get(i).toString());
//        }
//        Log.d("# of Columns", "" + columnCenters.size());
        if(shouldModifyImage){
            if(colorToFind == CRYPTOBOX_COLOR.BLUE) showBluePixels(pixels,height,width, Color.GREEN);
            else if(colorToFind == CRYPTOBOX_COLOR.RED)
            showColumnCenters(pixels,height,width,columnCenters,Color.RED);
            bmp.setPixels(pixels,0,width,0,0,width,height);
        }
        return columnCenters;
    }

    /**
     * This function modifies the pixels to show where the center of the colored columns are
     */

    private void showColumnCenters(int [] pixels, int height, int width, ArrayList<Integer> columnCenters, int colorToShowWith){
        for (int i = 0; i < columnCenters.size(); i++) {
            for (int r = 0; r < height; r++) {
                pixels[r * width + columnCenters.get(i).intValue()] = colorToShowWith;
            }
        }
    }


    /*
        this function will show the blue pixels in an image
     */
    public void showBluePixels(int [] pixels, int height, int width, int colorToReplaceWith){
        for (int c = 0; c < width; c++) {
            int numberOfBluePixels = 0;
            for (int r = 0; r < height; r++) {
                int color = pixels[r * width + c];
                int[] rgba = {Color.red(color), Color.blue(color), Color.green(color), Color.alpha(color)};
                float[] hsv = new float[3];
                Color.colorToHSV(color, hsv);
                //check for blue
                if (checkIfBlue(hsv)) {
                    rgba[0] = 0;
                    rgba[1] = 250;
                    rgba[2] = 250;
                    pixels[r * width + c] = colorToReplaceWith;
                    numberOfBluePixels++;
                }
            }
        }
    }


    public void showRedPixels(int [] pixels, int height, int width, int colorToReplaceWith){
        for (int c = 0; c < width; c++) {
            int numberOfBluePixels = 0;
            for (int r = 0; r < height; r++) {
                int color = pixels[r * width + c];
                int[] rgba = {Color.red(color), Color.blue(color), Color.green(color), Color.alpha(color)};
                float[] hsv = new float[3];
                Color.colorToHSV(color, hsv);
                //check for blue
                if (checkIfRed(hsv)) {
                    rgba[0] = 0;
                    rgba[1] = 250;
                    rgba[2] = 250;
                    pixels[r * width + c] = colorToReplaceWith;
                    numberOfBluePixels++;
                }
            }
        }
    }

    public int [] collapseVerticallyByBlueCount(int [] pixels, int imageWidth, int imageHeight){
        //collapse into a single, frequency based
        int [] toReturn = new int[imageWidth];
        for(int c = 0; c < imageWidth; c ++){
            for(int r = 0; r < imageHeight; r ++){
                int color = pixels[r*imageWidth + c];
                if(checkIfBlue(color)){
                    toReturn[c] ++;
                }
            }
        }
        return toReturn;
    }

    public int [] collapseVerticallyByRedCount(int [] pixels, int imageWidth, int imageHeight){
        //collapse into a single, frequency based
        int [] toReturn = new int[imageWidth];
        for(int c = 0; c < imageWidth; c ++){
            for(int r = 0; r < imageHeight; r ++){
                int color = pixels[r*imageWidth + c];
                if(checkIfRed(color)){
                    toReturn[c] ++;
                }
            }
        }
        return toReturn;
    }

    public boolean checkIfBlue(float [] hsl){
        if (hsl[0] > 180 && hsl[0] < 300) {
            //make sure it's not a white blue
            if (hsl[1] > .5) {
                //make sure it's not a black blue
                if (hsl[2] > .2) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean checkIfRed(float [] hsl){
        if (hsl[0] > 320 || hsl[0] < 40) {
            //make sure it's not a white red
            if (hsl[1] > .7) {
                //make sure it's not a black red
                if (hsl[2] > .3) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean checkIfBlue(int color){
        float [] hsl = new float[3];
        Color.colorToHSV(color,hsl);
        return checkIfBlue(hsl);
    }
    
    public boolean checkIfRed(int color){
        float [] hsl = new float[3];
        Color.colorToHSV(color,hsl);
        return checkIfRed(hsl);
    }
}
