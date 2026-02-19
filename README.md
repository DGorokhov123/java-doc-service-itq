# DocService - Сервис управления документами

Тестовое задание для ITQ Group

## Стек

- Java 21
- Spring Boot 4.0.2
- Spring Data JPA
- Liquibase
- PostgreSQL
- Maven

## 1. Запуск базы данных

```bash
docker-compose up -d db
```

### 2. Запуск сервиса

```bash
./mvnw spring-boot:run -pl service
```
Сервис запустится на порту `8080`.

### 3. Запуск генератора документов

```bash
./mvnw spring-boot:run -pl generator
```

## Конфигурация генератора

Основные параметры в `application.yaml`:
```yaml
app:
  generator:
    service-url: http://localhost:8080        # адрес сервиса
    documents-count: 300                      # Количество документов для генерации
    batch-size: 50                            # Размер пачки для обработки
    submit-interval-ms: 5000                  # Интервал SUBMIT worker
    approve-interval-ms: 5000                 # Интервал APPROVE worker
```

## Логи

Логи показывают:
- Создание документов с номерами
- Прогресс генерации (каждые batchSize документов)
- Время выполнения каждой пачки SUBMIT/APPROVE
- Общее количество обработанных/оставшихся документов
- Ошибки с подробным описанием

Создание документов:
```
Starting document generator. Target: 100 documents, batch size: 50
Generation progress: 50/100 documents created
Generation progress: 100/100 documents created
Document generation completed in 1114 ms
Created=100, errors=0
```
Согласование документов:
```
SubmitWorker: Checking for DRAFT documents...
SubmitWorker: Processing 50 DRAFT documents (batch size: 50)
SubmitWorker: Completed in 401 ms. Processed: 50, Success: 50, Failure: 0
SubmitWorker: Total stats - Processed: 50, Success: 50, Errors: 0
...
SubmitWorker: Checking for DRAFT documents...
SubmitWorker: No DRAFT documents to process
```
Утверждение документов:
```
ApproveWorker: Checking for SUBMITTED documents...
ApproveWorker: Processing 50 SUBMITTED documents (batch size: 50)
ApproveWorker: Completed in 553 ms. Processed: 50, Success: 50, Failure: 0
ApproveWorker: Total stats - Processed: 50, Success: 50, Errors: 0
...
ApproveWorker: Checking for SUBMITTED documents...
ApproveWorker: No SUBMITTED documents to process

```

## Тесты

Запуск тестов:

```bash
./mvnw test -pl service
```

## Масштабирование до 5000+ ID в запросе

Для обработки запросов с большим количеством ID (5000+):

1. Оптимизация запросов: Использовать `IN` с разбиением на чанки по 1000 элементов
2. Временная таблица: Создавать временную таблицу с ID и делать `JOIN`
5. Пагинация: Обязательная пагинация для больших списков

## Вынос реестра утверждений в отдельную систему

1. В проекте создается еще один модуль `regitry`, с отдельной БД. Туда переносится таблица `approval_registry`.
2. В контроллере создаются REST API с описанием интерфейса в модуле `common` 
   - для регистрации документа, 
   - поиска записи реестра по id документа
   - проверки наличия записи
3. В сервисе создается Feign-клиент, наследуется, как и контроллера от интерфейса API
4. В методе утверждения документа реализуется возможность отката утверждения в случае недоступности/ошибки сервиса регистрации
5. Для управления инстансами будем использовать Spring Cloud: 
   - Eureka Discovery Service для регистрации инстансов
   - Config Server для управления конфигурациями
