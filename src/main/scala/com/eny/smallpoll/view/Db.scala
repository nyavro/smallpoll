package com.eny.smallpoll.view

import android.content.Context
import android.util.Log
import com.eny.smallpoll.repository.SmallpollDatabase
import org.scaloid.common.SContext

/**
 * Created by eny on 12.05.15.
 */
trait Db extends SContext {
  Log.d("smallpoll", "Initializing DB")
  lazy val instance = {
    Log.d("smallpoll", "Initializing DB instance")
    new SmallpollDatabase(ctx)
  }

  def asOption(id:Long) = if (id.equals(-1L)) None else Some(id)
}