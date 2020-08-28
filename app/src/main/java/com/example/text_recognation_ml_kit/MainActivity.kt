package com.example.text_recognation_ml_kit

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.kotlinpermissions.KotlinPermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_click.setOnClickListener {
            KotlinPermissions.with(this) // where this is an FragmentActivity instance
                .permissions(Manifest.permission.CAMERA)
                .onAccepted { permissions ->
                    dispatchTakePictureIntent()
                }
                .onDenied { permissions ->
                    //List of denied permissions
                }
                .onForeverDenied { permissions ->
                    //List of forever denied permissions
                }
                .ask()
        }
    }


    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            textRecognize(imageBitmap)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun textRecognize(mBitmap : Bitmap) {
        val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
            .setLanguageHints(Arrays.asList("en", "hi"))
            .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
            .build()

        val image = FirebaseVisionImage.fromBitmap(mBitmap)
        val detector = FirebaseVision.getInstance().getCloudTextRecognizer(options)

        detector.processImage(image).addOnSuccessListener { texts -> processTextRecognitionResult(texts) }
            .addOnFailureListener { e -> e.printStackTrace() }
    }

    private fun processTextRecognitionResult(firebaseVisionText: FirebaseVisionText) {
        txv_result.setText(null)
        if (firebaseVisionText.textBlocks.size == 0) {
            txv_error.setText("Can't find text!")
            return
        }
        for (block in firebaseVisionText.textBlocks) {
            txv_result.append(block.text)

            //In case you want to extract each line
            /*
			for (FirebaseVisionText.Line line: block.getLines()) {
				for (FirebaseVisionText.Element element: line.getElements()) {
					mTextView.append(element.getText() + " ");
				}
			}
			*/
        }
    }
}

