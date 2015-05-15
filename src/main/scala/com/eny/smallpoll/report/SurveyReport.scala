package com.eny.smallpoll.report

import java.util.Date

/**
 * Created by Nyavro on 15.05.15
 */
case class SurveyReport(from:Date, to:Date, name:String, asked:Int, questionReports:List[QuestionReport])
