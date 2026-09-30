# DMB. — сборка APK с телефона через GitHub

## Что делать
1. Создай GitHub repository, например `DMB`.
2. Загрузи в него **содержимое этой папки**, включая `.github/workflows/build-apk.yml`.
3. Открой вкладку **Actions**.
4. Выбери workflow **Build DMB APK**.
5. Нажми **Run workflow**.
6. Дождись зелёной галочки.
7. Открой завершённый запуск workflow.
8. Внизу страницы найди **Artifacts** → `DMB-apk`.
9. Скачай ZIP artifact на телефон.
10. Распакуй его — внутри будет `app-debug.apk`.
11. Установи APK.

Workflow автоматически устанавливает JDK 17, Android SDK 35 и Gradle 8.9 на сервере GitHub.
