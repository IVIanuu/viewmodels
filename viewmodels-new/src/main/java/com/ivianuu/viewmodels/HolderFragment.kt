/*
 * Copyright 2018 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.viewmodels

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager

/**
 * Holds a [ViewModelStore] for a [Activity] or [Fragment] until it gets destroyed
 */
class ViewModelHolderFragment : Fragment() {

    private val viewModelStore = ViewModelStore()

    init {
        retainInstance = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        holderFragmentCreated(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelStore.clear()
    }

    companion object {
        private const val TAG_FRAGMENT = "ViewModelHolderFragment"

        private val notCommittedActivityHolders = HashMap<Activity, ViewModelHolderFragment>()
        private val notCommittedFragmentHolders = HashMap<Fragment, ViewModelHolderFragment>()

        private var activityCallbacksRegistered = false

        private val activityCallbacks = object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity?) {}
            override fun onActivityResumed(activity: Activity?) {}
            override fun onActivityPaused(activity: Activity?) {}
            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
            override fun onActivityStopped(activity: Activity?) {}
            override fun onActivityDestroyed(activity: Activity) {
                notCommittedActivityHolders.remove(activity)
            }
        }

        private val mParentDestroyedCallback = object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, parentFragment: Fragment) {
                notCommittedFragmentHolders.remove(parentFragment)
            }
        }

        private fun holderFragmentCreated(holderFragment: Fragment) {
            val parentFragment = holderFragment.parentFragment
            if (parentFragment != null) {
                notCommittedFragmentHolders.remove(parentFragment)
                parentFragment.fragmentManager?.unregisterFragmentLifecycleCallbacks(
                    mParentDestroyedCallback
                )
            } else {
                holderFragment.activity?.let { notCommittedActivityHolders.remove(it) }
            }
        }

        internal fun holderFragmentFor(activity: FragmentActivity): ViewModelHolderFragment {
            var holder = notCommittedActivityHolders[activity]
            if (holder != null) {
                return holder
            }

            holder = findHolderFragment(activity.supportFragmentManager)
            if (holder != null) {
                return holder
            }

            if (!activityCallbacksRegistered) {
                activityCallbacksRegistered = true
                activity.application.registerActivityLifecycleCallbacks(activityCallbacks)
            }

            holder = createHolderFragment(activity.supportFragmentManager)
            notCommittedActivityHolders[activity] = holder

            return holder
        }

        internal fun holderFragmentFor(fragment: Fragment): ViewModelHolderFragment {
            var holder = notCommittedFragmentHolders[fragment]

            if (holder != null) {
                return holder
            }

            holder = findHolderFragment(fragment.childFragmentManager)

            if (holder != null) {
                return holder
            }

            fragment.fragmentManager!!.registerFragmentLifecycleCallbacks(
                mParentDestroyedCallback, false)

            holder = createHolderFragment(fragment.fragmentManager!!)
            notCommittedFragmentHolders[fragment] = holder

            return holder
        }

        private fun createHolderFragment(fragmentManager: FragmentManager): ViewModelHolderFragment {
            val holder = ViewModelHolderFragment()
            fragmentManager.beginTransaction().add(holder, TAG_FRAGMENT).commitAllowingStateLoss()
            return holder
        }

        private fun findHolderFragment(manager: FragmentManager): ViewModelHolderFragment? {
            return manager.findFragmentByTag(TAG_FRAGMENT) as ViewModelHolderFragment?
        }
    }
}