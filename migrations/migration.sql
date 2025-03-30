create sequence if not exists tracked_link_seq
    increment by 50;

alter sequence tracked_link_seq owner to postgres;

create table if not exists users (
    id            bigint not null primary key,
    inactive_tags varchar(1024)
);

alter table users owner to postgres;

create table if not exists tracked_link (
    id                 bigint not null primary key,
    filters            varchar(255),
    last_update        bigint not null,
    monitoring_service varchar(255),
    service_id         varchar(255),
    tags               varchar(255),
    url                varchar(255),
    user_id            bigint
        constraint links_users_key references users
);

create index if not exists serviceId on tracked_link using btree(service_id);

alter table tracked_link owner to postgres;

