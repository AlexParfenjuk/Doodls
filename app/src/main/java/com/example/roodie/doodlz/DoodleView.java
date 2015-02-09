package com.example.roodie.doodlz;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by Roodie on 19.01.2015.
 */
public class DoodleView extends View {

    private static final float TOUCH_TOLERANCE = 10;
     private Bitmap bitmap; // область для отображения или сохранения
     private Canvas bitmapCanvas; // рисование на растре
     private Paint paintScreen; // рисование растра на экране
     private Paint paintLine; // рисование линий на растре
     private HashMap<Integer,Path> pathMap; // рисование текущего Paths
     private HashMap<Integer,Point> previousPointMap; // текущие Points

    //


    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paintScreen = new Paint();

        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.BLACK);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(5);
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        pathMap = new HashMap<Integer, Path>();
        previousPointMap = new HashMap<Integer, Point>();


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);
    }



    public void clear() {

        pathMap.clear();
        previousPointMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }

    public int getDrawingColor() {
        return paintLine.getColor();
    }

    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    public int getLineWidth() {
        return  (int)paintLine.getStrokeWidth();
    }





    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap,0,0,paintScreen);

        for(Integer key : pathMap.keySet())
            canvas.drawPath(pathMap.get(key),paintLine);
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();

        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(actionIndex),event.getY(actionIndex),event.getPointerId(actionIndex));
        }
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            touchEnded(event.getPointerId(actionIndex));

        }
        else {
            touchMoved(event);
        }
        invalidate();
        return true;
    }



    private void touchStarted(float x, float y, int lineID) {
        Path path;
        Point point;

        if(pathMap.containsKey(lineID)) {
            path = pathMap.get(lineID);
            path.reset();
            point = previousPointMap.get(lineID);
        }
        else {
            path = new Path();
            pathMap.put(lineID,path);
            point = new Point();
            previousPointMap.put(lineID,point);
        }
        path.moveTo(x,y);
        point.x = (int) x;
        point.y = (int) y;
    }



    private void touchMoved(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerID = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerID);
            if (pathMap.containsKey(pointerID)) {
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathMap.get(pointerID);
                Point point = previousPointMap.get(pointerID);

                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                if(deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    path.quadTo(point.x,point.y, (newX + point.x)/ 2, (newY + point.y)/ 2);

                    point.x = (int)newX;
                    point.y = (int)newY;
                }
            }
        }
    }



    private void touchEnded(int lineID) {
        Path path = pathMap.get(lineID);
        bitmapCanvas.drawPath(path,paintLine);
        path.reset();
    }



    public void saveImage()
     {
         // воспользуйтесь "Doodlz" с показаниями времени в качестве
// имени файла изображения
         String fileName = "Doodlz" + System.currentTimeMillis();

         ContentValues values = new ContentValues();
         values.put(MediaStore.Images.Media.TITLE, fileName);
         values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
         values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");

         // получение Uri местоположения, используемого для хранения файлов
         Uri uri = getContext().getContentResolver().insert(
                 MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

         try
         {
             // получение OutputStream для uri
             OutputStream outStream =
                     getContext().getContentResolver().openOutputStream(uri);

             // копирование растра в OutputStream
             bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

             // очистка и закрытие потока OutputStream
             outStream.flush(); // очистка буфера
             outStream.close(); // закрытие потока

             // отображение сообщения о сохранении изображения
             Toast message = Toast.makeText(getContext(),
                     R.string.message_saved, Toast.LENGTH_SHORT);
             message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                     message.getYOffset() / 2);
             message.show(); // отображение сообщения Toast
             } // конец определения блока try
         catch (IOException ex)
         {
             // отображение сообщения о сохранении изображения
             Toast message = Toast.makeText(getContext(),
                     R.string.message_error_saving, Toast.LENGTH_SHORT);
             message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                     message.getYOffset() / 2);
             message.show();
// отображение сообщения Toast
             } // конец блока catch
         } // конец описания метода saveImage
     } // конец описания класса DoodleView

