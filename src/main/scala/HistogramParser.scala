import cats.effect.IO
import model.HistogramEntry

import scala.io.Source

trait HistogramParser {
  def parse(source: Source): IO[Set[HistogramEntry]]
}