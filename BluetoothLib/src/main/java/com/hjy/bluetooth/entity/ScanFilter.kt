package com.hjy.bluetooth.entity

/**
 * author : HJY
 * date   : 2021/11/15
 * desc   :
 */
class ScanFilter {
    var isFuzzyMatching = false
        private set
    lateinit var names: Array<String>
        private set

    private constructor() {}
    constructor(fuzzyMatching: Boolean, vararg names: String) {
        isFuzzyMatching = fuzzyMatching
        this.names = names as Array<String>
    }
}