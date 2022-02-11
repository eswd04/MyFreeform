package com.demo.myfreeform

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    var binder: FreeFormService.MyBinder?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(i, 6);
                Toast.makeText(this, "应用需要使用悬浮窗权限,请允许本应用使用该权限", Toast.LENGTH_SHORT).show();
            }
        }
        val i = Intent(this, FreeFormService::class.java)
        startService(i)
        bindService(i, serviceConnection, BIND_AUTO_CREATE)

    }


    override fun onDestroy() {
        super.onDestroy()

        if (binder != null) {
            unbindService(serviceConnection)
        }
    }


    val serviceConnection=object :ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder= service as FreeFormService.MyBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    fun startFreeformService(view: View) {
        binder?.initWindowService()
        Log.d("TAG", "startFreeformService: ${binder==null}")
//        finish()
    }
}