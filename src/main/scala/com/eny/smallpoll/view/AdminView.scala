package com.eny.smallpoll.view

import android.content.Intent
import android.view.{Menu, MenuItem}
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

  override def onCreateOptionsMenu(menu:Menu):Boolean = {
    getMenuInflater.inflate(R.menu.main, menu)
    true
  }

  override def onOptionsItemSelected(item:MenuItem):Boolean =
    item.getItemId match {
      case R.id.preferences => new Intent().start[PreferencesView];true
      case _ => super.onOptionsItemSelected(item)
    }
}