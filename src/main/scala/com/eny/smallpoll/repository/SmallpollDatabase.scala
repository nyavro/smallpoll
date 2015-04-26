package com.eny.smallpoll.repository

import android.content.{ContentValues, Context}
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.util.Log

/**
 * Created by eny on 26.04.15.
 */
class SmallpollDatabase(context:Context, name:String, version:Int) extends SQLiteOpenHelper(context, name, null, version) {

  def this(context:Context) = this(context, "smallpoll", 1)

  override def onCreate(db:SQLiteDatabase) = {
    db.execSQL("CREATE TABLE answer (_id INTEGER PRIMARY KEY, txt text not null, indx integer not null)")
    db.execSQL("CREATE TABLE question (_id INTEGER PRIMARY KEY, txt text not null, indx integer not null)")
    db.execSQL("CREATE TABLE survey (_id INTEGER PRIMARY KEY, name text not null)")
    db.execSQL("CREATE TABLE question_answer (question_id integer not null, answer_id integer not null)")
    db.execSQL("CREATE TABLE survey_question (survey_id integer not null, question_id integer not null)")
    db.execSQL("CREATE TABLE result (date integer not null, question_id integer not null, answer_id integer not null)")
    saveSurvey(
      db,
      Survey(
        "Happiness",
        List(
          Question(
            "Are you happy with service?",
            List(
              Answer("Yes"),
              Answer("No"),
              Answer("Won't answer")
            )
          )
        )
      )
    )
    saveSurvey(
      db,
      Survey(
        "Poll",
        List(
          Question(
            "How did you know about company?",
            List(
              Answer("From friend"),
              Answer("Internet search"),
              Answer("Other")
            )
          ),
          Question(
            "Do you like our new logo?",
            List(
              Answer("Yes"),
              Answer("No")
            )
          )
        )
      )
    )
  }

  case class Answer(text:String)
  case class Question(text:String, answers:List[Answer])
  case class Survey(name:String, questions:List[Question])

  def saveSurvey(db:SQLiteDatabase, survey:Survey): Unit =
    saveQuestions(db, survey.questions, db.insert("survey", null, Values(Map("name"->survey.name)).content))

  def saveAnswers(db:SQLiteDatabase, answers:List[Answer], question:Long) =
    answers.foldLeft(0) {
      (index, answer) => {
        db.insert(
          "question_answer",
          null,
          Values(
            Map(
              "question_id"->question,
              "answer_id"->db.insert("answer", null, Values(Map("txt"->answer.text, "indx"->index)).content)
            )
          ).content
        )
        index + 1
      }
    }

  def saveQuestions(db:SQLiteDatabase, questions:List[Question], survey:Long) =
    questions.foldLeft(0) {
      (index, question) => {
        db.insert(
          "survey_question",
          null,
          Values(
            Map(
              "survey_id"->survey,
              "question_id"->db.insert("question", null, Values(Map("txt"->question.text, "indx"->index)).content)
            )
          ).content
        )
        index + 1
      }
    }

  override def onUpgrade(db:SQLiteDatabase, oldVersion:Int, newVersion:Int) = {
    Log.w(this.getClass.getName, s"Upgrading database from version $oldVersion to $newVersion")
  }
}
