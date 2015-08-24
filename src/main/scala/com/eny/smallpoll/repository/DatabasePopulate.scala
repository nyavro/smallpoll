package com.eny.smallpoll.repository

import android.util.Log
import com.eny.smallpoll.exchange.{AnswerEx, QuestionEx, SurveyEx}
import com.eny.smallpoll.model.{Answer, Question, Survey}

class DatabasePopulate(
  surveyRepository: SurveyRepository,
  questionRepository: QuestionRepository,
  answerRepository: AnswerRepository) {

  def clean() = {
    surveyRepository.clean()
    questionRepository.clean()
    answerRepository.clean()
  }

  def populate(surveys:List[SurveyEx]) = {
    surveys.map(saveSurvey)
  }

  private def saveSurvey(survey:SurveyEx) = saveQuestions(
    survey.questions,
    surveyRepository.save(new Survey(None, survey.name))
  )

  private def saveQuestions(questions:List[QuestionEx], survey:Long) = {
    Log.d("smallpoll", "Saving questions")
    questions.foldLeft(0) {
      (index, question) => {
        Log.d("SmallPoll", "Save question")
        saveAnswers(
          question.answers,
          questionRepository.save(new Question(None, question.text, question.multi, survey, index))
        )
        index + 1
      }
    }
  }

  private def saveAnswers(answers:List[AnswerEx], question:Long) = {
    Log.d("smallpoll", "Saving answers")
    answers.foldLeft(0) {
      (index, answer) => {
        answerRepository.save(new Answer(None, answer.text, index, question))
        index + 1
      }
    }
  }
}
