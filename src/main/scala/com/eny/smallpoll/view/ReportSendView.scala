package com.eny.smallpoll.view

import java.text.{DateFormat, SimpleDateFormat}
import java.util.concurrent.TimeUnit
import java.util.{Calendar, Date}

import android.app.{DatePickerDialog, DialogFragment, TimePickerDialog}
import android.content.Intent
import android.os.Bundle
import android.widget.{DatePicker, TimePicker}
import com.eny.smallpoll.R
import com.eny.smallpoll.report.{TableReport, MarkerRepository, Report, ResultRepository}
import com.eny.smallpoll.repository.{AnswerRepository, QuestionRepository, SurveyRepository}
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
  lazy val fullFormat = {
    val local = new ThreadLocal[DateFormat]
    local.set(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"))
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
      val list = new Report(markerRepo, resultRepo, answerRepo, questionRepo, surveyRepo).result(from, to)
      val fromStr = fullFormat.get.format(from)
      val toStr = fullFormat.get.format(to)
      val body =
        s"""
           |<html>
           |  <body>
           |    <div>Опросов за период с $fromStr по $toStr: ${list.size}</div>
           |    ${new TableReport(list).toString}
           |  </body>
           |</html>
           |
        """.stripMargin
      sendMail(s"Отчёт за период с $fromStr по $toStr", body)
      finish()
    }
    contentView(new SVerticalLayout += fromText += fromDate += fromTime += toText += toDate += toTime += send)
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

  def sendMail(subject:String, body:String) = {
    val preferences = new Preferences(defaultSharedPreferences)
    val intent = new Intent(Intent.ACTION_SEND)
    intent.setType("message/rfc822")
    intent.putExtra(Intent.EXTRA_EMAIL, Array(preferences.sendto("")))
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, body)
    startActivity(Intent.createChooser(intent, R.string.send_mail_title))
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
