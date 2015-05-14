package com.eny.smallpoll.repository

import java.lang
import java.util.Date

import android.content.ContentValues

/**
 * Created by eny on 27.04.15.
 */
case class Values(map:Map[String,Any]) {
  def content: ContentValues = {
    val res = new ContentValues
    map.map {
      case (key, value:String) => res.put(key, value)
      case (key, value:Int) => res.put(key, new Integer(value))
      case (key, value:Long) => res.put(key, new lang.Long(value))
      case (key, value:Boolean) => res.put(key, new lang.Boolean(value))
      case (key, value:Date) => res.put(key, new lang.Long(value.getTime))
    }
    res
  }
}
