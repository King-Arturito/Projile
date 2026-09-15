--liquibase formatted sql

--changeset you:001-create-event-publication.sql

create table event_publication (
    completion_date timestamp(6) with time zone,
    publication_date timestamp(6) with time zone not null,
    id uuid not null,
    event_type varchar(255) not null,
    listener_id varchar(255) not null,
    serialized_event varchar(255) not null,
    primary key (id)
);

--ROLLBACK DROP TABLE EVENT_PUBLICATION;