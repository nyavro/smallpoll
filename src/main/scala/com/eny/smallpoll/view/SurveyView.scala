package com.eny.smallpoll.view

import android.app.Activity
import android.os.Bundle
import com.eny.smallpoll.R

class SurveyView extends Activity {
  override def onCreate(bundle:Bundle) = {
    super.onCreate(bundle)
    setContentView(R.layout.surveyview)
  }
}
