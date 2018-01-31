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

/**
 * Stores [ViewModel]'S
 */
class ViewModelStore {

    private val map = HashMap<Any, ViewModel>()

    fun put(key: Any, viewModel: ViewModel) {
        map[key]?.onCleared()
        map[key] = viewModel
    }

    operator fun get(key: Any): ViewModel? {
        return map[key]
    }

    fun clear() {
        for (vm in map.values) {
            vm.onCleared()
        }
        map.clear()
    }
}