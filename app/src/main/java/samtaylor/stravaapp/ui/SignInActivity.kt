package samtaylor.stravaapp.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_sign_in.*
import samtaylor.stravaapp.R
import samtaylor.stravaapp.data.Persistence

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        if (Persistence(this).getString(Persistence.ACCESS_TOKEN) == null) {

            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_sign_in)

            signInButton.setOnClickListener {

                startActivity(Intent(this, AuthActivity::class.java))
            }
        }
        else {

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}