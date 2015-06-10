package com.eny.smallpoll.view

import java.util.concurrent.TimeUnit
import java.util.{TimerTask, Timer, Date}

import android.content.Context
import android.content.res.Resources
import android.gesture.GestureOverlayView.OnGesturePerformedListener
import android.gesture.{GestureLibrary, Gesture, GestureOverlayView, GestureLibraries}
import android.graphics.{Color, Bitmap, BitmapFactory, PixelFormat}
import android.graphics.drawable.Drawable
import android.os.{Build, Handler, Bundle}
import android.util.{DisplayMetrics, SparseBooleanArray}
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
  
  val SurveyID = "surveyId"
  val QuestionIDs = "questionIds"
  val IsStart = "isStart"
  val SessionID = "sessionID"
  
  lazy val questionRepository = new QuestionRepository(instance.getReadableDatabase)
  lazy val answerRepository = new AnswerRepository(instance.getReadableDatabase)
  lazy val resultRepository = new ResultRepository(instance.getWritableDatabase)
  lazy val markerRepository = new MarkerRepository(instance.getWritableDatabase)
  lazy val text = new STextView
  lazy val multiChoice = new SListView
  lazy val singleChoice = new SListView
  lazy val next = new SButton(getString(R.string.next))
  lazy val welcome = new STextView
  lazy val thanks = new STextView
  lazy val layout = new SGestureOverlayView
  lazy val preferences = new Preferences(defaultSharedPreferences)
  lazy val EndDelay = preferences.end_screen_delay(getString(R.string.end_screen_delay_default).toInt)
  lazy val InactivityDelay = preferences.inactivity_screen_delay(getString(R.string.restart_delay_default).toInt)
  lazy val UnlockGestureThreshold = preferences.gesture_threshold(getString(R.string.gesture_threshold_default).toInt)
  lazy val lock: Lock = new Lock(this)
  val handler = new Handler
  var restartTimer = new Timer
  var questionIds = Array[Long]()
  var surveyId = -1L
  var session = -1L
  var isStart = true
  var gestureLib:GestureLibrary = _

  onCreate {
    hideSystem()
    next.onClick {
      def selectedIds(i: Int, ids: SparseBooleanArray): List[Long] =
        if (i < ids.size)
          if (ids.valueAt(i)) multiChoice.getAdapter.getItem(i).asInstanceOf[Answer].id.getOrElse(-1L) :: selectedIds(i + 1, ids)
          else selectedIds(i + 1, ids)
        else Nil
      selectedIds(0, multiChoice.getCheckedItemPositions).map {
        answerId => resultRepository.save(Result(new Date, answerId))
      }
      questionIds = if (questionIds.isEmpty) Array() else questionIds.tail
      update()
    }
    singleChoice.onItemClick {
      (adapterView: AdapterView[_], view: View, position: Int, id: Long) =>
        val answerId = singleChoice.getAdapter.getItem(position).asInstanceOf[Answer].id.getOrElse(-1L)
        resultRepository.save(Result(new Date, answerId))
        questionIds = if (questionIds.isEmpty) Array() else questionIds.tail
        update()
    }
    multiChoice.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE)
    val splashTextSize = preferences.splash_text_size(getString(R.string.splash_text_size_default).toInt)
    thanks.setText(preferences.thanks(getString(R.string.thanks_default)))
    thanks.setTextSize(splashTextSize)
    thanks.gravity(Gravity.CENTER)
    thanks.setTextColor(Color.BLUE)
    welcome.setText(preferences.welcome(getString(R.string.welcome_default)))
    welcome.setTextSize(splashTextSize)
    welcome.gravity(Gravity.CENTER)
    welcome.setTextColor(Color.BLUE)
    text.setTextSize(preferences.question_text_size(getString(R.string.question_text_size_default).toInt).toFloat)
    text.gravity(Gravity.CENTER)
    text.setTextColor(Color.BLUE)
    layout.onClick {
      if (questionIds.isEmpty && !isStart) {
        initArguments()
        update()
      }
    }
    val topArea = new SRelativeLayout {
      text.<<.centerInParent.>>
    } += text
    val bottomArea = new SRelativeLayout {
      next.<<.centerInParent.>>
    } += next
    val centerArea = new SRelativeLayout {
      multiChoice.<<.centerInParent.>>
      singleChoice.<<.centerInParent.>>
    } += multiChoice += singleChoice
    val runArea = new SLinearLayout {
      topArea.<<.Weight(1.0f).>>
      centerArea.<<.Weight(1.0f).>>
      bottomArea.<<.Weight(1.0f).>>
    } += topArea += centerArea += bottomArea
    runArea.setOrientation(runArea.VERTICAL)
    val main = new SRelativeLayout {
      thanks.<<.centerInParent.>>
      welcome.<<.centerInParent.>>
      runArea.<<.fill.>>
    } += thanks += welcome += runArea
    main.setBackground(resources.getDrawable(R.drawable.vertical_blue_800_1200))
    contentView(
      layout += main
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
        scheduleRestart(
        EndDelay, {
          update()
        }
        )
      }
    }
    else {
      scheduleRestart(
      InactivityDelay, {
        questionIds = Array()
        isStart = true
        update()
      }
      )
      val question = questionRepository.load(questionIds.head)
      val answers = answerRepository.list(question.id.get)
      text.setText(question.text)
      if(question.multi) {
        multiChoice.setAdapter(new SArrayAdapter(answers.toArray, R.layout.custom_multichoice_layout))
        multiChoice.setVisibility(View.VISIBLE)
        singleChoice.setVisibility(View.GONE)
        next.setVisibility(View.VISIBLE)
      } else {
        singleChoice.setAdapter(new SArrayAdapter(answers.toArray, R.layout.custom_singlechoice_layout))
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
    surveyId = getIntent.getLongExtra(SurveyID, -1L)
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
              if (prediction.score > UnlockGestureThreshold) {
                lock.release()
                Toast.makeText(SurveyRunView.this, prediction.name + s" score:${prediction.score}", Toast.LENGTH_SHORT).show()
                finish()
              }
          }
        }
      }
    )
  }
  def hideSystem() = {
    if(Build.VERSION.SDK_INT>Build.VERSION_CODES.JELLY_BEAN) {
      lock.lock()
    }
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
  def scheduleRestart(delay: Int, code: => Unit) = {
    restartTimer.schedule(
      new TimerTask {
        override def run(): Unit = {
          handler.post(
            new Runnable() {
              override def run(): Unit = code
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
    bundle.putLong(SurveyID, surveyId)
    bundle.putLongArray(QuestionIDs, questionIds)
    bundle.putLong(SessionID, session)
    bundle.putBoolean(IsStart, isStart)
  }
  override def onRestoreInstanceState(bundle:Bundle) = {
    super.onRestoreInstanceState(bundle)
    surveyId = bundle.getLong(SurveyID)
    questionIds = bundle.getLongArray(QuestionIDs)
    session = bundle.getLong(SessionID)
    isStart = bundle.getBoolean(IsStart)
  }
  override def onBackPressed() = {
    //Disable Back button
  }
}
