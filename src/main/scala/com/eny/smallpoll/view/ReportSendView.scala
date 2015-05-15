package com.eny.smallpoll.view

import java.util.Date

import com.eny.smallpoll.R
import com.eny.smallpoll.model.Answer
import com.eny.smallpoll.report.ResultRepository
import com.eny.smallpoll.repository.AnswerRepository
import org.scaloid.common._

class ReportSendView extends SActivity with Db {

  lazy val from = new STextView
  lazy val fromDate = new SDatePicker
  lazy val fromTime = new STimePicker
  lazy val to = new STextView
  lazy val toDate = new SDatePicker
  lazy val toTime = new STimePicker
  lazy val send = new SButton
  lazy val repository = new ResultRepository(instance.getWritableDatabase)

  onCreate {
    from.setText(R.string.from)
    to.setText(R.string.to)
    send.setText(R.string.send)
    send.onClick {
      val answerResult = repository.report(new Date(fromDate.getDrawingTime), new Date(toDate.getDrawingTime))
      finish()
    }
    contentView(new SVerticalLayout += from += fromDate += fromTime += to += toDate += toTime += send)
  }
}
