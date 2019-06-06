# Index Memory Usage

This is a small utility for indexing memory usage in Elasticsearch for analysis. Currently it only supports indexing
heap histograms from the `jmap` utility

## Usage

##### Take one or more histograms from JVM processes:

```bash
> jmap -histo <pid> > <dir>/<key>.histo
```

Where `pid` is the process ID to take the histogram of, `dir` is a common directory to put all the histograms in, and 
`key` is an identifier for the process that will be meaningful when analyzing and is unique across all histograms.

##### (optional) Start Elasticsearch and Kibana in docker

```bash
docker run -d --name es6 -p 9200:9200 docker.elastic.co/elasticsearch/elasticsearch:6.7.1
docker run -d --name kib6 --link es6:elasticsearch -p 5601:5601 docker.elastic.co/kibana/kibana:6.7.2
```

Kibana should now be running at http://localhost:5601

##### Run `IndexJMapHisto`:

```bash
> sbt "run <dir> <indexname> <elasticsearchuri> [<user> <password>]"
```

Where `dir` is the same directory above. The program will search recursively for all files ending in `.histo`. 
`indexname` is the name of the index to create. `elasticsearchuri` is the URI of the Elasticsearch cluster to index to.
If running locally in docker this is `http://localhost:9200`. `user` and `password` may optionally be specified if the
Elasticsearch cluster uses basic authentication.