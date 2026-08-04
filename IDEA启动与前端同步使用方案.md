# IDEA 启动与前端同步使用方案

## 1. 方案目标

本方案用于解决以下问题：

- 在 IDEA 中一键启动完整项目。
- 日常修改 Vue 源码后，浏览器立即显示最新页面。
- 避免误把 Spring Boot `8081` 中的旧静态资源当成当前前端源码。
- 毕业答辩前可以构建为单端口 `8081` 版本，方便演示和验收。

采用两种明确分开的运行模式：

| 模式 | IDEA 配置名 | 页面地址 | 用途 |
| --- | --- | --- | --- |
| 日常开发 | `Flower Full Stack - Dev` | `http://127.0.0.1:5173/` | 开发、调试和页面优化 |
| 答辩打包 | `Flower Backend - Packaged` | `http://127.0.0.1:8081/` | 最终验收和单端口演示 |

## 2. 端口与目录职责

### 2.1 日常开发模式

- `5173`：Vite 前端开发服务，直接读取 `flower-frontend/src`。
- `8081`：Spring Boot 后端，提供 `/api` 接口和 `/images` 静态图片。
- `flower-frontend/vite.config.js` 已将 `/api` 和 `/images` 代理到 `8081`。
- 日常开发只访问 `5173`，不要通过 `8081` 判断前端源码是否已更新。

### 2.2 答辩打包模式

- 先执行 `npm run build`，生成 `flower-frontend/dist`。
- 再把 `dist` 中的新构建产物同步到 `flower-web/src/main/resources/static`。
- 重新构建并启动 Spring Boot 后，`8081` 才会显示最新前端。

## 3. IDEA 一键启动配置

以下配置只需要在 IDEA 中设置一次。

### 3.1 整理后端运行配置

1. 打开 IDEA 菜单：`Run` → `Edit Configurations...`。
2. 当前项目存在两个同名 `FlowerApplication` 配置，一个是 Spring Boot 类型，一个是普通 Java 类型。
3. 保留 Spring Boot 类型的配置，并重命名为：

   `Flower Backend - Dev`

4. 确认启动类为：

   `com.flower.FlowerApplication`

5. 确认工作目录为：

   `D:\GProject\flower_trae\flower-sales`

6. 在 `Environment variables` 中设置以下本机变量：

   - `FLOWER_JWT_SECRET`：必填，JWT 签名密钥，至少 32 字节。
   - `SPRING_DATASOURCE_USERNAME`：必填，本机 MySQL 应用账号。
   - `SPRING_DATASOURCE_PASSWORD`：必填，本机 MySQL 应用账号密码。
   - `SPRING_DATASOURCE_URL`：选填，用于覆盖默认的本机 `flower_sales` 连接地址。

   可以在本机 PowerShell 中生成随机 JWT 密钥，生成结果只填写到 IDEA，不要写入源码、文档或 Git：

   ```powershell
   $bytes = New-Object byte[] 32
   [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
   [Convert]::ToBase64String($bytes)
   ```

7. 当前数据库已经存在管理员时，不要启用管理员初始化。只有新数据库首次创建管理员时才临时增加：

   - `FLOWER_ADMIN_BOOTSTRAP_ENABLED`：首次创建时设为 `true`，成功后删除或改为 `false`。
   - `FLOWER_ADMIN_USERNAME`：管理员用户名，未设置时默认为 `admin`。
   - `FLOWER_ADMIN_PASSWORD`：首次创建使用的管理员密码，至少 8 位。

   管理员已经存在时，初始化器不会修改或重置其密码。

8. IDEA 项目目录下的 `.idea/` 已被 Git 忽略，但仍不要把真实变量值复制到截图、文档、提交信息或其他受 Git 跟踪的文件中。
9. 不要同时启动两个后端配置，否则可能出现 `8081` 端口被占用。

### 3.2 新建 Vite 前端运行配置

1. 在 `Run/Debug Configurations` 中点击 `+`。
2. 选择 `npm`。
3. 配置以下内容：

   - Name：`Flower Frontend - Dev`
   - package.json：`D:\GProject\flower_trae\flower-sales\flower-frontend\package.json`
   - Command：`run`
   - Scripts：`dev`
   - Arguments：`-- --host 127.0.0.1 --open`

4. Node.js 和 npm 使用 IDEA 当前已经识别到的本机版本，不需要重新安装。
5. 保存配置。

`--open` 会在 Vite 启动完成后自动打开 `http://127.0.0.1:5173/`。

### 3.3 新建一键启动组合配置

1. 在 `Run/Debug Configurations` 中点击 `+`。
2. 选择 `Compound`。
3. Name 填写：

   `Flower Full Stack - Dev`

4. 添加以下两个配置：

   - `Flower Backend - Dev`
   - `Flower Frontend - Dev`

5. 保存配置。

以后日常开发时，只选择 `Flower Full Stack - Dev` 并点击运行按钮即可。

组合配置会启动已经设置好环境变量的 `Flower Backend - Dev`，不需要在 `Flower Full Stack - Dev` 中重复填写变量。

## 4. 日常开发使用方法

### 4.1 启动

1. 在 IDEA 顶部选择 `Flower Full Stack - Dev`。
2. 点击运行按钮。
3. 等待后端控制台出现：

   `Tomcat started on port 8081 (http)`

   中文含义：Tomcat 已在 `8081` 端口启动。

4. 等待前端控制台出现：

   `Local: http://127.0.0.1:5173/`

   中文含义：Vite 前端已经可以通过 `5173` 访问。

5. 浏览器应自动打开 `http://127.0.0.1:5173/`。

### 4.2 修改前端

日常只修改以下源码：

- `flower-frontend/src/components`
- `flower-frontend/src/router`
- `flower-frontend/src/style.css`
- 其他 `flower-frontend/src` 下的业务文件

保存 Vue、JavaScript 或 CSS 文件后，Vite 会自动热更新，不需要执行 `npm run build`。

如果修改了 `vite.config.js`，需要重新启动 `Flower Frontend - Dev`。

### 4.3 修改后端

Java 后端代码修改后，根据 IDEA 的编译情况重新启动 `Flower Backend - Dev`。

后端重新启动不会影响前端源码；Vite 恢复连接后仍然访问 `5173`。

### 4.4 判断是否打开了正确页面

浏览器地址栏必须是：

`http://127.0.0.1:5173/`

可以查看页面 HTML，正确的 Vite 开发页面会加载：

- `/@vite/client`
- `/src/main.js`

如果页面加载的是以下形式，说明当前打开的是后端构建版本，而不是实时源码：

- `/assets/index-xxxxxxxx.js`
- `/assets/index-xxxxxxxx.css`

## 5. DIY 页面检查规则

当前正式 DIY 路由为：

- `/user/diy` → `flower-frontend/src/components/DiyPageMigrated.vue`
- `/user/plan/:id` → `flower-frontend/src/components/DiyPlanDetailMigrated.vue`

日常验证地址：

`http://127.0.0.1:5173/#/user/diy`

当前迁移版 DIY 页面应能看到：

- `DIY BOUQUET`
- 花材架
- 花艺模板
- 花束工作台
- 花材清单
- 当前真实花材 WebP

如果看到“选择花卉”“花束设计区”和旧圆形包装画布，说明打开了旧的 `8081` 构建产物。

## 6. 答辩前打包为 8081 单端口版本

日常开发完成并确认 `5173` 页面正确后，再执行本节。

### 6.1 构建前端

在 PowerShell 中执行：

```powershell
Set-Location -LiteralPath 'D:\GProject\flower_trae\flower-sales\flower-frontend'
npm run build
```

成功后必须存在：

- `flower-frontend/dist/index.html`
- `flower-frontend/dist/assets/index-*.js`
- `flower-frontend/dist/assets/index-*.css`

本项目已经存在可用的 `node_modules`。如果以后本机缺少依赖，不要直接执行 `npm install`，应先检查本地资源并按照 `AGENTS.md` 获得安装许可。

### 6.2 同步构建产物

执行以下 PowerShell 命令，只覆盖新的入口和构建资源，不删除后端 `images` 目录：

```powershell
$projectRoot = 'D:\GProject\flower_trae\flower-sales'
$frontendDist = Join-Path $projectRoot 'flower-frontend\dist'
$backendStatic = Join-Path $projectRoot 'flower-web\src\main\resources\static'
$backendAssets = Join-Path $backendStatic 'assets'

if (-not (Test-Path -LiteralPath (Join-Path $frontendDist 'index.html'))) {
    throw '前端 dist 不完整，请先执行 npm run build。'
}

if (-not (Test-Path -LiteralPath $backendAssets)) {
    New-Item -ItemType Directory -Path $backendAssets | Out-Null
}

Copy-Item -LiteralPath (Join-Path $frontendDist 'index.html') `
    -Destination (Join-Path $backendStatic 'index.html') -Force

Get-ChildItem -LiteralPath (Join-Path $frontendDist 'assets') -File | ForEach-Object {
    Copy-Item -LiteralPath $_.FullName `
        -Destination (Join-Path $backendAssets $_.Name) -Force
}
```

注意：

- 不要删除 `flower-web/src/main/resources/static/images`。
- 不要把其他项目的 `dist` 复制进来。
- 旧哈希文件即使暂时保留，也不会被新 `index.html` 引用；后续如需清理，应先核对新入口引用，再只清理旧的 `index-*.js` 和 `index-*.css`。

### 6.3 重新构建并启动后端

1. 停止正在运行的 `Flower Full Stack - Dev`。
2. 在 IDEA 中执行 `Build` → `Rebuild Project`。
3. 启动 Spring Boot 配置。
4. 访问：

   `http://127.0.0.1:8081/`

如果用于正式打包，也可以在项目根目录执行：

```powershell
Set-Location -LiteralPath 'D:\GProject\flower_trae\flower-sales'
mvn package -DskipTests
```

### 6.4 核对 8081 是否使用新资源

分别查看以下两个入口文件：

```powershell
Get-Content -LiteralPath 'D:\GProject\flower_trae\flower-sales\flower-frontend\dist\index.html'
Get-Content -LiteralPath 'D:\GProject\flower_trae\flower-sales\flower-web\src\main\resources\static\index.html'
```

两个文件引用的 `index-*.js` 和 `index-*.css` 名称应一致。

再访问 `8081`：

```powershell
(Invoke-WebRequest -Uri 'http://127.0.0.1:8081/' -UseBasicParsing).Content
```

实际返回的资源名也必须一致。

## 7. 浏览器缓存处理

后端静态资源当前配置为：

`Cache-Control: max-age=7200, public`

中文含义：浏览器可以公开缓存静态资源两小时。

因此，更新 `8081` 构建产物后应执行以下任一操作：

- 使用 `Ctrl + F5` 强制刷新。
- 打开浏览器开发者工具并勾选 `Disable cache`。
- 使用无痕窗口访问 `8081`。

Vite `5173` 使用开发模式和热更新，日常通常不需要处理构建缓存。

## 8. 常见问题排查

### 8.1 IDEA 启动后仍显示旧页面

先检查浏览器地址：

- `5173`：当前源码，日常开发应使用此地址。
- `8081`：后端 static 构建版本，只有重新构建和同步后才会更新。

### 8.2 5173 无法访问

- 检查 `Flower Frontend - Dev` 是否正在运行。
- 检查控制台是否出现 Vite 启动成功地址。
- 检查 `5173` 是否被其他进程占用。

### 8.3 8081 无法访问

- 检查 `Flower Backend - Dev` 是否正在运行。
- 检查 MySQL 是否可连接。
- 检查是否重复启动了两个 `FlowerApplication`，导致端口冲突。

### 8.4 5173 页面能打开，但接口或图片失败

- 检查 Spring Boot `8081` 是否已启动。
- `/api` 和 `/images` 都需要通过 Vite 代理到 `8081`。
- 如果修改过 `vite.config.js`，重启 Vite。

### 8.5 DIY 页面又变成旧版本

检查页面资源：

- 出现 `/src/main.js`：正在使用当前源码。
- 出现 `/assets/index-*.js`：正在使用后端构建产物。

再检查 `flower-frontend/src/router/index.js` 是否仍引用 `DiyPageMigrated.vue`。

## 9. 推荐工作习惯

1. 日常开发只运行 `Flower Full Stack - Dev`，只访问 `5173`。
2. 每次只优化一个页面或明确模块。
3. 修改前先确认设计方向，修改后验证功能和桌面截图。
4. 页面验证通过后再执行 `npm run build`。
5. 需要验证答辩版本时，再同步 `dist` 并检查 `8081` 的新哈希资源。
6. 构建、自测和视觉检查通过后，更新 `CONTEXT.md`，再提交和推送。

## 10. 一句话记忆

开发看 `5173`，答辩看重新构建后的 `8081`；只启动 Spring Boot 不会自动读取最新 Vue 源码。
