ALTER TABLE auth.account
    ADD COLUMN user_id bigint;

ALTER TABLE characters.guild
    ADD COLUMN public_access boolean;

ALTER TABLE characters.guild
    ADD COLUMN discord text;

ALTER TABLE characters.guild
    ADD COLUMN multi_faction boolean;


CREATE TABLE auth.client
(
    id              bigint AUTO_INCREMENT NOT NULL,
    username        varchar(50)           NOT NULL,
    password        text                  NOT NULL,
    status          boolean               NOT NULL,
    rol             varchar(50)           NOT NULL,
    jwt             text,
    refresh_token   text,
    expiration_date date,
    PRIMARY KEY (id),
    CONSTRAINT client_username_uq UNIQUE (username)
);



CREATE TABLE characters.character_transaction
(
    id               bigint auto_increment NOT NULL,
    character_id     bigint                NOT NULL,
    account_id       bigint                NOT NULL,
    user_id          bigint                NOT NULL,
    amount           bigint                NOT NULL,
    command          text,
    successful       boolean               NOT NULL,
    transaction_id   text,
    indebtedness     boolean               NOT NULL,
    transaction_date date                  NOT NULL,
    reference        varchar(50)           NOT NULL,
    status           boolean               NOT NULL,
    transaction_type varchar(60)           NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT character_transaction_reference_uq UNIQUE (reference)
);

