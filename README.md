# Grok 视频生成 (AIDE Pro 版本)

基于传统 XML 布局构建的 Grok 视频生成客户端，针对 AIDE Pro 优化。

## 功能

- 🎬 输入文字提示词生成视频
- ⚙️ 自定义配置 API Key、Base URL 和模型
- 📊 实时显示生成进度
- 📱 视频预览和播放
- 📂 历史记录管理

## 技术栈

- **UI**: 传统 XML 布局 + Material Components
- **架构**: Activity + Kotlin Coroutines
- **网络**: OkHttp + Gson
- **存储**: SharedPreferences + JSON 文件
- **图片加载**: Glide

## AIDE Pro 构建步骤

1. 打开 AIDE Pro
2. 文件 → 打开项目
3. 选择 `GrokVideoGen_AIDE` 目录
4. 等待 Gradle 同步完成
5. 点击运行按钮构建 APK

## 首次使用

1. 安装 APK 到设备
2. 打开应用
3. 点击右上角设置图标
4. 填入配置：
   - Base URL: `https://api.zayuapi.com`
   - API Key: 你的密钥
   - 模型: `grok-imagine-video-480p`
5. 返回主界面输入提示词

## 注意事项

- 最低支持 Android 5.0 (API 21)
- 首次构建需要下载依赖
- API Key 保存在本地，请勿分享
- 视频生成约需 30-60 秒

## 与原版的区别

- ❌ 移除了 Jetpack Compose
- ✅ 使用传统 XML 布局
- ✅ 降低了最低 API 要求
- ✅ 简化了 Gradle 配置
- ✅ AIDE Pro 可直接构建

## License

MIT
