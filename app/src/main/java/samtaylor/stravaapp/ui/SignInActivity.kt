package samtaylor.stravaapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.json.JSONObject
import samtaylor.stravaapp.R
import samtaylor.stravaapp.data.Persistence

class SignInActivity : AppCompatActivity() {

    private val AUTH_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
    }

    override fun onResume() {

        super.onResume()
        when {
            Persistence(this).getString(Persistence.ACCESS_TOKEN) == null -> signInButton.setOnClickListener {

                startActivityForResult(Intent(this, AuthActivity::class.java), AUTH_REQUEST)
            }

            Persistence(this).getInt(Persistence.TARGET) == 0 -> {

                startActivity(Intent(this, SetTargetActivity::class.java))
            }

            else -> {

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == AUTH_REQUEST && resultCode == Activity.RESULT_OK && data != null) {

            signInButton.visibility = View.GONE
            loadingSpinner.visibility = View.VISIBLE

            val code = data.getStringExtra(AUTH_CODE)

            val values = ArrayList<Pair<String, String>>()
            values.add("client_id" to "21008")
            values.add("client_secret" to "add22af4395113c65875779d96c5220d6b826d8e")
            values.add("code" to code)

            "https://www.strava.com/oauth/token".httpPost(values).responseString { _, _, result ->

                when (result) {

                    is Result.Success -> {

                        val json = JSONObject(result.get())

                        val accessToken = json.getString("access_token")
                        Persistence(this).putString(Persistence.ACCESS_TOKEN, accessToken)

                        startActivity(Intent(this, SetTargetActivity::class.java))
                    }

                    else -> {

                        signInButton.visibility = View.VISIBLE
                        loadingSpinner.visibility = View.GONE
                    }
                }
            }
        }
    }

    companion object {

        val AUTH_CODE = "auth_code"
    }


}