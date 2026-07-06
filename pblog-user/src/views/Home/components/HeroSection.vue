<template>
  <section class="hero-section">
    <div class="hero-content">
      <h1 class="hero-title animate__animated animate__fadeInDown">欢迎来到我的博客</h1>
      <p class="hero-subtitle animate__animated animate__fadeInUp animate__delay-1s">分享技术与生活</p>
    </div>
    <div
        class="scroll-down animate__animated animate__bounce animate__infinite"
        @click="scrollToNext"
    >
      <el-icon :size="30"><arrow-down /></el-icon>
    </div>
  </section>
</template>

<script setup>
import { ArrowDown } from '@element-plus/icons-vue'
import 'animate.css'

const scrollToNext = () => {
  const nextSection = document.querySelector('.popular-articles')
  nextSection.scrollIntoView({ behavior: 'smooth' })
}
</script>

<style scoped>
.hero-section {
  height: 100vh;

  /* 这些样式实现：第一屏内容应该从视窗顶部开始，导航栏覆盖在内容上方 */
  position: relative; /* 新增 */
  z-index: 0; /* 确保在导航栏下方 */
  margin-top: calc(-1 * var(--nav-height, 60px)); /* 上移导航栏高度 */
  padding-top: var(--nav-height, 60px); /* 补偿导航栏高度 */
  box-sizing: border-box; /* 确保padding不影响总高度 */

  background: linear-gradient(rgba(240, 248, 235, 0.7), rgba(240, 248, 235, 0.7)),
  url('https://images.unsplash.com/photo-1499750310107-5fef28a66643?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80');
  background-size: cover;
  background-position: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  padding: 0 20px;
}

.hero-title {
  font-size: 3.5rem;
  color: #2c3e50;
  margin-bottom: 20px;
  font-weight: 700;
}

.hero-subtitle {
  font-size: 1.5rem;
  color: #4a6b57;
  margin-bottom: 30px;
}

.scroll-down {
  position: absolute;
  bottom: 40px;
  color: #4a6b57;
  cursor: pointer;
  transition: all 0.3s;
}

.scroll-down:hover {
  color: #67c23a;
  transform: translateY(5px);
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .hero-subtitle {
    font-size: 1.2rem;
  }
}
</style>