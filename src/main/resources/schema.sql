-- Installing the required extensions
CREATE EXTENSION IF NOT EXISTS postgis;

--Creating the tables
create table
    if not exists media (
        filename varchar(513) not null,
        content_type varchar(255),
        process_status varchar(255) check (
            process_status in ('IN_QUEUE', 'PROCESSING', 'SUCCESS', 'FAIL')
        ),
        primary key (filename)
    );

create table
    if not exists post (
        post_id uuid not null,
        comments integer,
        created_at timestamp(6)
        with
            time zone,
            hearts integer,
            interactions integer,
            address integer,
            city varchar(255),
            state varchar(255),
            location geography (Point, 4326),
            owner_id numeric(38, 0),
            post_description varchar(500),
            publish boolean,
            filename varchar(513),
            primary key (post_id)
    );
    
create table
    if not exists comment (
        comment_id uuid not null,
        created_at timestamp(6)
        with
            time zone,
        replies integer,
        text varchar(500),
        top_level boolean,
        user_id numeric(38, 0),
        post_id uuid,
        primary key (comment_id)
    );

create table
    if not exists hierarchy_keeper (
        keeper_id integer not null,
        child_comment_id uuid,
        parent_comment_id uuid,
        primary key (keeper_id)
    );

create table 
    if not exists heart (
        heart_id numeric(38,0) not null,
        user_id numeric(38,0),
        post_id uuid,
        primary key (heart_id)
    );


-- Adding a QuadTree GiST index on the geometry colum for faster search
CREATE INDEX CONCURRENTLY IF NOT EXISTS locationIndex ON post USING GIST (location);

-- Adding an index on owner_id so that posts of a particular user can be fetched easily
CREATE INDEX CONCURRENTLY IF NOT EXISTS ownerIndex on post (owner_id);

-- Forcing postgres to update information about added index on geolocation to use in further queries
ANALYZE post (location);