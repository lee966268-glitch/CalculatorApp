# 简易计算器 CalculatorApp

一个用 Kotlin 编写的安卓简易计算器工程，可直接用 Android Studio 打开运行。

## 功能

- 四则运算：`+ − × ÷`，支持连续运算，遵守先乘除后加减
- 小数点、小数运算
- `%` 百分号（`50% = 0.5`）
- `+/−` 正负号切换
- `C` 清空、`⌫` 退格
- 除数为零、非法表达式时显示“错误”而不是崩溃
- 上方小字显示完整表达式（如 `12+3=`），下方大字显示输入/结果

## 工程信息

- 语言：Kotlin
- 包名：`com.example.calculator`
- minSdk 24（Android 7.0），targetSdk / compileSdk 34
- AGP 8.5.2，Gradle 8.7，Kotlin 1.9.24
- 依赖：androidx.appcompat / core-ktx / material / constraintlayout

## 目录结构

```
CalculatorApp/
├── settings.gradle
├── build.gradle            # 顶层构建文件（插件版本）
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle        # app 模块构建文件
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/calculator/MainActivity.kt  # 计算器逻辑
        └── res/
            ├── layout/activity_main.xml  # 界面布局
            └── values/strings.xml、colors.xml、themes.xml
```

## 如何运行（本地）

1. 用 Android Studio（推荐 Hedgehog / Koala 及以上）选择 **Open**，打开 `CalculatorApp` 文件夹；
2. 等待 Gradle Sync 完成（首次会自动下载 Gradle 8.7 和依赖，需要联网）；
3. 点击 **Run ▶**，选择模拟器或真机运行。

> 如果 Sync 失败：检查 `gradle-wrapper.properties` 中的 Gradle 版本、确认已安装 JDK 17（Android Studio 自带 JBR 即可）。

## 用 GitHub Actions 云端构建 APK（无需本地配环境）

工程已内置工作流 `.github/workflows/build-apk.yml`，push 后自动构建出可直接安装的 Debug APK。

1. 在 GitHub 新建一个仓库（例如 `CalculatorApp`，Public/Private 均可，Actions 免费额度内够用）；
2. **把 `CalculatorApp` 文件夹“里面”的所有文件**（`app/`、`gradle/`、`.github/`、`gradlew`、`settings.gradle` 等）推到仓库**根目录**；
   ⚠️ 注意：不要把 `CalculatorApp` 整个文件夹套一层推上去，否则工作流找不到 `gradlew`；
   ```bash
   cd CalculatorApp
   git init
   git add .
   git commit -m "简易计算器"
   git branch -M main
   git remote add origin https://github.com/<你的用户名>/<仓库名>.git
   git push -u origin main
   ```
   确保 `gradlew`、`gradle/wrapper/gradle-wrapper.jar`、`.github/workflows/build-apk.yml` 都被提交了（本工程的 `.gitignore` 不会忽略它们）；
3. 打开仓库页面 → **Actions** → 点进 `Build Debug APK` 那次运行 → 等绿色 ✅ → 拉到最下方 **Artifacts** → 下载 `CalculatorApp-debug-apk`，解压即得 `app-debug.apk`，传到手机直接安装。

**打正式包发布：**
```bash
git tag v1.0
git push origin v1.0
```
推送 `v*` 标签后，Actions 会自动在仓库 **Releases** 页创建 `v1.0` 版本并附上 APK 下载链接。

## 核心实现说明

- 界面：`activity_main.xml` 用纵向 `LinearLayout` + 5 行横向等权 `Button` 实现 4×5 键盘，`tvExpression` 显示表达式、`tvDisplay` 显示输入与结果。
- 输入校验：`appendInput()` 防止 `1++2`、`1..2`、运算符开头等非法输入。
- 求值：`evaluate()` 手写两遍扫描（先乘除后加减），不依赖脚本引擎，在 Android 上安全可用。
- 正负号：`toggleSign()` 只翻转最后一个数字的符号，并正确区分一元负号和二元减号。
