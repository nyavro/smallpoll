package com.eny.smallpoll.view

import android.os.Bundle
import com.eny.smallpoll.R
import com.eny.smallpoll.model.Answer
import com.eny.smallpoll.repository.AnswerRepository
import org.scaloid.common._

class AnswerView extends SActivity with Db {

  lazy val text = new SEditText
  lazy val save = new SButton()
  lazy val repository = new AnswerRepository(instance.getWritableDatabase)

  onCreate {
    text.setText(getIntent.getStringExtra("text"))
    val id = getIntent.getLongExtra("id", -1)
    val index = getIntent.getIntExtra("indx", -1)
    val questionId = getIntent.getLongExtra("questionId", -1)
    save.setText(R.string.save)
    save.onClick {
      repository.save(Answer(asOption(id), text.getText.toString, index, questionId))
      finish()
    }
    contentView(new SVerticalLayout += text += save)
  }
}
