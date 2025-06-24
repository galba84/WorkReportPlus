<template>
  <div class="about">
    <img
      alt="Vue logo"
      class="logo"
      src="@/assets/Personal Manager plus schema1.jpg"
      width="600"
      height="600"
    />
  </div>
</template>

<style scoped>
/* Ensure the container fills the viewport and allows scrolling */
.about {
  max-height: 100vh;
  overflow-y: auto;
  padding: 1rem;
  padding-bottom: 3rem; /* extra space at bottom */
  box-sizing: border-box;
}

/* Center image on large screens but still allow scroll */
@media (min-width: 1024px) {
  .about {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
