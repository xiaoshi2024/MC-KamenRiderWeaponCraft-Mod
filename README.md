# 假面骑士武器 (Kamen Rider Weapon Craft)

一个为 Minecraft 添加假面骑士主题武器的 NeoForge Mod。

## 功能特性

### 平成嘿嘿剑 (Heiseisword)
- ✨ 19位平成假面骑士的攻击特效
- 🎯 骑士选择系统 (X键/C键切换)
- ⚡ 能量系统 - 使用攻击消耗能量，自动恢复
- 🔥 必杀时刻模式 - 组合多位骑士发动连击
- 💥 超必杀模式 - 集合所有骑士的终极攻击
- 🚂 电王模式 - 4种武器形态切换 (剑/竿/斧/枪)

### 支持的假面骑士
| 骑士 | 名称 |
|------|------|
| Kuuga | 空我 |
| Agito | 亚极陀 |
| Ryuki | 龙骑 |
| Faiz | 法伊兹 |
| Blade | 剑 |
| Hibiki | 响鬼 |
| Kabuto | 甲斗 |
| Den-O | 电王 |
| Kiva | 月骑 |
| Decade | 帝骑 |
| W | 双骑 |
| OOO | 欧兹 |
| Fourze | 卌骑 |
| Wizard | 巫骑 |
| Gaim | 铠武 |
| Drive | 驰骑 |
| Ghost | 幽灵 |
| Ex-Aid | 艾克赛德 |
| Build | 创骑 |

## 操作说明

| 按键 | 功能 |
|------|------|
| 右键 | 远程攻击 (长按蓄力) |
| Shift + 右键 | 切换必杀时刻模式 |
| X (默认) | 选择下一个骑士 / 激活超必杀 |
| C (默认) | 选择上一个骑士 |

## 能量系统
- 最大能量: 100
- 自动恢复: 2/秒
- 普通攻击消耗: 约20能量
- 必杀攻击消耗: 根据组合骑士数量

## 开发信息

- **Minecraft版本**: 1.21.1
- **模组平台**: NeoForge 21.1.229+
- **依赖库**: GeckoLib 4.8.4+

## 安装方式

1. 安装 NeoForge 21.1.229 或更高版本
2. 将模组 JAR 文件放入 `.minecraft/mods` 文件夹
3. 启动游戏

## 构建项目

```bash
# 刷新依赖
./gradlew --refresh-dependencies

# 清理构建
./gradlew clean

# 构建模组
./gradlew build

# 运行客户端
./gradlew runClient