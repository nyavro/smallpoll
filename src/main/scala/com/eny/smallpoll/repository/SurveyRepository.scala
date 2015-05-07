package com.eny.smallpoll.repository

import com.eny.smallpoll.model.Survey

/**
 * Created by eny on 25.04.15.
 */
trait SurveyRepository {
  def load(id:Long):Survey
  def save(survey:Survey)
  def list():List[Survey]
}
