<template>
  <footer class="layout-footer">
    <div class="footer-content">
      <div class="footer-links">
        <div class="link-column">
          <h3>关于我们</h3>
          <ul>
            <li><a href="#">关于博客</a></li>
            <li><a href="#">加入我们</a></li>
            <li><a href="#">联系方式</a></li>
          </ul>
        </div>
        <div class="link-column">
          <h3>帮助中心</h3>
          <ul>
            <li><a href="#">常见问题</a></li>
            <li><a href="#">使用指南</a></li>
            <li><a href="#">反馈建议</a></li>
          </ul>
        </div>
        <div class="link-column">
          <h3>友情链接</h3>
          <ul>
            <li><a href="#">技术论坛</a></li>
            <li><a href="#">开发者社区</a></li>
            <li><a href="#">学习资源</a></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© 2023 我的个人博客 - 使用 Vue3 和 Element Plus 构建</p>
        <div class="social-icons">
          <a href="#"><i class="el-icon-wechat"></i></a>
          <a href="#"><i class="el-icon-weibo"></i></a>
          <a href="#"><i class="el-icon-github"></i></a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
// 这里可以添加一些逻辑，如果需要的话
</script>

<style scoped>
.layout-footer {
  background-color: #1e5631;
  color: #fff;
  padding: 40px 0 20px;
  margin-top: 40px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-links {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  margin-bottom: 30px;
}

.link-column {
  flex: 1;
  min-width: 200px;
  margin-bottom: 20px;
}

.link-column h3 {
  color: #a4de02;
  margin-bottom: 15px;
  font-size: 18px;
}

.link-column ul {
  list-style: none;
  padding: 0;
}

.link-column li {
  margin-bottom: 10px;
}

.link-column a {
  color: #fff;
  text-decoration: none;
  transition: color 0.3s;
}

.link-column a:hover {
  color: #a4de02;
}

.footer-bottom {
  border-top: 1px solid #2d7d46;
  padding-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.social-icons {
  display: flex;
  gap: 15px;
}

.social-icons a {
  color: #fff;
  font-size: 20px;
  transition: color 0.3s;
}

.social-icons a:hover {
  color: #a4de02;
}

@media (max-width: 768px) {
  .footer-links {
    flex-direction: column;
  }

  .footer-bottom {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
}
</style>