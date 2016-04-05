package com.eny.smallpoll.view.components

import android.content.Context
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View.OnTouchListener
import android.view.{MotionEvent, View}
import android.widget.{CheckBox, TextView}
import com.eny.smallpoll.R

class EditBooleanPreference(ctx:Context, attrs:AttributeSet) extends DialogPreference(ctx,attrs) {

  setDialogLayoutResource(R.layout.boolean_edit)

  var checkbox:CheckBox = _
  var text:TextView = _
  var value:Boolean = false

  override def onBindDialogView(view:View) = {
    checkbox = view.findViewById(R.id.checkbox).asInstanceOf[CheckBox]
    checkbox.setChecked(getPersistedBoolean(false))
    text = view.findViewById(R.id.title).asInstanceOf[TextView]
    text.setText(getSummary)
    text.setOnTouchListener(
      new OnTouchListener {
        override def onTouch(v: View, event: MotionEvent): Boolean = {
          checkbox.setChecked(!checkbox.isChecked)
          false
        }
      }
    )
    super.onBindDialogView(view)
  }

  override def onDialogClosed(positive:Boolean) = {
    super.onDialogClosed(positive)
    if (positive) {
      persistBoolean(checkbox.isChecked)
    }
  }
}
