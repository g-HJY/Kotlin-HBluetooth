package com.hjy.bluetooth.async

import android.os.AsyncTask
import java.lang.ref.WeakReference

/**
 * Created by _H_JY on 2018/2/12.
 */
abstract class WeakAsyncTask<Params, Progress, Result, WeakTarget>(target: WeakTarget?) : AsyncTask<Params, Progress, Result?>() {
    private var mTarget: WeakReference<WeakTarget?> = WeakReference(target)
    override fun onPreExecute() {
        val target = mTarget.get()
        if (target != null) {
            this.onPreExecute(target)
        }
    }

    override fun doInBackground(vararg params: Params): Result? {
        val target = mTarget.get()
        return if (target != null) {
            this.doInBackground(target, *params)
        } else {
            null
        }
    }

    override fun onPostExecute(result: Result?) {
        val target = mTarget.get()
        if (target != null) {
            this.onPostExecute(target, result)
        }
    }

    protected fun onPreExecute(target: WeakTarget) {
        // Nodefaultaction
    }

    protected abstract fun doInBackground(target: WeakTarget,
                                          vararg params: Params): Result

    protected open fun onPostExecute(target: WeakTarget, result: Result?) {
        // Nodefaultaction
    }

}