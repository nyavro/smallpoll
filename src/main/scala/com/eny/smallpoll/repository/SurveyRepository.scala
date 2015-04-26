package com.eny.smallpoll.repository

import com.eny.smallpoll.model.Survey

/**
 * Created by eny on 25.04.15.
 */
trait SurveyRepository {
  def load(name:String):Survey
  def save(survey:Survey)
  def names():List[String]
}
