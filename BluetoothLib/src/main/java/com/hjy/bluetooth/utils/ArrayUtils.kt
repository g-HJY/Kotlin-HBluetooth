package com.hjy.bluetooth.utils

import java.util.*

/**
 * author : HJY
 * date   : 2021/11/12
 * desc   :
 */
object ArrayUtils {
    /**
     * splitAry方法<br></br>
     * @param ary 要分割的数组
     * @param subSize 分割的块大小
     * @return
     */
    fun splitBytes(ary: ByteArray, subSize: Int): Array<Any?> {
        val count = if (ary.size % subSize == 0) ary.size / subSize else ary.size / subSize + 1
        val subAryList: MutableList<List<Byte>> = ArrayList()
        for (i in 0 until count) {
            var index = i * subSize
            val list: MutableList<Byte> = ArrayList()
            var j = 0
            while (j < subSize && index < ary.size) {
                list.add(ary[index++])
                j++
            }
            subAryList.add(list)
        }
        val subAry = arrayOfNulls<Any>(subAryList.size)
        for (i in subAryList.indices) {
            val subList = subAryList[i]
            val subAryItem = ByteArray(subList.size)
            for (j in subList.indices) {
                subAryItem[j] = subList[j]
            }
            subAry[i] = subAryItem
        }
        return subAry
    }
}