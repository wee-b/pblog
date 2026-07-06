<template>
<!--  <div class="home-page">-->
<!--    -->
<!--  </div>-->
  <HeroSection />
  <PopularArticles />
  <FeaturedCollections />
  <StatsDashboard />
</template>

<script setup>
import HeroSection from './components/HeroSection.vue'
import PopularArticles from './components/PopularArticles.vue'
import FeaturedCollections from './components/FeaturedCollections.vue'
import StatsDashboard from './components/StatsDashboard.vue'
</script>

<style scoped>
.home-page {
  scroll-behavior: smooth;
}
</style>