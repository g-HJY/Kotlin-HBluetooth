package com.hjy.test

import android.util.SparseArray
import android.view.View

/**
 * Created by Administrator on 2018/10/24.
 */
object ViewHolder {

    fun <T : View> getView(convertView: View?, childViewId: Int): T? {
        var viewHolder = convertView?.tag as? SparseArray<View?>
        if (viewHolder == null) {
            viewHolder = SparseArray()
            convertView?.tag = viewHolder
        }
        var childView: View? = viewHolder.get(childViewId)
        if (childView == null) {
            childView = convertView?.findViewById(childViewId)
            viewHolder.put(childViewId, childView)
        }
        return childView as T
    }
}