package com.eny.smallpoll.view

import android.content.Intent
import android.graphics.Color
import android.text.InputType
import android.view.View
import com.eny.smallpoll.R
import org.scaloid.common.{Preferences, _}

class LoginView extends SActivity with Db {

  lazy val password = new SEditText
  lazy val error = new STextView
  lazy val login = new SButton

  onCreate {
    password.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
    password.setHint(R.string.password_hint)
    error.setText(R.string.password_error)
    error.setVisibility(View.GONE)
    error.setTextColor(Color.RED)
    login.setText(R.string.login)
    login.onClick {
      if(new Digest(password.text.toString).text==new Preferences(defaultSharedPreferences).password("12345")) {
        startActivity(new Intent(LoginView.this, classOf[AdminView]))
      } else {
        Thread.sleep(2000)
        password.setText("")
        error.setVisibility(View.VISIBLE)
      }
    }
    setContentView(new SVerticalLayout += error += password += login)
  }
}