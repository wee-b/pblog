<template>
  <footer class="layout-footer">
    <!-- Geometric top border -->
    <div class="geo-top-border">
      <div class="geo-segment coral"></div>
      <div class="geo-segment gold"></div>
      <div class="geo-segment sky"></div>
      <div class="geo-segment navy"></div>
    </div>

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
        <p>© 2025 PBlog — Geometric Theme</p>
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
</script>

<style scoped>
.layout-footer {
  background-color: var(--geo-navy);
  color: #fff;
  padding: 0;
  margin-top: 60px;
  position: relative;
  overflow: hidden;
}

/* Geometric top border — colorful segments */
.geo-top-border {
  display: flex;
  width: 100%;
  height: 6px;
}

.geo-segment {
  flex: 1;
  height: 100%;
}

.geo-segment.coral { background: var(--geo-coral); }
.geo-segment.gold { background: var(--geo-gold); }
.geo-segment.sky { background: var(--geo-sky); }
.geo-segment.navy { background: var(--geo-navy-soft); }

/* Geometric decorative triangle */
.layout-footer::before {
  content: '';
  position: absolute;
  top: -60px;
  right: 10%;
  width: 0;
  height: 0;
  border-left: 120px solid transparent;
  border-right: 80px solid transparent;
  border-bottom: 100px solid rgba(240, 98, 146, 0.08);
  pointer-events: none;
}

.layout-footer::after {
  content: '';
  position: absolute;
  bottom: -40px;
  left: 5%;
  width: 0;
  height: 0;
  border-left: 80px solid transparent;
  border-right: 150px solid transparent;
  border-bottom: 120px solid rgba(79, 195, 247, 0.06);
  pointer-events: none;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 50px 20px 20px;
  position: relative;
  z-index: 1;
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
  color: var(--geo-gold);
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 700;
  position: relative;
  padding-bottom: 8px;
}

.link-column h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 3px;
  background: var(--geo-coral);
  border-radius: 2px;
}

.link-column ul {
  list-style: none;
  padding: 0;
}

.link-column li {
  margin-bottom: 10px;
}

.link-column a {
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  transition: all 0.25s;
  font-size: 14px;
}

.link-column a:hover {
  color: var(--geo-sky);
  padding-left: 6px;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

.social-icons {
  display: flex;
  gap: 15px;
}

.social-icons a {
  color: rgba(255, 255, 255, 0.6);
  font-size: 20px;
  transition: all 0.25s;
}

.social-icons a:hover {
  color: var(--geo-gold);
  transform: translateY(-2px);
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
