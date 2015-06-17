package com.eny.smallpoll.view.components

import java.io.FileOutputStream

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory.Options
import android.graphics.{Matrix, Color, Bitmap, BitmapFactory}
import android.util.DisplayMetrics
import android.view.ViewGroup.{MarginLayoutParams, LayoutParams}
import android.view.{ViewGroup, Menu, MenuItem}
import android.widget.RelativeLayout
import com.eny.smallpoll.R
import com.eny.smallpoll.view.{Db, PreferencesView}
import org.scaloid.common._

class ImageCropView extends SActivity {
  lazy val image = new SImageView
  lazy val select = new SImageView
  lazy val rotate = new SButton()
  var sourcePath:String = _
  var targetPath:String = _

  def metrics = {
    val res = new DisplayMetrics
    getWindowManager.getDefaultDisplay.getMetrics(res)
    res
  }

  def crop(srcPath: String, metrics: DisplayMetrics, bound:Options): Bitmap = {
    val maxSize = metrics.heightPixels.min(metrics.widthPixels)
    val resize = new BitmapFactory.Options()
    resize.inSampleSize =
      if (bound.outHeight > maxSize || bound.outWidth > maxSize) {
        1 >> Math.ceil(Math.log(maxSize / bound.outHeight.max(bound.outWidth) / Math.log(0.5))).toInt
      } else {
        1
      }
    BitmapFactory.decodeFile(srcPath, resize)
  }

  def imageBounds(srcPath: String) = {
    val bound:Options = new BitmapFactory.Options()
    bound.inJustDecodeBounds = true
    BitmapFactory.decodeFile(srcPath, bound)
    bound
  }

  def rotateImage(bitmap: Bitmap): Bitmap = {
    val matrix = new Matrix
    matrix.postRotate(90)
    Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth, bitmap.getHeight, matrix, true)
  }
  def cleanly[A,B](resource: => A)(cleanup: A => Unit)(code: A => B): Option[B] =
    try {
      val r = resource
      try { Some(code(r)) }
      finally { cleanup(r) }
    } catch {
      case e: Exception => None
    }

  def save(bitmap: Bitmap, target: String) = {
    val progress = ProgressDialog.show(this, "", getString(R.string.saving))
    new Thread() {
      override def run() = {
        cleanly(new FileOutputStream(target))(_.close) {
          fos => bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        progress.dismiss()
        finish()
      }
    }.start()
  }

  onCreate {
    sourcePath = getIntent.getStringExtra(ImageCropView.SourcePath)
    targetPath = getIntent.getStringExtra(ImageCropView.TargetPath)
    val buttons = new SLinearLayout()
    val ok = new SButton()
    val rotate = new SButton()
    ok.setText("OK")
    rotate.setText("Повернуть")
    val actions = new SLinearLayout {
      ok.<<.Weight(1.0f).>>
      rotate.<<.Weight(1.0f).>>
    } += ok += rotate
    val main = new SRelativeLayout {
      image.<<.centerInParent.>>
      actions.<<.alignParentBottom.>>
    } += image += actions
    val screenSize = metrics
    val imageOptions = imageBounds(sourcePath)
    var crop1: Bitmap = crop(sourcePath, screenSize, imageOptions)
    image.setImageBitmap(crop1)
    rotate.onClick {
      crop1 = rotateImage(crop1)
      image.setImageBitmap(crop1)
    }
    ok.onClick {
      save(crop1, targetPath)
    }
    //    select.setBackgroundResource(R.drawable.select)
    val selectHeight = imageOptions.outHeight * screenSize.widthPixels / imageOptions.outWidth
    val selectWidth = selectHeight*screenSize.widthPixels/screenSize.heightPixels
    val params = new RelativeLayout.LayoutParams(selectHeight, selectWidth)
//    select.setLayoutParams(params)
//    select.setTop(500)
//    select.setLeft(500)
//    select.setAlpha(0.5f)
    setContentView(main)
  }

  override def onCreateOptionsMenu(menu:Menu):Boolean = {
    getMenuInflater.inflate(R.menu.main, menu)
    true
  }

  override def onOptionsItemSelected(item:MenuItem):Boolean =
    item.getItemId match {
      case R.id.preferences => new Intent().start[PreferencesView];true
      case _ => super.onOptionsItemSelected(item)
    }
}

object ImageCropView {
  val SourcePath = "sourcePath"
  val TargetPath = "targetPath"
}