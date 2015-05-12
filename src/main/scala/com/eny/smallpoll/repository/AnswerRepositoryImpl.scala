package com.eny.smallpoll.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.{Answer, Question}

/**
 * Created by eny on 27.04.15.
 */
class AnswerRepositoryImpl(db:SQLiteDatabase) extends AnswerRepository with CursorConversion {

  override def list(questionId: Long): List[Answer] = {
    val cursor = db.rawQuery("SELECT a.* FROM answer a INNER JOIN question_answer qa ON a._id=qa.answer_id WHERE qa.question_id=?", Array(questionId.toString))
    cursor.moveToFirst
    toList(cursor, convert)
  }

  def convert(cursor:Cursor) = Answer(Some(cursor.getLong(0)), cursor.getString(1), cursor.getInt(2))
}
