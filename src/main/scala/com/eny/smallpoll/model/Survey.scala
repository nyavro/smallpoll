package com.eny.smallpoll.model

/**
 * Created by eny on 25.04.15.
 */
case class Survey(private val id:Long, name:String, question:List[Question])
