package com.eny.smallpoll.view.components

import java.lang

import android.app.AlertDialog
import android.content.Context
import android.graphics._
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.{MotionEvent, View}
import org.scaloid.common.Preferences

class ColorPreference(ctx:Context, attrs:AttributeSet) extends DialogPreference(ctx,attrs) {
  var color = 0
  var dialogColorChangedListener:OnColorChangedListener = _

  class ColorPickerView(listener:OnColorChangedListener, color:Int)(implicit context:Context) extends View(context) {
    val CENTER_X = 100
    val CENTER_Y = 100
    val CENTER_RADIUS = 32

    val paint = new Paint(Paint.ANTI_ALIAS_FLAG)
    private val colors: Array[Int] = Array(0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000)
    paint.setShader(new SweepGradient(0, 0, colors, null))
    paint.setStyle(Paint.Style.STROKE)
    paint.setStrokeWidth(32)
    val centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    centerPaint.setColor(color)
    centerPaint.setStrokeWidth(5)
    var trackingCenter = false

    override def onDraw(canvas:Canvas) = {
      val centerX = getRootView.getWidth/2 - (paint.getStrokeWidth/2).asInstanceOf[Int]
      val r = CENTER_X - paint.getStrokeWidth*0.5f
      canvas.translate(centerX, CENTER_Y)
      canvas.drawOval(new RectF(-r, -r, r, r), paint)
      canvas.drawCircle(0, 0, CENTER_RADIUS, centerPaint)
      if (trackingCenter) {
        val c = centerPaint.getColor
        centerPaint.setStyle(Paint.Style.STROKE)
        centerPaint.setAlpha(0x80)
        canvas.drawCircle(0, 0, CENTER_RADIUS + centerPaint.getStrokeWidth, centerPaint)
        centerPaint.setStyle(Paint.Style.FILL)
        centerPaint.setColor(c)
      }
    }
    override def onMeasure(widthMeasureSpec:Int, heightMeasureSpec:Int) = {
      val width = getRootView.getWidth
      setMeasuredDimension(
        if(width==0) {
          CENTER_X*2 + 50
        } else {
          width
        },
        CENTER_Y*2
      )
    }
    def ave(s:Int, d:Int, p:Float) = {
      s + Math.round(p * (d - s))
    }
    def interpColor(colors:Array[Int], unit:Float) = {
      if (unit <= 0) {
        colors(0)
      } else if (unit >= 1) {
        colors(colors.length - 1)
      } else {
        val p = unit * (colors.length - 1)
        val i = p.asInstanceOf[Int]
        val pp = p - i
        val c0 = colors(i)
        val c1 = colors(i + 1)
        Color.argb(
          ave(Color.alpha(c0), Color.alpha(c1), p),
          ave(Color.red(c0), Color.red(c1), p),
          ave(Color.green(c0), Color.green(c1), p),
          ave(Color.blue(c0), Color.blue(c1), p)
        )
      }
    }
    override def onTouchEvent(event:MotionEvent):Boolean = {
      val x = event.getX - getRootView.getWidth/2
      val y = event.getY - CENTER_Y
      def update() = {
        val angle = lang.Math.atan2(y, x).asInstanceOf[Float]
        val unit = {
          val v = (angle / (2 * Math.PI)).asInstanceOf[Float]
          if (v < 0) {
            v + 1
          } else {
            v
          }
        }
        centerPaint.setColor(interpColor(colors, unit))
        invalidate()
      }
      event.getAction match {
        case MotionEvent.ACTION_UP => ColorPreference.this.color = centerPaint.getColor
        case MotionEvent.ACTION_DOWN  => update()
        case MotionEvent.ACTION_MOVE => update()
      }
      true
    }
  }

  override def onDialogClosed(positiveResult:Boolean) = {
    super.onDialogClosed(positiveResult)
    if (positiveResult) {
      persistInt(color)
    }
  }

  override def onPrepareDialogBuilder(builder: AlertDialog.Builder) = {
    dialogColorChangedListener = new OnColorChangedListener() {
      override def colorChanged(c:Int) = {
        ColorPreference.this.color = c
      }
    }
    builder.setView(
      new ColorPickerView(dialogColorChangedListener, getPersistedInt(Color.BLACK))(getContext)
    )
    super.onPrepareDialogBuilder(builder)
  }
}

trait OnColorChangedListener {
  def colorChanged(color:Int):Unit
}
