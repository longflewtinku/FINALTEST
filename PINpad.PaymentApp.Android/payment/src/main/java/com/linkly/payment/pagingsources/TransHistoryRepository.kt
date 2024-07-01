package com.linkly.payment.pagingsources

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.linkly.libengine.engine.transactions.TransRecDao

class TransHistoryRepository(val transRecDao: TransRecDao) {

    // using a flow :D
    fun getTransRec() = Pager(
        config = PagingConfig(pageSize = 20, prefetchDistance = 100, enablePlaceholders = false),
        pagingSourceFactory = { transRecDao.getTransRecForPager() }
    ).flow
}