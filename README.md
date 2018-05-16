# BleBluetoothDoorLock v0.5

## 背景
```
网上的蓝牙门锁代码要么是蓝牙2.0版本的 要么只有蓝牙4.0demo的
没有两者集成的开源项目 毕设想混很难受
安卓没学过 单片机没学过 找毕设代做想买个毕设 结果连买都买不到！
```
```
最后fork了一份用传统蓝牙的安卓门锁项目
导入FastBle第三方开源库 将原本的socket传输改为Gatt传输
再把原本一些不够完善的功能完善了下
对应写了一份硬件的c代码
当毕设交了
```

## 功能

1. 开锁
2. 改密码
3. 显示密码
说明：默认密码
主人密码：128128
住户密码：281281
访客密码：812812

```
硬件：cc2541模块 51单片机 5v继电器 12v电磁锁 12v门禁电源
硬件代码放在目录下的 BleBluetoothDoorLock.c里

附上参考的文章：
https://blog.csdn.net/sunzhaojie613/article/details/51511054
```
```
软件：Android studio
时间比较赶，水平十分有限，代码写的很混乱，懒得改了
关于配对，已经写好并测过了取消配对的代码，放在ClsUtils和BleBluetoothManager里
不过还没加上修改PIN值的相关代码，再加功能怕停不下来毕设搞不完了。。
修改PIN我觉得可以通过断开蓝牙连接后通过单片机的串口发送AT指令给蓝牙模块实现

附上ble蓝牙参考的文章：
https://github.com/Jasonchenlijian/FastBle
http://www.android-doc.com/reference/android/bluetooth/BluetoothGatt.html
http://www.th7.cn/Program/Android/201703/1120984.shtml

附上安卓fork来的原代码：
https://github.com/huyifan/BlueTooth_lock


```

```
最后吐槽几句 边实习边做毕设是真的麻烦
实习用go 毕设用安卓.两者一比
同样是谷歌下的开发团队 Android studio怎么就这么难用 对新手太不友好了
新人教学文章和视频乱七八糟 导入各种项目都不能运行 
最后还是创了个空项目 再把其他项目代码拷进来才能运行 
导入个第三方库又麻烦的一批 
go就怎么用怎么简单舒服
这差距简直了...
```