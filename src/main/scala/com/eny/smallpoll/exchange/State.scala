package com.eny.smallpoll.exchange

import java.io._

import com.eny.smallpoll.repository._
import org.apache.commons.io.IOUtils
import org.scaloid.common.Preferences

import scala.collection.JavaConverters._

/**
 * Created by eny on 06.07.15.
 */


case class State(
  sr:SurveyRepository,
  qr:QuestionRepository,
  ar:AnswerRepository,
  exportSettings:Boolean,
  exportSurveys:Boolean,
  preferences:Preferences,
  dir:String,
  target:String
) {
  lazy val populate = new DatabasePopulate(sr, qr, ar)
  def export() = {
    val list = new java.util.ArrayList[SurveyEx]()
    sr.list().map {
      survey =>
        list.add(
          SurveyEx(
            survey.name,
            qr.list(survey.id.get).map {
              question => QuestionEx(
                question.text,
                ar.list(question.id.get).map {
                  answer => AnswerEx(answer.text)
                },
                question.multi
              )
            }
          )
        )
    }
    lazy val landscape = new File(dir, "landscape.png")
    lazy val portrait = new File(dir, "portrait.png")
    new ExchangeImpl().export[Any](
      List(
        if(exportSurveys) Some("surveys" -> list) else None,
        if(exportSettings) Some("preferences" -> allPrefs) else None,
        if(exportSettings && portrait.exists()) Some("portrait" -> portrait) else None,
        if(exportSettings && landscape.exists()) Some("landscape" -> landscape) else None
      ).filter(_.isDefined)
        .map(_.get)
        .toMap,
      new FileOutputStream(target)
    ) {
      case (key, data, stream) =>
        key match {
          case "landscape" | "portrait" => IOUtils.copy(new FileInputStream(data.asInstanceOf[File]), stream)
          case "surveys" | "preferences" => saveObject(data, stream)
        }
    }
  }

  def saveObject(data: Any, stream: OutputStream) = {
    val os = new ObjectOutputStream(stream)
    os.writeObject(data)
//    os.close()
  }

  def loadObject[A](stream:InputStream):A = {
    val ois = new ObjectInputStream(stream)
    val item = ois.readObject()
//    ois.close()
    item.asInstanceOf[A]
  }

  def allPrefs:Map[String, Any] = {
    preferences.preferences.getAll.asScala.toMap
  }

  def loadFile(stream: InputStream, name: String) = IOUtils.copy(stream, new FileOutputStream(new File(dir, name)))

  def loadSurveys(surveys: java.util.List[SurveyEx]): Unit = {
    populate.clean()
    populate.populate(surveys.asScala.toList)
  }

  def loadPreferences(map: Map[String, Any]): Unit = {
    map.map {
      case (key, value) =>
        preferences.remove(key)
        preferences.updateDynamic(key)(value)
    }
  }

  def `import`() = {
    new ExchangeImpl().`import`(
      new FileInputStream(target)
    ) {
      (name, item) =>
        name match {
          case "surveys" => loadSurveys(loadObject[java.util.List[SurveyEx]](item))
          case "preferences" => loadPreferences(loadObject[Map[String, Any]](item))
          case "portrait" => loadFile(item, "portrait.png")
          case "landscape" => loadFile(item, "landscape.png")
        }
    }
  }

}
