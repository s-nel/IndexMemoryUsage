import java.nio.file.{Files, Paths}
import java.time.{Duration, Instant}

import cats.effect.IO
import cats.implicits._
import com.sksamuel.elastic4s.cats.effect.instances._
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import model.{Histogram, InstanceData, InstanceId}
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback

import scala.collection.JavaConverters._
import scala.io.{Codec, Source}

object IndexJMapHisto {

  def main(args: Array[String]): Unit = {
    val program = for {
      path <- args.lift(0)
      indexName <- args.lift(1)
      uri <- args.lift(2)
      user = args.lift(3)
      password = args.lift(4)
    } yield {
      implicit val codec = Codec.UTF8
      val parser = DefaultHistogramParser
      for {
        paths <- IO(
          Files
            .walk(Paths.get(path))
            .filter(Files.isRegularFile(_))
            .filter(_.toString.endsWith(".histo"))
            .iterator()
            .asScala
            .toList
        )
        _ <- IO(println(s"Discovered ${paths.size} histogram files"))
        histograms <- paths.traverse[IO, Histogram] { path =>
          val source = Source.fromFile(path.toFile)
          val fileName = path.getFileName().toString()
          val instanceId = InstanceId(fileName.substring(0, fileName.indexOf('.')))
          for {
            histogramEntries <- parser.parse(source)
          } yield {
            Histogram(instanceId, histogramEntries, Set.empty)
          }
        }
        client = (user, password) match {
          case (Some(user), Some(password)) =>
            val provider = {
              val provider = new BasicCredentialsProvider()
              val credentials = new UsernamePasswordCredentials(user, password)
              provider.setCredentials(AuthScope.ANY, credentials)
              provider
            }
            val restClient = RestClient
              .builder(HttpHost.create(uri))
              .setHttpClientConfigCallback(new HttpClientConfigCallback {
                override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder) = {
                  httpClientBuilder.setDefaultCredentialsProvider(provider)
                }
              }).build()
            ElasticClient.fromRestClient(restClient)
          case _ =>
            val properties = ElasticProperties(uri)
            ElasticClient(properties)
        }

        indexer = new HistogramIndexerElastic4S[IO](client, indexName = indexName)
        startTime <- IO(Instant.now)
        _ <- histograms.traverse(indexer.index)
        endTime <- IO(Instant.now)
        duration = Duration.between(startTime, endTime)
      } yield {
        println(s"Indexed ${histograms.size} histograms in ${duration.toMillis}ms")
      }
    }

    program match {
      case Some(program) =>
        program.unsafeRunSync
      case None =>
        println("""
            |Usage:
            |
            |IndexJMapHisto <dir> <indexname> <https?://host:port> [<user> <password>]
          """.stripMargin)
    }
  }
}
