package com.eny.smallpoll.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Answer

/**
 * Created by eny on 27.04.15.
 */
class AnswerRepository(db:SQLiteDatabase) extends CursorConversion {

  def list(questionId: Long): List[Answer] = {
    val cursor = db.rawQuery("SELECT _id, txt, indx FROM answer WHERE question_id=?", Array(questionId.toString))
    cursor.moveToFirst
    toList(cursor, convert)
  }

  def convert(cursor:Cursor) = Answer(Some(cursor.getLong(0)), cursor.getString(1), cursor.getInt(2))
}
