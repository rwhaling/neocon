neocon: a New (neo) Concordance Framework
=========================================

neocon is an in-memory concordance framework, using Akka, Scala, and Play.  

Overview
--------
At startup, an `IndexActor` is created, which parses and tokenizes all xml files stored at the location specified by key `neocon.basedir` in `conf/application.conf`, and stores them as an in-memory `HashMap`, with the index entries stored as sorted `TreeSet`s per-word. 
Each client maintains a persistent websocket to the server, which allocates a `SocketActor` for the lifetime of the session.  
The socket actor accepts certain meta commands, `:dir` to list loaded documents, and `:freq` for a word frequency table; otherwise, all queries are interpreted as index lookups, which are streamed out to the client in bulk.

Usage
-----
`>sbt run`

browse to localhost:9000

TODO
----
1. docker
2. evaluate phrase queries
3. parse metadata from XML
4. streaming XPath processor
5. indexed metadata queries
6. tree-walk metadata queries
7. Akka Streams for big output
8. infinite scrolling client for #7
9. Redis backend

About the name
--------------
I learned to use concordances when I was reading Plato; specifically, I was using [PhiloLogic](https://github.com/ARTFL-Project/PhiloLogic4), [Perseus](http://www.perseus.tufts.edu/hopper/), and the [Thesaurus Linguae Graecae](http://stephanus.tlg.uci.edu/) while reading Meno, Phaedrus, and the Republic. I took at least one course where I read Plato each term for the first two years of college, and by far the best teacher I had was the late [Herman Sinaiko](http://news.uchicago.edu/article/2011/10/05/herman-l-sinaiko-longtime-college-professor-and-plato-scholar-1929-2011). Sinaiko was a student of [Leo Strauss](https://en.wikipedia.org/wiki/Leo_Strauss), who taught a great number of [conservative and not-so-conservative](https://en.wikipedia.org/wiki/Leo_Strauss#Straussianism) students, including, notoriously, "architect of the Iraq War" [Paul Wolfowitz](https://en.wikipedia.org/wiki/Paul_Wolfowitz). Because of this, many of my colleagues teased me, relentlessly, about being a closet conservative, despite my claims of [Deleuzianism](https://en.wikipedia.org/wiki/Anti-Oedipus#Schizoanalysis).

That's basically the joke. Concordances are undoubetedly a "conservative" tool for textual analysis in the current mode of "big data" inflected digital humanities scholarship, and this tool is particularly conservative architecturally--although there were a smattering of in-memory concordances in the 70's and 80's, the trend has been toward on-disk indexes for quite a while. Now however, the pendulum is swinging back toward high-throughput, in-memory systems, and this project is an attempt to sketch how those techniques can be applied to old-fashioned concordance systems.
