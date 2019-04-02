package com.company.archapp

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState

@Suppress("DEPRECATION")
class ResultActivity : AppCompatActivity() {

    private val slidingPanelLayout by lazy { findViewById<SlidingUpPanelLayout>(R.id.sliding_panel)!! }
    private val landmarkIv by lazy { findViewById<ImageView>(R.id.landmark_iv) }
    private val dragview by lazy { findViewById<LinearLayout>(R.id.dragview) }
    private val landmarkTv by lazy { findViewById<TextView>(R.id.landmark_tv) }
    private val resultPb by lazy { findViewById<ProgressBar>(R.id.result_pb) }

    private var nameOfLandmark: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val intent = intent
        val imageUri = intent.getParcelableExtra<Uri>(WelcomeActivity.IMAGE_URI)

        analyzeImage(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))

        slidingPanelLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                // Nothing to do here
            }

            override fun onPanelStateChanged(
                panel: View,
                previousState: PanelState,
                newState: PanelState
            ) {
                // When sliding panel transform to EXPANDED state, we remove radius from background
                if (newState == PanelState.EXPANDED) {
                    dragview.background = getDrawable(R.color.sliding_panel_color)
                } else {
                    // Otherwise we set to sliding panel radius
                    dragview.background = getDrawable(R.drawable.sliding_panel_radius)
                }
            }
        })
    }

    private fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        landmarkIv.setImageBitmap(null)
        showProgress()

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val options = FirebaseVisionCloudDetectorOptions.Builder()
            .setMaxResults(5)
            .build()
        val landmarkDetector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector(options)
        landmarkDetector.detectInImage(firebaseVisionImage)
            .addOnSuccessListener {
                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)

                recognizeLandmarks(it, mutableImage)

                landmarkIv.setImageBitmap(mutableImage)
                hideProgress()
                landmarkTv.text = nameOfLandmark
            }
            .addOnFailureListener {
                Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
                hideProgress()
            }
    }



    private fun recognizeLandmarks(landmarks: List<FirebaseVisionCloudLandmark>?, image: Bitmap?) {
        if (landmarks == null || image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        for (landmark in landmarks) {
            nameOfLandmark = landmark.landmark
        }
    }

    private fun showProgress() {
        slidingPanelLayout.visibility = View.GONE
        resultPb.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        slidingPanelLayout.visibility = View.VISIBLE
        resultPb.visibility = View.GONE
    }
}