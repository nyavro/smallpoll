package com.eny.smallpoll.view

import java.security.MessageDigest

import android.content.Intent
import android.text.InputType
import com.eny.smallpoll.R
import org.scaloid.common._

class PasswordView extends SActivity with Db {

  lazy val password = new SEditText
  lazy val login = new SButton

  def digest(text: String) = MessageDigest.getInstance("MD5").digest(text.getBytes)

  onCreate {
    password.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
    password.setHint(R.string.password_hint)
    login.setText(R.string.login)
    login.onClick {
      login.getText
//      if(digest(password.text.toString)==new Preferences(defaultSharedPreferences).passwordDigest) {
        startActivity(new Intent(PasswordView.this, classOf[AdminView]))
//      } else {
//        Thread.sleep(3000)
//        password.setText("")
//      }
    }
    setContentView(new SVerticalLayout += password += login)
  }
}