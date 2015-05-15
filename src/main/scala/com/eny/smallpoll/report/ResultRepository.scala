package com.eny.smallpoll.report

import java.util.Date

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Result
import com.eny.smallpoll.repository.{CursorConversion, Values}

/**
 * Created by Nyavro on 13.05.15
 */
class ResultRepository(db: SQLiteDatabase) extends CursorConversion {

  val Table = "result"
  val DateField = "date"
  val AnswerIdField = "answer_id"

  def report(from: Date, to: Date):Map[Long, Int] = {
    val cursor = db.rawQuery(
      s"SELECT $AnswerIdField, COUNT($AnswerIdField) FROM $Table WHERE $DateField > ? AND $DateField <= ? GROUP BY($AnswerIdField)",
      Array(from.getTime.toString, to.getTime.toString)
    )
    cursor.moveToFirst
    toList(cursor, convert).toMap
  }
  def save(result:Result) = {
    db.insert(Table, null, Values(Map(DateField->result.date, AnswerIdField->result.answerId)).content)
  }
  def convert(cursor:Cursor) = cursor.getLong(0) -> cursor.getInt(1)
}
