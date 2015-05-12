package com.eny.smallpoll.repository

import com.eny.smallpoll.model.Answer

/**
 * Created by eny on 25.04.15.
 */
trait AnswerRepository {
  def list(questionId:Long):List[Answer]
}
