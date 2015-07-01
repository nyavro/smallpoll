package com.eny.smallpoll.view

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager.LayoutParams
import android.view.{Gravity, View, ViewGroup, WindowManager}
import android.widget.RelativeLayout

class SystemLock(activity:Activity) {

  private lazy val lock1: Lock = new Lock(activity)

  lazy val params = {
    val res = new LayoutParams(
      WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
      PixelFormat.TRANSPARENT
    )
    res.gravity = Gravity.TOP
    res.width = ViewGroup.LayoutParams.MATCH_PARENT
    res.height = (50 * activity.getResources.getDisplayMetrics.scaledDensity).toInt
    res
  }

  lazy val layout = new RelativeLayout(activity)

  var locked = false

  def lock() = if(!locked) {
      lock1.lock()
      activity.getWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
      activity.getWindow.getDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN)
      activity.getActionBar.hide()
      activity.getApplicationContext
        .getSystemService(Context.WINDOW_SERVICE)
        .asInstanceOf[WindowManager]
        .addView(
          layout,
          params
        )
      locked = true
    }

  def unlock() = if(locked) {
      lock1.unlock()
      activity.getWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
      activity.getWindow.getDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE)
      activity.getActionBar.show()
      activity.getApplicationContext
        .getSystemService(Context.WINDOW_SERVICE)
        .asInstanceOf[WindowManager]
        .removeView(layout)
      locked = false
    }
}
