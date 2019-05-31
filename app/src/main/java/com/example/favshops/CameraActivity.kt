package com.example.favshops

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var currentPhotoPath: String
    private val REQUEST_TAKE_PHOTO = 1
    private val photoURIForAddShop: Uri = Uri.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        dispatchTakePictureIntent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("---", "onActivityResult")
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if(resultCode == RESULT_OK) {
                //Thumbnail
//                val photo = data?.extras?.get("data") as? Bitmap
//                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
//                    val f = File(currentPhotoPath)
//                    mediaScanIntent.data = Uri.fromFile(f)
//                    sendBroadcast(mediaScanIntent)
////                    setResult(MainActivity.PHOTO_REQUEST, mediaScanIntent)
////                    finish()
//                }
                val i = Intent().apply {
                    action = "com.example.favshops.PHOTO"
                    putExtra("currentPhotoPath", currentPhotoPath)
//                    val f = File(currentPhotoPath)
//                    photoIntent.data = Uri.fromFile(f)
//                    putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f))
//                    type = "image/jpeg"
                }
                Log.d("---", "sendBroadcast")
                sendBroadcast(i)
//                finish()
            } else {
                File(currentPhotoPath).delete()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) // miejsce gdzie będą zapisywane zdjęcia
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.apply {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }

                photoFile?.let {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this@CameraActivity,
                        "com.example.favshops.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }
}