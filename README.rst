Serve the MPC locally::

    $ lein ring server-headless

Run the tests::

    $ lein test

Build the docker image::

    $ docker build -t mpc .

Run the docker image::

    $ docker run -it --rm --name my-running-app mpc
