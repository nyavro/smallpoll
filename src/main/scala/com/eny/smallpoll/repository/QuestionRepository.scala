package com.eny.smallpoll.repository

import com.eny.smallpoll.model.{Question, Survey}

/**
 * Created by eny on 25.04.15.
 */
trait QuestionRepository {
  def load(id:Long):Question
  def save(question:Question)
  def list(surveyId:Long):List[Question]
}
