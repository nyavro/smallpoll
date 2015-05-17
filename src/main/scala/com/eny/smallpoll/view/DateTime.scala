package com.eny.smallpoll.view

import java.util.{Calendar, Date}

/**
 * Created by eny on 18.05.15.
 */
class DateTime(date:Date) {
  def apply(year:Int, month:Int, day:Int) = {
    val calendar = Calendar.getInstance()
    calendar.setTime(date)
    calendar.set(year, month, day)
    calendar.getTime
  }
  def apply(hour:Int, minute:Int) = {
    val calendar = Calendar.getInstance()
    calendar.setTime(date)
    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hour, minute, 0)
    calendar.getTime
  }
}
