请把接口文档写在这:

# Part和BOM管理系统API接口文档

## 概述
本文档基于华为云iDME平台创建的数据实体，提供Part零件管理和BOM物料清单管理的完整API接口。

## 数据实体说明
- **Part**: 零件版本对象，支持版本控制、分类属性、扩展属性
- **PartMaster**: 零件主对象，包含业务编码和基本信息
- **PartBranch**: 零件分支对象，管理版本分支
- **BOMLink**: BOM使用关系，连接父子零件
- **BOMUsesOccurrence**: BOM具体值，包含位号等详细信息

---

## 目录
1. [Part管理接口](#part管理接口)
2. [Part版本管理接口](#part版本管理接口)  
3. [BOM管理接口](#bom管理接口)
4. [通用查询接口](#通用查询接口)

---

## Part管理接口

### 1. 创建Part
**接口地址**: `POST /parts`

**功能描述**: 创建新的Part零件，自动生成PartMaster和初始版本

**请求体**:
```json
{
  "master": {
    "name": "电阻器RES001",
    "endPart": false,
    "phantomPart": false
  },
  "version": {
    "name": "电阻器RES001",
    "description": "0.25W 1kΩ 精密电阻器",
    "source": "Buy",
    "partType": "Part",
    "classificationId": "123456",
    "clsAttrs": {
      "额定功率": "0.25W",
      "阻值": "1000Ω",
      "精度": "±1%",
      "工作温度": "-55℃~+155℃"
    },
    "iterationNote": "初始创建"
  }
}
```

**请求参数说明**:
**master对象（PartMaster属性）**:
- `name`: 零件名称，文本类型，最大长度1000（必填）
- `endPart`: 是否成品，布尔值，默认false（可选）
- `phantomPart`: 是否虚拟制造Part，布尔值，默认false（可选）

**version对象（Part属性）**:
- `name`: 零件名称，文本类型，最大长度500（必填）
- `description`: 零件描述，文本类型，最大长度1024（可选）
- `source`: 零件来源，枚举类型PartSource（必填）
- `partType`: 装配模式，枚举类型AssemblyMode（必填）
- `classificationId`: 分类ID，字符串类型（可选）
- `clsAttrs`: 分类属性，JSON类型（可选）
- `iterationNote`: 迭代备注，文本类型，最大长度4000（可选）

**响应示例**:
```json
{
  "code": 200,
  "message": "Part创建成功",
  "data": {
    "masterId": 1001,
    "versionId": 2001,
    "number": "PART20250101001",
    "name": "电阻器RES001",
    "version": "A",
    "iteration": 1,
    "workingState": "WORKING"
  }
}
```

### 2. 查询Part列表
**接口地址**: `GET /parts`

**功能描述**: 分页查询Part列表，支持多条件筛选

**请求参数**:
- `name`: 零件名称（模糊查询，可选）
- `number`: 零件编码（精确查询，可选）
- `source`: 零件来源（可选）
- `partType`: 装配模式（可选）
- `workingState`: 工作状态（可选）
- `classificationId`: 分类ID（可选）
- `page`: 页码，默认1（可选）
- `size`: 每页大小，默认20（可选）

**请求示例**: `GET /parts?name=电阻&source=Buy&page=1&size=10`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 156,
    "page": 1,
    "size": 10,
    "records": [
      {
        "masterId": 1001,
        "versionId": 2001,
        "number": "PART20250101001",
        "name": "电阻器RES001",
        "description": "0.25W 1kΩ 精密电阻器",
        "version": "A",
        "iteration": 2,
        "source": "Buy",
        "partType": "Part",
        "workingState": "RELEASED",
        "latestIteration": true,
        "latestVersion": true,
        "creator": "张三",
        "createTime": "2025-01-01 10:00:00",
        "modifier": "李四",
        "lastUpdateTime": "2025-01-02 14:30:00",
        "classificationName": "电子元器件"
      }
    ]
  }
}
```

### 3. 获取Part详情
**接口地址**: `GET /parts/{masterId}`

**功能描述**: 获取指定Part主对象的详细信息，包含分类属性

**路径参数**:
- `masterId`: Part主对象ID

**请求示例**: `GET /parts/1001`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "masterId": 1001,
    "number": "PART20250101001",
    "name": "电阻器RES001",
    "description": "0.25W 1kΩ 精密电阻器",
    "endPart": false,
    "phantomPart": false,
    "creator": "张三",
    "createTime": "2025-01-01 10:00:00",
    "currentVersion": {
      "versionId": 2001,
      "version": "A",
      "iteration": 2,
      "source": "Buy",
      "partType": "Part",
      "workingState": "RELEASED",
      "latestIteration": true,
      "workingCopy": false,
      "checkOutUserName": null,
      "iterationNote": "修正技术参数",
      "clsAttrs": {
        "额定功率": "0.25W",
        "阻值": "1000Ω",
        "精度": "±1%",
        "工作温度": "-55℃~+155℃"
      }
    },
    "classification": {
      "id": "123456",
      "name": "电子元器件",
      "businessCode": "ELE001"
    }
  }
}
```

### 4. 更新Part
**接口地址**: `PUT /parts/{masterId}`

**功能描述**: 更新Part基本信息（需要先检出）

**路径参数**:
- `masterId`: Part主对象ID

**请求体**:
```json
{
  "name": "高精度电阻器RES001",
  "description": "0.25W 1kΩ 高精度电阻器",
  "source": "Make",
  "partType": "Part",
  "clsAttrs": {
    "额定功率": "0.25W",
    "阻值": "1000Ω",
    "精度": "±0.1%",
    "工作温度": "-55℃~+155℃"
  },
  "iterationNote": "提升精度等级"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Part更新成功",
  "data": {
    "versionId": 2002,
    "iteration": 3,
    "lastUpdateTime": "2025-01-03 09:15:00"
  }
}
```

### 5. 删除Part
**接口地址**: `DELETE /parts/{masterId}`

**功能描述**: 删除指定的Part主对象及其所有版本

**路径参数**:
- `masterId`: Part主对象ID

**请求示例**: `DELETE /parts/1001`

**响应示例**:
```json
{
  "code": 200,
  "message": "Part删除成功",
  "data": null
}
```

---

## Part版本管理接口

### 1. 检出Part
**接口地址**: `POST /parts/{masterId}/checkout`

**功能描述**: 检出Part以进行编辑，创建工作副本

**路径参数**:
- `masterId`: Part主对象ID

**请求示例**: `POST /parts/1001/checkout`

**响应示例**:
```json
{
  "code": 200,
  "message": "Part检出成功",
  "data": {
    "versionId": 2003,
    "workingCopy": true,
    "checkOutUserName": "张三",
    "checkOutTime": "2025-01-03 10:00:00"
  }
}
```

### 2. 撤销检出
**接口地址**: `POST /parts/{masterId}/undo-checkout`

**功能描述**: 撤销Part的检出状态，放弃修改

**路径参数**:
- `masterId`: Part主对象ID

**请求示例**: `POST /parts/1001/undo-checkout`

**响应示例**:
```json
{
  "code": 200,
  "message": "撤销检出成功",
  "data": null
}
```

### 3. 检入Part
**接口地址**: `POST /parts/{masterId}/checkin`

**功能描述**: 检入Part，提交修改并创建新迭代

**路径参数**:
- `masterId`: Part主对象ID

**请求体**:
```json
{
  "iterationNote": "修正技术参数，提升精度"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Part检入成功",
  "data": {
    "versionId": 2004,
    "iteration": 4,
    "workingState": "WORKING",
    "checkinTime": "2025-01-03 11:30:00"
  }
}
```

### 4. 获取Part版本历史
**接口地址**: `GET /parts/{masterId}/versions`

**功能描述**: 获取Part的所有版本和迭代历史

**路径参数**:
- `masterId`: Part主对象ID

**请求示例**: `GET /parts/1001/versions`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "versionId": 2001,
      "version": "A",
      "iteration": 1,
      "workingState": "RELEASED",
      "latestIteration": false,
      "creator": "张三",
      "createTime": "2025-01-01 10:00:00",
      "iterationNote": "初始创建"
    },
    {
      "versionId": 2002,
      "version": "A", 
      "iteration": 2,
      "workingState": "RELEASED",
      "latestIteration": false,
      "creator": "张三",
      "createTime": "2025-01-02 14:30:00",
      "iterationNote": "修正技术参数"
    },
    {
      "versionId": 2004,
      "version": "A",
      "iteration": 4,
      "workingState": "WORKING",
      "latestIteration": true,
      "creator": "张三",
      "createTime": "2025-01-03 11:30:00",
      "iterationNote": "修正技术参数，提升精度"
    }
  ]
}
```

### 5. 获取指定版本详情
**接口地址**: `GET /parts/versions/{versionId}`

**功能描述**: 获取指定版本的详细信息

**路径参数**:
- `versionId`: Part版本ID

**请求示例**: `GET /parts/versions/2001`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "versionId": 2001,
    "masterId": 1001,
    "number": "PART20250101001",
    "name": "电阻器RES001",
    "version": "A",
    "iteration": 1,
    "source": "Buy",
    "partType": "Part",
    "workingState": "RELEASED",
    "iterationNote": "初始创建",
    "clsAttrs": {
      "额定功率": "0.25W",
      "阻值": "1000Ω",
      "精度": "±1%"
    },
    "creator": "张三",
    "createTime": "2025-01-01 10:00:00"
  }
}
```

---

## BOM管理接口

### 1. 创建BOM关系
**接口地址**: `POST /bom-links`

**功能描述**: 创建父子零件的BOM使用关系

**请求体**:
```json
{
  "bomLink": {
    "source": 2001,
    "target": 1002,
    "quantity": 4.0,
    "sequenceNumber": 10
  },
  "bomUsesOccurrence": {
    "referenceDesignator": "R1,R2,R3,R4"
  }
}
```

**请求参数说明**:
**bomLink对象（BOMLink关系实体属性）**:
- `source`: 源实体，参考对象指向Part版本ID（必填）
- `target`: 目标实体，参考对象指向PartMaster主对象ID（必填）
- `quantity`: 使用数量，浮点型，长度10（必填）
- `sequenceNumber`: 行号，长整型（可选，自动生成）

**bomUsesOccurrence对象（BOMUsesOccurrence实体属性）**:
- `referenceDesignator`: 位号名称，文本类型，最大长度500（可选）

**响应示例**:
```json
{
  "code": 200,
  "message": "BOM关系创建成功",
  "data": {
    "bomLinkId": 3001,
    "sourceVersionId": 2001,
    "targetMasterId": 1002,
    "quantity": 4.0,
    "sequenceNumber": 10,
    "createTime": "2025-01-03 15:00:00"
  }
}
```

### 2. 查询BOM结构树
**接口地址**: `GET /parts/{versionId}/bom-tree`

**功能描述**: 获取指定Part版本的完整BOM树状结构

**路径参数**:
- `versionId`: Part版本ID

**请求参数**:
- `levels`: 展开层级，默认无限制（可选）
- `includeQty`: 是否包含数量信息，默认true（可选）

**请求示例**: `GET /parts/2001/bom-tree?levels=3&includeQty=true`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "rootPart": {
      "versionId": 2001,
      "masterId": 1001,
      "number": "PART20250101001",
      "name": "主控板PCB001",
      "version": "A",
      "iteration": 2
    },
    "bomTree": [
      {
        "bomLinkId": 3001,
        "level": 1,
        "sequenceNumber": 10,
        "quantity": 4.0,
        "referenceDesignator": "R1,R2,R3,R4",
        "targetPart": {
          "masterId": 1002,
          "number": "PART20250101002",
          "name": "电阻器RES001",
          "currentVersion": "A"
        },
        "children": []
      },
      {
        "bomLinkId": 3002,
        "level": 1, 
        "sequenceNumber": 20,
        "quantity": 2.0,
        "referenceDesignator": "C1,C2",
        "targetPart": {
          "masterId": 1003,
          "number": "PART20250101003", 
          "name": "电容器CAP001",
          "currentVersion": "A"
        },
        "children": [
          {
            "bomLinkId": 3003,
            "level": 2,
            "sequenceNumber": 10,
            "quantity": 1.0,
            "referenceDesignator": "DI1",
            "targetPart": {
              "masterId": 1004,
              "number": "PART20250101004",
              "name": "电介质材料",
              "currentVersion": "A"
            },
            "children": []
          }
        ]
      }
    ]
  }
}
```

### 3. 查询BOM清单
**接口地址**: `GET /parts/{versionId}/bom-list`

**功能描述**: 获取指定Part版本的平面BOM清单

**路径参数**:
- `versionId`: Part版本ID

**请求参数**:
- `recursive`: 是否递归查询所有层级，默认true（可选）
- `format`: 输出格式，csv/json，默认json（可选）

**请求示例**: `GET /parts/2001/bom-list?recursive=true`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询BOM清单成功",
  "data": {
    "summary": {
      "totalItems": 6,
      "totalQuantity": 12.0,
      "maxLevel": 2
    },
    "items": [
      {
        "level": 1,
        "sequenceNumber": 10,
        "partNumber": "PART20250101002",
        "partName": "电阻器RES001",
        "quantity": 4.0,
        "unitQuantity": 4.0,
        "referenceDesignator": "R1,R2,R3,R4",
        "source": "Buy",
        "description": "0.25W 1kΩ 精密电阻器"
      },
      {
        "level": 1,
        "sequenceNumber": 20,
        "partNumber": "PART20250101003",
        "partName": "电容器CAP001", 
        "quantity": 2.0,
        "unitQuantity": 2.0,
        "referenceDesignator": "C1,C2",
        "source": "Buy",
        "description": "100uF 电解电容器"
      },
      {
        "level": 2,
        "sequenceNumber": 10,
        "partNumber": "PART20250101004",
        "partName": "电介质材料",
        "quantity": 2.0,
        "unitQuantity": 1.0,
        "referenceDesignator": "DI1",
        "source": "Make",
        "description": "高性能电介质材料"
      }
    ]
  }
}
```

### 4. 更新BOM关系
**接口地址**: `PUT /bom-links/{bomLinkId}`

**功能描述**: 更新BOM关系的数量或位号信息

**路径参数**:
- `bomLinkId`: BOM关系ID

**请求体**:
```json
{
  "bomLink": {
    "quantity": 6.0,
    "sequenceNumber": 15
  },
  "bomUsesOccurrence": {
    "referenceDesignator": "R1,R2,R3,R4,R5,R6"
  }
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "BOM关系更新成功",
  "data": {
    "bomLinkId": 3001,
    "quantity": 6.0,
    "sequenceNumber": 15,
    "lastUpdateTime": "2025-01-03 16:30:00"
  }
}
```

### 5. 删除BOM关系
**接口地址**: `DELETE /bom-links/{bomLinkId}`

**功能描述**: 删除指定的BOM使用关系

**路径参数**:
- `bomLinkId`: BOM关系ID

**请求示例**: `DELETE /bom-links/3001`

**响应示例**:
```json
{
  "code": 200,
  "message": "BOM关系删除成功",
  "data": null
}
```

### 6. 批量创建BOM关系
**接口地址**: `POST /bom-links/batch`

**功能描述**: 批量创建多个BOM关系

**请求体**:
```json
{
  "sourceVersionId": 2001,
  "bomItems": [
    {
      "targetMasterId": 1002,
      "quantity": 4.0,
      "sequenceNumber": 10,
      "referenceDesignator": "R1,R2,R3,R4"
    },
    {
      "targetMasterId": 1003,
      "quantity": 2.0,
      "sequenceNumber": 20,
      "referenceDesignator": "C1,C2"
    },
    {
      "targetMasterId": 1004,
      "quantity": 1.0,
      "sequenceNumber": 30,
      "referenceDesignator": "U1"
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "批量创建BOM关系成功",
  "data": {
    "successCount": 3,
    "failCount": 0,
    "createdBomLinks": [3001, 3002, 3003]
  }
}
```

### 7. 查询Part的父级使用情况
**接口地址**: `GET /parts/{masterId}/where-used`

**功能描述**: 查询指定Part被哪些父级Part使用

**路径参数**:
- `masterId`: Part主对象ID

**请求参数**:
- `recursive`: 是否递归查询，默认false（可选）

**请求示例**: `GET /parts/1002/where-used?recursive=true`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "bomLinkId": 3001,
      "parentPart": {
        "masterId": 1001,
        "number": "PART20250101001",
        "name": "主控板PCB001",
        "currentVersion": "A"
      },
      "quantity": 4.0,
      "referenceDesignator": "R1,R2,R3,R4",
      "level": 1
    },
    {
      "bomLinkId": 3005,
      "parentPart": {
        "masterId": 1005,
        "number": "PART20250101005",
        "name": "电路模块MOD001",
        "currentVersion": "A"
      },
      "quantity": 8.0,
      "referenceDesignator": "R10-R17",
      "level": 1
    }
  ]
}
```

---

## 通用查询接口

### 1. 获取枚举值
**接口地址**: `GET /enums/{enumType}`

**功能描述**: 获取指定枚举类型的所有可选值

**路径参数**:
- `enumType`: 枚举类型，支持：PartSource/AssemblyMode/WorkingState

**请求示例**: `GET /enums/PartSource`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "value": "Make",
      "label": "制造",
      "description": "自主制造的零件"
    },
    {
      "value": "Buy",
      "label": "购买",
      "description": "外购零件"
    },
    {
      "value": "Buy_SingleSource",
      "label": "单一供应源",
      "description": "指定供应商采购"
    }
  ]
}
```

### 2. 全局搜索
**接口地址**: `GET /search`

**功能描述**: 全局搜索Part，支持模糊匹配多个字段

**请求参数**:
- `keyword`: 搜索关键词（必填）
- `type`: 搜索类型，part/bom/all，默认all（可选）
- `page`: 页码，默认1（可选）
- `size`: 每页大小，默认20（可选）

**请求示例**: `GET /search?keyword=电阻&type=part&page=1&size=10`

**响应示例**:
```json
{
  "code": 200,
  "message": "搜索成功",
  "data": {
    "total": 25,
    "page": 1,
    "size": 10,
    "keyword": "电阻",
    "results": [
      {
        "type": "part",
        "masterId": 1002,
        "number": "PART20250101002",
        "name": "电阻器RES001",
        "description": "0.25W 1kΩ 精密电阻器",
        "matchField": "name",
        "highlight": "<em>电阻</em>器RES001"
      }
    ]
  }
}
```

---

## 错误响应格式

所有接口在发生错误时，都会返回统一的错误格式：

```json
{
  "code": 400,
  "message": "参数错误：缺少必填字段name",
  "data": null
}
```

### 常见错误码
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权访问
- `403`: 权限不足
- `404`: 资源不存在
- `409`: 资源冲突（如Part已被检出）
- `500`: 服务器内部错误

---

## 接口测试用例

### Part管理测试用例

1. **创建Part测试**
```bash
curl -X POST http://localhost:8080/parts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-jwt-token" \
  -d '{
    "master": {
      "name": "测试电阻器",
      "endPart": false,
      "phantomPart": false
    },
    "version": {
      "name": "测试电阻器",
      "description": "0.25W 1kΩ 测试用电阻器",
      "source": "Buy",
      "partType": "Part",
      "classificationId": "123456",
      "clsAttrs": {
        "额定功率": "0.25W",
        "阻值": "1000Ω",
        "精度": "±1%"
      },
      "iterationNote": "初始创建"
    }
  }'
```

2. **查询Part列表测试**
```bash
curl -X GET "http://localhost:8080/parts?name=电阻&page=1&size=10" \
  -H "Authorization: Bearer your-jwt-token"
```

3. **获取Part详情测试**
```bash
curl -X GET http://localhost:8080/parts/1001 \
  -H "Authorization: Bearer your-jwt-token"
```

### BOM管理测试用例

1. **创建BOM关系测试**
```bash
curl -X POST http://localhost:8080/bom-links \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-jwt-token" \
  -d '{
    "bomLink": {
      "source": 2001,
      "target": 1002,
      "quantity": 4.0,
      "sequenceNumber": 10
    },
    "bomUsesOccurrence": {
      "referenceDesignator": "R1,R2,R3,R4"
    }
  }'
```

2. **查询BOM结构树测试**
```bash
curl -X GET http://localhost:8080/parts/2001/bom-tree \
  -H "Authorization: Bearer your-jwt-token"
```

3. **批量创建BOM关系测试**
```bash
curl -X POST http://localhost:8080/bom-links/batch \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-jwt-token" \
  -d '{
    "sourceVersionId": 2001,
    "bomItems": [
      {
        "bomLink": {
          "target": 1002,
          "quantity": 4.0,
          "sequenceNumber": 10
        },
        "bomUsesOccurrence": {
          "referenceDesignator": "R1,R2,R3,R4"
        }
      },
      {
        "bomLink": {
          "target": 1003,
          "quantity": 2.0,
          "sequenceNumber": 20
        },
        "bomUsesOccurrence": {
          "referenceDesignator": "C1,C2"
        }
      }
    ]
  }'
```

---

## 注意事项

1. **身份认证**: 所有接口都需要携带有效的JWT Token
2. **请求头**: 必须包含`Content-Type: application/json`
3. **版本控制**: Part修改前必须先检出，修改后需要检入
4. **并发控制**: 同一个Part同时只能被一个用户检出
5. **级联删除**: 删除Part会同时删除相关的BOM关系
6. **数据完整性**: 创建BOM关系前，确保父子Part都已存在
7. **权限控制**: 根据用户角色限制操作权限
8. **审计日志**: 所有操作都会记录审计日志
9. **事务一致性**: 批量操作支持事务回滚
10. **分页查询**: 大数据量查询建议使用分页参数

---

## 业务规则

1. **Part编码规则**: 自动生成，格式为`PART{YYYYMMDD}{3位序号}`
2. **版本命名**: 大版本使用字母A-Z，小版本使用数字
3. **工作状态流转**: WORKING → RELEASED → SUPERSEDED → OBSOLETE
4. **BOM层级限制**: 建议不超过10层，避免循环引用
5. **分类约束**: Part必须关联有效的分类，继承分类属性模板
6. **数量精度**: BOM数量支持4位小数
7. **位号规范**: 建议使用标准电子设计位号命名规则

