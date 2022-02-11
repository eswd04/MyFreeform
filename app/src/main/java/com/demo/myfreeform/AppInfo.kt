package com.demo.myfreeform

import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class AppInfo (
    val textName:String,
    val icon:Drawable,
    val applicationInfo: ResolveInfo?
)