# Link Tracker

Для запуска пректа необходимо:
- Произвести настройку проекта (отредактировать файлы application.yaml в scrapper и bot):
  - Указать адреса скрапера (`scrapper-url`) и бота (`bot-url`) при запуске на разных серверах
  - Указать адрес базы данных (`spring.datasource.url`)
- Задать необзодимые переменные среды
  - Для bot:
    - `TELEGRAM_TOKEN` токен бота Telegram
  - Для scrapper:
    - `GITHUB_TOKEN` токен Github
    - `SO_TOKEN_KEY` ключ Stackoverflow
    - `SO_ACCESS_TOKEN` токен Stackoverflow
    - `PG_USERNAME` логин базы данных
    - `PG_PASSWORD` парол базы данных
  - Для docker-compose:
    - `PG_USERNAME` логин базы данных
    - `PG_PASSWORD` парол базы данных

Перед запуском scrapper необходимо запустить docker-compose (`scraper/docker-compose.yaml`)

Запуск приложения: `mvn spring-boot:run` (выполнять в дериктории модуля (scrapper/bot))
