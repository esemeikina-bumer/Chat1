
# Чат-приложение (Клиент-Сервер)

## Архитектура

```mermaid
graph LR
    subgraph Пользователи
        A[Helen]
        B[Petr]
        C[Irina]
    end

    subgraph Сервер
        S[ChatServer<br/>port:12345]
    end

    subgraph Хранилище
        L[file.log]
    end

    A -->|"сообщение"| S
    B -->|"сообщение"| S
    C -->|"сообщение"| S
    
    S -->|"рассылка"| A
    S -->|"рассылка"| B
    S -->|"рассылка"| C
    
    S -->|"логирование"| L
    A -->|"логирование"| L
    B -->|"логирование"| L
    C -->|"логирование"| L
```


## Общая схема взаимодействия
```mermaid
graph TD
    subgraph Сеть
        Client1[Клиент #1<br/>Helen]
        Client2[Клиент #2<br/>Petr]
        Client3[Клиент #3<br/>Ivan]
        Server[СЕРВЕР<br/>Порт:12345]
        
        Client1 <--> Server
        Client2 <--> Server
        Client3 <--> Server
    end
    
    Server --> Log[(file.log)]
    Client1 --> Log
    Client2 --> Log
    Client3 --> Log

