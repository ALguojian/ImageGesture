## 手势放大缩小图片



1.Add it in your root build.gradle at the end of repositories:

```
allprojects {
        repositories {

            maven { url 'https://jitpack.io' }

                            }
            }
```

Step 2. Add the dependency
```
dependencies {
            compile 'com.github.ALguojian:ImageGesture:1.1.1'
	}
```


使用,上下文，默认索引，list的图片集合

```
 ImageGesture.setDate(this,1,strings);
```