package com.ewamo.skyapp.data

import android.net.Uri.Builder
import androidx.fragment.app.Fragment
import com.ewamo.skyapp.R
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageRequester(listeningActivity: Fragment) {

    interface ImageRequesterResponse {
        fun receivedNewPhoto(newPhoto: Photo)
    }

    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val responseListener: ImageRequesterResponse =
        listeningActivity as ImageRequesterResponse
    private val context: Fragment = listeningActivity
    private val client: OkHttpClient = OkHttpClient()

    var isLoadingData: Boolean = false
        private set

    fun getPhoto() {

        val date = dateFormat.format(calendar.time)

        val urlRequest = Builder().scheme(URL_SCHEME)
            .authority(URL_AUTHORITY)
            .appendPath(URL_PATH_1)
            .appendPath(URL_PATH_2)
            .appendQueryParameter(URL_QUERY_PARAM_DATE_KEY, date)
            .appendQueryParameter(URL_QUERY_PARAM_API_KEY, context.getString(R.string.api_key))
            .build().toString()

        val request = Request.Builder().url(urlRequest).build()
        isLoadingData = true

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isLoadingData = false
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {

                try {
                    val photoJSON = JSONObject(response.body!!.string())

                    calendar.add(Calendar.DAY_OF_YEAR, -1)

                    if (photoJSON.getString(MEDIA_TYPE_KEY) != MEDIA_TYPE_VIDEO_VALUE) {
                        val receivedPhoto = Photo(photoJSON)
                        responseListener.receivedNewPhoto(receivedPhoto)
                        isLoadingData = false
                    } else {
                        getPhoto()
                    }
                } catch (e: JSONException) {
                    isLoadingData = false
                    e.printStackTrace()
                }

            }
        })
    }

    companion object {
        private const val MEDIA_TYPE_KEY = "media_type"
        private const val MEDIA_TYPE_VIDEO_VALUE = "video"
        private const val URL_SCHEME = "https"
        private const val URL_AUTHORITY = "api.nasa.gov"
        private const val URL_PATH_1 = "planetary"
        private const val URL_PATH_2 = "apod"
        private const val URL_QUERY_PARAM_DATE_KEY = "date"
        private const val URL_QUERY_PARAM_API_KEY = "api_key"
    }
}
