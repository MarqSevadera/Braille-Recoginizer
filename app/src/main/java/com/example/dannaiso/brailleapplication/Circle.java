package com.example.dannaiso.brailleapplication;



import org.opencv.core.Point;
import org.opencv.core.Rect;

public class Circle {

   private Rect boundRect;
   private Point centerPoint;

    public Circle(Rect rect){
        boundRect = rect;
        int x =  rect.x + rect.width / 2;
        int  y = rect.y + rect.height / 2;
        centerPoint = new Point(x,y);

    }

    public Rect getBoundRect() {
        return boundRect;
    }


    public Point getCenterPoint() {
        return centerPoint;
    }




}
