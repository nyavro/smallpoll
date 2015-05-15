package com.eny.smallpoll.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Survey

/**
 * Created by eny on 25.04.15.
 */
class SurveyRepository(db:SQLiteDatabase) extends CursorConversion {

  val Table = "survey"

  def remove(id: Long):Unit = {
    db.delete(Table, "_id=?", Array(id.toString))
  }

  def list(): List[Survey] = {
    val cursor = db.query(Table, Array("_id", "name"), null, null, null, null, null)
    cursor.moveToFirst
    toList(cursor, convert)
  }
  
  def list(surveyIds:Iterable[Long]): List[Survey] = {
    val cursor = db.rawQuery(s"SELECT _id, name FROM $Table WHERE _id IN (?)", Array(surveyIds.mkString(",").toString))
    cursor.moveToFirst
    toList(cursor, convert)
  }

  def save(survey: Survey): Unit = {
    val values = Values(Map("name"->survey.name)).content
    survey.id match {
      case Some(id) =>
        db.update(Table, values, "_id=?", Array(id.toString))
      case None =>
        db.insert(Table, null, values)
    }
  }

  private def convert(cursor:Cursor) = Survey(Some(cursor.getLong(0)), cursor.getString(1))
}
