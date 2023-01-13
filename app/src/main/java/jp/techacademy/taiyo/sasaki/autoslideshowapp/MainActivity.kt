package jp.techacademy.taiyo.sasaki.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    //パーション
    private val PERMISSIONS_REQUET_CODE = 100

    //再生/停止ボタンの初期設定
    private var buttonFlag = true

    private var mHandler = Handler()
    private var mTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //再生/停止ボタンの初期設定
        start_button.text = "再生"

        //Android6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //許可されている
                getContentsInfo()
            } else {
                //許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUET_CODE
                )
            }
            //Android5.0以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantsResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUET_CODE ->
                if (grantsResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    textView.text = "ストレージへのアクセスを許可してください"
                }
        }
    }

    private fun getContentsInfo() {
        //画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //データの種類
            null,     //  項目(null = 全項目)
            null,      //  フィルタ条件 (null = フィルタなし)
            null,  //  フィルタ用パラメータ
            null      //  ソート
        )

        //ボタンが押される前に1枚目を表示
        if (cursor!!.moveToFirst()) {
            showImage(cursor)
        } else {
            textView.text = "画像が見つかりません"
        }

        //進むボタンの動作
        next_button.setOnClickListener {
            if (cursor.moveToNext()) {
                showImage(cursor)
            } else if (cursor.moveToFirst()) {
                showImage(cursor)
            }
        }

        //戻るボタンの動作
        previous_button.setOnClickListener {
            if (cursor.moveToPrevious()) {
                showImage(cursor)
            } else if (cursor.moveToLast()) {
                showImage(cursor)
            }
        }

        //再生/停止ボタンの動作
        start_button.setOnClickListener {
            when (buttonFlag) {
                true -> startSlideShow(cursor)
                false -> stopSlideShow(cursor)
            }
        }
    }

    private fun showImage (cursor:Cursor) {
        //indexからIDを取得し、そのIDから画像のURIを取得する
        val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        Log.d("ANDROID", "URI : " + imageUri.toString() )
        imageView.setImageURI(imageUri)
    }

    private fun startSlideShow(cursor:Cursor) {
        //ボタン設定
        start_button.text = "停止"
        buttonFlag = false
        next_button.isEnabled = false
        previous_button.isEnabled = false

        if (mTimer == null) {
            mTimer = Timer()
            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    if (cursor.moveToNext()) {
                        mHandler.post {
                            showImage(cursor)
                        }
                    } else if (cursor.moveToFirst()) {
                        mHandler.post {
                            showImage(cursor)
                        }
                    }
                }
            }, 2000, 2000)
        }
    }

    private fun stopSlideShow(cursor:Cursor) {
        //ボタン設定
        start_button.text = "再生"
        buttonFlag = true
        next_button.isEnabled = true
        previous_button.isEnabled = true

        if (mTimer != null) {
            mTimer!!.cancel()
            mTimer = null
        }

    }

}
