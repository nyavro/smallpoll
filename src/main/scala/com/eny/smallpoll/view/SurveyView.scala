package com.eny.smallpoll.view

import android.content.Intent
import android.view.View
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.{Question, Survey}
import com.eny.smallpoll.repository.{QuestionRepositoryImpl, SurveyRepository}
import org.scaloid.common._

class SurveyView extends SActivity with Db {

  lazy val name = new STextView("test")
  lazy val questions = new SListView()
  lazy val add = new SButton()
  lazy val repository = new QuestionRepositoryImpl(instance.getWritableDatabase)

  onCreate {
    name.setText(getIntent.getStringExtra("name"))
    val id = getIntent.getLongExtra("id", -1)
    questions.setAdapter(
      new ArrayAdapter[Question](
        this,
        android.R.layout.simple_list_item_1,
        repository.list(id).toArray
      )
    )
    questions.onItemClick {
      (adapterView: AdapterView[_], view: View, position: Int, id: Long) =>
        val intent = new Intent(SurveyView.this, classOf[QuestionView])
        val question: Question = adapterView.getItemAtPosition(position).asInstanceOf[Question]
        intent.putExtra("id", question.id.getOrElse(-1L))
        intent.putExtra("text", question.text)
        startActivity(intent)
    }
    add.setText(R.string.add)
    contentView(new SVerticalLayout += name += questions += add)
  }
}
