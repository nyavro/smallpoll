package com.eny.smallpoll.view

import com.eny.smallpoll.R
import com.eny.smallpoll.repository.{AnswerRepository, ResultRepository, QuestionRepository}
import com.eny.smallpoll.view.Db
import org.scaloid.common.{STextView, SVerticalLayout, SActivity}

/**
 * Created by Nyavro on 13.05.15
 */
class SurveyRunView extends SActivity with Db {
  lazy val questionRepository = new QuestionRepository(instance.getReadableDatabase)
  lazy val answerRepository = new AnswerRepository(instance.getReadableDatabase)
  lazy val resultRepository = new ResultRepository(instance.getWritableDatabase)

  onCreate {
    val questionIds = getIntent.getLongArrayExtra("questionIds")
    if(questionIds.isEmpty) {

    }
    val questions = questionRepository.list(surveyId)


    contentView(
      layout(multi)
    )
  }

  def layout(multi:Boolean) = {
    if
    new SVerticalLayout
    += new STextView(R.string.survey_name)
    += name
      += new STextView(R.string.survey_questions)
    += questions
      += add
      += start
  }
}
