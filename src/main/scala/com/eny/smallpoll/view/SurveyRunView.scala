package com.eny.smallpoll.view

import java.io.File
import java.util.concurrent.TimeUnit
import java.util.{Date, Timer, TimerTask}

import android.gesture.GestureOverlayView.OnGesturePerformedListener
import android.gesture.{Gesture, GestureLibraries, GestureLibrary, GestureOverlayView}
import android.graphics.{BitmapFactory, Color, Typeface}
import android.media.MediaPlayer
import android.os.{Bundle, Handler}
import android.util.SparseBooleanArray
import android.view._
import android.widget._
import com.eny.smallpoll.R
import com.eny.smallpoll.model.{Answer, Marker, Result}
import com.eny.smallpoll.report.ResultRepository
import com.eny.smallpoll.repository.{AnswerRepository, MarkerRepository, QuestionRepository}
import org.scaloid.common._
import org.scaloid.util.Configuration._

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
  lazy val systemLock = new SystemLock(this)
  lazy val typeface = {
    val fontPath = preferences.font_path("")
    if(fontPath.isEmpty) None else Some(Typeface.createFromFile(fontPath))
  }
  val handler = new Handler
  var restartTimer = new Timer
  var questionIds = Array[Long]()
  var surveyId = -1L
  var session = -1L
  var isStart = true
  var gestureLib:GestureLibrary = _

  onCreate {
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
    val splashColor = preferences.splash_color(Color.WHITE)
    def initTextView(view:TextView, text:String, textSize:Float, color:Int, typeface:Option[Typeface]) = {
      view.setText(text)
      view.setTextSize(textSize)
      view.setTextColor(color)
      view.gravity(Gravity.CENTER)
      typeface.map(view.setTypeface)
    }
    initTextView(thanks, preferences.thanks(getString(R.string.thanks_default)), splashTextSize, splashColor, typeface)
    initTextView(welcome, preferences.welcome(getString(R.string.welcome_default)), splashTextSize, splashColor, typeface)
    initTextView(text, "", preferences.question_text_size(getString(R.string.question_text_size_default).toInt).toFloat, preferences.question_text_color(Color.WHITE), typeface)
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
    val back = initBackground
    val main = new SRelativeLayout {
      back.<<.centerInParent.>>
      thanks.<<.centerInParent.>>
      welcome.<<.centerInParent.>>
      runArea.<<.fill.>>
    } += back += thanks += welcome += runArea
    contentView(
      layout += main
    )
    enableUnlock()
  }

  def initBackground: View with TraitView[_ >: SImageView with SVideoView <: View with TraitView[_ >: SImageView with SVideoView]] = {
    val videoPath = preferences.background_video_path("")
    if (videoPath.isEmpty) {
      val back = new SImageView
      val bckgrnd = new File(getApplicationContext.getFilesDir, if (landscape) "landscape.png" else "portrait.png")
      if (bckgrnd.exists) {
        back.setImageBitmap(BitmapFactory.decodeFile(bckgrnd.getPath))
      }
      back
    }
    else {
      val back = new SVideoView
      back.videoPath = preferences.background_video_path("")
      back.onPrepared { mp: MediaPlayer => mp.setLooping(true)}
      back.start()
      back
    }
  }

  def update():Unit = {
    restartTimer.cancel()
    restartTimer = new Timer
    if(questionIds.isEmpty) {
      markerRepository.save(Marker(session, new Date, start = isStart, surveyId))
      if(!isStart) {
        scheduleRestart(EndDelay, {update()})
      }
      isStart = !isStart
      welcome.setVisibility(if(isStart) View.VISIBLE else View.GONE)
      thanks.setVisibility(if(isStart) View.GONE else View.VISIBLE)
      multiChoice.setVisibility(View.GONE)
      singleChoice.setVisibility(View.GONE)
      next.setVisibility(View.GONE)
      text.setVisibility(View.GONE)
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
      val adapter = new CustomAdapter(
        answers.toArray,
        R.layout.custom_multichoice_layout,
        typeface,
        Some(preferences.answer_text_color(Color.WHITE)),
        Some(preferences.answer_text_size(getString(R.string.question_text_size_default).toInt).toFloat)
      )
      if(question.multi) {
        multiChoice.setAdapter(adapter)
      } else {
        singleChoice.setAdapter(adapter)
      }
      multiChoice.setVisibility(if(question.multi) View.VISIBLE else View.GONE)
      next.setVisibility(if(question.multi) View.VISIBLE else View.GONE)
      singleChoice.setVisibility(if(question.multi) View.GONE else View.VISIBLE)
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
                systemLock.unlock()
                Toast.makeText(SurveyRunView.this, prediction.name + s" score:${prediction.score}", Toast.LENGTH_SHORT).show()
                finish()
              }
          }
        }
      }
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
    systemLock.lock()
    update()
  }
  override def onSaveInstanceState(bundle:Bundle) = {
    super.onSaveInstanceState(bundle)
    systemLock.unlock()
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
