package com.eny.smallpoll.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.{Question, Survey}

/**
 * Created by eny on 25.04.15.
 */
class SurveyRepositoryImpl(db:SQLiteDatabase) extends SurveyRepository {

  override def load(name: String): Survey = ???

  override def names(): List[String] = {
    val cursor = db.query("survey", Array("name"), null, null, null, null, null)
    cursor.moveToFirst
    toList(cursor, cursor => cursor.getString(0))
  }

  override def save(survey: Survey): Unit = ???

  private def toList[A](cursor:Cursor, convert:Cursor => A): List[A] =
    if(cursor.isAfterLast) Nil
    else {
      val item = convert(cursor)
      cursor.moveToNext
      item :: toList(cursor, convert)
    }
}
