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

import android.content.Context
import androidx.lifecycle.*
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.lifecycle.AppLifecycleObserver
import it.ministerodellasalute.immuni.extensions.notifications.PushNotificationManager
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.ui.home.*
import it.ministerodellasalute.immuni.util.GooglePlayGamesHelper
import kotlinx.coroutines.flow.*

class MainViewModel(
        private val context: Context,
        private val pushNotificationManager: PushNotificationManager,
        private val exposureManager: ExposureManager,
        val gamesHelper: GooglePlayGamesHelper,
        appLifecycleObserver: AppLifecycleObserver
) : ViewModel() {

    val homeListModel = MutableLiveData<List<HomeItemType>>(listOf())
    val exposureStatus = exposureManager.exposureStatus.asLiveData()

    init {
        combine(
            exposureManager.isBroadcastingActive,
            exposureManager.exposureStatus,
            gamesHelper.isSignedIn.asFlow(),
            appLifecycleObserver.isActive.filter { it }
        ) { broadcastingIsActive, status, isSigned, isActive ->
            Triple(broadcastingIsActive, isSigned, isActive)
        }.onEach { (broadcastingIsActive, _, _) ->
            val protectionActive = when (broadcastingIsActive) {
                null -> null
                else -> broadcastingIsActive && pushNotificationManager.areNotificationsEnabled()
            }
            homeListModel.postValue(homeListModel(protectionActive))
        }.launchIn(viewModelScope)
    }

    private fun homeListModel(protectionActive: Boolean?): List<HomeItemType> {
        val items = mutableListOf<HomeItemType>()
        protectionActive?.let {
            items.add(ProtectionCard(it, exposureManager.exposureStatus.value))
        }
        if(gamesHelper.isSignedIn.value == true)
            items.add(LevelIndicatorItem)
        items.add(SectionHeader(context.getString(R.string.home_view_info_header_title)))
        items.add(HowItWorksCard)
        items.add(SelfCareCard)
        items.add(CountriesOfInterestCard)
        items.add(DisableExposureApi(protectionActive ?: false))
        return items
    }
}
