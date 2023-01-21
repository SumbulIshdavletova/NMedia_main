package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.api.GoogleApiModule
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.service.FCMService
import ru.netology.nmedia.service.FCMServiceModule
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject


@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app) {

    @Inject
    lateinit var auth: AppAuth
    private val viewModel: PostViewModel by viewModels()
    @Inject
    lateinit var fcmService: FCMServiceModule
    // lateinit var googleApiModule: GoogleApiModule


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        fcmService.provideFirebaseMessaging().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("some stuff happened: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            println(token)
        }

        checkGoogleApiAvailability()

//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                println("some stuff happened: ${task.exception}")
//                return@addOnCompleteListener
//            }
//
//            val token = task.result
//            println(token)
//        }
//
//        checkGoogleApiAvailability()
    }

    private fun checkGoogleApiAvailability() {
        if (::fcmService.isInitialized) {
            with(fcmService.provideGoogleApiAvailability()) {
                val code = isGooglePlayServicesAvailable(this@AppActivity)
                if (code == ConnectionResult.SUCCESS) {
                    return@with
                }
                if (isUserResolvableError(code)) {
                    getErrorDialog(this@AppActivity, code, 9000)?.show()
                    return
                }
                Toast.makeText(
                    this@AppActivity,
                    R.string.google_play_unavailable,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    }
}

//    private fun checkGoogleApiAvailability() {
//        with(GoogleApiAvailability.getInstance()) {
//            val code = isGooglePlayServicesAvailable(this@AppActivity)
//            if (code == ConnectionResult.SUCCESS) {
//                return@with
//            }
//            if (isUserResolvableError(code)) {
//                getErrorDialog(this@AppActivity, code, 9000)?.show()
//                return
//            }
//            Toast.makeText(
//                this@AppActivity,
//                R.string.google_play_unavailable,
//                Toast.LENGTH_LONG
//            )
//                .show()
//        }
//    }