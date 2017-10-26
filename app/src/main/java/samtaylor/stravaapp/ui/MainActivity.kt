package samtaylor.stravaapp.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import samtaylor.stravaapp.R
import samtaylor.stravaapp.data.Persistence

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = getString(R.string.title_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        MenuInflater(this).inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {

            R.id.menuSignOut -> {

                AlertDialog.Builder(this).setMessage(R.string.sign_out_dialog).setNegativeButton(R.string.cancel) { dialog, _ ->

                    dialog.dismiss()
                }.setPositiveButton(R.string.sign_out) { dialog, _ ->

                    Persistence(this).remove(Persistence.ACCESS_TOKEN)

                    startActivity(Intent(this, SignInActivity::class.java))
                    dialog.dismiss()
                    finish()
                }.create().show()

                true
            }

            R.id.menuChangeTarget -> {

                startActivity(Intent(this, SetTargetActivity::class.java))

                true
            }

            else -> {

                return super.onOptionsItemSelected(item)
            }
        }
    }
}