package com.eny.smallpoll.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Survey

/**
 * Created by eny on 25.04.15.
 */
class SurveyRepositoryImpl(db:SQLiteDatabase) extends SurveyRepository with CursorConversion {

  override def load(id: Long): Survey = {
    val cursor = db.rawQuery(s"SELECT _id, name FROM survey WHERE id=?", Array(id.toString))
    cursor.moveToFirst
    convert(cursor)
  }

  override def list(): List[Survey] = {
    val cursor = db.query("survey", Array("_id", "name"), null, null, null, null, null)
    cursor.moveToFirst
    toList(cursor, convert)
  }

  override def save(survey: Survey): Unit = {
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
