CREATE SCHEMA IF NOT EXISTS app_auth;
CREATE SCHEMA IF NOT EXISTS guest;
CREATE SCHEMA IF NOT EXISTS owner;
CREATE SCHEMA IF NOT EXISTS staff;
CREATE SCHEMA IF NOT EXISTS admin;

-- staff F&B tables (additive only, safe to run repeatedly)
CREATE TABLE IF NOT EXISTS staff.menus (
    id          BIGSERIAL PRIMARY KEY,
    property_id BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(50)  NOT NULL DEFAULT 'active'
);

CREATE TABLE IF NOT EXISTS staff.menu_categories (
    id          BIGSERIAL PRIMARY KEY,
    property_id BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS staff.menu_items (
    id           BIGSERIAL PRIMARY KEY,
    property_id  BIGINT         NOT NULL,
    menu_id      BIGINT         NOT NULL REFERENCES staff.menus(id),
    category_id  BIGINT         NOT NULL REFERENCES staff.menu_categories(id),
    name         VARCHAR(255)   NOT NULL,
    description  TEXT,
    price        NUMERIC(10, 2) NOT NULL,
    is_available BOOLEAN        DEFAULT TRUE,
    tag          VARCHAR(100),
    calories     INTEGER
);

CREATE TABLE IF NOT EXISTS staff.menu_item_images (
    menu_item_id BIGINT       NOT NULL REFERENCES staff.menu_items(id),
    image_url    VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS staff.menu_item_variants (
    menu_item_id BIGINT         NOT NULL REFERENCES staff.menu_items(id),
    label        VARCHAR(255),
    price        NUMERIC(10, 2)
);

CREATE TABLE IF NOT EXISTS staff.menu_item_modifiers (
    id           BIGSERIAL PRIMARY KEY,
    menu_item_id BIGINT       NOT NULL REFERENCES staff.menu_items(id),
    name         VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS staff.menu_item_modifier_options (
    modifier_id BIGINT         NOT NULL REFERENCES staff.menu_item_modifiers(id),
    label       VARCHAR(255),
    price       NUMERIC(10, 2)
);

CREATE TABLE IF NOT EXISTS staff.orders (
    id           BIGSERIAL PRIMARY KEY,
    property_id  BIGINT           NOT NULL,
    guest_id     BIGINT,
    table_id     BIGINT,
    table_number VARCHAR(50),
    guest_name   VARCHAR(255),
    guest_phone  VARCHAR(50),
    room_number  VARCHAR(50),
    total_amount DOUBLE PRECISION,
    status       VARCHAR(50)      NOT NULL,
    created_at   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS staff.order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT           NOT NULL REFERENCES staff.orders(id),
    menu_item_id    BIGINT           NOT NULL REFERENCES staff.menu_items(id),
    quantity        INTEGER          NOT NULL,
    price_at_order  DOUBLE PRECISION NOT NULL
);

