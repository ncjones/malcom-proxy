Malcom Proxy
============

A simple CLI-based HTTP debugging proxy.


Building
--------

Building requires Java 8 and Gradle 2.7. To build:

```
gradle clean build
```

The dist tarball will be in the "build/distributions" dir.


Usage
-----

To run the HTTP proxy server:

```
$ gradle -q run
```

or, after extracting the dist tarball to the current dir:

```
$ bin/malcom
```

The server will be listening on port 8888 and will log all requests to stdout.


Known Issues
------------

HTTPS connections fail.


License
-------

GNU GPL version 3 or later.
