package com.eny.smallpoll.view

import java.io.File

import android.app.Activity
import android.content.Intent
import android.os.Environment
import com.eny.smallpoll.R
import com.eny.smallpoll.exchange.State
import com.eny.smallpoll.repository._
import org.scaloid.common._

class ExportView extends SActivity with Db {
  lazy val surveyRepository = new SurveyRepository(instance.getWritableDatabase)
  lazy val questionRepository = new QuestionRepository(instance.getReadableDatabase)
  lazy val answerRepository = new AnswerRepository(instance.getReadableDatabase)
  
  lazy val settings = new SCheckBox(getString(R.string.settings))
  lazy val surveys = new SCheckBox(getString(R.string.surveys))
  lazy val export = new SButton(getString(R.string.export))

  lazy val filePath = new STextView(Environment.getExternalStorageDirectory.getPath)
  lazy val selectFile = new SButton(getString(R.string.folder_to_export))

  override def onActivityResult(request:Int, result:Int, data:Intent) = {
    if(request==2) {
      if(result==Activity.RESULT_OK) {
        val path = data.getStringExtra(FileSelectView.SelectedPath)
        filePath.setText(path)
        export.setEnabled(true)
      }
    }
  }

  onCreate {
    filePath.setEnabled(false)
    settings.setChecked(true)
    surveys.setChecked(true)
    export.onClick {
      State(
        surveyRepository,
        questionRepository,
        answerRepository,
        settings.isChecked,
        surveys.isChecked,
        new Preferences(defaultSharedPreferences),
        getApplicationContext.getFilesDir.getPath,
        new File(filePath.getText.toString, "export.zip").getPath
      ).export()
      finish()
    }
    export.setEnabled(false)
    selectFile.onClick {
      val intent = new Intent(this, classOf[FileSelectView])
      intent.putExtra(FileSelectView.Extensions, Array[String]())
      startActivityForResult(intent, 2)
    }
    setContentView(new SVerticalLayout += settings += surveys += filePath += selectFile += export)
  }
}
