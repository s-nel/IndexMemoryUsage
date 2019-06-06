import cats.effect.IO
import cats.implicits._
import model._

import scala.io.Source

object DefaultHistogramParser extends HistogramParser {
  override def parse(source: Source): IO[Set[HistogramEntry]] = {
    val regex = raw"^\s*(\d+):\s+(\d+)\s+(\d+)\s+(.+)$$".r
    for {
      lines <- IO(source.getLines().drop(3))
      histogramEntries <- lines.toList
        .flatMap(line => regex.findFirstMatchIn(line).map(m => line -> m))
        .traverse[IO, HistogramEntry] {
          case (line, m) =>
            val histogramEntry = for {
              instanceCount <- Option(m.group(2))
              totalSize <- Option(m.group(3))
              className <- Option(m.group(4))
            } yield {
              HistogramEntry(instanceCount.toLong, totalSize.toLong, className)
            }
            histogramEntry match {
              case Some(histogramEntry) => IO.pure(histogramEntry)
              case None => IO.raiseError(new Throwable(s"Unparseable histogram line: [$line]"))
            }
        }
    } yield {
      histogramEntries.toSet
    }
  }
}
