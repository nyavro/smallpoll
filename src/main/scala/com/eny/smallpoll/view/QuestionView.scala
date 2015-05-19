package com.eny.smallpoll.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.{Answer, Question}
import com.eny.smallpoll.repository.{AnswerRepository, QuestionRepository}
import org.scaloid.common._

class QuestionView extends SActivity with Db {

  lazy val text = new SEditText
  lazy val answers = new SListView
  lazy val multi = new SCheckBox
  lazy val add = new SButton
  lazy val saveBtn = new SButton
  lazy val answerRepository = new AnswerRepository(instance.getReadableDatabase)
  lazy val questionRepository = new QuestionRepository(instance.getWritableDatabase)
  var questionId:Long = -1
  var surveyId:Long = -1
  var isMulti:Boolean = false
  var index:Int = -1
  var questionText:String = ""

  onCreate {
    initParams()
    text.setActivated(false)
    multi.setText(R.string.multi_select)
    answers.onItemClick {
      (adapterView: AdapterView[_], view: View, position: Int, id: Long) =>
        edit(adapterView.getItemAtPosition(position).asInstanceOf[Answer])
    }
    answers.onItemLongClick {
      (adapterView:AdapterView[_], view:View, position:Int, id:Long) =>
        Alert(R.string.remove_answer).run(() => remove(adapterView.getItemAtPosition(position).asInstanceOf[Answer]))
        true
    }
    add.setText(R.string.add)
    add.onClick {
      save()
      edit(Answer(None, "", answers.getAdapter.getCount, questionId))
    }
    saveBtn.setText(R.string.save)
    saveBtn.onClick {
      save()
      finish()
    }
    contentView(new SVerticalLayout += new STextView(R.string.question_text) += text += new STextView(R.string.question_answers) += multi += answers += add += saveBtn)
  }
  def initParams() = {
    questionId = getIntent.getLongExtra("questionId", -1)
    surveyId = getIntent.getLongExtra("survey", -1)
    isMulti = getIntent.getBooleanExtra("multi", false)
    questionText = getIntent.getStringExtra("text")
    index = getIntent.getIntExtra("index", -1)
  }
  def edit(answer:Answer): Unit = {
    val intent = new Intent(QuestionView.this, classOf[AnswerView])
    intent.putExtra("id", answer.id.getOrElse(-1L))
    intent.putExtra("text", answer.text)
    intent.putExtra("questionId", answer.questionId)
    intent.putExtra("indx", answer.index)
    startActivity(intent)
  }
  def remove(answer:Answer): Unit = {
    answerRepository.remove(answer.id.getOrElse(-1L))
    update()
  }
  def save() = {
    questionId = questionRepository.save(Question(asOption(questionId), text.getText.toString, multi.isChecked, surveyId, index))
    questionId
  }
  def update() = {
    Log.d("smallpoll", s"Updating question $questionId")
    multi.setChecked(isMulti)
    text.setText(questionText)
    answers.setAdapter(
      new ArrayAdapter[Answer](
        this,
        android.R.layout.simple_list_item_1,
        answerRepository.list(questionId).toArray
      )
    )
  }
  override def onResume() {
    super.onResume()
    update()
  }
  override def onPause(): Unit = {
    super.onPause()
  }
  override def onSaveInstanceState(bundle:Bundle) = {
    super.onSaveInstanceState(bundle)
    questionText = text.text.toString
    isMulti = multi.isChecked
    bundle.putLong("questionId", questionId)
    bundle.putLong("survey", surveyId)
    bundle.putBoolean("isMulti", isMulti)
    bundle.putString("questionText", questionText)
  }
  override def onRestoreInstanceState(bundle:Bundle) = {
    super.onRestoreInstanceState(bundle)
    questionId = bundle.getLong("questionId")
    surveyId = bundle.getLong("survey")
    isMulti = bundle.getBoolean("isMulti")
    questionText = bundle.getString("questionText")
  }
}
