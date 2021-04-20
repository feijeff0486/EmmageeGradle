# EmmageeGradle
Emmagee gradle版本，引入RxJava优化线程，更好兼容TV上UI操作；

## 渠道包：
```groovy
    flavorDimensions "device"
    productFlavors {
        //大麦盒子
        damai {
            dimension "device"
            versionCode 1
            versionName "1.0"
        }
        //其他设备
        other {
            dimension "device"
            versionCode 1
            versionName "1.0"
        }
    }
```