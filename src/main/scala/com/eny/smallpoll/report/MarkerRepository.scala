package com.eny.smallpoll.report

import java.util.Date

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.eny.smallpoll.model.Result
import com.eny.smallpoll.repository.{CursorConversion, Values}

/**
  * Created by Nyavro on 13.05.15
  */
class MarkerRepository(db: SQLiteDatabase) extends CursorConversion {

  val Table = "marker"
  val Session = "session"
  val DateField = "date"
  val StartField = "start"
  val SurveyIdField = "survey_id"

   def save(marker:Marker) = {
     db.insert(Table, null, Values(Map(Session->marker.session, DateField->marker.date, StartField->marker.start, SurveyIdField->marker.surveyId)).content)
   }

   def convert(cursor:Cursor) = cursor.getLong(0) -> cursor.getInt(1)
 }
