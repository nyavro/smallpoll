package com.eny.smallpoll.view.components

import android.content.Context
import android.preference.EditTextPreference
import android.util.AttributeSet

class EditIntPreference(ctx:Context, attrs:AttributeSet) extends EditTextPreference(ctx,attrs) {

  override def getPersistedString(defaultReturnValue:String):String = {
    String.valueOf(getPersistedInt(-1))
  }

  override def persistString(value:String):Boolean = {
    persistInt(Integer.valueOf(value))
  }
}
