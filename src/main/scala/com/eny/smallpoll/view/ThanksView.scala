package com.eny.smallpoll.view

import com.eny.smallpoll.R
import org.scaloid.common._

class ThanksView extends SActivity {

  lazy val text = new STextView

  onCreate {
    val surveyId = getIntent.getLongExtra("surveyId", -1)
    text.setText(R.string.thanks)
    contentView(new SVerticalLayout += text)
  }
}
