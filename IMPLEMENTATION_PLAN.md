# План реалізації веб-системи "Прокат автомобілів" (коротко)

2. Схема БД та JPA
- Реалізувати JPA-entity з відносинами:
  - User —< Order (1..*),
  - User —< Roles (ManyToMany),
  - Order — Car (ManyToOne),
  - Car —< Order (1..*),
  - Order — Payment (OneToOne),
  - Order — Damage (OneToMany / або OneToOne за вимогою),
- Валідації: @Email, @Size(min=8) для password, унікальність email (uniq index).

3. Репозиторії
- Spring Data JPA репозиторії для всіх сутностей + кастомні методи (доступні авто за періодом).

4. Сервіси (логіка)
- AuthService: реєстрація, логін, сесії/токени.
- UserService, CarService, OrderService, PaymentService, DamageService, AdminService.
- Правила валідації: дата початку < дата кінця, перевірка доступності авто, права доступу.

5. Контролери і маршрути
- Web MVC (Thymeleaf) або REST API + SPA:
  - /auth/login, /auth/register
  - /cars, /cars/{id}, /cars/available
  - /orders, /orders/create, /orders/{id}
  - /profile, /profile/change-password
  - /admin/** (CRUD користувачі/ролі/авто/замовлення)
  - /manager/** (фіксація пошкоджень, рахунки)
- Перенаправлення/флеш-повідомлення після дій.

6. Безпека
- Spring Security: BCryptPasswordEncoder, конфігурація ролей, захист маршрутів.
- Перевірка доступу на рівні сервісу.

7. UI (Thymeleaf)
- Layout.html, home, auth/login.html, auth/register.html, cars/list.html, cars/detail.html, orders/*, admin/*.
- Форми з client-side validation + server-side.

8. Оплата
- PaymentService: створення запису платежу, статус (PENDING/PAID/FAILED).
- Псевдо-інтеграція з платіжним шлюзом (емулятор) роботи.

9. Пошкодження та штрафи
- Менеджер створює Damage, ставить суму; Admin підтверджує/редагує.
- Генерація рахунку у Payment, сповіщення клієнта.

10. Валідація даних
- Формати email, довжина пароля (>=8), унікальність email, коректність дат, заповненість обов'язкових полів.

11. Тестування
- Unit-тести сервісів, інтеграційні тести контролерів, тести на безпеку.

12. Логи та моніторинг
- Логування важливих подій (створення/редагування замовлень, оплати, пошкодження).

13. Деплой та інструкції
- application.properties профілі, docker-compose (опціонально), README з інструкцією запуску.

# Примітки
- Реалізацію розбити на ітерації (MVP → додатковий функціонал):
  1. Базовий аутентифікація/реєстрація + CRUD авто + перегляд авто.
  2. Створення замовлення + перевірка доступності + псевдооплата.
  3. Адмін/менеджер функції + робота з ушкодженнями та звіти.
  4. Тести, документація, деплой.
