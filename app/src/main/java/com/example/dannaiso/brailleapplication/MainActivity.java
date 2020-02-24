package com.example.dannaiso.brailleapplication;

import android.content.pm.ActivityInfo;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.constraint.solver.widgets.Rectangle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.FONT_HERSHEY_COMPLEX_SMALL;
import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.HoughCircles;
import static org.opencv.imgproc.Imgproc.circle;
import static org.opencv.imgproc.Imgproc.cvtColor;

import org.opencv.core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2,TextToSpeech.OnInitListener {


    CameraBridgeViewBase cameraBridgeViewBase;

    Mat mat1, mat2, mat3;
    BaseLoaderCallback baseLoaderCallback;

    private ArrayList<Circle> circleList;

    private ArrayList<CharMatrix> charMatrices;

    private Point bot1,bot2,top1,top2,left1,right1;

    private TextToSpeech tts;
    int res;
    boolean available = true;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        circleList = new ArrayList<>();
        charMatrices = new ArrayList<>();
        Braille.init();
        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.myCameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;

                }
            }
        };

        tts = new TextToSpeech(MainActivity.this,this);


    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mat1 = inputFrame.rgba();
        Mat imgGrayscale = new Mat();
        imgGrayscale = performBlur(imgGrayscale);
        try{
            TimeUnit.MILLISECONDS.sleep(100);
        }catch (InterruptedException e){e.printStackTrace();}


        if(available) {
            available = false;
            //Guassian Blur

            //Canny
            int threshold = 100;
            Mat cannyOutput = new Mat();
            Imgproc.Canny(imgGrayscale, cannyOutput, threshold, threshold * 2);

            //Get Contours
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            circleList.clear();
            Point rectCenter = null;

            for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {

                MatOfPoint2f approxCurve = new MatOfPoint2f();
                MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(contourIdx).toArray());
                double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);


                MatOfPoint points = new MatOfPoint(approxCurve.toArray());

                // Get bounding rect of contour
                Rect rect = Imgproc.boundingRect(points);

                // Get the center of the rectangle of the contour
                rectCenter = new Point((rect.x + rect.width / 2), (rect.y + rect.height / 2));

                //only consider detected circles with width or height > 3 but less than 7
                if ((rect.width > 3  || rect.height > 3) && (rect.width < 10 || rect.height < 10)) {
                    Scalar white = new Scalar(255, 255, 255);
                    Imgproc.circle(imgGrayscale, rectCenter, 3, white, 6);
                    //if(findPoint(rectCenter))continue;
                    if(isOverlapping(rect)) continue;
                    circleList.add(new Circle(rect));
                    Log.i("Width:", " " + rect.width);
                }

            }

            Log.i("The Size: ", " " + circleList.size());
            if (circleList.size() > 0) {
                sortCircleList();
                createMatrices();
                String result = "";
                if(!charMatrices.isEmpty()) {
                    result = Braille.parseMatrix(charMatrices);
                    Log.i("WORD", result);
                    if(TextUtils.isEmpty(result)) {
                        available = true;
                        return mat1;
                    }
                    Bundle params = new Bundle();
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
                    tts.speak(result, TextToSpeech.QUEUE_FLUSH, params, "UniqueID");
                }else{
                    available = true;
                }

                Imgproc.putText(imgGrayscale,result, new Point(0,mat1.height()/2),
                        FONT_HERSHEY_COMPLEX_SMALL, 3, new Scalar(255,8,0), 3);

            }else{
                available = true;
            }
        }

        return imgGrayscale;
    }



    @Override
    public void onCameraViewStopped() { mat1.release(); }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mat1 = new Mat(width,height,CV_8UC4);
        mat2 = new Mat(width,height,CV_8UC4);
        mat3 = new Mat(width,height,CV_8UC4);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null)
            cameraBridgeViewBase.disableView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug())
            Toast.makeText(getApplicationContext(), "There is  problem in OpenCV", Toast.LENGTH_SHORT).show();
        else
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }

    private Mat performBlur(Mat imgGrayscale){
        Imgproc.cvtColor(mat1, imgGrayscale, Imgproc.COLOR_BGR2GRAY);

        Imgproc.GaussianBlur(imgGrayscale, imgGrayscale, new Size(5, 5), 0);
        Imgproc.adaptiveThreshold(imgGrayscale, imgGrayscale, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 4);

        Imgproc.medianBlur(imgGrayscale, imgGrayscale, 5);
        Imgproc.threshold(imgGrayscale, imgGrayscale, 0, 255, Imgproc.THRESH_OTSU);

        return imgGrayscale;
    }

    private void sortCircleList(){
        Collections.sort(circleList, new Comparator<Circle>() {
            @Override
            public int compare(Circle c1, Circle c2) {
                double y1 = c1.getCenterPoint().y;
                double y2 = c2.getCenterPoint().y;
                double x1 = c1.getCenterPoint().x;
                double x2 = c2.getCenterPoint().x;
                if(y1 < y2 ){
                    return -1;
                }else{
                    if(y1 == y2){
                        if(x1 < x2)return -1;
                        else return 0;
                    }else{
                        return 1;
                    }
                }
            }
        });
    }

    private Circle getFirstCircle(Point start){
        Circle first = null;
        double lowestDistance = 100000;//highest possible para safe
        for(Circle circle : circleList){
            double distanceFromStart =  getDistance(start,circle.getCenterPoint());
            if(distanceFromStart < lowestDistance){
                lowestDistance = distanceFromStart;
                first = circle;
            }
        }
        return first;
    }

    private double getDistanceFromOrigin(Point point){
        double x =  Math.pow(point.y - 0 , 2);
        double y = Math.pow(point.x - 0 , 2);
        return Math.sqrt(x+y);

    }

    private void createMatrices(){

            //get the first circle to process (nearest to origin (0,0))
            Circle firstCircle = getFirstCircle(new Point(0,0));
            double dist = getLowestDistanceFromCircleList();
            //immidiately remove it from list

            //the lowest distance from the circle will serve as the
            //distance to its neighboring circle
            //double dist = getLowestDistanceFrom(firstCircle.getCenterPoint());

            Log.i("LOWEST DISTANCE:",""+dist);
            //this will store the equivalent matrix of the circles
            int matChar[][];
            charMatrices.clear();

            int size = circleList.size();
            for(int i = 0; i < size; i++) {
                if(circleList.size() == 0)break;

                //check muna ung taas ng first circle kung walang laman
                double x = firstCircle.getCenterPoint().x;
                double y = firstCircle.getCenterPoint().y;
                Point topLeft = moveUp(firstCircle.getCenterPoint(),dist);
                Point topRight = moveRight(topLeft,dist);

                if(findPoint(topLeft) || findPoint(topRight)){
                    topLeft = moveUp(topLeft,dist);
                    topRight = moveRight(topLeft,dist);
                    //check ulit kung meron sa taas
                    if(findPoint(topLeft) || findPoint(topRight)){
                        //third pattern
                        right1 = new Point(firstCircle.getCenterPoint().x + dist, firstCircle.getCenterPoint().y);
                        top1 = new Point(right1.x, right1.y + dist);
                        top2 = new Point(top1.x, top1.y - dist);
                        left1 = new Point(top2.x - dist, top2.y);
                        bot1 = new Point(left1.x, left1.y + dist);
                        bot2 = new Point(bot1.x, bot1.y + dist);
                        //kapag ung last step(bot2)parehas ng first Circle ibig sabihin sa pangatlo sya
                        if (firstCircle.getBoundRect().contains(bot2)) {
                            matChar = new int[3][2];
                            circleList.remove(firstCircle);
                            matChar[2][0] = 1;
                            matChar[2][1] = findPointAndRemove(right1);
                            matChar[1][1] = findPointAndRemove(top1);
                            matChar[0][1] = findPointAndRemove(top2);
                            matChar[0][0] = findPointAndRemove(left1);
                            matChar[1][0] = findPointAndRemove(bot1);
                            charMatrices.add(new CharMatrix(matChar));
                            if(circleList.size() == 0) break;
                            firstCircle = getFirstCircle(top2);
                            continue;
                        }
                    }else{
                        //second pattern
                        bot1 = new Point(firstCircle.getCenterPoint().x, firstCircle.getCenterPoint().y + dist);
                        right1 = new Point(bot1.x + dist, bot1.y);
                        top1 = new Point(right1.x, right1.y - dist);
                        top2 = new Point(top1.x, top1.y - dist);
                        left1 = new Point(top2.x - dist, top2.y);
                        bot2 = new Point(left1.x, left1.y + dist);
                        //kapag ung last step(bot 2) parehas ng first Circle ibig sabihin sa pangalawa sya
                        if (firstCircle.getBoundRect().contains(bot2)) {
                            matChar = new int[3][2];
                            circleList.remove(firstCircle);
                            matChar[1][0] = 1;
                            matChar[2][0] = findPointAndRemove(bot1);
                            matChar[2][1] = findPointAndRemove(right1);
                            matChar[1][1] = findPointAndRemove(top1);
                            matChar[0][1] = findPointAndRemove(top2);
                            matChar[0][0] = findPointAndRemove(left1);
                            charMatrices.add(new CharMatrix(matChar));
                            firstCircle = getFirstCircle(top2);
                            if(circleList.size() == 0) break;
                        }
                    }
                }else{
                    //first pattern
                    bot1 = new Point(firstCircle.getCenterPoint().x, firstCircle.getCenterPoint().y + dist);
                    bot2 = new Point(bot1.x, bot1.y + dist);
                    right1 = new Point(bot2.x + dist, bot2.y);
                    top1 = new Point(right1.x, right1.y - dist);
                    top2 = new Point(top1.x, top1.y - dist);
                    left1 = new Point(top2.x - dist, top2.y);
                    //kapag ung last step(left 1) parehas ng first Circle ibig sabihin sa una sya
                    if (firstCircle.getBoundRect().contains(left1)) {
                        Log.i("CIRCLE LEFT:" , circleList.size()+"");
                        matChar = new int[3][2];
                        circleList.remove(firstCircle);
                        matChar[0][0] = 1;
                        matChar[1][0] = findPointAndRemove(bot1);
                        matChar[2][0] = findPointAndRemove(bot2);
                        matChar[2][1] = findPointAndRemove(right1);
                        matChar[1][1] = findPointAndRemove(top1);
                        matChar[0][1] = findPointAndRemove(top2);
                        charMatrices.add(new CharMatrix(matChar));
                        if(circleList.size() == 0) break;
                        firstCircle = getFirstCircle(top2);
                    }
                }
            }

            Log.i("TOTAL MATRIX", charMatrices.size() + "");

    }

    private int findPointAndRemove(Point point){
        for(Circle circle : circleList){
            if(circle.getBoundRect().contains(point)){
                circleList.remove(circle);
                Log.i("REMOVED" , "Left: " + circleList.size());
                return 1;
            }
        }
        return 0;
    }

    private double getLowestDistanceFrom(Point point){
        double lowest = 1000 ;
        for(Circle circle: circleList){
            double dist = getDistance(circle.getCenterPoint() , point);
            if(dist < lowest){
                lowest = dist;
            }
        }
        return lowest;
    }

    private double getDistance(Point a, Point b){
        double x =  Math.pow(a.x - b.x, 2);
        double y = Math.pow(a.y - b.y , 2);
        return Math.sqrt(x+y);
    }

    private double getLowestDistanceFromCircleList(){
        double min = 10000;
        for(int i = 0; i < circleList.size(); i++){
            for(int j = i+1; j < circleList.size();j++){
                double distance = getDistance(circleList.get(i).getCenterPoint() , circleList.get(j).getCenterPoint());
                Log.i("DISTANSYA", "" + distance);
                if(distance < min)
                    min = distance;
            }
        }
        return min;
    }

    private Point moveUp(Point point,double distance){
        return new Point(point.x,point.y - distance);
    }
    private Point moveRight(Point point, double distance){
        return new Point(point.x + distance , point.y);
    }
    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            res = tts.setLanguage(Locale.US);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) { }

                @Override
                public void onDone(String utteranceId) {
                    available = true;
                }

                @Override
                public void onError(String utteranceId) {available = true;}
            });
        }else{
           Toast.makeText(this,"Feature not supported!",Toast.LENGTH_SHORT).show();
        }
    }


    private boolean checkOverlaps(Rect rect1 , Rect rect2){
        return rect1.x < rect2.x + rect2.width && rect1.x + rect1.width> rect2.x && rect1.y < rect2.y + rect2.height && rect1.y + rect1.height > rect2.y;
    }
    private boolean findPoint(Point point){

        for(Circle circle : circleList){
            if(circle.getBoundRect().contains(point)){
                return true;
            }
        }
        return false;
    }

    private boolean isOverlapping(Rect rect1){
        for(Circle circle : circleList){
            if(checkOverlaps(rect1,circle.getBoundRect()))return true;
        }
        return false;
    }
}

