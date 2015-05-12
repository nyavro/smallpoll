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
    db.setForeignKeyConstraintsEnabled(true)
    db.execSQL("CREATE TABLE survey (_id INTEGER PRIMARY KEY, name text not null)")
    db.execSQL("CREATE TABLE question (_id INTEGER PRIMARY KEY, txt text not null, indx integer not null, multi boolean not null, FOREIGN KEY(survey_id) REFERENCES survey(_id))")
    db.execSQL("CREATE TABLE answer (_id INTEGER PRIMARY KEY, txt text not null, indx integer not null, FOREIGN KEY(question_id) REFERENCES question(_id))")
    db.execSQL("CREATE TABLE result (date integer not null, FOREIGN KEY(answer_id) REFERENCES answer(_id))")
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
            ),
            multi = false
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
            ),
            multi = true
          ),
          Question(
            "Do you like our new logo?",
            List(
              Answer("Yes"),
              Answer("No")
            ),
            multi = false
          )
        )
      )
    )
  }

  case class Answer(text:String)
  case class Question(text:String, answers:List[Answer], multi:Boolean)
  case class Survey(name:String, questions:List[Question])

  def saveSurvey(db:SQLiteDatabase, survey:Survey): Unit =
    saveQuestions(db, survey.questions, db.insert("survey", null, Values(Map("name"->survey.name)).content))

  def saveAnswers(db:SQLiteDatabase, answers:List[Answer], question:Long) = {
    Log.d("smallpoll", "Saving answers")
    answers.foldLeft(0) {
      (index, answer) => {
        db.insert("answer", null, Values(Map("txt" -> answer.text, "indx" -> index, "question_id" -> question)).content)
        index + 1
      }
    }
  }

  def saveQuestions(db:SQLiteDatabase, questions:List[Question], survey:Long) = {
    Log.d("smallpoll", "Saving questions")
    questions.foldLeft(0) {
      (index, question) => {
        Log.d("SmallPoll", "Save question")
        saveAnswers(db, question.answers, db.insert("question", null, Values(Map("txt" -> question.text, "indx" -> index, "multi" -> question.multi, "survey_id" -> survey)).content))
        index + 1
      }
    }
  }

  override def onUpgrade(db:SQLiteDatabase, oldVersion:Int, newVersion:Int) = {
    Log.w(this.getClass.getName, s"Upgrading database from version $oldVersion to $newVersion")
  }
}
