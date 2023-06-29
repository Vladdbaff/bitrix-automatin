# Мини-консольное приложение на скорую руку для автоматизации работы с CRM-bitrix)
## Тех. задание

Каждый месяц отдел продаж формирует файл-таблицу с hash-строками(emailhash),
по каторым можно найти lead-сущность в bitrix(это заявка клиента). Задача - 
пройти по всем emailhash, и найти информацию по клиенту по следующему приоритерту:

1) Если в bitrix у клиента есть deal-сущность(это сделка с клиентом), то нужно
вернуть данные только по этой lead-сущности
2) Если у клиента нет deal-сущнсти, в таком случае нужно найти любой lead клиента,
и если у данного lead поле **STATUS_ID != 27**, вернуть данный lead
3) Если у клиента есть только lead-сущности со статусом 27, в таком случае нужно 
вернуть этот статус(этот статус говорит о том, что данная lead является дубликатом)


## Реализация 

1) Первым делом открываем Scanner, который будет читать из консоли emailhash. 
Так же открываем HttpClient, при помощи этого обьека будем делать запросы в bitrix
2) Далее в цикле wile при помощи Scanner читаем по одному emailhash.
3) По полученному emailhash формируем http запрос на получение lead-сущности. 

**Пометка** - сразу получить deal сущность по emailhash нельзя, для этого нужно 
делать отдельный запрос с полем id полученной lead
4) Так как http-запрос может вернуть более одной лиды, нужно проверить **STATUS_ID**
у каждой лиды, и если этот **STATUS_ID = CONVERTED**, в таком случае у клиента уже 
есть сделка, и можно выдергивать из лиды id и делать запрос на получение deal-сущности 
5) После получения нужной сущности, из нее выдергиваются данные данные и помещаются 
в список **rows**. Этот список хранит данные в виде строк, которые будут помещены в
 таблицу в итоговый .csv файл 
6) После обработки всех emailhash вызывается метод **writeToCSV** который сгенерирует 
файл, и запишет туда все полученные данные в виде таблицы