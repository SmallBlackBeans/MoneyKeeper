/*
 * Copyright 2018 Bakumon. https://github.com/Bakumon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.bakumon.moneykeeper.ui.assets.transfer

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.base.Resource
import me.bakumon.moneykeeper.database.entity.Assets
import me.bakumon.moneykeeper.database.entity.AssetsTransferRecord
import me.bakumon.moneykeeper.datasource.AppDataSource
import me.bakumon.moneykeeper.ui.common.BaseViewModel

/**
 * TransferViewModel
 *
 * @author Bakumon https://bakumon.me
 */
class TransferViewModel(dataSource: AppDataSource) : BaseViewModel(dataSource) {

    fun getAssets(): LiveData<List<Assets>> {
        return mDataSource.getAssets()
    }

    fun addTransferRecord(outAssets: Assets, inAssets: Assets, transferRecord: AssetsTransferRecord): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        mDisposable.add(mDataSource.insertTransferRecord(transferRecord)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateOutAssets(liveData, outAssets, inAssets, transferRecord)
                }
                ) { throwable ->
                    liveData.value = Resource.create(throwable)
                })
        return liveData
    }

    private fun updateOutAssets(liveData: MutableLiveData<Resource<Boolean>>, outAssets: Assets, inAssets: Assets, transferRecord: AssetsTransferRecord) {
        outAssets.money = outAssets.money.subtract(transferRecord.money)
        mDisposable.add(mDataSource.updateAssets(outAssets)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateInAssets(liveData, inAssets, transferRecord)
                }
                ) { throwable ->
                    liveData.value = Resource.create(throwable)
                })
    }

    private fun updateInAssets(liveData: MutableLiveData<Resource<Boolean>>, inAssets: Assets, transferRecord: AssetsTransferRecord) {
        inAssets.money = inAssets.money.add(transferRecord.money)
        mDisposable.add(mDataSource.updateAssets(inAssets)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveData.value = Resource.create(true)
                }
                ) { throwable ->
                    liveData.value = Resource.create(throwable)
                })
    }
}
