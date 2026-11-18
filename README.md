# VillagerInABukkit · 村民装起来

本项目是 JustDoom 的 VillagerInABukkit 1.6.4 简体中文版，服务端使用 **Paper 26.3 和 Java 25**。 [上游项目](https://github.com/JustDoom/VillagerInABukkit)

## 使用

将插件 JAR 放入 Paper 的 `plugins` 文件夹。手持空桶右键装起村民、僵尸村民或流浪商人，再手持装好的桶右键点击方块放出。管理命令使用 `/viab`，重新读取配置用 `/viab reload`。

Java 版 1.21.4 至 26.3 可以把配套 ZIP 放入客户端的 `resourcepacks` 文件夹并启用。若要在玩家进服时自动发送，请上传 ZIP，并在插件的 `config.yml` 中填入下载直链 `resource-pack-url`、ZIP 的 SHA-1 `resource-pack-hash`，再将 `resource-pack` 设为 `true`。上游版本已经装好的桶仍可放出生物，重新装入后会显示中文。

## 构建

在 Java 25 环境中运行以下命令。Windows 使用 `gradlew.bat`。

```sh
./gradlew clean shadowJar resourcePackZip -PbuildWithoutGitHash
```

从 `build/libs/` 取得插件 JAR，从 `build/distributions/` 取得材质包 ZIP。 [GitHub Actions 构建](https://github.com/mcdudu233/VillagerInABukkit/actions/workflows/build.yml)

## 上游开发计划

- [ ] 成就
- [x] 村民、流浪商人和僵尸村民的配置
- [x] 支持僵尸村民和流浪商人
- [x] 物品材质包
- [x] 通过权限控制玩家操作
- [x] 支持发射器
