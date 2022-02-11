package com.demo.myfreeform

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.PointF
import android.os.Binder
import android.os.IBinder
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.view.marginRight
import com.demo.myfreeform.util.DispalyUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FreeFormService:Service() {


    val binder=MyBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder;
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessage(message: AppInfo) {
        val intent2 = Intent()
        val pkg= message.applicationInfo?.activityInfo?.packageName.toString()
        val name=message.applicationInfo?.activityInfo?.name.toString()

        Log.d("TAG", "onGetMessage: ")
        val componentName= ComponentName(pkg,name)
        intent2.setComponent(componentName)

        binder.testView.startActivity(
            PendingIntent.getActivity(
                this,
                0,
                intent2,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

    }

    inner class MyBinder:Binder(){
        lateinit var testView: ActivityContainer
        private lateinit var windowLayoutParams: WindowManager.LayoutParams
        var wm: WindowManager? = null


        fun initWindowService(){

            //初始化悬浮窗口参数
            wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowLayoutParams = WindowManager.LayoutParams()
            windowLayoutParams.format = PixelFormat.RGBA_8888
            windowLayoutParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                        WindowManager.LayoutParams.FLAGS_CHANGED
            windowLayoutParams.gravity = Gravity.START or Gravity.TOP
            windowLayoutParams.x = 200
            windowLayoutParams.y = 200
            windowLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            windowLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
//            windowLayoutParams.width = 480
//            windowLayoutParams.height = 800
            windowLayoutParams.type =WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

            val windowView = LayoutInflater.from(this@FreeFormService).inflate(R.layout.window_view, null)
            windowView.layoutParams = windowLayoutParams

            val cardView = windowView.findViewById<CardView>(R.id.main_card)




            cardView.layoutParams.width=480
            cardView.layoutParams.height=800

            val windowW = getResources().getDisplayMetrics().widthPixels;
            val windowH = getResources().getDisplayMetrics().heightPixels;


            val moveRight=windowView.findViewById<View>(R.id.view_right)

            val pointDown=PointF()
//            var moveX=0F;
//            var moveY=0F;
            var defaultX=0
            var defaultY=0
            var carViewdefaultR=0
            var carViewdefaultB=0
            val dp2=DispalyUtil.dip2px(this@FreeFormService,2F)
            moveRight.setOnTouchListener { v, event ->
                when(event.action){
                    MotionEvent.ACTION_DOWN->{
                        pointDown.x=event.rawX
                        pointDown.y=event.rawY
                        defaultX=windowView.right
                        defaultY=windowView.bottom
                        carViewdefaultR=cardView.right
                        carViewdefaultB=cardView.bottom
                    }
                    MotionEvent.ACTION_MOVE->{
                        val distX=pointDown.x-event.rawX
                        val distY=pointDown.y-event.rawY
                        cardView.layoutParams.width=carViewdefaultR-distX.toInt()-DispalyUtil.dip2px(this@FreeFormService,4F)
                        cardView.layoutParams.height=carViewdefaultB-distY.toInt()
//                        val cl=cardView.layoutParams as LinearLayout.LayoutParams
//                        cl.rightMargin=DispalyUtil.dip2px(this@FreeFormService,2F)
                        windowLayoutParams.width = defaultX-distX.toInt()
                        windowLayoutParams.height = defaultY-distY.toInt()
                        wm!!.updateViewLayout(windowView, windowLayoutParams);

                    }
                    MotionEvent.ACTION_UP->{

                    }
                }

                return@setOnTouchListener true
            }

            println("WindowWidth : $windowW")
//            cardView.layoutParams.width = 840       //默认启动大小
//            cardView.layoutParams.height = (1600).toInt()
            val topBar=windowView.findViewById<ImageView>(R.id.ic_top_bar)


            val pointDown2=PointF()
            var wStartX = 0
            var wStartY = 0

            topBar.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        pointDown2.x = event.rawX
                        pointDown2.y = event.rawY;
                        wStartX = windowLayoutParams.x
                        wStartY = windowLayoutParams.y
                        return@setOnTouchListener true;
                    };
                    MotionEvent.ACTION_MOVE -> {
                        windowLayoutParams.x = wStartX + (event.rawX - pointDown2.x).toInt();
                        windowLayoutParams.y = wStartY + ((event.rawY - pointDown2.y)).toInt();
                        wm!!.updateViewLayout(windowView, windowLayoutParams);
                    }

                    MotionEvent.ACTION_UP -> {

                        if (event.rawX >= windowW - windowView.measuredWidth/3) {
                            windowLayoutParams.x = windowW - windowView.measuredWidth
                        }else if (event.rawX<=windowView.measuredWidth/3){
                            windowLayoutParams.x = 0
                        }
                        if (event.rawY >= (windowH- windowView.measuredHeight)) {
                            windowLayoutParams.y = (windowH - windowView.measuredHeight)
                            println("Height:${windowView.measuredHeight}")
                        }else if(event.rawY <= 0){
                            windowLayoutParams.y = 0
                        }
                        wm!!.updateViewLayout(windowView, windowLayoutParams);

                    }
                }

                return@setOnTouchListener true
            }


            testView= ActivityContainer(this@FreeFormService)
            cardView.addView(testView)


            val mainIntent = Intent(this@FreeFormService, SecondActivity::class.java)
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK)
            val mainPendingIntent = PendingIntent.getActivity(
                this@FreeFormService,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val activityState = object : ActivityContainer.StateCallback() {
                override fun onActivityViewReady(view: ActivityContainer?) {
                    cardView?.post {
                        try {
                            testView.startActivity(mainPendingIntent)
                        } catch (e: RuntimeException) {

                        }
                    }
                }
                override fun onActivityViewDestroyed(view: ActivityContainer?) {
                }

            }

            testView.setCallback(activityState)

            val bottomBar=windowView.findViewById<ImageView>(R.id.ic_back)


            var touchRawY=0F;
            var isClose=false;
            var fullScreen=false;
            val gestureDetector= GestureDetector(this@FreeFormService, object :
                GestureDetector.OnGestureListener {
                override fun onDown(e: MotionEvent?): Boolean {
                    touchRawY= e?.rawY!!
                    return true
                }


                override fun onShowPress(e: MotionEvent?) {
                    println("showPress:${e?.action}")
                }

                override fun onSingleTapUp(e: MotionEvent?): Boolean {
                    testView.backPress()
                    return true
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent?,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {

                    if (e2?.rawY?.minus(touchRawY)!!<0){
                        cardView.alpha=(300-(touchRawY-e2.rawY))/300
                    }
                    isClose = touchRawY-e2.rawY >200
                    fullScreen=e2.rawY.minus(touchRawY) >200
                    if (touchRawY-e2.rawY > 0){
                        var dist=touchRawY-e2.rawY
                        if (dist>300F) dist= 300F
                        var sPercent=1-dist/1500F

                        windowView.scaleX=sPercent
                        windowView.scaleY=sPercent
                    }
                    if (touchRawY-e2.rawY < 0) {

                        var dist = e2.rawY - touchRawY
                        if (dist > 300F) dist = 300F
                        var sPercent = 1 + dist / 1500F

                        windowView.scaleX = sPercent
                        windowView.scaleY = sPercent
                    }

                        return true
                }

                override fun onLongPress(e: MotionEvent?) {
                    testView.startActivity(mainPendingIntent)
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent?,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    return true
                }
            })

            bottomBar.setOnTouchListener { v, event ->
                when(event.action){
                    MotionEvent.ACTION_UP->{
                        if (isClose){
                            if (windowView != null && windowView.isAttachedToWindow) {
                                println("Close")
                                isClose=false;
                                testView.release()
                                wm!!.removeView(windowView)
                                android.os.Process.killProcess(android.os.Process.myPid())

//                                finish()
                            }
                        }else if (fullScreen){
                            testView.moveActivityToDefault()
                            fullScreen=false;
                            testView.release()
                            wm!!.removeView(windowView)
//                            android.os.Process.killProcess(android.os.Process.myPid())
                        }else{
                            val alpha=cardView.alpha
                            val animatorA= ObjectAnimator.ofFloat(cardView,"alpha",alpha,1F)
                            animatorA.duration=150

                            val scale=windowView.scaleX


                            val animatorX= ObjectAnimator.ofFloat(windowView,"scaleX",scale,1F)
                            animatorX.duration=150
                            val animatorY= ObjectAnimator.ofFloat(windowView,"scaleY",scale,1F)
                            animatorY.duration=150

                            val animatorSet=AnimatorSet()
                            animatorSet.playTogether(animatorA,animatorX,animatorY)
                            animatorSet.start()

                        }
                    }
                }
                return@setOnTouchListener gestureDetector.onTouchEvent(event)
            }

            bottomBar.setOnClickListener {
                testView.backPress()
            }
            windowView.findViewById<ImageView>(R.id.ic_back).setOnLongClickListener {
                testView.startActivity(mainPendingIntent)
                return@setOnLongClickListener true
            }

            wm!!.addView(windowView, windowLayoutParams)
        }



    }



}