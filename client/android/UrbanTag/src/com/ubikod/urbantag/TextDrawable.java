/*
 * Copyright 2012 Ubikod
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.ubikod.urbantag;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Class helper to create a drawable in order to display data on the map
 * @author cdesneuf
 */
public class TextDrawable extends Drawable
{
  /**
   * text of the drawable
   */
  private final String text;

  /**
   * Paint for text
   */
  private final Paint paint;

  /**
   * Paint for background
   */
  private final Paint backgroundPaint;

  /**
   * Constructor
   * @param text
   * @param backgroundColor
   */
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
