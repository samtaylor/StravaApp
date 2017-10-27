package samtaylor.stravaapp.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_auth.*
import samtaylor.stravaapp.R

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        title = getString(R.string.title_authenticate)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        webView.webViewClient = object: WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {

                progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

                return if (request != null) {

                    if (request.url.host == "127.0.0.1") {

                        val code = request.url.getQueryParameter("code")

                        val intent = Intent()
                        intent.putExtra(SignInActivity.AUTH_CODE, code)

                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {

                        webView.loadUrl(request.url.toString())
                    }
                    true
                } else {

                    false
                }

            }
        }

        webView.webChromeClient = object: WebChromeClient() {

            override fun onProgressChanged(view: WebView, newProgress: Int) {

            }
        }

        webView.loadUrl("https://www.strava.com/oauth/authorize?client_id=21008&response_type=code&redirect_uri=http://127.0.0.1&scope=view_private")
    }

    override fun onDestroy() {

        webView.webChromeClient = null
        webView.webViewClient = null

        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {

            android.R.id.home -> {

                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}