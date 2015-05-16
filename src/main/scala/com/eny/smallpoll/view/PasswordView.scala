package com.eny.smallpoll.view

import android.content.Intent
import android.text.InputType
import android.view.View
import com.eny.smallpoll.R
import org.scaloid.common.{Preferences, _}

class PasswordView extends SActivity with Db {

  lazy val password = new SEditText
  lazy val error = new STextView
  lazy val login = new SButton

  onCreate {
    password.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
    password.setHint(R.string.password_hint)
    error.setText(R.string.password_error)
    error.setVisibility(View.GONE)
    login.setText(R.string.login)
    login.onClick {
      login.getText
      if(new Digest(password.text.toString).text==new Preferences(defaultSharedPreferences).password("12345")) {
        startActivity(new Intent(PasswordView.this, classOf[AdminView]))
      } else {
        Thread.sleep(2000)
        password.setText("")
        error.setVisibility(View.VISIBLE)
      }
    }
    setContentView(new SVerticalLayout += error += password += login)
  }
}