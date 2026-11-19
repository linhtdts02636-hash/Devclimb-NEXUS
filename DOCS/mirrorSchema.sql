--
-- PostgreSQL database dump
--

\restrict lQN9Ph8SvWKT99o4bw96NndIJnIo4k0dti4TUgOLfr46LV8QYIyeiIHY36rOI7h

-- Dumped from database version 18.7
-- Dumped by pg_dump version 18.7

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: anno_type_enum; Type: TYPE; Schema: public; Owner: avnadmin
--

CREATE TYPE public.anno_type_enum AS ENUM (
    'GENERAL',
    'MAINTENANCE',
    'RELEASE',
    'FEATURE',
    'BUG_FIX',
    'SECURITY',
    'INCIDENT',
    'EVENT'
);


ALTER TYPE public.anno_type_enum OWNER TO avnadmin;

--
-- Name: appe_status_enum; Type: TYPE; Schema: public; Owner: avnadmin
--

CREATE TYPE public.appe_status_enum AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED'
);


ALTER TYPE public.appe_status_enum OWNER TO avnadmin;

--
-- Name: atta_type_enum; Type: TYPE; Schema: public; Owner: avnadmin
--

CREATE TYPE public.atta_type_enum AS ENUM (
    'IMAGE',
    'FILE',
    'VIDEO',
    'GIF'
);


ALTER TYPE public.atta_type_enum OWNER TO avnadmin;

--
-- Name: puzz_difficulty_enum; Type: TYPE; Schema: public; Owner: avnadmin
--

CREATE TYPE public.puzz_difficulty_enum AS ENUM (
    'EASY',
    'MEDIUM',
    'HARD'
);


ALTER TYPE public.puzz_difficulty_enum OWNER TO avnadmin;

--
-- Name: reac_type_enum; Type: TYPE; Schema: public; Owner: avnadmin
--

CREATE TYPE public.reac_type_enum AS ENUM (
    'LIKE',
    'DISLIKE'
);


ALTER TYPE public.reac_type_enum OWNER TO avnadmin;

--
-- Name: repo_status_enum; Type: TYPE; Schema: public; Owner: avnadmin
--

CREATE TYPE public.repo_status_enum AS ENUM (
    'PENDING',
    'RESOLVED',
    'DISMISSED'
);


ALTER TYPE public.repo_status_enum OWNER TO avnadmin;

--
-- Name: subm_status_enum; Type: TYPE; Schema: public; Owner: avnadmin
--

CREATE TYPE public.subm_status_enum AS ENUM (
    'PENDING',
    'ACCEPTED',
    'WRONG_ANSWER',
    'TIME_LIMIT',
    'RUNTIME_ERROR',
    'COMPILE_ERROR'
);


ALTER TYPE public.subm_status_enum OWNER TO avnadmin;

--
-- Name: violation_type_enum; Type: TYPE; Schema: public; Owner: avnadmin
--

CREATE TYPE public.violation_type_enum AS ENUM (
    'SPAM',
    'HARASSMENT_OR_BULLYING',
    'HATE_SPEECH_OR_DISCRIMINATION',
    'THREATS_OR_VIOLENCE',
    'INAPPROPRIATE_CONTENT',
    'FRAUD_OR_PHISHING',
    'MALICIOUS_OR_HARMFUL_CONTENT',
    'OTHER_VIOLATIONS'
);


ALTER TYPE public.violation_type_enum OWNER TO avnadmin;

--
-- Name: warn_set_enddate(); Type: FUNCTION; Schema: public; Owner: avnadmin
--

CREATE FUNCTION public.warn_set_enddate() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF NEW.warn_enddate IS NULL THEN
    NEW.warn_enddate := NEW.warn_startdate + INTERVAL '3 months';
  END IF;
  RETURN NEW;
END;
$$;


ALTER FUNCTION public.warn_set_enddate() OWNER TO avnadmin;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: achievement; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.achievement (
    achi_id integer NOT NULL,
    cont_id integer NOT NULL,
    achi_name character varying(250) NOT NULL,
    achi_content text NOT NULL,
    achi_image text
);


ALTER TABLE public.achievement OWNER TO avnadmin;

--
-- Name: achievement_achi_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.achievement ALTER COLUMN achi_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.achievement_achi_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: announcement; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.announcement (
    anno_id integer NOT NULL,
    user_id integer NOT NULL,
    anno_title character varying(255) NOT NULL,
    anno_content text NOT NULL,
    anno_type public.anno_type_enum,
    anno_ispinned boolean DEFAULT false NOT NULL,
    anno_ispublished boolean DEFAULT true NOT NULL,
    anno_createdat timestamp with time zone DEFAULT now() NOT NULL,
    anno_updatedat timestamp with time zone
);


ALTER TABLE public.announcement OWNER TO avnadmin;

--
-- Name: announcement_anno_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.announcement ALTER COLUMN anno_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.announcement_anno_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: appeal; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.appeal (
    appe_id integer NOT NULL,
    warn_id integer NOT NULL,
    user_id integer NOT NULL,
    appe_content text NOT NULL,
    appe_status public.appe_status_enum DEFAULT 'PENDING'::public.appe_status_enum NOT NULL,
    appe_date timestamp with time zone DEFAULT now() NOT NULL,
    appe_reviewedat timestamp with time zone
);


ALTER TABLE public.appeal OWNER TO avnadmin;

--
-- Name: appeal_appe_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.appeal ALTER COLUMN appe_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.appeal_appe_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: attachment; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.attachment (
    atta_id integer NOT NULL,
    post_id integer NOT NULL,
    user_id integer NOT NULL,
    atta_url text NOT NULL,
    atta_type public.atta_type_enum NOT NULL,
    atta_createdat timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.attachment OWNER TO avnadmin;

--
-- Name: attachment_atta_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.attachment ALTER COLUMN atta_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.attachment_atta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: community; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.community (
    comm_id integer NOT NULL,
    comm_name character varying(50) NOT NULL,
    comm_image text,
    comm_banner text,
    comm_shoutout text,
    comm_ispublic boolean DEFAULT true NOT NULL,
    comm_isactive boolean DEFAULT true NOT NULL,
    comm_createdat timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.community OWNER TO avnadmin;

--
-- Name: community_comm_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.community ALTER COLUMN comm_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.community_comm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: contest; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.contest (
    cont_id integer NOT NULL,
    comm_id integer NOT NULL,
    cont_title character varying(250) NOT NULL,
    cont_content text NOT NULL,
    cont_image text,
    cont_banner text,
    cont_start timestamp with time zone DEFAULT now() NOT NULL,
    cont_end timestamp with time zone NOT NULL,
    CONSTRAINT contest_check CHECK ((cont_end > cont_start))
);


ALTER TABLE public.contest OWNER TO avnadmin;

--
-- Name: contest_cont_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.contest ALTER COLUMN cont_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.contest_cont_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: ctag; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.ctag (
    ctag_id integer NOT NULL,
    ctag_name character varying(50) NOT NULL
);


ALTER TABLE public.ctag OWNER TO avnadmin;

--
-- Name: ctag_community; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.ctag_community (
    ctag_id integer NOT NULL,
    comm_id integer NOT NULL
);


ALTER TABLE public.ctag_community OWNER TO avnadmin;

--
-- Name: ctag_ctag_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.ctag ALTER COLUMN ctag_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.ctag_ctag_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: follow; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.follow (
    foll_followerid integer NOT NULL,
    foll_followeeid integer NOT NULL,
    foll_createdat timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT follow_check CHECK ((foll_followerid <> foll_followeeid))
);


ALTER TABLE public.follow OWNER TO avnadmin;

--
-- Name: language; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.language (
    lang_id integer NOT NULL,
    lang_judge0 integer NOT NULL,
    lang_name character varying(20) NOT NULL
);


ALTER TABLE public.language OWNER TO avnadmin;

--
-- Name: language_lang_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.language ALTER COLUMN lang_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.language_lang_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: maintenance; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.maintenance (
    main_id integer NOT NULL,
    user_id integer NOT NULL,
    main_enabled boolean DEFAULT false NOT NULL,
    main_enabledat timestamp with time zone
);


ALTER TABLE public.maintenance OWNER TO avnadmin;

--
-- Name: maintenance_main_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.maintenance ALTER COLUMN main_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.maintenance_main_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: notification; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.notification (
    noti_id integer NOT NULL,
    noti_userid integer NOT NULL,
    noti_actorid integer,
    noti_content text NOT NULL,
    noti_isread boolean DEFAULT false NOT NULL,
    noti_createdat timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.notification OWNER TO avnadmin;

--
-- Name: notification_noti_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.notification ALTER COLUMN noti_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.notification_noti_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: permission; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.permission (
    perm_id integer NOT NULL,
    role_id integer NOT NULL,
    perm_name character varying(100) NOT NULL
);


ALTER TABLE public.permission OWNER TO avnadmin;

--
-- Name: permission_perm_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.permission ALTER COLUMN perm_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.permission_perm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: post; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.post (
    post_id integer NOT NULL,
    user_id integer NOT NULL,
    comm_id integer NOT NULL,
    post_parentid integer,
    post_title character varying(255),
    post_content text NOT NULL,
    post_createdat timestamp with time zone DEFAULT now() NOT NULL,
    post_updatedat timestamp with time zone,
    CONSTRAINT post_check CHECK (((post_parentid IS NULL) OR (post_parentid <> post_id)))
);


ALTER TABLE public.post OWNER TO avnadmin;

--
-- Name: post_post_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.post ALTER COLUMN post_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.post_post_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: ptag; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.ptag (
    ptag_id integer NOT NULL,
    ptag_name character varying(50) NOT NULL
);


ALTER TABLE public.ptag OWNER TO avnadmin;

--
-- Name: ptag_ptag_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.ptag ALTER COLUMN ptag_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.ptag_ptag_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: ptag_puzzle; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.ptag_puzzle (
    ptag_id integer NOT NULL,
    puzz_id integer NOT NULL
);


ALTER TABLE public.ptag_puzzle OWNER TO avnadmin;

--
-- Name: puzzle; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.puzzle (
    puzz_id integer NOT NULL,
    user_id integer NOT NULL,
    comm_id integer NOT NULL,
    puzz_title text,
    puzz_content text NOT NULL,
    puzz_createdat timestamp with time zone DEFAULT now() NOT NULL,
    puzz_difficulty public.puzz_difficulty_enum NOT NULL
);


ALTER TABLE public.puzzle OWNER TO avnadmin;

--
-- Name: puzzle_contest; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.puzzle_contest (
    puzz_id integer NOT NULL,
    cont_id integer NOT NULL
);


ALTER TABLE public.puzzle_contest OWNER TO avnadmin;

--
-- Name: puzzle_language; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.puzzle_language (
    puzz_id integer NOT NULL,
    lang_id integer NOT NULL,
    pula_functionheader text NOT NULL,
    CONSTRAINT chk_pula_functionheader_notblank CHECK ((btrim(pula_functionheader) <> ''::text))
);


ALTER TABLE public.puzzle_language OWNER TO avnadmin;

--
-- Name: puzzle_puzz_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.puzzle ALTER COLUMN puzz_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.puzzle_puzz_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: reaction; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.reaction (
    post_id integer NOT NULL,
    user_id integer NOT NULL,
    reac_type public.reac_type_enum NOT NULL,
    reac_createdat timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.reaction OWNER TO avnadmin;

--
-- Name: report; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.report (
    repo_id integer NOT NULL,
    user_id integer NOT NULL,
    post_id integer NOT NULL,
    repo_type public.violation_type_enum NOT NULL,
    repo_reason text,
    repo_status public.repo_status_enum DEFAULT 'PENDING'::public.repo_status_enum NOT NULL,
    repo_date timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.report OWNER TO avnadmin;

--
-- Name: report_repo_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.report ALTER COLUMN repo_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.report_repo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: role; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.role (
    role_id integer NOT NULL,
    role_name character varying(50) NOT NULL
);


ALTER TABLE public.role OWNER TO avnadmin;

--
-- Name: role_role_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.role ALTER COLUMN role_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.role_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: submission; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.submission (
    subm_id integer NOT NULL,
    puzz_id integer NOT NULL,
    user_id integer NOT NULL,
    subm_code text,
    subm_status public.subm_status_enum DEFAULT 'PENDING'::public.subm_status_enum NOT NULL,
    subm_time integer,
    subm_date timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.submission OWNER TO avnadmin;

--
-- Name: submission_subm_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.submission ALTER COLUMN subm_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.submission_subm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: testcase; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.testcase (
    test_id integer NOT NULL,
    puzz_id integer NOT NULL,
    test_input text NOT NULL,
    test_output text NOT NULL
);


ALTER TABLE public.testcase OWNER TO avnadmin;

--
-- Name: testcase_test_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.testcase ALTER COLUMN test_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.testcase_test_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_achievement; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.user_achievement (
    user_id integer NOT NULL,
    achi_id integer NOT NULL,
    uach_awardedat timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.user_achievement OWNER TO avnadmin;

--
-- Name: user_community; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.user_community (
    user_id integer NOT NULL,
    comm_id integer NOT NULL,
    ucom_joinedat timestamp with time zone DEFAULT now() NOT NULL,
    ucom_isowner boolean DEFAULT false NOT NULL
);


ALTER TABLE public.user_community OWNER TO avnadmin;

--
-- Name: user_community_role; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.user_community_role (
    user_id integer NOT NULL,
    comm_id integer NOT NULL,
    role_id integer NOT NULL,
    ucrl_assignedat timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.user_community_role OWNER TO avnadmin;

--
-- Name: user_contest; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.user_contest (
    user_id integer NOT NULL,
    cont_id integer NOT NULL,
    ucon_joinedat timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.user_contest OWNER TO avnadmin;

--
-- Name: users; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.users (
    user_id integer NOT NULL,
    user_email character varying(50) NOT NULL,
    user_name character varying(50) NOT NULL,
    user_firebaseuid character varying(300) NOT NULL,
    user_isadmin boolean DEFAULT false NOT NULL,
    user_image text,
    user_bio text,
    user_isactive boolean DEFAULT true NOT NULL,
    user_createdat timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.users OWNER TO avnadmin;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.users ALTER COLUMN user_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: warn; Type: TABLE; Schema: public; Owner: avnadmin
--

CREATE TABLE public.warn (
    warn_id integer NOT NULL,
    comm_id integer NOT NULL,
    warn_warnerid integer NOT NULL,
    warn_warneeid integer NOT NULL,
    warn_type public.violation_type_enum NOT NULL,
    warn_reason text NOT NULL,
    warn_isactive boolean DEFAULT true NOT NULL,
    warn_startdate timestamp with time zone DEFAULT now() NOT NULL,
    warn_enddate timestamp with time zone,
    CONSTRAINT warn_check CHECK (((warn_enddate IS NULL) OR (warn_enddate > warn_startdate)))
);


ALTER TABLE public.warn OWNER TO avnadmin;

--
-- Name: warn_warn_id_seq; Type: SEQUENCE; Schema: public; Owner: avnadmin
--

ALTER TABLE public.warn ALTER COLUMN warn_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.warn_warn_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: achievement achievement_cont_id_key; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.achievement
    ADD CONSTRAINT achievement_cont_id_key UNIQUE (cont_id);


--
-- Name: achievement achievement_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.achievement
    ADD CONSTRAINT achievement_pkey PRIMARY KEY (achi_id);


--
-- Name: announcement announcement_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.announcement
    ADD CONSTRAINT announcement_pkey PRIMARY KEY (anno_id);


--
-- Name: appeal appeal_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.appeal
    ADD CONSTRAINT appeal_pkey PRIMARY KEY (appe_id);


--
-- Name: attachment attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.attachment
    ADD CONSTRAINT attachment_pkey PRIMARY KEY (atta_id);


--
-- Name: community community_comm_name_key; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.community
    ADD CONSTRAINT community_comm_name_key UNIQUE (comm_name);


--
-- Name: community community_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.community
    ADD CONSTRAINT community_pkey PRIMARY KEY (comm_id);


--
-- Name: contest contest_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.contest
    ADD CONSTRAINT contest_pkey PRIMARY KEY (cont_id);


--
-- Name: ctag_community ctag_community_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.ctag_community
    ADD CONSTRAINT ctag_community_pkey PRIMARY KEY (ctag_id, comm_id);


--
-- Name: ctag ctag_ctag_name_key; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.ctag
    ADD CONSTRAINT ctag_ctag_name_key UNIQUE (ctag_name);


--
-- Name: ctag ctag_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.ctag
    ADD CONSTRAINT ctag_pkey PRIMARY KEY (ctag_id);


--
-- Name: follow follow_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.follow
    ADD CONSTRAINT follow_pkey PRIMARY KEY (foll_followerid, foll_followeeid);


--
-- Name: language language_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_pkey PRIMARY KEY (lang_id);


--
-- Name: maintenance maintenance_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.maintenance
    ADD CONSTRAINT maintenance_pkey PRIMARY KEY (main_id);


--
-- Name: maintenance maintenance_user_id_key; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.maintenance
    ADD CONSTRAINT maintenance_user_id_key UNIQUE (user_id);


--
-- Name: notification notification_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (noti_id);


--
-- Name: permission permission_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_pkey PRIMARY KEY (perm_id);


--
-- Name: permission permission_role_id_perm_name_key; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_role_id_perm_name_key UNIQUE (role_id, perm_name);


--
-- Name: post post_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_pkey PRIMARY KEY (post_id);


--
-- Name: ptag ptag_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.ptag
    ADD CONSTRAINT ptag_pkey PRIMARY KEY (ptag_id);


--
-- Name: ptag ptag_ptag_name_key; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.ptag
    ADD CONSTRAINT ptag_ptag_name_key UNIQUE (ptag_name);


--
-- Name: ptag_puzzle ptag_puzzle_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.ptag_puzzle
    ADD CONSTRAINT ptag_puzzle_pkey PRIMARY KEY (ptag_id, puzz_id);


--
-- Name: puzzle_contest puzzle_contest_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.puzzle_contest
    ADD CONSTRAINT puzzle_contest_pkey PRIMARY KEY (puzz_id, cont_id);


--
-- Name: puzzle_language puzzle_language_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.puzzle_language
    ADD CONSTRAINT puzzle_language_pkey PRIMARY KEY (puzz_id, lang_id);


--
-- Name: puzzle puzzle_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.puzzle
    ADD CONSTRAINT puzzle_pkey PRIMARY KEY (puzz_id);


--
-- Name: reaction reaction_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.reaction
    ADD CONSTRAINT reaction_pkey PRIMARY KEY (post_id, user_id);


--
-- Name: report report_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.report
    ADD CONSTRAINT report_pkey PRIMARY KEY (repo_id);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (role_id);


--
-- Name: submission submission_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT submission_pkey PRIMARY KEY (subm_id);


--
-- Name: testcase testcase_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.testcase
    ADD CONSTRAINT testcase_pkey PRIMARY KEY (test_id);


--
-- Name: user_achievement user_achievement_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_achievement
    ADD CONSTRAINT user_achievement_pkey PRIMARY KEY (user_id, achi_id);


--
-- Name: user_community user_community_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_community
    ADD CONSTRAINT user_community_pkey PRIMARY KEY (user_id, comm_id);


--
-- Name: user_community_role user_community_role_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_community_role
    ADD CONSTRAINT user_community_role_pkey PRIMARY KEY (user_id, comm_id, role_id);


--
-- Name: user_contest user_contest_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_contest
    ADD CONSTRAINT user_contest_pkey PRIMARY KEY (user_id, cont_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: users users_user_email_key; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_user_email_key UNIQUE (user_email);


--
-- Name: users users_user_firebaseuid_key; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_user_firebaseuid_key UNIQUE (user_firebaseuid);


--
-- Name: users users_user_name_key; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_user_name_key UNIQUE (user_name);


--
-- Name: warn warn_pkey; Type: CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.warn
    ADD CONSTRAINT warn_pkey PRIMARY KEY (warn_id);


--
-- Name: idx_anno_user; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_anno_user ON public.announcement USING btree (user_id);


--
-- Name: idx_appe_user; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_appe_user ON public.appeal USING btree (user_id);


--
-- Name: idx_appe_warn; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_appe_warn ON public.appeal USING btree (warn_id);


--
-- Name: idx_atta_post; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_atta_post ON public.attachment USING btree (post_id);


--
-- Name: idx_atta_user; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_atta_user ON public.attachment USING btree (user_id);


--
-- Name: idx_cont_comm; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_cont_comm ON public.contest USING btree (comm_id);


--
-- Name: idx_ctcm_comm; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_ctcm_comm ON public.ctag_community USING btree (comm_id);


--
-- Name: idx_foll_followee; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_foll_followee ON public.follow USING btree (foll_followeeid);


--
-- Name: idx_noti_user; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_noti_user ON public.notification USING btree (noti_userid);


--
-- Name: idx_perm_role; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_perm_role ON public.permission USING btree (role_id);


--
-- Name: idx_post_comm; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_post_comm ON public.post USING btree (comm_id);


--
-- Name: idx_post_parent; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_post_parent ON public.post USING btree (post_parentid);


--
-- Name: idx_post_user; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_post_user ON public.post USING btree (user_id);


--
-- Name: idx_ptpz_puzz; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_ptpz_puzz ON public.ptag_puzzle USING btree (puzz_id);


--
-- Name: idx_puzz_comm; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_puzz_comm ON public.puzzle USING btree (comm_id);


--
-- Name: idx_puzz_user; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_puzz_user ON public.puzzle USING btree (user_id);


--
-- Name: idx_pzct_cont; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_pzct_cont ON public.puzzle_contest USING btree (cont_id);


--
-- Name: idx_pzlg_lang; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_pzlg_lang ON public.puzzle_language USING btree (lang_id);


--
-- Name: idx_reac_user; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_reac_user ON public.reaction USING btree (user_id);


--
-- Name: idx_repo_post; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_repo_post ON public.report USING btree (post_id);


--
-- Name: idx_repo_user; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_repo_user ON public.report USING btree (user_id);


--
-- Name: idx_subm_puzz; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_subm_puzz ON public.submission USING btree (puzz_id);


--
-- Name: idx_subm_user; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_subm_user ON public.submission USING btree (user_id);


--
-- Name: idx_test_puzz; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_test_puzz ON public.testcase USING btree (puzz_id);


--
-- Name: idx_uach_achi; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_uach_achi ON public.user_achievement USING btree (achi_id);


--
-- Name: idx_ucom_comm; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_ucom_comm ON public.user_community USING btree (comm_id);


--
-- Name: idx_ucon_cont; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_ucon_cont ON public.user_contest USING btree (cont_id);


--
-- Name: idx_ucrl_role; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_ucrl_role ON public.user_community_role USING btree (role_id);


--
-- Name: idx_warn_comm; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_warn_comm ON public.warn USING btree (comm_id);


--
-- Name: idx_warn_warnee; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_warn_warnee ON public.warn USING btree (warn_warneeid);


--
-- Name: idx_warn_warner; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE INDEX idx_warn_warner ON public.warn USING btree (warn_warnerid);


--
-- Name: uq_ucom_one_owner; Type: INDEX; Schema: public; Owner: avnadmin
--

CREATE UNIQUE INDEX uq_ucom_one_owner ON public.user_community USING btree (comm_id) WHERE ucom_isowner;


--
-- Name: warn trg_warn_enddate; Type: TRIGGER; Schema: public; Owner: avnadmin
--

CREATE TRIGGER trg_warn_enddate BEFORE INSERT ON public.warn FOR EACH ROW EXECUTE FUNCTION public.warn_set_enddate();


--
-- Name: achievement achievement_cont_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.achievement
    ADD CONSTRAINT achievement_cont_id_fkey FOREIGN KEY (cont_id) REFERENCES public.contest(cont_id) ON DELETE RESTRICT;


--
-- Name: announcement announcement_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.announcement
    ADD CONSTRAINT announcement_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: appeal appeal_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.appeal
    ADD CONSTRAINT appeal_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: appeal appeal_warn_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.appeal
    ADD CONSTRAINT appeal_warn_id_fkey FOREIGN KEY (warn_id) REFERENCES public.warn(warn_id);


--
-- Name: attachment attachment_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.attachment
    ADD CONSTRAINT attachment_post_id_fkey FOREIGN KEY (post_id) REFERENCES public.post(post_id) ON DELETE CASCADE;


--
-- Name: attachment attachment_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.attachment
    ADD CONSTRAINT attachment_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: contest contest_comm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.contest
    ADD CONSTRAINT contest_comm_id_fkey FOREIGN KEY (comm_id) REFERENCES public.community(comm_id);


--
-- Name: ctag_community ctag_community_comm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.ctag_community
    ADD CONSTRAINT ctag_community_comm_id_fkey FOREIGN KEY (comm_id) REFERENCES public.community(comm_id) ON DELETE CASCADE;


--
-- Name: ctag_community ctag_community_ctag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.ctag_community
    ADD CONSTRAINT ctag_community_ctag_id_fkey FOREIGN KEY (ctag_id) REFERENCES public.ctag(ctag_id) ON DELETE CASCADE;


--
-- Name: follow follow_foll_followeeid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.follow
    ADD CONSTRAINT follow_foll_followeeid_fkey FOREIGN KEY (foll_followeeid) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: follow follow_foll_followerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.follow
    ADD CONSTRAINT follow_foll_followerid_fkey FOREIGN KEY (foll_followerid) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: maintenance maintenance_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.maintenance
    ADD CONSTRAINT maintenance_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: notification notification_noti_actorid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_noti_actorid_fkey FOREIGN KEY (noti_actorid) REFERENCES public.users(user_id) ON DELETE SET NULL;


--
-- Name: notification notification_noti_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_noti_userid_fkey FOREIGN KEY (noti_userid) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: permission permission_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(role_id) ON DELETE CASCADE;


--
-- Name: post post_comm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_comm_id_fkey FOREIGN KEY (comm_id) REFERENCES public.community(comm_id) ON DELETE CASCADE;


--
-- Name: post post_post_parentid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_post_parentid_fkey FOREIGN KEY (post_parentid) REFERENCES public.post(post_id) ON DELETE CASCADE;


--
-- Name: post post_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: ptag_puzzle ptag_puzzle_ptag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.ptag_puzzle
    ADD CONSTRAINT ptag_puzzle_ptag_id_fkey FOREIGN KEY (ptag_id) REFERENCES public.ptag(ptag_id) ON DELETE CASCADE;


--
-- Name: ptag_puzzle ptag_puzzle_puzz_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.ptag_puzzle
    ADD CONSTRAINT ptag_puzzle_puzz_id_fkey FOREIGN KEY (puzz_id) REFERENCES public.puzzle(puzz_id) ON DELETE CASCADE;


--
-- Name: puzzle puzzle_comm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.puzzle
    ADD CONSTRAINT puzzle_comm_id_fkey FOREIGN KEY (comm_id) REFERENCES public.community(comm_id);


--
-- Name: puzzle_contest puzzle_contest_cont_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.puzzle_contest
    ADD CONSTRAINT puzzle_contest_cont_id_fkey FOREIGN KEY (cont_id) REFERENCES public.contest(cont_id) ON DELETE CASCADE;


--
-- Name: puzzle_contest puzzle_contest_puzz_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.puzzle_contest
    ADD CONSTRAINT puzzle_contest_puzz_id_fkey FOREIGN KEY (puzz_id) REFERENCES public.puzzle(puzz_id) ON DELETE CASCADE;


--
-- Name: puzzle_language puzzle_language_lang_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.puzzle_language
    ADD CONSTRAINT puzzle_language_lang_id_fkey FOREIGN KEY (lang_id) REFERENCES public.language(lang_id) ON DELETE RESTRICT;


--
-- Name: puzzle_language puzzle_language_puzz_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.puzzle_language
    ADD CONSTRAINT puzzle_language_puzz_id_fkey FOREIGN KEY (puzz_id) REFERENCES public.puzzle(puzz_id) ON DELETE CASCADE;


--
-- Name: puzzle puzzle_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.puzzle
    ADD CONSTRAINT puzzle_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: reaction reaction_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.reaction
    ADD CONSTRAINT reaction_post_id_fkey FOREIGN KEY (post_id) REFERENCES public.post(post_id) ON DELETE CASCADE;


--
-- Name: reaction reaction_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.reaction
    ADD CONSTRAINT reaction_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: report report_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.report
    ADD CONSTRAINT report_post_id_fkey FOREIGN KEY (post_id) REFERENCES public.post(post_id) ON DELETE CASCADE;


--
-- Name: report report_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.report
    ADD CONSTRAINT report_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: submission submission_puzz_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT submission_puzz_id_fkey FOREIGN KEY (puzz_id) REFERENCES public.puzzle(puzz_id);


--
-- Name: submission submission_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT submission_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: testcase testcase_puzz_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.testcase
    ADD CONSTRAINT testcase_puzz_id_fkey FOREIGN KEY (puzz_id) REFERENCES public.puzzle(puzz_id) ON DELETE CASCADE;


--
-- Name: user_achievement user_achievement_achi_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_achievement
    ADD CONSTRAINT user_achievement_achi_id_fkey FOREIGN KEY (achi_id) REFERENCES public.achievement(achi_id) ON DELETE CASCADE;


--
-- Name: user_achievement user_achievement_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_achievement
    ADD CONSTRAINT user_achievement_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: user_community user_community_comm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_community
    ADD CONSTRAINT user_community_comm_id_fkey FOREIGN KEY (comm_id) REFERENCES public.community(comm_id) ON DELETE CASCADE;


--
-- Name: user_community_role user_community_role_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_community_role
    ADD CONSTRAINT user_community_role_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(role_id) ON DELETE CASCADE;


--
-- Name: user_community_role user_community_role_user_id_comm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_community_role
    ADD CONSTRAINT user_community_role_user_id_comm_id_fkey FOREIGN KEY (user_id, comm_id) REFERENCES public.user_community(user_id, comm_id) ON DELETE CASCADE;


--
-- Name: user_community user_community_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_community
    ADD CONSTRAINT user_community_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: user_contest user_contest_cont_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_contest
    ADD CONSTRAINT user_contest_cont_id_fkey FOREIGN KEY (cont_id) REFERENCES public.contest(cont_id) ON DELETE CASCADE;


--
-- Name: user_contest user_contest_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.user_contest
    ADD CONSTRAINT user_contest_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: warn warn_comm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.warn
    ADD CONSTRAINT warn_comm_id_fkey FOREIGN KEY (comm_id) REFERENCES public.community(comm_id);


--
-- Name: warn warn_warn_warneeid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.warn
    ADD CONSTRAINT warn_warn_warneeid_fkey FOREIGN KEY (warn_warneeid) REFERENCES public.users(user_id);


--
-- Name: warn warn_warn_warnerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: avnadmin
--

ALTER TABLE ONLY public.warn
    ADD CONSTRAINT warn_warn_warnerid_fkey FOREIGN KEY (warn_warnerid) REFERENCES public.users(user_id);


--
-- PostgreSQL database dump complete
--

\unrestrict lQN9Ph8SvWKT99o4bw96NndIJnIo4k0dti4TUgOLfr46LV8QYIyeiIHY36rOI7h

