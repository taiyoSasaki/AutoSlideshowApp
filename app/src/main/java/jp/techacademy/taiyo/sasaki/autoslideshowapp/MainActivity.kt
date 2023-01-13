package jp.techacademy.taiyo.sasaki.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUET_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Android6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //許可されている
                getContentsInfo()
            } else {
                //許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUET_CODE)
            }
            //Android5.0以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantsResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUET_CODE ->
                if (grantsResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private  fun getContentsInfo() {
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

        next_button.setOnClickListener {
            if (cursor.moveToNext()) {
                showImage(cursor)
            } else if (cursor.moveToFirst()){
                showImage(cursor)
            }
        }

        previous_button.setOnClickListener {
            if (cursor.moveToPrevious()) {
                showImage(cursor)
            } else if (cursor.moveToLast()){
                showImage(cursor)
            }
        }



    }

    private fun showImage (cursorx:Cursor) {
        //indexからIDを取得し、そのIDから画像のURIを取得する
        val fieldIndex = cursorx.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursorx.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        Log.d("ANDROID", "URI : " + imageUri.toString() )
        imageView.setImageURI(imageUri)
    }


}
