# RingtonePicker（铃声选择）

这是一个铃声选择底部对话框，内置系统铃声和本地铃声的选择现成解决方案。

它是一个小项目，目录结构非常简单：

![image-20230821174918701](https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/image-20230821174918701.png)

只有一层，也就是一个 `Activity` 里套了俩 `Fragment`，然后从 `RingtoneDialogFragment`  开始，两个 `Fragment` 交互。

--

项目运行后的效果就像这样：

**底部对话框**：

<img src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/6a9eb087d958d9b26763e7c2590a60e.jpg" alt="6a9eb087d958d9b26763e7c2590a60e" style="zoom:33%;" />

**选择列表**：

<img src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/d06b17434607bc1ce82bcc8d417668f.jpg" alt="d06b17434607bc1ce82bcc8d417668f" style="zoom:33%;" />

--

核心功能没问题，也实现了一些特有功能：

- 所有铃声都能够实现播放、暂停、暂停后继续播放，并实现了播放完成的监听（使用更底层的 `MediaPlayer` 而非 `Ringtone` 控制）；
- 从对话框进入铃声选择页面时，能快速选中并定位到存储的已选铃声的位置；
- 和页面内容颜色一致的、沉浸式的状态栏；
- 快速获取并打开系统铃声列表，几乎无延迟（抛弃了系统的 `getTitle()` 方法，从铃声 `Uri` 中直接获取）。

当然，还有一些问题没能解决：

- 弹出对话框时，底部导航栏会变成黑灰色，在启用虚拟导航栏而非全面屏的设备上会影响美观。这个问题相当棘手，尝试了各种方案始终没能解决；
- UI 优化，现在的 UI 显然还不够美观；
- 库化，它应该可以变为一个可以轻松引用的依赖库，这么做的时候就得提供对外 `API`，并开放更多自定义配置了。

--

分享遇到的一些异常和解决思路：

返回的歌曲信息不对；

> 在 onBindViewHolder 中直接执行点击后的回调，没有给视图绑定监听器，在监听器中进行。

同意权限后进入空页面；

> 进入新页面的代码的位置放的不对，应该放在权限请求成功之后的块里边。

选中的状态不能保存，滑出屏幕之后消失；

在不滑动屏幕的前提下，前后点击列表中的不同项，会执行多选。这不成，应该是单选。

> 在适配器中维护一个 `selectedPosition` 属性，在选中新的条目时更新这个值，并通知局部更新。这解决了后一个问题。
>
> 要解决第一个问题，还必须在绑定视图的时候进行判断，判断动态的 `position` 是否是 `selecctedPosition`，是的话就勾选。

返回时不弹 Toast；

> 不知不觉中解决，也不知道是怎么出的问题。

点击系统铃声，进入非常慢，UI 卡死了；

> 是 Ringtone 对象提供的 getTitle 方法太重了，根据 Uri 自定义一个就可以了。

进入主界面时，RingtoneDialogFragment 不是从底部弹出来的，而是一来就在页面顶部；

> 要使用 Dialog 的 show 方法展示 Dialog，而不是 Fragment 的事务，即不是在 Activity 上 add Fragment，然后 commit。

点击新铃声，旧铃声没有停止播放，退出页面，最新选择的铃声停止播放，原先选择的铃声没有停止。

> 问题出在控制器（控制播放、停止的对象）的重建上，当点击新项时又会新建一个控制器，这个新控制器的缓存为空，所以无法停止之前的音乐。
>
> 而退出页面的时候，由于没有新建控制器，就能正常停止。
>
> 把控制器变为单例，无需创建，直接调用里边的方法即可。当前铃声的缓存还是在的！

铃声选择对话框里的当前铃声条目内，铃声名称太长的话会把播放/停止按钮给挤出去；

> 修改 `android:maxEms="8"` 即可。

如果底部对话框的当前铃声正在播放，点击进入系统铃声、本地铃声、无后，仍会播放。

> 把 currentRingtone 提到 Fragment 的成员变量上，新建一个停止方法即可。

铃声自然播放，停止后不会自动切换到停止图标；

> 使用 Ringtone 无法监听完成后的状态，得改用 MediaPlayer 实现更精细的控制。
>
> 包括完成后通知、从暂停处继续播放。

在 onViewCreated 中注册观察者，会接受到多个状态通知，且图标不随状态变化（播放、暂停的时候没问题）。

> 主要问题是从列表回来后当前铃声的图标不变化，播放完成后也不变化。最后检查发现，竟然是自己图标设置错误的问题，不是程序的问题。
>
> 当然，程序也有点问题，那就是注册了多个观察者，多个观察者同时接受状态改变的通知，在日志中输出多个结果。
>
> 这是 Fragment 销毁是观察者没有释放的问题，也是 lifecycleOwner 设置错误的问题。在 Fragment 中应该使用 `getViewLifecyleOwner()`，而非 `requireActivity`。

底部对话框的圆角无法调整；

> 创建了一个自定义主题，在自定义主题中引入了圆角样式（Shape），然后在 Dialog 创建的时候返回自定义主题的 Dialog 对象。
>
> 但这也留下了一个非常棘手的问题：系统导航栏变成黑灰色了！

啥都没选再回来，当前铃声无法播放；

> 什么都没选，回来的时候 `from_back` 标志就不应该设为 true，使播放器不能初始化，进而导致铃声无法播放。
>
> 改 `from_back` 标志为 `from_back_selected`，只有在选择了铃声再回来才设置为 true，否则就设置为 false（需要手动设置，之前的状态可能已经被设为 true 但还没改过来）。

