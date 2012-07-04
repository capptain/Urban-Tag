package com.ubikod.urbantag;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class TextDrawable extends Drawable
{

  private final String text;
  private final Paint paint;
  private final Paint backgroundPaint;

  public TextDrawable(String text, int backgroundColor)
  {

    this.text = text;

    this.paint = new Paint();
    paint.setColor(Color.WHITE);
    paint.setTextSize(25f);
    paint.setAntiAlias(true);
    paint.setFakeBoldText(true);
    paint.setShadowLayer(6f, 0, 0, Color.TRANSPARENT);
    paint.setStyle(Paint.Style.FILL);
    paint.setTextAlign(Paint.Align.LEFT);

    this.backgroundPaint = new Paint();
    backgroundPaint.setColor(backgroundColor);
    backgroundPaint.setStyle(Paint.Style.FILL);

    backgroundPaint.setShadowLayer(4f, 0, 0, Color.GRAY);
  }

  @Override
  public void draw(Canvas canvas)
  {
    Rect rect = new Rect();
    paint.getTextBounds(text, 0, text.length(), rect);
    rect.inset(-5, -5);
    canvas.drawRect(rect, backgroundPaint);
    canvas.drawText(text, 0, 0, paint);
    this.setBounds(rect);
  }

  @Override
  public void setAlpha(int alpha)
  {
    paint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter cf)
  {
    paint.setColorFilter(cf);
  }

  @Override
  public int getOpacity()
  {
    return PixelFormat.TRANSLUCENT;
  }
}