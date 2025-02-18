ALTER TABLE acore_auth.account
    ADD COLUMN user_id bigint;

ALTER TABLE acore_characters.guild
    ADD COLUMN public_access boolean,
    ADD COLUMN discord       text,
    ADD COLUMN multi_faction boolean;


CREATE TABLE acore_auth.client
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



CREATE TABLE acore_characters.character_transaction
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

CREATE TABLE acore_auth.server_publications
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    img         text NOT NULL,
    title       VARCHAR(80)  NOT NULL,
    description TEXT         NOT NULL
);
