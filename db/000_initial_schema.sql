--
-- PostgreSQL database dump
--


-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1


--
-- Name: citext; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;


--
-- Name: EXTENSION citext; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION citext IS 'data type for case-insensitive character strings';


--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: category_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.category_type AS ENUM (
    'income',
    'expense'
);


ALTER TYPE public.category_type OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: budget_categories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.budget_categories (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    budget_id uuid NOT NULL,
    name character varying(100) NOT NULL,
    description text NOT NULL,
    percentage numeric(5,2) NOT NULL,
    icon_url character varying(255),
    CONSTRAINT budget_categories_name_length_check CHECK ((char_length((name)::text) >= 3)),
    CONSTRAINT budget_categories_percentage_check CHECK (((percentage >= (0)::numeric) AND (percentage <= (100)::numeric))),
    CONSTRAINT budget_categories_description_length_check CHECK (((char_length(description) >= 10) AND (char_length(description) <= 255)))
);


ALTER TABLE public.budget_categories OWNER TO postgres;

--
-- Name: budgets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.budgets (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    finance_book_id uuid NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT budgets_description_length_check CHECK (((description IS NULL) OR ((char_length(description) >= 10) AND (char_length(description) <= 255)))),
    CONSTRAINT budgets_name_length_check CHECK ((char_length((name)::text) >= 3))
);


ALTER TABLE public.budgets OWNER TO postgres;

--
-- Name: categories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categories (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    name public.citext NOT NULL UNIQUE,
    description text,
    type public.category_type NOT NULL,
    icon_url character varying(255),
    color character varying(7) DEFAULT '#000000'::character varying,
    CONSTRAINT categories_description_length_check CHECK (((description IS NULL) OR ((char_length(description) >= 10) AND (char_length(description) <= 255)))),
    CONSTRAINT categories_name_length_check CHECK (((char_length(name) >= 3) AND (char_length(name) <= 60)))
);


ALTER TABLE public.categories OWNER TO postgres;

--
-- Name: finance_books; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.finance_books (
    id uuid DEFAULT gen_random_uuid() CONSTRAINT finance_book_id_not_null NOT NULL,
    name character varying(60) CONSTRAINT finance_book_name_not_null NOT NULL,
    description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP CONSTRAINT finance_books_created_at_not_null1 NOT NULL,
    creator_id uuid CONSTRAINT finance_book_creator_id_not_null NOT NULL,
    CONSTRAINT finance_books_description_length_check CHECK (((description IS NULL) OR ((char_length(description) >= 10) AND (char_length(description) <= 255)))),
    CONSTRAINT finance_books_name_length_check CHECK ((char_length((name)::text) >= 3))
);


ALTER TABLE public.finance_books OWNER TO postgres;

--
-- Name: goals; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.goals (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    finance_book_id uuid NOT NULL,
    name character varying(100) NOT NULL,
    description text NOT NULL,
    target_amount numeric(12,2) NOT NULL,
    current_amount numeric(12,2) DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT goals_description_length_check CHECK (((char_length(description) >= 10) AND (char_length(description) <= 255))),
    CONSTRAINT goals_name_length_check CHECK ((char_length((name)::text) >= 3))
);


ALTER TABLE public.goals OWNER TO postgres;

--
-- Name: subcategories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.subcategories (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    category_id uuid NOT NULL,
    name public.citext NOT NULL,
    description text,
    icon_url character varying(255),
    color character varying(7) DEFAULT '#000000'::character varying,
    CONSTRAINT subcategories_description_length_check CHECK ((description IS NULL OR (char_length(description) >= 30) AND (char_length(description) <= 255))),
    CONSTRAINT subcategories_name_length_check CHECK (((char_length(name) >= 3) AND (char_length(name) <= 60)))
);


ALTER TABLE public.subcategories OWNER TO postgres;

--
-- Name: tags; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tags (
    name public.citext NOT NULL UNIQUE,
    description text,
    color character varying(7),
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    CONSTRAINT tags_description_length_check CHECK ((description IS NULL OR ((char_length(description) >= 10) AND (char_length(description) <= 255)))),
    CONSTRAINT tags_name_length_check CHECK (((char_length((name)::text) >= 3) AND (char_length((name)::text) <= 50)))
);


ALTER TABLE public.tags OWNER TO postgres;

--
-- Name: transactions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transactions (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    finance_book_id uuid NOT NULL,
    amount numeric(12,2) NOT NULL,
    name character varying(60) NOT NULL,
    description text,
    subcategory_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    transaction_date timestamp without time zone NOT NULL,
    CONSTRAINT transactions_description_length_check CHECK ((description IS NULL OR ((char_length(description) >= 3) AND (char_length(description) <= 255)))),
    CONSTRAINT transactions_name_length_check CHECK ((char_length((name)::text) >= 3))
);


ALTER TABLE public.transactions OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    username public.citext NOT NULL UNIQUE,
    email public.citext NOT NULL UNIQUE,
    password_hash character varying(255) NOT NULL,
    birth_date date,
    phone_number character varying(30),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP CONSTRAINT users_created_at_not_null1 NOT NULL,
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    CONSTRAINT users_email_length_check CHECK ((char_length((email)::text) <= 60)),
    CONSTRAINT users_password_hash_length_check CHECK ((char_length((password_hash)::text) >= 50)),
    CONSTRAINT users_username_length_check CHECK (((char_length(username) >= 3) AND (char_length(username) <= 20)))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_finance_books; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users_finance_books (
    user_id uuid NOT NULL,
    finance_book_id uuid NOT NULL,
    role character varying(30) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.users_finance_books OWNER TO postgres;

--
-- Name: budget_categories budget_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.budget_categories
    ADD CONSTRAINT budget_categories_pkey PRIMARY KEY (id);


--
-- Name: budgets budgets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT budgets_pkey PRIMARY KEY (id);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- Name: finance_books finance_books_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.finance_books
    ADD CONSTRAINT finance_books_pkey PRIMARY KEY (id);


--
-- Name: goals goals_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goals
    ADD CONSTRAINT goals_pkey PRIMARY KEY (id);


--
-- Name: subcategories subcategories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subcategories
    ADD CONSTRAINT subcategories_pkey PRIMARY KEY (id);


--
-- Name: tags tags_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_pkey PRIMARY KEY (id);


--
-- Name: transactions transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_pkey PRIMARY KEY (id);


--
-- Name: users_finance_books users_finance_books_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users_finance_books
    ADD CONSTRAINT users_finance_books_pkey PRIMARY KEY (user_id, finance_book_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: subcategories subcategories_category_name_unique; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subcategories
    ADD CONSTRAINT subcategories_category_name_unique
    UNIQUE (category_id, name);


--
-- Name: idx_transactions_finance_book_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_transactions_finance_book_id
    ON public.transactions (finance_book_id);


--
-- Name: idx_transactions_subcategory_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_transactions_subcategory_id
    ON public.transactions (subcategory_id);


--
-- Name: idx_budget_categories_budget_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_budget_categories_budget_id
    ON public.budget_categories (budget_id);


--
-- Name: budget_categories budget_categories_fk_budget_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.budget_categories
    ADD CONSTRAINT budget_categories_fk_budget_id FOREIGN KEY (budget_id) REFERENCES public.budgets(id) ON DELETE CASCADE;


--
-- Name: budgets budgets_fk_finance_book_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT budgets_fk_finance_book_id FOREIGN KEY (finance_book_id) REFERENCES public.finance_books(id) ON DELETE CASCADE;


--
-- Name: finance_books finance_books_fk_creator_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.finance_books
    ADD CONSTRAINT finance_books_fk_creator_id FOREIGN KEY (creator_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: goals goals_fk_finance_book_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goals
    ADD CONSTRAINT goals_fk_finance_book_id FOREIGN KEY (finance_book_id) REFERENCES public.finance_books(id) ON DELETE CASCADE;


--
-- Name: subcategories subcategories_fk_category_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subcategories
    ADD CONSTRAINT subcategories_fk_category_id FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE CASCADE;


--
-- Name: transactions transactions_fk_finance_book_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_fk_finance_book_id FOREIGN KEY (finance_book_id) REFERENCES public.finance_books(id) ON DELETE CASCADE;


--
-- Name: transactions transactions_fk_subcategory_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_fk_subcategory_id FOREIGN KEY (subcategory_id) REFERENCES public.subcategories(id) ON DELETE SET NULL;


--
-- Name: users_finance_books users_finance_books_fk_finance_book_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users_finance_books
    ADD CONSTRAINT users_finance_books_fk_finance_book_id FOREIGN KEY (finance_book_id) REFERENCES public.finance_books(id) ON DELETE CASCADE;


--
-- Name: users_finance_books users_finance_books_fk_user_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users_finance_books
    ADD CONSTRAINT users_finance_books_fk_user_id FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--