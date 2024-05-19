package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.ml.ModelUnquant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

class HomeActivity : AppCompatActivity() {
    private lateinit var captureImage : ImageView

    private lateinit var resulttext :TextView
    lateinit var bitmap: Bitmap

    fun checkandGetpermissions(){
        if(checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        }
        else{
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        val openCamera=findViewById<Button>(R.id.btnOpenCamera)
        val uploadImage=findViewById<Button>(R.id.btnUploadImage)
        val verifyImage=findViewById<Button>(R.id.btnVerify)
        captureImage = findViewById(R.id.captureImageView)
        val btnAbout=findViewById<TextView>(R.id.textViewAbout)


        checkandGetpermissions()

        val labels = application.assets.open("labels.txt").bufferedReader().use { it.readText() }.split("\n")

        btnAbout.setOnClickListener{
            startActivity(Intent(this@HomeActivity,AboutActivity::class.java))
        }
        openCamera.setOnClickListener{

            var camera : Intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(camera, 200)

            //     contract.launch(imageUrl)
        }
        uploadImage.setOnClickListener{
            Log.d("mssg", "button pressed")
            var intent : Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            startActivityForResult(intent, 250)
            // galleryLauncher.launch("image/*")
        }
        verifyImage.setOnClickListener{



            try {

                var resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
                val model = ModelUnquant.newInstance(this)

                val tensorImage = TensorImage.fromBitmap(resized)

// Creates inputs for reference.
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(tensorImage.buffer)
                var tbuffer = TensorImage.fromBitmap(resized)
                var byteBuffer = tbuffer.buffer
               // val byteBuffer: ByteBuffer = convertBitmapToByteBuffer(resizedImage)
                inputFeature0.loadBuffer(byteBuffer)

                // Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer


                model.close()
            }catch (e:Exception){
                Toast.makeText(this@HomeActivity,"Image doesnt match",Toast.LENGTH_LONG).show()
            }




        }


            }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 250){
            captureImage.setImageURI(data?.data)

            var uri : Uri ?= data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }
        else if(requestCode == 200 && resultCode == Activity.RESULT_OK){
            bitmap = data?.extras?.get("data") as Bitmap


                    captureImage.setImageBitmap(bitmap)
        }

    }

}