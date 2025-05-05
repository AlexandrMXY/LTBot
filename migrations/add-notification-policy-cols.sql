alter table users add column if not exists
    notification_policy varchar(64) CHECK (notification_policy IN ('INSTANT', 'DELAYED'));

alter table users add column if not exists
    notification_time   integer;
