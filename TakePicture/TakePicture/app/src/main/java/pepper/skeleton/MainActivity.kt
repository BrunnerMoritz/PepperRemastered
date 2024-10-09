package pepper.skeleton

import android.content.ContentValues.TAG
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.compose.material.*
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TakePictureBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.`object`.camera.TakePicture
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.`object`.image.EncodedImage
import com.aldebaran.qi.sdk.`object`.image.EncodedImageHandle
import java.nio.ByteBuffer
import androidx.activity.compose.setContent as setContent


class MainActivity : RobotActivity(), RobotLifecycleCallbacks {

    val onPepper = true
    private var qiContext: QiContext? = null

    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?,) {
        super.onCreate(savedInstanceState)
        QiSDK.register(this,this)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.picture_view)
        val Button = findViewById<Button>(R.id.take_pic_button)
        Button.setOnClickListener { takePic() }
    }


    override fun onDestroy() {
        this.qiContext = null
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override fun onRobotFocusGained(qiContext: QiContext) {
        this.qiContext = qiContext
    }

    override fun onRobotFocusLost() {
    }

    override fun onRobotFocusRefused(reason: String?) {
    }

    fun takePic() {

        if(onPepper){
            val takePictureFuture: Future<TakePicture> = TakePictureBuilder.with(qiContext).buildAsync()

            val timestampedImageHandleFuture = takePictureFuture?.andThenCompose { takePicture ->
                Log.i(TAG, "take picture launched!")
                takePicture.async().run()
            }

            timestampedImageHandleFuture?.andThenConsume { timestampedImageHandle ->
                Log.i(TAG, "Picture taken")
                val encodedImageHandle: EncodedImageHandle = timestampedImageHandle.image
                val encodedImage: EncodedImage = encodedImageHandle.value

                Log.i(TAG, "PICTURE RECEIVED!")

                val buffer: ByteBuffer = encodedImage.data
                buffer.rewind()
                val pictureBufferSize: Int = buffer.remaining()
                val pictureArray: ByteArray = ByteArray(pictureBufferSize)
                buffer.get(pictureArray)

                Log.i(TAG, "PICTURE RECEIVED! ($pictureBufferSize Bytes)")

                val pictureBitmap = BitmapFactory.decodeByteArray(pictureArray, 0, pictureBufferSize)
                runOnUiThread { imageView.setImageBitmap(pictureBitmap) }
            }
        }
    }
}
