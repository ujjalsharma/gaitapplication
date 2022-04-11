package com.example.gaitapplication

import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class AnalyzeResultsActivity : AppCompatActivity() {
    var videoPath: Uri? = null
    var tv: TextView? = null
    var videoView: VideoView? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyze_results)
        tv = findViewById(R.id.textView)
        videoView = findViewById(R.id.videoView2)
        videoPath = Uri.parse(intent.getStringExtra("videoPath"))
        videoView!!.setVideoURI(videoPath)
        videoView!!.start()
        //DownloadTask(this.findViewById(R.id.textView)).execute("http://ec2-43-204-41-47.ap-south-1.compute.amazonaws.com:8000/files")
        UploadFile(this.findViewById(R.id.textView)).execute(videoPath)
        videoPath!!.path?.let { Log.i("something", it) }

    }

    class DownloadTask(textView: TextView): AsyncTask<String?, Void?, String?>() {
        val innerTv: TextView? = textView
        override fun doInBackground(vararg params: String?): String? {
            var result: String? = ""
            val url: URL
            var urlConnection: HttpURLConnection? = null
            return try {
                url = URL(params.get(0))
                urlConnection = url.openConnection() as HttpURLConnection
                val `in` = urlConnection.inputStream
                val reader = InputStreamReader(`in`)
                var data = reader.read()
                while (data != -1) {
                    val current = data.toChar()
                    result += current
                    data = reader.read()
                }
                result
            } catch (e: Exception) {
                e.toString()
            }
        }

        override fun onPostExecute(result: String?) {

            //val jsonObject = JSONObject(result)
            innerTv?.setText(result.toString())

        }
    }

    class UploadFile(textView: TextView) : AsyncTask<Any?, String?, String>() {
        var file_name = ""
        val innerTv: TextView? = textView
        override fun doInBackground(params: Array<Any?>): String {
            return try {
                val lineEnd = "\r\n"
                val twoHyphens = "--"
                val boundary = "*****"
                var bytesRead: Int
                var bytesAvailable: Int
                var bufferSize: Int
                val buffer: ByteArray
                val maxBufferSize = 1024 * 1024
                val url = URL("http://ec2-43-204-41-47.ap-south-1.compute.amazonaws.com:8000/files/new.mp4")
                val connection = url.openConnection() as HttpURLConnection

                // Allow Inputs &amp; Outputs.
                connection.doInput = true
                connection.doOutput = true
                connection.useCaches = false

                // Set HTTP method to POST.
                connection.requestMethod = "POST"
                connection.setRequestProperty("Connection", "Keep-Alive")
                connection.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data;boundary=$boundary"
                )
                val fileInputStream: FileInputStream
                val outputStream: DataOutputStream
                outputStream = DataOutputStream(connection.outputStream)
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes("Content-Disposition: form-data; name=\"reference\"$lineEnd")
                outputStream.writeBytes(lineEnd)
                outputStream.writeBytes("my_refrence_text")
                outputStream.writeBytes(lineEnd)
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                val uri: Uri = params[0] as Uri
                outputStream.writeBytes(
                    "Content-Disposition: form-data; name=\"uploadFile\";filename=\"" + uri.getLastPathSegment()
                        .toString() + "\"" + lineEnd
                )
                outputStream.writeBytes(lineEnd)
                uri.path?.let { Log.i("path", it) }

                fileInputStream = FileInputStream(File(java.lang.String.valueOf(uri.path)))
                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                buffer = ByteArray(bufferSize)

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize)
                    bytesAvailable = fileInputStream.available()
                    bufferSize = Math.min(bytesAvailable, maxBufferSize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                }
                outputStream.writeBytes(lineEnd)
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

                // Responses from the server (code and message)
                val serverResponseCode = connection.responseCode
                var result: String? = null
                if (serverResponseCode == 200) {
                    val s_buffer = StringBuilder()
                    val `is`: InputStream = BufferedInputStream(connection.inputStream)
                    val br = BufferedReader(InputStreamReader(`is`))
                    var inputLine: String?
                    while (br.readLine().also { inputLine = it } != null) {
                        s_buffer.append(inputLine)
                    }
                    result = s_buffer.toString()
                }
                fileInputStream.close()
                outputStream.flush()
                outputStream.close()
                if (result != null) {
                    Log.d("result_for upload", result)
                }
                "success"
            } catch (e: java.lang.Exception) {
                e.toString()
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            innerTv?.setText(result.toString())
        }
    }


}