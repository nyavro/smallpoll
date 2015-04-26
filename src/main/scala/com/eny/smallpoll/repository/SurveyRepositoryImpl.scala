package com.eny.smallpoll.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.{Question, Survey}

/**
 * Created by eny on 25.04.15.
 */
class SurveyRepositoryImpl(db:SQLiteDatabase) extends SurveyRepository {

  override def load(id: Long): Survey = {
    val cursor = db.rawQuery(s"SELECT id, name FROM survey WHERE id=?", Array(id.toString))
    cursor.moveToFirst
    convert(cursor)
  }

  override def list(): List[Survey] = {
    val cursor = db.query("survey", Array("id", "name"), null, null, null, null, null)
    cursor.moveToFirst
    toList(cursor, convert)
  }

  override def save(survey: Survey): Unit = ???

  private def toList[A](cursor:Cursor, convert:Cursor => A): List[A] =
    if(cursor.isAfterLast) Nil
    else {
      val item = convert(cursor)
      cursor.moveToNext
      item :: toList(cursor, convert)
    }

  private def convert(cursor:Cursor) = Survey(cursor.getLong(0), cursor.getString(1))
}
