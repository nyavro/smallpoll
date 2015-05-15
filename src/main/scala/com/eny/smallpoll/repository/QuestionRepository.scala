package com.eny.smallpoll.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Question

/**
 * Created by eny on 27.04.15.
 */
class QuestionRepository(db:SQLiteDatabase) extends CursorConversion {

  val Table = "question"

  def load(questionId: Long):Question = {
    val cursor = db.rawQuery("SELECT _id, txt, multi, survey_id FROM question WHERE _id=?", Array(questionId.toString))
    cursor.moveToFirst
    convert(cursor)
  }


  def remove(id: Long):Unit = {
    db.delete("question", "_id=?", Array(id.toString))
  }

  def list(surveyId: Long): List[Question] = {
    val cursor = db.rawQuery("SELECT _id, txt, multi, survey_id FROM question WHERE survey_id=?", Array(surveyId.toString))
    cursor.moveToFirst
    toList(cursor, convert)
  }

  def list(questionIds: Iterable[Long]): List[Question] = {
    val cursor = db.rawQuery("SELECT _id, txt, multi, survey_id FROM question WHERE _id IN (?)", Array(questionIds.mkString(",").toString))
    cursor.moveToFirst
    toList(cursor, convert)
  }

  def save(question: Question): Unit = {
    val values = Values(Map("txt"->question.text, "multi"->question.multi)).content
    question.id match {
      case Some(id) =>
        db.update("question", values, "_id=?", Array(id.toString))
      case None =>
        db.insert("question", null, values)
    }
  }

  def convert(cursor:Cursor) = Question(Some(cursor.getLong(0)), cursor.getString(1), cursor.getInt(2)==1, cursor.getLong(3))
}
