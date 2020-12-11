package it.ministerodellasalute.immuni.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.games.*
import com.google.android.gms.tasks.OnCompleteListener
import it.ministerodellasalute.immuni.R
import org.koin.core.KoinComponent
import java.lang.IllegalStateException

/**
 * This is the actual implementation of Google Play provider. The [GooglePlayGamesHelper]
 * implementation is just a dummy that does not require any dependencies.
 */
class GooglePlayGamesHelper : KoinComponent {
    private val tag = GooglePlayGamesHelper::class.java.simpleName
    private val context: Context
    private var googleSignInClient: GoogleSignInClient
    private var gamesClient: GamesClient? = null
    private var leaderboardsClient: LeaderboardsClient? = null
    private var achievementsClient: AchievementsClient? = null
    private var eventsClient: EventsClient? = null
    private var playersClient: PlayersClient? = null
    private var signedIn = MutableLiveData<Boolean>(false)
    val isSignedIn: LiveData<Boolean>
        get() = signedIn
    
    var player: Player? = null
    var currentGoogleAccount: GoogleSignInAccount? = null
    
    val isAvailable: Boolean
        get() = (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS)

    constructor(context: Context): super() {
        this.context = context

        googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)

        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (lastAccount == null) {
            googleSignInClient.silentSignIn().addOnSuccessListener { setGoogleAccount(it) }
        } else {
            // make sure we are not immediately calling the listener in the constructor
            Handler().post {
                setGoogleAccount(lastAccount)
            }
        }
    }

    fun signIn(activity: Activity, requestCode: Int) {
        Log.d(tag, "Starting sign in to Google Play Games")
        val intent = googleSignInClient.signInIntent
        activity.startActivityForResult(intent, requestCode)
    }

    fun signIn(fragment: Fragment, requestCode: Int) {
        Log.d(tag, "Starting sign in to Google Play Games")
        val intent = googleSignInClient.signInIntent
        fragment.startActivityForResult(intent, requestCode)
    }

    private fun setGoogleAccount(account: GoogleSignInAccount?) {
        if (account === currentGoogleAccount) return

        if (account != null) {
            leaderboardsClient = Games.getLeaderboardsClient(context, account)
            achievementsClient = Games.getAchievementsClient(context, account)
            eventsClient = Games.getEventsClient(context, account)
            gamesClient = Games.getGamesClient(context, account)
            playersClient = Games.getPlayersClient(context, account).also {
                it.currentPlayer.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val player = task.result ?: return@addOnCompleteListener
                        this.player = player
                        currentGoogleAccount = account
                        signedIn.value = true
                    } else {
                        signOut()
                    }
                }
            }
        } else {
            leaderboardsClient = null
            achievementsClient = null
            gamesClient = null
            signedIn.value = false
            player = null
            currentGoogleAccount = null
        }
    }

    fun setWindowForPopups(window: Window) {
        gamesClient?.setViewForPopups(window.decorView)
    }

    fun signOut() {
        val onSignOutCompleteListener: OnCompleteListener<Void> = OnCompleteListener { setGoogleAccount(null) }
        googleSignInClient.signOut()?.addOnCompleteListener(onSignOutCompleteListener)
    }

    fun unlock(achievement: String) {
        achievementsClient?.unlock(achievement)
    }

    fun increment(achievement: String? = null, event: String? = null, increment: Int) {
        achievement?.let { achievementsClient?.increment(achievement, increment) }
        event?.let { eventsClient?.increment(event, increment) }
    }

    fun submitScore(leaderboard: String, score: Long) {
        leaderboardsClient?.submitScore(leaderboard, score)
    }

    fun startAchievementsIntent(activity: Activity, requestCode: Int) {
        achievementsClient?.achievementsIntent?.addOnSuccessListener { intent ->
            activity.startActivityForResult(intent, requestCode)
        }
    }

    fun startAchievementsIntent(fragment: Fragment, requestCode: Int) {
        achievementsClient?.achievementsIntent?.addOnSuccessListener { intent ->
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    fun startLeaderboardIntent(activity: Activity, leaderboard: String, requestCode: Int) {
        leaderboardsClient?.getLeaderboardIntent(leaderboard)?.addOnSuccessListener { intent ->
            activity.startActivityForResult(intent, requestCode)
        }
    }

    fun startLeaderboardIntent(fragment: Fragment, leaderboard: String, requestCode: Int) {
        leaderboardsClient?.getLeaderboardIntent(leaderboard)?.addOnSuccessListener { intent ->
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    /**
     * Handle activity result. Call this method from your Activity's
     * onActivityResult callback . If the activity result pertains to the sign-in
     * process, processes it appropriately.
     *
     * @return an optional error message, in case of error
     */
    fun onActivityResult(responseCode: Int, data: Intent?) {
        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data) ?: return
        if (result.status.isSuccess) {
            val account = result.signInAccount ?: throw IllegalStateException("account is null")
            setGoogleAccount(account)
        } else if (result.status.isCanceled) {
            // user aborted is no error
        } else {
            val message = result.status.statusMessage
            Log.e(tag, "Sign in result: ${result.status}")
        }
    }

    companion object {
        //Request codes
        const val RC_UNUSED = 5001
        const val RC_SIGN_IN = 9001
        const val RC_A_SIGN_IN = 74537
    }
}
