package com.eny.smallpoll.report

/**
 * Created by Nyavro on 15.05.15
 */
class TableReport(reports:List[SurveyReport], fromStr:String, toStr:String) {
  override def toString = {
    val Separator: String = "\r\n"
    reports.map {
      report =>
        val questions = report.questionReports.map {
          question =>
            val answers = question.answerReports.map {
              answer =>
                s"""<tr>
                   |  <td>${answer.answer}</td>
                   |  <td>${answer.count}</td>
                   |</tr>
                """.stripMargin
            }.mkString(Separator)
            s"""<tr>
               |  <td>${question.question}</td>
               |  <td/>
               |</tr>
               |$answers""".stripMargin
        }.mkString(Separator)
        s"""<table>
           |  <caption>${report.name}</caption>
           |  <tr>
           |    <td>Период</td>
           |    <td>$fromStr-$toStr</td>
           |  </tr>
           |  <tr>
           |    <td>Количество опрошенных</td>
           |    <td>${report.asked}</td>
           |  </tr>
           |  <tr/>
           |  $questions
           |</table>""".stripMargin
    }.mkString(Separator)
  }
}
