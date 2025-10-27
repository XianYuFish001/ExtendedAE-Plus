# ExtendedAE Plus
_这个分支是粗浅的EMI适配,技术力不足,请见谅_

_(这里也许不应该完完全全列出所有差异, 但是懒癌发作, 引导什么的不会写了, 只能放在这里了😅)_

### 相较于主分支(1.4.3)增加/更改的QoL功能
- 新增
  - EMI 适配
  - 配方树/配方界面快速拉起样板编写&上传 _(EMI 独占)_
    - 配方树界面: [`ctrl` + `中键`] 点击节点
    - 配方界面: [`ctrl` + `左键`] 点击配方转移按钮 _(需要设置项`独立上传按钮`关闭)_
  - 样板供应器列表过滤功能增强:
    - 支持匹配本地化键名
    - 支持在多个获取到的候选词之间切换
      - 使用 [`鼠标滚轮`] 切换
  - 远程打开机器界面功能增强:
    - 支持真正意义上的不限距离远程打开 _(暴力检测)_
    - AE2 与 mekanism 适配
    - [`shift` + `左键`] 点击 CPU 界面的物品触发
    - _(仍然使用暴力枚举, 存在打开错误界面可能性)_
  - 不再会自动上传合成(即非编码样板)
- 修改
  - 设置项:
    - 独立上传按钮:
      - 控制样板编码终端是否存在独立的上传按钮
      - 关闭时, [`ctrl` + `左键`] 点击 编码/配方转移 _(EMI 独占)_ 按钮 即可快速上传样板
    - 覆盖 AE2WT 选取:
      - 开启时, 使用 EAEP 内置的选取逻辑 _(不限制拉取数量, 每次拉取一组被选取方块)_
      - 关闭时, 使用 AE2WT 的选取逻辑 _(即不做修改, 未安装时无选取功能)_
    - 需要上传核心:
      - 开启时, 样板只能上传至拥有上传核心的装配矩阵
- New Features
  - EMI Support
  - Quick pattern encoding & uploading from the recipe tree/recipe screen _(EMI Exclusive)_
    - Recipe Tree screen: [`Ctrl` + `Middle-Click`] on a node
    - Recipe screen: [`Ctrl` + `Left-Click`] on the Recipe Transfer Button _(Requires the `Independent Uploading Button` setting toggled off)_
  - Pattern Provider List Filtering Enhancements:
    - Supports matching localized key names
    - Supports cycling through multiple matched candidate terms
      - Use the [`Mouse Wheel`] to cycle
  - Remote Machine GUI Opening Enhancements:
    - Supports truly unlimited distance remote opening _(Brute-force detection)_
    - AE2 and Mekanism adaptation
    - Triggered by [`Shift` + `Left-Click`] on a item which in the CPU screen
    - _(Still uses brute-force enumeration; may potentially open the wrong GUI)_
  - It will no longer uploads crafting patterns automatic.
- Modifications
  - Settings:
    - Independent Uploading Button:
      - Controls whether the Pattern Encoding Terminal has a dedicated uploading button
      - When disabled, [`Ctrl` + `Left-Click`] on the Encode Button / the Recipe Transfer Button _(EMI Exclusive)_ to quickly upload a pattern
    - Override AE2WT Picking:
      - When enabled, uses EAEP's built-in block-picking logic _(No pull quantity limit, pulls one stack of the block per click)_
      - When disabled, uses AE2WT's block-picking logic _(Unmodified, no pick block functionality if AE2WT is not installed)_
    - Needs Uploading Core:
      - When enabled, patterns can only be uploaded to an Assembly Matrix that has an Uploading Core
