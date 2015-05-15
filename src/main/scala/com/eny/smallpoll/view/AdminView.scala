package com.eny.smallpoll.view

import java.security.MessageDigest

import android.content.{Context, Intent}
import android.text.InputType
import com.eny.smallpoll.R
import org.scaloid.common._

class AdminView extends SActivity with Db {

  lazy val surveys = new SButton
  lazy val report = new SButton

  onCreate {
    surveys.setText(R.string.surveys)
    report.setText(R.string.report)
    surveys.onClick {
      new Intent().start[SurveyList]
    }
    report.onClick {
      new Intent().start[ReportSendView]
    }
    setContentView(new SVerticalLayout += surveys += report)
  }
}