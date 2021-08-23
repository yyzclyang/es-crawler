create table Links
(
    id         bigint primary key auto_increment,
    url        varchar(4096) not null unique,
    status     tinyint       not null default 0,
    CREATED_AT timestamp              default now(),
    UPDATED_AT timestamp              default now()
);

create table News
(
    id         bigint primary key auto_increment,
    title      text,
    content    text,
    url        varchar(4096),
    CREATED_AT timestamp default now(),
    UPDATED_AT timestamp default now()
);
