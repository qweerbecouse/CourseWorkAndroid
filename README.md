# 📁 Coursework — Приложение для доступа к облачным хранилищам данных с поддержкой мультиаккаунта на Android (Яндекс.Диск)

## 📱 Описание

**Coursework** — Android-приложение, которое позволяет:
- подключать и переключаться между несколькими аккаунтами Яндекс.Диска;
- скачивать, просматривать и открывать файлы;
- загружать файлы с устройства;
- создавать текстовые документы, таблицы, презентации и папки;
- переименовывать, перемещать и удалять файлы;
- синхронизировать содержимое;
- использовать тёмную/светлую тему.

---

## 🧱 Технологии

- **Язык:** Kotlin
- **Архитектура:** MVVM
- **UI:** Jetpack Compose + Material 3
- **Сетевой стек:** Retrofit + OkHttp
- **Хранилище:** Room, DataStore
- **Фоновые задачи:** SwipeRefresh
- **OAuth:** встроенная авторизация Яндекс

---

## 🗂 Возможности

- 📂 Многоаккаунтность (смена аккаунта в 1 клик)
- 📝 Создание шаблонных файлов из `assets/templates`
- ☁ Загрузка с устройства
- 📥 Скачивание, загрузка и открытие файлов через FileProvider
- 🔃 Pull-to-refresh
- 🌙 Поддержка темной темы (вручную)
- ➕ Переименование, удаление, перемещение файлов
- 📊 Сортировка (по имени, размеру, типу, дате изменения)


---

## 📂 Структура проекта

CourseWork  
├── **.gradle/**  
├── **.idea/**  
├── **app/**  
│   ├── **build/**  
│   └── **src/**  
│       ├── **androidTest/**  
│       ├── **main/**  
│       │   ├── **assets/**  
│       │   ├── **java/com/example/coursework/**  
│       │   │   ├── **data/**  
│       │   │   │   ├── **local/**  
│       │   │   │   │   ├── **dao/**  
│       │   │   │   │   │   └── DiskFileDao.kt  
│       │   │   │   │   ├── **model/**  
│       │   │   │   │   │   └── DiskFileEntity.kt  
│       │   │   │   │   └── AppDatabase.kt  
│       │   │   │   ├── **remote/**  
│       │   │   │   │   ├── **model/**  
│       │   │   │   │   │   ├── DiskResourceResponse.kt  
│       │   │   │   │   │   ├── DownloadResponse.kt  
│       │   │   │   │   │   ├── UploadHrefResponse.kt  
│       │   │   │   │   │   └── YandexUserInfo.kt  
│       │   │   │   │   ├── ApiClient.kt  
│       │   │   │   │   ├── YandexDiskApiService.kt  
│       │   │   │   │   └── YandexUserInfoApi.kt  
│       │   │   │   ├── **repository/**  
│       │   │   │   │   └── FilesRepository.kt  
│       │   │   │   └── **storage/**  
│       │   │   │       └── TokenDataStore.kt  
│       │   │   └── **presentation/**  
│       │   │       ├── **auth/**  
│       │   │       │   └── AuthScreen.kt  
│       │   │       ├── **files/**  
│       │   │       │   ├── FilesScreen.kt  
│       │   │       │   ├── FilesViewModel.kt  
│       │   │       │   └── FilesViewModelFactory.kt  
│       │   │       ├── **main/navigation/**  
│       │   │       │   └── MainActivity.kt  
│       │   │       └── **ui/theme/**  
│       │   │           ├── Theme.kt  
│       │   │           └── App.kt  
│       │   └── **res/**  
│       │       └── AndroidManifest.xml  
│       └── **test/**  
├── **.gitignore**  
└── **settings.gradle.kts**

---

## 🔑 Авторизация

1. При первом запуске открывается OAuth-страница Яндекса.
2. После успешного входа токен сохраняется в DataStore.
3. Имя пользователя подтягивается с `https://login.yandex.ru/info`.

---

## 💽 Сборка и запуск

### 1. Сборка `.apk`

1. Создайте keystore (один раз):
   ```bash
   keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mykey
   ```
2. Перейдите в **Build → Generate Signed Bundle / APK**
3. Выберите путь к `keystore.jks`, введите пароль, alias и key password
4. Нажмите **Next**, затем **Finish**
5. Файл `.apk` появится в:  
   `CourseWork/app/release/app-release.apk`
