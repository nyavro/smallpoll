package com.eny.smallpoll.view

import java.io.{File, FileWriter}
import java.text.{DateFormat, SimpleDateFormat}
import java.util.concurrent.TimeUnit
import java.util.{Calendar, Date}

import android.app.{DatePickerDialog, DialogFragment, ProgressDialog, TimePickerDialog}
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.{DatePicker, TimePicker}
import com.eny.smallpoll.R
import com.eny.smallpoll.report.{Report, ResultRepository, TableReport}
import com.eny.smallpoll.repository.{MarkerRepository, AnswerRepository, QuestionRepository, SurveyRepository}
import org.scaloid.common._

class ReportSendView extends SActivity with Db {

  lazy val fromText = new STextView
  lazy val fromDate = new STextView
  lazy val fromTime = new STextView
  lazy val toText = new STextView
  lazy val toDate = new STextView
  lazy val toTime = new STextView
  lazy val send = new SButton
  lazy val resultRepo = new ResultRepository(instance.getReadableDatabase)
  lazy val markerRepo = new MarkerRepository(instance.getReadableDatabase)
  lazy val answerRepo = new AnswerRepository(instance.getReadableDatabase)
  lazy val questionRepo = new QuestionRepository(instance.getReadableDatabase)
  lazy val surveyRepo = new SurveyRepository(instance.getReadableDatabase)
  lazy val dateFormat = {
    val local = new ThreadLocal[DateFormat]
    local.set(new SimpleDateFormat("dd.MM.yyyy"))
    local
  }
  lazy val timeFormat = {
    val local = new ThreadLocal[DateFormat]
    local.set(new SimpleDateFormat("HH:mm:ss"))
    local
  }
  var from:Date = _
  var to:Date = _

  def setFrom(h:Int, m:Int) = {
    from = new DateTime(from).apply(h, m)
    update()
  }

  def setTo(h:Int, m:Int) = {
    to = new DateTime(to).apply(h, m)
    update()
  }

  def setFrom(y:Int, m:Int, d:Int) = {
    from = new DateTime(from).apply(y, m, d)
    update()
  }

  def setTo(y:Int, m:Int, d:Int) = {
    to = new DateTime(to).apply(y, m, d)
    update()
  }

  onCreate {
    from = new DateTime(new Date).apply(0, 0)
    to = new Date(from.getTime + TimeUnit.DAYS.toMillis(1))
    fromText.setText(R.string.from)
    toText.setText(R.string.to)
    send.setText(R.string.send)
    fromTime.onClick {
      val calendar = Calendar.getInstance()
      calendar.setTime(from)
      new TimePickerFragment(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), setFrom).show(getFragmentManager, "timePicker")
    }
    toTime.onClick {
      val calendar = Calendar.getInstance()
      calendar.setTime(to)
      new TimePickerFragment(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), setTo).show(getFragmentManager, "timePicker")
    }
    fromDate.onClick {
      val calendar = Calendar.getInstance()
      calendar.setTime(from)
      new DatePickerFragment(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), setFrom).show(getFragmentManager, "datePicker")
    }
    toDate.onClick {
      val calendar = Calendar.getInstance()
      calendar.setTime(to)
      new DatePickerFragment(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), setTo).show(getFragmentManager, "datePicker")
    }
    send.onClick {
      val list = new Report(markerRepo, resultRepo, answerRepo, questionRepo, surveyRepo).result2(from, to)
      val fromStr = format(from)
      val toStr = format(to)
      val body =
        s"""
           |<html>
           |  <head>
           |    <meta http-equiv="content-type" content="text/html; charset=utf-8">
           |  </head>
           |  <table>
           |    <tr><td>Опросов за период: </td><td>${list.size}</td></tr>
           |  </table>
           |${new TableReport(list, fromStr, toStr).toString}
           |</html>
           |
        """.stripMargin
      sendMail(s"Отчёт за период с $fromStr по $toStr", body)
      finish()
    }
    contentView(new SVerticalLayout += fromText += fromDate += fromTime += toText += toDate += toTime += send)
  }
  def format(date:Date):String = {
    val calendar = Calendar.getInstance()
    calendar.setTime(date)
    if(calendar.get(Calendar.HOUR)!=0 || calendar.get(Calendar.MINUTE)!=0 || calendar.get(Calendar.SECOND)!=0)
      dateFormat.get().format(date) + " " + timeFormat.get().format(date)
    else
      dateFormat.get().format(date)
  }
  override def onResume() = {
    super.onResume()
    update()
  }
  def update() = {
    fromDate.setText(dateFormat.get.format(from))
    toDate.setText(dateFormat.get.format(to))
    fromTime.setText(timeFormat.get.format(from))
    toTime.setText(timeFormat.get.format(to))
  }

  //Todo
  def cleanly[A,B](resource: => A)(cleanup: A => Unit)(code: A => B): Option[B] =
    try {
      val r = resource
      try { Some(code(r)) }
      finally { cleanup(r) }
    } catch {
      case e: Exception => None
    }

  def sendMail(subject:String, body:String) = {
    val preferences = new Preferences(defaultSharedPreferences)
    val progress = ProgressDialog.show(this, "", getString(R.string.saving))
    new Thread() {
      override def run() = {
        val file = new File(getExternalCacheDir, "report.xls")
        cleanly(new FileWriter(file))(_.close()) {
          fw => fw.write(body)
        }
        progress.dismiss()
        val intent = new Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_EMAIL, Array(preferences.sendto("")))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, subject)

        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        startActivity(Intent.createChooser(intent, R.string.send_mail_title))
      }
    }.start()
  }

  class TimePickerFragment(h:Int, m:Int, onSet: (Int, Int)=> Unit) extends DialogFragment with TimePickerDialog.OnTimeSetListener {
    override def onCreateDialog(bundle:Bundle) = {
      new TimePickerDialog(getActivity, this, h, m, true)
    }
    override def onTimeSet(view:TimePicker, hour:Int, minute:Int) = onSet(hour, minute)
  }

  class DatePickerFragment(y:Int, m:Int, d:Int, onSet: (Int, Int, Int)=> Unit) extends DialogFragment with DatePickerDialog.OnDateSetListener {
    override def onCreateDialog(bundle:Bundle) = {
      new DatePickerDialog(getActivity, this, y, m, d)
    }
    override def onDateSet(view: DatePicker, year: Int, month: Int, day: Int) = onSet(year, month, day)
  }
  override def onSaveInstanceState(bundle:Bundle) = {
    super.onSaveInstanceState(bundle)
    bundle.putLong("from", from.getTime)
    bundle.putLong("to", to.getTime)
  }
  override def onRestoreInstanceState(bundle:Bundle) = {
    from = new Date(bundle.getLong("from"))
    to = new Date(bundle.getLong("to"))
  }
}
