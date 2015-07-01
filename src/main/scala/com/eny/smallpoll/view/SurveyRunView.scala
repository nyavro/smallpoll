package com.eny.smallpoll.view

import java.io.File
import java.util.concurrent.TimeUnit
import java.util.{Date, Timer, TimerTask}

import android.content.Context
import android.gesture.GestureOverlayView.OnGesturePerformedListener
import android.gesture.{Gesture, GestureLibraries, GestureLibrary, GestureOverlayView}
import android.graphics.{Typeface, BitmapFactory, Color, PixelFormat}
import android.os.{Build, Bundle, Handler}
import android.util.SparseBooleanArray
import android.view.WindowManager.LayoutParams
import android.view._
import android.widget._
import com.eny.smallpoll.R
import com.eny.smallpoll.model.{Marker, Answer, Result}
import com.eny.smallpoll.report.ResultRepository
import com.eny.smallpoll.repository.{MarkerRepository, AnswerRepository, QuestionRepository}
import org.scaloid.common._
import scala.collection.JavaConversions._
import org.scaloid.util.Configuration._
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
    thanks.setText(preferences.thanks(getString(R.string.thanks_default)))
    thanks.setTextSize(splashTextSize)
    thanks.gravity(Gravity.CENTER)
    thanks.setTextColor(splashColor)
    welcome.setText(preferences.welcome(getString(R.string.welcome_default)))
    welcome.setTextSize(splashTextSize)
    welcome.gravity(Gravity.CENTER)
    welcome.setTextColor(splashColor)
    text.setTextSize(preferences.question_text_size(getString(R.string.question_text_size_default).toInt).toFloat)
    text.gravity(Gravity.CENTER)
    text.setTextColor(preferences.question_text_color(Color.WHITE))
    val fontPath = preferences.font_path("")
    if(!fontPath.isEmpty) {
      val typeface = Typeface.createFromFile(fontPath)
      thanks.setTypeface(typeface)
      welcome.setTypeface(typeface)
      text.setTypeface(typeface)
    }
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
    val back = new SImageView
      val bckgrnd = new File(getApplicationContext.getFilesDir, if(landscape) "landscape.png" else "portrait.png")
      if(bckgrnd.exists) {
        back.setImageBitmap(BitmapFactory.decodeFile(bckgrnd.getPath))
      }
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
      val typeface = {
        preferences.font_path("") match {
          case "" => None
          case path => Some(path)
        }
      }.map (Typeface.createFromFile)
      if(question.multi) {
        multiChoice.setAdapter(new CustomAdapter(answers.toArray, R.layout.custom_multichoice_layout, typeface))
        multiChoice.setVisibility(View.VISIBLE)
        singleChoice.setVisibility(View.GONE)
        next.setVisibility(View.VISIBLE)
      } else {
        singleChoice.setAdapter(new CustomAdapter(answers.toArray, R.layout.custom_singlechoice_layout, typeface))
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

class CustomAdapter(items:Array[Answer], res:Int, typeface:Option[Typeface])(implicit context: android.content.Context) extends SArrayAdapter[Nothing, Answer](items, res) {
  lazy val prefs = new Preferences(defaultSharedPreferences)

  def ensureView(view:View, id:Int, group:ViewGroup) =
    if(view==null) {
      getContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater].inflate(id, group, false)
    }
    else {
      view
    }
  
  override def getView(position:Int, v:View, parent:ViewGroup):View = {
    val mView = ensureView(v, res, parent)
    val text = mView.findViewById(android.R.id.text1).asInstanceOf[TextView]
    if(position < items.length) {
      text.setText(items(position).toString)
      text.setTextColor(prefs.answer_text_color(Color.WHITE))
      text.setTextSize(prefs.answer_text_size(context.getString(R.string.question_text_size_default).toInt).toFloat)
      typeface.map {
        item => text.setTypeface(item)
      }
    }
    mView
  }

}
