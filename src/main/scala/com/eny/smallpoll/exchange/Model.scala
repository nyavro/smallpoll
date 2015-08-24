package com.eny.smallpoll.exchange

@SerialVersionUID(101L)
case class AnswerEx(text:String) extends Serializable
@SerialVersionUID(102L)
case class QuestionEx(text:String, answers:List[AnswerEx], multi:Boolean) extends Serializable
@SerialVersionUID(103L)
case class SurveyEx(name:String, questions:List[QuestionEx]) extends Serializable