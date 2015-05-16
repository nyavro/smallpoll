package com.eny.smallpoll.view

import android.app.{Activity, AlertDialog}
import android.os.Bundle
import android.view.{MotionEvent, Gravity, WindowManager}
import android.widget.FrameLayout

/**
 * Created by eny on 16.05.15.
 */
class Lock(activity:Activity) {
  new OverlayDialog(activity).show()
  
  class OverlayDialog(activity:Activity) extends AlertDialog(activity) {
    val params = getWindow.getAttributes
    params.`type` = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
    params.dimAmount = 0.0f
    params.width = 0
    params.height = 0
    params.gravity = Gravity.BOTTOM
    getWindow.setAttributes(params)
    getWindow.setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, 0xffffff)
    setOwnerActivity(activity)
    setCancelable(false)

    override def dispatchTouchEvent(motionevent:MotionEvent) = true

    override def onCreate(bundle:Bundle) = {
      super.onCreate(bundle)
      val framelayout = new FrameLayout(getContext)
      framelayout.setBackgroundColor(0)
      setContentView(framelayout)
    }
  }
}
