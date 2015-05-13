package com.eny.smallpoll.repository

import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Result

/**
 * Created by Nyavro on 13.05.15
 */
class ResultRepository(db: SQLiteDatabase) {
  val Table = "result"
  val DateField = "date"
  val AnswerIdField = "answer_id"

  def save(result:Result) = {
    db.insert(Table, null, Values(Map(DateField->result.date, AnswerIdField->result.answerId)).content)
  }
}
