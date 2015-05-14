package com.eny.smallpoll.view

import java.util.Date

import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.View
import android.widget.AbsListView
import com.eny.smallpoll.R
import com.eny.smallpoll.model.{Answer, Result}
import com.eny.smallpoll.repository.{AnswerRepository, QuestionRepository, ResultRepository}
import org.scaloid.common._

/**
 * Created by Nyavro on 13.05.15
 */
class SurveyRunView extends SActivity with Db {
  lazy val questionRepository = new QuestionRepository(instance.getReadableDatabase)
  lazy val answerRepository = new AnswerRepository(instance.getReadableDatabase)
  lazy val resultRepository = new ResultRepository(instance.getWritableDatabase)
  lazy val text = new STextView
  lazy val multiChoice = new SListView
  lazy val singleChoice = new SListView
  lazy val next = new SButton
  var questionIds = Array[Long]()
  onCreate {
    questionIds = questionRepository.list(getIntent.getLongExtra("surveyId", -1)).map(_.id.get).toArray
    if(questionIds.isEmpty) {
    } else {
      next.setText(R.string.next)
      next.onClick {
        def selectedIds(i:Int, ids:SparseBooleanArray):List[Long] = {
          if(i<ids.size) {
            if(ids.valueAt(i)) {
              val orElse: Long = multiChoice.getAdapter.getItem(i).asInstanceOf[Answer].id.getOrElse(-1L)
              orElse::selectedIds(i+1, ids)
            } else {
              selectedIds(i+1, ids)
            }
          } else {
            Nil
          }
        }
        selectedIds(0, multiChoice.getCheckedItemPositions).map {
          answerId => resultRepository.save(Result(new Date, answerId))
        }
        questionIds = questionIds.tail
        update
      }
      multiChoice.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE)
      contentView(
        new SVerticalLayout += text += multiChoice += singleChoice += next
      )
    }
  }
  def update() = {
    val question = questionRepository.load(questionIds(0))
    val answers = answerRepository.list(question.id.get)
    text.setText(question.text)
    if(question.multi) {
      multiChoice.setAdapter(new SArrayAdapter(answers.toArray, android.R.layout.simple_list_item_multiple_choice))
      multiChoice.setVisibility(View.VISIBLE)
      singleChoice.setVisibility(View.GONE)
      next.setVisibility(View.VISIBLE)
    } else {
      singleChoice.setAdapter(new SArrayAdapter(answers.toArray))
      multiChoice.setVisibility(View.GONE)
      singleChoice.setVisibility(View.VISIBLE)
      next.setVisibility(View.GONE)
    }
  }
  onResume {
    update()
  }
  override def onSaveInstanceState(bundle:Bundle) = {
    super.onSaveInstanceState(bundle)
    bundle.putLongArray("questionIds", questionIds)
  }
  override def onRestoreInstanceState(bundle:Bundle) = {
    super.onRestoreInstanceState(bundle)
    questionIds = Array(bundle.getLong("questionIds"))
  }
}
