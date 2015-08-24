package com.eny.smallpoll.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Question

/**
 * Created by eny on 27.04.15.
 */
class QuestionRepository(db:SQLiteDatabase) extends CursorConversion {

  val Table = "question"

  def init() = {
    db.execSQL(s"CREATE TABLE $Table (_id INTEGER PRIMARY KEY, txt text not null, indx integer not null, multi boolean not null, survey_id LONG NOT NULL, FOREIGN KEY(survey_id) REFERENCES survey(_id) ON DELETE CASCADE)")
  }

  def load(questionId: Long):Question = {
    val cursor = db.rawQuery("SELECT _id, txt, multi, survey_id, indx FROM question WHERE _id=?", Array(questionId.toString))
    cursor.moveToFirst
    convert(cursor)
  }

  def remove(id: Long):Unit = {
    db.delete("question", "_id=?", Array(id.toString))
  }

  def list(surveyId: Long): List[Question] = {
    val cursor = db.rawQuery("SELECT _id, txt, multi, survey_id, indx FROM question WHERE survey_id=?", Array(surveyId.toString))
    cursor.moveToFirst
    toList(cursor, convert)
  }

  def list(questionIds: Iterable[Long]): List[Question] = {
    val cursor = db.rawQuery("SELECT _id, txt, multi, survey_id, indx FROM question WHERE _id IN (?)", Array(questionIds.mkString(",").toString))
    cursor.moveToFirst
    toList(cursor, convert)
  }

  def save(question: Question): Long = {
    val values = Values(Map("txt"->question.text, "multi"->question.multi, "survey_id"->question.surveyId, "indx"->question.index)).content
    question.id match {
      case Some(id) =>
        db.update("question", values, "_id=?", Array(id.toString))
        id
      case None =>
        db.insert("question", null, values)
    }
  }

  def clean() = {
    db.execSQL(s"DELETE FROM $Table", Array())
  }

  def convert(cursor:Cursor) = Question(Some(cursor.getLong(0)), cursor.getString(1), cursor.getInt(2)==1, cursor.getLong(3), cursor.getInt(4))
}
