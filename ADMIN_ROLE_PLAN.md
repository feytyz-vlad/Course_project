# План добавления ролей администратора и менеджера

## 1. Обновление UserRole enum

Добавить новые роли:
- ADMIN
- MANAGER

Файл: `src/main/java/ua/com/kisit/course_project/Entity/UserRole.java`

## 2. Создание WebAdminController

Контроллер для обработки запросов администратора:
- Редактирование данных пользователей
- Управление ролями пользователей
- Редактирование заказов
- Работа с повреждениями
- Принятие/отклонение заявок

Файл: `src/main/java/ua/com/kisit/course_project/Controller/Web/WebAdminController.java`

Методы:
- GET /admin/users - список пользователей
- GET /admin/users/{id} - детали пользователя
- POST /admin/users/{id} - обновление данных пользователя
- POST /admin/users/{id}/role - изменение роли пользователя
- GET /admin/orders - список всех заказов
- GET /admin/orders/{id} - детали заказа
- POST /admin/orders/{id}/approve - подтверждение заказа
- POST /admin/orders/{id}/reject - отклонение заказа
- GET /admin/damages - список повреждений
- POST /admin/damages/{id}/confirm - подтверждение повреждения
- POST /admin/damages/{id}/reject - отклонение повреждения

## 3. Создание WebManagerController

Контроллер для обработки запросов менеджера:
- Фиксация повреждений
- Выставление счетов за повреждения
- Редактирование информации об автомобилях

Файл: `src/main/java/ua/com/kisit/course_project/Controller/Web/WebManagerController.java`

Методы:
- GET /manager/cars - список автомобилей
- GET /manager/cars/{id} - детали автомобиля
- POST /manager/cars/{id} - обновление информации об автомобиле
- POST /manager/cars/{id}/damage - фиксация повреждения
- POST /manager/damages/{id}/invoice - выставление счета

## 4. Обновление User entity

Добавить поле role типа UserRole.

Файл: `src/main/java/ua/com/kisit/course_project/Entity/User.java`

## 5. Обновление шаблонов

Добавить ссылки на админку и менеджмент в навигационное меню.

Файл: `src/main/resources/templates/layout.html`

## 6. Добавление security

Защитить эндпоинты по ролям с помощью Spring Security.

Файл: `src/main/java/ua/com/kisit/course_project/SecurityConfig.java` (если есть)