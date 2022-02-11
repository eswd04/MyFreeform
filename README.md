# 介绍
该应用是基于Android hidden api 制作的一个悬浮窗app,主要能像国内小窗一样让应用运行时以小窗的方式呈现

为了实现系统小窗的功能，主要参照其他一些应用的做法，以及查看了Android系统中ActivityView源码来实现。
系统中的ActivityView采用的是SurfaceView,要实现画面的缩放有些困难。



# 缺点
需要系统签名，系统签名可以到相关ROM的开源项目中查找(或许可以考虑嵌入到类原生系统中去？)
功能不完善，仅供学习和参考

以及...

尝试试过使用Root实现的方法，但是如果使用context.startActivity这个方法，应用会启动失败，因为没权限。
具体查看 <a href="https://source.android.com/devices/tech/display/multi_display/activity-launch">Activity 启动政策</a>

# 其他
（或许...可以在android 9上运行？（没试过））

android30.jar 可以使用其他的库代替，主要是用于隐藏Api的调用


# 截图
具体查看screenshots目录



有兴趣可以一起交流
