package com.eny.smallpoll.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Survey

/**
 * Created by eny on 25.04.15.
 */
class SurveyRepository(db:SQLiteDatabase) extends CursorConversion {

  def remove(id: Long):Unit = {
    db.delete("survey", "_id=?", Array(id.toString))
  }

  def list(): List[Survey] = {
    val cursor = db.query("survey", Array("_id", "name"), null, null, null, null, null)
    cursor.moveToFirst
    toList(cursor, convert)
  }

  def save(survey: Survey): Unit = {
    val values = Values(Map("name"->survey.name)).content
    survey.id match {
      case Some(id) =>
        db.update("survey", values, "_id=?", Array(id.toString))
      case None =>
        db.insert("survey", null, values)
    }
  }

  private def convert(cursor:Cursor) = Survey(Some(cursor.getLong(0)), cursor.getString(1))
}
