package com.eny.smallpoll.view

import android.app.AlertDialog
import android.content.{Context, DialogInterface}
import android.preference.DialogPreference
import android.text.{Editable, TextWatcher}
import android.util.AttributeSet
import android.view.{MotionEvent, View}
import android.view.View.OnTouchListener
import android.widget.EditText
import com.eny.smallpoll.R
import org.scaloid.common.Preferences

/**
 * Created by eny on 15.05.15.
 */
class EditPasswordPreference(ctx:Context, attrs:AttributeSet) extends DialogPreference(ctx,attrs) {

  setDialogLayoutResource(R.layout.password_edit)

  var password:EditText = _
  var passwordCheck:EditText = _
  val DummyText = "#z№ёф?7"

  override def onBindDialogView(view:View) = {
    password = view.findViewById(R.id.password).asInstanceOf[EditText]
    passwordCheck = view.findViewById(R.id.password_check).asInstanceOf[EditText]
    password.setHint(R.string.password_hint)
    passwordCheck.setHint(R.string.password_check_hint)
    password.setText(DummyText)
    passwordCheck.setText(DummyText)
    val watcher = new TextWatcher {
      override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int): Unit = {}
      override def afterTextChanged(s: Editable): Unit = {}
      override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int): Unit = {
        val okButton = getDialog.asInstanceOf[AlertDialog].getButton(DialogInterface.BUTTON_POSITIVE)
        okButton.setEnabled(
          password.getText.toString == passwordCheck.getText.toString && !password.getText.toString.isEmpty
        )
      }
    }
    password.addTextChangedListener(watcher)
    passwordCheck.addTextChangedListener(watcher)
    class ErasingListener(edit:EditText) extends OnTouchListener {
      override def onTouch(v: View, event: MotionEvent): Boolean = {
        if(edit.getText.toString==DummyText) {
          edit.setText("")
        }
        false
      }
    }
    password.setOnTouchListener(new ErasingListener(password))
    passwordCheck.setOnTouchListener(new ErasingListener(passwordCheck))
    super.onBindDialogView(view)
  }

  override def onDialogClosed(positive:Boolean) = {
    super.onDialogClosed(positive)
    if(positive) {
      val pwd = password.getText.toString
      if (!pwd.isEmpty) {
        new Preferences(getSharedPreferences).password = new Digest(pwd).text
      }
    }
  }
}
