package com.eny.smallpoll.view

import com.eny.smallpoll.repository.{QuestionRepositoryImpl, SurveyRepositoryImpl}
import org.scaloid.common.{SActivity, STextView, SVerticalLayout}

class SurveyView extends SActivity with Db {

  lazy val name = new STextView("test")
  lazy val repository = new QuestionRepositoryImpl(instance.getWritableDatabase)

  onCreate {
//    val id = getIntent.getLongExtra("id")
    name.setText(getIntent.getStringExtra("name"))
    contentView(new SVerticalLayout += name)
  }
//  override def onCreate(bundle:Bundle) = {
//    super.onCreate(bundle)
//    setContentView(R.layout.surveyview)
//  }
}
