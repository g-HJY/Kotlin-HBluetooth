package com.hjy.bluetooth.utils

import android.text.TextUtils
import java.util.*

/**
 * 公用内存锁仓库.实现非阻塞型的同步锁，区别于synchronized
 */
object LockStore {
    // volatile保证所有线程看到的锁相同
    @Volatile
    private var locks: MutableMap<String, Date> = HashMap()

    /**
     * 根据锁名获取锁
     *
     * @param lockName 锁名
     * @return 是否锁定成功
     */
    @Synchronized
    fun getLock(lockName: String): Boolean {
        var locked = false
        if (TextUtils.isEmpty(lockName)) {
            throw RuntimeException("Lock name can't be empty")
        }
        val lockDate = locks[lockName]
        if (lockDate == null) {
            locks[lockName] = Date()
            locked = true
        }
        return locked
    }

    /**
     * 根据锁名释放锁
     *
     * @param lockName 锁名
     */
    @Synchronized
    fun releaseLock(lockName: String?) {
        if (TextUtils.isEmpty(lockName)) {
            throw RuntimeException("Lock name can't be empty")
        }
        val lockDate = locks[lockName]
        if (lockDate != null) {
            locks.remove(lockName)
        }
    }

    /**
     * 获取上次成功锁定的时间
     *
     * @param lockName 锁名
     * @return 如果还没有锁定返回NULL
     */
    @Synchronized
    fun getLockDate(lockName: String?): Date? {
        if (TextUtils.isEmpty(lockName)) {
            throw RuntimeException("Lock name can't be empty")
        }
        return locks[lockName]
    }
}