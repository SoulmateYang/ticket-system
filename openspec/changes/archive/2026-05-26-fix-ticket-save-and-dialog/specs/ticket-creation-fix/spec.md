# ticket-creation-fix

## ADDED Requirements

### Requirement: 年/月卡创建字段正确映射
前端传入的 `idCard` 字段应映射为后端 DTO 的 `visitorId` 字段，系统 SHALL 正确保存身份证号。

#### Scenario: 创建年/月卡成功
- **WHEN** 用户在年/月卡管理页面填写访客姓名、手机号、身份证号并提交
- **THEN** 系统正确保存所有字段并在列表中显示新创建的票

### Requirement: 次票创建前端预校验
次票数量必须大于0，演出名称为必填项。

#### Scenario: 次票数量为0时不发请求
- **WHEN** 用户输入 quantity <= 0 并点击创建
- **THEN** 前端显示"数量必须大于0"错误提示，不发送请求

#### Scenario: 演出名称为空时不发请求
- **WHEN** 用户未填写演出名称并点击创建
- **THEN** 前端显示"演出名称不能为空"错误提示，不发送请求

### Requirement: 错误信息对用户可见
后端返回的校验错误信息应展示给用户，不得隐藏。

#### Scenario: 后端返回校验错误时显示具体信息
- **WHEN** 后端返回 {success: false, message: "手机号不能为空"}
- **THEN** 前端弹窗显示"手机号不能为空"，而非固定文案"创建失败"

### Requirement: 年/月卡列表可分页加载
系统 SHALL 提供分页接口获取所有年/月票列表。

#### Scenario: 分页获取年/月卡列表
- **WHEN** 用户访问年/月卡管理页面
- **THEN** 系统加载并显示第一页数据，支持分页切换
