create table LINKS
(
    id         bigint primary key auto_increment,
    url        varchar(768) not null unique,
    status     tinyint       not null default 0,
    CREATED_AT timestamp              default now(),
    UPDATED_AT timestamp              default now()
) DEFAULT CHARSET = utf8mb4;

create table NEWS
(
    id         bigint primary key auto_increment,
    title      text,
    content    text,
    url        varchar(768),
    CREATED_AT timestamp default now(),
    UPDATED_AT timestamp default now()
) DEFAULT CHARSET = utf8mb4;
