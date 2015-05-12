package com.eny.smallpoll.view

import android.app.AlertDialog.Builder
import android.content.DialogInterface.OnClickListener
import android.content.{DialogInterface, Intent}
import android.view.View
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.{view, R}
import com.eny.smallpoll.model.Answer
import com.eny.smallpoll.repository.AnswerRepository
import com.eny.smallpoll.view.QuestionView
import org.scaloid.common._

class AnswerView extends SActivity with Db {

  lazy val text = new STextView("test")
  lazy val save = new SButton()
  lazy val repository = new AnswerRepository(instance.getWritableDatabase)

  onCreate {
    text.setText(getIntent.getStringExtra("text"))
    val id = getIntent.getLongExtra("id", -1)
    val index = getIntent.getIntExtra("indx", -1)
    save.setText(R.string.save)
    save.onClick {
      repository.save(Answer(if (id.equals(-1L)) None else Some(id), text.getText.toString, index))
    }
    contentView(new SVerticalLayout += text += save)
  }
}
