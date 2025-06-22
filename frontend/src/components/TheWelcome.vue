<template>
  <div class="welcome-dashboard">
    <WelcomeItem>
      <template #icon>
        <DocsIcon />
      </template>
      <template #heading>Personal Manager Plus</template>
      <strong>Personal Manager Plus</strong> — це безпечний та гнучкий інструмент для управління персоналом, який
      забезпечує звітність, координацію та контроль доступу на основі ролей у розподілених командах.
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <AdminIcon />
      </template>
      <template #heading>Роль ADMIN</template>
      Користувачі з роллю <strong>ADMIN</strong> мають повний доступ до системи. Вони можуть:
      <ul>
        <li>Керувати користувачами та призначати ролі</li>
        <li>Отримувати доступ до всіх регіонів і звітів</li>
        <li>Створювати та змінювати шаблони</li>
        <li>Переглядати адміністративну панель</li>
        <li>Експортувати будь-які звіти</li>
      </ul>
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <PowerUserIcon />
      </template>
      <template #heading>Роль POWER_USER</template>
      Користувачі з роллю <strong>POWER_USER</strong> є менеджерами на рівні регіону. Вони можуть:
      <ul>
        <li>Переглядати та подавати звіти по призначених регіонах</li>
        <li>Керувати даними груп і підрядників</li>
        <li>Генерувати та експортувати звіти по регіону</li>
      </ul>
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <UserIcon />
      </template>
      <template #heading>Роль USER</template>
      Користувачі з роллю <strong>USER</strong> мають обмежений доступ. Вони можуть:
      <ul>
        <li>Переглядати дані, що стосуються їхньої групи або завдань</li>
        <li>Експортувати звіти, доступні їм</li>
        <li>Не мають доступу до шаблонів чи адміністративних функцій</li>
      </ul>
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <GuestIcon />
      </template>
      <template #heading>Роль GUEST</template>
      Користувачі з роллю <strong>GUEST</strong> мають мінімальні права:
      <ul>
        <li>Надавати базові звіти (якщо дозволено)</li>
        <li>Переглядати лише загальнодоступні або надані дані</li>
        <li>Не мають доступу до експорту чи адміністрування</li>
      </ul>
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <SupportIcon />
      </template>
      <template #heading>Підтримка</template>
      Зверніться до свого адміністратора для зміни ролі або отримання доступу. З технічних питань — перегляньте
      <a
        href="https://github.com/galba84/WorkReportPlus"
        target="_blank"
        rel="noopener"
      >
        репозиторій GitHub
      </a>
      або скористайтеся внутрішнім каналом підтримки.
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <SupportIcon />
      </template>
      <template #heading>Джерела Даних</template>
      <strong>Довідник:</strong>
      <a
        href="https://docs.google.com/spreadsheets/d/1z78PLdhrabCpJR1fQfCW28d9FOE8B8YHvgq-aStBkss/edit?gid=2039202704"
        target="_blank"
        rel="noopener"
      >
        Google Sheets
      </a>

    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <SupportIcon />
      </template>
      <template #heading>Підтримка</template>
      <strong>БПЛА оперативний звіт:</strong>
      <a
        href="https://docs.google.com/spreadsheets/d/1YtvuAvmyZ5JZFG9uQTy3GF38oHJpQsHRAjAGOl7ioF8"
        target="_blank"
        rel="noopener"
      > звіт
      </a>
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <SupportIcon />
      </template>
      <template #heading>Підтримка</template>
      <strong>РЕБ оперативний звіт:</strong>
      <a
        href="https://docs.google.com/spreadsheets/d/1je5TCzCRfCY-1dcpFrqLk7eCVjiZABgZfzd2POMMCGA"
        target="_blank"
        rel="noopener"
      > звіт
      </a>
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <SupportIcon />
      </template>
      <template #heading>Підтримка</template>
      <strong>РЕБ оперативний звіт:</strong>
      <a
        href="https://docs.google.com/spreadsheets/d/1je5TCzCRfCY-1dcpFrqLk7eCVjiZABgZfzd2POMMCGA"
        target="_blank"
        rel="noopener"
      > звіт
      </a>
    </WelcomeItem>

  </div>
</template>

<script setup>
import WelcomeItem from './WelcomeItem.vue'
import AdminIcon from './icons/IconTooling.vue'
import PowerUserIcon from './icons/IconEcosystem.vue'
import GuestIcon from './icons/IconCommunity.vue'
import DocsIcon from './icons/IconDocumentation.vue'
import SupportIcon from './icons/IconSupport.vue'
import UserIcon from './icons/IconSupport.vue'
</script>

<style scoped>
.welcome-dashboard {
  display: grid;
  gap: 1.5rem;
  padding: 2rem;
  max-height: 80vh;
  overflow-y: auto;
}
</style>
