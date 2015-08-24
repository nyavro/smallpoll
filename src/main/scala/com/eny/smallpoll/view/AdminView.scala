package com.eny.smallpoll.view

import android.content.Intent
import android.view.{Menu, MenuItem}
import com.eny.smallpoll.R
import org.scaloid.common._

class AdminView extends SActivity with Db {

  lazy val surveys = new SButton(getString(R.string.surveys))
  lazy val report = new SButton(getString(R.string.report))
  lazy val export = new SButton(getString(R.string.export))
  lazy val imprt = new SButton(getString(R.string.imprt))

  onCreate {
    surveys.onClick {
      new Intent().start[SurveyListView]
    }
    report.onClick {
      new Intent().start[ReportSendView]
    }
    export.onClick {
      new Intent().start[ExportView]
    }
    imprt.onClick {
      new Intent().start[ImportView]
    }
    setContentView(new SVerticalLayout += surveys += report += export += imprt)
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