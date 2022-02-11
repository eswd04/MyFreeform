package com.demo.myfreeform

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.input.InputManager
import android.util.Log
import android.view.*
import java.lang.RuntimeException
import java.util.function.Consumer
import android.content.ComponentName
import android.hardware.display.*
import android.hardware.display.DisplayManager.*
import android.os.*
import android.util.AttributeSet
import java.util.*


class ActivityContainer : ViewGroup {


    constructor(context: Context) : super(context){
        initView(context)
    }
    constructor(context: Context,attributeSet: AttributeSet) : super(context){
        initView(context)

    }

    private var mVirtualDisplay: VirtualDisplay? = null
    private var textureView: TextureView? = null
    private val DISPLAY_NAME = "MyVirtualDisplay"
    private var mActivityViewCallback: StateCallback? = null

    var scalePercent=1F;
    var displayWidth=-1;

    fun initView(context: Context) {
        textureView = TextureView(context)
        textureView!!.surfaceTextureListener = TextureLinstener()


        addView(textureView)
    }


    inner class TextureLinstener:TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            if (mVirtualDisplay == null) {
                val dispWidth = resources.displayMetrics.widthPixels.also {
                    displayWidth=it;
                }
                val dispHeight = resources.displayMetrics.heightPixels

                scalePercent=(width / dispWidth).toFloat()
                if (scalePercent<0.4F) scalePercent=0.4F;
                val mwidth = (width / scalePercent).toInt()
                val mheight = (height / scalePercent).toInt()

                val surface = Surface(surfaceTexture)

                val displayManager = context.getSystemService(DisplayManager::class.java)
                mVirtualDisplay = displayManager.createVirtualDisplay(
                    DISPLAY_NAME + "@" + System.identityHashCode(this), mwidth, mheight,
                    400, surface,
                    VIRTUAL_DISPLAY_FLAG_PUBLIC or VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or 256);

                surfaceTexture.setDefaultBufferSize(mwidth, mheight)
                if (mVirtualDisplay == null) {
                    Log.e("TAG", "Failed to initialize ActivityView")
                    return
                }
//                mVirtualDisplay.setDisplayState(true);
                if (mVirtualDisplay != null && mActivityViewCallback != null) {
                    mActivityViewCallback!!.onActivityViewReady(this@ActivityContainer)
                }
//                try {
//                    val displayId = mVirtualDisplay!!.display.displayId
//                    val wm: IWindowManager = WindowManagerGlobal.getWindowManagerService()
//                    wm.dontOverrideDisplayInfo(displayId)
//                } catch (e: RemoteException) {
//                }
            }
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            scalePercent = width.toFloat() / displayWidth
            if (scalePercent < 0.4F) scalePercent = 0.4f

            val wS=(width / scalePercent).toInt()
            val hS=(height / scalePercent).toInt()
            mVirtualDisplay!!.resize(wS, hS, 400)
            surface.setDefaultBufferSize(wS, hS)

        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }



    open fun startActivity(intent: Intent) {
        val options: ActivityOptions = prepareActivityOptions()
        context.startActivity(intent,options.toBundle())
    }

    fun startActivity(pendingIntent: PendingIntent, options: ActivityOptions) {
        options.launchDisplayId = getDisplayId()
        try {
            pendingIntent.send(
                null /* context */, 0 /* code */, null /* intent */,
                null /* onFinished */, null /* handler */, null /* requiredPermission */,
                options.toBundle()
            )
        } catch (e: PendingIntent.CanceledException) {
            throw RuntimeException(e)
        }
    }

    fun startActivity(pendingIntent: PendingIntent) {
        val options = prepareActivityOptions()
        try {
            pendingIntent.send(
                null /* context */, 0 /* code */, null /* intent */,
                null /* onFinished */, null /* handler */, null /* requiredPermission */,
                options.toBundle()
            )
        } catch (e: PendingIntent.CanceledException) {
            throw RuntimeException(e)
        }
    }

    private fun prepareActivityOptions(): ActivityOptions {
        var activity: Activity
        checkNotNull(mVirtualDisplay) { "Trying to start activity before ActivityView is ready." }
        val options = ActivityOptions.makeBasic()
        options.launchDisplayId = getDisplayId()
        return options

    }
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        var inputEvent = MotionEvent.obtain(
            System.currentTimeMillis(),
            ev!!.getEventTime(),  //                ev.getAction(),(ev.getX()),ev.getY(),
            ev.getAction(),
            ev.getX()/scalePercent ,
            ev.getY()/scalePercent ,
            ev.getPressure(),
            ev.getSize(),
            ev.getMetaState(),
            ev.getXPrecision(),
            ev.getYPrecision(),
            ev.getDeviceId(),
            ev.getEdgeFlags(),
            ev.getSource(),
            getDisplayId()
        )
        InputManager.getInstance().injectInputEvent(inputEvent,0)
        return true
    }




    fun backPress() {
        val displayId = getDisplayId()
        val b = ServiceManager.getService("activity")
        val activityManager1 = IActivityManager.Stub.asInterface(b)
        activityManager1.getTasks(2).forEach { runningTaskInfo ->
            if (runningTaskInfo.displayId === getDisplayId()) {
                if (!context.getPackageName()
                        .equals(runningTaskInfo.baseActivity.packageName)
                ) {
                    val im: InputManager =
                        InputManager.getInstance()
                    im.injectInputEvent(
                        createKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, displayId),
                        InputManager.INJECT_INPUT_EVENT_MODE_ASYNC
                    )
                    im.injectInputEvent(
                        createKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, displayId),
                        InputManager.INJECT_INPUT_EVENT_MODE_ASYNC
                    )
                }
            }
        }
    }


    private fun createKeyEvent(action: Int, code: Int, displayId: Int): KeyEvent? {
        val `when` = SystemClock.uptimeMillis()
        val ev = KeyEvent(
            `when`, `when`, action, code, 0 /* repeat */,
            0 /* metaState */, KeyCharacterMap.VIRTUAL_KEYBOARD, 0 /* scancode */,
            KeyEvent.FLAG_FROM_SYSTEM or KeyEvent.FLAG_VIRTUAL_HARD_KEY,
            InputDevice.SOURCE_KEYBOARD
        )
        ev.setDisplayId(displayId)
        return ev
    }


    @Throws(RemoteException::class)
    fun moveActivityToDefault() {
        val b: IBinder=ServiceManager.getService("activity")
        val activityManager1 = IActivityManager.Stub.asInterface(b)
        val options = ActivityOptions.makeBasic()
        options.launchDisplayId = 0
        activityManager1.getTasks(10)
            .forEach(object : Consumer<ActivityManager.RunningTaskInfo?> {
                override fun accept(it: ActivityManager.RunningTaskInfo?) {
                    if (it?.displayId === getDisplayId()) {
                        if (!context.getPackageName()
                                .equals(it?.baseActivity!!.packageName)
                        ) {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    activityManager1.startActivityFromRecents(
                                        it.taskId,
                                        options.toBundle()
                                    )
                                }
                                return
                            } catch (e: RemoteException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    println(it.toString())
                }
            })
    }

    private fun getDisplayId(): Int {
        return mVirtualDisplay?.getDisplay()?.getDisplayId() ?: 0
    }


    /**
     * 释放资源
     */
    fun release() {
        checkNotNull(mVirtualDisplay) { "Trying to release container that is not initialized." }
        performRelease()
    }

    private fun performRelease() {
        textureView!!.surfaceTexture!!.release()
        val displayReleased: Boolean
        if (mVirtualDisplay != null) {
//            mVirtualDisplay.setDisplayState(false);
            mVirtualDisplay!!.release()
            mVirtualDisplay = null
            displayReleased = true
        } else {
            displayReleased = false
        }
        if (displayReleased && mActivityViewCallback != null) {
            mActivityViewCallback!!.onActivityViewDestroyed(this)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        textureView?.layout(0,0, r - l, b - t);
    }


    fun setCallback(callback: StateCallback) {
        mActivityViewCallback = callback
        if (mVirtualDisplay != null && mActivityViewCallback != null) {
            mActivityViewCallback!!.onActivityViewReady(this)
        }
    }

    abstract class StateCallback {
        abstract fun onActivityViewReady(view: ActivityContainer?)
        abstract fun onActivityViewDestroyed(view: ActivityContainer?)

        /**
         * Called when a task is created inside the container.
         * This is a filtered version of [TaskStackListener]
         */
        fun onTaskCreated(taskId: Int, componentName: ComponentName?) {}

        /**
         * Called when a task is moved to the front of the stack inside the container.
         * This is a filtered version of [TaskStackListener]
         */
        fun onTaskMovedToFront(taskId: Int) {}

        /**
         * Called when a task is about to be removed from the stack inside the container.
         * This is a filtered version of [TaskStackListener]
         */
        fun onTaskRemovalStarted(taskId: Int) {}
    }


}
