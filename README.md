# Test Fintech IQ

## Технологии

- Java 17
- Spring Boot
- PostgreSQL
- Liquibase (для миграции БД)
- Docker и Docker Compose

## Запуск приложения

### Запуск с помощью Docker (рекомендуется)

1. Клонируйте репозиторий:
   ```
   git clone <url репозитория>
   cd test-fintechiq
   ```
2. Запустите приложение с помощью Docker Compose:
   ```
   docker-compose up
   ```

Приложение будет доступно по адресу: http://localhost:8080

### Ручной запуск

1. Создайте базу данных в PostgreSQL:
   ```
   psql -U postgres -c "CREATE DATABASE postgres"
   ```

2. Настройте параметры подключения к БД в файле `src/main/resources/application.yaml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/postgres
       username: postgres
       password: 1234
   ```

3. Запустите приложение:
   ```
   ./mvnw spring-boot:run
   ```

## API endpoints

Приложение предоставляет следующие REST API endpoints:

- `POST /api/process` - Обработка одного запроса
  - Принимает JSON в теле запроса в формате:
    ```json
    {
        "loanRequestID": "0190e7b2-14a8-72e4-8528-89a8cd91d430",
        "regPerson": {
            "firstName": "Ogada",
            "middleName": "Isaac Abraham",
            "lastName": "Samuel"
        },
        "creditBureau": {
            "account_info": [
                {
                    "account_number": "25470392059420191215",
                    "account_status": "Closed",
                    "current_balance": "0.00000",
                    "date_opened": "2019-12-15",
                    "days_in_arrears": 0,
                    "delinquency_code": "003",
                    "highest_days_in_arrears": 51,
                    "is_your_account": false,
                    "last_payment_amount": "0.00000",
                    "last_payment_date": null,
                    "loaded_at": "2020-04-16",
                    "original_amount": "608.10000",
                    "overdue_balance": "0.00000",
                    "overdue_date": null,
                    "product_type_id": 7
                }
            ]
        },
        "verified_name": {
            "first_name": "ISAAC",
            "other_name": "ABRAHAM SAMUEL",
            "surname": "OGADA"
        }
    }
    ```
  - Возвращает `200 OK` с сообщением об успешной обработке

- `POST /api/process-all` - Обработка всех запросов из базы данных
  - Не требует тела запроса
  - Возвращает `200 OK` с сообщением "All requests processed successfully"

- `POST /api/calculate` - Расчет Стоп-Фактора
  - Принимает JSON в теле запроса в формате:
    ```json
    {
      "regPersonString": "Ogada Isaac Abraham Samuel",
      "verifiedNameString": "ISAAC ABRAHAM SAMUEL OGADA"
    }
    ```
  - Возвращает `200 OK` с результатом в формате:
    ```json
    {
      "stopFactor": true
    }
    ```

## Как работает алгоритм расчета Стоп-Фактора

1. Из входных строк `regPersonString` и `verifiedNameString` формируются все возможные сочетания по два слова
2. Полученные пары объединяются в строки (например, для слов А, Б, В, Г получаются комбинации: "АБ", "АВ", "АГ", "БВ", "БГ", "ВГ")
3. Для каждой пары из первого и второго массивов рассчитывается расстояние Левенштейна
4. Находится максимальное значение из всех сравнений
5. Если максимальное значение больше или равно порогу (`distanceRatioThreshold` из таблицы settings, по умолчанию 0.9), то Стоп-Фактор = true, иначе false

### Пример расчета:

Входные данные:
- `regPersonString` = "Solomon Awich"
- `verifiedNameString` = "SOLOMON RAORE AWICH"

Генерация комбинаций:
- Для regPersonString: ["Solomon Awich"]
- Для verifiedNameString: ["SOLOMON RAORE", "SOLOMON AWICH", "RAORE AWICH"]

Результаты сравнения (расстояние Левенштейна):
- "Solomon Awich" <-> "SOLOMON RAORE": 0.6
- "Solomon Awich" <-> "SOLOMON AWICH": 1.0
- "Solomon Awich" <-> "RAORE AWICH": 0.5

Максимальное значение: 1.0
Порог (distanceRatioThreshold): 0.9

Итоговый результат (Стоп-Фактор): true (так как 1.0 >= 0.9)

## Структура базы данных

При запуске приложение автоматически создает следующие таблицы:

1. `settings` - Таблица настроек
   - Содержит значение порога `distanceRatioThreshold` (по умолчанию 0.9)
   - Используется для определения порога при расчете Стоп-Фактора

2. `request_content` - Таблица с JSON запросами
   - Содержит поле типа JSON с данными о запросах кредита

3. `reg_person` - Таблица с данными о регистрационных данных лица
   - Содержит поля для хранения имени, отчества и фамилии клиента

4. `verified_name` - Таблица с данными о проверенном имени
   - Содержит информацию о проверенных данных клиента из кредитного бюро

5. `account_info` - Таблица с информацией о счетах
   - Содержит данные о счетах клиента из кредитного бюро

### Инициализация данных

При запуске приложения выполняется:

1. Парсинг данных из таблицы `request_content`:
   - Десериализация JSON-объектов в Java-объекты
   - Сохранение объектов в соответствующие таблицы

2. Расчет Стоп-Фактора для всех записей:
   - Применение алгоритма расчета расстояния Левенштейна
   - Сравнение с пороговым значением из таблицы `settings`
   - Определение результата (true/false)

## Тестирование

Приложение включает тесты, проверяющие:
1. Расчет Стоп-Фактора
2. Алгоритм расчета расстояния Левенштейна
3. Алгоритм генерации сочетаний слов

## Примеры запросов для тестирования API

### Расчет Стоп-Фактора

```bash
curl -X POST http://localhost:8080/api/calculate \
  -H "Content-Type: application/json" \
  -d '{"regPersonString": "Solomon Awich", "verifiedNameString": "SOLOMON RAORE AWICH"}'
```

Пример ответа:
```json
{
  "stopFactor": true
}
```

### Обработка одного запроса

```bash
curl -X POST http://localhost:8080/api/process \
  -H "Content-Type: application/json" \
  -d '{
    "loanRequestID": "test-id",
    "regPerson": {
        "firstName": "John",
        "lastName": "Doe"
    },
    "creditBureau": {
        "account_info": []
    },
    "verified_name": {
        "first_name": "JOHN",
        "surname": "DOE"
    }
}'
```

Пример ответа:
```
Request processed successfully
```

### Обработка всех запросов в базе данных

```bash
curl -X POST http://localhost:8080/api/process-all
```

Пример ответа:
```
All requests processed successfully
``` 