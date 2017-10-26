package samtaylor.stravaapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_auth.*
import org.json.JSONObject
import samtaylor.stravaapp.R
import samtaylor.stravaapp.data.Persistence
import samtaylor.stravaapp.model.Athlete
import java.util.ArrayList

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        webView.webViewClient = object: WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

                return if (request != null) {

                    if (request.url.host == "127.0.0.1") {

                        val code = request.url.getQueryParameter("code")

                        val values = ArrayList<Pair<String, String>>()
                        values.add("client_id" to "21008")
                        values.add("client_secret" to "add22af4395113c65875779d96c5220d6b826d8e")
                        values.add("code" to code)

                        "https://www.strava.com/oauth/token".httpPost(values).responseString { _, _, result ->

                            when (result) {

                                is Result.Success -> {

                                    val json = JSONObject(result.get())

                                    val accessToken = json.getString("access_token")

                                    Persistence(this@AuthActivity).setString(Persistence.ACCESS_TOKEN, accessToken)
                                    startActivity(Intent(this@AuthActivity, MainActivity::class.java))

                                    finish()
                                }

                                else -> {

                                    finish()
                                }
                            }
                        }
                    } else {

                        webView.loadUrl(request.url.toString())
                    }
                    true
                } else {

                    false
                }

            }
        }

        webView.loadUrl("https://www.strava.com/oauth/authorize?client_id=21008&response_type=code&redirect_uri=http://127.0.0.1&approval_prompt=force")
    }
}