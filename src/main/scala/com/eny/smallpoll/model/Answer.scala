package com.eny.smallpoll.model

/**
 * Created by eny on 25.04.15.
 */
case class Answer(id:Option[Long], text:String, index:Int) {
  override def toString = text
}
