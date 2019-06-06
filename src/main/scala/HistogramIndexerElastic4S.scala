import cats.Monad
import cats.implicits._
import com.sksamuel.elastic4s.http.{ElasticClient, Executor, Functor}
import com.sksamuel.elastic4s.http.ElasticDsl._

class HistogramIndexerElastic4S[F[_]: Executor: Monad: Functor](client: ElasticClient, indexName: String = "memory-usage")
  extends HistogramIndexer[F] {
  override def index(histogram: model.Histogram): F[Unit] = {
    for {
      _ <- client.execute {
        createIndex(indexName).mappings(
          mapping("doc").fields(
            longField("instanceCount"),
            longField("totalSize"),
            keywordField("className"),
            keywordField("instanceId"),
            floatField("sizePerInstance")
          )
        )
      }
      requests = histogram.entries.toList.map { histogramEntry =>
        indexInto(indexName / "doc").fields(
          Map(
            "instanceCount" -> histogramEntry.instanceCount,
            "totalSize" -> histogramEntry.totalSize,
            "className" -> histogramEntry.className,
            "instanceId" -> histogram.instanceId.value,
            "sizePerInstance" -> histogramEntry.sizePerInstance()
          ) ++
            histogram.instanceData.map(d => d.key -> d.value).toMap
        )
      }
      _ <- client.execute {
        bulk(requests)
      }
    } yield {
      ()
    }
  }
}
