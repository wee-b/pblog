<template>
  <div class="layout-container">
    <LayoutNav />
    <main class="main-content">
      <RouterView />
    </main>
    <LayoutFooter />
  </div>

</template>

<script setup>
import LayoutNav from './components/LayoutNav.vue'
import LayoutFooter from './components/LayoutFooter.vue'
import {RouterView} from "vue-router";
</script>

<style scoped>
.layout-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  /* 去除边框相关样式 */
  padding: 0;
  margin: 0 auto;
  width: 100%;
  max-width: none;
}
</style>
