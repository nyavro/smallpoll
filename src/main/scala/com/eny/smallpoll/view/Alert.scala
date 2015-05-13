package com.eny.smallpoll.view

import android.app.AlertDialog.Builder
import android.content.DialogInterface.OnClickListener
import android.content.{Context, DialogInterface}
import com.eny.smallpoll.R

/**
 * Created by Nyavro on 13.05.15
 */
case class Alert(title:Int) {
  def run(onApprove: ()=>Unit, onDecline: ()=>Unit = () => {})(implicit ctx:Context) = {
    new Builder(ctx)
      .setIcon(android.R.drawable.alert_light_frame)
      .setTitle(title)
      .setPositiveButton(
        R.string.dialog_ok,
        new OnClickListener() {
          override def onClick(dialog: DialogInterface, whichButton: Int) = onApprove()
        }
      )
      .setNegativeButton(
        R.string.dialog_cancel,
        new OnClickListener {
          override def onClick(dialog: DialogInterface, whichButton: Int) = onDecline()
        }
      )
      .create()
      .show()
  }
}
