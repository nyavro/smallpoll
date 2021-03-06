package com.eny.smallpoll.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Answer

/**
 * Created by eny on 27.04.15.
 */
class AnswerRepository(db:SQLiteDatabase) extends CursorConversion {

  val Table = "answer"

  def init() = {
    db.execSQL(s"CREATE TABLE $Table (_id INTEGER PRIMARY KEY, txt text not null, indx integer not null, question_id LONG NOT NULL, FOREIGN KEY(question_id) REFERENCES question(_id) ON DELETE CASCADE)")
  }

  def save(answer: Answer):Long = {
    val content = Values(Map("txt" -> answer.text, "indx" -> answer.index, "question_id" -> answer.questionId)).content
    answer.id match {
      case Some(id) =>
        db.update("answer", content, "_id=?", Array(id.toString))
        id
      case None =>
        db.insert("answer", null, content)
    }
  }

  def remove(id: Long) = {
    db.delete("answer", "_id=?", Array(id.toString))
  }

  def list(questionId: Long): List[Answer] = {
    val cursor = db.rawQuery("SELECT _id, txt, indx, question_id FROM answer WHERE question_id=?", Array(questionId.toString))
    cursor.moveToFirst
    toList(cursor, convert)
  }

  def list(answerIds: Iterable[Long]): List[Answer] = {
    val cursor = db.rawQuery("SELECT _id, txt, indx, question_id FROM answer WHERE _id IN (?)", Array(answerIds.mkString(",")))
    cursor.moveToFirst
    toList(cursor, convert)
  }

  def clean() = {
    db.execSQL(s"DELETE FROM $Table", Array())
  }

  def convert(cursor:Cursor) = Answer(Some(cursor.getLong(0)), cursor.getString(1), cursor.getInt(2), cursor.getLong(3))
}
