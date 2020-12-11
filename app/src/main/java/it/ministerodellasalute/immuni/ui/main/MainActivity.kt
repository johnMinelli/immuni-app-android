/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.ui.main

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.ImmuniActivity
import it.ministerodellasalute.immuni.ui.main.navigation.setupWithNavController
import it.ministerodellasalute.immuni.util.GooglePlayGamesHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel


class MainActivity : ImmuniActivity() {

    private val gamesHelper: GooglePlayGamesHelper by inject()
    private var currentNavController: LiveData<NavController>? = null
    private lateinit var viewModel: MainViewModel

    // Client used to sign in with Go
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        viewModel = getViewModel()
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)

        val navGraphIds = listOf(R.navigation.home, R.navigation.home, R.navigation.settings)

        val clickListenerEx = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            if(item.itemId == R.id.achievements){
                gamesHelper.startAchievementsIntent(this, GooglePlayGamesHelper.RC_UNUSED)
            }
            true
        }
        
        // Setup the bottom navigation view with a list of navigation graphs
        bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            clickListenerEx = clickListenerEx,
            containerId = R.id.nav_host_container,
            intent = intent
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

//    override fun onResume() {
//        gamesHelper.unlock(R.string.achievement_one_step_at_a_time.toString()) 
//        gamesHelper.increment(achievement = getString(R.string.achievement_what_a_month), increment = 1)
//        gamesHelper.increment(event = getString(R.string.event_app_opened), increment = 1)
//        super.onResume()
//    }
}
