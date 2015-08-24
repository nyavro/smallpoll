package com.eny.smallpoll.view

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import com.eny.smallpoll.R
import com.eny.smallpoll.exchange.State
import com.eny.smallpoll.repository._
import org.scaloid.common._

class ImportView extends SActivity with Db {
  lazy val surveyRepository = new SurveyRepository(instance.getWritableDatabase)
  lazy val questionRepository = new QuestionRepository(instance.getReadableDatabase)
  lazy val answerRepository = new AnswerRepository(instance.getReadableDatabase)
  lazy val filePath = new STextView(Environment.getExternalStorageDirectory.getPath)
  lazy val selectFile = new SButton(getString(R.string.file_to_import))
  lazy val imprt = new SButton(getString(R.string.imprt))

  override def onActivityResult(request:Int, result:Int, data:Intent) = {
    if(request==1) {
      if(result==Activity.RESULT_OK) {
        val path = data.getStringExtra(FileSelectView.SelectedPath)
        filePath.setText(path)
        imprt.setEnabled(true)
      }
    }
  }

  onCreate {
    filePath.setEnabled(false)
    imprt.onClick {
      State(
        surveyRepository,
        questionRepository,
        answerRepository,
        true,
        true,
        new Preferences(defaultSharedPreferences),
        getApplicationContext.getFilesDir.getPath,
        filePath.text.toString
      ).`import`()
      //todo: report
      //Toast.makeText(SurveyRunView.this, prediction.name + s" score:${prediction.score}", Toast.LENGTH_SHORT).show()
      finish()
    }
    selectFile.onClick {
      val intent = new Intent(this, classOf[FileSelectView])
      intent.putExtra(FileSelectView.Extensions, Array(".zip"))
      startActivityForResult(intent, 1)
    }
    imprt.setEnabled(false)

//    val main = new SRelativeLayout {
//      filePath.<<.alignParentTop.>>
//      runArea.<<.fill.>>
//      imprt.<<.alignParentBottom.>>
//    } += filePath += runArea += imprt
    setContentView(new SVerticalLayout += filePath += selectFile += imprt)
  }


}
