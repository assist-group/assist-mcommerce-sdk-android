**Assist Mobile SDK**

Проект содержит SDK и пример приложения для проведения платежей через платёжный шлюз Ассист.

Реализована поддержка платежей через веб-сервис Assist https://server_name/pay/order.cfm
с отображением информации о ходе платежа в WebView и платежей через сервис https://server_name/pay/tokenpay.cfm
с использованием Google Pay.

Процесс проведения платежа контролируется экземпляром класса AssistPayEngine.
Перед началом платежа требуется установить адрес сервера для обработки платежа и
слушатель результата работы экземпляра AssistPayEngine, соответствующими методами:
 - setServerURL()
 - setEngineListener()

При запуске платежа методом payWeb или payToken на вход подаётся информация о платеже в экземпляре класса AssistPaymentData.
Отказаться от начатого, но незавершённого платежа можно методом declineByNumber.

При оплате через веб-сервис реализована возможность ввода номера банковской карты с помощью камеры смартфона.
Для этого в проекте используется библиотека card.io.

В примере приложения представлен вариант заполнения AssistPaymentData и 
первичной инициализации AssistPaymentEngine в классе MainActivity.
В классе ConfirmationActivity представлена окончательная инциализация AssistPaymentEngine и запуск платежа.
Также здесь представлен пример работы с кошельком Google в рамках работы с GooglePay.

**Фискализация**

Полный список полей, доступных для передачи в сервисы оплаты, предоставлен в классе FieldName.
Среди них - поля для фискализации платежа:
- GenerateReceipt - признак формирования чека (1/0)
- ReceiptLine - наименование позиции по умолчанию
- TAX - НДС (novat/vat0/vat10/vat20/vat110/vat120)
- FPMode - способ расчёта (1-7)
- TaxationSystem - система налогообложения (0-5)

Позиции заказа можно передать в экземпляре AssistPaymentData в виде списка объектов или в виде JSON: методы setOrderItems и setOrderItemsFromJSON.
Класс AssistOrderItem содержит полный список возможных полей позиции заказа, в том числе фискальные.
С особенностями комбинаций фискальных полей AssistPaymentData и отдельных позиций можно ознакомиться в [документации Assist](https://docs.assist.ru/pages/viewpage.action?pageId=5768155).

**Поддержка Google Pay**

Для работы с Google Pay рекомендуется предварительно ознакомиться с документацией на сайте разработчика
[https://developers.google.com/pay](https://developers.google.com/pay).

На данный момент работа с кошельком Google представлена в режиме ENVIRONMENT_TEST.

**Поддержка Samsung Pay**

Для использования SamsungPay в вашем приложении, вам необходимо зарегистрироваться в Samsung и зарегистрировать свое приложение, затем получить Samsung Pay SDK. Смотрите http://www.samsung.com/ru/apps/mobile/samsungpay/.

Затем вам нужно создать запрос на сертификат, выпустить сертификат магазина в Samsung и передать его в assist для подключения услуги Samsung Pay вашему аккаунту через [support@assist.ru](mailto:support@assist.ru).

В вашем приложении вы должны следовать инструкции Samsung для инициации платежа через Samsung Pay.
Для завершения оплаты Samsung Pay вам необходимо передать данные полученные из Samsung Pay SDK в Assist через функцию AssistPayEngine.payToken().

**Оплата созданного заказа**

Если вы создаёте заказ сервисом https://server_name/pay/payrequest.cfm, то его можно оплатить, используя только имеющиеся orderNumber и link созданного заказа.
- Для оплаты через веб-сервис необходимо заполнить соответствующие поля в AssistPaymentData и вызвать метод payWeb.
- Для оплаты Google (или Samsung) Pay необходимо сначала получить токен, для этого вам надо знать сумму и валюту. Для этого можно воспользоваться getAmountForOrder. Полученный токен вместе с полями orderNumber и link необходимо передать в payToken().

Примеры работы указанных сценариев находятся в классе ConfirmationActivity.

По всем вопросам и багам обращайтесь в поддержку.
Служба поддержки Ассист [support@assist.ru](mailto:support@assist.ru)

**Assist Mobile SDK**

The project contains an SDK and an app example for making payments through the Assist payment gateway.

Implemented support for payments through the Assist web service https://server_name/pay/order.cfm with displaying information about the payment progress in WebView and payments through the service https://server_name/pay/tokenpay.cfm using Google Pay.

The payment process is controlled by an instance of the AssistPayEngine class. Before starting the payment, you need to set the address of the server for processing the payment and the listener for the result of the operation of the AssistPayEngine instance, using the appropriate methods:
 - setServerURL()
 - setEngineListener()

When starting a payment using the payWeb or payToken method, information about the payment is sent as input in an instance of the AssistPaymentData class. You can cancel a started but incomplete payment using the declineByNumber method.

When paying through a web service, it is possible to enter a bank card number using a smartphone camera. To do this, the project uses the card.io library.

The sample app presents a variant to fill the AssistPaymentData and initialize the AssistPaymentEngine in the MainActivity class.
The ConfirmationActivity class represents the final initialization of the AssistPaymentEngine and the start of the payment. Also here is an example of working with a Google wallet as part of working with GooglePay.

**Fiscalization**

The full list of fields for transfer to payment services is available in the FieldName class. Among them are the fields for fiscalization of payment:
 - GenerateReceipt - cheque generation flag (1/0)
 - ReceiptLine - default item name
 - Tax - VAT (novat/vat0/vat10/vat20/vat110/vat120)
 - FPMode - payment method (1-7)
 - TaxationSystem - taxation system (0-5)

Order items can be transfered in the AssistPaymentData instance as a list of objects or as JSON: setOrderItems and setOrderItemsFromJSON methods. The AssistOrderItem class contains a complete list of possible order item fields, including fiscal ones. You can find the features of AssistPaymentData fiscal fields combinations and line items in the [Assist documentation](https://docs.assist.ru/pages/viewpage.action?pageId=5768155).

**Google Pay Support**

To work with Google Pay, it is recommended that you first read the documentation on the developer's website
[https://developers.google.com/pay](https://developers.google.com/pay).

At the moment, work with the Google wallet is presented in the ENVIRONMENT_TEST mode.

**Samsung Pay support**

To use Samsung Pay in their app, a merchant needs to sign up with Samsung and register their app, then get the Samsung Pay SDK http://www.samsung.com/ru/apps/mobile/samsungpay/.

Next, you should create a certificate request, issue a merchant certificate in Samsung and transfer it to Assist via [support@assist.ru](mailto:support@assist.ru) to connect the Samsung Pay service.

In your app, you need to follow Samsung's instructions to initiate a payment with Samsung Pay.
To complete the Samsung Pay payment, you need to transfer the data received from the Samsung Pay SDK to Assist using the AssistPayEngine.payToken() function.

**Payment for the created order**

If you create an order using the https://server_name/pay/payrequest.cfm service, then you can pay for it using only the existing orderNumber and link of the created order.
 - to pay via a web service, you must fill in the appropriate fields in AssistPaymentData and call the payWeb method;
 - to pay Google Pay (or Samsung Pay), you must first obtain a token, for this you need to know the amount and currency; to do this, you can use getAmountForOrder; the received token, together with the orderNumber and link fields, must be transfered to payToken().

Examples of these scripts working are in the ConfirmationActivity class.

For all questions and bugs, please contact support service Assist [support@assist.ru](mailto:support@assist.ru).
