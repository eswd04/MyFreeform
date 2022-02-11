package com.demo.myfreeform

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.demo.myfreeform.databinding.SecondViewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

class SecondActivity : AppCompatActivity() {


    lateinit var binding:SecondViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= SecondViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val list = ArrayList<AppInfo>()
        val i = Intent(Intent.ACTION_MAIN)
        i.addCategory(Intent.CATEGORY_LAUNCHER)

        val adapter=AppAdapter(list)
        binding.recyclerView.adapter = adapter
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                packageManager.queryIntentActivities(i, 0).forEach {
                    val icon = it.activityInfo.loadIcon(packageManager)
                    val name = it.activityInfo.loadLabel(packageManager)
                    list.add(AppInfo(name.toString(), icon, it));
                }.also {
                    withContext(Dispatchers.Main){
                        adapter.notifyDataSetChanged()
                    }

                }
            }

        }
        adapter.clickListener=object : AppAdapter.OnClickListener{
            override fun onItemClick(appInfo: AppInfo, position: Int) {
                EventBus.getDefault().post(appInfo)
            }
        }

    }

}