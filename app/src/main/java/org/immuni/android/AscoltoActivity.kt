package org.immuni.android

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.bendingspoons.secretmenu.SecretMenu
import org.immuni.android.managers.PermissionsManager
import org.koin.android.ext.android.inject

@SuppressLint("Registered")
open class AscoltoActivity : AppCompatActivity() {

    val permissionsManager: PermissionsManager by inject()
    val secretMenu: SecretMenu by inject()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            secretMenu.onTouchEvent(it)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun dispatchDialogTouchEvent(ev: MotionEvent?) {
        ev?.let {
            secretMenu.onTouchEvent(it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionsManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }
}