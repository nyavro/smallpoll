package com.eny.smallpoll.view

import org.scaloid.common.{SActivity, STextView, SVerticalLayout}

class SurveyView extends SActivity {

  lazy val name = new STextView("test")

  onCreate {
    contentView(new SVerticalLayout += name)
  }
//  override def onCreate(bundle:Bundle) = {
//    super.onCreate(bundle)
//    setContentView(R.layout.surveyview)
//  }
}
