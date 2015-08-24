package com.eny.smallpoll.exchange

import java.io.{BufferedOutputStream, OutputStream, InputStream}
import java.util.zip.{ZipInputStream, ZipEntry, ZipOutputStream}

import org.apache.commons.io.IOUtils

trait Exchange {

  def `import`(source:InputStream)(proc: (String, InputStream) => Unit):Unit

  def export[A](chunks:Map[String, A], target:OutputStream)(proc: (String, A, OutputStream) => Unit): Unit
}

class ExchangeImpl extends Exchange {

  override def export[A](chunks:Map[String, A], target:OutputStream)(proc: (String, A, OutputStream) => Unit) = {
    val zip = new ZipOutputStream(new BufferedOutputStream(target))
    chunks.map {
      case (key, data) =>
        zip.putNextEntry(new ZipEntry(key))
        proc(key, data, zip)
    }
    zip.close()
  }

  override def `import`(source:InputStream)(proc: (String, InputStream) => Unit): Unit = {
    val zip = new ZipInputStream(source)
    def process():Unit = {
      val entry = zip.getNextEntry
      if(entry!=null) {
        proc(entry.getName, zip)
        process()
      }
    }
    process()
    zip.close()
  }
}