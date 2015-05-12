package com.eny.smallpoll.view

import android.content.Intent
import android.view.View
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.{Answer, Question}
import com.eny.smallpoll.repository.{AnswerRepository, QuestionRepositoryImpl}
import org.scaloid.common._

class QuestionView extends SActivity with Db {

  lazy val text = new STextView("test")
  lazy val answers = new SListView()
  lazy val add = new SButton()
  lazy val repository = new AnswerRepository(instance.getWritableDatabase)

  onCreate {
    text.setText(getIntent.getStringExtra("text"))
    val id = getIntent.getLongExtra("id", -1)
    answers.setAdapter(
      new ArrayAdapter[Answer](
        this,
        android.R.layout.simple_list_item_1,
        repository.list(id).toArray
      )
    )
    add.setText(R.string.add)
    contentView(new SVerticalLayout += text += answers += add)
  }
}
