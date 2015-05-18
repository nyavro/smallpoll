package com.eny.smallpoll.report

import java.util.Date

import com.eny.smallpoll.model.Question
import com.eny.smallpoll.repository.{SurveyRepository, QuestionRepository, AnswerRepository}

/**
 * Created by Nyavro on 15.05.15
 */
class Report(markers:MarkerRepository, results:ResultRepository, answers:AnswerRepository, questions:QuestionRepository, surveys:SurveyRepository) {
  def result(from:Date, to:Date): List[SurveyReport] = {
    val answerCounts = results.report(from, to)
    val answersMap:Map[Long, List[AnswerReport]] =
      answers
        .list(answerCounts.keys)
        .groupBy(_.questionId)
        .map {
          case (questionId, answersList) => questionId -> answersList.map(answer => AnswerReport(answer.text, answerCounts(answer.id.getOrElse(-1))))
        }
    val questionsMap:Map[Long, List[QuestionReport]] =
      questions
        .list(answersMap.keys)
        .groupBy(_.surveyId)
        .map {
          case (surveyId, questionsList) => surveyId -> questionsList.map(question => QuestionReport(question.text, answersMap(question.id.getOrElse(-1))))
        }
    surveys.list(questionsMap.keys).map {
      survey => SurveyReport(from, to, survey.name, markers.count(from, to, survey.id.getOrElse(-1L)), questionsMap(survey.id.getOrElse(-1)))
    }
  }
  def result2(from:Date, to:Date): List[SurveyReport] = {
    results
      .fullReport(from, to)
      .groupBy(item => (item._1,item._2))
      .map {
        case ((id, name), qs) =>
          SurveyReport(from, to, name, markers.count(from, to, id),
            qs
              .groupBy(_._3)
              .map {
                case (questionTxt, answerList) => QuestionReport(questionTxt,
                    answerList
                      .map {
                        case (_, _, q, a, c) => AnswerReport(a, c)
                      }
                  )
              }
              .toList
          )
      }
      .toList
  }
}
