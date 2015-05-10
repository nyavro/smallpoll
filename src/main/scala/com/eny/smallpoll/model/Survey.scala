package com.eny.smallpoll.model

/**
 * Created by eny on 25.04.15.
 */
case class Survey(id:Option[Long], name:String) {
  override def toString = name
}
