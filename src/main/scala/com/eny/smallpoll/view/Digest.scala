package com.eny.smallpoll.view

import java.security.MessageDigest

import android.util.Base64

class Digest(value:String) {
  def text:String = {
    new String(Base64.encode(MessageDigest.getInstance("MD5").digest(value.getBytes("UTF-8")), Base64.DEFAULT))
  }
}