package com.eny.smallpoll.model

/**
 * Created by eny on 25.04.15.
 */
case class Question(id:Option[Long], text:String, multi:Boolean, surveyId:Long, index:Int) {
  override def toString = text
}