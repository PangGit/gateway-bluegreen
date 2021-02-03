# 蓝绿发布demo

#### 1、导入configfile中文件到nacos public空间；</p>

#### 2、启动naocs；

接口调用时，给headers注入属性version，值例：v2020, 该值是nacos配置release.yaml中的二级键名；

在二级键名后配置样例说明: 服务名=版本号，英文逗号分隔， 例 consumer=v2021,provider=v2021   




