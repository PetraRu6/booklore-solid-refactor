# 📚 Booklore – SOLID рефакторинг

Това repository съдържа рефакторинг на backend частта на **Booklore**, с фокус върху **SOLID принципите**, clean architecture и по-добра тестируемост.

Основните цели на рефакторинга са:
- намаляване на coupling-а между компонентите
- разбиване на „God service“-и на по-малки и фокусирани класове
- превръщане на service-ите в orchestration слоеве
- по-лесно разширяване без промяна на съществуваща логика

---

## 🔔 NotificationService

### Какво беше променено
Първоначално `NotificationService` беше силно обвързан с WebSocket инфраструктурата и логиката за избор на потребители.

### Нови компоненти
- **UserMessageSender**  
  Абстракция за изпращане на съобщения към потребители.
- **WebSocketUserMessageSender**  
  WebSocket имплементация, използваща `SimpMessagingTemplate`.
- **PermissionUserSelector**  
  Отговаря за избора на получатели според permissions.

### Резултат
`NotificationService` вече само координира изпращането на съобщения.

**SOLID**
- SRP – разделя избора на потребители, изпращането и orchestration логиката
- DIP – service-ът зависи от абстракции
- OCP – лесно добавяне на Email / Push канали

---

## 📐 BookRuleEvaluatorService

### Проблеми преди
- Предаване на твърде много параметри
- Големи switch конструкции по оператори
- Смесени отговорности (parsing, joins, predicates)
- Bug-ове при сравнение на дати

### Нови компоненти
- **RuleCriteriaContext** – обединява Criteria API обекти
- **RuleTextUtils** – помощни методи за string и LIKE операции
- **ValueNormalizer** – нормализира стойности (дати, числа, статуси)
- **FieldExpressionResolver** – резолвва entity полета и join-ове
- **RulePredicateFactory** – избира handler според оператора
- **EqualsRuleHandler** – примерен handler за equals оператор

### Резултат
Evaluator-ът е по-малък, по-четим и лесен за разширяване без модификации.

**SOLID**
- SRP – логиката е разделена по отговорности
- OCP – нови оператори чрез нови handler-и
- DIP – evaluator-ът работи с абстракции
- ISP / LSP – малки, взаимозаменяеми handler-и

---

## 📂 FileStreamingService

### Проблеми преди
Един клас отговаряше за:
- HTTP протоколна логика
- RFC 7233 range parsing
- File I/O streaming

### Нов дизайн
- **RangeParser / Rfc7233RangeParser** – парсване на Range header
- **ByteStreamer** – абстракция за streaming
- **RandomAccessFileByteStreamer** – имплементация с файлов достъп
- **ClientDisconnectDetector** – детекция на client disconnect
- **FileStreamingService** – само orchestration

### Резултат
Streaming логиката е модулна и лесно заменяема.

**SOLID**
- SRP – една отговорност на клас
- OCP – лесно добавяне на S3 или cloud storage
- DIP – зависимост от интерфейси, не от имплементации

---

## 📊 TelemetryService

### Проблеми преди
- Над 20 repository зависимости
- Един голям `collectTelemetry()` метод
- Смесване на data access, бизнес логика и mapping

### Решение – Collector Pattern
- **TelemetryCollector<T>** – общ интерфейс
- Специализирани collector-и:
  - UserTelemetryCollector
  - BookTelemetryCollector
  - LibraryTelemetryCollector
  - Metadata / Installation collectors

### Резултат
`TelemetryService` е оркестратор, който само събира резултатите.

**SOLID**
- SRP – една група метрики на collector
- DIP – service-ът зависи от абстракции
- OCP – добавяне на нови telemetry секции без промени в ядрото

---

## 🪄 MagicShelfService

### Нови компоненти
- **MagicShelfMapper** – mapping между Entity и DTO
- **MagicShelfAuthorization** – централизирана логика за права

### Резултат
Бизнес логиката, mapping-ът и authorization логиката са напълно разделени.

**SOLID**
- SRP – всяка класа има една отговорност
- OCP – правилата за права могат да се променят независимо

---

## 📖 ReadingSessionService

### Подобрения
- **ReadingSessionContext** – централизирано извличане на userId
- **ReadingSessionMappers** – mapping на response DTO-та
- Рефакторирани return type-ове в repository-тата

### Резултат
Service логиката е по-чиста и по-лесна за тестване.

**SOLID**
- SRP – service-ът изпълнява само orchestration

---

## ✅ Обобщение

- Service-ите са orchestration слоеве, а не контейнери за логика
- Отговорностите са ясно разделени
- Кодът е по-лесен за тестване, поддръжка и разширяване
