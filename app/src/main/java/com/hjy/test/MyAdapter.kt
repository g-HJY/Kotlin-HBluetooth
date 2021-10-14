package com.hjy.test

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hjy.bluetooth.entity.BluetoothDevice

/**
 * Created by Administrator on 2018/10/24.
 */
class MyAdapter(private val mContext: Context, private val list: List<BluetoothDevice>?) : BaseAdapter() {

    override fun getCount(): Int {
        return list!!.size
    }

    override fun getItem(position: Int): Any {
        return list!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View? {
        var convertView = view
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null)
        }
        val tvName = ViewHolder.getView<TextView>(convertView, R.id.tv_name)
        val tvAddress = ViewHolder.getView<TextView>(convertView, R.id.tv_address)
        val tvRecord = ViewHolder.getView<TextView>(convertView, R.id.tv_record)
        val bluetoothDevice = list!![position]

        with(bluetoothDevice){
            tvName!!.text = name
            tvAddress!!.text = address
            val scanRecord = scanRecord
            if (scanRecord != null && scanRecord.isNotEmpty()) {
                tvRecord!!.visibility = View.VISIBLE
                tvRecord.text = Tools.bytesToHexString(scanRecord)
            } else {
                tvRecord!!.visibility = View.GONE
            }
        }

        return convertView
    }

}