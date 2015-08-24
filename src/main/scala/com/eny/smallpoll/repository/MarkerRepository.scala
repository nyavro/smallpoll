package com.eny.smallpoll.repository

import java.util.Date

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Marker

/**
  * Created by Nyavro on 13.05.15
  */
class MarkerRepository(db: SQLiteDatabase) extends CursorConversion {

  val Table = "marker"
  val Session = "session"
  val DateField = "date"
  val StartField = "start"
  val SurveyIdField = "survey_id"

  def init() = {
    db.execSQL(s"CREATE TABLE $Table (session LONG NOT NULL, date INTEGER NOT NULL, start BOOLEAN NOT NULL, survey_id LONG NOT NULL, FOREIGN KEY(survey_id) REFERENCES survey(_id) ON DELETE CASCADE)")
  }

  def save(marker:Marker) = {
    db.insert(Table, null, Values(Map(Session->marker.session, DateField->marker.date, StartField->marker.start, SurveyIdField->marker.surveyId)).content)
  }
  
  def count(from: Date, to: Date, surveyId: Long): Int = {
    val cursor = db.query(
      Table,
      Array(s"COUNT($Session)"),
      s"$DateField > ? AND $DateField <= ? AND $SurveyIdField = ? AND $StartField = ?", Array(from.getTime.toString, to.getTime.toString, surveyId.toString, 1.toString), null, null, null
    )
    cursor.moveToFirst
    cursor.getInt(0)
  }

  def clean() = {
    db.execSQL(s"DELETE FROM $Table", Array())
  }
  
  def convert(cursor:Cursor) = cursor.getLong(0) -> cursor.getInt(1)
 }
