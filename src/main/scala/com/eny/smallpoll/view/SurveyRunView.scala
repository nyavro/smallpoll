package com.eny.smallpoll.view

import java.util.concurrent.TimeUnit
import java.util.{TimerTask, Timer, Date}

import android.content.Context
import android.gesture.GestureOverlayView.OnGesturePerformedListener
import android.gesture.{GestureLibrary, Gesture, GestureOverlayView, GestureLibraries}
import android.graphics.PixelFormat
import android.os.{Handler, Bundle}
import android.util.SparseBooleanArray
import android.view.WindowManager.LayoutParams
import android.view._
import android.widget.{Toast, AbsListView, AdapterView, RelativeLayout}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.{Answer, Result}
import com.eny.smallpoll.report.{Marker, MarkerRepository, ResultRepository}
import com.eny.smallpoll.repository.{AnswerRepository, QuestionRepository}
import org.scaloid.common._
import scala.collection.JavaConversions._

/**
 * Created by Nyavro on 13.05.15
 */
class SurveyRunView extends SActivity with Db {
  lazy val questionRepository = new QuestionRepository(instance.getReadableDatabase)
  lazy val answerRepository = new AnswerRepository(instance.getReadableDatabase)
  lazy val resultRepository = new ResultRepository(instance.getWritableDatabase)
  lazy val markerRepository = new MarkerRepository(instance.getWritableDatabase)
  lazy val text = new STextView
  lazy val multiChoice = new SListView
  lazy val singleChoice = new SListView
  lazy val next = new SButton
  lazy val welcome = new STextView
  lazy val thanks = new STextView
  lazy val layout = new SGestureOverlayView
  val handler = new Handler
  lazy val preferences = new Preferences(defaultSharedPreferences)
  lazy val EndDelay = preferences.end_screen_delay(10)
  lazy val InactivityDelay = preferences.inactivity_screen_delay(30)
  var restartTimer = new Timer
  var questionIds = Array[Long]()
  var surveyId = -1L
  var session = -1L
  var isStart = true
  var gestureLib:GestureLibrary = _

  onCreate {
    hideSystem()
    next.setText(R.string.next)
    next.onClick {
      def selectedIds(i: Int, ids: SparseBooleanArray): List[Long] =
        if (i < ids.size)
          if (ids.valueAt(i)) multiChoice.getAdapter.getItem(i).asInstanceOf[Answer].id.getOrElse(-1L) :: selectedIds(i + 1, ids)
          else selectedIds(i + 1, ids)
        else Nil
      selectedIds(0, multiChoice.getCheckedItemPositions).map {
        answerId => resultRepository.save(Result(new Date, answerId))
      }
      questionIds = questionIds.tail
      update()
    }
    singleChoice.onItemClick {
      (adapterView: AdapterView[_], view: View, position: Int, id: Long) =>
        val answerId = singleChoice.getAdapter.getItem(position).asInstanceOf[Answer].id.getOrElse(-1L)
        resultRepository.save(Result(new Date, answerId))
        questionIds = questionIds.tail
        update()
    }
    multiChoice.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE)
    thanks.setText(preferences.thanks(R.string.default_thanks))
    welcome.setText(preferences.welcome(R.string.default_welcome))
    layout.onClick {
      if (questionIds.isEmpty) {
        if(isStart) {

        }
        initArguments()
        update()
      }
    }
    val linearLayout = new SLinearLayout()
    linearLayout.setOrientation(linearLayout.VERTICAL)
    linearLayout += text += multiChoice += singleChoice += next += thanks += welcome
    contentView(
      layout += linearLayout
    )
    enableUnlock()
  }
  def update():Unit = {
    restartTimer.cancel()
    restartTimer = new Timer
    if(questionIds.isEmpty) {
      if(isStart) {
        welcome.setVisibility(View.VISIBLE)
        thanks.setVisibility(View.GONE)
        multiChoice.setVisibility(View.GONE)
        singleChoice.setVisibility(View.GONE)
        next.setVisibility(View.GONE)
        text.setVisibility(View.GONE)
        markerRepository.save(Marker(session, new Date, start = true, surveyId))
        isStart = false
      }
      else {
        welcome.setVisibility(View.GONE)
        thanks.setVisibility(View.VISIBLE)
        multiChoice.setVisibility(View.GONE)
        singleChoice.setVisibility(View.GONE)
        next.setVisibility(View.GONE)
        text.setVisibility(View.GONE)
        markerRepository.save(Marker(session, new Date, start = false, surveyId))
        isStart = true
        scheduleRestart2(EndDelay)
      }
    }
    else {
      scheduleRestart(InactivityDelay)
      val question = questionRepository.load(questionIds.head)
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
      text.setVisibility(View.VISIBLE)
      thanks.setVisibility(View.GONE)
      welcome.setVisibility(View.GONE)
    }
  }
  def initArguments() = {
    surveyId = getIntent.getLongExtra("surveyId", -1L)
    questionIds = questionRepository.list(surveyId).map(_.id.get).toArray
  }
  def enableUnlock() = {
    gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures)
    if (!gestureLib.load()) {
      finish()
    }
    layout.addOnGesturePerformedListener(
      new OnGesturePerformedListener {
        override def onGesturePerformed(overlay: GestureOverlayView, gesture: Gesture): Unit = {
          gestureLib.recognize(gesture).map {
            prediction =>
              if (prediction.score > 5.0) {
                Toast.makeText(SurveyRunView.this, prediction.name + s" score:${prediction.score}", Toast.LENGTH_SHORT).show()
                finish()
              }
          }
        }
      }
    )
  }
  def hideSystem() = {
    new Lock(this).lock()
    getWindow.getDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN)
    getActionBar.hide()
    val params = new LayoutParams(
      WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
      PixelFormat.TRANSPARENT
    )
    params.gravity = Gravity.TOP
    params.width = ViewGroup.LayoutParams.MATCH_PARENT
    params.height = (50 * getResources.getDisplayMetrics.scaledDensity).toInt
    getApplicationContext
      .getSystemService(Context.WINDOW_SERVICE)
      .asInstanceOf[WindowManager]
      .addView(
        new RelativeLayout(this),
        params
      )
  }
  def scheduleRestart(delay: Int) = {
    restartTimer.schedule(
      new TimerTask {
        override def run(): Unit = {
          handler.post(
            new Runnable() {
              override def run(): Unit = {
                initArguments()
                update()
              }
            }
          )
        }
      },
      new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(delay))
    )
  }
  def scheduleRestart2(delay: Int) = {
    restartTimer.schedule(
      new TimerTask {
        override def run(): Unit = {
          handler.post(
            new Runnable() {
              override def run(): Unit = {
                update()
              }
            }
          )
        }
      },
      new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(delay))
    )
  }
  onResume {
    update()
  }
  override def onSaveInstanceState(bundle:Bundle) = {
    super.onSaveInstanceState(bundle)
    bundle.putLong("suveyId", surveyId)
    bundle.putLongArray("questionIds", questionIds)
    bundle.putLong("session", session)
    bundle.putBoolean("isStart", isStart)
  }
  override def onRestoreInstanceState(bundle:Bundle) = {
    super.onRestoreInstanceState(bundle)
    surveyId = bundle.getLong("surveyId")
    questionIds = Array(bundle.getLong("questionIds"))
    session = bundle.getLong("session")
    isStart = bundle.getBoolean("isStart")
  }
  override def onBackPressed() = {
    //Disable Back button
  }
}
