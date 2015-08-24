package com.eny.smallpoll.repository

import android.content.{ContentValues, Context}
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.util.Log
import com.eny.smallpoll.exchange.{QuestionEx, AnswerEx, SurveyEx}
import com.eny.smallpoll.report.ResultRepository

/**
 * Created by eny on 26.04.15.
 */
class SmallpollDatabase(context:Context, name:String, version:Int) extends SQLiteOpenHelper(context, name, null, version) {

  def this(context:Context) = this(context, "smallpoll", 1)

  override def onCreate(db:SQLiteDatabase) = {
    db.execSQL("PRAGMA foreign_keys=ON;")
    val surveyRepository = new SurveyRepository(db)
    val questionRepository = new QuestionRepository(db)
    val answerRepository = new AnswerRepository(db)
    surveyRepository.init()
    questionRepository.init()
    answerRepository.init()
    new ResultRepository(db).init()
    new MarkerRepository(db).init()
    new DatabasePopulate(surveyRepository, questionRepository, answerRepository)
      .populate(
        List(
          SurveyEx(
            "Happiness",
            List(
              QuestionEx(
                "Are you happy with service?",
                List(
                  AnswerEx("Yes"),
                  AnswerEx("No"),
                  AnswerEx("Won't answer")
                ),
                multi = false
              )
            )
          ),
          SurveyEx(
            "Poll",
            List(
              QuestionEx(
                "How did you know about company?",
                List(
                  AnswerEx("From friend"),
                  AnswerEx("Internet search"),
                  AnswerEx("Other")
                ),
                multi = true
              ),
              QuestionEx(
                "Do you like our new logo?",
                List(
                  AnswerEx("Yes"),
                  AnswerEx("No")
                ),
                multi = false
              )
            )
          )
        )
      )
  }

  override def onUpgrade(db:SQLiteDatabase, oldVersion:Int, newVersion:Int) = {
    Log.w(this.getClass.getName, s"Upgrading database from version $oldVersion to $newVersion")
  }
}
