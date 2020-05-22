**Assist Mobile SDK**

Проект содержит SDK и пример приложения для проведения платежей через платёжный шлюз Ассист.

Реализована поддержка платежей через веб-сервис Assist https://server_name/pay/order.cfm
с отображением информации о ходе платежа в WebView и платежей через сервис https://server_name/pay/tokenpay.cfm
с использованием Google Pay.

Процесс проведения платежа контролируется экземпляром класса AssistPayEngine.
Перед началом платежа требуется установить адрес сервера для обработки платежа и
слушатель результата работы экземпляра AssistPayEngine, соответствующимим методами:
 - setServerURL()
 - setEngineListener()

При запуске платежа методом payWeb или payToken на вход подаётся информация о платеже в экземпляре класса AssistPaymentData.
Отказаться от начатого, но незавершённого платежа можно методом declineByNumber.

При оплате через веб-сервис реализована возможность ввода номера банковской карты с помощью камеры смартфона.
Для этого в проекте используется библиотека card.io.

В примере приложения представлен вариант заполенния AssistPaymentData и 
первичной инициализации AssistPaymentEngine в классе MainActivity.
В классе ConfirmationActivity представлена окончательная инциализация AssistPaymentEngine и запуск платежа.
Так же здесь представлен пример работы с кошельком Google в рамках работы с GooglePay.

**Фискализация**

Полный список полей, доступных для передачи в сервисы оплаты доступен в классе FieldName.
Среди них - поля для фискализации платежа:
- GenerateReceipt - признак формирования чека (1/0)
- ReceiptLine - наименование позиции по умолчанию
- TAX - НДС (novat/vat0/vat10/vat20/vat110/vat120)
- FPMode - способ расчёта (1-7)
- TaxationSystem - система налогообложения (0-5)

Позиции заказа можно передать в экземпляре AssistPaymentData в виде списка объектов или в виде JSON: методы setOrderItems и setOrderItemsFromJSON.
Класс AssistOrderItem содержит полный список возможных полей позиции заказа, в том числе фискальные.
С особенностями комбинаций фискальных полей AssistPaymentData и отдельных позиций можно ознакомиться в [документации Assist](https://docs.assist.ru/pages/viewpage.action?pageId=5768155).

**Поддержка GooglePay**

Для работы с GooglePay рекомендуется предварительно ознакомиться с документацией на сайте разработчика
[https://developers.google.com/pay](https://developers.google.com/pay)

На данный момент работа с кошельком Google представлена в режиме ENVIRONMENT_TEST.

**Поддержка SamsungPay**

Для использования SamsungPay в вашем приложении, вам необходимо зарегистрироваться в Samsung и зарегистрировать свое приложение, затем получить SamsungPay SDK. Смотрите http://www.samsung.com/ru/apps/mobile/samsungpay/.

Затем вам нужно создать запрос на сертификат и выпустить сертификат магазина в Samsung и передать его в assist для подключения услуги SamsungPay вашему аккаунту через [support@assist.ru](mailto:support@assist.ru).

В вашем приложении вы должны следовать инструкции Samsung для инициации платежа через SamsungPay.
Для завершения оплаты SamsungPay вам необходимо передать данные полученные из SamsungPay SDK в Assist через функцию AssistPayEngine.payToken().


Служба поддержки Ассист [support@assist.ru](mailto:support@assist.ru)