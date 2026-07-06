### 核心配置项详细解释

#### 1. 索引基础配置（必配）



|        配置项        | 取值 |                             说明                             |
| :------------------: | :--: | :----------------------------------------------------------: |
|  `number_of_shards`  |  1   | 索引分片数：**单节点环境固定为 1**，多节点集群可根据数据量调整（如 3/5），分片数一旦创建无法修改 |
| `number_of_replicas` |  1   | 副本数：单节点环境设为 1（无实际副本，仅占位），生产集群建议设为 1-2（提高可用性） |
|  `refresh_interval`  |  1s  | 索引刷新间隔：默认 1 秒，即新增 / 修改数据后 1 秒可被检索到（测试环境可设为 1s，生产可设为 30s 提升性能） |

#### 2. 分词器配置（核心，适配中文检索）

这是中文检索的关键，依赖你已安装的 **IK 分词器插件**（必须提前安装，否则会报错）：

- `default`：默认分词器，使用 `ik_max_word`（细粒度分词，适合索引构建，比如 “程序员” 会拆分为 “程序、程序员、员”）；
- `default_search`：默认搜索分词器，使用 `ik_smart`（智能分词，适合搜索，比如 “程序员” 仅拆分为 “程序员”）；
- `ik_analyzer`：自定义分词器，基于 `ik_max_word` 并增加 `lowercase` 过滤器（将所有字符转为小写，实现大小写不敏感检索）；
- `tokenizer`：声明 IK 分词器的两种核心模式，供分析器调用。





```
{
  "index": {
    "number_of_shards": 1,  # 分片数：单节点=1
    "number_of_replicas": 1, # 副本数：单节点=1
    "refresh_interval": "1s", # 刷新间隔：1秒
    "analysis": {
      "analyzer": {
        "default": {
          "type": "ik_max_word", # 默认索引分词器：IK细粒度
          "use_smart": false
        },
        "default_search": {
          "type": "ik_smart", # 默认搜索分词器：IK智能
          "use_smart": true
        },
        "ik_analyzer": {
          "type": "custom", # 自定义分词器
          "tokenizer": "ik_max_word",
          "filter": ["lowercase"] # 小写过滤器
        }
      },
      "tokenizer": {
        "ik_max_word": {
          "type": "ik_max_word" # 声明IK细粒度分词器
        },
        "ik_smart": {
          "type": "ik_smart" # 声明IK智能分词器
        }
      }
    }
  }
}
```

