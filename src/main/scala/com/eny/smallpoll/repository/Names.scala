package com.eny.smallpoll.repository

/**
 * Created by eny on 26.04.15.
 */
trait Names {
  val Name = "smallpoll"
  //Tables
  val Answer = "answer"
  val Question = "question"
  val QuestionAnswer = "question_answer"
  val Survey = "survey"
  val SureveyQuestion = "survey_question"
  val Result = "result"
  //Fields
  val Id = "id"

  val DbVersion = 1
}
