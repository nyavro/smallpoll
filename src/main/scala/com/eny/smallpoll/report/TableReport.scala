package com.eny.smallpoll.report

/**
 * Created by Nyavro on 15.05.15
 */
class TableReport(reports:List[SurveyReport]) {
  override def toString = {
    val Separator: String = "\r\n"
    reports.map {
      report =>
        val questions = report.questionReports.map {
          question =>
            val answers = question.answerReports.map {
              answer =>
                s"""
                   |<tr>
                   |  <tc>${answer.answer}</tc>
                   |  <tc>${answer.count}</tc>
                   |</tr>
                """.stripMargin
            }.mkString(Separator)
            s"""
               |<tr>
               |  <tc>${question.question}</tc>
               |  <tc/>
               |</tr>
               |$answers
            """.stripMargin
        }.mkString(Separator)
        s"""
           |<table>
           |  <caption>${report.name}</caption>
           |  <tr>
           |    <tc>Период</tc>
           |    <tc>${report.from}-${report.to}</tc>
           |  </tr>
           |  <tr>
           |    <tc>Количество опрошенных</tc>
           |    <tc>${report.asked}</tc>
           |  </tr>
           |  <tr/>
           |  $questions
           |</table>
         """.stripMargin
    }.mkString(Separator)
  }
}
