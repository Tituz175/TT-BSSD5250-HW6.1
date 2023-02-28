package com.example.tt_bssd5250_61

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val API_KEY = "6682969b2b2104c72d74e04f54de032b"
    private val service = "https://api.themoviedb.org/3/search/movie"
    private val imageBasePath = "https://image.tmdb.org/t/p/w500";

    private lateinit var posterImage: ImageView
    private var yearGuess: String = "2014"
    private var movie: String = ""
    private var responseYear: String = ""
    private  var searchResponse:String = ""
    private var initialmovie: String = ""
    private lateinit var inputYear: TextView
    private lateinit var inputTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inputTitle = EditText(this).apply {
            hint = "Enter Movie Title"
        }
        inputYear = EditText(this).apply {
            hint = "Enter Guess for Year"
        }

        val submitButton = Button(this).apply {
            text = "Submit"
            setOnClickListener {
                movie = inputTitle.text.toString()
                yearGuess = inputYear.text.toString()
                if (yearGuess == "") {
                    this@MainActivity.runOnUiThread(java.lang.Runnable {
                        Toast.makeText(applicationContext, "Enter all fields", Toast.LENGTH_SHORT)
                            .show()
                    })
                } else {
                    showResult(movie, yearGuess)
                }

            }
        }

        posterImage = ImageView(this).apply {

            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
        }


        val linearLayout = LinearLayoutCompat(this).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayoutCompat.VERTICAL
            addView(inputTitle)
            addView(inputYear)
            addView(submitButton)
            addView(posterImage)
        }
        setContentView(linearLayout)

    }

    private fun showResult(title: String, yearGuess: String) {
        thread(true) {
            var query = title
            val requestURL = "$service?api_key=$API_KEY&query=$query"
            if (initialmovie != movie) {
                Log.d("Submit", "Title Changed")
                searchResponse = getRequest(requestURL).toString()
                var response = ""
                response = JSONObject(searchResponse).getString("total_results")
                if (response.toInt() < 1) {
                    this@MainActivity.runOnUiThread(java.lang.Runnable {
                        Toast.makeText(applicationContext, "Try Again", Toast.LENGTH_SHORT).show()
                    })
                } else {
                    parseJSON(getRequest(requestURL).toString(), yearGuess)
                    initialmovie = movie
                }

            } else {
                parseJSON(searchResponse, yearGuess)
                Log.d("Submit", "Title equals prev title")
            }
        }
    }


    private fun parseJSON(jsonString: String, guess:String) {
        val jsonData = JSONObject(jsonString)
        val jsonArray = jsonData.getJSONArray("results")
        val film = jsonArray.getJSONObject(0)
        val posterPath = film.getString("poster_path")
        val fullPath = imageBasePath + posterPath
        var year = film.getString("release_date")
        responseYear = year.substring(0,4)
        var answer = "Incorrect"
        if(responseYear == guess){
            answer = "Correct"
        }
        this@MainActivity.runOnUiThread(java.lang.Runnable {
            Toast.makeText(applicationContext, answer, Toast.LENGTH_SHORT).show()
        })
        thread (true){
            val bmp = loadBitmapData(fullPath)
            this@MainActivity.runOnUiThread(java.lang.Runnable{
                posterImage.setImageBitmap(bmp)
            })

        }
    }

    private fun loadBitmapData(path: String): Bitmap? {
        val inputStream: InputStream
        var result: Bitmap? = null

        try {
            val url = URL(path)
            val conn: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            conn.connect()
            inputStream = conn.inputStream

            result = BitmapFactory.decodeStream(inputStream)
        } catch (err: Error) {
            print("Error when executing fet request: " + err.localizedMessage)
        }
        return result
    }

    private fun getRequest(sUrl: String): String? {
        val inputStream: InputStream
        var result: String? = null

        try {
            val url = URL(sUrl)
            val conn: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            conn.connect()
            inputStream = conn.inputStream

            result = if (inputStream != null)
                inputStream.bufferedReader().use(BufferedReader::readText)
            else
                "error: inputStream is null"
        } catch (err: Error) {
            print("Error when executing get resquest: " + err.localizedMessage)
        }
        return result
    }
}