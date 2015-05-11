package com.eny.smallpoll.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Question

/**
 * Created by eny on 27.04.15.
 */
class QuestionRepositoryImpl(db:SQLiteDatabase) extends QuestionRepository with CursorConversion {

  override def load(id: Long): Question = {
    val cursor = db.rawQuery(s"SELECT id, txt FROM question WHERE id=?", Array(id.toString))
    cursor.moveToFirst
    convert(cursor)
  }

  override def list(surveyId: Long): List[Question] = {
    val cursor = db.query("question", Array("_id", "txt"), null, null, null, null, null)
    cursor.moveToFirst
    toList(cursor, convert)
  }

  override def save(question: Question): Unit = {
    val values = Values(Map("txt"->question.text)).content
    question.id match {
      case Some(id) =>
        db.update("question", values, "_id=?", Array(id.toString))
      case None =>
        db.insert("question", null, values)
    }
  }

  def convert(cursor:Cursor) = Question(Some(cursor.getLong(0)), cursor.getString(1))
}
