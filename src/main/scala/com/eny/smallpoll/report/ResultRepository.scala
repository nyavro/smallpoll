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
  def fullReport(from: Date, to: Date):List[(Long, String, String, String, Int)] = {
    val cursor = db.rawQuery(
      s"""
         |SELECT s._id, s.name, q.txt, a.txt, COUNT($AnswerIdField)
         |  FROM $Table r
         |  INNER JOIN answer a ON a._id=r.answer_id
         |  INNER JOIN question q ON q._id=a.question_id
         |  INNER JOIN survey s ON s._id=q.survey_id
         |  WHERE r.$DateField > ? AND r.$DateField <= ?
         |  GROUP BY(r.$AnswerIdField)""".stripMargin,
      Array(from.getTime.toString, to.getTime.toString)
    )
    cursor.moveToFirst
    toList(cursor, cursor => (cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4)))
  }
  def convert(cursor:Cursor) = cursor.getLong(0) -> cursor.getInt(1)
}
