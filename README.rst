.. image:: https://travis-ci.org/dativebase/mpc.svg?branch=master
    :target: https://travis-ci.org/dativebase/mpc

================================================================================
  MPC: Morphological Parser Creator
================================================================================

The MPC is the Morphological Parser Creator, a web service that can create
morphological parsers using finite-state transducers (FSTs) and N-gram language
models (LMs).



Basics
================================================================================

Serve the MPC locally::

    $ lein ring server-headless

Run the tests::

    $ lein test

Run the tests continuously and have them refresh as the code changes::

    $ lein test-refresh


Docker Stuff
================================================================================

This doesn't work currently.

Build the docker image::

    $ docker build -t mpc .

Run the docker image::

    $ docker run -it --rm --name my-running-app mpc


Database
================================================================================

MPC is using PostgreSQL as the database backend and Clojure's ragtime to manage
migrations.

To start the PostgreSQL server on a Mac::

    $ postgres -D /usr/local/var/postgres

To create the needed PostgreSQL database, make sure you have PostgreSQL
installed and then run::

    $ psql
    youruser=# CREATE USER mpc WITH PASSWORD 'mpc';
    youruser=# CREATE DATABASE mpc OWNER mpc;

To destroy the database::

    $ psql
    youruser=# DROP DATABASE mpc;

To use ragtime to apply the migrations::

    $ export MPC_DB_URI="jdbc:postgresql://localhost:5432/mpc?user=mpc&password=mpc"
    $ lein repl
    mpc.core=> (require '[mpc.migrate :refer :all])
    mpc.core=> (require '[ragtime.repl :as repl])
    (repl/migrate config)
    Applying 001-initial

To undo the migrations::

    mpc.core=> (repl/rollback config)


PostgreSQL Cheatsheet
--------------------------------------------------------------------------------

Open PostgreSQL command-line interface::

    $ psql
    youruser=#

Open PostgreSQL command-line interface and connect to ``mpc`` db::

    $ psql mpc
    mpc=#

Show databases::

    youruser=# \l
                                     List of databases
        Name    |   Owner    | Encoding |   Collate   |    Ctype    |     Access privileges
    ------------+------------+----------+-------------+-------------+---------------------------
     youruser   | youruser   | UTF8     | en_US.UTF-8 | en_US.UTF-8 |
     mpc        | mpc        | UTF8     | en_US.UTF-8 | en_US.UTF-8 |

Connect to database ``mpc``::

    youruser=# \c mpc
    You are now connected to database "mpc" as user "youruser".
    mpc=#

Show tables::

    mpc=# \dt
                    List of relations
    Schema |         Name         | Type  |  Owner
    --------+----------------------+-------+----------
    public | candidate_ranker     | table | mpc
    public | morphological_parser | table | mpc
    public | morphology           | table | mpc
    public | morphophonology      | table | mpc
    public | phonology            | table | mpc
    public | ragtime_migrations   | table | mpc

Describe the ``phonology`` table::

    mpc=# \d+ phonology
                                                 Table "public.phonology"
         Column      |          Type          | Collation | Nullable | Default | Storage  | Stats target | Description 
    -----------------+------------------------+-----------+----------+---------+----------+--------------+-------------
     id              | integer                |           | not null |         | plain    |              | 
     script          | text                   |           |          |         | extended |              | 
     compiled_script | character varying(500) |           |          |         | extended |              | 
    Indexes:
        "phonology_pkey" PRIMARY KEY, btree (id)
    Referenced by:
        TABLE "morphophonology" CONSTRAINT "morphophonology_phonology_id_fkey" FOREIGN KEY (phonology_id) REFERENCES phonology(id)

The PostgreSQL log file on a Homebrew Mac installation is at
/usr/local/var/log/postgres.log by default.


Access Database via Clojure REPL and Korma
--------------------------------------------------------------------------------

.::

    $ lein repl
    mpc.core=> (require 'korma.db)
    mpc.core=> (korma.db/defdb db (korma.db/postgres {:db "mpc"
                                                      :user "mpc"
                                                      :password "mpc"}))


cURL client examples
================================================================================

Create a phonology::

    $ curl \
    $     -H "Content-Type: application/json" \
    $     -X POST \
    $     -d "{\"script\": \"define phonology ...\"}" \
    $     http://localhost:3000/phonologies

Get all phonologies::

    $ curl http://localhost:3000/phonologies | jq


REDIS Task Queue
================================================================================

- https://github.com/ptaoussanis/carmine#message-queue
